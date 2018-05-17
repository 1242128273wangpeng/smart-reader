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
import com.dingyue.bookshelf.view.BookDeleteDialog
import com.dingyue.bookshelf.view.RemoveMenuPopup
import com.dingyue.contract.CommonContract
import de.greenrobot.event.EventBus
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.qbzsydq.bookshelf_refresh_header.view.*
import kotlinx.android.synthetic.qbzsydq.frag_bookshelf.*
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

    private var bookRackUpdateTime: Long = 0
    private var latestLoadDataTime: Long = 0
    private lateinit var sharedPreferences: SharedPreferences

    private var bookShelfInterface: BookShelfInterface? = null

    val presenter: BookShelfPresenter by lazy { BookShelfPresenter(this) }

    val bookShelfAdapter: BookShelfAdapter by lazy {
        BookShelfAdapter(activity, object : BookShelfAdapter.BookShelfItemListener {
            override fun clickedBookShelfItem(book: Book?, position: Int) {
                if (position < 0 || position >= presenter.iBookList.size) return
                if (isRemoveMenuShow()) {
                    bookShelfAdapter.insertSelectedPosition(position)
                    removeMenuPopup.setSelectedNum(bookShelfAdapter.selectedBooks.size)
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

        }, presenter.iBookList, presenter.aDViews)
    }

    private val removeMenuPopup: RemoveMenuPopup by lazy {
        val popup = RemoveMenuPopup(activity)
        popup.onSelectAllClickListener = { isSelectAll ->
            selectAll(isSelectAll)
        }
        popup.onDeleteClickListener = {
            bookDeleteDialog.show(bookShelfAdapter.selectedBooks)
        }
        popup
    }

    private val bookDeleteDialog: BookDeleteDialog by lazy {
        val dialog = BookDeleteDialog(activity)
        dialog.onConfirmListener = { books, isDeleteCacheOnly ->
            deleteBooks(books, isDeleteCacheOnly)
        }
        dialog.onCancelListener = {
            BookShelfLogger.uploadBookShelfEditDelete(0, null, false)
        }
        dialog
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.frag_bookshelf, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        bookRackUpdateTime = AppUtils.getLongPreferences(activity, "book_rack_update_time", System.currentTimeMillis())
        initRecyclerView()
        bookshelf_refresh_view.setOnPullRefreshListener(object : SuperSwipeRefreshLayout.OnPullRefreshListener {
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
        img_empty.setOnClickListener {
            bookShelfInterface?.changeHomePagerIndex(1)
            BookShelfLogger.uploadBookShelfToBookCity()
        }

        img_head_setting.setOnClickListener {
            BookShelfLogger.uploadBookShelfPersonal()
            EventBus.getDefault().post(ConsumeEvent(R.id.first_use_view))
            RouterUtil.navigation(activity, RouterConfig.SETTING_ACTIVITY)
        }

        img_head_search.setOnClickListener {
            val bundle = Bundle()
            bundle.putBoolean("isShowLastSearch", false)
            RouterUtil.navigation(activity, RouterConfig.SEARCH_BOOK_ACTIVITY, bundle)
            BookShelfLogger.uploadBookShelfSearch()
        }

        img_download_manager.setOnClickListener {
            RouterUtil.navigation(activity, RouterConfig.DOWNLOAD_MANAGER_ACTIVITY)
            BookShelfLogger.uploadBookShelfCacheManager()
        }

        img_remove_back.setOnClickListener {
            dismissRemoveMenu()
        }

        txt_remove_cancel.setOnClickListener {
            dismissRemoveMenu()
        }

    }

    private val refreshHeader by lazy {
        LayoutInflater.from(activity).inflate(R.layout.bookshelf_refresh_header, null)
    }

    private fun initRecyclerView() {
        bookshelf_refresh_view.setHeaderViewBackgroundColor(0x00000000)
        bookshelf_refresh_view.setHeaderView(refreshHeader)
        bookshelf_refresh_view.isTargetScrollWithLayout = true
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

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)

        initUpdateService()

        if (presenter.iBookList.size > 0) {
            bookshelf_refresh_view.isRefreshing = true
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun initUpdateService() {
        if (presenter.updateService != null) return
        val intent = Intent()
        val context = activity.applicationContext
        intent.setClass(context, CheckNovelUpdateService::class.java)
        context.startService(intent)
        context.bindService(intent, presenter.updateConnection, Context.BIND_AUTO_CREATE)
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
        presenter.iBookList.clear()
        presenter.clear()
    }

    /**
     * 查Book数据库更新界面
     */
    fun updateUI() {
        val isShowAd = !bookShelfAdapter.isRemove && isResumed && !Constants.isHideAD
        doAsync {
            presenter.queryBookListAndAd(activity, isShowAd)
            uiThread {
                bookShelfAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onBookListQuery(bookList: ArrayList<Book>) {
        if (activity != null && !activity.isFinishing) {
            if (bookList.isEmpty()) {
                bookshelf_refresh_view.setPullToRefreshEnabled(false)
                ll_empty.visibility = View.VISIBLE
            } else {
                bookshelf_refresh_view.setPullToRefreshEnabled(true)
                ll_empty.visibility = View.GONE
            }
        }

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
        AppLog.e(TAG, "handleBook")
        if (book != null && activity != null && !activity.isFinishing) {
            if (!TextUtils.isEmpty(book.book_id) && book.book_type == 0) {
                presenter.resetUpdateStatus(book.book_id)
            }
            BookRouter.navigateCoverOrRead(activity, book, BookRouter.NAVIGATE_TYPE_BOOKSHELF)
            AppLog.e(TAG, "goToCoverOrRead")
        }

    }

    override fun onSuccess(result: BookUpdateResult) {
        if (activity != null && !activity.isFinishing) {
            latestLoadDataTime = System.currentTimeMillis()
            bookRackUpdateTime = System.currentTimeMillis()
            if (bookshelf_refresh_view != null) {
                bookshelf_refresh_view.onRefreshComplete()
            }
            presenter.handleSuccessUpdate(result)
            AppUtils.setLongPreferences(activity, "book_rack_update_time", bookRackUpdateTime)
            AppLog.e(TAG, "onSuccess的刷新ui调用")
            updateUI()
        }

    }

    override fun onException(e: Exception) {
        if (activity != null && !activity.isFinishing) {
            latestLoadDataTime = System.currentTimeMillis()
            showToastDelay(R.string.bookshelf_refresh_network_problem)
            if (bookshelf_refresh_view != null) {
                bookshelf_refresh_view.onRefreshComplete()
            }
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
                            bookLastChapterName)
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

    override fun onBookDelete() {
        if (activity != null && !activity.isFinishing) {
            updateUI()
            bookDeleteDialog.dismiss()
            dismissRemoveMenu()
        }
    }

    override fun showRemoveMenu() {
        bookshelf_refresh_view.setPullToRefreshEnabled(false)
        presenter.removeAd()
        bookShelfAdapter.insertRemoveState(true)
        bookShelfInterface?.changeHomeNavigationState(true)
        removeMenuPopup.show(rl_content)

        bookshelf_refresh_view.setPadding(0, bookshelf_refresh_view.paddingTop, 0, 140)
//        txt_head_select_all.text = getString(R.string.select_all)
        rl_head_normal.visibility = View.GONE
        rl_head_remove.visibility = View.VISIBLE
//        bookshelf_float_ad.visibility = View.GONE
    }

    override fun dismissRemoveMenu() {
        bookShelfAdapter.insertRemoveState(false)
        removeMenuPopup.dismiss()
        bookshelf_refresh_view.setPadding(0, bookshelf_refresh_view.paddingTop, 0, 0)
        bookShelfInterface?.changeHomeNavigationState(false)

//        txt_head_select_all.text = getString(R.string.select_all_cancel)
        rl_head_normal.visibility = View.VISIBLE
        rl_head_remove.visibility = View.GONE
//        bookshelf_float_ad.visibility = View.VISIBLE

        BookShelfLogger.uploadBookShelfEditCancel()
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
        presenter.deleteBooks(books, isDeleteCacheOnly)
    }

    override fun onAdRefresh() {
        if (activity != null && !activity.isFinishing) {
            bookShelfAdapter.notifyDataSetChanged()
        }
    }

    companion object {
        private const val PULL_REFRESH_DELAY = 30 * 1000
        private val TAG = BookShelfFragment::class.java.simpleName
    }

}