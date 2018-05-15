package com.dingyue.bookshelf


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dingyue.bookshelf.*
import com.dingyue.contract.CommonContract
import com.intelligent.reader.view.BookDeleteDialog
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.txtqbmfxs.frag_bookshelf.*
import kotlinx.android.synthetic.txtqbmfxs.layout_head.view.*
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

    private val bookSensitiveWords: ArrayList<String> = ArrayList()
    private val noBookSensitive = false
    private var bookRackUpdateTime: Long = 0
    private var latestLoadDataTime: Long = 0
    private val presenter: BookShelfPresenter by lazy { BookShelfPresenter(this) }
    private var bookShelfInterface: BookShelfInterface? = null


    val bookShelfAdapter: BookShelfAdapter by lazy {
        BookShelfAdapter(activity, object : BookShelfAdapter.BookShelfItemListener {
            override fun clickedBookShelfItem(book: Book?, position: Int) {
                if (position < 0 || position >= presenter.iBookList.size) return
                if (isRemoveMenuShow()) {
                    bookShelfAdapter.insertSelectedPosition(position)
                    var isSelectAll: Boolean = presenter.iBookList.size == bookShelfAdapter.selectedBooks.size
                    removeMenuPopup.setSelectedNum(bookShelfAdapter.selectedBooks.size, isSelectAll)
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


    private val headerView: View by lazy {
        LayoutInflater.from(ssfl_refresh_view.context)
                .inflate(R.layout.layout_head, null)
    }

    private lateinit var sharedPreferences: SharedPreferences

    private val bookDeleteDialog: BookDeleteDialog by lazy {
        val dialog = BookDeleteDialog(activity)
        dialog.onConfirmListener = { books, isDeleteCacheOnly ->
            if (books != null && books.isNotEmpty()) {
                if (!bookClearCacheDialog.isShow()) bookClearCacheDialog.show()
                deleteBooks(books, isDeleteCacheOnly)
            }
        }
        dialog.onCancelListener = {
            BookShelfLogger.uploadBookShelfEditDelete(0, null, false)
        }
        dialog
    }


    private val bookClearCacheDialog: BookClearCacheDialog by lazy {
        val dialog = BookClearCacheDialog(activity)

        dialog
    }


    private val removeMenuPopup: BookShelfRemoveMenuPopup by lazy {
        val popup = BookShelfRemoveMenuPopup(activity)
        popup.onDeleteClickListener = {
            bookDeleteDialog.show(bookShelfAdapter.selectedBooks)
        }
        popup.onSelectClickListener = { isSelectAll ->

            selectAll(isSelectAll)
        }
        popup
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.frag_bookshelf, container, false)
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)

        try {
            bookShelfInterface = activity as BookShelfInterface
        } catch (classCastException: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement BookShelfInterface")
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        bookRackUpdateTime = AppUtils.getLongPreferences(activity, "book_rack_update_time", System.currentTimeMillis())
        initRecyclerView()
        ssfl_refresh_view.setOnPullRefreshListener(object : SuperSwipeRefreshLayout.OnPullRefreshListener {
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
        img_download.setOnClickListener { RouterUtil.navigation(activity, RouterConfig.DOWNLOAD_MANAGER_ACTIVITY) }
        img_empty_btn.setOnClickListener {
            bookShelfInterface?.changeHomePagerIndex(1)
            BookShelfLogger.uploadBookShelfToBookCity()
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)

        initUpdateService()
        //根据书架数量确定是否刷新
        if (presenter.iBookList.size > 0) {
            ssfl_refresh_view.isRefreshing = true
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
//        presenter?.getFloatAd(activity)
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
        if (activity != null && !activity.isFinishing) {
            val isShowAd = !bookShelfAdapter.isRemove && isResumed && !Constants.isHideAD
            doAsync {
                presenter.queryBookListAndAd(activity, isShowAd)
                uiThread {
                    bookShelfAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onBookListQuery(bookList: ArrayList<Book>) {
        if (activity != null && !activity.isFinishing) {
            if (bookList.isEmpty()) {
                if (ssfl_refresh_view != null) {
                    ssfl_refresh_view.setPullToRefreshEnabled(false)
                }
                if (ll_empty != null) {
                    ll_empty.visibility = View.VISIBLE
                }

            } else {
                if (ssfl_refresh_view != null) {
                    ssfl_refresh_view.setPullToRefreshEnabled(true)
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
            if (ssfl_refresh_view != null) {
                ssfl_refresh_view!!.onRefreshComplete()
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
            if (ssfl_refresh_view != null) {
                ssfl_refresh_view.onRefreshComplete()
            }
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
        presenter.addUpdateTask(this)
    }

    override fun doUpdateBook(updateService: CheckNovelUpdateService) {
        if (activity != null) {
            updateService.setBookUpdateListener(activity as CheckNovelUpdateService.OnBookUpdateListener)
            presenter.addUpdateTask(this)
        }
    }

    override fun onBookDelete() {
        if (activity != null && !activity.isFinishing) {

            updateUI()
            bookDeleteDialog.dismiss()
            if (bookClearCacheDialog.isShow()) {
                bookClearCacheDialog.dimiss()
            }
            dismissRemoveMenu()
        }
    }

    override fun onAdRefresh() {
        if (activity != null && !activity.isFinishing) {
            bookShelfAdapter.notifyDataSetChanged()
        }
    }

    private fun initUpdateService() {
        if (presenter.updateService != null) return
        val intent = Intent()
        val context = activity.applicationContext
        intent.setClass(context, CheckNovelUpdateService::class.java)
        context.startService(intent)
        context.bindService(intent, presenter.updateConnection, Context.BIND_AUTO_CREATE)
    }

    private fun initRecyclerView() {
        ssfl_refresh_view.setHeaderViewBackgroundColor(0x00000000)
        ssfl_refresh_view.setHeaderView(createHeaderView())
        ssfl_refresh_view.isTargetScrollWithLayout = true
        recl_content.recycledViewPool.setMaxRecycledViews(0, 12)
        val layoutManager = ShelfGridLayoutManager(activity, 1)
        recl_content.layoutManager = layoutManager
        //        recyclerView.getItemAnimator().setSupportsChangeAnimations(false);
        recl_content.itemAnimator.addDuration = 0
        recl_content.itemAnimator.changeDuration = 0
        recl_content.itemAnimator.moveDuration = 0
        recl_content.itemAnimator.removeDuration = 0
        (recl_content.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        recl_content.adapter = bookShelfAdapter
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
            ssfl_refresh_view.isRefreshing = false
            showToastDelay(R.string.bookshelf_refresh_network_problem)
            return
        }

        val startPullTime = System.currentTimeMillis()
        val interval = Math.abs(startPullTime - latestLoadDataTime)

        // 刷新间隔小于30秒无效
        if (interval <= PULL_REFRESH_DELAY) {
            ssfl_refresh_view.onRefreshComplete()
            AppLog.d(TAG, "刷新间隔小于30秒不请求数据")
            showToastDelay(R.string.main_update_no_new)
        } else {
            // 刷新间隔大于30秒直接请求更新，
            presenter.addUpdateTask(this)
            AppLog.d(TAG, "刷新间隔大于30秒请求数据")
        }

    }

    /**
     * 处理被点击或更新通知的book
     */
    private fun handleBook(book: Book?) {
        if (book != null) {
            if (Constants.isShielding && !noBookSensitive && bookSensitiveWords.contains(book.book_id.toString())) {
                ToastUtils.showToastNoRepeat("抱歉，该小说已下架！")
            } else {
                BookRouter.navigateCoverOrRead(activity, book, 0)
            }
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

    companion object {

        private val PULL_REFRESH_DELAY = 30 * 1000
        private val TAG = BookShelfFragment::class.java.simpleName
    }


    override fun showRemoveMenu() {
        ssfl_refresh_view.setPullToRefreshEnabled(false)
        presenter.removeAd()
        bookShelfAdapter.insertRemoveState(true)
        bookShelfInterface?.changeHomeNavigationState(true)
        removeMenuPopup.show(rl_main)
        img_download.visibility = View.GONE

    }

    override fun dismissRemoveMenu() {
        ssfl_refresh_view.setPullToRefreshEnabled(true)
        bookShelfAdapter.insertRemoveState(false)
        removeMenuPopup.dismiss()
        bookShelfInterface?.changeHomeNavigationState(false)
        BookShelfLogger.uploadBookShelfEditCancel()
        img_download.visibility = View.VISIBLE
    }

    override fun isRemoveMenuShow(): Boolean = bookShelfAdapter.isRemove

    override fun selectAll(isAll: Boolean) {
        bookShelfAdapter.insertSelectAllState(isAll)
        var isSelectAll: Boolean = presenter.iBookList.size == bookShelfAdapter.selectedBooks.size
        removeMenuPopup.setSelectedNum(bookShelfAdapter.selectedBooks.size, isSelectAll)
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

}
