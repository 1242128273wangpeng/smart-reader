package com.intelligent.reader.activity

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.alibaba.android.arouter.facade.annotation.Route
import com.dingyue.bookshelf.BookShelfFragment
import com.dingyue.bookshelf.BookShelfInterface
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dingyue.contract.util.showToastMessage
import com.intelligent.reader.R
import com.intelligent.reader.fragment.BookStoreFragment
import com.intelligent.reader.fragment.WebViewFragment
import com.intelligent.reader.presenter.home.HomeView
import iyouqu.theme.BaseCacheableActivity
import kotlinx.android.synthetic.txtqbmfxs.act_home.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.appender_loghub.appender.AndroidLogStorage
import net.lzbook.kit.book.component.service.CheckNovelUpdateService
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.utils.*
import net.lzbook.kit.utils.oneclick.AntiShake
import net.lzbook.kit.utils.update.ApkUpdateUtils
import java.io.File
import java.util.*

/**
 * Function：书架、书城界面
 *
 * Created by JoannChen on 2018/6/16 0016 10:38
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
@Route(path = RouterConfig.HOME_ACTIVITY)
class HomeActivity : BaseCacheableActivity(), WebViewFragment.FragmentCallback,
        CheckNovelUpdateService.OnBookUpdateListener, HomeView, BookShelfInterface, View.OnClickListener {


    //    private var viewPager: NonSwipeViewPager? = null
    private var bookView: BookShelfFragment? = null
    private var isClosed = false
    private var apkUpdateUtils: ApkUpdateUtils? = null
    private var receiver: MyReceiver? = null
    private var mLoadDataManager: LoadDataManager? = null
    private val shake = AntiShake()
    private var bottomType: Int = 0//青果打点搜索 1 精选 2 榜单
    private var currentTab = 0

    private var bookStoreFragment: BookStoreFragment? = null
    private var bookShelfFragment: BookShelfFragment? = null

    private var sharedPreferences: SharedPreferences? = null
    private var homeAdapter: HomeAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* EventBus.getDefault().register(this)*/

        setContentView(R.layout.act_home)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)


        initData()
        initListener()
        initViewPager()

        homeAdapter = HomeAdapter(supportFragmentManager)
        view_pager.adapter = homeAdapter


        //注册广播接收器
        receiver = MyReceiver()
        /*  filter = IntentFilter()
          filter.addAction(ActionConstants.DOWN_APP_SUCCESS_ACTION)
          this@HomeActivity.registerReceiver(receiver, filter)*/

        apkUpdateUtils = ApkUpdateUtils(this)
        try {
            apkUpdateUtils!!.getApkUpdateInfo(this, handler, "HomeActivity")
        } catch (e: Exception) {
            e.printStackTrace()
        }
/*
        initPositon()
        checckUrlIsTest()*/

        AndroidLogStorage.getInstance().clear()

    }


    override fun onClick(v: View) {
        when (v.id) {
            bookshelf_search.id -> intentSearch()
            bookshelf_setting.id -> RouterUtil.navigation(this, RouterConfig.SETTING_ACTIVITY)
            ll_home_bookshelf.id -> {
                view_pager.currentItem = 0
                bottomType = 1
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.BOOKSHELF)

            }
            ll_home_bookstore.id -> {
                view_pager.currentItem = 1
            }

        }
    }

    private fun initListener() {
        bookshelf_search.setOnClickListener(this)
        bookshelf_setting.setOnClickListener(this)
        ll_home_bookshelf.setOnClickListener(this)
        ll_home_bookstore.setOnClickListener(this)
    }

    private fun initViewPager() {

        view_pager.offscreenPageLimit = 1
        view_pager.isScrollable = true

        switchState(true)

        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> switchState(true)
                    1 -> switchState(false)
                    else -> switchState(true)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    override fun changeHomeNavigationState(state: Boolean) {
    }

    override fun changeHomePagerIndex(index: Int) {
    }

    override fun changeDrawerLayoutState() {
    }

    override fun receiveUpdateCallBack(preNTF: Notification) {
        val intent = Intent(this, HomeActivity::class.java)
        val pending = PendingIntent.getActivity(applicationContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT)
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

    private fun initData() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                applicationContext)
        val edit = sharedPreferences.edit()
        //获取阅读页背景
        if (sharedPreferences.getInt("content_mode", 51) < 50) {
            Constants.MODE = 51
            edit.putInt("content_mode", Constants.MODE)
            edit.putInt("current_light_mode", Constants.MODE)
            edit.apply()
        } else {
            Constants.MODE = sharedPreferences.getInt("content_mode", 51)
        }

        //判断用户是否是当日首次打开应用
        val first_time = sharedPreferences.getLong(Constants.TODAY_FIRST_OPEN_APP, 0)
        AppLog.e("BaseBookApplication", "first_time=" + first_time)
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
        AppLog.e("BaseBookApplication",
                "Constants.is_user_today_first=" + Constants.is_user_today_first)

        mLoadDataManager = LoadDataManager(this)
        Constants.upload_userinformation = sharedPreferences.getBoolean(Constants.IS_UPLOAD, false)

        val premVersionCode = Constants.preVersionCode
        val currentVersionCode = AppUtils.getVersionCode()

        if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
            //
            if (!Constants.upload_userinformation || premVersionCode != currentVersionCode) {
                /*  // 获取用户基础数据
                  StatisticManager.getStatisticManager().sendUserData()
    */
                Constants.upload_userinformation = true
                Constants.preVersionCode = currentVersionCode
                sharedPreferences.edit().putBoolean(Constants.IS_UPLOAD,
                        Constants.upload_userinformation).apply()
            }
        }


        if (Constants.is_user_today_first) {
            // 老用户更新书架书籍的完结/连载状态,和dex值
            /*   mLoadDataManager!!.updateShelfBooks()*/

            // 用户第一次启动时删掉物料表中的信息
            /*  new Thread() {
                @Override
                public void run() {
                    try {
                        AdDao.getInstance(HomeActivity.this).deleteAdMaterial();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();*/
        }
    }
