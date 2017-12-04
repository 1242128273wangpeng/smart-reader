package com.intelligent.reader.fragment

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
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.dingyueads.sdk.Native.YQNativeAdInfo
import com.intelligent.reader.BuildConfig
import com.intelligent.reader.R
import com.intelligent.reader.activity.DownloadManagerActivity
import com.intelligent.reader.activity.HomeActivity
import com.intelligent.reader.adapter.BookShelfReAdapter
import com.intelligent.reader.app.BookApplication
import com.intelligent.reader.presenter.bookshelf.BookShelfPresenter
import com.intelligent.reader.presenter.bookshelf.BookShelfView
import com.intelligent.reader.read.help.BookHelper
import com.intelligent.reader.util.BookShelfRemoveHelper
import com.intelligent.reader.util.ShelfGridLayoutManager
import com.intelligent.reader.view.BookDeleteDialog
import de.greenrobot.event.EventBus
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.qbmfxsydq.fragment_bookshelf.*
import net.lzbook.kit.ad.OwnNativeAdManager
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.component.service.CheckNovelUpdateService
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.UpdateCallBack
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.BookUpdate
import net.lzbook.kit.data.bean.BookUpdateResult
import net.lzbook.kit.data.bean.EventBookshelfAd
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.pulllist.SuperSwipeRefreshLayout
import net.lzbook.kit.utils.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 书架页Fragment
 */
