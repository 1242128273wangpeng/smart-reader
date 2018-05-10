package com.dingyue.bookshelf

import android.app.Activity
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
import android.view.animation.AlphaAnimation
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.txtqbmfyd.fragment_bookshelf.*
import kotlinx.android.synthetic.txtqbmfyd.layout_head.view.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.component.service.CheckNovelUpdateService
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.UpdateCallBack
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.BookUpdate
import net.lzbook.kit.data.bean.BookUpdateResult
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.pulllist.SuperSwipeRefreshLayout
import net.lzbook.kit.router.BookRouter
import net.lzbook.kit.router.RouterConfig
import net.lzbook.kit.router.RouterUtil
import net.lzbook.kit.utils.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 书架页Fragment
 */
class BookShelfFragment : Fragment(), UpdateCallBack, BookShelfView {

    private val bookshelfPresenter: BookShelfPresenter by lazy { BookShelfPresenter(this) }

    private val homeMenuPopup: HomeMenuPopup by lazy {
        val popup = HomeMenuPopup(this.activity.applicationContext)
        popup.setOnDownloadClickListener {
            RouterUtil.navigation(RouterConfig.DOWNLOAD_MANAGER_ACTIVITY)
            bookshelfPresenter.uploadDownloadManagerLog()
        }
        popup.setOnSortingClickListener {
            bookSortingDialog.show()
            bookshelfPresenter.uploadBookSortingLog()
        }
        popup
    }

    private val bookSortingDialog: BookSortingDialog by lazy {
        val dialog = BookSortingDialog(this.activity)
        dialog.setOnRecentReadClickListener {
            StatServiceUtils.statAppBtnClick(this.activity.applicationContext, StatServiceUtils.me_set_cli_shelf_rak_time)
            settingItemsHelper.putInt(settingItemsHelper.booklistSortType, 0)
            Constants.book_list_sort_type = 0
            updateUI()
        }
        dialog.setOnUpdateTimeClickListener {
            StatServiceUtils.statAppBtnClick(this.activity.applicationContext, StatServiceUtils.me_set_cli_shelf_rak_time)
            settingItemsHelper.putInt(settingItemsHelper.booklistSortType, 1)
            Constants.book_list_sort_type = 1
            updateUI()
        }
        dialog
    }

    private val settingItemsHelper by lazy { SettingItemsHelper.getSettingHelper(this.activity.applicationContext) }

    val bookShelfReAdapter: BookShelfReAdapter by lazy {
        BookShelfReAdapter(activity, bookshelfPresenter.iBookList, bookshelfPresenter.aDViews,
                BookShelfReAdapter.ShelfItemClickListener { _, position ->
                    AppLog.e(TAG, "BookShelfItemClick")


                    if (position < 0 || position > bookshelfPresenter.iBookList.size) {
                        return@ShelfItemClickListener
                    }

                    if (position == bookshelfPresenter.iBookList.size) {
                        bookShelfInterface?.changeHomePagerIndex(1)
                    }

                    if (bookShelfRemoveHelper.isRemoveMode) {
                        bookShelfRemoveHelper.setCheckPosition(position)
                    } else {
                        AppLog.e(TAG, "intoNovelContent")
                        if (position >= bookshelfPresenter.iBookList.size || position < 0) return@ShelfItemClickListener
                        val book = bookshelfPresenter.iBookList[position]
                        handleBook(book)
                        bookshelfPresenter.uploadItemClickLog(position)
                    }
                },

                BookShelfReAdapter.ShelfItemLongClickListener { _, _ ->
                    if (!bookShelfRemoveHelper.isRemoveMode) {
                        bookShelfRemoveHelper.showRemoveMenu(bookshelf_refresh_view)
                        bookshelfPresenter.uploadItemLongClickLog()
                    }
                })
    }

    var onRemoveModeAllCheckedListener: ((isAllChecked: Boolean) -> Unit)? = null