/*
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (viewPager != null && viewPager!!.currentItem != 0 && mHomeFragment != null) {
                mHomeFragment!!.setTabSelected(0)
                return true
            } else if (removeMenuHelper != null && removeMenuHelper!!.dismissRemoveMenu()) {

                return true
            } else {
                doubleClickFinish()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }*/

/*    override fun onPause() {
        try {
            super.onPause()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (frameHelper != null) {
            frameHelper!!.onPauseAction()
        }
    }*/

    /**
     * 接收默认书籍的加载完成刷新
     */
/*    fun onEvent(event: BookEvent) {
        if (event.getMsg().equals(BookEvent.DEFAULTBOOK_UPDATED)) {
            if (mLoadDataManager != null) {
                mLoadDataManager!!.updateShelfBooks()
            }
        } else if (event.getMsg().equals(BookEvent.PULL_BOOK_STATUS)) {
            if (bookView != null) {
                bookView!!.updateBook()
            }
        }
    }*/

    /**
     * 两次返回键退出
     */
/*    private fun doubleClickFinish() {
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
    }*/

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

//    fun getViewPager(pager: ViewPager) {
//        this.viewPager = pager as NonSwipeViewPager
//    }

/*   fun getRemoveMenuHelper(helper: BookShelfRemoveHelper) {
       this.removeMenuHelper = helper
   }

   fun getFrameBookRankView(bookView: Fragment) {
       this.bookView = bookView as BookShelfFragment
   }
*/
/*    fun frameHelper() {
        if (frameHelper == null) {
            frameHelper = FrameBookHelper(applicationContext, this@HomeActivity)
        }
        frameHelper!!.setCancleUpdate(this)
    }

    fun getAllCheckedState(isAllChecked: Boolean) {}

    fun getMenuShownState(state: Boolean) {
        if (mHomeFragment != null) {
            mHomeFragment!!.onMenuShownState(state)
        }
    }

    fun setSelectTab(index: Int) {
        if (mHomeFragment != null) {
            mHomeFragment!!.setTabSelected(index)
        }
    }*/

    fun restoreSystemState() {
        restoreSystemDisplayState()
    }

    override fun onDestroy() {
        super.onDestroy()
        AndroidLogStorage.getInstance().clear()
        this@HomeActivity.unregisterReceiver(receiver)
        /*if (frameHelper != null) {
            try {
                frameHelper!!.restoreState()
                frameHelper = null
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        mHomeFragment = null
        removeMenuHelper = null
        viewPager = null
        bookView = null
        mHomeFragment = null
        try {
            setContentView(R.layout.empty)
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }

        if (BuildConfig.DEBUG) {
            BookApplication.getRefWatcher().watch(this)
        }
        EventBus.getDefault().unregister(this)
*/
    }

    override fun webJsCallback(jsInterfaceHelper: JSInterfaceHelper) {


        jsInterfaceHelper.setOnEnterAppClick { AppLog.e(TAG, "doEnterApp") }
        jsInterfaceHelper.setOnSearchClick { keyWord, search_type, filter_type, filter_word, sort_type ->
            try {
                val data = HashMap<String, String>()
                data.put("keyword", keyWord)
                data.put("type", "0")//0 代表从分类过来
                StartLogClickUtil.upLoadEventLog(this@HomeActivity,
                        StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.SYSTEM_SEARCHRESULT,
                        data)

                SearchBookActivity.isStayHistory = false
                val intent = Intent()
                intent.setClass(this@HomeActivity, SearchBookActivity::class.java)
                intent.putExtra("word", keyWord)
                intent.putExtra("search_type", search_type)
                intent.putExtra("filter_type", filter_type)
                intent.putExtra("filter_word", filter_word)
                intent.putExtra("sort_type", sort_type)
                intent.putExtra("from_class", "fromClass")//是否从分类来
                startActivity(intent)
                AppLog.e("kkk", search_type + "===")

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
            data.put("BOOKID", book_id)
            data.put("source", "WEBVIEW")
            StartLogClickUtil.upLoadEventLog(this@HomeActivity,
                    StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.ENTER, data)
/*
            val requestItem = RequestItem()
            requestItem.book_id = book_id
            requestItem.book_source_id = book_source_id
            requestItem.host = host
            requestItem.name = name
            requestItem.authorType = authorType
            requestItem.parameter = parameter
            requestItem.extra_parameter = extra_parameter

            val intent = Intent()
            intent.setClass(applicationContext, CoverPageActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable(Constants.REQUEST_ITEM, requestItem)
            intent.putExtras(bundle)
            startActivity(intent)*/
        })


        jsInterfaceHelper.setOnEnterCategory { _, _, _, _ -> AppLog.e(TAG, "doCategory") }


    }

    override fun startLoad(webView: WebView, url: String): String {
        return url
    }

    /**
     * 获取广播数据
     *
     * @authorType jiqinlin
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
            return AppUtils.scanLocalInstallAppList(
                    packageManager)
        }

        override fun onPostExecute(s: String) {

            /* StartLogClickUtil.upLoadApps(this@HomeActivity, s)*/
        }
    }

    override fun supportSlideBack(): Boolean {
        return false
    }

    companion object {
        private val BACK = 12
        private var BACK_COUNT: Int = 0
        internal var handler = Handler(Handler.Callback { msg ->
            when (msg.what) {
                BACK -> BACK_COUNT = 0
            }
            true
        })
    }


    private fun intentSearch() {
        RouterUtil.navigation(this, RouterConfig.SEARCH_BOOK_ACTIVITY)
        when (bottomType) {
            2 -> StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.RECOMMEND_PAGE, StartLogClickUtil.QG_TJY_SEARCH)
            3 -> StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.TOP_PAGE, StartLogClickUtil.QG_BDY_SEARCH)
            4 -> StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.CLASS_PAGE, StartLogClickUtil.QG_FL_SEARCH)
            else -> StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.SEARCH)
        }

        SearchBookActivity.isStayHistory = false

    }


    //头部2TAB的方式
    private fun switchState(isBookShelf: Boolean) {
        ll_home_bookshelf?.isSelected = isBookShelf
        ll_home_bookstore?.isSelected = !isBookShelf

    }


    /**
     * 顶部导航适配器
     */
    inner class HomeAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getCount(): Int {
            return 2
        }

        override fun getItem(position: Int): Fragment? {
            return when (position) {
                0 -> {
                    if (bookShelfFragment == null) bookShelfFragment = BookShelfFragment()
                    bookShelfFragment
                }
                1 -> {
                    if (bookStoreFragment == null) {
                        bookStoreFragment = BookStoreFragment.newInstance()
                        sharedPreferences?.edit()?.putString(Constants.FINDBOOK_SEARCH, "recommend")?.apply()
                    }
                    bookStoreFragment
                }
                else -> {
                    if (bookShelfFragment == null) bookShelfFragment = BookShelfFragment()
                    bookShelfFragment
                }
            }
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            return when (position) {
                0 -> {
                    val bookShelfFragment = super.instantiateItem(container, position) as BookShelfFragment
                    bookShelfFragment.doUpdateBook()
                    bookShelfFragment
                }
                else -> super.instantiateItem(container, position)
            }
        }

        override fun getItemPosition(`object`: Any): Int {
            return PagerAdapter.POSITION_NONE
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            super.destroyItem(container, position, `object`)
        }
    }


}
