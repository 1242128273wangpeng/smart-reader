package com.intelligent.reader.activity

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.*
import android.view.animation.AlphaAnimation
import android.webkit.WebView
import android.widget.LinearLayout
import com.alibaba.sdk.android.feedback.impl.FeedbackAPI
import com.baidu.mobstat.StatService

import com.dingyue.bookshelf.BookShelfFragment
import com.intelligent.reader.BuildConfig
import com.intelligent.reader.R
import com.intelligent.reader.app.BookApplication
import com.dingyue.bookshelf.BaseFragment
import com.intelligent.reader.fragment.WebViewFragment
import com.dingyue.bookshelf.BookShelfRemoveHelper
import com.intelligent.reader.event.DownloadManagerToHome
import com.intelligent.reader.fragment.CategoryFragment
import com.intelligent.reader.presenter.home.HomePresenter
import com.intelligent.reader.presenter.home.HomeView
import com.intelligent.reader.util.EventBookStore
import com.intelligent.reader.widget.BookSortingDialog
import com.intelligent.reader.widget.ClearCacheDialog
import com.intelligent.reader.widget.HomeMenuPopup
import com.intelligent.reader.widget.drawer.DrawerLayout

import net.lzbook.kit.app.ActionConstants
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.appender_loghub.appender.AndroidLogStorage
import net.lzbook.kit.book.component.service.CheckNovelUpdateService
import net.lzbook.kit.book.view.NonSwipeViewPager
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.BookEvent
import net.lzbook.kit.data.bean.ReadConfig
import net.lzbook.kit.data.bean.RequestItem
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.utils.oneclick.AntiShake
import net.lzbook.kit.utils.update.ApkUpdateUtils

import java.io.File
import java.util.HashMap

import de.greenrobot.event.EventBus
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import iyouqu.theme.BaseCacheableActivity
import iyouqu.theme.StatusBarCompat
import iyouqu.theme.ThemeMode
import kotlinx.android.synthetic.txtqbmfyd.content_view.*
import kotlinx.android.synthetic.txtqbmfyd.content_view_main.*
import kotlinx.android.synthetic.txtqbmfyd.content_view_menu.*
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.cache.DataCleanManager
import net.lzbook.kit.constants.SPKeys
import net.lzbook.kit.encrypt.URLBuilderIntterface
import net.lzbook.kit.router.RouterConfig
import net.lzbook.kit.router.RouterUtil
import net.lzbook.kit.utils.*
import java.util.concurrent.TimeUnit

/**
 * 书架,书城页面
 * Created by q on 2015/9/7.
 */
class HomeActivity : BaseCacheableActivity(), BaseFragment.FragmentCallback, WebViewFragment.FragmentCallback, CheckNovelUpdateService.OnBookUpdateListener, HomeView {
    var frameHelper: FrameBookHelper? = null

    private var fManager: FragmentManager? = null
    private var viewPager: NonSwipeViewPager? = null

    private lateinit var filter: IntentFilter

