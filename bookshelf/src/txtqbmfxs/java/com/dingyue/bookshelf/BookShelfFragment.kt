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
import com.dingyue.bookshelf.contract.BookShelfADContract
import com.dingyue.contract.CommonContract
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.txtqbmfxs.frag_bookshelf.*
import kotlinx.android.synthetic.txtqbmfxs.bookshelf_refresh_header.view.*
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

/**
 * Desc
 * Author zhenxiang
 * 2018\5\15 0015
 */
class BookShelfFragment : Fragment(), UpdateCallBack, BookShelfView, MenuManager {

    private val popupHeight by lazy {
        resources.getDimensionPixelSize(R.dimen.bookshelf_popup_height)
    }


    private val bookshelfPresenter: BookShelfPresenter by lazy { BookShelfPresenter(this) }

    private var bookRackUpdateTime: Long = 0
    private var latestLoadDataTime: Long = 0

    private lateinit var sharedPreferences: SharedPreferences

    private var bookShelfInterface: BookShelfInterface? = null

    private val headerView: View by lazy {
        LayoutInflater.from(srl_refresh.context)
                .inflate(R.layout.bookshelf_refresh_header, null)
    }

    private val removeMenuPopup: RemoveMenuPopup by lazy {
        val popup = RemoveMenuPopup(activity)
        popup.onDeleteClickListener = {
            bookShelfDeleteDialog.show(bookShelfAdapter.selectedBooks)
        }
        popup.onSelectClickListener = { isSelectAll ->
            selectAll(isSelectAll)
        }
        popup
    }

    val bookShelfAdapter: BookShelfAdapter by lazy {
        BookShelfAdapter(activity, object : BookShelfAdapter.BookShelfItemListener {
            override fun clickedBookShelfItem(book: Book?, position: Int) {
                if (position < 0 || position >= bookshelfPresenter.iBookList.size) return

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

        }, bookshelfPresenter.iBookList, bookshelfPresenter.aDViews)
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

        if (bookshelfPresenter.iBookList.size > 0) {
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
                headerView.txt_refresh_prompt.text = getString(R.string.refresh_running)
                headerView.img_refresh_arrow.visibility = View.GONE
                headerView.pgbar_refresh_loading.visibility = View.VISIBLE
                checkBookUpdate()
            }

            override fun onPullDistance(distance: Int) {}

            override fun onPullEnable(enable: Boolean) {
                headerView.pgbar_refresh_loading.visibility = View.GONE
                headerView.txt_refresh_prompt.text = if (enable) getString(R.string.refresh_release) else getString(R.string.refresh_start)
                headerView.img_refresh_arrow.visibility = View.VISIBLE
                headerView.img_refresh_arrow.rotation = (if (enable) 180 else 0).toFloat()
            }
        })
        img_download_float.setOnClickListener { RouterUtil.navigation(activity, RouterConfig.DOWNLOAD_MANAGER_ACTIVITY) }
        img_empty_add_book.setOnClickListener {
            bookShelfInterface?.changeHomePagerIndex(1)
            BookShelfLogger.uploadBookShelfToBookCity()
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
        bookshelfPresenter.clear()
    }

