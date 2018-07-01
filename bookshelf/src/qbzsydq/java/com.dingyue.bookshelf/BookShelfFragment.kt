package com.dingyue.bookshelf

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ding.basic.bean.Book
import com.ding.basic.bean.BookUpdate
import com.dingyue.bookshelf.view.BookShelfDeleteDialog
import com.dingyue.bookshelf.view.RemoveMenuPopup
import com.dingyue.contract.CommonContract
import com.dingyue.contract.router.BookRouter
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dingyue.contract.util.showToastMessage
import com.dy.media.MediaControl
import de.greenrobot.event.EventBus
import kotlinx.android.synthetic.qbzsydq.bookshelf_refresh_header.view.*
import kotlinx.android.synthetic.qbzsydq.frag_bookshelf.*
import net.lzbook.kit.book.component.service.CheckNovelUpdateService
import net.lzbook.kit.book.view.ConsumeEvent
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.UpdateCallBack
import net.lzbook.kit.data.bean.BookUpdateResult
import net.lzbook.kit.pulllist.SuperSwipeRefreshLayout
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.doAsync
import net.lzbook.kit.utils.uiThread

/**
 * 书架页Fragment
 */
class BookShelfFragment : Fragment(), UpdateCallBack, BookShelfView, MenuManager {

    private val popupHeight by lazy {
        resources.getDimensionPixelSize(R.dimen.bookshelf_popup_height)
    }

    val bookShelfPresenter: BookShelfPresenter by lazy { BookShelfPresenter(this) }

    private var latestLoadDataTime: Long = 0

    private var bookShelfInterface: BookShelfInterface? = null

    private val refreshHeader by lazy {
        LayoutInflater.from(requireActivity()).inflate(R.layout.bookshelf_refresh_header, null)
    }

    private val removeMenuPopup: RemoveMenuPopup by lazy {
        val popup = RemoveMenuPopup(requireActivity())
        popup.onSelectAllClickListener = { isSelectAll ->
            selectAll(isSelectAll)
        }
        popup.onDeleteClickListener = {
            bookShelfDeleteDialog.show(bookShelfAdapter.selectedBooks)
        }
        popup
    }