    private var removeMenuHelper: BookShelfRemoveHelper? = null
    private var bookView: BookShelfFragment? = null
    private var isClosed = false
    private var receiver: MyReceiver? = null
    private var mLoadDataManager: LoadDataManager? = null
    private val shake = AntiShake()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main2)
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }

        try {
            fManager = this.supportFragmentManager
        } catch (e: NoSuchMethodError) {
            e.printStackTrace()
        }

        initView()
        initData()
        //ownUpdate
        //注册广播接收器
        receiver = MyReceiver()
        filter = IntentFilter()
        filter.addAction(ActionConstants.DOWN_APP_SUCCESS_ACTION)
        this@HomeActivity.registerReceiver(receiver, filter)

        apkUpdateUtils = ApkUpdateUtils(this)
        try {
            apkUpdateUtils.getApkUpdateInfo(this, handler, "HomeActivity")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        initPositon()
        checckUrlIsTest()
        EventBus.getDefault().register(this)
        AndroidLogStorage.getInstance().clear()

        preferencesUtils = SharedPreferencesUtils(PreferenceManager.getDefaultSharedPreferences(this))
        versionCode = AppUtils.getVersionCode()

        adapter?.notifyDataSetChanged()
        frameHelper()

        showCacheMessage()
    }

    private fun checckUrlIsTest() {
        if (UrlUtils.getBookNovelDeployHost().contains("test") || UrlUtils.getBookWebviewHost().contains("test")) {
            ToastUtils.showToastNoRepeat("请注意！！请求的是测试地址！！！")
        }
    }

    private fun initPositon() {
        val intent = intent
        val position: Int
        if (intent != null) {
            if (intent.hasExtra(EventBookStore.BOOKSTORE)) {
                position = intent.getIntExtra(EventBookStore.BOOKSTORE, 0)
                selectTab(position)

            } else {
                val intExtra = intent.getIntExtra(EventBookStore.BOOKSTORE, EventBookStore
                        .TYPE_ERROR)
                if (intExtra != EventBookStore.TYPE_ERROR) {
                    if (!isFinishing) {
                        selectTab(intExtra)
                    }
                }
            }
        }
    }

    override fun receiveUpdateCallBack(preNTF: Notification) {
        val intent = Intent(this, HomeActivity::class.java)
        val pending = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        preNTF.contentIntent = pending
    }

    /**
     * 打开安装包文件
     */
    fun setup(filePath: String) {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.action = Intent.ACTION_VIEW
        val type = "application/vnd.android.package-archive"
        intent.setDataAndType(Uri.fromFile(File(filePath)), type)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        AppLog.e(TAG, "onResume")
        bookShelfFragment?.bookShelfReAdapter?.notifyDataSetChanged()
        selectTab(currentTab)
        StatService.onResume(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        var position = 0
        if (intent != null && intent.hasExtra("position")) {
            position = intent.getIntExtra("position", 0)
            viewPager!!.currentItem = position
        } else {
            if (intent != null) {//for bookend
                val intExtra = intent.getIntExtra(EventBookStore.BOOKSTORE, EventBookStore
                        .TYPE_ERROR)
                if (intExtra != EventBookStore.TYPE_ERROR) {
                    if (!isFinishing) {
                        selectTab(intExtra)
                    }
                }
            }
        }
    }

    private fun initView() {
        //main
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                onTabSelected(position)
            }
        })
        view_pager.offscreenPageLimit = 3
        view_pager.isScrollable = false
        fManager?.let {
            adapter = MainAdapter(it)
        }
        view_pager.adapter = adapter
        viewPager = view_pager
        getViewPager(view_pager)

        selectTab(currentTab)
        onTabSelected(currentTab)

        drawer_layout.setOnMenuStateChangeListener { state ->
            if (state == DrawerLayout.MenuState.MENU_OPENED) {
                showCacheMessage()
                val bookShelfRemoveHelper = bookShelfFragment?.bookShelfRemoveHelper
                if (bookShelfRemoveHelper?.isRemoveMode == true) {
                    bookShelfRemoveHelper.dismissRemoveMenu()
                }
            }
        }

        img_head_setting.setOnClickListener {
            //            EventBus.getDefault().post(ConsumeEvent(R.id.red_point_head_setting))
            if (drawer_layout.isOpened) {
                drawer_layout.closeMenu()
            } else {
                drawer_layout.openMenu()
            }
            presenter.uploadHeadSettingLog()
        }

        img_head_search.setOnClickListener {
            AppLog.e(TAG, "SearchBookActivity -----> Start")
            startActivity(Intent(this, SearchBookActivity::class.java))
            presenter.uploadHeadSearchLog(bottomType)
        }

        rl_recommend_search.setOnClickListener {
            startActivity(Intent(this, SearchBookActivity::class.java))
            presenter.uploadHeadSearchLog(bottomType)
        }

        img_ranking_search.setOnClickListener {
            startActivity(Intent(this, SearchBookActivity::class.java))
            presenter.uploadHeadSearchLog(bottomType)
        }

        img_head_menu.setOnClickListener {
            homeMenuPopup.show(img_head_menu)
            StartLogClickUtil.upLoadEventLog(this,
                    StartLogClickUtil.SHELF_PAGE, StartLogClickUtil.MORE)
        }

        ll_bottom_tab_bookshelf.setOnClickListener {
            AppLog.e(TAG, "BookShelf Selected")
            selectTab(0)
            presenter.uploadBookshelfSelectedLog()
        }

        ll_bottom_tab_recommend.setOnClickListener {
            AppLog.e(TAG, "Selection Selected")
            selectTab(1)
            //双击回到顶部
//            if (AppUtils.isDoubleClick(System.currentTimeMillis())) {
//                if (view_pager.currentItem == 1) {
//                    recommendFragment.loadWebData(recommendFragment.url)
//                }
//            }
            preferencesUtils.putString(Constants.FINDBOOK_SEARCH, "recommend")
            presenter.uploadRecommendSelectedLog()
        }

        ll_bottom_tab_ranking.setOnClickListener {
            AppLog.e(TAG, "Ranking Selected")
            selectTab(2)
            preferencesUtils.putString(Constants.FINDBOOK_SEARCH, "top")
            presenter.uploadRankingSelectedLog()
        }

        ll_bottom_tab_category.setOnClickListener {
            AppLog.e(TAG, "Classify Selected")
            selectTab(3)
            preferencesUtils.putString(Constants.FINDBOOK_SEARCH, "class")
            presenter.uploadCategorySelectedLog()
        }

