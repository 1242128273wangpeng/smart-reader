package com.intelligent.reader.activity


import android.app.Notification
import android.app.PendingIntent
import android.content.*
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.KeyEvent
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.alibaba.android.arouter.facade.annotation.Route
import com.dingyue.bookshelf.BookShelfFragment
import com.dingyue.bookshelf.BookShelfInterface
import com.dingyue.statistics.DyStatService
import com.dy.media.MediaLifecycle
import com.intelligent.reader.R
import com.intelligent.reader.fragment.BookStoreFragment
import com.intelligent.reader.view.PushSettingDialog
import com.umeng.message.PushAgent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.txtqbmfxs.act_home.*
import net.lzbook.kit.constants.ActionConstants
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.pointpage.EventPoint
import net.lzbook.kit.presenter.HomePresenter
import net.lzbook.kit.service.CheckNovelUpdateService
import net.lzbook.kit.ui.activity.DownloadErrorActivity
import net.lzbook.kit.ui.activity.base.BaseCacheableActivity
import net.lzbook.kit.ui.widget.BannerDialog
import net.lzbook.kit.utils.*
import net.lzbook.kit.utils.AppUtils.fixInputMethodManagerLeak
import net.lzbook.kit.utils.encrypt.MD5Utils
import net.lzbook.kit.utils.logger.HomeLogger
import net.lzbook.kit.utils.oneclick.OneClickUtil
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.view.HomeView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File

/**
 * Function：书架、书城界面
 *
 * Created by JoannChen on 2018/6/16 0016 10:38
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
@Route(path = RouterConfig.HOME_ACTIVITY)
class HomeActivity : BaseCacheableActivity(), CheckNovelUpdateService.OnBookUpdateListener,
        HomeView, BookShelfInterface, View.OnClickListener, BookStoreFragment.SearchClickListener,
        BookShelfFragment.OnIsShowHomePageTitle {

    override fun showTitle() {
        home_fragment_head.visibility = View.VISIBLE
    }

    override fun hideTitle() {
        home_fragment_head.visibility = View.GONE
    }


    private var homePresenter: HomePresenter? = null
    private var isClosed = false
    private var apkUpdateUtils: ApkUpdateUtils? = null
    private var homeBroadcastReceiver: HomeBroadcastReceiver? = null
    private var bottomType: Int = 0//青果打点搜索 1 精选 2 榜单

    private var bookShelfFragment: BookShelfFragment? = null

    private val bookStoreFragment: BookStoreFragment by lazy {
        val fragment = BookStoreFragment()
        fragment
    }

    private var sharedPreferences: SharedPreferences? = null
    private var homeAdapter: HomeAdapter? = null

    private val pushSettingDialog: PushSettingDialog by lazy {
        val dialog = PushSettingDialog(this)
        dialog.openPushListener = {
            openPushSetting()
            DyStatService.onEvent(EventPoint.MAIN_POPUPNOWOPEN)
        }
        lifecycle.addObserver(dialog)
        dialog
    }

    private val bannerDialog: BannerDialog by lazy {
        BannerDialog(this, null)
    }

    override fun getCurrent(position: Int) {
        bottomType = position
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* EventBus.getDefault().register(this)*/

        setContentView(R.layout.act_home)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        homePresenter = HomePresenter(this, this.packageManager)

        initListener()
        initViewPager()
        homePresenter!!.initParameters()

        homeAdapter = HomeAdapter(supportFragmentManager)
        view_pager.adapter = homeAdapter


        //注册广播接收器
        registerHomeReceiver()

        apkUpdateUtils = ApkUpdateUtils(this)
        try {
            apkUpdateUtils!!.getApkUpdateInfo(this, handler, "HomeActivity")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        DyStatService.clearInvalidData()

        homePresenter!!.initDownloadService()
        HomeLogger.uploadHomeBookListInformation()

        if (isShouldShowPushSettingDialog()) {
            pushSettingDialog.show()
        }

        EventBus.getDefault().register(this)

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            val position = intent.getIntExtra("position", -1)
            if (position == 0 || position == 1) {
                changeHomePagerIndex(position)
            }

        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            bookshelf_search.id -> {
                if (OneClickUtil.isDoubleClick(System.currentTimeMillis())) {
                    return
                }
                HomeLogger.uploadHomeSearch(bottomType)
                RouterUtil.navigation(this, RouterConfig.SEARCH_BOOK_ACTIVITY)
            }
            bookshelf_setting.id -> {
                if (OneClickUtil.isDoubleClick(System.currentTimeMillis())) {
                    return
                }
                HomeLogger.uploadHomePersonal()
                RouterUtil.navigation(this, RouterConfig.SETTING_ACTIVITY)
            }
            ll_home_bookshelf.id -> {
                view_pager.currentItem = 0
                DyStatService.onEvent(EventPoint.MAIN_BOOKSHELF)

            }
            ll_home_bookstore.id -> {
                view_pager.currentItem = 1
            }

        }
    }

    /***
     * 注册广播接受器
     * **/
    private fun registerHomeReceiver() {
        homeBroadcastReceiver = HomeBroadcastReceiver()

        val intentFilter = IntentFilter()
        intentFilter.addAction(ActionConstants.ACTION_ADD_DEFAULT_SHELF)
        intentFilter.addAction(ActionConstants.ACTION_CHECK_UPDATE_FINISH)
        intentFilter.addAction(ActionConstants.ACTION_DOWNLOAD_APP_SUCCESS)

        this.registerReceiver(homeBroadcastReceiver, intentFilter)
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
        // 去书城
        view_pager.currentItem = index
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
    fun setupApplication(filePath: String) {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.action = Intent.ACTION_VIEW
        val type = "application/vnd.android.package-archive"
        intent.setDataAndType(Uri.fromFile(File(filePath)), type)
        startActivity(intent)
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            when {
                view_pager?.currentItem != 0 -> changeHomePagerIndex(0)
                bookShelfFragment?.isRemoveMenuShow() == true -> bookShelfFragment?.dismissRemoveMenu()
                else -> doubleClickFinish()
            }
            return true

        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * 两次返回键退出
     */
    private fun doubleClickFinish() {
        BACK_COUNT++

        if (BACK_COUNT == 1) {
            ToastUtil.showToastMessage(R.string.mian_click_tiwce_exit)
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

    override fun onResume() {
        super.onResume()
        MediaLifecycle.onResume()
    }

    override fun onPause() {
        super.onPause()
        MediaLifecycle.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        DyStatService.clearInvalidData()
        this.unregisterReceiver(homeBroadcastReceiver)
        MediaLifecycle.onDestroy()
        try {
            homeAdapter = null
            setContentView(R.layout.common_empty)
        } catch (exception: Resources.NotFoundException) {
            exception.printStackTrace()
        }
        fixInputMethodManagerLeak(applicationContext)

        EventBus.getDefault().unregister(this)
    }

    /**
     * 接收广播数据
     */
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
            }
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


    //头部2TAB的方式
    private fun switchState(isBookShelf: Boolean) {
        ll_home_bookshelf?.isSelected = isBookShelf
        ll_home_bookstore?.isSelected = !isBookShelf
        if (ll_home_bookstore?.isSelected == true) {
            bookShelfFragment?.dismissRemoveMenu()
        }
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
                    bookStoreFragment.setOnBottomClickListener(this@HomeActivity)
                    sharedPreferences?.edit()?.putString(Constants.FINDBOOK_SEARCH, "recommend")?.apply()

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

}
