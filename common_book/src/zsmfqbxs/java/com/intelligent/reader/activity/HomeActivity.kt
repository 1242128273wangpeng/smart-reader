package com.intelligent.reader.activity

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.net.Uri
import android.os.Build
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
import android.widget.LinearLayout
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.sdk.android.feedback.impl.FeedbackAPI
import com.baidu.mobstat.StatService
import com.bumptech.glide.Glide
import com.dingyue.bookshelf.BookShelfFragment
import com.dingyue.bookshelf.BookShelfInterface
import com.dingyue.contract.CommonContract
import com.dingyue.contract.logger.HomeLogger
import com.dingyue.contract.logger.PersonalLogger
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dingyue.contract.util.SharedPreUtil
import com.dingyue.contract.util.showToastMessage
import com.dy.reader.event.EventSetting
import com.dy.reader.setting.ReaderSettings
import com.intelligent.reader.R
import com.intelligent.reader.app.BookApplication
import com.intelligent.reader.fragment.WebViewFragment
import com.intelligent.reader.presenter.home.HomePresenter
import com.intelligent.reader.presenter.home.HomeView
import com.intelligent.reader.util.EventBookStore
import de.greenrobot.event.EventBus
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import iyouqu.theme.BaseCacheableActivity
import iyouqu.theme.ThemeMode
import kotlinx.android.synthetic.zsmfqbxs.act_home.*
import net.lzbook.kit.app.ActionConstants
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.appender_loghub.appender.AndroidLogStorage
import net.lzbook.kit.book.component.service.CheckNovelUpdateService
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.cache.DataCleanManager
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.ReadConfig
import net.lzbook.kit.encrypt.URLBuilderIntterface
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.utils.*
import net.lzbook.kit.utils.download.DownloadAPKService
import net.lzbook.kit.utils.update.ApkUpdateUtils
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

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

    private lateinit var sharedPreUtil: SharedPreUtil

    private lateinit var apkUpdateUtils: ApkUpdateUtils

    private var bookShelfFragment: BookShelfFragment? = null

    private var recommendFragment: WebViewFragment? = null
    private var rankingFragment: WebViewFragment? = null
    private var categoryFragment: WebViewFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.act_home)

        versionCode = AppUtils.getVersionCode()
        sharedPreUtil = SharedPreUtil(SharedPreUtil.SHARE_DEFAULT)

        initView()
        initGuide()

        homePresenter.initParameters()

        registerHomeReceiver()

        checkAppUpdate()

        initPosition()

        checkUrlDevelop()

        AndroidLogStorage.getInstance().clear()


        homePresenter.initDownloadService()

        HomeLogger.uploadHomeBookListInformation()
    }

    override fun onResume() {
        super.onResume()

        this.changeHomePagerIndex(currentIndex)

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

    override fun onDestroy() {
        super.onDestroy()

        AndroidLogStorage.getInstance().clear()

        this.unregisterReceiver(homeBroadcastReceiver)

        try {
            bookShelfFragment = null
            recommendFragment = null
            rankingFragment = null
            categoryFragment = null
            homeAdapter = null
            Glide.get(this).clearMemory()
            setContentView(R.layout.common_empty)
        } catch (exception: Resources.NotFoundException) {
            exception.printStackTrace()
        }
        AppUtils.fixInputMethodManagerLeak(applicationContext)
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

        view_pager.isScrollable = false

        homeAdapter = HomeAdapter(supportFragmentManager)

        view_pager.adapter = homeAdapter

        this.changeHomePagerIndex(currentIndex)

        onChangeNavigation(currentIndex)

        ll_bottom_tab_bookshelf.setOnClickListener {
            this.changeHomePagerIndex(0)
            HomeLogger.uploadHomeBookShelfSelected()
        }

        ll_bottom_tab_recommend.setOnClickListener {
            this.changeHomePagerIndex(1)
            recommendFragment?.setTitle("推荐",2);
            sharedPreUtil.putString(SharedPreUtil.HOME_FINDBOOK_SEARCH, "recommend")
            HomeLogger.uploadHomeRecommendSelected()
        }

        ll_bottom_tab_ranking.setOnClickListener {
            this.changeHomePagerIndex(2)
            sharedPreUtil.putString(SharedPreUtil.HOME_FINDBOOK_SEARCH, "top")
            rankingFragment?.setTitle("榜单",3);
            HomeLogger.uploadHomeRankSelected()
        }

        ll_bottom_tab_category.setOnClickListener {
            this.changeHomePagerIndex(3)
            sharedPreUtil.putString(SharedPreUtil.HOME_FINDBOOK_SEARCH, "class")
            categoryFragment?.setTitle("分类",4);
            HomeLogger.uploadHomeCategorySelected()
        }

        setMenuTitleMargin()



    }

    private fun initGuide() {
        val key = SharedPreUtil.BOOKSHELF_GUIDE_TAG
        if (!sharedPreUtil.getBoolean(key)) {
            fl_guide_layout.visibility = View.VISIBLE
            img_guide_remove.visibility = View.VISIBLE
            fl_guide_layout.setOnClickListener {
                if (guideDownload) {
                    img_guide_download.visibility = View.VISIBLE
                    img_guide_remove.visibility = View.GONE
                    guideDownload = false
                } else {
                    sharedPreUtil.putBoolean(key, true)
                    img_guide_download.visibility = View.GONE
                    fl_guide_layout.visibility = View.GONE
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

        ll_bottom_tab_bookshelf.isSelected = position == 0
        ll_bottom_tab_recommend.isSelected = position == 1
        ll_bottom_tab_ranking.isSelected = position == 2
        ll_bottom_tab_category.isSelected = position == 3
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
        if (UrlUtils.getBookNovelDeployHost().contains("test") || UrlUtils.getBookWebviewHost().contains("test")) {
            this.showToastMessage("请注意！！请求的是测试地址！！！", 0L)
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
            this.showToastMessage(R.string.mian_click_tiwce_exit)
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
                if (CommonContract.isDoubleClick(System.currentTimeMillis())) {
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
            if (CommonContract.isDoubleClick(System.currentTimeMillis())) {
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
            if (CommonContract.isDoubleClick(System.currentTimeMillis())) {
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
                if (CommonContract.isDoubleClick(System.currentTimeMillis())) {
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
                if (CommonContract.isDoubleClick(System.currentTimeMillis())) {
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

    override fun startLoad(webView: WebView, url: String): String {
        return url
    }

    override fun supportSlideBack(): Boolean {
        return false
    }

    /***
     * 设置抽屉布局中标题的Margin
     * **/
    private fun setMenuTitleMargin() {
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
                1 -> {
                    if (recommendFragment == null) {
                        recommendFragment = WebViewFragment()
                        val bundle = Bundle()
                        bundle.putString("type", "recommend")
                        val uri = URLBuilderIntterface.WEB_RECOMMEND.replace("{packageName}", AppUtils.getPackageName())
                        bundle.putString("url", UrlUtils.buildWebUrl(uri, HashMap()))
                        recommendFragment!!.setArguments(bundle)
                    }
                    recommendFragment
                }
                2 -> {
                    if (rankingFragment == null) {
                        rankingFragment = WebViewFragment()
                        val bundle = Bundle()
                        bundle.putString("type", "rank")
                        val uri = URLBuilderIntterface.WEB_RANK.replace("{packageName}", AppUtils.getPackageName())
                        bundle.putString("url", UrlUtils.buildWebUrl(uri, HashMap()))
                        rankingFragment!!.arguments = bundle
                    }
                    rankingFragment
                }
                3 -> {
                    if (categoryFragment == null) {
                        categoryFragment = WebViewFragment()
                        val bundle = Bundle()
                        bundle.putString("type", "category")
                        val uri = URLBuilderIntterface.WEB_CATEGORY.replace("{packageName}", AppUtils.getPackageName())
                        bundle.putString("url", UrlUtils.buildWebUrl(uri, HashMap()))
                        categoryFragment!!.arguments = bundle
                    }
                    categoryFragment
                }
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
            content_tab_selection.visibility = View.GONE
            AnimationHelper.smoothScrollTo(view_pager, 0)
        } else {
            content_tab_selection.visibility = View.VISIBLE
            AnimationHelper.smoothScrollTo(view_pager, 0)
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