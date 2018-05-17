package com.dingyue.bookshelf

/**
 * Desc
 * Author zhenxiang
 * 2018\5\15 0015
 */


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
import android.view.animation.AlphaAnimation
import com.dingyue.bookshelf.*
import com.dingyue.contract.CommonContract
import com.intelligent.reader.view.BookShelfDeleteDialog
import de.greenrobot.event.EventBus
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.zsmfqbxs.frag_bookshelf.*
import kotlinx.android.synthetic.zsmfqbxs.bookshelf_refresh_header.view.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
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
import net.lzbook.kit.utils.pulllist.DividerItemDecoration
import java.util.concurrent.TimeUnit

/**
 * 书架页Fragment
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

        }, presenter.iBookList, presenter.aDViews)
    }


    fun showEditor(state: Boolean) {
        if (state) {
            if (!content_head_editor.isShown) {
                val showAnimation = AlphaAnimation(0.0f, 1.0f)
                showAnimation.duration = 200
                content_head_editor.startAnimation(showAnimation)
                content_head_editor.visibility = View.VISIBLE
                bookshelf_float_ad.visibility = View.GONE

            }
        } else {
            if (content_head_editor.isShown) {
                content_head_editor.visibility = View.GONE
                bookshelf_float_ad.visibility = View.VISIBLE
            }
        }
    }


    private val headerView: View by lazy {
        LayoutInflater.from(ssfl_refresh_view.context)
                .inflate(R.layout.bookshelf_refresh_header, null)
    }

    private lateinit var sharedPreferences: SharedPreferences


    private val bookDeleteDialog: BookShelfDeleteDialog by lazy {
        val dialog = BookShelfDeleteDialog(activity)
        dialog.onConfirmListener = { books, isDeleteCacheOnly ->
            if (books.isNotEmpty()) {
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
            if(!bookDeleteDialog.isShow()){
                bookDeleteDialog.show(bookShelfAdapter.selectedBooks)
            }
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
//        if(PlatformSDK.config() != null){
//            PlatformSDK.config().setBookShelfGrid(true)
//        }

        bookRackUpdateTime = AppUtils.getLongPreferences(activity, "book_rack_update_time", System.currentTimeMillis())
        initRecyclerView()
        ssfl_refresh_view.setOnPullRefreshListener(object : SuperSwipeRefreshLayout.OnPullRefreshListener {
            override fun onRefresh() {
                headerView.txt_refresh_prompt.text = "正在刷新"
                headerView.img_refresh_arrow.visibility = View.GONE
                headerView.pgbar_refresh_loading.visibility = View.VISIBLE
                checkBookUpdate()
            }

            override fun onPullDistance(distance: Int) {}

            override fun onPullEnable(enable: Boolean) {
                headerView.pgbar_refresh_loading.visibility = View.GONE
                headerView.txt_refresh_prompt.text = if (enable) "松开刷新" else "下拉刷新"
                headerView.img_refresh_arrow.visibility = View.VISIBLE
                headerView.img_refresh_arrow.rotation = (if (enable) 180 else 0).toFloat()
            }
        })
        initClick()

    }

    fun initClick() {
        img_empty_btn.setOnClickListener {
            //            fragmentCallback.setSelectTab(1)
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SHELF_PAGE, StartLogClickUtil.TOBOOKCITY)
        }
        img_head_setting!!.setOnClickListener(View.OnClickListener {
            if (CommonContract.isDoubleClick(System.currentTimeMillis())) {
                return@OnClickListener
            }
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.MAIN_PAGE,
                    StartLogClickUtil.PERSONAL)
            RouterUtil.navigation(activity, RouterConfig.SETTING_ACTIVITY)
            EventBus.getDefault().post(ConsumeEvent(R.id.redpoint_home_setting))
            //                startActivity(new Intent(context, SettingActivity.class));
            StatServiceUtils.statAppBtnClick(activity,
                    StatServiceUtils.bs_click_mine_menu)
        })
        img_head_search!!.setOnClickListener {
            RouterUtil.navigation(activity, RouterConfig.SEARCH_BOOK_ACTIVITY)

            StatServiceUtils.statAppBtnClick(activity,
                    StatServiceUtils.bs_click_search_btn)
        }
        img_download_manage!!.setOnClickListener {
            RouterUtil.navigation(activity, RouterConfig.DOWNLOAD_MANAGER_ACTIVITY)
            StatServiceUtils.statAppBtnClick(activity,
                    StatServiceUtils.bs_click_download_btn)
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.MAIN_PAGE,
                    StartLogClickUtil.CACHEMANAGE)
        }
        home_edit_back!!.setOnClickListener {
            dismissRemoveMenu()
        }
        home_edit_cancel!!.setOnClickListener {
            dismissRemoveMenu()
        }
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

        val typeColor = R.color.color_gray_e8e8e8
        val layoutManager = ShelfGridLayoutManager(activity, 3)
        recl_content.addItemDecoration(
                DividerItemDecoration(activity, DividerItemDecoration.BOTH_SET, 2,
                        activity.getResources().getColor(typeColor)))

        recl_content.setLayoutManager(layoutManager)
        recl_content.setFocusable(false)//放弃焦点
//      recyclerView.getItemAnimator().setSupportsChangeAnimations(false);
        recl_content.getItemAnimator().setAddDuration(0)
        recl_content.getItemAnimator().setChangeDuration(0)
        recl_content.getItemAnimator().setMoveDuration(0)
        recl_content.getItemAnimator().setRemoveDuration(0)
        (recl_content.getItemAnimator() as SimpleItemAnimator).supportsChangeAnimations = false
        recl_content.adapter = bookShelfAdapter
    }

    private fun createHeaderView(): View {
        headerView.txt_refresh_prompt.text = "下拉刷新"
        headerView.img_refresh_arrow.visibility = View.VISIBLE
        headerView.img_refresh_arrow.setImageResource(R.drawable.pulltorefresh_down_arrow)
        headerView.pgbar_refresh_loading.visibility = View.GONE
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

        ll_books_content.setPadding(0, ll_books_content.paddingTop, 0, 140)
        showEditor(true)
    }

    override fun dismissRemoveMenu() {
        ssfl_refresh_view.setPullToRefreshEnabled(true)
        bookShelfAdapter.insertRemoveState(false)
        removeMenuPopup.dismiss()
        ll_books_content.setPadding(0, ll_books_content.paddingTop, 0, 0)
        bookShelfInterface?.changeHomeNavigationState(false)
        showEditor(false)
        BookShelfLogger.uploadBookShelfEditCancel()
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
        presenter.deleteBooks(books, isDeleteCacheOnly)
    }

}
