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
import com.baidu.mobstat.StatService
import com.bumptech.glide.Glide
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.dingyue.bookshelf.BookShelfFragment
import com.dingyue.bookshelf.BookShelfInterface
import com.dy.media.MediaLifecycle
import com.intelligent.reader.R
import com.intelligent.reader.fragment.RecommendFragment
import com.intelligent.reader.fragment.WebViewFragment
import com.intelligent.reader.util.fragmentBundle
import com.intelligent.reader.view.BookStoreDialog
import com.intelligent.reader.view.PushSettingDialog
import com.umeng.message.PushAgent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.qbmfkdxs.act_home.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.appender_loghub.appender.AndroidLogStorage
import net.lzbook.kit.bean.EventBookStore
import net.lzbook.kit.constants.ActionConstants
import net.lzbook.kit.presenter.HomePresenter
import net.lzbook.kit.service.CheckNovelUpdateService
import net.lzbook.kit.ui.activity.DownloadErrorActivity
import net.lzbook.kit.ui.activity.base.BaseCacheableActivity
import net.lzbook.kit.ui.widget.BannerDialog
import net.lzbook.kit.utils.*
import net.lzbook.kit.utils.AppUtils.fixInputMethodManagerLeak
import net.lzbook.kit.utils.encrypt.MD5Utils
import net.lzbook.kit.utils.logger.HomeLogger
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.utils.user.UserManager
import net.lzbook.kit.utils.web.WebViewIndex
import net.lzbook.kit.view.HomeView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File

