package com.intelligent.reader.activity

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.content.res.TypedArray
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import android.text.TextUtils
import android.view.ViewGroup
import android.webkit.WebView
import com.alibaba.android.arouter.facade.annotation.Route
import com.baidu.mobstat.StatService
import com.ding.basic.Config
import com.ding.basic.request.RequestService
import com.dingyue.bookshelf.BookShelfFragment
import com.dingyue.bookshelf.BookShelfInterface
import com.intelligent.reader.R
import com.intelligent.reader.app.BookApplication
import com.intelligent.reader.fragment.CategoryFragment
import com.intelligent.reader.fragment.RankingFragment
import com.intelligent.reader.fragment.RecommendFragment
import com.intelligent.reader.fragment.WebViewFragment
import kotlinx.android.synthetic.qbmfkkydq.act_home.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.appender_loghub.appender.AndroidLogStorage
import net.lzbook.kit.bean.EventBookStore
import net.lzbook.kit.constants.ActionConstants
import net.lzbook.kit.presenter.home.HomePresenter
import net.lzbook.kit.presenter.home.HomeView
import net.lzbook.kit.service.CheckNovelUpdateService
import net.lzbook.kit.service.DownloadAPKService
import net.lzbook.kit.ui.activity.DownloadErrorActivity
import net.lzbook.kit.ui.activity.WelfareCenterActivity
import net.lzbook.kit.ui.activity.base.BaseCacheableActivity
import net.lzbook.kit.utils.ApkUpdateUtils
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.encrypt.MD5Utils
import net.lzbook.kit.utils.logger.AppLog
import net.lzbook.kit.utils.logger.HomeLogger
import net.lzbook.kit.utils.logger.PersonalLogger
import net.lzbook.kit.utils.oneclick.OneClickUtil
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.sp.SPKey
import net.lzbook.kit.utils.sp.SPUtils
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.utils.webview.JSInterfaceHelper
import net.lzbook.kit.utils.webview.UrlUtils
import java.io.File
import java.util.*

@Route(path = RouterConfig.HOME_ACTIVITY)
class HomeActivity : BaseCacheableActivity(), CheckNovelUpdateService.OnBookUpdateListener, BookShelfInterface, HomeView, WebViewFragment.FragmentCallback {

    override fun checkShowShelfGuide() {
        if (currentIndex == 0) {

        }

    }