    val bookShelfRemoveHelper: BookShelfRemoveHelper by lazy {
        val helper = BookShelfRemoveHelper(activity, bookShelfReAdapter)
        helper.setLayout(bookshelf_refresh_view)
        helper.setOnMenuStateListener(object : BookShelfRemoveHelper.OnMenuStateListener {
            override fun getMenuShownState(isShown: Boolean) {
                AppLog.e(TAG, "getMenuShowState: $isShown")
                if (isShown) {
                    bookshelf_refresh_view.setPullToRefreshEnabled(false)
                } else {
                    if (bookshelfPresenter.iBookList.isNotEmpty()) {
                        bookshelf_refresh_view.setPullToRefreshEnabled(true)
                        updateUI()
                    }
                }
                bookShelfInterface?.changeHomeNavigationState(isShown)

                changeHeaderState(isShown)

                if (isShown) {
                    book_shelf_ad.visibility = View.GONE
                } else {
                    book_shelf_ad.visibility = View.VISIBLE
                }
            }

            override fun getAllCheckedState(isAllChecked: Boolean) {
                onRemoveModeAllCheckedListener?.invoke(isAllChecked)
            }

            override fun doHideAd() {
                bookshelfPresenter.removeAd()
            }

        })

        helper.setOnMenuDeleteListener(BookShelfRemoveHelper.OnMenuDeleteClickListener { checked_state ->
            if (checked_state.isEmpty()) return@OnMenuDeleteClickListener
            val checkedBooks = ArrayList<Book>()
            val size = bookshelfPresenter.iBookList.size
            (0 until size).filter {
                checked_state.contains(it)
            }.mapTo(checkedBooks) {
                bookshelfPresenter.iBookList[it]
            }
            bookDeleteDialog.show(checkedBooks)
        })
        helper
    }

    private val bookSensitiveWords: ArrayList<String> = ArrayList()
    private val noBookSensitive = false

    private var bookDaoHelper: BookDaoHelper = BookDaoHelper.getInstance()
    private var bookRackUpdateTime: Long = 0
    private var latestLoadDataTime: Long = 0

    private val headerView: View by lazy {
        LayoutInflater.from(bookshelf_refresh_view.context)
                .inflate(R.layout.layout_head, null)
    }

    private lateinit var sharedPreferences: SharedPreferences

