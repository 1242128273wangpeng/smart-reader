package com.dingyue.bookshelf

import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ding.basic.bean.Book
import com.ding.basic.bean.BookUpdate
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.dingyue.bookshelf.BookShelfAdapter.BookShelfItemListener
import com.dingyue.bookshelf.view.BookShelfDeleteDialog
import com.dingyue.bookshelf.view.BookShelfSortingPopup
import com.dingyue.bookshelf.view.HeadMenuPopup
import com.dingyue.bookshelf.view.RemoveMenuPopup
import com.dy.media.MediaControl
import kotlinx.android.synthetic.mfxsqbyd.bookshelf_refresh_header.view.*
import kotlinx.android.synthetic.mfxsqbyd.frag_bookshelf.*
import net.lzbook.kit.bean.BookUpdateResult
import net.lzbook.kit.bean.UpdateCallBack
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.service.CheckNovelUpdateService
import net.lzbook.kit.ui.widget.ApplicationShareDialog
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.book.CommonContract
import net.lzbook.kit.utils.oneclick.OneClickUtil
import net.lzbook.kit.utils.router.BookRouter
import net.lzbook.kit.utils.router.BookRouter.NAVIGATE_TYPE_BOOKSHELF
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.utils.uiThread
import net.lzbook.kit.ui.widget.pulllist.SuperSwipeRefreshLayout

class BookShelfFragment : Fragment(), UpdateCallBack, BookShelfView, MenuManager {

    private val popupHeight by lazy {
        resources.getDimensionPixelSize(R.dimen.bookshelf_popup_height)
    }

    private val bookShelfPresenter: BookShelfPresenter by lazy { BookShelfPresenter(this) }

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
        popup.setOnImportClickListener {
            RouterUtil.navigation(requireActivity(), RouterConfig.LOCAL_IMPORT_ACTIVITY)
            BookShelfLogger.uploadBookShelfLocalImport()
        }
        popup.setOnShareListener {
            applicationShareDialog.show()
            bookShelfInterface?.registerShareCallback(true)
            BookShelfLogger.uploadBookShelfShare()
        }
        popup.setOnGoneListener {
            view_head_menu.visibility = View.GONE
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
                        BookShelfLogger.uploadBookShelfBookClick(book, position)
                        handleBook(book)
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

    private val applicationShareDialog: ApplicationShareDialog by lazy {
        val dialog = ApplicationShareDialog(requireActivity())
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        MediaControl.insertBookShelfMediaType(true)

        initRecyclerView()

        srl_refresh.setOnPullRefreshListener(object : SuperSwipeRefreshLayout.OnPullRefreshListener {
            override fun onRefresh() {
                refreshHeader.img_head.visibility = View.GONE
                refreshHeader.img_anim.visibility = View.VISIBLE
                (refreshHeader.img_anim.drawable as AnimationDrawable).start()
                checkBookUpdate()
            }

            override fun onPullDistance(distance: Int) {}

            override fun onPullEnable(enable: Boolean) {
                refreshHeader.img_anim.visibility = View.GONE
                refreshHeader.img_head.visibility = View.VISIBLE
//                headerView.img_anim.rotation = (if (enable) 180 else 0).toFloat()
                refreshHeader.img_head.setImageResource((if (enable) R.drawable.refresh_head_pull_light_1 else R.drawable.refresh_head_pull_light_0))
            }
        })
        txt_head_title.text = getString(R.string.bookshelf)

        img_head_personal.setOnClickListener {
            bookShelfInterface?.changeDrawerLayoutState()
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
            if (OneClickUtil.isDoubleClick(System.currentTimeMillis())) {
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

        val isImportPromptGone = SPUtils.getDefaultSharedBoolean(SPKey.BOOKSHELF_IMPORT_PROMPT)
                ?: false
        val isSharePromptGone = !Constants.SHARE_SWITCH_ENABLE || SPUtils.getDefaultSharedBoolean(SPKey.BOOKSHELF_SHARE_PROMPT)
                ?: false
        if (isImportPromptGone && isSharePromptGone) {
            view_head_menu.visibility = View.GONE
        } else {
            view_head_menu.visibility = View.VISIBLE
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
        bookShelfPresenter.iBookList.clear()
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
        refreshHeader.img_head.visibility = View.VISIBLE
        refreshHeader.img_head.setImageResource(R.drawable.refresh_head_pull_light_0)
        refreshHeader.img_anim.visibility = View.GONE
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


    }

    /**
     * 下拉时检查更新
     */
    private fun checkBookUpdate() {
        if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
            srl_refresh.isRefreshing = false

            if (isAdded) {
                ToastUtil.showToastMessage(R.string.bookshelf_network_error, 2000L)
            }
            return
        }

        val startPullTime = System.currentTimeMillis()
        val interval = Math.abs(startPullTime - latestLoadDataTime)

        // 刷新间隔小于30秒无效
        if (interval <= PULL_REFRESH_DELAY) {
            srl_refresh.onRefreshComplete()

            if (isAdded) {
                ToastUtil.showToastMessage(R.string.bookshelf_no_book_update, 2000L)
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
            updateUI()
        }
    }

    override fun onException(exception: Exception) {
        if (isAdded && !requireActivity().isFinishing) {
            latestLoadDataTime = System.currentTimeMillis()
            if (isAdded) {
                ToastUtil.showToastMessage(R.string.bookshelf_network_error, 2000L)
            }
            if (srl_refresh != null) {
                srl_refresh.onRefreshComplete()
            }
        }
    }

    override fun doUpdateBook(updateService: CheckNovelUpdateService) {
        if (!requireActivity().isFinishing) {
            updateService.setBookUpdateListener(requireActivity() as CheckNovelUpdateService.OnBookUpdateListener)
            bookShelfPresenter.addUpdateTask(this)
        }
    }

    override fun onBookListQuery(books: List<Book>?) {
        if (isAdded && !requireActivity().isFinishing) {
            if (books != null && books.isNotEmpty()) {
                srl_refresh?.setPullToRefreshEnabled(true)
                ll_empty?.visibility = View.GONE
            } else {
                srl_refresh?.setPullToRefreshEnabled(false)
                ll_empty?.visibility = View.VISIBLE
            }
        }
    }

    override fun onBookDelete(onlyDeleteCache: Boolean) {
        if (isAdded && !requireActivity().isFinishing) {
            updateUI()
            bookShelfDeleteDialog.dismiss()
            dismissRemoveMenu()
            ToastUtil.showToastMessage(R.string.bookshelf_delete_success)
        }
    }

    override fun onSuccessUpdateHandle(updateCount: Int, firstBook: BookUpdate?) {
        if (requireActivity().isFinishing) {
            return
        }
        if (updateCount == 0) {
            if (isAdded) {
                ToastUtil.showToastMessage(R.string.bookshelf_no_book_update, 2000L)
            }
        } else {
            val bookName = firstBook?.book_name
            val bookLastChapterName = firstBook?.last_chapter_name
            if (bookName?.isNotEmpty() == true && bookLastChapterName?.isNotEmpty() == true && !requireActivity().isFinishing) {
                if (updateCount == 1) {
                    if (isAdded) {
                        ToastUtil.showToastMessage(
                                "《$bookName${requireActivity()?.getString(R.string.bookshelf_book_update_chapter)}" + "$bookLastChapterName",
                                2000L)
                    }
                } else {
                    if(isAdded){
                        ToastUtil.showToastMessage(
                                "《$bookName${requireActivity()?.getString(R.string.bookshelf_books_update_more)}"
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