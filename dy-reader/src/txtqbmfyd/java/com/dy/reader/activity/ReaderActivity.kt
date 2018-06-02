package com.dy.reader.activity

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.text.TextUtils
import android.util.EventLog
import android.view.KeyEvent
import android.view.SurfaceHolder
import android.view.View
import android.view.WindowManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.ding.basic.bean.Book
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dingyue.contract.util.showToastMessage
import com.dy.reader.R
import com.dy.reader.R.id.ad_view
import com.dy.reader.R.id.recyclerView
import com.dy.reader.ReadMediaManager
import com.dy.reader.data.DataProvider
import com.dy.reader.event.EventLoading
import com.dy.reader.event.EventReaderConfig
import com.dy.reader.event.EventSetting
import com.dy.reader.fragment.AutoReadOptionFragment
import com.dy.reader.fragment.CatalogMarkFragment
import com.dy.reader.fragment.LoadingDialogFragment
import com.dy.reader.fragment.ReadSettingFragment
import com.dy.reader.helper.AppHelper
import com.dy.reader.page.*
import com.dy.reader.presenter.ReadPresenter
import com.dy.reader.setting.ReaderSettings
import com.dy.reader.setting.ReaderStatus
import com.dycm_adsdk.PlatformSDK
import iyouqu.theme.BaseCacheableActivity
import iyouqu.theme.FrameActivity
import kotlinx.android.synthetic.txtqbmfyd.act_read.*
import kotlinx.android.synthetic.txtqbmfyd.reading_page.*
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.repair_books.RepairHelp
import net.lzbook.kit.request.UrlUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Route(path = RouterConfig.READER_ACTIVITY)
class ReaderActivity : BaseCacheableActivity(), SurfaceHolder.Callback {

    private var mCatalogMarkFragment: CatalogMarkFragment? = null
    private val mReadSettingFragment by lazy {
        val readSettingFragment = ReadSettingFragment()
        readSettingFragment.fm = fragmentManager
        readSettingFragment
    }

    private val mLoadingFragment by lazy {
        val loadingDialogFragment = LoadingDialogFragment()
        loadingDialogFragment.fm = fragmentManager
        loadingDialogFragment
    }

    lateinit var mReadPresenter: ReadPresenter