    private val bookDeleteDialog: BookDeleteDialog by lazy {
        val dialog = BookDeleteDialog(activity)
        dialog.setOnConfirmListener { books, isOnlyDeleteCache ->
            if (books != null && books.isNotEmpty()) {
                dialog.showLoading()
                bookshelfPresenter.deleteBooks(books, isOnlyDeleteCache)
            }
            StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.bs_click_delete_ok_btn)
        }
        dialog.setOnAbrogateListener {
            bookshelfPresenter.uploadBookDeleteCancelLog()
            StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.bs_click_delete_cancel_btn)
        }
        dialog
    }

    private var bookShelfInterface: BookShelfInterface? = null

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)

        try {
            bookShelfInterface = activity as BookShelfInterface
        } catch (classCastException: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement BookShelfInterface")
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_bookshelf, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        bookRackUpdateTime = AppUtils.getLongPreferences(activity, "book_rack_update_time", System.currentTimeMillis())
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
            bookShelfInterface?.changeHomePagerIndex(1)
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SHELF_PAGE, StartLogClickUtil.TOBOOKCITY)
        }
        //悬浮广告 1-2
        if (Constants.isHideAD) return


        img_head_setting.setOnClickListener {
            bookShelfInterface?.changeDrawerLayoutState()
            bookshelfPresenter.uploadHeadSettingLog()
        }

        txt_head_title.text = "书架"

        img_head_search.setOnClickListener {
            RouterUtil.navigation(RouterConfig.SEARCH_BOOK_ACTIVITY)
            bookshelfPresenter.uploadHeadSearchLog(0)
        }

        img_head_menu.setOnClickListener {
            homeMenuPopup.show(img_head_menu)
            StartLogClickUtil.upLoadEventLog(this.activity.applicationContext,
                    StartLogClickUtil.SHELF_PAGE, StartLogClickUtil.MORE)
        }



        txt_editor_select_all.setOnClickListener {
            val isAllSelected = bookShelfRemoveHelper?.isAllChecked ?: false
            if (isAllSelected) {
                txt_editor_select_all.text = getString(R.string.select_all)
                bookShelfRemoveHelper?.selectAll(false)
            } else {
                txt_editor_select_all.text = getString(R.string.select_all_cancel)
                bookShelfRemoveHelper?.selectAll(true)
            }
            bookshelfPresenter.uploadEditorSelectAllLog(isAllSelected)
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)

        bookshelfPresenter.clickNotification(context, activity.intent)

        initUpdateService()

        //根据书架数量确定是否刷新
        if (bookshelfPresenter.iBookList.size > 0) {
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

        bookshelfPresenter.iBookList.clear()

        rl_head_bookshelf.clearAnimation()
        rl_head_editor.clearAnimation()
    }

    /**
     * 查Book数据库更新界面
     */
    fun updateUI() {
        AppLog.e(TAG, "updateUI")
        val isShowAd = !bookShelfRemoveHelper.isRemoveMode && isResumed && !Constants.isHideAD
        doAsync {
            bookshelfPresenter.queryBookListAndAd(activity, isShowAd)
            uiThread {
                bookShelfReAdapter.setUpdate_table(bookshelfPresenter.filterUpdateTableList())
                bookShelfReAdapter.notifyDataSetChanged()
                bookshelfPresenter.uploadFirstOpenLog(sharedPreferences)
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
        if (activity != null && !activity.isFinishing) {
            latestLoadDataTime = System.currentTimeMillis()
            bookRackUpdateTime = System.currentTimeMillis()
            bookRackUpdateTime = System.currentTimeMillis()
            if (bookshelf_refresh_view != null) {
                bookshelf_refresh_view!!.onRefreshComplete()
            }
            bookshelfPresenter.handleSuccessUpdate(result)
            AppUtils.setLongPreferences(activity, "book_rack_update_time", bookRackUpdateTime)
            AppLog.e(TAG, "onSuccess的刷新ui调用")
//        updateUI()
        }
    }

    override fun onException(e: Exception) {
        latestLoadDataTime = System.currentTimeMillis()
        showToastDelay(R.string.bookshelf_refresh_network_problem)
        if (bookshelf_refresh_view != null) {
            bookshelf_refresh_view.onRefreshComplete()
        }
    }

    override fun onSuccessUpdateHandle(updateCount: Int, firstBook: BookUpdate?) {
        if (activity == null || activity.isFinishing) {
            return
        }
        if (updateCount == 0) {
            showToastDelay(R.string.main_update_no_new)
        } else {
            val bookName = firstBook?.book_name
            val bookLastChapterName = firstBook?.last_chapter_name
            if (bookName?.isNotEmpty() == true && bookLastChapterName?.isNotEmpty() == true) {
                if (updateCount == 1 && activity != null) {
                    showToastDelay("《$bookName${activity.getString(R.string.bookshelf_one_book_update)}" +
                            "$bookLastChapterName")
                } else if (activity != null) {
                    showToastDelay("《$bookName${activity.getString(R.string.bookshelf_more_book_update)}" +
                            "$updateCount${activity.getString(R.string.bookshelf_update_chapters)}")
                }
            }
        }
    }

    fun doUpdateBook() {
        bookshelfPresenter.addUpdateTask(this)
    }

    override fun doUpdateBook(updateService: CheckNovelUpdateService) {
        if (activity != null) {
            updateService.setBookUpdateListener(activity as CheckNovelUpdateService.OnBookUpdateListener)
            bookshelfPresenter.addUpdateTask(this)
        }
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
        bookDeleteDialog.dismiss()
        bookShelfRemoveHelper.dismissRemoveMenu()
        activity.toastShort(R.string.book_delete_success)
    }

    override fun onAdRefresh() {
        bookShelfReAdapter.notifyDataSetChanged()
    }

    private fun initUpdateService() {
        if (bookshelfPresenter.updateService != null) return
        val intent = Intent()
        val context = activity.applicationContext
        intent.setClass(context, CheckNovelUpdateService::class.java)
        context.startService(intent)
        context.bindService(intent, bookshelfPresenter.updateConnection, Context.BIND_AUTO_CREATE)
    }

    private fun initRecyclerView() {
        bookshelf_refresh_view.setHeaderViewBackgroundColor(0x00000000)
        bookshelf_refresh_view.setHeaderView(createHeaderView())
        bookshelf_refresh_view.isTargetScrollWithLayout = true
        recycler_view.recycledViewPool.setMaxRecycledViews(0, 12)
        val layoutManager = ShelfGridLayoutManager(activity, 3)
        recycler_view.layoutManager = layoutManager
        recycler_view.isFocusable = false//放弃焦点
        //        recyclerView.getItemAnimator().setSupportsChangeAnimations(false);
        recycler_view.itemAnimator.addDuration = 0
        recycler_view.itemAnimator.changeDuration = 0
        recycler_view.itemAnimator.moveDuration = 0
        recycler_view.itemAnimator.removeDuration = 0
        (recycler_view.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        recycler_view.adapter = bookShelfReAdapter
        recycler_view.topShadow = img_head_shadow
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
            bookshelfPresenter.addUpdateTask(this)
            AppLog.d(TAG, "刷新间隔大于30秒请求数据")
        }

    }

    /**
     * 处理被点击或更新通知的book
     */
    private fun handleBook(book: Book) {
        AppLog.e(TAG, "handleBook")
        if (!TextUtils.isEmpty(book.book_id) && book.book_type == 0) {
            bookshelfPresenter.resetUpdateStatus(book.book_id)
        }

        if (Constants.isShielding && !noBookSensitive && bookSensitiveWords.contains(book.book_id.toString())) {
            ToastUtils.showToastNoRepeat("抱歉，该小说已下架！")
        } else {
            BookRouter.navigateCoverOrRead(activity, book, 0)
            AppLog.e(TAG, "goToCoverOrRead")
        }
    }

    private fun showToastDelay(textId: Int) {
        if (!isAdded) return
        Observable.timer(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (activity != null && !activity.isFinishing) {
                        activity.applicationContext.toastShort(textId, false)
                    }
                }
    }

    private fun showToastDelay(text: String) {
        if (!isAdded) return
        Observable.timer(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (activity != null && !activity.isFinishing) {
                        activity.applicationContext.toastShort(text, false)
                    }
                }
    }

    companion object {

        private val PULL_REFRESH_DELAY = 30 * 1000
        private val TAG = BookShelfFragment::class.java.simpleName
    }

    interface BookShelfInterface {
        fun changeHomeNavigationState(state: Boolean)

        fun changeHomePagerIndex(index: Int)

        fun changeDrawerLayoutState()
    }





    /***
     * 更改头部布局状态
     * **/
    private fun changeHeaderState(state: Boolean) {

        val showAnimation = AlphaAnimation(0.0f, 1.0f)
        showAnimation.duration = 200

        val hideAnimation = AlphaAnimation(1.0f, 0.0f)
        hideAnimation.duration = 200

        if (state) {
            if (rl_head_bookshelf.isShown) {
                rl_head_bookshelf.startAnimation(hideAnimation)
                rl_head_bookshelf.visibility = View.GONE
            }

            if (!rl_head_editor.isShown) {
                rl_head_editor.startAnimation(showAnimation)
                rl_head_editor.visibility = View.VISIBLE
            }
        } else {
            if (!rl_head_bookshelf.isShown) {
                rl_head_bookshelf.visibility = View.VISIBLE
            }

            if (rl_head_editor.isShown) {
                rl_head_editor.visibility = View.GONE
            }
        }
    }
}