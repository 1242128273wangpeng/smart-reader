package com.dingyue.bookshelf

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dingyue.bookshelf.contract.BookShelfADContract
import com.dingyue.bookshelf.view.RemoveMenuPopup
import com.dingyue.contract.CommonContract
import com.dingyue.bookshelf.view.BookShelfDeleteDialog
import com.dingyue.contract.util.showToastMessage
import de.greenrobot.event.EventBus
import kotlinx.android.synthetic.zsmfqbxs.frag_bookshelf.*
import kotlinx.android.synthetic.zsmfqbxs.bookshelf_refresh_header.view.*
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

/**
 * Desc
 * Author zhenxiang
 * 2018\5\15 0015
 */
class BookShelfFragment : Fragment(), UpdateCallBack, BookShelfView, MenuManager {

    private val popupHeight by lazy {
        resources.getDimensionPixelSize(R.dimen.bookshelf_popup_height)
    }

    private val dividerHeight by lazy {
        resources.getDimensionPixelSize(R.dimen.bookshelf_divider_height)
    }

    private val bookShelfPresenter: BookShelfPresenter by lazy { BookShelfPresenter(this) }

    private var latestLoadDataTime: Long = 0

    private var bookShelfInterface: BookShelfInterface? = null

    private val refreshHeader: View by lazy {
        LayoutInflater.from(srl_refresh.context).inflate(R.layout.bookshelf_refresh_header, null)
    }

    private val removeMenuPopup: RemoveMenuPopup by lazy {
        val popup = RemoveMenuPopup(activity)
        popup.onDeleteClickListener = {
            if(!bookShelfDeleteDialog.isShow()){
                bookShelfDeleteDialog.show(bookShelfAdapter.selectedBooks)
            }
        }
        popup.onSelectClickListener = { isSelectAll ->

            selectAll(isSelectAll)
        }
        popup
    }


