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
import com.intelligent.reader.R
import com.dingyue.bookshelf.BaseFragment
import com.intelligent.reader.fragment.WebViewFragment
import com.dingyue.bookshelf.BookShelfRemoveHelper
import com.intelligent.reader.event.DownloadManagerToHome
import com.intelligent.reader.fragment.CategoryFragment
import com.intelligent.reader.presenter.home.HomePresenter
import com.intelligent.reader.presenter.home.HomeView
import com.intelligent.reader.util.EventBookStore
import com.intelligent.reader.widget.ClearCacheDialog
import com.intelligent.reader.widget.drawer.DrawerLayout

import net.lzbook.kit.app.ActionConstants
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.appender_loghub.appender.AndroidLogStorage
import net.lzbook.kit.book.component.service.CheckNovelUpdateService
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
import kotlinx.android.synthetic.main.act_home.*
import kotlinx.android.synthetic.txtqbmfyd.content_view_main.*
import kotlinx.android.synthetic.txtqbmfyd.content_view_menu.*
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.cache.DataCleanManager
import net.lzbook.kit.constants.SPKeys
import net.lzbook.kit.encrypt.URLBuilderIntterface
import net.lzbook.kit.utils.*
import java.util.concurrent.TimeUnit

class HomeActivity : BaseCacheableActivity(), BaseFragment.FragmentCallback, WebViewFragment.FragmentCallback, CheckNovelUpdateService.OnBookUpdateListener, HomeView {

    private val homePresenter by lazy { HomePresenter(this) }

