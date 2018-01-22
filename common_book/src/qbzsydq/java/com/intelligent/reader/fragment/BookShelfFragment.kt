package com.intelligent.reader.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.widget.SimpleItemAnimator
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dycm_adsdk.PlatformSDK
import com.dycm_adsdk.callback.AbstractCallback
import com.dycm_adsdk.callback.ResultCode
import com.intelligent.reader.BuildConfig
import com.intelligent.reader.R
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
import kotlinx.android.synthetic.main.layout_head.view.*
import kotlinx.android.synthetic.qbzsydq.fragment_bookshelf.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.component.service.CheckNovelUpdateService
import net.lzbook.kit.book.component.service.DownloadService
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

/**
 * 书架页Fragment
 */
class BookShelfFragment : Fragment(), UpdateCallBack, BookShelfView {

    private val presenter: BookShelfPresenter by lazy { BookShelfPresenter(this) }

    val bookShelfReAdapter: BookShelfReAdapter by lazy {
        BookShelfReAdapter(activity, presenter.iBookList, presenter.aDViews,
                BookShelfReAdapter.ShelfItemClickListener { _, position ->
                    AppLog.e(TAG, "BookShelfItemClick")
                    if (position < 0 || position > presenter.iBookList.size) {
                        return@ShelfItemClickListener
                    }
                    if (bookShelfRemoveHelper.isRemoveMode) {
                        bookShelfRemoveHelper.setCheckPosition(position)
                    } else {
                        AppLog.e(TAG, "intoNovelContent")
                        if (position >= presenter.iBookList.size || position < 0) return@ShelfItemClickListener
                        val book = presenter.iBookList[position]
                        handleBook(book)
                        presenter.uploadItemClickLog(position)
                    }
                },
                BookShelfReAdapter.ShelfItemLongClickListener { _, _ ->
                    if (!bookShelfRemoveHelper.isRemoveMode) {
                        bookShelfRemoveHelper.showRemoveMenu(bookshelf_refresh_view)
                        presenter.uploadItemLongClickLog()
                    }
                }, true)
    }

    val bookShelfRemoveHelper: BookShelfRemoveHelper by lazy {
        val helper = BookShelfRemoveHelper(activity, bookShelfReAdapter)
        helper.setLayout(bookshelf_refresh_view)
        helper.setOnMenuStateListener(object : BookShelfRemoveHelper.OnMenuStateListener {
            override fun getMenuShownState(isShown: Boolean) {
                AppLog.e(TAG, "getMenuShowState: $isShown")
                if (isShown) {
                    bookshelf_refresh_view.setPullToRefreshEnabled(false)
                } else {
                    if (presenter.iBookList.isNotEmpty()) {
                        bookshelf_refresh_view.setPullToRefreshEnabled(true)
                    }
                    updateUI()
                }
                fragmentCallback.getMenuShownState(isShown)
            }

            override fun getAllCheckedState(isAll: Boolean) {
                fragmentCallback.getAllCheckedState(isAll)
            }

            override fun doHideAd() {
                if (isShowAD) {
                    presenter.removeAd()
                }
            }

        })
        helper.setOnMenuDeleteListener(BookShelfRemoveHelper.OnMenuDeleteClickListener { checked_state ->
            if (checked_state.isEmpty()) return@OnMenuDeleteClickListener
            val checkedBooks = ArrayList<Book>()
            val size = presenter.iBookList.size
            (0 until size).filter {
                checked_state.contains(it)
            }.mapTo(checkedBooks) {
                presenter.iBookList[it]
            }
            bookDeleteDialog.show(checkedBooks)
        })
        helper
    }

    private val fragmentCallback: BaseFragment.FragmentCallback by lazy { activity as BaseFragment.FragmentCallback }
    private val bookSensitiveWords: ArrayList<String> = ArrayList()
    private val noBookSensitive = false

    private var frameBookHelper: FrameBookHelper? = null
    private var bookDaoHelper: BookDaoHelper = BookDaoHelper.getInstance()
    private var isShowAD = false
    private var bookRackUpdateTime: Long = 0
    private var latestLoadDataTime: Long = 0

    private val headerView: View by lazy {
        LayoutInflater.from(bookshelf_refresh_view.context)
                .inflate(R.layout.layout_head, null)
    }

