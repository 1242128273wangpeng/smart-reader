package com.dingyue.bookshelf

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ding.basic.bean.Book
import com.ding.basic.bean.BookUpdate
import com.dingyue.bookshelf.childmvp.ChildBookShelfPresenter
import com.dingyue.bookshelf.childmvp.ChildBookShelfView
import com.dingyue.bookshelf.view.*
import com.dingyue.contract.CommonContract
import com.dingyue.contract.router.BookRouter
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dingyue.contract.util.showToastMessage
import com.dy.media.MediaControl
import kotlinx.android.synthetic.qbmfkkydq.bookshelf_refresh_header.view.*
import kotlinx.android.synthetic.qbmfkkydq.frag_bookshelf.*
import net.lzbook.kit.book.component.service.CheckNovelUpdateService
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.UpdateCallBack
import net.lzbook.kit.data.bean.BookUpdateResult
import net.lzbook.kit.pulllist.SuperSwipeRefreshLayout
import net.lzbook.kit.rvextension.HFRecyclerControl
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.uiThread
import java.util.*

/**
 * 书架页Fragment
 */

class BookShelfFragment : Fragment(), UpdateCallBack, ChildBookShelfView, MenuManager {

    override fun onCurrentBookComplete(book: Book?, title: String?) {

        addHeaderView(book, title)
    }


    private val popupHeight by lazy {
        resources.getDimensionPixelSize(R.dimen.bookshelf_popup_height)
    }

    private var iconBgViewHeight = 1
    private var headerViewHeight = 1
    private var mScrollDistance = 0
    private var isEditMode = false// 是否在编辑模式

    private val bookShelfPresenter: ChildBookShelfPresenter by lazy { ChildBookShelfPresenter(this) }

    private var latestLoadDataTime: Long = 0

    private var bookShelfInterface: BookShelfInterface? = null

    private val refreshHeader: View by lazy {
        LayoutInflater.from(srl_refresh.context).inflate(R.layout.bookshelf_refresh_header, null)
    }

    private val bookshelfLayoutManager: ShelfGridLayoutManager by lazy {
        ShelfGridLayoutManager(activity, 3)
    }

    private val headMenuPopup: HeadMenuPopup by lazy {
        val popup = HeadMenuPopup(requireContext().applicationContext)
        popup.setOnDownloadClickListener {
            RouterUtil.navigation(requireActivity(), RouterConfig.DOWNLOAD_MANAGER_ACTIVITY)
            BookShelfLogger.uploadBookShelfCacheManager()
        }
        popup.setOnSortingClickListener {
            bookShelfSortingPopup.show(rl_content)
            BookShelfLogger.uploadBookShelfBookSort()
        }
        popup
    }

    private val removeMenuPopup: RemoveMenuPopup by lazy {
        val popup = RemoveMenuPopup(requireContext())
        popup.setOnDeletedClickListener {
            bookShelfDeleteDialog.show(bookShelfAdapter.selectedBooks)
        }
        popup
    }

    private val bookShelfSortingPopup: BookShelfSortingPopup by lazy {
        val popup = BookShelfSortingPopup(requireActivity())
        popup.setOnRecentAddClickListener {
            sortBooks(2)
        }
        popup.setOnRecentReadClickListener {
            sortBooks(0)
        }
        popup.setOnUpdateTimeClickListener {
            sortBooks(1)
        }
        popup
    }

    val bookShelfAdapter: BookShelfAdapter by lazy {
        BookShelfAdapter(object : BookShelfAdapter.BookShelfItemListener {
            override fun clickedBookShelfItem(book: Book?, position: Int) {
                val realPosition = position - hfRecyclerControl.getHeaderCount()

                if (realPosition < 0 || realPosition > bookShelfPresenter.iBookList.size) {
                    return
                }

                if (!bookShelfAdapter.isRemove) {
                    if (realPosition == bookShelfPresenter.iBookList.size) {
                        bookShelfInterface?.changeHomePagerIndex(1)
                        return
                    }

                    if (realPosition >= bookShelfPresenter.iBookList.size || realPosition < 0) {
                        return
                    }

                    if (book != null) {
                        handleBook(book)
                        BookShelfLogger.uploadBookShelfBookClick(book, realPosition)
                    }
                } else {
                    bookShelfAdapter.insertSelectedPosition(realPosition)
                    removeMenuPopup.setSelectedNum(bookShelfAdapter.selectedBooks.size)
                    txt_editor_select_all.text = if (bookShelfAdapter.isSelectedAll()) getString(R.string.cancel_select_all) else getString(R.string.select_all)
                }
            }

            override fun longClickedBookShelfItem(): Boolean {
                if (!bookShelfAdapter.isRemove) {
                    showRemoveMenu()
                    isEditMode = true

                    BookShelfLogger.uploadBookShelfLongClickBookShelfEdit()
                }
                return false
            }

        }, bookShelfPresenter.iBookList, true)
    }