    val bookShelfAdapter: BookShelfAdapter by lazy {
        BookShelfAdapter(object : BookShelfAdapter.BookShelfItemListener {
            override fun clickedBookShelfItem(book: Book?, position: Int) {
                if (position < 0 || position >= bookShelfPresenter.iBookList.size) return
                if (isRemoveMenuShow()) {
                    bookShelfAdapter.insertSelectedPosition(position)
                    removeMenuPopup.setSelectedNum(bookShelfAdapter.selectedBooks.size)
                    removeMenuPopup.setSelectAllText(if (bookShelfAdapter.isSelectedAll()) getString(R.string.cancel_select_all) else getString(R.string.select_all))
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
        val dialog = BookShelfDeleteDialog(requireActivity())
        dialog.onConfirmListener = { books, isDeleteCacheOnly ->
            deleteBooks(books, isDeleteCacheOnly)
        }
        dialog.onCancelListener = {
            BookShelfLogger.uploadBookShelfEditDelete(0, null, false)
        }
        dialog
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            bookShelfInterface = context as BookShelfInterface
        } catch (classCastException: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement BookShelfInterface")
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initUpdateService()

        if (bookShelfPresenter.iBookList.size > 0) {
            srl_refresh.isRefreshing = true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.frag_bookshelf, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        MediaControl.insertBookShelfMediaType(false)

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
        img_empty_find.setOnClickListener {
            bookShelfInterface?.changeHomePagerIndex(1)
            BookShelfLogger.uploadBookShelfToBookCity()
        }

        img_head_setting.setOnClickListener {
            BookShelfLogger.uploadBookShelfPersonal()
            EventBus.getDefault().post(ConsumeEvent(R.id.fup_head_personal))
            RouterUtil.navigation(requireActivity(), RouterConfig.SETTING_ACTIVITY)
        }

        img_head_search.setOnClickListener {
            val bundle = Bundle()
            bundle.putBoolean("isShowLastSearch", false)
            RouterUtil.navigation(requireActivity(), RouterConfig.SEARCH_BOOK_ACTIVITY, bundle)
            BookShelfLogger.uploadBookShelfSearch()
        }

        img_download_manager.setOnClickListener {
            RouterUtil.navigation(requireActivity(), RouterConfig.DOWNLOAD_MANAGER_ACTIVITY)
            BookShelfLogger.uploadBookShelfCacheManager()
        }

        img_remove_back.setOnClickListener {
            dismissRemoveMenu()
        }

        txt_remove_cancel.setOnClickListener {
            dismissRemoveMenu()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()

        if (!Constants.isHideAD && Constants.dy_shelf_boundary_switch && bookShelfPresenter.iBookList.isNotEmpty()) {
            bookShelfPresenter.requestFloatAD(requireActivity(), fl_ad_float)
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
        val context = requireActivity().applicationContext
        intent.setClass(context, CheckNovelUpdateService::class.java)
        context.startService(intent)
        context.bindService(intent, bookShelfPresenter.updateConnection, Context.BIND_AUTO_CREATE)
    }


    private fun initRecyclerView() {
        srl_refresh.setHeaderViewBackgroundColor(0x00000000)
        srl_refresh.setHeaderView(refreshHeader)
        srl_refresh.isTargetScrollWithLayout = true

        recl_content.recycledViewPool.setMaxRecycledViews(0, 12)

        val layoutManager = ShelfGridLayoutManager(requireActivity(), 1)

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
        bookShelfPresenter.queryBookListAndAd(requireActivity(), isShowAD, true)
        uiThread {
            bookShelfAdapter.notifyDataSetChanged()
        }
    }

    /**
     * 下拉时检查更新
     */
    private fun checkBookUpdate() {

        if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
            srl_refresh.isRefreshing = false
            if (isAdded) {
                requireActivity().applicationContext.showToastMessage(R.string.bookshelf_network_error, 2000L)
            }
            return
        }

        val startPullTime = System.currentTimeMillis()
        val interval = Math.abs(startPullTime - latestLoadDataTime)

        // 刷新间隔小于30秒无效
        if (interval <= PULL_REFRESH_DELAY) {
            srl_refresh.onRefreshComplete()
            if (isAdded) {
                requireActivity().applicationContext.showToastMessage(R.string.bookshelf_no_book_update, 2000L)
            }
        } else {
            // 刷新间隔大于30秒直接请求更新，
            bookShelfPresenter.addUpdateTask(this)
        }
    }

    fun doUpdateBook() {
        bookShelfPresenter.addUpdateTask(this)
    }

    /**
     * 处理被点击或更新通知的book
     */
    private fun handleBook(book: Book?) {
        if (book != null && isAdded && !requireActivity().isFinishing) {
            BookRouter.navigateCoverOrRead(requireActivity(), book, BookRouter.NAVIGATE_TYPE_BOOKSHELF)
        }
    }

    override fun onSuccess(result: BookUpdateResult) {
        if (isAdded && !requireActivity().isFinishing) {
            latestLoadDataTime = System.currentTimeMillis()
            if (srl_refresh != null) {
                srl_refresh.onRefreshComplete()
            }
            bookShelfPresenter.handleSuccessUpdate(result)
            updateUI()
        }
    }

    override fun onException(exception: Exception) {
        if (isAdded && !requireActivity().isFinishing) {
            latestLoadDataTime = System.currentTimeMillis()
            if (isAdded) {
                requireActivity().applicationContext.showToastMessage(R.string.bookshelf_network_error, 2000L)
            }
            if (srl_refresh != null) {
                srl_refresh.onRefreshComplete()
            }
        }
    }

    override fun doUpdateBook(updateService: CheckNovelUpdateService) {
        updateService.setBookUpdateListener(requireActivity() as CheckNovelUpdateService.OnBookUpdateListener)
        bookShelfPresenter.addUpdateTask(this)
    }

    override fun onBookListQuery(books: List<Book>?) {
        if (isAdded && !requireActivity().isFinishing) {
            if (books == null || books.isEmpty()) {
                srl_refresh.setPullToRefreshEnabled(false)
                ll_empty.visibility = View.VISIBLE
            } else {
                srl_refresh.setPullToRefreshEnabled(true)
                ll_empty.visibility = View.GONE
            }
        }
    }

    override fun onBookDelete() {
        if (isAdded && !requireActivity().isFinishing) {
            updateUI()
            bookShelfDeleteDialog.dismiss()
            dismissRemoveMenu()
        }
    }

    override fun onSuccessUpdateHandle(updateCount: Int, firstBook: BookUpdate?) {
        if (updateCount == 0) {
            if (isAdded) {
                requireActivity().applicationContext.showToastMessage(R.string.bookshelf_no_book_update, 2000L)
            }
        } else {
            val bookName = firstBook?.book_name
            val bookLastChapterName = firstBook?.last_chapter_name
            if (bookName?.isNotEmpty() == true && bookLastChapterName?.isNotEmpty() == true) {
                if (updateCount == 1) {
                    if (isAdded) {
                        requireActivity().applicationContext.showToastMessage("《$bookName${requireActivity().getString(R.string.bookshelf_book_update_chapter)}" + bookLastChapterName,
                                2000L)
                    }
                } else {
                    if (isAdded) {
                        requireActivity().applicationContext.showToastMessage("《$bookName${requireActivity().getString(R.string.bookshelf_books_update_more)}" + "$updateCount${requireActivity().getString(R.string.bookshelf_books_update_chapters)}",
                                2000L)
                    }
                }
            }
        }
    }

    override fun onAdRefresh() {
        if (isAdded && !requireActivity().isFinishing) {
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
        rl_head_normal.visibility = View.VISIBLE
        rl_head_remove.visibility = View.GONE

        BookShelfLogger.uploadBookShelfEditCancel()

        updateUI()

        if (!Constants.isHideAD && Constants.dy_shelf_boundary_switch && bookShelfPresenter.iBookList.isNotEmpty()) {
            bookShelfPresenter.requestFloatAD(requireActivity(), fl_ad_float)
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