    private val sharedPreferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
    }

    private val bookDeleteDialog: BookDeleteDialog by lazy {
        val dialog = BookDeleteDialog(activity)
        dialog.setOnConfirmListener { books ->
            if (books != null && books.isNotEmpty()) presenter.deleteBooks(books)
            StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.bs_click_delete_ok_btn)
        }
        dialog.setOnAbrogateListener {
            presenter.uploadBookDeleteCancelLog()
            StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.bs_click_delete_cancel_btn)
        }
        dialog
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_bookshelf, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
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
        //悬浮广告 1-2
        PlatformSDK.adapp().dycmNativeAd(activity, "1-2", book_shelf_ad, object : AbstractCallback() {
            override fun onResult(adswitch: Boolean, views: List<ViewGroup>?, jsonResult: String?) {
                super.onResult(adswitch, views, jsonResult)
                if (!adswitch) return
                try {
                    val jsonObject = JSONObject(jsonResult)
                    if (jsonObject.has("state_code")) {
                        when (ResultCode.parser(jsonObject.getInt("state_code"))) {
                            ResultCode.AD_REQ_SUCCESS -> {
                                book_shelf_ad.addView(views?.get(0))
                                book_shelf_ad.postInvalidate()
                            }
                            ResultCode.AD_REQ_FAILED -> {
                            }
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        fragmentCallback.frameHelper()
        if (activity is HomeActivity) {
            frameBookHelper = (activity as HomeActivity).frameHelper
        }

        frameBookHelper?.setDownNotify {
            updateUI()
        }

        presenter.clickNotification(activity.intent)

        initDownloadService()
        initUpdateService()

        //根据书架数量确定是否刷新
        if (presenter.iBookList.size > 0) {
            bookshelf_refresh_view.isRefreshing = true
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }


    override fun onDetach() {
        super.onDetach()
        try {
            val childFragmentManager = Fragment::class.java.getDeclaredField("mChildFragmentManager")
            childFragmentManager.isAccessible = true
            childFragmentManager.set(this, null)

        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
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
        val isShowAd = !bookShelfRemoveHelper.isRemoveMode && isResumed
        doAsync {
            presenter.queryBookListAndAd(activity, isShowAd)
            runOnMain {
                bookShelfReAdapter.setUpdate_table(presenter.filterUpdateTableList())
                bookShelfReAdapter.notifyDataSetChanged()
                presenter.uploadFirstOpenLog(sharedPreferences)
            }
        }
    }

    override fun onBookListQuery(bookList: ArrayList<Book>) {
        if (bookList.isEmpty()) {
            bookshelf_refresh_view?.setPullToRefreshEnabled(false)
            bookshelf_empty?.visibility = View.VISIBLE
        } else {
            bookshelf_refresh_view?.setPullToRefreshEnabled(true)
            bookshelf_empty?.visibility = View.GONE
        }
    }

    override fun onSuccess(result: BookUpdateResult) {
        latestLoadDataTime = System.currentTimeMillis()
        bookRackUpdateTime = System.currentTimeMillis()
        if (bookshelf_refresh_view != null) {
            bookshelf_refresh_view!!.onRefreshComplete()
        }
        presenter.handleSuccessUpdate(result)
        AppUtils.setLongPreferences(activity, "book_rack_update_time", bookRackUpdateTime)
        AppLog.e(TAG, "onSuccess的刷新ui调用")
        isShowAD = true
        updateUI()
    }

    override fun onException(e: Exception) {
        latestLoadDataTime = System.currentTimeMillis()
        showToastDelay(R.string.bookshelf_refresh_network_problem)
        if (bookshelf_refresh_view != null) {
            bookshelf_refresh_view.onRefreshComplete()
        }
    }

    override fun onSuccessUpdateHandle(updateCount: Int, firstBook: BookUpdate?) {
        if (updateCount == 0) {
            showToastDelay(R.string.main_update_no_new)
        } else {
            val bookName = firstBook?.book_name
            val bookLastChapterName = firstBook?.last_chapter_name
            if (bookName?.isNotEmpty() == true && bookLastChapterName?.isNotEmpty() == true) {
                if (updateCount == 1) {
                    showToastDelay("《$bookName${activity.getString(R.string.bookshelf_one_book_update)}" +
                            "$bookLastChapterName")
                } else {
                    showToastDelay("《$bookName${activity.getString(R.string.bookshelf_more_book_update)}" +
                            "$updateCount${activity.getString(R.string.bookshelf_update_chapters)}")
                }
            }
        }
    }

    fun doUpdateBook() {
        presenter.addUpdateTask(this)
    }

    override fun doUpdateBook(updateService: CheckNovelUpdateService) {
        updateService.setBookUpdateListener(activity as CheckNovelUpdateService.OnBookUpdateListener)
        presenter.addUpdateTask(this)
    }

    override fun notification(gid: String) {
        if (!TextUtils.isEmpty(gid)) {
            val book = bookDaoHelper.getBook(gid, 0) as Book
            handleBook(book)
        }
    }

    override fun onBookDelete() {
        AppLog.e(TAG, "onBookDelete")
        updateUI()
        bookShelfRemoveHelper.dismissRemoveMenu()
    }

    override fun onAdRefresh() {
        bookShelfReAdapter.notifyDataSetChanged()
    }

    private fun initDownloadService() {
        if (presenter.downloadService != null) return
        val intent = Intent()
        val context = activity.applicationContext
        intent.setClass(context, DownloadService::class.java)
        context.startService(intent)
        context.bindService(intent, presenter.downloadConnection, Context.BIND_AUTO_CREATE)
    }

    private fun initUpdateService() {
        if (presenter.updateService != null) return
        val intent = Intent()
        val context = activity.applicationContext
        intent.setClass(context, CheckNovelUpdateService::class.java)
        context.startService(intent)
        context.bindService(intent, presenter.updateConnection, Context.BIND_AUTO_CREATE)
    }

    private fun initRecyclerView() {
        bookshelf_refresh_view.setHeaderViewBackgroundColor(0x00000000)
        bookshelf_refresh_view.setHeaderView(createHeaderView())
        bookshelf_refresh_view.isTargetScrollWithLayout = true
        recycler_view.recycledViewPool.setMaxRecycledViews(0, 12)
        val layoutManager = ShelfGridLayoutManager(activity, 1)
        recycler_view.layoutManager = layoutManager
        //        recyclerView.getItemAnimator().setSupportsChangeAnimations(false);
        recycler_view.itemAnimator.addDuration = 0
        recycler_view.itemAnimator.changeDuration = 0
        recycler_view.itemAnimator.moveDuration = 0
        recycler_view.itemAnimator.removeDuration = 0
        (recycler_view.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        recycler_view.adapter = bookShelfReAdapter
    }

    private fun createHeaderView(): View {
        headerView.head_text_view.text = "下拉刷新"
        headerView.head_image_view.visibility = View.VISIBLE
        headerView.head_image_view.setImageResource(R.drawable.pulltorefresh_down_arrow)
        headerView.head_pb_view.visibility = View.GONE
        return headerView
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
            AppLog.d(TAG, "刷新间隔小于30秒不请求数据")
            showToastDelay(R.string.main_update_no_new)
        } else {
            // 刷新间隔大于30秒直接请求更新，
            presenter.addUpdateTask(this)
            AppLog.d(TAG, "刷新间隔大于30秒请求数据")
        }

    }

    /**
     * 处理被点击或更新通知的book
     */
    private fun handleBook(book: Book) {
        AppLog.e(TAG, "handleBook")
        if (!TextUtils.isEmpty(book.book_id) && book.book_type == 0) {
            presenter.resetUpdateStatus(book.book_id)
        }

        if (Constants.isShielding && !noBookSensitive && bookSensitiveWords.contains(book.book_id.toString())) {
            ToastUtils.showToastNoRepeat("抱歉，该小说已下架！")
        } else {
            BookHelper.goToCoverOrRead(activity.applicationContext, activity, book, 0)
            AppLog.e(TAG, "goToCoverOrRead")
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

    companion object {

        val ACTION_CHKHIDE = AppUtils.getPackageName()
        private val PULL_REFRESH_DELAY = 30 * 1000
        private val TAG = BookShelfFragment::class.java.simpleName
    }
}