@Route(path = RouterConfig.HOME_ACTIVITY)
class HomeActivity : BaseCacheableActivity(),
        CheckNovelUpdateService.OnBookUpdateListener, HomeView, BookShelfInterface {

    private var homePresenter: HomePresenter? = null

    private var homeBroadcastReceiver: HomeBroadcastReceiver? = null

    private lateinit var intentFilter: IntentFilter

    private var homeAdapter: HomeAdapter? = null

    private var closed = false

    private var currentIndex = 0
    private var versionCode: Int = 0

    private var guideLongPress: Boolean = true

    private lateinit var apkUpdateUtils: ApkUpdateUtils

    private val bookStoreDialog: BookStoreDialog by lazy {
        BookStoreDialog(this)
    }

    private val bookShelfFragment: BookShelfFragment by lazy {
        BookShelfFragment()
    }

    private val recommendFragment: RecommendFragment by lazy {
        RecommendFragment()
    }

    private val rankingFragment: WebViewFragment by lazy {
        fragmentBundle("rank", WebViewIndex.rank)
    }

    private val categoryFragment: WebViewFragment by lazy {
        fragmentBundle("category", WebViewIndex.category)
    }

    private val fragmentList: ArrayList<Fragment> = arrayListOf(
            bookShelfFragment,
            recommendFragment,
            rankingFragment,
            categoryFragment
    )

    private var registerShareCallback = false

    private val pushSettingDialog: PushSettingDialog by lazy {
        val dialog = PushSettingDialog(this)
        dialog.openPushListener = {
            openPushSetting()
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PAGE_SHELF,
                    StartLogClickUtil.POPUPNOWOPEN)
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
        homePresenter = HomePresenter(this, this.packageManager)

        initView()
        initGuide()

        homePresenter?.initParameters()

        registerHomeReceiver()

        checkAppUpdate()

        initPosition()

        AndroidLogStorage.getInstance().clear()

        homePresenter?.initDownloadService()

        HomeLogger.uploadHomeBookListInformation()

        if (isShouldShowPushSettingDialog()) {
            pushSettingDialog.show()
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PAGE_SHELF,
                    StartLogClickUtil.POPUPMESSAGE)
        }

        EventBus.getDefault().register(this)
    }

    override fun onResume() {
        super.onResume()
        MediaLifecycle.onResume()
        this.changeHomePagerIndex(currentIndex)
        this.registerShareCallback = false
        StatService.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        StatService.onPause(this)
        MediaLifecycle.onPause()
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
        MediaLifecycle.onDestroy()
        try {
            homeAdapter = null
            homePresenter = null
            Glide.get(this).clearMemory()
            setContentView(R.layout.common_empty)
        } catch (exception: Resources.NotFoundException) {
            exception.printStackTrace()
        }
        fixInputMethodManagerLeak(applicationContext)
        EventBus.getDefault().unregister(this)
    }

    override fun onBackPressed() {
        when {
            view_pager?.currentItem != 0 -> changeHomePagerIndex(0)
            bookShelfFragment.isRemoveMenuShow() -> bookShelfFragment.dismissRemoveMenu()
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
            SPUtils.putDefaultSharedString(SPKey.HOME_FINDBOOK_SEARCH, "recommend")
            HomeLogger.uploadHomeRecommendSelected()
        }

        ll_bottom_tab_ranking.setOnClickListener {
            this.changeHomePagerIndex(2)
            SPUtils.putDefaultSharedString(SPKey.HOME_FINDBOOK_SEARCH, "top")
            HomeLogger.uploadHomeRankSelected()
        }

        ll_bottom_tab_category.setOnClickListener {
            this.changeHomePagerIndex(3)
            SPUtils.putDefaultSharedString(SPKey.HOME_FINDBOOK_SEARCH, "class")
            HomeLogger.uploadHomeCategorySelected()
        }
    }

    private fun initGuide() {
        val key = SPKey.getBOOKSHELF_GUIDE_TAG()
        if (!SPUtils.getDefaultSharedBoolean(key)) {
            fl_guide_layout.visibility = View.VISIBLE
            img_guide_download.visibility = View.VISIBLE
            fl_guide_layout.setOnClickListener {
                if (guideLongPress) {
                    img_guide_remove.visibility = View.VISIBLE
                    img_guide_download.visibility = View.GONE
                    guideLongPress = false
                } else {
                    SPUtils.putDefaultSharedBoolean(key, true)
                    img_guide_remove.visibility = View.GONE
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

        if (currentIndex != 0 && bookShelfFragment.isRemoveMenuShow()) {
            bookShelfFragment.dismissRemoveMenu()
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
                if (SPUtils.getDefaultSharedBoolean(SPKey.FIRST_COME_IN_RECOMMEND, true)) {
                    bookStoreDialog.show()
                    SPUtils.putDefaultSharedBoolean(SPKey.FIRST_COME_IN_RECOMMEND, false)
                }
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

    override fun registerShareCallback(state: Boolean) {
        this.registerShareCallback = state
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (registerShareCallback) {
            UserManager.registerQQShareCallBack(requestCode, resultCode, data)
        }
    }


    override fun supportSlideBack(): Boolean {
        return false
    }

    /***
     * HomeActivity子页面的Adapter
     * **/
    inner class HomeAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

        override fun getCount(): Int = fragmentList.size

        override fun getItem(position: Int): Fragment? {
            return fragmentList[position]
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
                bookShelfFragment.updateUI()
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
                bookShelfFragment.updateUI()
            } else if (intent.action == ActionConstants.ACTION_ADD_DEFAULT_SHELF) {
                bookShelfFragment.updateUI()
            }
        }
    }

    /***
     * 更改底部导航栏状态
     * **/
    override fun changeHomeNavigationState(state: Boolean) {
        if (state) {
            ll_home_tab.visibility = View.GONE
            view_bottom_divider.visibility = View.GONE
            AnimationHelper.smoothScrollTo(view_pager, 0)
        } else {
            view_bottom_divider.visibility = View.VISIBLE
            ll_home_tab.visibility = View.VISIBLE
            AnimationHelper.smoothScrollTo(view_pager, 0)
        }
    }

    /***
     * 改变ViewPager Index
     * **/
    override fun changeHomePagerIndex(index: Int) {
        if (currentIndex != index) {
            view_pager.setCurrentItem(index, false)
        }
    }

    /***
     * 改变DrawerLayout状态，当书架页点击设置时，显示抽屉布局
     * **/
    override fun changeDrawerLayoutState() {

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