    val bookShelfAdapter: BookShelfAdapter by lazy {
        BookShelfAdapter(activity, object : BookShelfAdapter.BookShelfItemListener {
            override fun clickedBookShelfItem(book: Book?, position: Int) {
                if (position < 0 || position >= bookShelfPresenter.iBookList.size) return
                if (isRemoveMenuShow()) {
                    bookShelfAdapter.insertSelectedPosition(position)
                    removeMenuPopup.setSelectedNum(bookShelfAdapter.selectedBooks.size, bookShelfAdapter.isSelectedAll())
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
            if (books.isNotEmpty()) {
                deleteBooks(books, isDeleteCacheOnly)
            }
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

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.frag_bookshelf, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {

        BookShelfADContract.insertBookShelfType(true)

        initRecyclerView()
        srl_refresh.setOnPullRefreshListener(object : SuperSwipeRefreshLayout.OnPullRefreshListener {
            override fun onRefresh() {
                refreshHeader.txt_refresh_prompt.text = getText(R.string.refresh_running)
                refreshHeader.img_refresh_arrow.visibility = View.GONE
                refreshHeader.pgbar_refresh_loading.visibility = View.VISIBLE
                checkBookUpdate()
            }

            override fun onPullDistance(distance: Int) {}

            override fun onPullEnable(enable: Boolean) {
                refreshHeader.pgbar_refresh_loading.visibility = View.GONE
                refreshHeader.txt_refresh_prompt.text = if (enable) getText(R.string.refresh_release) else getText(R.string.refresh_start)
                refreshHeader.img_refresh_arrow.visibility = View.VISIBLE
                refreshHeader.img_refresh_arrow.rotation = (if (enable) 180 else 0).toFloat()
            }
        })

        txt_empty_add_book.setOnClickListener {
            BookShelfLogger.uploadBookShelfToBookCity()
        }
        img_head_setting!!.setOnClickListener(View.OnClickListener {
            if (CommonContract.isDoubleClick(System.currentTimeMillis())) {
                return@OnClickListener
            }
            BookShelfLogger.uploadBookShelfPersonal()
            RouterUtil.navigation(activity, RouterConfig.SETTING_ACTIVITY)
            EventBus.getDefault().post(ConsumeEvent(R.id.fup_head_setting))
        })
        img_head_search!!.setOnClickListener {
            RouterUtil.navigation(activity, RouterConfig.SEARCH_BOOK_ACTIVITY)
            BookShelfLogger.uploadBookShelfSearch()
        }
        img_head_download!!.setOnClickListener {
            RouterUtil.navigation(activity, RouterConfig.DOWNLOAD_MANAGER_ACTIVITY)
            BookShelfLogger.uploadBookShelfCacheManager()
        }
        img_editor_back!!.setOnClickListener {
            BookShelfLogger.uploadBookShelfEditBack()
            dismissRemoveMenu()
        }
        home_edit_cancel!!.setOnClickListener {
            BookShelfLogger.uploadBookShelfEditCancel()
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

//        if (!Constants.isHideAD && Constants.dy_shelf_boundary_switch && bookShelfPresenter.iBookList.isNotEmpty()) {
            bookShelfPresenter.requestFloatAD(activity, fl_ad_float)
//        }
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
        if (bookShelfPresenter.updateService != null) return
        val intent = Intent()
        val context = activity.applicationContext
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

        val bookShelfItemDecoration = BookShelfItemDecoration(dividerHeight, Color.parseColor("#FFE8E8E8"), BookShelfItemDecoration.CROSS_DIVIDER)
        recl_content.addItemDecoration(bookShelfItemDecoration)
    }

    private fun createHeaderView(): View {
        refreshHeader.txt_refresh_prompt.text = getText(R.string.refresh_start)
        refreshHeader.img_refresh_arrow.visibility = View.VISIBLE
        refreshHeader.img_refresh_arrow.setImageResource(R.drawable.pulltorefresh_down_arrow)
        refreshHeader.pgbar_refresh_loading.visibility = View.GONE
        return refreshHeader
    }

    /**
     * 查Book数据库更新界面
     */
    fun updateUI() {
        if (activity != null && !activity.isFinishing) {
            val isShowAD = !bookShelfAdapter.isRemove && isResumed && !Constants.isHideAD
            doAsync {
                bookShelfPresenter.queryBookListAndAd(activity, isShowAD, false)
                uiThread {
                    bookShelfAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    /**
     * 下拉时检查更新
     */
    private fun checkBookUpdate() {
        if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
            srl_refresh.isRefreshing = false
            if (isAdded) {
                activity.applicationContext.showToastMessage(R.string.bookshelf_network_error, 2000L)
            }
            return
        }

        val startPullTime = System.currentTimeMillis()
        val interval = Math.abs(startPullTime - latestLoadDataTime)

        // 刷新间隔小于30秒无效
        if (interval <= PULL_REFRESH_DELAY) {
            srl_refresh.onRefreshComplete()
            if (isAdded) {
                activity.applicationContext.showToastMessage(R.string.bookshelf_no_book_update, 2000L)
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
        if (book != null && activity != null && !activity.isFinishing) {
            BookRouter.navigateCoverOrRead(activity, book, 0)

        }
    }

    private fun changeHeaderState(state: Boolean) {
        if (state) {
            if (!rl_head_remove.isShown) {
                rl_head_remove.visibility = View.VISIBLE
                fl_ad_float.visibility = View.GONE

            }
        } else {
            if (rl_head_remove.isShown) {
                rl_head_remove.visibility = View.GONE
                fl_ad_float.visibility = View.VISIBLE
            }
        }
    }

    override fun onSuccess(result: BookUpdateResult) {
        if (activity != null && !activity.isFinishing) {
            latestLoadDataTime = System.currentTimeMillis()
            if (srl_refresh != null) {
                srl_refresh!!.onRefreshComplete()
            }
            bookShelfPresenter.handleSuccessUpdate(result)
            updateUI()
        }
    }

    override fun onException(exception: Exception) {
        if (activity != null && !activity.isFinishing) {
            latestLoadDataTime = System.currentTimeMillis()
            if (isAdded) {
                activity.applicationContext.showToastMessage(R.string.bookshelf_network_error, 2000L)
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

    override fun onBookListQuery(bookList: ArrayList<Book>) {
        if (activity != null && !activity.isFinishing) {
            if (bookList.isEmpty()) {
                if (srl_refresh != null) {
                    srl_refresh.setPullToRefreshEnabled(false)
                }
                if (ll_empty != null) {
                    ll_empty.visibility = View.VISIBLE
                }

            } else {
                if (srl_refresh != null) {
                    srl_refresh.setPullToRefreshEnabled(true)
                }
                if (ll_empty != null) {
                    ll_empty.visibility = View.GONE
                }
            }
        }
    }

    override fun onBookDelete() {
        if (activity != null && !activity.isFinishing) {
            updateUI()
            bookShelfDeleteDialog.dismiss()
            dismissRemoveMenu()
            activity.applicationContext.showToastMessage(R.string.bookshelf_delete_success)
        }
    }

    override fun onSuccessUpdateHandle(updateCount: Int, firstBook: BookUpdate?) {
        if (activity == null || activity.isFinishing) {
            return
        }
        if (updateCount == 0) {
            if (isAdded) {
                activity.applicationContext.showToastMessage(R.string.bookshelf_no_book_update, 2000L)
            }
        } else {
            val bookName = firstBook?.book_name
            val bookLastChapterName = firstBook?.last_chapter_name
            if (bookName?.isNotEmpty() == true && bookLastChapterName?.isNotEmpty() == true) {
                if (updateCount == 1 && activity != null) {
                    if (isAdded) {
                        activity.applicationContext.showToastMessage(
                                "《$bookName${activity.getString(R.string.bookshelf_book_update_chapter)}" + "$bookLastChapterName",
                                2000L)
                    }
                } else if (activity != null) {
                    if (isAdded) {
                        activity.applicationContext.showToastMessage(
                                "《$bookName${activity.getString(R.string.bookshelf_books_update_more)}" + "$updateCount${activity.getString(R.string.bookshelf_books_update_chapters)}",
                                2000L)
                    }
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
        fl_ad_float.visibility = View.GONE

        srl_refresh.setPadding(0, srl_refresh.paddingTop, 0, popupHeight)
        
        changeHeaderState(true)
    }

    override fun dismissRemoveMenu() {
        srl_refresh.setPullToRefreshEnabled(true)
        bookShelfAdapter.insertRemoveState(false)
        removeMenuPopup.dismiss()
        srl_refresh.setPadding(0, srl_refresh.paddingTop, 0, 0)
        bookShelfInterface?.changeHomeNavigationState(false)
        
        changeHeaderState(false)

        BookShelfLogger.uploadBookShelfEditCancel()

        updateUI()

        if (!Constants.isHideAD && Constants.dy_shelf_boundary_switch && bookShelfPresenter.iBookList.isNotEmpty()) {
            bookShelfPresenter.requestFloatAD(activity, fl_ad_float)
        }
    }

    override fun isRemoveMenuShow(): Boolean = bookShelfAdapter.isRemove

    override fun selectAll(isAll: Boolean) {
        bookShelfAdapter.insertSelectAllState(isAll)
        removeMenuPopup.setSelectedNum(bookShelfAdapter.selectedBooks.size, bookShelfAdapter.isSelectedAll())
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