    var isResume = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_read)
        ReadMediaManager.init(this)
        ad_view.frameLayout = fl_menu_gesture
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)

        glSurfaceView.holder.addCallback(this)

        mReadPresenter = ReadPresenter(this)
        mReadPresenter.onCreateInit(savedInstanceState)


        read_catalog_mark_drawer.layoutParams.width = AppHelper.screenWidth
        read_catalog_mark_drawer.layoutParams.height = AppHelper.screenHeight


        mCatalogMarkFragment = supportFragmentManager.findFragmentById(R.id.read_catalog_mark_layout) as? CatalogMarkFragment

        mReadSettingFragment.setCurrentThemeMode(mReadPresenter.currentThemeMode)

        window.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        window.decorView.systemUiVisibility = FrameActivity.UI_OPTIONS_IMMERSIVE_STICKY

        trans_coding_tv.setOnClickListener {
            showDisclaimerActivity()
        }
        origin_tv.setOnClickListener {
            onOriginRead()
        }

        read_catalog_mark_drawer.addDrawerListener(mDrawerListener)
        read_catalog_mark_drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        if (!ReaderSettings.instance.isLandscape) {
            mReadPresenter.loadData()
        }

        RepairHelp.showFixMsg(this, ReaderStatus.book, {
            if (!this!!.isFinishing) {
                RouterUtil.navigation(this, RouterConfig.DOWNLOAD_MANAGER_ACTIVITY)
            }
        })

        mCatalogMarkFragment?.fixBook()

        initGuide()
    }

    private fun initGuide() {
        val sp = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        if (!sp.getBoolean(mReadPresenter.versionCode.toString() + Constants.READING_GUIDE_TAG, false)) {
            ll_guide_layout!!.visibility = View.VISIBLE
            iv_guide_reading.visibility = View.VISIBLE
            ll_guide_layout!!.setOnClickListener {
                sp.edit()
                        .putBoolean(mReadPresenter.versionCode.toString() + Constants.READING_GUIDE_TAG, true)
                        .apply()
                iv_guide_reading.visibility = View.GONE
                ll_guide_layout!!.visibility = View.GONE
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        glSurfaceView.visibility = View.GONE

        window.decorView.systemUiVisibility = FrameActivity.UI_OPTIONS_IMMERSIVE_STICKY

        read_catalog_mark_drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        mReadPresenter.onNewIntent(intent)
    }

    val orientaionRunnable  = {
        glSurfaceView.visibility = View.GONE

        window.decorView.systemUiVisibility = FrameActivity.UI_OPTIONS_IMMERSIVE_STICKY

        read_catalog_mark_drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        mReadPresenter.onConfigurationChanged()

        read_catalog_mark_drawer.layoutParams.width = AppHelper.screenWidth
        read_catalog_mark_drawer.layoutParams.height = AppHelper.screenHeight
        read_catalog_mark_drawer.requestLayout()
    }

    var lastOrientation = Configuration.ORIENTATION_PORTRAIT

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if ((ReaderSettings.instance.isLandscape && newConfig.orientation != Configuration.ORIENTATION_PORTRAIT) ||
                (!ReaderSettings.instance.isLandscape && newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)) {
            if(lastOrientation != newConfig.orientation) {
                lastOrientation = newConfig.orientation
                AppHelper.mainHandler.removeCallbacks(orientaionRunnable)
                AppHelper.mainHandler.postDelayed(orientaionRunnable, 500)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        try {
            outState?.putInt("sequence", ReaderStatus.position.group)
            outState?.putInt("offset", ReaderStatus.position.offset)
            outState?.putSerializable("book", ReaderStatus.book)
            if (ReaderStatus.currentChapter != null) {
                outState?.putSerializable("currentChapter", ReaderStatus.currentChapter)
            }
            outState?.putString("thememode", mThemeHelper?.getMode())
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            if (data != null) {// 更新目录后，重新获取chapterList

                val bundle = data.extras

                val receiveBook = bundle.getSerializable("book") as Book?
                if (receiveBook != null) {
                    ReaderStatus.book = receiveBook
                    ReaderStatus.book.book_source_id = ReaderStatus.book.book_source_id
                    val group = bundle.getInt("sequence")
                    val offset = bundle.getInt("offset", 0)
                    ReaderStatus.position = Position(ReaderStatus.book.book_id, group, offset)
                }

                ReaderStatus.book.fromType = 1//打点 书籍封面（0）/书架（1）/上一页翻页（2）
                if (Constants.QG_SOURCE == ReaderStatus.book.host) {
                    ReaderStatus.book.channel_code = 1
                } else {
                    ReaderStatus.book.channel_code = 2
                }
                val extras = Bundle()
                extras.putInt("sequence", ReaderStatus.position.group)
                extras.putInt("offset", ReaderStatus.position.offset)
                extras.putSerializable("book", ReaderStatus.book)
                RouterUtil.navigation(this, RouterConfig.READER_ACTIVITY, extras)
            }
        }
    }

    private val mDrawerListener = object : DrawerLayout.DrawerListener {

        override fun onDrawerSlide(drawerView: View, slideOffset: Float) = Unit
        //解锁， 可滑动关闭
        override fun onDrawerOpened(drawerView: View) {
            read_catalog_mark_drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNDEFINED)
            mCatalogMarkFragment?.loadData()
        }

        //锁定不可滑出
        override fun onDrawerClosed(drawerView: View) {
            read_catalog_mark_drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }

        override fun onDrawerStateChanged(newState: Int) = Unit
    }

    override fun onResume() {
        super.onResume()
        isResume = true
        glSurfaceView.onResume()
        // 设置全屏
        when (!Constants.isFullWindowRead) {
            true -> window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
            false -> window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        mReadPresenter.onResume()
    }

    override fun shouldShowNightShadow(): Boolean = false

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus && !ReaderStatus.isMenuShow) {
            window.decorView.systemUiVisibility = FrameActivity.UI_OPTIONS_IMMERSIVE_STICKY
        } else if (ReaderSettings.instance.animation == GLReaderView.AnimationType.LIST) {
            window.decorView.systemUiVisibility = FrameActivity.UI_OPTIONS_IMMERSIVE_STICKY
        }
    }

    override fun onPause() {
        super.onPause()
        isResume = false
        glSurfaceView.onPause()
        mReadPresenter.onPause()
    }

    override fun onStop() {
        super.onStop()
        mReadPresenter.onStop()
    }


    override fun onBackPressed() {
        if (read_catalog_mark_drawer.isDrawerOpen(GravityCompat.START)) {
            read_catalog_mark_drawer.closeDrawers()
            return
        }

        if (ReaderSettings.instance.isAutoReading) {
            ReaderSettings.instance.isAutoReading = false
            return
        }

        EventBus.getDefault().post(EventSetting(EventSetting.Type.MENU_STATE_CHANGE, false))

        val isFinish = mReadPresenter.onBackPressed()
        if (isFinish && !isFinishing) {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        read_catalog_mark_drawer.removeDrawerListener(mDrawerListener)
        EventBus.getDefault().unregister(this)
        ReaderStatus.clear()
        DataProvider.clear()
        AppHelper.glSurfaceView = null
        mReadPresenter.onDestroy()
        ReaderStatus.chapterList.clear()
        PlatformSDK.lifecycle()?.onDestroy()
        ReadMediaManager.onDestroy()
    }


    override fun surfaceCreated(holder: SurfaceHolder?) {
        println("holder surfaceCreated")
        AppHelper.glSurfaceView = glSurfaceView
        ReadMediaManager.frameLayout = ad_cache

        if (!EventBus.getDefault().isRegistered(glSurfaceView))
            EventBus.getDefault().register(glSurfaceView)
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        println("holder surfaceChanged $width * $height")
//        LoadingDialogFragment.newInstance().dismissDiaslog(isResume)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        println("holder surfaceDestroyed")

        EventBus.getDefault().unregister(glSurfaceView)
    }

    fun showDisclaimerActivity() {
        try {
            val bundle = Bundle()
            bundle.putBoolean("isFromReadingPage", true)
            RouterUtil.navigation(this, RouterConfig.DISCLAIMER_ACTIVITY, bundle)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onOriginRead() {
        var url: String? = null
        if (ReaderStatus.currentChapter != null) {
            url = UrlUtils.buildContentUrl(ReaderStatus.currentChapter!!.url)
        }
        if (!TextUtils.isEmpty(url)) {
            try {
                val uri = Uri.parse(url!!.trim { it <= ' ' })
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }catch (e:Exception){
                e.printStackTrace()
            }

            mReadPresenter.updateOriginLog()

        } else {
            this.applicationContext.showToastMessage("无法查看原文链接！")
        }
    }

    fun full(isFull: Boolean) {
//        if (!Constants.isFullWindowRead) {
//            return
//        }
//        if (isFull) {
//            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
//            window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
//        } else {
//            window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
//        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRecieveEvent(event: EventLoading) {
        if (event.type == EventLoading.Type.START) {
            showLoadingDialog(LoadingDialogFragment.DialogType.LOADING)
        } else if (event.type == EventLoading.Type.PROGRESS_CHANGE) {
            if (ReaderStatus.position.group > -1) {
                novel_bottom.visibility = View.VISIBLE
                novel_top.visibility = View.VISIBLE
                novel_page.text = "本章第${ReaderStatus.position.index + 1}/${ReaderStatus.position.groupChildCount}"
                novel_chapter.text = "${ReaderStatus.position.group + 1}/${ReaderStatus.chapterCount}章"
                if (ReaderStatus.position.group < ReaderStatus.chapterList.size) {
                    novel_title.text = "${ReaderStatus.chapterList[ReaderStatus.position.group].name}"
                }
            } else {
                novel_bottom.visibility = View.GONE
                novel_top.visibility = View.GONE
            }
            mReadPresenter?.checkManualDialogShow()
        } else if (event.type == EventLoading.Type.RETRY) {
            showLoadingDialog(LoadingDialogFragment.DialogType.ERROR, event.retry)
        } else if (event.type == EventLoading.Type.SUCCESS) {

            mLoadingFragment.dismissDiaslog(isResume)
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return when {
            Constants.isVolumeTurnover && recyclerView.onKeyEvent(event) -> true
            Constants.isVolumeTurnover && glSurfaceView.onKeyEvent(event) -> true
            else -> super.dispatchKeyEvent(event)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRecieveEvent(event: EventReaderConfig) {
        when (event.type) {
            ReaderSettings.ConfigType.ANIMATION -> {
                if (recyclerView.visibility != View.VISIBLE && ReaderSettings.instance.animation == GLReaderView.AnimationType.LIST) {
                    if (ReaderStatus.isMenuShow) {
                        mReadSettingFragment.show(false)
                        ReaderStatus.isMenuShow = false
                    }
                    PageManager.clear()
                    hideAd()
                    ReadMediaManager.tonken++
                    ReadMediaManager.adCache.clear()
                    mReadPresenter.loadData(true)
                } else if (glSurfaceView.visibility != View.VISIBLE && ReaderSettings.instance.animation != GLReaderView.AnimationType.LIST) {
                    PageManager.clear()

                    if (ReaderStatus.isMenuShow) {
                        mReadSettingFragment.show(false)
                        ReaderStatus.isMenuShow = false
                    }

                    ReadMediaManager.tonken++
                    ReadMediaManager.adCache.clear()
                    mReadPresenter.loadData(true)
                }
            }
            ReaderSettings.ConfigType.CHAPTER_REFRESH -> {
                hideAd()
                read_catalog_mark_drawer.closeDrawers()
            }
            ReaderSettings.ConfigType.FONT_REFRESH -> {
                hideAd()
                if (ReaderSettings.instance.animation != GLReaderView.AnimationType.LIST) {
                    ReadMediaManager.tonken++
                    ReadMediaManager.clearAllAd()
                }
                read_catalog_mark_drawer.closeDrawers()
            }
            ReaderSettings.ConfigType.GO_TO_BOOKEND -> mReadPresenter.goToBookEnd()
            ReaderSettings.ConfigType.TITLE_COCLOR_REFRESH -> {
                val titleColor = ReaderSettings.instance.titleColor
                novel_time.setTextColor(titleColor)
                origin_tv.setTextColor(titleColor)
                trans_coding_tv.setTextColor(titleColor)
                novel_page.setTextColor(titleColor)
                novel_chapter.setTextColor(titleColor)
                novel_title.setTextColor(titleColor)
                BatteryView.update()
            }
            else -> Unit
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRecieveEvent(event: EventSetting) {
        when (event.type) {
            EventSetting.Type.OPEN_CATALOG -> {
                read_catalog_mark_drawer.openDrawer(GravityCompat.START)
                mReadSettingFragment.show(!ReaderStatus.isMenuShow)
                ReaderStatus.isMenuShow = !ReaderStatus.isMenuShow
            }
            EventSetting.Type.MENU_STATE_CHANGE -> {
                if (!ReaderStatus.isMenuShow && ReaderSettings.instance.isAutoReading) {
                    val fragmet = fragmentManager.findFragmentByTag("auto")

                    if (fragmet == null) {
                        AutoReadOptionFragment().show(fragmentManager, "auto")
                    } else {
                        if (fragmet is AutoReadOptionFragment) {
                            fragmet.dismissAllowingStateLoss()
                        }
                    }
                    return
                }

                if (event.obj != null) {
                    val flag = event.obj as Boolean
                    mReadSettingFragment.show(flag)
                    ReaderStatus.isMenuShow = flag
                } else {
                    mReadSettingFragment.show(!ReaderStatus.isMenuShow)
                    ReaderStatus.isMenuShow = !ReaderStatus.isMenuShow
                }
            }
            EventSetting.Type.CHANGE_SCREEN_MODE -> {
                ReadMediaManager.tonken++
                ReadMediaManager.clearAllAd()
                mReadSettingFragment.show(!ReaderStatus.isMenuShow)
                ReaderStatus.isMenuShow = !ReaderStatus.isMenuShow
                mReadPresenter.changeScreenMode()
            }
            EventSetting.Type.FULL_WINDOW_CHANGE -> full(event.obj as Boolean)
            EventSetting.Type.HIDE_AD -> hideAd()
            EventSetting.Type.SHOW_AD -> showAd()

            else -> Unit
        }
    }

    private fun showAd() {
        //条件
        if (ad_view != null) {
            ad_view.alpha = if (mThemeHelper.isNight) 0.5f else 1f
        }
        if (ReaderStatus.currentChapter == null) return
        val mChapter = ReaderStatus.currentChapter!!
        val count = ReaderStatus.position.groupChildCount
        if (Constants.isHideAD || (ReaderStatus.position.group < 0 || mChapter.chargeChapter == 1 || count < 3 || ReaderStatus.position.index < 2)) {
            hideAd()
            return
        }
        //add广告
        ad_view.removeAllViews()
        val adType = ReadMediaManager.generateAdType(ReaderStatus.position.group, ReaderStatus.position.index)
        val adView = ReadMediaManager.adCache.get(adType)
        if (adView != null) {
            if (adView.loaded) {//加载成功
                if (adView.view != null) {

                    //加载成功后清除掉回调
                    ReadMediaManager.loadAdComplete = null

                    if (ReaderStatus.position.index == count - 2) {//8-1
                        val space = (AppHelper.screenDensity * ReaderSettings.instance.mLineSpace).toInt()
                        ad_view.addView(adView.view, ReadMediaManager.getLayoutParams(AppHelper.screenHeight - adView.height - AppHelper.dp2px(26)))
                        ad_view?.visibility = View.VISIBLE
                    } else {//5-1 5-2 6-1 6-2
                        ad_view.addView(adView.view, ReadMediaManager.getLayoutParams())
                        ad_view?.visibility = View.VISIBLE
                    }

                    if (!PageManager.currentPage.filledAD) {
                        EventBus.getDefault()
                                .post(EventReaderConfig(ReaderSettings.ConfigType.PAGE_REFRESH))
                    }

                } else {
                    ReadMediaManager.loadAdComplete = { type: String ->
                        if (type == adType) showAd()//本页的广告请求回来，重走方法
                    }
                }
            } else {//加载失败
                val adMark = ReadMediaManager.generateAdMark()
                ReadMediaManager.requestAd(adType, adMark)
                ReadMediaManager.loadAdComplete = { type: String ->
                    if (type == adType) showAd()//本页的广告请求回来，重走方法
                }
            }
        } else {
            hideAd()
        }
    }

    fun showReader() {
        runOnUiThread {
            when {
                ReaderSettings.instance.animation == GLReaderView.AnimationType.LIST -> {
                    glSurfaceView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    recyclerView?.entrance()
                    mLoadingFragment.dismissDiaslog(isResume)
                }
                else -> {
                    recyclerView.visibility = View.GONE
                    glSurfaceView.visibility = View.VISIBLE
                    glSurfaceView.requestRender()
                    showAd()
                }
            }
        }
    }

    private fun hideAd() {
        ad_view?.visibility = View.GONE
    }

    fun showLoadingDialog(type: LoadingDialogFragment.DialogType, retry: (() -> Unit)? = null) {
        mLoadingFragment.show(type, retry)
    }

    override fun supportSlideBack(): Boolean = false

}