    private var fragmentManager: FragmentManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.act_home)

        try {
            fragmentManager = this.supportFragmentManager
        } catch (exception: NoSuchMethodError) {
            exception.printStackTrace()
        }

        initView()

        homePresenter.initParameters()

        initData()
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

        initPosition()
        checkUrlDevelop()
        EventBus.getDefault().register(this)
        AndroidLogStorage.getInstance().clear()

        preferencesUtils = SharedPreferencesUtils(PreferenceManager.getDefaultSharedPreferences(this))
        versionCode = AppUtils.getVersionCode()

        adapter?.notifyDataSetChanged()
        frameHelper()

        showCacheMessage()

        homePresenter.initDownloadService()
    }

    override fun onResume() {
        super.onResume()

        bookShelfFragment?.bookShelfReAdapter?.notifyDataSetChanged()

        selectTab(currentTab)

        StatService.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        StatService.onPause(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        var position = 0

        if (intent != null && intent.hasExtra("position")) {
            position = intent.getIntExtra("position", 0)
            view_pager!!.currentItem = position
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

        bookView = null

        try {
            setContentView(R.layout.empty)
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }

        EventBus.getDefault().unregister(this)

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


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (dl_content.isOpened) {
                dl_content.closeMenu()
                return true
            } else if (view_pager != null && view_pager!!.currentItem != 0) {
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


    /***
     * 初始化View
     * **/
    private fun initView() {
        dl_content.setOnMenuStateChangeListener { state ->
            if (state == DrawerLayout.MenuState.MENU_OPENED) {
                showCacheMessage()

                val bookShelfRemoveHelper = bookShelfFragment?.bookShelfRemoveHelper
                if (bookShelfRemoveHelper?.isRemoveMode == true) {
                    bookShelfRemoveHelper.dismissRemoveMenu()
                }
            }
        }

        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                onChangeNavigation(position)
            }
        })

        view_pager.offscreenPageLimit = 3

        view_pager.isScrollable = false

        fragmentManager?.let {
            adapter = MainAdapter(it)
        }

        view_pager.adapter = adapter

        selectTab(currentTab)
        onChangeNavigation(currentTab)


        rl_recommend_search.setOnClickListener {
            startActivity(Intent(this, SearchBookActivity::class.java))
            homePresenter.uploadHeadSearchLog(bottomType)
        }

        img_ranking_search.setOnClickListener {
            startActivity(Intent(this, SearchBookActivity::class.java))
            homePresenter.uploadHeadSearchLog(bottomType)
        }

        ll_bottom_tab_bookshelf.setOnClickListener {
            selectTab(0)
            homePresenter.uploadBookshelfSelectedLog()
        }

        ll_bottom_tab_recommend.setOnClickListener {
            AppLog.e(TAG, "Selection Selected")
            selectTab(1)
            preferencesUtils.putString(Constants.FINDBOOK_SEARCH, "recommend")
            homePresenter.uploadRecommendSelectedLog()
        }

        ll_bottom_tab_ranking.setOnClickListener {
            AppLog.e(TAG, "Ranking Selected")
            selectTab(2)
            preferencesUtils.putString(Constants.FINDBOOK_SEARCH, "top")
            homePresenter.uploadRankingSelectedLog()
        }

        ll_bottom_tab_category.setOnClickListener {
            AppLog.e(TAG, "Classify Selected")
            selectTab(3)
            preferencesUtils.putString(Constants.FINDBOOK_SEARCH, "class")
            homePresenter.uploadCategorySelectedLog()
        }

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
            homePresenter.uploadEditorSelectAllLog(isAllSelected)
        }

        setMenuTitleMargin()

        setNightMode(false)

        bt_night_shift.setOnCheckedChangeListener { _, isChecked ->
            homePresenter.uploadModeChangeLog()
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
            homePresenter.uploadAutoCacheLog(isChecked)
        }

        txt_push_setting.setOnClickListener {
            homePresenter.uploadPushSettingClickLog()
            startActivity(Intent(this, SettingMoreActivity::class.java))
        }

        txt_feedback.setOnClickListener {
            homePresenter.uploadFeedbackClickLog()
            Observable.timer(500, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        FeedbackAPI.openFeedbackActivity()
                    }
        }

        txt_mark.setOnClickListener {
            homePresenter.uploadMarkClickLog()
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
            homePresenter.uploadDisclaimerClickLog()
            startActivity(Intent(this, DisclaimerActivity::class.java))
        }

        val versionName = "V${AppUtils.getVersionName()}"


        txt_version_name.text = versionName

        rl_check_update.setOnClickListener {
            homePresenter.uploadCheckUpdateLog()
            try {
                apkUpdateUtils.getApkUpdateInfo(this, null, "SettingActivity")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        rl_clear_cache.setOnClickListener {
            homePresenter.uploadClearCacheClickLog()

            if (!this.isFinishing) {
                clearCacheDialog.show()
            }
        }

        txt_clear_cache_message.text = "0B"
    }

    /***
     * 当ViewPager改变时，更新界面信息
     * **/
    private fun onChangeNavigation(position: Int) {
        currentTab = position

        bottomType = position + 1

        if (currentTab != 0) {
            bookShelfFragment?.bookShelfRemoveHelper?.dismissRemoveMenu()
        }

        ll_bottom_tab_bookshelf.isSelected = position == 0
        ll_bottom_tab_recommend.isSelected = position == 1
        ll_bottom_tab_ranking.isSelected = position == 2
        ll_bottom_tab_category.isSelected = position == 3

        when (position) {
            0 -> {
                rl_recommend_head.visibility = View.GONE
                rl_head_ranking.visibility = View.GONE
            }
            1 -> {
                rl_recommend_head.visibility = View.VISIBLE
                rl_head_ranking.visibility = View.GONE
            }
            2 -> {
                rl_recommend_head.visibility = View.GONE
                rl_head_ranking.visibility = View.VISIBLE
                homePresenter.uploadRankingEntryLog()
            }
            else -> {
                rl_recommend_head.visibility = View.GONE
                rl_head_ranking.visibility = View.GONE
                homePresenter.uploadCategoryEntryLog()
            }
        }
    }

    /***
     * 初始化数据，移到Presenter中
     * **/
    private fun initData() {
        mLoadDataManager = LoadDataManager(this)
    }

    /***
     * 初始化ViewPager的位置：Position
     * **/
    private fun initPosition() {
        val intent = intent
        val position: Int
        if (intent != null) {
            if (intent.hasExtra(EventBookStore.BOOKSTORE)) {
                position = intent.getIntExtra(EventBookStore.BOOKSTORE, 0)
                selectTab(position)
            } else {
                val intExtra = intent.getIntExtra(EventBookStore.BOOKSTORE, EventBookStore.TYPE_ERROR)
                if (intExtra != EventBookStore.TYPE_ERROR) {
                    if (!isFinishing) {
                        selectTab(intExtra)
                    }
                }
            }
        }
    }

    /***
     * 检查请求地址是否为测试地址
     * **/
    private fun checkUrlDevelop() {
        if (UrlUtils.getBookNovelDeployHost().contains("test") || UrlUtils.getBookWebviewHost().contains("test")) {
            ToastUtils.showToastNoRepeat("请注意！！请求的是测试地址！！！")
        }
    }













    var frameHelper: FrameBookHelper? = null

    private lateinit var filter: IntentFilter

    private var removeMenuHelper: BookShelfRemoveHelper? = null
    private var bookView: BookShelfFragment? = null
    private var isClosed = false
    private var receiver: MyReceiver? = null
    private var mLoadDataManager: LoadDataManager? = null
    private val shake = AntiShake()

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
        if (!isEvent) homePresenter.uploadCurModeLog(isNightMode)
        if (isNightMode) {
            tv_night_shift.setText(R.string.mode_day)
            bt_night_shift.isChecked = true
        } else {
            tv_night_shift.setText(R.string.mode_night)
            bt_night_shift.isChecked = false
        }
    }


    fun selectTab(position: Int) {
        if (currentTab != position) {
            view_pager.setCurrentItem(position, false)
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
















    /***
     * 获取广播数据
     * **/
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




    /***
     * 上传安装应用列表，后期优化，完全移入到Presenter中
     * **/
    override fun updateAppList() {
        GetAppList().execute()
    }

    /***
     * 获取用户app列表，后期优化，完全移入到Presenter中
     * **/
    internal inner class GetAppList : AsyncTask<Void, Int, String>() {
        override fun doInBackground(vararg params: Void): String {
            return AppUtils.scanLocalInstallAppList(packageManager)
        }

        override fun onPostExecute(s: String) {
            StartLogClickUtil.upLoadApps(this@HomeActivity, s)
        }
    }

    /***
     * 移出，不在使用内部类的方式
     * **/
    private inner class MainAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getCount(): Int = 4

        override fun getItem(position: Int): Fragment? {
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

}