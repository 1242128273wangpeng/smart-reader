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
import com.dingyue.bookshelf.BookShelfAdapter.BookShelfItemListener
import com.dingyue.bookshelf.view.BookShelfDeleteDialog
import com.dingyue.bookshelf.view.BookShelfSortingPopup
import com.dingyue.bookshelf.view.HeadMenuPopup
import com.dingyue.bookshelf.view.RemoveMenuPopup
import com.dingyue.contract.CommonContract
import com.dingyue.contract.router.BookRouter
import com.dingyue.contract.router.BookRouter.NAVIGATE_TYPE_BOOKSHELF
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dingyue.contract.util.SharedPreUtil
import com.dingyue.contract.util.showToastMessage
import com.dy.media.MediaControl
import kotlinx.android.synthetic.txtqbmfyd.bookshelf_refresh_header.view.*
import kotlinx.android.synthetic.txtqbmfyd.frag_bookshelf.*
import net.lzbook.kit.book.component.service.CheckNovelUpdateService
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.UpdateCallBack
import net.lzbook.kit.data.bean.BookUpdateResult
import net.lzbook.kit.pulllist.SuperSwipeRefreshLayout
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.doAsync
import net.lzbook.kit.utils.uiThread

class BookShelfFragment : Fragment(), UpdateCallBack, BookShelfView, MenuManager {

    private val popupHeight by lazy {
        resources.getDimensionPixelSize(R.dimen.bookshelf_popup_height)
    }

    private val bookShelfPresenter: BookShelfPresenter by lazy { BookShelfPresenter(this) }
    private val sharePre:SharedPreUtil by lazy { SharedPreUtil(SharedPreUtil.SHARE_DEFAULT) }

    private var latestLoadDataTime: Long = 0

    private var bookShelfInterface: BookShelfInterface? = null

    private val refreshHeader: View by lazy {
        LayoutInflater.from(srl_refresh.context).inflate(R.layout.bookshelf_refresh_header, null)
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
        popup.setOnCancelClickListener {
            dismissRemoveMenu()
            BookShelfLogger.uploadBookShelfEditCancel()
        }
        popup
    }

    private val bookShelfSortingPopup: BookShelfSortingPopup by lazy {
        val popup = BookShelfSortingPopup(requireActivity())
        popup.setOnRecentReadClickListener {
            sortBooks(0)
        }
        popup.setOnUpdateTimeClickListener {
            sortBooks(1)
        }
        popup
    }

