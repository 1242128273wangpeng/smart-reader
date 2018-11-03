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
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.intelligent.reader.fragment.WebViewFragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.baidu.mobstat.StatService
import com.ding.basic.net.Config
import com.ding.basic.net.api.service.RequestService
import com.ding.basic.util.MD5Utils
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.dingyue.bookshelf.BookShelfFragment
import com.dingyue.bookshelf.BookShelfInterface
import com.dingyue.bookshelf.BookShelfLogger
import com.dingyue.statistics.DyStatService
import com.dingyue.statistics.log.AppLog
import com.dy.media.MediaLifecycle
import com.intelligent.reader.R
import com.intelligent.reader.app.BookApplication
import com.intelligent.reader.view.PushSettingDialog
import com.umeng.message.PushAgent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.txtqbdzs.act_home.*
import net.lzbook.kit.bean.EventBookStore
import net.lzbook.kit.bean.PagerDesc
import net.lzbook.kit.constants.ActionConstants
import net.lzbook.kit.pointpage.EventPoint
import net.lzbook.kit.presenter.HomePresenter
import net.lzbook.kit.service.CheckNovelUpdateService
import net.lzbook.kit.service.DownloadAPKService
import net.lzbook.kit.ui.activity.DownloadErrorActivity
import net.lzbook.kit.ui.activity.WelfareCenterActivity
import net.lzbook.kit.ui.activity.base.BaseCacheableActivity
import net.lzbook.kit.ui.widget.BannerDialog
import net.lzbook.kit.utils.*
import net.lzbook.kit.utils.logger.HomeLogger
import net.lzbook.kit.utils.oneclick.OneClickUtil
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.utils.webview.JSInterfaceHelper
import net.lzbook.kit.utils.webview.UrlUtils
import net.lzbook.kit.view.HomeView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File