    /**
     * 如果编辑书籍状态下，headerview不可点击
     */
    private fun changeHeaderViewState(edit: Boolean) {
        headerView.setViewClickEnable(!edit)
    }

    private val bookShelfDeleteDialog: BookShelfDeleteDialog by lazy {
        val dialog = BookShelfDeleteDialog(requireActivity())
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

        //根据书架数量确定是否刷新
        if (bookShelfPresenter.iBookList.size > 0) {
            srl_refresh.isRefreshing = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //        设置默认排序方式为添加时间
        Constants.book_list_sort_type = CommonContract.queryBookSortingType()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.frag_bookshelf, container, false)
    }

    var titleHeight = 0
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        headerViewHeight = AppUtils.dip2px(view.context, 144f)
        MediaControl.insertBookShelfMediaType(true)

        initRecyclerView()
        if (fl_bg_layout != null) {
            fl_bg_layout.post {
                iconBgViewHeight = fl_bg_layout.height
            }
        } else {
            iconBgViewHeight = AppUtils.dip2px(context, 220f)
        }

        ll_container?.let {
            it.post {
                titleHeight = it.height
            }
        }

        srl_refresh.setOnPullRefreshListener(object : SuperSwipeRefreshLayout.OnPullRefreshListener {
            override fun onRefresh() {
                refreshHeader.txt_refresh_prompt.text = getString(R.string.refresh_running)
                refreshHeader.img_refresh_arrow.visibility = View.GONE
                refreshHeader.pgbar_refresh_loading.visibility = View.VISIBLE
                checkBookUpdate()
            }

            override fun onPullDistance(distance: Int) {
                if (distance >= 0) {
                    fl_bg_layout.layoutParams.height = iconBgViewHeight + distance
                }

            }

            override fun onPullEnable(enable: Boolean) {
                refreshHeader.pgbar_refresh_loading.visibility = View.GONE
                refreshHeader.txt_refresh_prompt.text = if (enable) getString(R.string.refresh_release) else getString(R.string.refresh_start)
                refreshHeader.img_refresh_arrow.visibility = View.VISIBLE
                refreshHeader.img_refresh_arrow.rotation = (if (enable) 180 else 0).toFloat()
            }
        })

        img_head_personal.setOnClickListener {
            RouterUtil.navigation(requireActivity(), RouterConfig.SETTING_ACTIVITY)

            BookShelfLogger.uploadBookShelfPersonal()
        }

        img_head_search.setOnClickListener {
            RouterUtil.navigation(requireActivity(), RouterConfig.SEARCH_BOOK_ACTIVITY)
            BookShelfLogger.uploadBookShelfSearch()
        }

        img_head_menu.setOnClickListener {
            headMenuPopup.show(img_head_menu)
            BookShelfLogger.uploadBookShelfMore()
        }

        txt_editor_select_all.setOnClickListener {
            if (txt_editor_select_all.text == getString(R.string.select_all)) {
                txt_editor_select_all.text = getString(R.string.cancel_select_all)
                selectAll(true)
            } else {
                txt_editor_select_all.text = getString(R.string.select_all)
                selectAll(false)
            }
        }

        txt_editor_finish.setOnClickListener {
            isEditMode = false
            dismissRemoveMenu()
        }

        txt_empty_add_book.setOnClickListener {
            bookShelfInterface?.changeHomePagerIndex(1)
            BookShelfLogger.uploadBookShelfToBookCity()
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
        if (bookShelfPresenter.updateService != null) {
            return
        }

        val intent = Intent()
        val context = requireActivity().applicationContext
        intent.setClass(context, CheckNovelUpdateService::class.java)
        context.startService(intent)
        context.bindService(intent, bookShelfPresenter.updateConnection, Context.BIND_AUTO_CREATE)
    }

