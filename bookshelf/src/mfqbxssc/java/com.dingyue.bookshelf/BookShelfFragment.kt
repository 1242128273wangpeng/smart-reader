package com.dingyue.bookshelf

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.widget.SimpleItemAnimator
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import de.greenrobot.event.EventBus
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.mfqbxssc.fragment_bookshelf.*
import kotlinx.android.synthetic.mfqbxssc.layout_head.view.*
import kotlinx.android.synthetic.mfqbxssc.popwindow_paixu.view.*
import kotlinx.android.synthetic.mfqbxssc.popwindow_title_right.view.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.component.service.CheckNovelUpdateService
import net.lzbook.kit.book.view.ConsumeEvent
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.UpdateCallBack
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.BookUpdate
import net.lzbook.kit.data.bean.BookUpdateResult
import net.lzbook.kit.data.bean.SettingItems
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.pulllist.SuperSwipeRefreshLayout
import net.lzbook.kit.router.BookRouter
import net.lzbook.kit.router.RouterConfig
import net.lzbook.kit.router.RouterUtil
import net.lzbook.kit.utils.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * 书架页Fragment
 */
class BookShelfFragment : Fragment(), UpdateCallBack, BookShelfRemoveHelper.OnMenuDeleteClickListener,
        BookShelfRemoveHelper.OnMenuStateListener, BookShelfView {

    override fun onAdRefresh() {
        if (activity != null && !activity.isFinishing) {
            bookShelfReAdapter.notifyDataSetChanged()
        }
    }

    val presenter: BookShelfPresenter by lazy { BookShelfPresenter(this) }

    val bookShelfReAdapter: BookShelfReAdapter by lazy {
        BookShelfReAdapter(activity, presenter.iBookList, presenter.aDViews,
                object : BookShelfReAdapter.ShelfItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        AppLog.e(TAG, "BookShelfItemClick")
                        if (position < 0 || position > presenter.iBookList.size) {
                            return
                        }
                        if (bookShelfRemoveHelper.isRemoveMode) {
                            bookShelfRemoveHelper.setCheckPosition(position)
                        } else {
                            AppLog.e(TAG, "intoNovelContent")
                            if (position >= presenter.iBookList.size || position < 0) return
                            val book = presenter.iBookList[position]
                            handleBook(book)
                            presenter.uploadItemClickLog(position)
                        }
                    }
                },
                object : BookShelfReAdapter.ShelfItemLongClickListener {
                    override fun onItemLongClick(view: View, position: Int) {
                        if (!bookShelfRemoveHelper.isRemoveMode) {
                            this@BookShelfFragment.bookShelfRemoveHelper
                                    .showRemoveMenu2(bookshelf_refresh_view, position)
                            if (bookshelf_float_ad != null) {
                                bookshelf_float_ad.visibility = View.GONE
                            }
                            presenter.uploadItemLongClickLog()
                        }
                    }

                }, false)

    }
    val bookShelfRemoveHelper: BookShelfRemoveHelper by lazy {
        val helper = BookShelfRemoveHelper(activity, bookShelfReAdapter)
        helper.setLayout(bookshelf_refresh_view)
        helper.setOnMenuStateListener(this)
        helper.setOnMenuDeleteListener(this)
        helper
    }

    private val fragmentCallback: BaseFragment.FragmentCallback by lazy { activity as BaseFragment.FragmentCallback }
    private val bookSensitiveWords: ArrayList<String> = ArrayList()
    private val noBookSensitive = false

    private var frameBookHelper: FrameBookHelper? = null
    private var bookDaoHelper: BookDaoHelper = BookDaoHelper.getInstance()
    private var isShowAD = false
    private var bookRackUpdateTime: Long = 0
    private var latestLoadDataTime: Long = 0
    private var sharedPreferences: SharedPreferences? = null

    private var bookShelfInterface: BookShelfInterface? = null

    private val bookDeleteDialog: BookDeleteDialog by lazy {
        val dialog = BookDeleteDialog(activity)
        dialog.setOnConfirmListener { books, isChecked ->
            if (books != null && books.isNotEmpty()) presenter.deleteBooks(books, isChecked)
            StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.bs_click_delete_ok_btn)
        }
        dialog.setOnAbrogateListener {
            presenter.uploadBookDeleteCancelLog()
            StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.bs_click_delete_cancel_btn)
        }
        dialog
    }

    var onRemoveModeAllCheckedListener: ((isAllChecked: Boolean) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_bookshelf, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
//        if(PlatformSDK.config() != null){
//            PlatformSDK.config().setBookShelfGrid(true)
//        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        bookRackUpdateTime = AppUtils.getLongPreferences(activity, "book_rack_update_time", System.currentTimeMillis())
//        fragmentCallback.getRemoveMenuHelper(bookShelfRemoveHelper)
        initRecyclerView()
        bookshelf_refresh_view.setOnPullRefreshListener(object : SuperSwipeRefreshLayout.OnPullRefreshListener {
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
        bookshelf_empty_btn.setOnClickListener {
            fragmentCallback.setSelectTab(1)
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SHELF_PAGE, StartLogClickUtil.TOBOOKCITY)
        }

        img_head_setting.setOnClickListener {
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.PERSONAL)
            net.lzbook.kit.utils.StatServiceUtils.statAppBtnClick(activity, net.lzbook.kit.utils.StatServiceUtils.bs_click_mine_menu)
            EventBus.getDefault().post(ConsumeEvent(R.id.redpoint_home_setting))
            RouterUtil.navigation(RouterConfig.SETTING_ACTIVITY)
        }

        rl_head_search.setOnClickListener {
            //TODO 在 mfqbxssc 的 SearchBookActivity 中接收此 bundle，并将值赋给 isSatyHistory
            val bundle = Bundle()
            bundle.putBoolean("isShowLastSearch", false)
            RouterUtil.navigation(RouterConfig.SEARCH_BOOK_ACTIVITY, bundle)
            net.lzbook.kit.utils.StatServiceUtils.statAppBtnClick(activity, net.lzbook.kit.utils.StatServiceUtils.bs_click_search_btn)
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SHELF_PAGE, StartLogClickUtil.SEARCH)
        }

        img_head_menu.setOnClickListener {
            showTitleRightPop(img_head_menu)
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.MORE)
        }

    }

    private val headerView: View by lazy {
        LayoutInflater.from(bookshelf_refresh_view.context)
                .inflate(R.layout.layout_head, null)
    }

    private fun initRecyclerView() {
        bookshelf_refresh_view.setHeaderViewBackgroundColor(0x00000000)
        bookshelf_refresh_view.setHeaderView(createHeaderView())
        bookshelf_refresh_view.isTargetScrollWithLayout = true
        recycler_view.recycledViewPool.setMaxRecycledViews(0, 12)
        val layoutManager = ShelfGridLayoutManager(activity, 3)
        recycler_view.layoutManager = layoutManager
        recycler_view.isFocusable = false//放弃焦点
        //        recyclerView.getItemAnimator().setSupportsChangeAnimations(false);
        recycler_view.itemAnimator.addDuration = 0
        recycler_view.itemAnimator.changeDuration = 0
        recycler_view.itemAnimator.moveDuration = 0
        recycler_view.itemAnimator.removeDuration = 0
        (recycler_view.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        recycler_view.adapter = bookShelfReAdapter
    }

    private fun createHeaderView(): View {
        headerView.head_text_view.text = "下拉刷新"
        headerView.head_image_view.visibility = View.VISIBLE
        headerView.head_image_view.setImageResource(R.drawable.pulltorefresh_down_arrow)
        headerView.head_pb_view.visibility = View.GONE
        return headerView
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

        presenter.clickNotification(context, activity.intent)

        initUpdateService()

        //根据书架数量确定是否刷新
        if (presenter.iBookList.size > 0) {
            bookshelf_refresh_view.isRefreshing = true
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
//        getFloatAd(activity)
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
    }

    /**
     * 查Book数据库更新界面
     */
    fun updateUI() {
        if (activity != null && !activity.isFinishing) {
//        val isNotShowAd = !isShowAD || bookShelfRemoveHelper.isRemoveMode || !isResumed

            Log.d("isShowAd", "bookShelfRemoveHelper：" + !bookShelfRemoveHelper.isRemoveMode)
            Log.d("isShowAd", "isResumed：" + isResumed)
            Log.d("isShowAd", "Constants.isHideAD：" + !Constants.isHideAD)
            Log.d("isShowAd", "Constants.book_shelf_state：" + Constants.book_shelf_state)

            val isShowAd = !bookShelfRemoveHelper.isRemoveMode && isResumed && !Constants.isHideAD && Constants.book_shelf_state != 0
//        doAsync {
            presenter.queryBookListAndAd(activity, isShowAd)

            runOnMain {
                if (bookShelfReAdapter != null) {
                    bookShelfReAdapter.setUpdate_table(presenter.filterUpdateTableList())
                    bookShelfReAdapter.notifyDataSetChanged()
                }
                if (sharedPreferences != null) {
                    presenter.uploadFirstOpenLog(sharedPreferences!!)
                }

            }
//        }
        }
    }

    override fun onBookListQuery(bookList: ArrayList<Book>) {
        if (activity != null && !activity.isFinishing) {
            if (bookList.isEmpty()) {
                if (bookshelf_refresh_view != null) {
                    bookshelf_refresh_view.setPullToRefreshEnabled(false)
                }
                if (bookshelf_empty != null) {
                    bookshelf_empty.visibility = View.VISIBLE
                }
                if (item_ad_layout != null) {
                    item_ad_layout.visibility = View.GONE
                }

            } else {
                if (bookshelf_refresh_view != null) {
                    bookshelf_refresh_view.setPullToRefreshEnabled(true)
                }
                if (bookshelf_empty != null) {
                    bookshelf_empty.visibility = View.GONE
                }
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
                        activity.applicationContext.toastShort(textId)
                    }
                }
    }

    private fun showToastDelay(text: String) {
        if (!isAdded) return
        Flowable.timer(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (activity != null && !activity.isFinishing) {
                        activity.applicationContext.toastShort(text)
                    }
                }
    }

    /**
     * 处理被点击或更新通知的book
     */
    private fun handleBook(book: Book?) {
        AppLog.e(TAG, "handleBook")
        if (book != null && activity != null && !activity.isFinishing) {
            if (!TextUtils.isEmpty(book.book_id) && book.book_type == 0 && presenter != null) {
                presenter.resetUpdateStatus(book.book_id)
            }

            if (Constants.isShielding && !noBookSensitive && bookSensitiveWords.contains(book.book_id.toString())) {
                ToastUtils.showToastNoRepeat("抱歉，该小说已下架！")
            } else {
                if (book != null) {
//                    BookHelper.goToCoverOrRead(activity.applicationContext, activity, book, 0)
                    BookRouter.navigateCoverOrRead(activity, book, BookRouter.NAVIGATE_TYPE_BOOKSHELF)
                    AppLog.e(TAG, "goToCoverOrRead")
                }
            }
        }

    }

    override fun onSuccess(result: BookUpdateResult) {
        if (activity != null && !activity.isFinishing) {
            latestLoadDataTime = System.currentTimeMillis()
            bookRackUpdateTime = System.currentTimeMillis()
            if (bookshelf_refresh_view != null) {
                bookshelf_refresh_view.onRefreshComplete()
            }
            if (presenter != null) {
                presenter.handleSuccessUpdate(result)
            }
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
            if (firstBook != null) {
                val bookName = firstBook?.book_name
                val bookLastChapterName = firstBook?.last_chapter_name
                if (bookName?.isNotEmpty() == true && bookLastChapterName?.isNotEmpty() == true) {
                    if (updateCount == 1) {
                        showToastDelay("《$bookName${activity.getString(R.string.bookshelf_one_book_update)}" +
                                "$bookLastChapterName")
                    } else {
                        showToastDelay("《$bookName${activity.getString(R.string.bookshelf_more_book_update)}" +
                                "$updateCount${activity.getString(R.string.bookshelf_update_chapters)}")
                    }
                }
            }
        }
    }

    fun doUpdateBook() {
        presenter.addUpdateTask(this)
    }

    override fun doUpdateBook(updateService: CheckNovelUpdateService) {
        if (updateService != null) {
            updateService.setBookUpdateListener(activity as CheckNovelUpdateService.OnBookUpdateListener)
        }

        presenter.addUpdateTask(this)

    }

    override fun notification(gid: String) {
        if (!TextUtils.isEmpty(gid) && bookDaoHelper != null) {
            var book = bookDaoHelper.getBook(gid, 0) as Book?
            handleBook(book)
        }
    }

    override fun onMenuDelete(checked_state: HashSet<Int>) {
        if (checked_state.isEmpty()) return
        val checkedBooks = ArrayList<Book>()
        if (presenter != null) {
            val size = presenter.iBookList.size
            (0 until size).filter {
                checked_state.contains(it)
            }.mapTo(checkedBooks) {
                presenter.iBookList[it]
            }
        }

        if (checkedBooks.isNotEmpty()) {
            bookDeleteDialog.show(checkedBooks)
        }
    }

    override fun onBookDelete() {
        if (activity != null && !activity.isFinishing) {
            updateUI()
            bookShelfRemoveHelper.dismissRemoveMenu()
        }

    }

    override fun getMenuShownState(isShown: Boolean) {
        AppLog.e(TAG, "getMenuShowState: $isShown")
        if (isShown) {
            if (bookshelf_refresh_view != null) {
                bookshelf_refresh_view.setPullToRefreshEnabled(false)
            }
        } else {
            if (presenter.iBookList.isNotEmpty()) {
                if (bookshelf_refresh_view != null) {
                    bookshelf_refresh_view.setPullToRefreshEnabled(true)
                }
                updateUI()
//                getFloatAd(activity)
            }

        }
//        fragmentCallback.getMenuShownState(state)
        bookShelfInterface?.changeHomeNavigationState(isShown)
    }

    override fun getAllCheckedState(isAllChecked: Boolean) {
//        fragmentCallback.getAllCheckedState(isAll)
        onRemoveModeAllCheckedListener?.invoke(isAllChecked)
    }

    override fun doHideAd() {
        presenter.removeAd()
    }


    //顶部横条广告位
//    override fun showShlefTopItem(shouldShow: Boolean) {
//        if (shouldShow) {
//            doAsync {
//                PlatformSDK.adapp().dycmNativeAd(activity.applicationContext, "1-1", null, object : AbstractCallback() {
//                    override fun onResult(adswitch: Boolean, views: List<ViewGroup>, jsonResult: String?) {
//                        super.onResult(adswitch, views, jsonResult)
//                        if (!adswitch) {
//                            return
//                        }
//                        try {
//                            val jsonObject = JSONObject(jsonResult)
//                            if (jsonObject.has("state_code")) {
//                                when (ResultCode.parser(jsonObject.getInt("state_code"))) {
//                                    ResultCode.AD_REQ_SUCCESS
//                                    -> {
//                                        runOnMain {
//                                            if (item_ad_layout != null) {
//                                                item_ad_layout.removeAllViews()
//                                                if (views != null && views.size > 0) {
//                                                    item_ad_layout.addView(views[0])
//                                                    item_ad_layout.visibility = View.VISIBLE
//                                                } else {
//                                                    item_ad_layout.visibility = View.GONE
//                                                }
//                                            }
//                                        }
//                                    }
//                                    else -> {
//                                        runOnMain {
//                                            if (item_ad_layout != null) {
//                                                item_ad_layout.visibility = View.GONE
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        } catch (e: JSONException) {
//                            e.printStackTrace()
//                        }
//                    }
//                })
//            }
//
//        } else {
//            runOnMain {
//                if (item_ad_layout != null) {
//                    item_ad_layout.visibility = View.GONE
//                }
//            }
//        }
//    }
//
//    override fun showShlefFloatAd(shouldShow: Boolean, showView: ViewGroup?) {
//        if (shouldShow) {
//            if (bookshelf_float_ad != null && showView != null) {
//                bookshelf_float_ad.visibility = View.VISIBLE
//                bookshelf_float_ad.removeAllViews()
//                bookshelf_float_ad.addView(showView)
//            }
//        } else {
//            if (bookshelf_float_ad != null) {
//                bookshelf_float_ad.visibility = View.GONE
//            }
//        }
//
//    }

    /**
     * 获取悬浮广告位
     */
//    fun getFloatAd(activity: Activity) {
//        if (!Constants.isHideAD && Constants.dy_shelf_boundary_switch && presenter.iBookList.isNotEmpty()) {
//            doAsync {
//                if (PlatformSDK.adapp() != null) {
//                    PlatformSDK.adapp().dycmNativeAd(activity, "1-2", null, object : AbstractCallback() {
//                        override fun onResult(adswitch: Boolean, views: List<ViewGroup>?, jsonResult: String?) {
//                            super.onResult(adswitch, views, jsonResult)
//                            if (!adswitch) return
//                            try {
//                                val jsonObject = JSONObject(jsonResult)
//                                if (jsonObject.has("state_code")) {
//                                    runOnMain {
//                                        when (ResultCode.parser(jsonObject.getInt("state_code"))) {
//                                            ResultCode.AD_REQ_SUCCESS -> {
//                                                if (views != null && views.size > 0) {
//                                                    if (bookshelf_float_ad != null) {
//                                                        bookshelf_float_ad.visibility = View.VISIBLE
//                                                        bookshelf_float_ad.removeAllViews()
//                                                        bookshelf_float_ad.addView(views.get(0))
//                                                    }
//                                                }
//                                            }
//                                            ResultCode.AD_REQ_FAILED -> {
//                                                if (bookshelf_float_ad != null) {
//                                                    bookshelf_float_ad.visibility = View.GONE
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            } catch (e: JSONException) {
//                                e.printStackTrace()
//                            }
//                        }
//                    })
//                }
//
//            }
//
//        }
//
//    }

    private fun showTitleRightPop(view: View) {
        //自定义PopupWindow的布局
        val contentView = LayoutInflater.from(activity).inflate(R.layout.popwindow_title_right, null)
        val popupWindow = PopupWindow(contentView)
        popupWindow.width = LinearLayout.LayoutParams.WRAP_CONTENT
        popupWindow.height = LinearLayout.LayoutParams.WRAP_CONTENT
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(0x00000000))   //为PopupWindow设置透明背景.
        popupWindow.isOutsideTouchable = false

        //缓存
        contentView.linear_huancun.setOnClickListener{
            RouterUtil.navigation(RouterConfig.DOWNLOAD_MANAGER_ACTIVITY)
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SHELF_PAGE, StartLogClickUtil.CACHEMANAGE)
            popupWindow.dismiss()
        }
        //排序
        contentView.linear_paixu.setOnClickListener{
            popupWindow.dismiss()
            showSortPop(rl_content)
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SHELF_PAGE, StartLogClickUtil.BOOKSORT)
        }

        //设置PopupWindow显示的位置
        popupWindow.showAsDropDown(view, 0, -(view.height + 30))
    }

    private var settingItemsHelper: SettingItemsHelper? = null
    private var settingItems: SettingItems? = null

    private fun showSortPop(view: View) {
        //自定义PopupWindow的布局
        val contentView = LayoutInflater.from(activity).inflate(R.layout.popwindow_paixu, null)
        val popupWindow = PopupWindow(contentView)
        popupWindow.width = LinearLayout.LayoutParams.MATCH_PARENT
        popupWindow.height = LinearLayout.LayoutParams.WRAP_CONTENT
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(-0x50000000))   //为PopupWindow设置半透明背景.
        popupWindow.isOutsideTouchable = false

        settingItemsHelper = SettingItemsHelper.getSettingHelper(activity)
        settingItems = settingItemsHelper?.values
        if (settingItems?.booklist_sort_type == 1) {
            contentView.btn_time.isChecked = true
            contentView.btn_recent.isChecked = false
        } else {
            contentView.btn_recent.isChecked = true
            contentView.btn_time.isChecked = false
        }
        setBackgroundAlpha(0.6f)

        contentView.btn_time.setOnClickListener {
            contentView.btn_time.isChecked = true
            contentView.btn_recent.isChecked = false
            StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.me_set_cli_shelf_rak_time)
            settingItemsHelper?.putInt(settingItemsHelper?.booklistSortType, 1)
            Constants.book_list_sort_type = 1
            setBackgroundAlpha(1.0f)
            updateUI()
            popupWindow.dismiss()
        }
        contentView.btn_recent.setOnClickListener {
            contentView.btn_recent.isChecked = true
            contentView.btn_time.isChecked = false
            StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.me_set_cli_shelf_rak_time)
            settingItemsHelper?.putInt(settingItemsHelper?.booklistSortType, 0)
            Constants.book_list_sort_type = 0
            setBackgroundAlpha(1.0f)
            updateUI()
            popupWindow.dismiss()
        }

        //设置PopupWindow进入和退出动画
        popupWindow.animationStyle = R.style.remove_menu_anim_style
        //设置PopupWindow显示的位置
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0)
    }

    private fun setBackgroundAlpha(bgAlpha: Float) {
        val lp = activity.window.attributes
        lp.alpha = bgAlpha
        activity.window.attributes = lp
    }

    interface BookShelfInterface {

        fun changeHomeNavigationState(state: Boolean)

        fun changeHomePagerIndex(index: Int)

        fun changeDrawerLayoutState()
    }

    companion object {
        private const val PULL_REFRESH_DELAY = 30 * 1000
        private val TAG = BookShelfFragment::class.java.simpleName
    }

}
