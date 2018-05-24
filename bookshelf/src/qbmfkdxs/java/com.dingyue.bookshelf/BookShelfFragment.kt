package com.dingyue.bookshelf

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dingyue.bookshelf.contract.BookShelfADContract
import com.dingyue.bookshelf.view.BookShelfDeleteDialog
import com.dingyue.bookshelf.view.BookShelfSortingPopup
import com.dingyue.bookshelf.view.HeadMenuPopup
import com.dingyue.bookshelf.view.RemoveMenuPopup
import com.dingyue.contract.CommonContract
import de.greenrobot.event.EventBus
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.qbmfkdxs.bookshelf_refresh_header.view.*
import kotlinx.android.synthetic.qbmfkdxs.frag_bookshelf.*
import net.lzbook.kit.book.component.service.CheckNovelUpdateService
import net.lzbook.kit.book.view.ConsumeEvent
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

/**
 * 书架页Fragment
 */
class BookShelfFragment : Fragment(), UpdateCallBack, BookShelfView, MenuManager {

    private val popupHeight by lazy {
        resources.getDimensionPixelSize(R.dimen.bookshelf_popup_height)
    }

    val bookShelfPresenter: BookShelfPresenter by lazy { BookShelfPresenter(this) }

    private var bookRackUpdateTime: Long = 0
    private var latestLoadDataTime: Long = 0

    private var bookShelfInterface: BookShelfInterface? = null

    private val refreshHeader: View by lazy {
        LayoutInflater.from(srl_refresh.context)
                .inflate(R.layout.bookshelf_refresh_header, null)
    }

    private val headMenuPopup: HeadMenuPopup by lazy {
        val popup = HeadMenuPopup(activity)
        popup.onDownloadManagerClickListener = {
            RouterUtil.navigation(activity, RouterConfig.DOWNLOAD_MANAGER_ACTIVITY)
            BookShelfLogger.uploadBookShelfCacheManager()
        }
        popup.onBookSortingClickListener = {
            bookShelfSortingPopup.show(rl_content)
            BookShelfLogger.uploadBookShelfBookSort()
        }
        popup
    }

    private val removeMenuPopup: RemoveMenuPopup by lazy {
        val popup = RemoveMenuPopup(activity)
        popup.onDeleteClickListener = {
            bookShelfDeleteDialog.show(bookShelfAdapter.selectedBooks)
        }
        popup
    }

    private val bookShelfSortingPopup: BookShelfSortingPopup by lazy {
        val popup = BookShelfSortingPopup(activity)
        popup.onTimeSortingClickListener = {
            sortBooks(1)
        }
        popup.onRecentReadSortingClickListener = {
            sortBooks(0)
        }
        popup
    }

    val bookShelfAdapter: BookShelfAdapter by lazy {
        BookShelfAdapter(activity, object : BookShelfAdapter.BookShelfItemListener {
            override fun clickedBookShelfItem(book: Book?, position: Int) {
                if (position < 0 || position >= bookShelfPresenter.iBookList.size) return
                if (isRemoveMenuShow()) {
                    bookShelfAdapter.insertSelectedPosition(position)
                    removeMenuPopup.setSelectedNum(bookShelfAdapter.selectedBooks.size)
                    txt_remove_head_select_all.text =
                            if (bookShelfAdapter.isSelectedAll())
                                getString(R.string.cancel_select_all)
                            else
                                getString(R.string.select_all)
                } else {
                    handleBook(book)
                    book?.let {
                        BookShelfLogger.uploadBookShelfBookClick(it, position)
                    }
                }
            }

            override fun longClickedBookShelfItem(): Boolean {
                if (!isRemoveMenuShow()) {
                    showRemoveMenu()
                    BookShelfLogger.uploadBookShelfLongClickBookShelfEdit()
                }
                return false
            }

        }, bookShelfPresenter.iBookList)
    }