//        img_editor_back.setOnClickListener {
        //TODO move
//            bookShelfFragment?.bookShelfRemoveHelper?.dismissRemoveMenu()
//            presenter.uploadEditorBackLog()
//        }

        txt_editor_select_all.setOnClickListener {
            val bookShelfRemoveHelper = bookShelfFragment?.bookShelfRemoveHelper
            val isAllSelected = bookShelfRemoveHelper?.isAllChecked ?: false
            if (isAllSelected) {
                txt_editor_select_all.text = getString(R.string.select_all)
                bookShelfRemoveHelper?.selectAll(false)
            } else {
                txt_editor_select_all.text = getString(R.string.select_all_cancel)
                bookShelfRemoveHelper?.selectAll(true)
            }
            presenter.uploadEditorSelectAllLog(isAllSelected)
        }


        //menu
        setMenuTitleMargin()

        setNightMode(false)
        bt_night_shift.setOnCheckedChangeListener { _, isChecked ->
            presenter.uploadModeChangeLog()
            if (isChecked) {
                tv_night_shift.setText(R.string.mode_day)
                ReadConfig.MODE = 61
                preferencesUtils.putInt("current_light_mode", ReadConfig.MODE)
                mThemeHelper.setMode(ThemeMode.NIGHT)
            } else {
                tv_night_shift.setText(R.string.mode_night)
                ReadConfig.MODE = 51
                preferencesUtils.putInt("current_night_mode", ReadConfig.MODE)
                mThemeHelper.setMode(ThemeMode.THEME1)
            }
            preferencesUtils.putInt("content_mode", ReadConfig.MODE)
            nightShift(isChecked, true)
        }

        val isAutoDownload = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(SPKeys.Setting.AUTO_UPDATE_CAHCE, true)
        btn_auto_download.isChecked = isAutoDownload
        btn_auto_download.setOnCheckedChangeListener { view, isChecked ->
            preferencesUtils.putBoolean(SPKeys.Setting.AUTO_UPDATE_CAHCE, isChecked)
            presenter.uploadAutoCacheLog(isChecked)
        }

        txt_push_setting.setOnClickListener {
            presenter.uploadPushSettingClickLog()
            startActivity(Intent(this, SettingMoreActivity::class.java))
        }

        txt_feedback.setOnClickListener {
            presenter.uploadFeedbackClickLog()
            Observable.timer(500, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        FeedbackAPI.openFeedbackActivity()
                    }
        }

        txt_mark.setOnClickListener {
            presenter.uploadMarkClickLog()
            try {
                val uri = Uri.parse("market://details?id=" + this.packageName)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (e: Exception) {
                this.toastShort(R.string.menu_no_market, false)
            }

        }

        txt_disclaimer_statement.setOnClickListener {
            presenter.uploadDisclaimerClickLog()
            startActivity(Intent(this, DisclaimerActivity::class.java))
        }

        val versionName = "V${AppUtils.getVersionName()}"
        txt_version_name.text = versionName
        rl_check_update.setOnClickListener {
            presenter.uploadCheckUpdateLog()
            try {
                apkUpdateUtils.getApkUpdateInfo(this, null, "SettingActivity")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        rl_clear_cache.setOnClickListener {
            presenter.uploadClearCacheClickLog()

            if(this != null){
                clearCacheDialog.show()
            }
        }

        txt_clear_cache_message.text = "0B"

    }

    protected fun initData() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val edit = sharedPreferences.edit()
        //获取阅读页背景
        if (sharedPreferences.getInt("content_mode", 51) < 50) {
            Constants.MODE = 51
            ReadConfig.MODE = 51
            edit.putInt("content_mode", Constants.MODE)
            edit.putInt("current_light_mode", Constants.MODE)
            edit.apply()
        } else {
            Constants.MODE = sharedPreferences.getInt("content_mode", 51)
            ReadConfig.MODE = sharedPreferences.getInt("content_mode", 51)
        }

        //判断用户是否是当日首次打开应用
        val first_time = sharedPreferences.getLong(Constants.TODAY_FIRST_OPEN_APP, 0)
        AppLog.e("BaseBookApplication", "first_time=$first_time")
        val currentTime = System.currentTimeMillis()
        val b = AppUtils.isToday(first_time, currentTime)
        if (b) {
            //用户非首次打开
            Constants.is_user_today_first = false
        } else {
            //用户首次打开，记录当前时间
            Constants.is_user_today_first = true
            sharedPreferences.edit().putLong(Constants.TODAY_FIRST_OPEN_APP, currentTime).apply()
            sharedPreferences.edit().putBoolean(Constants.IS_UPLOAD, false).apply()
            GetAppList().execute()
        }
        AppLog.e("BaseBookApplication", "Constants.is_user_today_first=" + Constants.is_user_today_first)

        mLoadDataManager = LoadDataManager(this)
        Constants.upload_userinformation = sharedPreferences.getBoolean(Constants.IS_UPLOAD, false)

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (getDrawerLayout().isOpened) {
                getDrawerLayout().closeMenu()
                return true
            } else if (viewPager != null && viewPager!!.currentItem != 0) {
                selectTab(0)
                return true
            } else if (removeMenuHelper != null && removeMenuHelper!!.dismissRemoveMenu()) {

                return true
            } else {
                doubleClickFinish()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onPause() {
        try {
            super.onPause()
            StatService.onPause(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 接收默认书籍的加载完成刷新
     */
    fun onEvent(event: BookEvent) {
        if (event.msg == BookEvent.DEFAULTBOOK_UPDATED) {
            if (mLoadDataManager != null)
                mLoadDataManager!!.updateShelfBooks()
        } else if (event.msg == BookEvent.PULL_BOOK_STATUS) {
            if (bookView != null) {
                bookView!!.updateUI()
            }
        }
    }

    /**
     * 两次返回键退出
     */
    private fun doubleClickFinish() {
        BACK_COUNT++
        if (BACK_COUNT == 1) {
            showToastLong(R.string.mian_click_tiwce_exit)
        } else if (BACK_COUNT > 1 && !isClosed) {
            isClosed = true
            restoreSystemDisplayState()
            ATManager.exitClient()
            finish()
        }
        val message = handler.obtainMessage(0)
        message.what = BACK
        handler.sendMessageDelayed(message, 2000)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun getViewPager(pager: ViewPager) {
        this.viewPager = pager as NonSwipeViewPager
    }

    override fun getRemoveMenuHelper(helper: BookShelfRemoveHelper) {
        this.removeMenuHelper = helper
    }

    override fun getFrameBookRankView(bookView: Fragment) {
        this.bookView = bookView as BookShelfFragment
    }

    override fun frameHelper() {
        if (frameHelper == null) {
            frameHelper = FrameBookHelper(applicationContext, this@HomeActivity)
        }
    }

    override fun getAllCheckedState(isAllChecked: Boolean) {}

    override fun getMenuShownState(state: Boolean) {
        onMenuShownState(state)
    }

    override fun setSelectTab(index: Int) {
        selectTab(index)
    }

    override fun onDestroy() {
        super.onDestroy()
        AndroidLogStorage.getInstance().clear()
        this@HomeActivity.unregisterReceiver(receiver)

        EventBus.getDefault().unregister(this)

        if (frameHelper != null) {
            frameHelper!!.restoreState()
            frameHelper = null
        }
        removeMenuHelper = null
        viewPager = null
        bookView = null
        try {
            setContentView(R.layout.empty)
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }

        if (BuildConfig.DEBUG) {
            BookApplication.getRefWatcher().watch(this)
        }
        EventBus.getDefault().unregister(this)
        //        PlatformSDK.lifecycle().onDestroy();

        AppLog.e(TAG, "onDetach")
        bookShelfFragment?.onRemoveModeAllCheckedListener = null
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

    override fun webJsCallback(jsInterfaceHelper: JSInterfaceHelper) {


        jsInterfaceHelper.setOnEnterAppClick { AppLog.e(TAG, "doEnterApp") }
        jsInterfaceHelper.setOnSearchClick { keyWord, search_type, filter_type, filter_word, sort_type ->
            try {
                val data = HashMap<String, String>()
                data["keyword"] = keyWord
                data["type"] = "0"//0 代表从分类过来
                StartLogClickUtil.upLoadEventLog(this@HomeActivity, StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.SYSTEM_SEARCHRESULT, data)

                val intent = Intent()
                intent.setClass(this@HomeActivity, SearchBookActivity::class.java)
                intent.putExtra("word", keyWord)
                intent.putExtra("search_type", search_type)
                intent.putExtra("filter_type", filter_type)
                intent.putExtra("filter_word", filter_word)
                intent.putExtra("sort_type", sort_type)
                intent.putExtra("from_class", "fromClass")//是否从分类来
                startActivity(intent)
                AppLog.e("kkk", "$search_type===")

            } catch (e: Exception) {
                AppLog.e(TAG, "Search failed")
                e.printStackTrace()
            }
        }
        jsInterfaceHelper.setOnAnotherWebClick(JSInterfaceHelper.onAnotherWebClick { url, name ->
            if (shake.check()) {
                return@onAnotherWebClick
            }
            AppLog.e(TAG, "doAnotherWeb")
            try {
                val intent = Intent()
                intent.setClass(this@HomeActivity, FindBookDetail::class.java)
                intent.putExtra("url", url)
                intent.putExtra("title", name)
                startActivity(intent)
                AppLog.e(TAG, "EnterAnotherWeb")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
        jsInterfaceHelper.setOnOpenAd { AppLog.e(TAG, "doOpenAd") }
        jsInterfaceHelper.setOnEnterCover(JSInterfaceHelper.onEnterCover { host, book_id, book_source_id, name, author, parameter, extra_parameter ->
            if (shake.check()) {
                return@onEnterCover
            }
            val data = HashMap<String, String>()
            data["BOOKID"] = book_id
            data["source"] = "WEBVIEW"
            StartLogClickUtil.upLoadEventLog(this@HomeActivity, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.ENTER, data)

            val requestItem = RequestItem()
            requestItem.book_id = book_id
            requestItem.book_source_id = book_source_id
            requestItem.host = host
            requestItem.name = name
            requestItem.author = author
            requestItem.parameter = parameter
            requestItem.extra_parameter = extra_parameter

            val intent = Intent()
            intent.setClass(applicationContext, CoverPageActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable(Constants.REQUEST_ITEM, requestItem)
            intent.putExtras(bundle)
            startActivity(intent)
        })


        jsInterfaceHelper.setOnEnterCategory { gid, nid, name, lastSort -> AppLog.e(TAG, "doCategory") }


    }

    override fun startLoad(webView: WebView, url: String): String {
        return url
    }

    /**
     * 获取广播数据
     *
     * @author jiqinlin
     */
    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val bundle = intent.extras
            val count = bundle!!.getInt("count")
            val filePath = bundle.getString("filePath")
            val downloadLink = bundle.getString("downloadLink")
            val md5 = bundle.getString("md5")
            val fileName = filePath!!.substring(filePath.lastIndexOf("/") + 1)
            if (count == 100) {
                AppLog.e("--------------->", MD5Utils.getFileMD5(File(filePath)))
                if (MD5Utils.getFileMD5(File(filePath))!!.equals(md5!!, ignoreCase = true)) {
                    setup(filePath)
                } else {
                    val errorIntent = Intent()
                    errorIntent.setClass(context, DownloadErrorActivity::class.java)
                    errorIntent.putExtra("downloadLink", downloadLink)
                    errorIntent.putExtra("md5", md5)
                    errorIntent.putExtra("fileName", fileName)
                    errorIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(errorIntent)
                }
            }
        }
    }

    // 获取用户app列表
    internal inner class GetAppList : AsyncTask<Void, Int, String>() {


        override fun doInBackground(vararg params: Void): String {
            return AppUtils.scanLocalInstallAppList(packageManager)
        }

        override fun onPostExecute(s: String) {

            StartLogClickUtil.upLoadApps(this@HomeActivity, s)
        }
    }

    override fun supportSlideBack(): Boolean {
        return false
    }

    companion object {

        val EVENT_CHANGE_NIGHT_MODE = "event_change_night_mode"

        private val BACK = 12
        private var BACK_COUNT: Int = 0

        internal var handler = Handler(Handler.Callback { msg ->
            when (msg.what) {
                BACK -> BACK_COUNT = 0
            }
            true
        })
    }














    private val presenter by lazy { HomePresenter(this) }

    private var bookShelfFragment: BookShelfFragment? = null

    private lateinit var apkUpdateUtils: ApkUpdateUtils

    private val recommendFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()
        val bundle = Bundle()
        bundle.putString("type", "recommend")
        val uri = URLBuilderIntterface.WEB_RECOMMEND.replace("{packageName}", AppUtils.getPackageName())
        bundle.putString("url", UrlUtils.buildWebUrl(uri, HashMap()))
        fragment.arguments = bundle
        fragment
    }

    private val rankingFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()
        val bundle = Bundle()
        bundle.putString("type", "rank")
        val uri = URLBuilderIntterface.WEB_RANK.replace("{packageName}", AppUtils.getPackageName())
        bundle.putString("url", UrlUtils.buildWebUrl(uri, HashMap()))
        fragment.arguments = bundle
        fragment
    }

    private val categoryFragment: CategoryFragment by lazy {
        val fragment = CategoryFragment()
        fragment
    }

    private val clearCacheDialog: ClearCacheDialog by lazy {
        val dialog = ClearCacheDialog(this)
        dialog.setOnConfirmListener {
            dialog.showLoading()
            this.doAsync {
                CacheManager.removeAll()
                UIHelper.clearAppCache()
                DataCleanManager.clearAllCache(this.applicationContext)
                Thread.sleep(1000)
                uiThread {
                    dialog.dismiss()
                    txt_clear_cache_message.text = "0B"
                }
            }
        }
        dialog
    }

    private val homeMenuPopup: HomeMenuPopup by lazy {
        val popup = HomeMenuPopup(this)
        popup.setOnDownloadClickListener {
            RouterUtil.navigation(RouterConfig.DOWNLOAD_MANAGER_ACTIVITY)
            presenter.uploadDownloadManagerLog()
        }
        popup.setOnSortingClickListener {
            bookSortingDialog.show()
            presenter.uploadBookSortingLog()
        }
        popup
    }

    private val settingItemsHelper by lazy { SettingItemsHelper.getSettingHelper(this) }

    private val bookSortingDialog: BookSortingDialog by lazy {
        val dialog = BookSortingDialog(this)
        dialog.setOnRecentReadClickListener {
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_cli_shelf_rak_time)
            settingItemsHelper.putInt(settingItemsHelper.booklistSortType, 0)
            Constants.book_list_sort_type = 0
            bookShelfFragment?.updateUI()
        }
        dialog.setOnUpdateTimeClickListener {
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_cli_shelf_rak_time)
            settingItemsHelper.putInt(settingItemsHelper.booklistSortType, 1)
            Constants.book_list_sort_type = 1
            bookShelfFragment?.updateUI()
        }
        dialog
    }

    private var adapter: MainAdapter? = null
    private var currentTab = 0
    private var versionCode: Int = 0
    private val titles = arrayOf("书架", "推荐", "榜单", "分类")
    private var b: Boolean = true
    private var bottomType: Int = 0//青果打点搜索 2 推荐  3 榜单
    private lateinit var preferencesUtils: SharedPreferencesUtils



    private fun initGuide() {
        val key = versionCode.toString() + Constants.BOOKSHELF_GUIDE_TAG
        if (!preferencesUtils.getBoolean(key)) {
            fl_guide_layout.visibility = View.VISIBLE
            img_guide_remove.visibility = View.VISIBLE
            fl_guide_layout.setOnClickListener {
                if (b) {
                    img_guide_download.visibility = View.VISIBLE
                    img_guide_remove.visibility = View.GONE
                    b = false
                } else {
                    preferencesUtils.putBoolean(key, true)
                    img_guide_download.visibility = View.GONE
                    fl_guide_layout.visibility = View.GONE
                }
            }
        }
    }

    private fun setNightMode(isEvent: Boolean) {
        val isNightMode = this.mThemeHelper.isNight
        if (!isEvent) presenter.uploadCurModeLog(isNightMode)
        if (isNightMode) {
            tv_night_shift.setText(R.string.mode_day)
            bt_night_shift.isChecked = true
        } else {
            tv_night_shift.setText(R.string.mode_night)
            bt_night_shift.isChecked = false
        }
    }

    fun getDrawerLayout(): DrawerLayout = drawer_layout

    fun selectTab(position: Int) {
        if (currentTab != position) {
            AppLog.e(TAG, "position: " + position)
//            view_pager.currentItem = position
            view_pager.setCurrentItem(position, false)
        }
    }

    private fun onTabSelected(position: Int) {
        currentTab = position
        bottomType = position + 1
        if (currentTab != 0) {
            bookShelfFragment?.bookShelfRemoveHelper?.dismissRemoveMenu()
        }
        txt_head_title.text = titles[position]
        ll_bottom_tab_bookshelf.isSelected = position == 0
        ll_bottom_tab_recommend.isSelected = position == 1
        ll_bottom_tab_ranking.isSelected = position == 2
        ll_bottom_tab_category.isSelected = position == 3

        when (position) {
            0 -> {
                rl_head_bookshelf.visibility = View.VISIBLE
                rl_recommend_head.visibility = View.GONE
                rl_head_ranking.visibility = View.GONE
//                img_head_shadow.visibility = View.VISIBLE
            }
            1 -> {
                rl_head_bookshelf.visibility = View.INVISIBLE
                rl_recommend_head.visibility = View.VISIBLE
                rl_head_ranking.visibility = View.GONE
//                img_head_shadow.visibility = View.VISIBLE
            }
            2 -> {
                rl_head_bookshelf.visibility = View.INVISIBLE
                rl_recommend_head.visibility = View.GONE
                rl_head_ranking.visibility = View.VISIBLE
//                img_head_shadow.visibility = View.VISIBLE
                presenter.uploadRankingEntryLog()
            }
            else -> {
                rl_head_bookshelf.visibility = View.GONE
                rl_recommend_head.visibility = View.GONE
                rl_head_ranking.visibility = View.GONE
//                img_head_shadow.visibility = View.GONE
                presenter.uploadCategoryEntryLog()
            }
        }
    }


    /**
     * EventBus 接收下载管理页面的跳转请求
     */
    fun onEventMainThread(event: DownloadManagerToHome) {
        selectTab(event.tabPosition)
    }

    fun onEventMainThread(event: String) {
        if (event == EVENT_CHANGE_NIGHT_MODE) {
            setNightMode(true)
        }
    }


    fun onMenuShownState(state: Boolean) {
        if (state) {
            content_tab_selection.visibility = View.GONE
            img_bottom_shadow.visibility = View.GONE
            if (!rl_head_editor.isShown) {
                val showAnimation = AlphaAnimation(0.0f, 1.0f)
                showAnimation.duration = 200
                rl_head_editor.startAnimation(showAnimation)
                rl_head_editor.visibility = View.VISIBLE
            }
            AnimationHelper.smoothScrollTo(view_pager, 0)
        } else {
            if (rl_head_editor.isShown) {
                rl_head_editor.visibility = View.GONE
            }
            img_bottom_shadow.visibility = View.VISIBLE
            content_tab_selection.visibility = View.VISIBLE
            AnimationHelper.smoothScrollTo(view_pager, 0)
        }
    }


    private inner class MainAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getCount(): Int = 4

        override fun getItem(position: Int): Fragment? {
            AppLog.e(TAG, "position: " + position)
            return when (position) {
                0 -> {
                    if (bookShelfFragment == null) {
                        bookShelfFragment = BookShelfFragment()
                        bookShelfFragment?.onRemoveModeAllCheckedListener = { isAllChecked ->
                            AppLog.e(TAG, "isAllChecked: $isAllChecked")
                            if (isAllChecked) {
                                txt_editor_select_all.text = getString(R.string.select_all_cancel)
                            } else {
                                txt_editor_select_all.text = getString(R.string.select_all)
                            }
                        }
                    }
                    bookShelfFragment
                }
                1 -> recommendFragment
                2 -> rankingFragment
                3 -> categoryFragment
                else -> null
            }
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            return if (position == 0) {
                val bookShelfFragment = super.instantiateItem(container, position) as BookShelfFragment
                bookShelfFragment.doUpdateBook()

                if (view_pager != null && frameHelper != null) {
                    getFrameBookRankView(bookShelfFragment)
                }

                bookShelfFragment
            } else {
                super.instantiateItem(container, position)
            }
        }

        override fun getItemPosition(`object`: Any?): Int = PagerAdapter.POSITION_NONE
    }

    private fun showCacheMessage() {
        doAsync {
            var result = "0B"
            try {
                result = DataCleanManager.getTotalCacheSize(this)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            uiThread { txt_clear_cache_message?.text = result }
        }
    }

    private fun setMenuTitleMargin() {
        val statusBarHeight = StatusBarCompat.getStatusBarHeight(this)
        AppLog.e(TAG, "statusBarHeight: $statusBarHeight")
        val density = resources.displayMetrics.density
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val left = (12 * density + 0.5f).toInt()
        var top = 20
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || isMIUISupport || isFlymeSupport) {
            top += 20
        }
        top = (top * density + 0.5f).toInt()
        params.topMargin = top
        params.leftMargin = left
        txt_menu_title.layoutParams = params
    }
}