    override fun webJsCallback(jsInterfaceHelper: JSInterfaceHelper) {
        jsInterfaceHelper.setOnEnterAppClick { AppLog.e(TAG, "doEnterApp") }
        jsInterfaceHelper.setOnSearchClick { keyWord, search_type, filter_type, filter_word, sort_type ->
            try {
                if (OneClickUtil.isDoubleClick(System.currentTimeMillis())) {
                    return@setOnSearchClick
                }
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
                val title = if (TextUtils.isEmpty(name)) {
                    AppUtils.getPackageName()
                } else {
                    name
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
    }


    override fun startLoad(webView: WebView?, url: String): String {
        return url
    }

    override fun receiveUpdateCallBack(notification: Notification) {
        val intent = Intent(this, HomeActivity::class.java)
        val pending = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        notification.contentIntent = pending

    }

    override fun changeHomeNavigationState(state: Boolean) {

    }

    override fun changeHomePagerIndex(index: Int) {
        if (currentIndex != index) {
            view_pager?.setCurrentItem(index, false)
        }
    }


    private var homeAdapter: HomeAdapter? = null
    private var currentIndex = 0
    private val homePresenter by lazy { HomePresenter(this, this.packageManager) }
    private val fragmentTypeBookShelf = 0 //书架
    private val fragmentTypeRecommend = 1 //推荐
    private val fragmentTypeRanking = 2 //分类
    private val fragmentTypeClassify = 3 //榜单
    private lateinit var apkUpdateUtils: ApkUpdateUtils
    private var bookShelfFragment: BookShelfFragment? = null
    private val recommendFragment: RecommendFragment by lazy {
        val fragment = RecommendFragment()
        val bundle = Bundle()
        bundle.putString("type", "recommend")
        val uri = RequestService.WEB_RECOMMEND_V3.replace("{packageName}", AppUtils.getPackageName())
        bundle.putString("url", UrlUtils.buildWebUrl(uri, HashMap()))
        fragment.arguments = bundle
        fragment
    }

    private val rankingFragment: RankingFragment by lazy {
        val fragment = RankingFragment()
        val bundle = Bundle()
        bundle.putString("type", "rank")
        val uri = RequestService.WEB_RANK_V3.replace("{packageName}", AppUtils.getPackageName())
        bundle.putString("url", UrlUtils.buildWebUrl(uri, HashMap()))
        fragment.arguments = bundle
        fragment
    }

    private val classifyFragment: CategoryFragment by lazy {
        val fragment = CategoryFragment()
        val bundle = Bundle()
        bundle.putString("type", "rank")
        val uri = RequestService.WEB_RANK_V3.replace("{packageName}", AppUtils.getPackageName())
        bundle.putString("url", UrlUtils.buildWebUrl(uri, HashMap()))
        fragment.arguments = bundle
        fragment
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_home)
        initView()
        homePresenter.initParameters()
        homePresenter.initDownloadService()
        registerHomeReceiver()
        checkAppUpdate()
        checkUrlDevelop()
        AndroidLogStorage.getInstance().clear()
        HomeLogger.uploadHomeBookListInformation()

    }

    override fun onResume() {
        super.onResume()
        this.setNightMode(false)

        StatService.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        StatService.onPause(this)
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


    override fun supportSlideBack(): Boolean {
        return false
    }

    private val mTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) {
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }

        override fun onTabSelected(tab: TabLayout.Tab) {
            currentIndex = tab.position
            when (tab.position) {
                fragmentTypeBookShelf -> {
                    HomeLogger.uploadHomeBookShelfSelected()
                }
                fragmentTypeRecommend -> {
                    SPUtils.putDefaultSharedString(SPKey.HOME_FINDBOOK_SEARCH, "recommend")
                    HomeLogger.uploadHomeRecommendSelected()
                }
                fragmentTypeClassify -> {
                    SPUtils.putDefaultSharedString(SPKey.HOME_FINDBOOK_SEARCH, "class")
                    HomeLogger.uploadHomeCategorySelected()
                }
                fragmentTypeRanking -> {
                    SPUtils.putDefaultSharedString(SPKey.HOME_FINDBOOK_SEARCH, "top")
                    HomeLogger.uploadHomeRankSelected()
                }
            }
        }

    }

    private fun initView() {

        view_pager?.offscreenPageLimit = 4
        view_pager?.isScrollable = false

        homeAdapter = HomeAdapter(supportFragmentManager)

        view_pager?.adapter = homeAdapter

        tabs_nav.setupWithViewPager(view_pager)
        tabs_nav.removeAllTabs()
        val titles = resources.getStringArray(R.array.nav_bottom_text) as Array<String>
        val icons = resources.obtainTypedArray(R.array.nav_bottom_icon) as TypedArray

        for (i in 0 until titles.size) {
            tabs_nav.addTab(tabs_nav.newTab()
                    .setCustomView(R.layout.nav_bottom_item_view)
                    .setText(titles[i])
                    .setIcon(icons.getResourceId(i, 0)))
        }
        icons.recycle()
        tabs_nav.addOnTabSelectedListener(mTabSelectedListener)


    }

    private var homeBroadcastReceiver: HomeBroadcastReceiver? = null
    private lateinit var intentFilter: IntentFilter
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
     * 检查请求地址是否为测试地址
     * **/
    private fun checkUrlDevelop() {
        if (Config.loadRequestAPIHost().contains("test") || Config.loadWebViewHost().contains("test")) {
            ToastUtil.showToastMessage("请注意！！请求的是测试地址！！！", 0L)
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

    var firstTime: Long = 0

    override fun onBackPressed() {
        when {
            view_pager.currentItem != 0 -> changeHomePagerIndex(0)
            bookShelfFragment?.isRemoveMenuShow() == true -> bookShelfFragment?.dismissRemoveMenu()
            else -> doubleClickFinish()
        }

    }

    /***
     * 两次返回键退出应用
     */
    private fun doubleClickFinish() {
        if (System.currentTimeMillis() - firstTime > 2000) {
            ToastUtil.showToastMessage(R.string.mian_click_tiwce_exit)
            firstTime = System.currentTimeMillis()
        } else {
            super.onBackPressed()
        }
    }


    /***
     * 是否夜间模式
     * **/
    private fun setNightMode(isEvent: Boolean) {
        val isNightMode = this.mThemeHelper.isNight
        if (!isEvent) PersonalLogger.uploadPersonalCurrentMode(isNightMode)

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


    /**
     * HomeActivity子页面的Adapter
     */
    inner class HomeAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

        override fun getCount(): Int = 4

        override fun getItem(position: Int): Fragment? {
            return when (position) {
                fragmentTypeBookShelf -> {
                    if (bookShelfFragment == null) {
                        bookShelfFragment = BookShelfFragment()
                    }
                    bookShelfFragment
                }
                fragmentTypeRecommend -> recommendFragment
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


}