    val bookShelfAdapter: BookShelfAdapter by lazy {
        BookShelfAdapter(object : BookShelfItemListener {
            override fun clickedBookShelfItem(book: Book?, position: Int) {

                if (position < 0 || position > bookShelfPresenter.iBookList.size) {
                    return
                }

                if (!bookShelfAdapter.isRemove) {
                    if (position == bookShelfPresenter.iBookList.size) {
                        bookShelfInterface?.changeHomePagerIndex(1)
                        return
                    }

                    if (position >= bookShelfPresenter.iBookList.size || position < 0) {
                        return
                    }

                    if (book != null) {
                        handleBook(book)
                        BookShelfLogger.uploadBookShelfBookClick(book, position)
                    }
                } else {
                    bookShelfAdapter.insertSelectedPosition(position)
                    removeMenuPopup.setSelectedNum(bookShelfAdapter.selectedBooks.size)
                    txt_editor_select_all.text = if (bookShelfAdapter.isSelectedAll()) getString(R.string.cancel_select_all) else getString(R.string.select_all)
                }
            }

            override fun longClickedBookShelfItem(): Boolean {
                if (!bookShelfAdapter.isRemove) {
                    showRemoveMenu()
                    BookShelfLogger.uploadBookShelfLongClickBookShelfEdit()
                }
                return false
            }

        }, bookShelfPresenter.iBookList, true)
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.frag_bookshelf, container, false)
    }

    fun dimissPersonRed(){
        redpoint_home_setting?.visibility = View.GONE
        sharePre.putBoolean(SharedPreUtil.BOOKSHELF_PERSON_RED,true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        MediaControl.insertBookShelfMediaType(true)

        initRecyclerView()

        if(sharePre.getBoolean(SharedPreUtil.BOOKSHELF_PERSON_RED,false)){
            redpoint_home_setting?.visibility = View.GONE
        }
        srl_refresh.setOnPullRefreshListener(object : SuperSwipeRefreshLayout.OnPullRefreshListener {
            override fun onRefresh() {
                refreshHeader.txt_refresh_prompt.text = getString(R.string.refresh_running)
                refreshHeader.img_refresh_arrow.visibility = View.GONE
                refreshHeader.pgbar_refresh_loading.visibility = View.VISIBLE
                checkBookUpdate()
            }

            override fun onPullDistance(distance: Int) {}

            override fun onPullEnable(enable: Boolean) {
                refreshHeader.pgbar_refresh_loading.visibility = View.GONE
                refreshHeader.txt_refresh_prompt.text = if (enable) getString(R.string.refresh_release) else getString(R.string.refresh_start)
                refreshHeader.img_refresh_arrow.visibility = View.VISIBLE
                refreshHeader.img_refresh_arrow.rotation = (if (enable) 180 else 0).toFloat()
            }
        })
        txt_head_title.text = getString(R.string.bookshelf)

        img_head_personal.setOnClickListener {
            bookShelfInterface?.changeDrawerLayoutState()
            BookShelfLogger.uploadBookShelfPersonal()
            sharePre.putBoolean(SharedPreUtil.BOOKSHELF_PERSON_RED,true)
            redpoint_home_setting.visibility = View.GONE
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
            if (CommonContract.isDoubleClick(System.currentTimeMillis())) {
                return@setOnClickListener
            }
            if (txt_editor_select_all.text == getString(R.string.select_all)) {
                txt_editor_select_all.text = getString(R.string.cancel_select_all)
                selectAll(true)
            } else {
                txt_editor_select_all.text = getString(R.string.select_all)
                selectAll(false)
            }
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

        val bookshelfLayoutManager = ShelfGridLayoutManager(activity, 3)

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

        recl_content.topShadow = img_head_shadow
    }

    private fun createHeaderView(): View {
        refreshHeader.txt_refresh_prompt.text = getString(R.string.refresh_start)
        refreshHeader.img_refresh_arrow.visibility = View.VISIBLE
        refreshHeader.img_refresh_arrow.setImageResource(R.drawable.pulltorefresh_down_arrow)
        refreshHeader.pgbar_refresh_loading.visibility = View.GONE
        return refreshHeader
    }

    /**
     * 查Book数据库更新界面
     */
    fun updateUI() {
        val isShowAD = !bookShelfAdapter.isRemove && isResumed && !Constants.isHideAD && Constants.book_shelf_state != 0
        bookShelfPresenter.queryBookListAndAd(requireActivity(), isShowAD, true)
        uiThread {
            bookShelfAdapter.notifyDataSetChanged()
            BookShelfLogger.uploadFirstOpenBooks()
        }
        if(bookShelfPresenter.iBookList.isNotEmpty()){
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
        BookRouter.navigateCoverOrRead(requireActivity(), book, NAVIGATE_TYPE_BOOKSHELF)
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
                srl_refresh!!.onRefreshComplete()
            }
            bookShelfPresenter.handleSuccessUpdate(result)
        }
    }

    override fun onException(exception: Exception) {
        latestLoadDataTime = System.currentTimeMillis()

        if (isAdded) {
            requireActivity().applicationContext.showToastMessage(R.string.bookshelf_network_error, 2000L)
        }

        if (srl_refresh != null) {
            srl_refresh.onRefreshComplete()
        }
    }

    override fun doUpdateBook(updateService: CheckNovelUpdateService) {
        if (activity != null) {
            updateService.setBookUpdateListener(activity as CheckNovelUpdateService.OnBookUpdateListener)
            bookShelfPresenter.addUpdateTask(this)
        }
    }

    override fun onBookListQuery(books: List<Book>?) {
        if (books != null && books.isNotEmpty()) {
            srl_refresh?.setPullToRefreshEnabled(true)
            ll_empty?.visibility = View.GONE
        } else {
            srl_refresh?.setPullToRefreshEnabled(false)
            ll_empty?.visibility = View.VISIBLE
        }
    }

    override fun onBookDelete() {
        updateUI()
        bookShelfDeleteDialog.dismiss()
        dismissRemoveMenu()
        requireActivity().applicationContext.showToastMessage(R.string.bookshelf_delete_success)
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
                } else if (activity != null) {
                    requireActivity().applicationContext.showToastMessage(
                            "《$bookName${activity?.getString(R.string.bookshelf_books_update_more)}"
                                    + "$updateCount${activity?.getString(R.string.bookshelf_books_update_chapters)}",
                            2000L)
                }
            }
        }
    }

    override fun onAdRefresh() {
        bookShelfAdapter.notifyDataSetChanged()
    }

    override fun showRemoveMenu() {
        srl_refresh.setPullToRefreshEnabled(false)

        bookShelfPresenter.removeAd()

        bookShelfAdapter.insertRemoveState(true)

        bookShelfInterface?.changeHomeNavigationState(true)

        fl_ad_float.visibility = View.GONE

        removeMenuPopup.show(ll_content)

        changeHeaderState(true)

        rl_content.setPadding(0, rl_content.paddingTop, 0, popupHeight)

        txt_editor_select_all.text = getString(R.string.select_all)
    }

    override fun dismissRemoveMenu() {
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