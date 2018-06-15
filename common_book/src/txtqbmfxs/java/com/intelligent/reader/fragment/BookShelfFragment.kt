package com.intelligent.reader.fragment

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.widget.SimpleItemAnimator
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.dingyue.downloadmanager.DownloadManagerActivity
import com.dycm_adsdk.PlatformSDK
import com.dycm_adsdk.callback.AbstractCallback
import com.dycm_adsdk.callback.ResultCode
import com.intelligent.reader.BuildConfig
import com.intelligent.reader.R
import com.intelligent.reader.activity.DownloadErrorActivity
import com.intelligent.reader.activity.HomeActivity
import com.intelligent.reader.adapter.BookShelfReAdapter
import com.intelligent.reader.app.BookApplication
import com.intelligent.reader.presenter.bookshelf.BookShelfPresenter
import com.intelligent.reader.presenter.bookshelf.BookShelfView
import com.intelligent.reader.read.help.BookHelper
import com.intelligent.reader.util.BookShelfRemoveHelper
import com.intelligent.reader.util.ShelfGridLayoutManager
import com.intelligent.reader.view.BookDeleteDialog
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.layout_head.*
import kotlinx.android.synthetic.main.layout_head.view.*
import kotlinx.android.synthetic.txtqbmfxs.fragment_bookshelf.*
import net.lzbook.kit.ad.AdTag
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.component.service.CheckNovelUpdateService
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.UpdateCallBack
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.BookUpdate
import net.lzbook.kit.data.bean.BookUpdateResult
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.pulllist.SuperSwipeRefreshLayout
import net.lzbook.kit.utils.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * Function：书架页Fragment
 *
 * Created by JoannChen on 2018/5/2 0002 11:08
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
class BookShelfFragment : Fragment(), UpdateCallBack, FrameBookHelper.BookUpdateService,
        FrameBookHelper.DownLoadStateCallback, FrameBookHelper.DownLoadNotify,
        FrameBookHelper.NotificationCallback, BookShelfRemoveHelper.OnMenuDeleteClickListener,
        BookShelfRemoveHelper.OnMenuStateListener, FrameBookHelper.BookChanged,
        BookShelfView {

    private var isUpdateUi = false
    var container: ViewGroup? = null

    override fun onAdRefresh() {
        if (activity != null && !activity.isFinishing) {
            bookShelfReAdapter.notifyDataSetChanged()
        }
    }

    val presenter: BookShelfPresenter by lazy { BookShelfPresenter(this) }

    val bookShelfReAdapter: BookShelfReAdapter by lazy {

        BookShelfReAdapter(activity, presenter.iBookList, presenter.aDViews, true,
                BookShelfReAdapter.ShelfItemClickListener { _, position ->
                    AppLog.e("BookShelfItemClick")
                    if (position < 0 || position > presenter.iBookList.size) {
                        return@ShelfItemClickListener
                    }

                    if (bookShelfRemoveHelper.isRemoveMode) {
                        bookShelfRemoveHelper.setCheckPosition(position)
                    } else {
                        AppLog.e("intoNovelContent")
                        if (position >= presenter.iBookList.size || position < 0) return@ShelfItemClickListener
                        val book = presenter.iBookList[position]
                        handleBook(book)
                        presenter.uploadItemClickLog(position)
                    }

                },
                BookShelfReAdapter.ShelfItemLongClickListener { _, _ ->
                    if (!bookShelfRemoveHelper.isRemoveMode) {

                        /*
                        Joann
                        this.bookShelfRemoveHelper.showRemoveMenu2(bookshelf_refresh_view, position)*/
                        this.bookShelfRemoveHelper.showRemoveMenu(bookshelf_refresh_view)
                        if (bookshelf_float_ad != null) {
                            bookshelf_float_ad.visibility = View.GONE
                        }
                        presenter.uploadItemLongClickLog()
                    }

                })

    }
    val bookShelfRemoveHelper: BookShelfRemoveHelper by lazy {
        val helper = BookShelfRemoveHelper(activity, bookShelfReAdapter)
        helper.setLayout(bookshelf_refresh_view)
        helper.setOnMenuStateListener(this)
        helper.setOnMenuDeleteListener(this)
        helper
    }

    private val fragmentCallback: BaseFragment.FragmentCallback by lazy { activity as BaseFragment.FragmentCallback }
    private val bookSensitiveWords: ArrayList<String> = ArrayList()
    private val noBookSensitive = false

    private var frameBookHelper: FrameBookHelper? = null
    private var bookDaoHelper: BookDaoHelper = BookDaoHelper.getInstance()

    private var bookRackUpdateTime: Long = 0
    private var latestLoadDataTime: Long = 0
    private var sharedPreferences: SharedPreferences? = null
    private var headPbView: ProgressBar? = null
    private var headTextView: TextView? = null
    private var headImageView: ImageView? = null

    private val bookDeleteDialog: BookDeleteDialog by lazy {
        val dialog = BookDeleteDialog(activity)
        dialog.setOnConfirmListener { books, isChecked ->
            if (books != null && books.isNotEmpty())
                presenter.deleteBooks(books, isChecked, activity)
            StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.bs_click_delete_ok_btn)
        }
        dialog.setOnAbrogateListener {
            presenter.uploadBookDeleteCancelLog()
            StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.bs_click_delete_cancel_btn)
        }
        dialog
    }

    private val headerView: View by lazy {
        LayoutInflater.from(bookshelf_refresh_view.context)
                .inflate(R.layout.layout_head, null)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.container = container
        return inflater?.inflate(R.layout.fragment_bookshelf, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {

        if (PlatformSDK.config() != null) {
            PlatformSDK.config().setBookShelfGrid(false)
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        bookRackUpdateTime = AppUtils.getLongPreferences(activity, "book_rack_update_time", System.currentTimeMillis())
        fragmentCallback.getRemoveMenuHelper(bookShelfRemoveHelper)
        initRecyclerView()
        bookshelf_refresh_view.setOnPullRefreshListener(object : SuperSwipeRefreshLayout.OnPullRefreshListener {
            override fun onRefresh() {
                headerView.head_text_view.text = "正在刷新"
                headerView.head_image_view.visibility = View.GONE
                headerView.head_pb_view.visibility = View.VISIBLE
                checkBookUpdate()
            }

            override fun onPullDistance(distance: Int) {}

            override fun onPullEnable(enable: Boolean) {
                headerView.head_pb_view.visibility = View.GONE
                headerView.head_text_view.text = if (enable) "松开刷新" else "下拉刷新"
                headerView.head_image_view.visibility = View.VISIBLE
                headerView.head_image_view.rotation = (if (enable) 180 else 0).toFloat()
            }
        })
        bookshelf_empty_btn.setOnClickListener {
            fragmentCallback.setSelectTab(1)
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SHELF_PAGE, StartLogClickUtil.TOBOOKCITY)
        }
        // 下载缓存按钮
        fab_goto_down_act.setOnClickListener {
            IntentUtils.start(activity, DownloadManagerActivity::class.java, false)

        }
    }


    private fun initRecyclerView() {
        bookshelf_refresh_view.setHeaderViewBackgroundColor(0x00000000)
        bookshelf_refresh_view.setHeaderView(createHeaderView())
        bookshelf_refresh_view.isTargetScrollWithLayout = true
        recycler_view.recycledViewPool.setMaxRecycledViews(0, 12)
        val layoutManager = ShelfGridLayoutManager(activity, 1)
        recycler_view.layoutManager = layoutManager
        recycler_view.isFocusable = false//放弃焦点
        //        recyclerView.getItemAnimator().setSupportsChangeAnimations(false);
        recycler_view.itemAnimator.addDuration = 0
        recycler_view.itemAnimator.changeDuration = 0
        recycler_view.itemAnimator.moveDuration = 0
        recycler_view.itemAnimator.removeDuration = 0
        (recycler_view.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        recycler_view.adapter = bookShelfReAdapter
    }

    private fun createHeaderView(): View {
        headerView.head_text_view?.text = "下拉刷新"
        headerView.head_image_view?.visibility = View.VISIBLE
        headerView.head_image_view?.setImageResource(R.drawable.pulltorefresh_down_arrow)
        headerView.head_pb_view?.visibility = View.GONE
        return headerView
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        fragmentCallback.frameHelper()
        if (activity is HomeActivity) {
            frameBookHelper = (activity as HomeActivity).frameHelper
        }

        frameBookHelper?.setBookUpdate(this)
        frameBookHelper?.setDownLoadState(this)
        frameBookHelper?.setDownNotify(this)
        frameBookHelper?.setNotification(this)
        frameBookHelper?.initDownUpdateService()
        frameBookHelper?.clickNotification(activity.intent)
        frameBookHelper?.setBookChanged(this)

        //根据书架数量确定是否刷新
        if (presenter.iBookList.size > 0) {
            bookshelf_refresh_view.isRefreshing = true
        }
    }

    override fun onResume() {
        super.onResume()
        isUpdateUi = true
        updateUI()
        getFloatAd(activity)
    }

    override fun onPause() {
        super.onPause()
        isUpdateUi = false
    }

    override fun onDetach() {
        super.onDetach()
        try {
            val childFragmentManager = Fragment::class.java.getDeclaredField("mChildFragmentManager")
            childFragmentManager.isAccessible = true
            childFragmentManager.set(this, null)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (BuildConfig.DEBUG) {
            BookApplication.getRefWatcher().watch(this)
        }
        frameBookHelper?.recycleCallback()
        presenter.iBookList.clear()
    }

    /**
     * 查Book数据库更新界面
     */
    fun updateUI() {
        if (!isUpdateUi) return
        runOnMain {
            if (activity != null && !activity.isFinishing) {
                val isShowAd = !bookShelfRemoveHelper.isRemoveMode && isResumed && !Constants.isHideAD && Constants.book_shelf_state != 0

                AppLog.d("isShowAd", "bookShelfRemoveHelper：" + !bookShelfRemoveHelper.isRemoveMode)
                AppLog.d("isShowAd", "isResumed：" + isResumed)
                AppLog.d("isShowAd", "Constants.isHideAD：" + !Constants.isHideAD)
                AppLog.d("isShowAd", "Constants.book_shelf_state：" + Constants.book_shelf_state)

                presenter.queryBookListAndAd(activity, isShowAd)
                bookShelfReAdapter.setUpdate_table(presenter.filterUpdateTableList())
                bookShelfReAdapter.notifyDataSetChanged()

                if (sharedPreferences != null) {
                    presenter.uploadFirstOpenLog(sharedPreferences!!)
                }

            }
        }
    }

    override fun onBookListQuery(bookList: ArrayList<Book>) {
        if (activity != null && !activity.isFinishing) {
            if (bookList.isEmpty()) {
                if (bookshelf_refresh_view != null) {
                    bookshelf_refresh_view.setPullToRefreshEnabled(false)
                }
                if (bookshelf_empty != null) {
                    bookshelf_empty.visibility = View.VISIBLE
                }
                if (item_ad_layout != null) {
                    item_ad_layout.visibility = View.GONE
                }

            } else {
                if (bookshelf_refresh_view != null) {
                    bookshelf_refresh_view.setPullToRefreshEnabled(true)
                }
                if (bookshelf_empty != null) {
                    bookshelf_empty.visibility = View.GONE
                }
            }
        }

    }

    /**
     * 下拉时检查更新
     */
    private fun checkBookUpdate() {

        if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
            bookshelf_refresh_view.isRefreshing = false
            showToastDelay(R.string.bookshelf_refresh_network_problem)
            return
        }

        val startPullTime = System.currentTimeMillis()
        val interval = Math.abs(startPullTime - latestLoadDataTime)

        // 刷新间隔小于30秒无效
        if (interval <= PULL_REFRESH_DELAY) {
            bookshelf_refresh_view.onRefreshComplete()
            AppLog.d("刷新间隔小于30秒不请求数据")
            showToastDelay(R.string.main_update_no_new)
        } else {
            // 刷新间隔大于30秒直接请求更新，
            addUpdateTask()
            AppLog.d("刷新间隔大于30秒请求数据")
        }

    }

    private fun addUpdateTask() {
        val updateService = frameBookHelper?.updateService
        if (bookDaoHelper.booksCount > 0 && updateService != null) {
            val list = bookDaoHelper.booksList
            AppLog.e("BookUpdateCount", "BookUpdateCount: " + list.size)
            updateService.checkUpdate(BookHelper.getBookUpdateTaskData(list, this))
        }
    }

    private fun showToastDelay(textId: Int) {
        if (!isAdded) return
        Flowable.timer(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (activity != null && !activity.isFinishing) {
                        activity.applicationContext.toastShort(textId)
                    }
                }
    }

    private fun showToastDelay(text: String) {
        if (!isAdded) return
        Flowable.timer(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (activity != null && !activity.isFinishing) {
                        activity.applicationContext.toastShort(text)
                    }
                }
    }

    /**
     * 处理被点击或更新通知的book
     */
    private fun handleBook(book: Book?) {
        AppLog.e("handleBook")
        if (book != null && activity != null && !activity.isFinishing) {
            if (!TextUtils.isEmpty(book.book_id) && book.book_type == 0) {
                presenter.resetUpdateStatus(book.book_id)
            }

            if (Constants.isShielding && !noBookSensitive && bookSensitiveWords.contains(book.book_id.toString())) {
                ToastUtils.showToastNoRepeat("抱歉，该小说已下架！")
            } else {
                // 跳转小说封面页或者小说阅读页
                BookHelper.goToCoverOrRead(activity.applicationContext, activity, book, 0)
                AppLog.e("goToCoverOrRead")

            }
        }

    }

    override fun onSuccess(result: BookUpdateResult) {
        if (activity != null && !activity.isFinishing) {
            latestLoadDataTime = System.currentTimeMillis()
            bookRackUpdateTime = System.currentTimeMillis()
            if (bookshelf_refresh_view != null) {
                bookshelf_refresh_view.onRefreshComplete()
            }
            presenter.handleSuccessUpdate(result)

            AppUtils.setLongPreferences(activity, "book_rack_update_time", bookRackUpdateTime)
            AppLog.e("onSuccess的刷新ui调用")
//            updateUI()


        }

    }

    override fun onException(e: Exception) {
        if (activity != null && !activity.isFinishing) {
            latestLoadDataTime = System.currentTimeMillis()
            showToastDelay(R.string.bookshelf_refresh_network_problem)
            if (bookshelf_refresh_view != null) {
                bookshelf_refresh_view.onRefreshComplete()
            }
        }
    }

    override fun onSuccessUpdateHandle(updateCount: Int, firstBook: BookUpdate?) {
        if (updateCount == 0) {
            showToastDelay(R.string.main_update_no_new)
        } else {
            if (firstBook != null) {
                val bookName = firstBook.book_name
                val bookLastChapterName = firstBook.last_chapter_name
                if (bookName?.isNotEmpty() == true && bookLastChapterName?.isNotEmpty() == true) {
                    if (updateCount == 1) {
                        showToastDelay("《$bookName${activity.getString(R.string.bookshelf_one_book_update)}" + bookLastChapterName)
                    } else {
                        showToastDelay("《$bookName${activity.getString(R.string.bookshelf_more_book_update)}" +
                                "$updateCount${activity.getString(R.string.bookshelf_update_chapters)}")
                    }
                }
            }
        }
    }

    fun doUpdateBook() {
        addUpdateTask()
    }

    override fun doUpdateBook(updateService: CheckNovelUpdateService) {

        updateService.setBookUpdateListener(activity as CheckNovelUpdateService.OnBookUpdateListener)
        addUpdateTask()

    }

    override fun changeDownLoadBtn(isDownLoading: Boolean) {}

    override fun doNotifyDownload() {
        updateUI()
    }

    override fun notification(gid: String) {
        if (!TextUtils.isEmpty(gid)) {
            val book = bookDaoHelper.getBook(gid, 0)
            handleBook(book)
        }
    }

    override fun onMenuDelete(checked_state: HashSet<Int>) {
        if (checked_state.isEmpty()) return
        val checkedBooks = ArrayList<Book>()

        val size = presenter.iBookList.size
        (0 until size).filter {
            checked_state.contains(it)
        }.mapTo(checkedBooks) {
            presenter.iBookList[it]
        }

        if (checkedBooks.isNotEmpty()) {
            bookDeleteDialog.show(checkedBooks)
        }
    }

    override fun onBookDelete() {
        if (activity != null && !activity.isFinishing) {
            updateUI()
            bookShelfRemoveHelper.dismissRemoveMenu()
        }

    }

    override fun getMenuShownState(state: Boolean) {
        AppLog.e("getMenuShowState: $state")
        if (state) {
            if (bookshelf_refresh_view != null) {
                bookshelf_refresh_view.setPullToRefreshEnabled(false)
            }
        } else {
            if (presenter.iBookList.isNotEmpty()) {
                if (bookshelf_refresh_view != null) {
                    bookshelf_refresh_view.setPullToRefreshEnabled(true)
                }
                updateUI()
                getFloatAd(activity)
            }

        }
        fragmentCallback.getMenuShownState(state)
    }

    override fun getAllCheckedState(isAll: Boolean) {
        fragmentCallback.getAllCheckedState(isAll)
    }

    override fun doHideAd() {
        presenter.removeAd()
    }

    override fun updateBook() {
        AppLog.e("updateBook的刷新UI调用")
        updateUI()
    }

    /**
     *  GridList顶部横条广告位
     */
    override fun showShelfTopItem(shouldShow: Boolean) {
        if (shouldShow) {
            PlatformSDK.adapp().dycmNativeAd(activity, AdTag.SHELF_BOOK_1_1, null, object : AbstractCallback() {
                override fun onResult(adswitch: Boolean, views: List<ViewGroup>, jsonResult: String?) {
                    super.onResult(adswitch, views, jsonResult)
                    if (!adswitch) {
                        return
                    }
                    try {
                        val jsonObject = JSONObject(jsonResult)
                        if (jsonObject.has("state_code")) {
                            when (ResultCode.parser(jsonObject.getInt("state_code"))) {
                                ResultCode.AD_REQ_SUCCESS
                                -> {
                                    runOnMain {
                                        if (item_ad_layout != null) {
                                            item_ad_layout.removeAllViews()
                                            if (views.isNotEmpty()) {
                                                item_ad_layout.addView(views[0])
                                                item_ad_layout.visibility = View.VISIBLE
                                            } else {
                                                item_ad_layout.visibility = View.GONE
                                            }
                                        }
                                    }
                                }
                                else -> {
                                    runOnMain {
                                        if (item_ad_layout != null) {
                                            item_ad_layout.visibility = View.GONE
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            })

        } else {
            runOnMain {
                if (item_ad_layout != null) {
                    item_ad_layout.visibility = View.GONE
                }
            }
        }
    }

    override fun showShelfFloatAd(shouldShow: Boolean, showView: ViewGroup?) {
        if (shouldShow) {
            if (bookshelf_float_ad != null && showView != null) {
                bookshelf_float_ad.visibility = View.VISIBLE
                bookshelf_float_ad.removeAllViews()
                bookshelf_float_ad.addView(showView)
            }
        } else {
            if (bookshelf_float_ad != null) {
                bookshelf_float_ad.visibility = View.GONE
            }
        }

    }

    /**
     * 获取悬浮广告位
     */
    private fun getFloatAd(activity: Activity) {
        if (!isUpdateUi) return
        if (!Constants.isHideAD && Constants.dy_shelf_boundary_switch && presenter.iBookList.isNotEmpty()) {
            if (PlatformSDK.adapp() != null) {
                PlatformSDK.adapp().dycmNativeAd(activity, AdTag.SHELF_FLOAT_1_2, null, object : AbstractCallback() {
                    override fun onResult(adSwitch: Boolean, views: List<ViewGroup>?, jsonResult: String?) {
                        super.onResult(adSwitch, views, jsonResult)
                        if (!adSwitch) return
                        try {
                            val jsonObject = JSONObject(jsonResult)
                            if (jsonObject.has("state_code")) {
                                runOnMain {
                                    when (ResultCode.parser(jsonObject.getInt("state_code"))) {
                                        ResultCode.AD_REQ_SUCCESS -> {
                                            if (views != null && views.isNotEmpty()) {
                                                if (bookshelf_float_ad != null) {
                                                    bookshelf_float_ad.visibility = View.VISIBLE
                                                    bookshelf_float_ad.removeAllViews()
                                                    bookshelf_float_ad.addView(views[0])
                                                }
                                            }
                                        }
                                        ResultCode.AD_REQ_FAILED -> {
                                            if (bookshelf_float_ad != null) {
                                                bookshelf_float_ad.visibility = View.GONE
                                            }
                                        }
                                        else -> {
                                        }
                                    }
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                })
            }


        }

    }


    companion object {
        val ACTION_CHKHIDE = AppUtils.getPackageName()
        private val PULL_REFRESH_DELAY = 30 * 1000
        /*   private val TAG = BookShelfFragment::class.java.simpleName*/
    }
}