class BookShelfFragment : Fragment(), UpdateCallBack, FrameBookHelper.BookUpdateService,
        FrameBookHelper.DownLoadStateCallback, FrameBookHelper.DownLoadNotify,
        FrameBookHelper.NotificationCallback, BookShelfRemoveHelper.OnMenuDeleteClickListener,
        BookShelfRemoveHelper.OnMenuStateListener, FrameBookHelper.BookChanged,
        BookShelfReAdapter.ShelfItemClickListener, BookShelfReAdapter.ShelfItemLongClickListener,
        BookShelfView {

    private val presenter: BookShelfPresenter by lazy { BookShelfPresenter(this) }

    val bookShelfReAdapter: BookShelfReAdapter by lazy {
        BookShelfReAdapter(activity, presenter.iBookList, this, this, true)
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
    //自有广告管理类
    private val ownNativeAdManager: OwnNativeAdManager? by lazy {
        var manager: OwnNativeAdManager? = null
        if (!Constants.isHideAD) {
            manager = OwnNativeAdManager.getInstance(activity)
            manager.setActivity(activity)
        }
        manager
    }

    private var frameBookHelper: FrameBookHelper? = null
    private var bookDaoHelper: BookDaoHelper = BookDaoHelper.getInstance()
    private var isShowAD = false
    private var bookRackUpdateTime: Long = 0
    private var latestLoadDataTime: Long = 0
    private val sharedPreferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
    }
    private var head_pb_view: ProgressBar? = null
    private var head_text_view: TextView? = null
    private var head_image_view: ImageView? = null

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
                head_text_view?.text = "正在刷新"
                head_image_view?.visibility = View.GONE
                head_pb_view?.visibility = View.VISIBLE
                checkBookUpdate()
            }

            override fun onPullDistance(distance: Int) {}

            override fun onPullEnable(enable: Boolean) {
                head_pb_view?.visibility = View.GONE
                head_text_view?.text = if (enable) "松开刷新" else "下拉刷新"
                head_image_view?.visibility = View.VISIBLE
                head_image_view?.rotation = (if (enable) 180 else 0).toFloat()
            }
        })
        bookshelf_empty_btn.setOnClickListener {
            fragmentCallback.setSelectTab(1)
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SHELF_PAGE, StartLogClickUtil.TOBOOKCITY)
        }
        fab_goto_down_act.setOnClickListener {
            val intent = Intent(activity, DownloadManagerActivity::class.java)
            startActivity(intent)
        }
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
        val headerView = LayoutInflater.from(bookshelf_refresh_view.context)
                .inflate(R.layout.layout_head, null)
        head_pb_view = headerView.findViewById(R.id.head_pb_view) as ProgressBar
        head_text_view = headerView.findViewById(R.id.head_text_view) as TextView
        head_text_view?.text = "下拉刷新"
        head_image_view = headerView.findViewById(R.id.head_image_view) as ImageView
        head_image_view?.visibility = View.VISIBLE
        head_image_view?.setImageResource(R.drawable.pulltorefresh_down_arrow)
        head_pb_view?.visibility = View.GONE
        return headerView
    }

    override fun onItemClick(view: View, position: Int) {
        AppLog.e(TAG, "BookShelfItemClick")
        if (position < 0 || position > presenter.iBookList.size) {
            return
        }
        if (bookShelfRemoveHelper.isRemoveMode) {
            bookShelfRemoveHelper.setCheckPosition(position)
        } else {
            AppLog.e(TAG, "intoNovelContent")
            if (position >= presenter.iBookList.size || position < 0) return
            val book = presenter.iBookList[position]
            handleBook(book)
            presenter.uploadItemClickLog(position)
        }
    }

    override fun onItemLongClick(view: View, position: Int) {
        if (!bookShelfRemoveHelper.isRemoveMode) {
            bookShelfRemoveHelper.showRemoveMenu(bookshelf_refresh_view)
            presenter.uploadItemLongClickLog()
        }
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
        updateUI()
        ownNativeAdManager?.setActivity(activity)
    }

    fun onEvent(eventBookshelfAd: EventBookshelfAd) {
        AppLog.e("ADSDK", "onEvent")
        val isHandle = activity != null && isAdded
        if (isHandle) isShowAD = true
        val isNotShowAd = !isShowAD || bookShelfRemoveHelper.isRemoveMode || !isResumed
        presenter.handleBookShelfAd(eventBookshelfAd, isHandle, isNotShowAd, ownNativeAdManager, true)
    }

    override fun onBookShelfAdHandle() {
        bookShelfReAdapter.notifyDataSetChanged()
        AppLog.e(TAG, "notifyDataSetChanged")
        StatServiceUtils.statBookEventShow(activity, StatServiceUtils.type_ad_shelf)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
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
        presenter.adInfoHashMap.clear()
    }

    /**
     * 查Book数据库更新界面
     */
    private fun updateUI() {
        val isNotShowAd = !isShowAD || bookShelfRemoveHelper.isRemoveMode || !isResumed
        doAsync {
            presenter.queryBookListAndAd(ownNativeAdManager, isNotShowAd, true)
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

    override fun hideBannerAd() {
        //NONE
    }

    override fun showBannerAd(adInfo: YQNativeAdInfo) {
        //NONE
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

        //下拉刷新时删除标记的360广告信息
        presenter.remove360Ads()

        // 刷新间隔小于30秒无效
        if (interval <= PULL_REFRESH_DELAY) {
            bookshelf_refresh_view.onRefreshComplete()
            AppLog.d(TAG, "刷新间隔小于30秒不请求数据")
            showToastDelay(R.string.main_update_no_new)
        } else {
            // 刷新间隔大于30秒直接请求更新，
            addUpdateTask()
            AppLog.d(TAG, "刷新间隔大于30秒请求数据")
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
                    activity.applicationContext.toastShort(textId)
                }
    }

    private fun showToastDelay(text: String) {
        if (!isAdded) return
        Flowable.timer(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    activity.applicationContext.toastShort(text)
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

    override fun onSuccess(result: BookUpdateResult) {
        latestLoadDataTime = System.currentTimeMillis()
        bookRackUpdateTime = System.currentTimeMillis()
        bookshelf_refresh_view.onRefreshComplete()
        presenter.handleSuccessUpdate(result)
        AppUtils.setLongPreferences(activity, "book_rack_update_time", bookRackUpdateTime)
        AppLog.e(TAG, "onSuccess的刷新ui调用")
        isShowAD = true
        updateUI()
    }

    override fun onException(e: Exception) {
        latestLoadDataTime = System.currentTimeMillis()
        showToastDelay(R.string.bookshelf_refresh_network_problem)
        bookshelf_refresh_view.onRefreshComplete()
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
        addUpdateTask()
    }

    override fun doUpdateBook(updateService: CheckNovelUpdateService?) {
        updateService?.setBookUpdateListener(activity as CheckNovelUpdateService.OnBookUpdateListener)
        addUpdateTask()

    }

    override fun changeDownLoadBtn(isDownLoading: Boolean) {}

    override fun doNotifyDownload() {
        updateUI()
    }

    override fun notification(gid: String) {
        if (!TextUtils.isEmpty(gid)) {
            val book = bookDaoHelper.getBook(gid, 0) as Book
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
        bookDeleteDialog.show(checkedBooks)
    }

    override fun onBookDelete() {
        AppLog.e(TAG, "onBookDelete")
        updateUI()
        bookShelfRemoveHelper.dismissRemoveMenu()
    }

    override fun getMenuShownState(state: Boolean) {
        AppLog.e(TAG, "getMenuShowState: $state")
        if (state) {
            bookshelf_refresh_view.setPullToRefreshEnabled(false)
        } else {
            if (presenter.iBookList.isNotEmpty()) {
                bookshelf_refresh_view.setPullToRefreshEnabled(true)
            }
            updateUI()
        }
        fragmentCallback.getMenuShownState(state)
    }

    override fun getAllCheckedState(isAll: Boolean) {
        fragmentCallback.getAllCheckedState(isAll)
    }

    override fun doHideAd() {
        if (isShowAD) {
            presenter.removeAd()
        }
    }

    override fun updateBook() {
        AppLog.e(TAG, "updateBook的刷新UI调用")
        updateUI()
    }

    companion object {

        val ACTION_CHKHIDE = AppUtils.getPackageName()
        private val PULL_REFRESH_DELAY = 30 * 1000
        private val TAG = BookShelfFragment::class.java.simpleName
    }
}
