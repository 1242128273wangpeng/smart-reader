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
import com.dingyue.bookshelf.BookShelfAdapter.BookShelfItemListener
import com.dingyue.contract.CommonContract
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.txtqbmfyd.bookshelf_refresh_head.view.*
import kotlinx.android.synthetic.txtqbmfyd.fragment_bookshelf.*
import net.lzbook.kit.book.component.service.CheckNovelUpdateService
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.UpdateCallBack
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.BookUpdate
import net.lzbook.kit.data.bean.BookUpdateResult
import net.lzbook.kit.pulllist.SuperSwipeRefreshLayout
import net.lzbook.kit.router.BookRouter
import net.lzbook.kit.router.RouterConfig
import net.lzbook.kit.router.RouterUtil
import net.lzbook.kit.utils.*
import java.util.concurrent.TimeUnit

class BookShelfFragment : Fragment(), UpdateCallBack, BookShelfView, MenuManager {

    private val bookshelfPresenter: BookShelfPresenter by lazy { BookShelfPresenter(this) }

    private var bookRackUpdateTime: Long = 0
    private var latestLoadDataTime: Long = 0

    private lateinit var sharedPreferences: SharedPreferences

    private var bookShelfInterface: BookShelfInterface? = null

    private val headerView: View by lazy {
        LayoutInflater.from(bookshelf_refresh_view.context).inflate(R.layout.bookshelf_refresh_head, null)
    }

    private val homeMenuPopup: HomeMenuPopup by lazy {
        val popup = HomeMenuPopup(this.activity.applicationContext)
        popup.setOnDownloadClickListener {
            RouterUtil.navigation(activity, RouterConfig.DOWNLOAD_MANAGER_ACTIVITY)
            BookShelfLogger.uploadBookShelfCacheManager()
        }
        popup.setOnSortingClickListener {
            bookSortingDialog.show()
            BookShelfLogger.uploadBookShelfBookSort()
        }
        popup
    }

    private val bookShelfRemovePopup: BookShelfRemovePopup by lazy {
        val popup = BookShelfRemovePopup(this.context)
        popup.setOnDeletedClickListener {
            bookDeleteDialog.show(bookShelfAdapter.selectedBooks)
        }
        popup.setOnCancelClickListener {
            dismissRemoveMenu()
            BookShelfLogger.uploadBookShelfEditCancel()
        }
        popup
    }

    private val bookSortingDialog: BookSortingDialog by lazy {
        val dialog = BookSortingDialog(this.activity)
        dialog.setOnRecentReadClickListener {
            sortBooks(0)
        }
        dialog.setOnUpdateTimeClickListener {
            sortBooks(1)
        }
        dialog
    }

    val bookShelfAdapter: BookShelfAdapter by lazy {
        BookShelfAdapter(context, object : BookShelfItemListener {
            override fun clickedBookShelfItem(book: Book?, position: Int) {

                if (position < 0 || position > bookshelfPresenter.iBookList.size) {
                    return
                }

                if (!bookShelfAdapter.isRemove) {
                    if (position == bookshelfPresenter.iBookList.size) {
                        bookShelfInterface?.changeHomePagerIndex(1)
                        return
                    }

                    if (position >= bookshelfPresenter.iBookList.size || position < 0) {
                        return
                    }

                    if (book != null) {
                        handleBook(book)
                        BookShelfLogger.uploadBookShelfBookClick(book, position)
                    }
                } else {
                    bookShelfAdapter.insertSelectedPosition(position)
                    bookShelfRemovePopup.setSelectedNum(bookShelfAdapter.selectedBooks.size)
                    txt_editor_select_all.text = if (bookShelfAdapter.isSelectedAll()) getString(R.string.select_all_cancel) else getString(R.string.select_all)
                }
            }

            override fun longClickedBookShelfItem(): Boolean {
                if (!bookShelfAdapter.isRemove) {
                    showRemoveMenu()
                    BookShelfLogger.uploadBookShelfLongClickBookShelfEdit()
                }
                return false
            }

        }, bookshelfPresenter.iBookList, bookshelfPresenter.aDViews, true)
    }

