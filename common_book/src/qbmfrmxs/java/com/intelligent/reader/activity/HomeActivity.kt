package com.intelligent.reader.activity

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.sdk.android.feedback.impl.FeedbackAPI
import com.baidu.mobstat.StatService
import com.ding.basic.config.ParameterConfig
import com.ding.basic.net.Config
import com.ding.basic.net.api.service.RequestService
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.dingyue.bookshelf.BookShelfFragment
import com.dingyue.bookshelf.BookShelfInterface
import com.dingyue.searchbook.fragment.HotWordFragment
import com.dy.reader.setting.ReaderSettings
import com.intelligent.reader.R
import com.intelligent.reader.fragment.ClassifyFragment
import com.intelligent.reader.fragment.WebViewFragment
import com.intelligent.reader.widget.ClearCacheDialog
import com.intelligent.reader.widget.drawer.DrawerLayout
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.qbmfrmxs.act_home.*
import kotlinx.android.synthetic.qbmfrmxs.home_drawer_layout_main.*
import kotlinx.android.synthetic.qbmfrmxs.home_drawer_layout_menu.*
import net.lzbook.kit.appender_loghub.appender.AndroidLogStorage
import net.lzbook.kit.bean.EventBookStore
import net.lzbook.kit.constants.ActionConstants
import net.lzbook.kit.presenter.HomePresenter
import net.lzbook.kit.service.CheckNovelUpdateService
import net.lzbook.kit.ui.activity.DownloadErrorActivity
import net.lzbook.kit.ui.activity.base.BaseCacheableActivity
import net.lzbook.kit.utils.*
import net.lzbook.kit.utils.cache.DataCleanManager
import net.lzbook.kit.utils.cache.UIHelper
import net.lzbook.kit.utils.download.CacheManager
import net.lzbook.kit.utils.encrypt.MD5Utils
import net.lzbook.kit.utils.logger.HomeLogger
import net.lzbook.kit.utils.logger.PersonalLogger
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.theme.ThemeMode
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.utils.webview.UrlUtils
import net.lzbook.kit.view.HomeView
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

@Route(path = RouterConfig.HOME_ACTIVITY)
class HomeActivity : BaseCacheableActivity(), CheckNovelUpdateService.OnBookUpdateListener, HomeView, BookShelfInterface {
    private val fragmentTypeBookShelf = 0 //书架
    private val fragmentTypeRecommend = 1 //精选
    private val fragmentTypeSearchBook = 2 //搜索
    private val fragmentTypeClassify = 3 //分类
    private val fragmentTypeRanking = 4 //榜单


    private val homePresenter by lazy { HomePresenter(this, this.packageManager) }
    private var homeAdapter: HomeAdapter? = null


    private var homeBroadcastReceiver: HomeBroadcastReceiver? = null

    private lateinit var intentFilter: IntentFilter


    private var closed = false

    private var currentIndex = 0
    private var versionCode: Int = 0

    private var guideDownload: Boolean = true
    private lateinit var apkUpdateUtils: ApkUpdateUtils

    private var bookShelfFragment: BookShelfFragment? = null