    private fun initRecyclerView() {
        srl_refresh.setHeaderViewBackgroundColor(0x00000000)
        srl_refresh.setHeaderView(createHeaderView())
        srl_refresh.isTargetScrollWithLayout = true
        recl_content.recycledViewPool.setMaxRecycledViews(0, 12)
        val layoutManager = ShelfGridLayoutManager(activity, 1)
        recl_content.layoutManager = layoutManager
        recl_content.itemAnimator.addDuration = 0
        recl_content.itemAnimator.changeDuration = 0
        recl_content.itemAnimator.moveDuration = 0
        recl_content.itemAnimator.removeDuration = 0
        (recl_content.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        recl_content.adapter = bookShelfAdapter
    }

    private fun createHeaderView(): View {
        headerView.txt_refresh_prompt.text = getString(R.string.refresh_start)
        headerView.img_refresh_arrow.visibility = View.VISIBLE
        headerView.img_refresh_arrow.setImageResource(R.drawable.pulltorefresh_down_arrow)
        headerView.pgbar_refresh_loading.visibility = View.GONE
        return headerView
    }

    /**
     * 查Book数据库更新界面
     */
    fun updateUI() {
        if (activity != null && !activity.isFinishing) {
            val isShowAd = !bookShelfAdapter.isRemove && isResumed && !Constants.isHideAD
            doAsync {
                bookshelfPresenter.queryBookListAndAd(activity, isShowAd)
                uiThread {
                    bookShelfAdapter.notifyDataSetChanged()
                }
            }
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

    override fun onSuccess(result: BookUpdateResult) {
        if (activity != null && !activity.isFinishing) {
            latestLoadDataTime = System.currentTimeMillis()
            bookRackUpdateTime = System.currentTimeMillis()
            if (srl_refresh != null) {
                srl_refresh!!.onRefreshComplete()
            }
            bookshelfPresenter.handleSuccessUpdate(result)
            AppUtils.setLongPreferences(activity, "book_rack_update_time", bookRackUpdateTime)
            updateUI()
        }
    }

    override fun onException(e: Exception) {
        if (activity != null && !activity.isFinishing) {
            latestLoadDataTime = System.currentTimeMillis()
            showToastDelay(R.string.bookshelf_network_error)
            if (srl_refresh != null) {
                srl_refresh.onRefreshComplete()
            }
        }
    }

    override fun onSuccessUpdateHandle(updateCount: Int, firstBook: BookUpdate?) {
        if (activity == null || activity.isFinishing) {
            return
        }
        if (updateCount == 0) {
            showToastDelay(R.string.bookshelf_no_book_update)
        } else {
            val bookName = firstBook?.book_name
            val bookLastChapterName = firstBook?.last_chapter_name
            if (bookName?.isNotEmpty() == true && bookLastChapterName?.isNotEmpty() == true) {
                if (updateCount == 1 && activity != null) {
                    showToastDelay("《$bookName${activity.getString(R.string.bookshelf_book_update_chapter)}" +
                            "$bookLastChapterName")
                } else if (activity != null) {
                    showToastDelay("《$bookName${activity.getString(R.string.bookshelf_books_update_more)}" +
                            "$updateCount${activity.getString(R.string.bookshelf_books_update_chapters)}")
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
        if (activity != null && !activity.isFinishing) {
            updateUI()
            bookShelfDeleteDialog.dismiss()
            dismissRemoveMenu()
        }
    }

    override fun onAdRefresh() {
        if (activity != null && !activity.isFinishing) {
            bookShelfAdapter.notifyDataSetChanged()
        }
    }

    private fun initUpdateService() {
        if (bookshelfPresenter.updateService != null) return
        val intent = Intent()
        val context = activity.applicationContext
        intent.setClass(context, CheckNovelUpdateService::class.java)
        context.startService(intent)
        context.bindService(intent, bookshelfPresenter.updateConnection, Context.BIND_AUTO_CREATE)
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
            bookshelfPresenter.addUpdateTask(this)
        }
    }

    /**
     * 处理被点击或更新通知的book
     */
    private fun handleBook(book: Book?) {
        if (book != null && activity != null && !activity.isFinishing) {
            if (!TextUtils.isEmpty(book.book_id) && book.book_type == 0) {
                bookshelfPresenter.resetUpdateStatus(book.book_id)
            }

            BookRouter.navigateCoverOrRead(activity, book, BookRouter.NAVIGATE_TYPE_BOOKSHELF)
        }
    }

    private fun showToastDelay(textId: Int) {
        if (!isAdded) return
        Observable.timer(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (activity != null && !activity.isFinishing) {
                        activity.applicationContext.toastShort(textId)
                    }
                }
    }

    private fun showToastDelay(text: String) {
        if (!isAdded) return
        Observable.timer(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (activity != null && !activity.isFinishing) {
                        activity.applicationContext.toastShortStr(text)
                    }
                }
    }


    override fun showRemoveMenu() {
        srl_refresh.setPullToRefreshEnabled(false)
        bookshelfPresenter.removeAd()
        bookShelfAdapter.insertRemoveState(true)
        bookShelfInterface?.changeHomeNavigationState(true)
        srl_refresh.setPadding(0, srl_refresh.paddingTop, 0, popupHeight)
        removeMenuPopup.show(rl_content)
        img_download_float.visibility = View.GONE

    }

    override fun dismissRemoveMenu() {
        srl_refresh.setPullToRefreshEnabled(true)
        bookShelfAdapter.insertRemoveState(false)
        removeMenuPopup.dismiss()
        bookShelfInterface?.changeHomeNavigationState(false)
        srl_refresh.setPadding(0, srl_refresh.paddingTop, 0, 0)
        BookShelfLogger.uploadBookShelfEditCancel()
        img_download_float.visibility = View.VISIBLE
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
        bookshelfPresenter.deleteBooks(books, isDeleteCacheOnly)
    }

    companion object {
        private const val PULL_REFRESH_DELAY = 30 * 1000
    }
}