    private val bookDeleteDialog: BookDeleteDialog by lazy {
        val dialog = BookDeleteDialog(activity)
        dialog.setOnConfirmListener { books, isDeleteCacheOnly ->
            if (books != null && books.isNotEmpty()) {
                dialog.showLoading()
                deleteBooks(books, isDeleteCacheOnly)
            }
        }
        dialog.setOnAbrogateListener {
            BookShelfLogger.uploadBookShelfEditDelete(0, null, false)
        }
        dialog
    }

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
                headerView.txt_head_prompt.text = "正在刷新"
                headerView.img_head_arrow.visibility = View.GONE
                headerView.pgbar_head_loading.visibility = View.VISIBLE
                checkBookUpdate()
            }

            override fun onPullDistance(distance: Int) {}

            override fun onPullEnable(enable: Boolean) {
                headerView.pgbar_head_loading.visibility = View.GONE
                headerView.txt_head_prompt.text = if (enable) "松开刷新" else "下拉刷新"
                headerView.img_head_arrow.visibility = View.VISIBLE
                headerView.img_head_arrow.rotation = (if (enable) 180 else 0).toFloat()
            }
        })
        txt_head_title.text = "书架"

        img_head_setting.setOnClickListener {
            bookShelfInterface?.changeDrawerLayoutState()
            BookShelfLogger.uploadBookShelfPersonal()
        }

        img_head_search.setOnClickListener {
            RouterUtil.navigation(activity, RouterConfig.SEARCH_BOOK_ACTIVITY)
            BookShelfLogger.uploadBookShelfSearch()
        }

        img_head_menu.setOnClickListener {
            homeMenuPopup.show(img_head_menu)
            BookShelfLogger.uploadBookShelfMore()
        }

        txt_editor_select_all.setOnClickListener {
            if (CommonContract.isDoubleClick(System.currentTimeMillis())) {
                return@setOnClickListener
            }
            if (txt_editor_select_all.text == getString(R.string.select_all)) {
                txt_editor_select_all.text = getString(R.string.select_all_cancel)
                selectAll(true)
            } else {
                txt_editor_select_all.text = getString(R.string.select_all)
                selectAll(false)
            }
        }

        bookshelf_empty_btn.setOnClickListener {
            bookShelfInterface?.changeHomePagerIndex(1)
            BookShelfLogger.uploadBookShelfToBookCity()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)

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
    }

    /**
     * 查Book数据库更新界面
     */
    fun updateUI() {
        val isShowAd = !bookShelfAdapter.isRemove && isResumed && !Constants.isHideAD
        doAsync {
            bookshelfPresenter.queryBookListAndAd(activity, isShowAd)
            uiThread {
                bookShelfAdapter.notifyDataSetChanged()
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

    override fun onBookDelete() {
        AppLog.e(TAG, "onBookDelete")
        updateUI()
        bookDeleteDialog.dismiss()
        dismissRemoveMenu()
        activity.toastShort(R.string.book_delete_success)
    }

    override fun onAdRefresh() {
        bookShelfAdapter.notifyDataSetChanged()
    }

    private fun initUpdateService() {
        if (bookshelfPresenter.updateService != null) {
            return
        }

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
        recycler_view.itemAnimator.addDuration = 0
        recycler_view.itemAnimator.changeDuration = 0
        recycler_view.itemAnimator.moveDuration = 0
        recycler_view.itemAnimator.removeDuration = 0
        (recycler_view.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        recycler_view.adapter = bookShelfAdapter
        recycler_view.topShadow = img_head_shadow
    }

    private fun createHeaderView(): View {
        headerView.txt_head_prompt.text = "下拉刷新"
        headerView.img_head_arrow.visibility = View.VISIBLE
        headerView.img_head_arrow.setImageResource(R.drawable.pulltorefresh_down_arrow)
        headerView.pgbar_head_loading.visibility = View.GONE
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

        BookRouter.navigateCoverOrRead(activity, book, 0)
        AppLog.e(TAG, "goToCoverOrRead")
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

    /***
     * 更改头部布局状态
     * **/
    private fun changeHeaderState(state: Boolean) {
        if (state) {
            if (rl_head_bookshelf.isShown) {
                rl_head_bookshelf.visibility = View.GONE
            }

            if (!rl_head_editor.isShown) {
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

    override fun showRemoveMenu() {
        bookshelf_refresh_view.setPullToRefreshEnabled(false)

        bookshelfPresenter.removeAd()

        bookShelfAdapter.insertRemoveState(true)

        bookShelfInterface?.changeHomeNavigationState(true)

        book_shelf_ad.visibility = View.GONE

        bookShelfRemovePopup.show(ll_content)

        changeHeaderState(true)

        bookshelf_main.setPadding(0, bookshelf_main.paddingTop, 0, 140)

        txt_editor_select_all.text = getString(R.string.select_all)
    }

    override fun dismissRemoveMenu() {
        bookShelfAdapter.insertRemoveState(false)

        bookShelfRemovePopup.dismiss()

        bookshelf_main.setPadding(0, bookshelf_main.paddingTop, 0, 0)

        bookShelfInterface?.changeHomeNavigationState(false)

        changeHeaderState(false)

        txt_editor_select_all.text = getString(R.string.select_all_cancel)
    }

    override fun isRemoveMenuShow(): Boolean = bookShelfAdapter.isRemove

    override fun selectAll(isAll: Boolean) {
        bookShelfAdapter.insertSelectAllState(isAll)
        bookShelfRemovePopup.setSelectedNum(bookShelfAdapter.selectedBooks.size)
        BookShelfLogger.uploadBookShelfEditSelectAll(isAll)
    }

    override fun sortBooks(type: Int) {
        CommonContract.insertShelfSortType(type)
        updateUI()
        BookShelfLogger.uploadBookShelfSortType(type)
    }

    override fun deleteBooks(books: ArrayList<Book>, isDeleteCacheOnly: Boolean) {
        bookshelfPresenter.deleteBooks(books, isDeleteCacheOnly)
    }
}