@Route(path = RouterConfig.HOME_ACTIVITY)
class HomeActivity : BaseCacheableActivity(), WebViewFragment.FragmentCallback,
        CheckNovelUpdateService.OnBookUpdateListener, HomeView, BookShelfInterface {

    private val homePresenter by lazy { HomePresenter(this, this.packageManager) }

    private var homeBroadcastReceiver: HomeBroadcastReceiver? = null

    private lateinit var intentFilter: IntentFilter

    private var homeAdapter: HomeAdapter? = null

    private var closed = false

    private var currentIndex = 0
    private var versionCode: Int = 0

    private var guideDownload: Boolean = true


    private lateinit var apkUpdateUtils: ApkUpdateUtils

    private var bookShelfFragment: BookShelfFragment? = null

    private var currPageIndex = 1

    private val recommendFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()
        val bundle = Bundle()
        bundle.putString("type", "recommend")
        val uri = RequestService.WEB_RECOMMEND_H5.replace("{packageName}", AppUtils.getPackageName())
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

    private val categoryFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()
        val bundle = Bundle()
        bundle.putString("type", "category")
        val uri = RequestService.WEB_CATEGORY_H5.replace("{packageName}", AppUtils.getPackageName())
        bundle.putString("url", UrlUtils.buildWebUrl(uri, HashMap()))
        fragment.arguments = bundle
        fragment
    }


    private val pushSettingDialog: PushSettingDialog by lazy {
        val dialog = PushSettingDialog(this)
        dialog.openPushListener = {
            openPushSetting()
            DyStatService.onEvent(EventPoint.SHELF_CACHEMANAGE)
        }
        lifecycle.addObserver(dialog)
        dialog
    }

    private val bannerDialog: BannerDialog by lazy {
        BannerDialog(this, Intent(this, FindBookDetail::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.act_home)

        versionCode = AppUtils.getVersionCode()

        initView()
        initGuide()

        homePresenter.initParameters()

        registerHomeReceiver()

        checkAppUpdate()

        initPosition()

        checkUrlDevelop()

        DyStatService.clearInvalidData()


        homePresenter.initDownloadService()

        HomeLogger.uploadHomeBookListInformation()

        if (isShouldShowPushSettingDialog()) {
            pushSettingDialog.show()
        }

        EventBus.getDefault().register(this)
    }



    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val position: Int

        if (intent != null && intent.hasExtra("position")) {
            position = intent.getIntExtra("position", 0)
            view_pager!!.currentItem = position
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

    override fun onResume() {
        super.onResume()
        this.changeHomePagerIndex(currentIndex)
        StatService.onResume(this)
        MediaLifecycle.onResume()
    }

    override fun onPause() {
        super.onPause()
        StatService.onPause(this)
        MediaLifecycle.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()

        DyStatService.clearInvalidData()

        this.unregisterReceiver(homeBroadcastReceiver)
        MediaLifecycle.onDestroy()
        try {
            setContentView(R.layout.common_empty)
        } catch (exception: Resources.NotFoundException) {
            exception.printStackTrace()
        }
        AppUtils.fixInputMethodManagerLeak(applicationContext)
        EventBus.getDefault().unregister(this)
    }

    override fun onBackPressed() {
        when {
            view_pager?.currentItem != 0 -> changeHomePagerIndex(0)
            bookShelfFragment?.isRemoveMenuShow() == true -> bookShelfFragment?.dismissRemoveMenu()
            else -> doubleClickFinish()
        }
    }

    /***
     * 初始化View
     * **/
    private fun initView() {

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

        homeAdapter = HomeAdapter(supportFragmentManager)

        view_pager.adapter = homeAdapter

        this.changeHomePagerIndex(currentIndex)

        onChangeNavigation(currentIndex)

        tab_bookshelf.setOnClickListener {
            this.changeHomePagerIndex(0)
            HomeLogger.uploadHomeBookShelfSelected()
        }

        tab_recommend.setOnClickListener {
            this.changeHomePagerIndex(1)
            SPUtils.putDefaultSharedString(SPKey.HOME_FINDBOOK_SEARCH, "recommend")
            HomeLogger.uploadHomeRecommendSelected()
        }

        tab_ranking.setOnClickListener {
            this.changeHomePagerIndex(2)
            SPUtils.putDefaultSharedString(SPKey.HOME_FINDBOOK_SEARCH, "top")
            HomeLogger.uploadHomeRankSelected()
        }

        tab_category.setOnClickListener {
            this.changeHomePagerIndex(3)
            SPUtils.putDefaultSharedString(SPKey.HOME_FINDBOOK_SEARCH, "class")
            HomeLogger.uploadHomeCategorySelected()
        }

        content_head_user.setOnClickListener {
            HomeLogger.uploadHomePersonal()
            RouterUtil.navigation(this, RouterConfig.SETTING_ACTIVITY)
        }

        content_head_download.setOnClickListener {
            HomeLogger.uploadHomeCacheManager()
            RouterUtil.navigation(this, RouterConfig.DOWNLOAD_MANAGER_ACTIVITY)
        }

        bookshelf_search_view.setOnClickListener {
            HomeLogger.uploadHomeSearch(currPageIndex)
            RouterUtil.navigation(this, RouterConfig.SEARCH_BOOK_ACTIVITY)
        }

        home_edit_back.setOnClickListener {
            // 书架编辑页，返回按钮点位
            BookShelfLogger.uploadBookShelfEditBack()
            bookShelfFragment?.dismissRemoveMenu()
        }

        home_edit_cancel.setOnClickListener {
            // 书架编辑页，右上角取消点位
            BookShelfLogger.uploadBookShelfEditCancel()
            bookShelfFragment?.dismissRemoveMenu()
        }
    }

    private fun initGuide() {
        val key = SPKey.getBOOKSHELF_GUIDE_TAG()
        if (!SPUtils.getDefaultSharedBoolean(key)) {
            ll_guide_layout.visibility = View.VISIBLE
            iv_guide_remove.visibility = View.VISIBLE
            ll_guide_layout.setOnClickListener {
                if (guideDownload) {
                    iv_guide_download.visibility = View.VISIBLE
                    iv_guide_remove.visibility = View.GONE
                    guideDownload = false
                } else {
                    SPUtils.putDefaultSharedBoolean(key, true)
                    iv_guide_download.visibility = View.GONE
                    ll_guide_layout.visibility = View.GONE
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

        tab_bookshelf.isSelected = position == 0
        tab_recommend.isSelected = position == 1
        tab_ranking.isSelected = position == 2
        tab_category.isSelected = position == 3

        currPageIndex = position + 1
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

    override fun webJsCallback(jsInterfaceHelper: JSInterfaceHelper) {
        jsInterfaceHelper.setOnEnterAppClick { AppLog.e(TAG, "doEnterApp") }
        jsInterfaceHelper.setOnSearchClick { keyWord, search_type, filter_type, filter_word, sort_type ->
            try {
                DyStatService.onEvent(EventPoint.SYSTEM_SEARCHRESULT, mapOf("keyword" to keyWord, "type" to "0"))//0 代表从分类过来

                this.enterSearch(
                        keyWord, search_type, filter_type, filter_word, sort_type,
                        "fromClass")

                AppLog.e("kkk", "$search_type===")

            } catch (e: Exception) {
                AppLog.e(TAG, "Search failed")
                e.printStackTrace()
            }
        }
        jsInterfaceHelper.setOnAnotherWebClick(JSInterfaceHelper.onAnotherWebClick { url, name ->
            if (OneClickUtil.isDoubleClick(System.currentTimeMillis())) {
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
            if (OneClickUtil.isDoubleClick(System.currentTimeMillis())) {
                return@onEnterCover
            }

            if (!isFinishing) {
                val intent = Intent()
                intent.putExtra("author", author)
                intent.putExtra("book_id", book_id)
                intent.putExtra("book_source_id", book_source_id)
                intent.setClass(applicationContext, CoverPageActivity::class.java)
                startActivity(intent)
            }
        })

        //为webview 加载广告提供回调
        jsInterfaceHelper.setOnWebGameClick(JSInterfaceHelper.onWebGameClick { url, name ->
            try {
                if (OneClickUtil.isDoubleClick(System.currentTimeMillis())) {
                    return@onWebGameClick
                }
                var title = ""
                if (TextUtils.isEmpty(name)) {
                    title = AppUtils.getPackageName()
                } else {
                    title = name
                }
                val welfareIntent = Intent()
                welfareIntent.putExtra("url", url)
                welfareIntent.putExtra("title", title)
                welfareIntent.setClass(applicationContext, WelfareCenterActivity::class.java)
                startActivity(welfareIntent)
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        })

        jsInterfaceHelper.setOnGameAppClick(JSInterfaceHelper.onGameAppClick { url, name ->
            AppLog.e("福利中心", "下载游戏: $name : $url")

            try {
                if (OneClickUtil.isDoubleClick(System.currentTimeMillis())) {
                    return@onGameAppClick
                }
                val intent = Intent(BookApplication.getGlobalContext(), DownloadAPKService::class.java)
                intent.putExtra("url", url)
                intent.putExtra("name", name)
                startService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        jsInterfaceHelper.setOnEnterCategory { _, _, _, _ -> AppLog.e(TAG, "doCategory") }

        if (recommendFragment.isNeedInterceptSlide()) {

            jsInterfaceHelper.setOnH5PagerInfo (JSInterfaceHelper.OnH5PagerInfoListener { x, y, width, height ->
                    recommendFragment.mPagerDesc = PagerDesc(y, x, x + width, y + height)
            })
        }



    }

    override fun startLoad(webView: WebView, url: String): String {
        return url
    }

    override fun supportSlideBack(): Boolean {
        return false
    }

    /***
     * HomeActivity子页面的Adapter
     * **/
    inner class HomeAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

        override fun getCount(): Int = 4

        override fun getItem(position: Int): Fragment? {
            return when (position) {
                0 -> {
                    if (bookShelfFragment == null) {
                        bookShelfFragment = BookShelfFragment()
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
            if (intent.action == ActionConstants.ACTION_CHECK_UPDATE_FINISH) {
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
//                setNightMode(true)
            }
        }
    }

    /***
     * 更改底部导航栏状态
     * **/
    override fun changeHomeNavigationState(state: Boolean) {
        if (state) {
            bookshelf_search_view.visibility = View.GONE
            content_head_editor.visibility = View.VISIBLE
            AnimationHelper.smoothScrollTo(view_pager, 0)
            // 书架编辑 不允许滑动
            view_pager.isScrollable = false
        } else {
            content_head_editor.visibility = View.GONE
            bookshelf_search_view.visibility = View.VISIBLE
            AnimationHelper.smoothScrollTo(view_pager, 0)
            // 书架编辑还原,允许滑动
            view_pager.isScrollable = true
        }
    }

    override fun changeDrawerLayoutState() {
    }


    /***
     * 改变ViewPager Index
     * **/
    override fun changeHomePagerIndex(index: Int) {
        if (currentIndex != index) {
            view_pager.setCurrentItem(index, false)
        }
    }

    @Subscribe(sticky = true)
    fun onReceiveEvent(type: String) {
        if (type != EVENT_UPDATE_TAG) return

        val udid = OpenUDID.getOpenUDIDInContext(this)
        PushAgent.getInstance(this)
                .updateTags(this, udid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    loge("活动弹窗图片地址: $it")
                    bannerDialog.show(it)
                }, onError = {
                    it.printStackTrace()
                })
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