    private val bookShelfDeleteDialog: BookShelfDeleteDialog by lazy {
        val dialog = BookShelfDeleteDialog(activity)
        dialog.onConfirmListener = { books, isDeleteCacheOnly ->
            deleteBooks(books, isDeleteCacheOnly)
        }
        dialog.onCancelListener = {
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initUpdateService()

        if (bookShelfPresenter.iBookList.size > 0) {
            srl_refresh.isRefreshing = true
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.frag_bookshelf, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {

        BookShelfADContract.insertBookShelfType(false)

        bookRackUpdateTime = AppUtils.getLongPreferences(activity, "book_rack_update_time", System.currentTimeMillis())

        initRecyclerView()

        srl_refresh.setOnPullRefreshListener(object : SuperSwipeRefreshLayout.OnPullRefreshListener {
            override fun onRefresh() {
                refreshHeader.txt_refresh_title.text = getString(R.string.refresh_running)
                refreshHeader.img_refresh_arrow.visibility = View.GONE
                refreshHeader.pgbar_refresh_loading.visibility = View.VISIBLE
                checkBookUpdate()
            }

            override fun onPullDistance(distance: Int) {}

            override fun onPullEnable(enable: Boolean) {
                refreshHeader.pgbar_refresh_loading.visibility = View.GONE
                refreshHeader.txt_refresh_title.text = if (enable) getString(R.string.refresh_release) else getString(R.string.refresh_start)
                refreshHeader.img_refresh_arrow.visibility = View.VISIBLE
                refreshHeader.img_refresh_arrow.rotation = (if (enable) 180 else 0).toFloat()
            }
        })

        txt_empty.setOnClickListener {
            bookShelfInterface?.changeHomePagerIndex(1)
            BookShelfLogger.uploadBookShelfToBookCity()
        }

        img_head_setting.setOnClickListener {
            BookShelfLogger.uploadBookShelfPersonal()
            EventBus.getDefault().post(ConsumeEvent(R.id.fup_head_personal))
            RouterUtil.navigation(activity, RouterConfig.SETTING_ACTIVITY)
        }

        rl_head_search.setOnClickListener {
            val bundle = Bundle()
            bundle.putBoolean("isShowLastSearch", false)
            RouterUtil.navigation(activity, RouterConfig.SEARCH_BOOK_ACTIVITY, bundle)
            BookShelfLogger.uploadBookShelfSearch()
        }

        img_head_menu.setOnClickListener {
            headMenuPopup.show(img_head_menu)
            BookShelfLogger.uploadBookShelfMore()
        }

        txt_remove_head_select_all.setOnClickListener {

            if (CommonContract.isDoubleClick(System.currentTimeMillis())) {
                return@setOnClickListener
            }
            if (txt_remove_head_select_all.text == getString(R.string.select_all)) {
                txt_remove_head_select_all.text = getString(R.string.cancel_select_all)
                selectAll(true)
            } else {
                txt_remove_head_select_all.text = getString(R.string.select_all)
                selectAll(false)
            }
        }

        txt_remove_head_cancel.setOnClickListener {
            dismissRemoveMenu()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()

        if (!Constants.isHideAD && Constants.dy_shelf_boundary_switch && bookShelfPresenter.iBookList.isNotEmpty()) {
            bookShelfPresenter.requestFloatAD(activity, fl_ad_float)
        }
    }

    override fun onDetach() {
        super.onDetach()
        try {
            val childFragmentManager = Fragment::class.java.getDeclaredField("mChildFragmentManager")
            childFragmentManager.isAccessible = true
            childFragmentManager.set(this, null)

        } catch (exception: NoSuchFieldException) {
            exception.printStackTrace()
        } catch (exception: IllegalAccessException) {
            exception.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bookShelfPresenter.clear()
    }

    private fun initUpdateService() {
        if (bookShelfPresenter.updateService != null) return
        val intent = Intent()
        val context = activity.applicationContext
        intent.setClass(context, CheckNovelUpdateService::class.java)
        context.startService(intent)
        context.bindService(intent, bookShelfPresenter.updateConnection, Context.BIND_AUTO_CREATE)
    }

    private fun initRecyclerView() {
        srl_refresh.setHeaderViewBackgroundColor(0x00000000)
        srl_refresh.setHeaderView(refreshHeader)
        srl_refresh.isTargetScrollWithLayout = true

        recl_content.recycledViewPool.setMaxRecycledViews(0, 12)

        val layoutManager = ShelfGridLayoutManager(activity, 1)

        recl_content.layoutManager = layoutManager
        recl_content.isFocusable = false
        recl_content.itemAnimator.addDuration = 0
        recl_content.itemAnimator.changeDuration = 0
        recl_content.itemAnimator.moveDuration = 0
        recl_content.itemAnimator.removeDuration = 0
        (recl_content.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        recl_content.adapter = bookShelfAdapter
    }

    /**
     * 查Book数据库更新界面
     */
    fun updateUI() {
        val isShowAD = !bookShelfAdapter.isRemove && isResumed && !Constants.isHideAD
        doAsync {
            bookShelfPresenter.queryBookListAndAd(activity, isShowAD, true)
            uiThread {
                bookShelfAdapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * 下拉时检查更新
     */
    private fun checkBookUpdate() {

        if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
            srl_refresh.isRefreshing = false
            showToastDelay(R.string.bookshelf_network_error)
            return
        }

        val startPullTime = System.currentTimeMillis()
        val interval = Math.abs(startPullTime - latestLoadDataTime)

        // 刷新间隔小于30秒无效
        if (interval <= PULL_REFRESH_DELAY) {
            srl_refresh.onRefreshComplete()
            showToastDelay(R.string.bookshelf_no_book_update)
        } else {
            // 刷新间隔大于30秒直接请求更新，
            bookShelfPresenter.addUpdateTask(this)
        }
    }

    fun doUpdateBook() {
        bookShelfPresenter.addUpdateTask(this)
    }

    private fun showToastDelay(textId: Int) {
        if (!isAdded) return
        Flowable.timer(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (activity != null && !activity.isFinishing) {
                        activity.applicationContext.toastShort(textId, false)
                    }
                }
    }

    private fun showToastDelay(text: String) {
        if (!isAdded) return
        Flowable.timer(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (activity != null && !activity.isFinishing) {
                        activity.applicationContext.toastShort(text, false)
                    }
                }
    }

    /**
     * 处理被点击或更新通知的book
     */
    private fun handleBook(book: Book?) {
        if (book != null && activity != null && !activity.isFinishing) {
            BookRouter.navigateCoverOrRead(activity, book, BookRouter.NAVIGATE_TYPE_BOOKSHELF)
        }
    }

    override fun onSuccess(result: BookUpdateResult) {
        if (activity != null && !activity.isFinishing) {
            latestLoadDataTime = System.currentTimeMillis()
            bookRackUpdateTime = System.currentTimeMillis()
            if (srl_refresh != null) {
                srl_refresh.onRefreshComplete()
            }
            bookShelfPresenter.handleSuccessUpdate(result)
            AppUtils.setLongPreferences(activity, "book_rack_update_time", bookRackUpdateTime)
            updateUI()
        }
    }

    override fun onException(exception: Exception) {
        if (activity != null && !activity.isFinishing) {
            latestLoadDataTime = System.currentTimeMillis()
            showToastDelay(R.string.bookshelf_refresh_network_problem)
            if (srl_refresh != null) {
                srl_refresh.onRefreshComplete()
            }
        }
    }

    override fun doUpdateBook(updateService: CheckNovelUpdateService) {
        updateService.setBookUpdateListener(activity as CheckNovelUpdateService.OnBookUpdateListener)
        bookShelfPresenter.addUpdateTask(this)
    }

    override fun onBookListQuery(bookList: ArrayList<Book>) {
        if (activity != null && !activity.isFinishing) {
            if (bookList.isEmpty()) {
                srl_refresh.setPullToRefreshEnabled(false)
                rl_empty.visibility = View.VISIBLE
            } else {
                srl_refresh.setPullToRefreshEnabled(true)
                rl_empty.visibility = View.GONE
            }
        }

    }

    override fun onBookDelete() {
        if (activity != null && !activity.isFinishing) {
            updateUI()
            bookShelfDeleteDialog.dismiss()
            dismissRemoveMenu()
        }
    }

    override fun onSuccessUpdateHandle(updateCount: Int, firstBook: BookUpdate?) {
        if (updateCount == 0) {
            showToastDelay(R.string.bookshelf_no_book_update)
        } else {
            val bookName = firstBook?.book_name
            val bookLastChapterName = firstBook?.last_chapter_name
            if (bookName?.isNotEmpty() == true && bookLastChapterName?.isNotEmpty() == true) {
                if (updateCount == 1) {
                    showToastDelay("《$bookName${activity.getString(R.string.bookshelf_book_update_chapter)}" +
                            bookLastChapterName)
                } else {
                    showToastDelay("《$bookName${activity.getString(R.string.bookshelf_books_update_more)}" +
                            "$updateCount${activity.getString(R.string.bookshelf_books_update_chapters)}")
                }
            }
        }
    }

    override fun onAdRefresh() {
        if (activity != null && !activity.isFinishing) {
            bookShelfAdapter.notifyDataSetChanged()
        }
    }

    override fun showRemoveMenu() {
        srl_refresh.setPullToRefreshEnabled(false)
        bookShelfPresenter.removeAd()
        bookShelfAdapter.insertRemoveState(true)
        bookShelfInterface?.changeHomeNavigationState(true)
        removeMenuPopup.show(rl_content)

        srl_refresh.setPadding(0, srl_refresh.paddingTop, 0, popupHeight)
        txt_remove_head_select_all.text = getString(R.string.select_all)
        rl_head_normal.visibility = View.GONE
        rl_head_remove.visibility = View.VISIBLE
        fl_ad_float.visibility = View.GONE
    }

    override fun dismissRemoveMenu() {
        srl_refresh.setPullToRefreshEnabled(true)
        bookShelfAdapter.insertRemoveState(false)
        bookShelfInterface?.changeHomeNavigationState(false)
        removeMenuPopup.dismiss()

        srl_refresh.setPadding(0, srl_refresh.paddingTop, 0, 0)
        txt_remove_head_select_all.text = getString(R.string.cancel_select_all)
        rl_head_normal.visibility = View.VISIBLE
        rl_head_remove.visibility = View.GONE

        BookShelfLogger.uploadBookShelfEditCancel()

        updateUI()

        if (!Constants.isHideAD && Constants.dy_shelf_boundary_switch && bookShelfPresenter.iBookList.isNotEmpty()) {
            bookShelfPresenter.requestFloatAD(activity, fl_ad_float)
        }
    }

    override fun isRemoveMenuShow(): Boolean = bookShelfAdapter.isRemove

    override fun selectAll(isAll: Boolean) {
        bookShelfAdapter.insertSelectAllState(isAll)
        removeMenuPopup.setSelectedNum(bookShelfAdapter.selectedBooks.size)
        BookShelfLogger.uploadBookShelfEditSelectAll(isAll)
    }

    override fun sortBooks(type: Int) {
        CommonContract.insertShelfSortType(type)
        updateUI()
        BookShelfLogger.uploadBookShelfSortType(type)
    }

    override fun deleteBooks(books: ArrayList<Book>, isDeleteCacheOnly: Boolean) {
        bookShelfPresenter.deleteBooks(books, isDeleteCacheOnly)
    }

    companion object {
        private const val PULL_REFRESH_DELAY = 30 * 1000
    }
}