    private fun initRecyclerView() {
        srl_refresh.setHeaderViewBackgroundColor(0x00000000)
        srl_refresh.setHeaderView(createHeaderView())
        srl_refresh.isTargetScrollWithLayout = true
        recl_content.recycledViewPool.setMaxRecycledViews(0, 12)


        val bookshelfShelfSpanSizeLookup = BookShelfSpanSizeLookup(bookShelfAdapter)
        bookshelfLayoutManager.spanSizeLookup = bookshelfShelfSpanSizeLookup

        recl_content.layoutManager = bookshelfLayoutManager
        recl_content.isFocusable = false
        recl_content.itemAnimator.addDuration = 0
        recl_content.itemAnimator.changeDuration = 0
        recl_content.itemAnimator.moveDuration = 0
        recl_content.itemAnimator.removeDuration = 0

        (recl_content.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        recl_content.adapter = bookShelfAdapter

        recl_content.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                mScrollDistance += dy
                if (mScrollDistance > headerViewHeight) {
                    mScrollDistance = headerViewHeight
                } else if (mScrollDistance < 0) {
                    mScrollDistance = 0
                }
                if (bookshelfLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                    mScrollDistance = 0
                }
                val percent = mScrollDistance * 1f / headerViewHeight

                setTitleLayoutAlpha(percent)

            }
        })

    }

    /**
     * 添加头部视图，正在阅读的书籍
     */
    private val hfRecyclerControl: HFRecyclerControl by lazy {
        HFRecyclerControl()
    }

    private val headerView: BookShelfHeaderView by lazy {
        BookShelfHeaderView(recl_content.context)
    }

    /**
     * 当前阅读书籍不为空时添加头部视图,为空时显示头部文案提示
     */
    private fun addHeaderView(cReadBook: Book?, cTitle: String?) {

        if (hfRecyclerControl.getHeaderCount() == 0) {
            hfRecyclerControl.setAdapter(recl_content, bookShelfAdapter)
            hfRecyclerControl.addHeaderView(headerView)
            headerView.post {
                if (headerView.height != 0) {
                    headerViewHeight = headerView.height
                }
//                var paddingTop = iconBgViewHeight - titleHeight - headerViewHeight
//                headerView.setPadding(headerView.paddingLeft, paddingTop, headerView.paddingRight, headerView.paddingBottom)

            }
        }
        headerView.setData(cReadBook, cTitle, activity!!)

    }

    /**
     * 改变头部headerview的透明度
     */
    private fun setTitleLayoutAlpha(percent: Float) {
        if (!isEditMode) {
            var rAlpha = (255 * percent).toInt()
            if (rAlpha > 255) {
                rAlpha = 255
            }
            ll_container.setBackgroundColor(Color.argb(rAlpha, 42, 202, 176))
            var rPercent = percent
            if (rPercent > 1) {
                rPercent = 1f
            }
            headerView.alpha = (1 - rPercent)
        }

    }

    private fun createHeaderView(): View {
        refreshHeader.txt_refresh_prompt.text = getString(R.string.refresh_start)
        refreshHeader.img_refresh_arrow.visibility = View.VISIBLE
        refreshHeader.img_refresh_arrow.setImageResource(R.drawable.pulltorefresh_down_arrow_white)
        refreshHeader.pgbar_refresh_loading.visibility = View.GONE
        return refreshHeader
    }

    /**
     * 查Book数据库更新界面
     */
    fun updateUI() {
        val isShowAD = !bookShelfAdapter.isRemove && isResumed && !Constants.isHideAD && Constants.book_shelf_state != 0
        bookShelfPresenter.queryCurrentReadBook()
        bookShelfPresenter.queryBookListAndAd(requireActivity(), isShowAD, true)
        uiThread {
            bookShelfAdapter.notifyDataSetChanged()

            if (bookShelfAdapter.itemCount > 0 && bookShelfInterface != null) {
                bookShelfInterface?.checkShowShelfGuide()
            }
            BookShelfLogger.uploadFirstOpenBooks()
        }
        if (bookShelfPresenter.iBookList.isNotEmpty()) {
            BookShelfLogger.uploadFirstOpenBooks()
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
    private fun handleBook(book: Book) {
        if (isAdded && !requireActivity().isFinishing) {
            BookRouter.navigateCoverOrRead(requireActivity(), book, BookRouter.NAVIGATE_TYPE_BOOKSHELF)
        }

    }

    /***
     * 更改头部布局状态
     * **/
    private fun changeHeaderState(state: Boolean) {
        if (state) {
            if (rl_head_normal.isShown) {
                rl_head_normal.visibility = View.GONE
            }

            if (!rl_head_editor.isShown) {
                rl_head_editor.visibility = View.VISIBLE
            }
        } else {
            if (!rl_head_normal.isShown) {
                rl_head_normal.visibility = View.VISIBLE
            }

            if (rl_head_editor.isShown) {
                rl_head_editor.visibility = View.GONE
            }
        }
    }

    override fun onSuccess(result: BookUpdateResult) {
        if (isAdded && activity?.isFinishing == false) {
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
        if (activity != null) {
            updateService.setBookUpdateListener(activity as CheckNovelUpdateService.OnBookUpdateListener)
            bookShelfPresenter.addUpdateTask(this)
        }
    }

    override fun onBookListQuery(books: List<Book>?) {
        if (isAdded && !requireActivity().isFinishing) {
            if (books != null && books.isNotEmpty()) {
                srl_refresh?.setPullToRefreshEnabled(true)
                srl_refresh?.visibility = View.VISIBLE
                ll_empty?.visibility = View.GONE
            } else {
                srl_refresh?.setPullToRefreshEnabled(false)
                ll_empty?.visibility = View.VISIBLE
                srl_refresh?.visibility = View.GONE
            }
        }

    }

    override fun onBookDelete() {
        if (isAdded && !requireActivity().isFinishing) {
            updateUI()
            isEditMode = false
            bookShelfDeleteDialog.dismiss()
            dismissRemoveMenu()
            requireActivity().applicationContext.showToastMessage(R.string.bookshelf_delete_success)
        }
    }

    override fun onSuccessUpdateHandle(updateCount: Int, firstBook: BookUpdate?) {
        if (activity == null || requireActivity().isFinishing) {
            return
        }
        if (updateCount == 0) {
            if (isAdded) {
                requireActivity().applicationContext.showToastMessage(R.string.bookshelf_no_book_update, 2000L)
            }
        } else {
            val bookName = firstBook?.book_name
            val bookLastChapterName = firstBook?.last_chapter_name
            if (bookName?.isNotEmpty() == true && bookLastChapterName?.isNotEmpty() == true) {
                if (updateCount == 1 && activity != null) {
                    if (isAdded) {
                        requireActivity().applicationContext.showToastMessage(
                                "《$bookName${activity?.getString(R.string.bookshelf_book_update_chapter)}" + "$bookLastChapterName",
                                2000L)
                    }
                } else {
                    if (isAdded) {
                        requireActivity().applicationContext.showToastMessage(
                                "《$bookName${activity?.getString(R.string.bookshelf_books_update_more)}"
                                        + "$updateCount${activity?.getString(R.string.bookshelf_books_update_chapters)}",
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
        changeHeaderViewState(true)
        srl_refresh.setPullToRefreshEnabled(false)

        bookShelfPresenter.removeAd()

        bookShelfAdapter.insertRemoveState(true)

        bookShelfInterface?.changeHomeNavigationState(true)

        fl_ad_float.visibility = View.GONE

        removeMenuPopup.show(ll_content)

        changeHeaderState(true)

//        rl_content.setPadding(0, rl_content.paddingTop, 0, popupHeight)

        txt_editor_select_all.text = getString(R.string.select_all)
    }

    override fun dismissRemoveMenu() {
        changeHeaderViewState(false)
        srl_refresh.setPullToRefreshEnabled(true)

        bookShelfAdapter.insertRemoveState(false)

        bookShelfInterface?.changeHomeNavigationState(false)

        removeMenuPopup.dismiss()

        changeHeaderState(false)

        rl_content.setPadding(0, rl_content.paddingTop, 0, 0)

        txt_editor_select_all.text = getString(R.string.cancel_select_all)

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