    private val recommendFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()
        val bundle = Bundle()
        bundle.putString("type", "recommend")
        val uri = RequestService.WEB_RECOMMEND_H5.replace("{packageName}", AppUtils.getPackageName())
//        val url = if (NetWorkUtils.isNetworkAvailable(this))
//            UrlUtils.buildWebUrl(uri, HashMap()) else
//            SPUtils.getDefaultSharedString(SPKey.HOME_RECOMMEND_URL)
//        SPUtils.putDefaultSharedString(SPKey.HOME_RECOMMEND_URL, url)
//        bundle.putString("url", url)
//        AppLog.e("DingYue-当前的url：" + url)
//        AppLog.e("DingYue-服务端url：" + UrlUtils.buildWebUrl(uri, HashMap()))
//        AppLog.e("DingYue-存储的url：" + SPUtils.getDefaultSharedString(SPKey.HOME_RECOMMEND_URL))
        bundle.putString("url", UrlUtils.buildWebUrl(uri, HashMap()))
        fragment.arguments = bundle
        fragment
    }

    private val rankingFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()
        val bundle = Bundle()
        bundle.putString("type", "rank")
        val uri = RequestService.WEB_RANK_H5.replace("{packageName}", AppUtils.getPackageName())
        bundle.putString("url", UrlUtils.buildWebUrl(uri, HashMap()))
        fragment.arguments = bundle
        fragment
    }

    private val classifyFragment: ClassifyFragment by lazy {
        val fragment = ClassifyFragment()
        fragment
    }

    private val searchBookFragment: HotWordFragment by lazy {
        val fragment = HotWordFragment()
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
                    txt_clear_cache_message.text = applicationContext.getString(R.string.application_cache_size)
                }
            }
        }
        dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.act_home)

        versionCode = AppUtils.getVersionCode()

        initView()

        initListener()

        checkShowShelfGuide()

        homePresenter.initParameters()

        registerHomeReceiver()

        checkAppUpdate()

        initPosition()

        checkUrlDevelop()

        AndroidLogStorage.getInstance().clear()

        showCacheMessage()

        homePresenter.initDownloadService()

        HomeLogger.uploadHomeBookListInformation()
    }

    override fun onResume() {
        super.onResume()

        this.changeHomePagerIndex(currentIndex)
        this.setNightMode(false)

        StatService.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        StatService.onPause(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val position: Int

        if (intent != null && intent.hasExtra("position")) {
            position = intent.getIntExtra("position", 0)
            view_pager?.currentItem = position
        } else {
            if (intent != null) {
                val intExtra = intent.getIntExtra(EventBookStore.BOOKSTORE, EventBookStore.TYPE_ERROR)
                if (intExtra != EventBookStore.TYPE_ERROR) {
                    if (!isFinishing) {
                        this.changeHomePagerIndex(intExtra)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        AndroidLogStorage.getInstance().clear()

        this.unregisterReceiver(homeBroadcastReceiver)

        try {
            setContentView(R.layout.common_empty)
        } catch (exception: Resources.NotFoundException) {
            exception.printStackTrace()
        }
    }

    override fun onBackPressed() {
        when {
            dl_home_content.isOpened -> dl_home_content.closeMenu()
            view_pager?.currentItem != 0 -> changeHomePagerIndex(0)
            bookShelfFragment?.isRemoveMenuShow() == true -> bookShelfFragment?.dismissRemoveMenu()
            else -> doubleClickFinish()
        }
    }

    private fun initView() {

        view_pager?.offscreenPageLimit = 4
        view_pager?.isScrollable = false

        homeAdapter = HomeAdapter(supportFragmentManager)

        view_pager?.adapter = homeAdapter

        this.changeHomePagerIndex(currentIndex)

        onChangeNavigation(currentIndex)

        setNightMode(false)

        bt_night_shift.setOnCheckedChangeListener { _, isChecked ->
            PersonalLogger.uploadPersonalNightModeChange()
            ReaderSettings.instance.initValues()
            if (isChecked) {
                tv_night_shift.setText(R.string.mode_day)
                ReaderSettings.instance.readLightThemeMode = ReaderSettings.instance.readThemeMode
                ReaderSettings.instance.readThemeMode = 61
                mThemeHelper.setMode(ThemeMode.NIGHT)
            } else {
                tv_night_shift.setText(R.string.mode_night)
                ReaderSettings.instance.readThemeMode = ReaderSettings.instance.readLightThemeMode
                mThemeHelper.setMode(ThemeMode.THEME1)
            }
            ReaderSettings.instance.save()
            nightShift(isChecked, true)
        }

        val isAutoDownload = SPUtils.getDefaultSharedBoolean(SPKey.AUTO_UPDATE_CAHCE, true)

        btn_auto_download.isChecked = isAutoDownload

        btn_auto_download.setOnCheckedChangeListener { _, isChecked ->
            SPUtils.putDefaultSharedBoolean(SPKey.AUTO_UPDATE_CAHCE, isChecked)
            PersonalLogger.uploadPersonalAutoCache(isChecked)
        }

        txt_push_setting.setOnClickListener {
            PersonalLogger.uploadPersonalPushSetting()
            startActivity(Intent(this, SettingMoreActivity::class.java))
        }

        txt_feedback.setOnClickListener {
            PersonalLogger.uploadPersonalFeedback()
            Observable.timer(500, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        FeedbackAPI.openFeedbackActivity()
                    }
        }

        txt_mark.setOnClickListener {
            PersonalLogger.uploadPersonalMark()
            try {
                val uri = Uri.parse("market://details?id=" + this.packageName)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (e: Exception) {
                ToastUtil.showToastMessage(R.string.menu_no_market)
            }

        }

        txt_disclaimer_statement.setOnClickListener {
            PersonalLogger.uploadPersonalDisclaimer()
            val bundle = Bundle()
            bundle.putBoolean(RouterUtil.FROM_DISCLAIMER_PAGE, true)
            RouterUtil.navigation(this, RouterConfig.DISCLAIMER_ACTIVITY, bundle)
        }

        val versionName = "V${AppUtils.getVersionName()}"


        txt_version_name.text = versionName

        rl_check_update.setOnClickListener {
            PersonalLogger.uploadPersonalCheckUpdate()
            try {
                apkUpdateUtils.getApkUpdateInfo(this, handler, "SettingActivity")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        rl_clear_cache.setOnClickListener {
            PersonalLogger.uploadPersonalClearCache()

            if (!this.isFinishing) {
                clearCacheDialog.show()
            }
        }

        txt_clear_cache_message.text = applicationContext.getString(R.string.application_cache_size)
    }


    private fun initListener() {
        // 书架
        ll_tab_bookshelf.setOnClickListener {
            this.changeHomePagerIndex(fragmentTypeBookShelf)
            HomeLogger.uploadHomeBookShelfSelected()
        }

        // 精选
        ll_tab_recommend.setOnClickListener {
            this.changeHomePagerIndex(fragmentTypeRecommend)
            recommendFragment.setTitle(resources.getString(R.string.recommend))
            SPUtils.putDefaultSharedString(SPKey.HOME_FINDBOOK_SEARCH, "recommend")
            HomeLogger.uploadHomeRecommendSelected()
        }

        // 搜索
        ll_tab_search.setOnClickListener {
            this.changeHomePagerIndex(fragmentTypeSearchBook)
            SPUtils.putDefaultSharedString(SPKey.HOME_FINDBOOK_SEARCH, "search")
            HomeLogger.uploadHomeSearch(1)
        }

        // 分类
        ll_tab_classify.setOnClickListener {
            this.changeHomePagerIndex(fragmentTypeClassify)
            SPUtils.putDefaultSharedString(SPKey.HOME_FINDBOOK_SEARCH, "class")
            HomeLogger.uploadHomeCategorySelected()
        }

        // 榜单
        ll_tab_ranking.setOnClickListener {
            this.changeHomePagerIndex(fragmentTypeRanking)
            rankingFragment.setTitle(resources.getString(R.string.ranking))
            SPUtils.putDefaultSharedString(SPKey.HOME_FINDBOOK_SEARCH, "top")
            HomeLogger.uploadHomeRankSelected()
        }

        dl_home_content.setOnMenuStateChangeListener { state ->
            when (state) {
                DrawerLayout.MenuState.MENU_OPENED -> {
                    showCacheMessage()
                    if (bookShelfFragment?.isRemoveMenuShow() == true) {
                        bookShelfFragment?.dismissRemoveMenu()
                    }
                }
                DrawerLayout.MenuState.MENU_START_SCROLL,
                DrawerLayout.MenuState.MENU_END_SCROLL -> {
                    ll_home_tab.requestLayout()
                }
            }
        }

        view_pager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                onChangeNavigation(position)

            }
        })
    }

    private fun initGuide() {
        val key = SPKey.getBOOKSHELF_GUIDE_TAG()
        if (!SPUtils.getDefaultSharedBoolean(key)) {
            fl_guide_layout.visibility = View.VISIBLE
            img_guide_remove.visibility = View.VISIBLE
            dl_home_content.lock()
            fl_guide_layout.setOnClickListener {
                if (guideDownload) {
                    img_guide_download.visibility = View.VISIBLE
                    img_guide_remove.visibility = View.GONE
                    guideDownload = false
                } else {
                    SPUtils.putDefaultSharedBoolean(key, true)
                    img_guide_download.visibility = View.GONE
                    fl_guide_layout.visibility = View.GONE
                    dl_home_content.unlock()
                    changeHomePagerIndex(1)//首次展示精选页
                }
            }
        }
    }

    /***
     * 当ViewPager改变时，更新界面信息
     * **/
    private fun onChangeNavigation(position: Int) {
        currentIndex = position

        if (currentIndex != 0 && bookShelfFragment?.isRemoveMenuShow() == true) {
            bookShelfFragment?.dismissRemoveMenu()
        }

        ll_tab_bookshelf.isSelected = position == fragmentTypeBookShelf
        ll_tab_recommend.isSelected = position == fragmentTypeRecommend
        ll_tab_search.isSelected = position == fragmentTypeSearchBook
        ll_tab_classify.isSelected = position == fragmentTypeClassify
        ll_tab_ranking.isSelected = position == fragmentTypeRanking
    }

    /***
     * 注册广播接受器
     * **/
    private fun registerHomeReceiver() {
        homeBroadcastReceiver = HomeBroadcastReceiver()

        intentFilter = IntentFilter()
        intentFilter.addAction(ActionConstants.ACTION_ADD_DEFAULT_SHELF)
        intentFilter.addAction(ActionConstants.ACTION_CHECK_UPDATE_FINISH)
        intentFilter.addAction(ActionConstants.ACTION_DOWNLOAD_APP_SUCCESS)

        this.registerReceiver(homeBroadcastReceiver, intentFilter)
    }

    /***
     * 检查版本更新
     * **/
    private fun checkAppUpdate() {
        apkUpdateUtils = ApkUpdateUtils(this)
        try {
            apkUpdateUtils.getApkUpdateInfo(this, handler, "HomeActivity")
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
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
                this.changeHomePagerIndex(position)
            } else {
                val intExtra = intent.getIntExtra(EventBookStore.BOOKSTORE, EventBookStore.TYPE_ERROR)
                if (intExtra != EventBookStore.TYPE_ERROR) {
                    if (!isFinishing) {
                        this.changeHomePagerIndex(intExtra)
                    }
                }
            }
        }
    }

    /***
     * 检查请求地址是否为测试地址
     * **/
    private fun checkUrlDevelop() {
        if (Config.loadRequestAPIHost().contains("test") || Config.loadWebViewHost().contains("test")) {
            ToastUtil.showToastMessage("请注意！！请求的是测试地址！！！", 0L)
        }
    }

    /***
     * 获取缓存大小
     * **/
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

    override fun receiveUpdateCallBack(notification: Notification) {
        val intent = Intent(this, HomeActivity::class.java)
        val pending = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        notification.contentIntent = pending
    }

    /***
     * 打开安装包文件
     * **/
    fun setupApplication(filePath: String) {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.action = Intent.ACTION_VIEW
        val type = "application/vnd.android.package-archive"
        intent.setDataAndType(Uri.fromFile(File(filePath)), type)
        startActivity(intent)
    }

    /***
     * 两次返回键退出应用
     * **/
    private fun doubleClickFinish() {
        BACK_COUNT++

        if (BACK_COUNT == 1) {
            ToastUtil.showToastMessage(R.string.mian_click_tiwce_exit)
        } else if (BACK_COUNT > 1 && !closed) {
            closed = true
            restoreSystemDisplayState()
            ATManager.exitClient()
            super.onBackPressed()
        }

        val message = handler.obtainMessage(0)
        message.what = BACK
        handler.sendMessageDelayed(message, 2000)
    }

    override fun supportSlideBack(): Boolean {
        return false
    }

    /***
     * 是否夜间模式
     * **/
    private fun setNightMode(isEvent: Boolean) {
        val isNightMode = this.mThemeHelper.isNight
        if (!isEvent) PersonalLogger.uploadPersonalCurrentMode(isNightMode)
        if (isNightMode) {
            tv_night_shift.setText(R.string.mode_day)
            bt_night_shift.isChecked = true
        } else {
            tv_night_shift.setText(R.string.mode_night)
            bt_night_shift.isChecked = false
        }
    }

    /**
     * HomeActivity子页面的Adapter
     */
    inner class HomeAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

        override fun getCount(): Int = 5

        override fun getItem(position: Int): Fragment? {
            return when (position) {
                fragmentTypeBookShelf -> {
                    if (bookShelfFragment == null) {
                        bookShelfFragment = BookShelfFragment()
                    }
                    bookShelfFragment
                }
                fragmentTypeRecommend -> recommendFragment
                fragmentTypeSearchBook -> searchBookFragment
                fragmentTypeClassify -> classifyFragment
                fragmentTypeRanking -> rankingFragment
                else -> null
            }
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            return if (position == 0) {
                val bookShelfFragment = super.instantiateItem(container, position) as BookShelfFragment
                bookShelfFragment.doUpdateBook()
                bookShelfFragment
            } else {
                super.instantiateItem(container, position)
            }
        }

        override fun getItemPosition(`object`: Any): Int = PagerAdapter.POSITION_NONE
    }

    /***
     * 接收广播数据
     * **/
    inner class HomeBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ActionConstants.ACTION_ADD_DEFAULT_SHELF) {
                if (bookShelfFragment != null) {
                    bookShelfFragment?.updateUI()
                }
            } else if (intent.action == ActionConstants.ACTION_CHECK_UPDATE_FINISH) {
                if (bookShelfFragment != null) {
                    bookShelfFragment?.updateUI()
                }
            } else if (intent.action == ActionConstants.ACTION_DOWNLOAD_APP_SUCCESS) {
                val bundle = intent.extras

                if (bundle != null) {
                    val md5 = bundle.getString("md5")
                    val count = bundle.getInt("count")
                    val filePath = bundle.getString("filePath")
                    val downloadLink = bundle.getString("downloadLink")
                    val fileName = filePath!!.substring(filePath.lastIndexOf("/") + 1)

                    if (count == 100) {
                        if (MD5Utils.getFileMD5(File(filePath))!!.equals(md5!!, ignoreCase = true)) {
                            setupApplication(filePath)
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
            } else if (intent.action == ActionConstants.ACTION_CHECK_QING_STATE_SUCCESS) {
                if (bookShelfFragment != null) {
                    bookShelfFragment?.updateUI()
                }
            } else if (intent.action == ActionConstants.ACTION_CHANGE_NIGHT_MODE) {
                setNightMode(true)
            }
        }
    }

    /***
     * 更改底部导航栏状态
     * **/
    override fun changeHomeNavigationState(state: Boolean) {
        if (state) {
            ll_home_tab.visibility = View.GONE
            img_bottom_shadow.visibility = View.GONE
            AnimationHelper.smoothScrollTo(view_pager, 0)
        } else {
            img_bottom_shadow.visibility = View.VISIBLE
            ll_home_tab.visibility = View.VISIBLE
            AnimationHelper.smoothScrollTo(view_pager, 0)
        }
    }

    /***
     * 改变ViewPager Index
     * **/
    override fun changeHomePagerIndex(index: Int) {
        if (currentIndex != index) {
            view_pager?.setCurrentItem(index, false)
        }
    }

    /***
     * 改变DrawerLayout状态，当书架页点击设置时，显示抽屉布局
     * **/
    override fun changeDrawerLayoutState() {
        if (dl_home_content.isOpened) {
            dl_home_content.closeMenu()
        } else {
            dl_home_content.openMenu()
            HomeLogger.uploadHomePersonal()
        }
    }

    override fun lockDrawerLayout(isToLock: Boolean) {
        if (isToLock) {
            dl_home_content.lock()
        } else {
            dl_home_content.unlock()
        }
    }

    /***
     * 检查是否展示书架引导
     * **/
    override fun checkShowShelfGuide() {
        if (currentIndex == 0) {
            initGuide()
        }
    }

    companion object {
        private const val BACK = 0x80
        private var BACK_COUNT: Int = 0
        internal var handler = Handler(Handler.Callback { msg ->
            when (msg.what) {
                BACK -> BACK_COUNT = 0
            }
            true
        })
    }
}