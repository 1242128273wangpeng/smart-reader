package com.intelligent.reader.activity

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.intelligent.reader.R
import com.intelligent.reader.app.BookApplication
import com.intelligent.reader.fragment.CatalogMarkFragment
import com.intelligent.reader.presenter.read.CatalogMarkPresenter
import com.intelligent.reader.presenter.read.ReadOptionPresenter
import com.intelligent.reader.presenter.read.ReadPreInterface
import com.intelligent.reader.presenter.read.ReadPresenter
import com.intelligent.reader.read.DataProvider
import com.intelligent.reader.read.help.IReadPageChange
import com.intelligent.reader.read.mode.ReadState
import com.intelligent.reader.read.page.AutoReadMenu
import com.intelligent.reader.read.page.ReadSettingView
import com.intelligent.reader.read.page.ReaderViewWidget
import com.intelligent.reader.reader.ReaderViewModel
import com.logcat.sdk.LogEncapManager
import iyouqu.theme.BaseCacheableActivity
import iyouqu.theme.FrameActivity
import kotlinx.android.synthetic.txtqbmfyd.act_read.*
import kotlinx.android.synthetic.txtqbmfyd.reading_page.*
import net.lzbook.kit.BuildConfig
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.CallBackDownload
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.ReadConfig
import net.lzbook.kit.data.bean.ReadViewEnums
import net.lzbook.kit.data.bean.Source
import net.lzbook.kit.router.RouterConfig
import net.lzbook.kit.utils.*
import java.lang.Exception
import java.lang.reflect.Method
import java.util.*


/**
 * ReadingActivity
 * 小说阅读页
 */

@Route(path = RouterConfig.READING_ACTIVITY)
class ReadingActivity : BaseCacheableActivity(), AutoReadMenu.OnAutoMemuListener, ReadSettingView.OnReadSettingListener, ReadPreInterface.View, IReadPageChange
        , ReaderViewWidget.OnAutoReadCallback, CallBackDownload {

    // 系统存储设置
    private lateinit var mSharedPreferencesUtils: SharedPreferencesUtils

    private var mCatalogMarkFragment: CatalogMarkFragment? = null

    private lateinit var mReadPresenter: ReadPresenter

    private val startReadTime = System.currentTimeMillis()

    private var isReadAutoOpening = false

    private lateinit var book: Book

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        book = intent.getSerializableExtra("book") as Book
        DataProvider.getInstance().readingActivity = this
        ReadState.addObserver(DataProvider.getInstance())
        setUIOptions()

        setContentView(R.layout.act_read)

        mCatalogMarkFragment = supportFragmentManager.findFragmentById(R.id.read_catalog_mark_layout) as? CatalogMarkFragment

        window.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        window.decorView.systemUiVisibility = FrameActivity.UI_OPTIONS_IMMERSIVE_STICKY

        mSharedPreferencesUtils = SharedPreferencesUtils(PreferenceManager.getDefaultSharedPreferences(applicationContext))

        mReadPresenter = ReadPresenter(this)
        mReadPresenter.onCreateInit(savedInstanceState)
        auto_menu.setRateValue()
        mCatalogMarkFragment?.fixBook()

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        book = intent.getSerializableExtra("book") as Book
        setUIOptions()
        read_catalog_mark_drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        mReadPresenter.onNewIntent(intent)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setUIOptions()
        read_catalog_mark_drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        mReadPresenter.onConfigurationChanged(mCatalogMarkFragment, option_header, readerWidget.childCount)
        ReadConfig.IS_LANDSCAPE = (newConfig.orientation != Configuration.ORIENTATION_PORTRAIT)
    }

    override fun initView(fac: ReaderViewModel) {
        readSettingView.setOnReadSettingListener(this)
        readSettingView.setDataFactory(fac, mThemeHelper)
        readSettingView.currentThemeMode = mReadPresenter.currentThemeMode

        readerWidget.setOnAutoReadCallback(this)

        read_catalog_mark_drawer.addDrawerListener(mDrawerListener)
        read_catalog_mark_drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        readerWidget.removeAllViews()
        auto_menu.setOnAutoMemuListener(this)

        readerWidget.setIReadPageChange(this)
        readerWidget.entrance()
        readSettingView.setNovelMode(ReadConfig.MODE)

        initGuide()

        setNavigationBarListener()
    }

    private fun setUIOptions() {
        if (ReadConfig.animation == ReadViewEnums.Animation.list) {
            window.decorView.systemUiVisibility = FrameActivity.UI_OPTIONS_IMMERSIVE_STICKY
        } else {
            window.decorView.systemUiVisibility = FrameActivity.UI_OPTIONS_IMMERSIVE_STICKY
        }
    }

    private fun initGuide() {
        if (!mSharedPreferencesUtils.getBoolean(mReadPresenter.versionCode.toString() + Constants.READING_GUIDE_TAG)) {
            ll_guide_layout.visibility = View.VISIBLE
            iv_guide_reading.visibility = View.VISIBLE
            ll_guide_layout.setOnClickListener {
                mSharedPreferencesUtils.putBoolean(mReadPresenter.versionCode.toString() + Constants.READING_GUIDE_TAG, true)
                iv_guide_reading.visibility = View.GONE
                ll_guide_layout.visibility = View.GONE
            }
        }
    }

    override fun initCatlogView() {
        mReadPresenter.initCatalogPresenter(mCatalogMarkFragment, option_header)
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

    override fun onNewInitView(): Boolean {
        mReadPresenter.initCatalogPresenter(mCatalogMarkFragment, option_header)
        return true
    }

    fun searchChapterCallBack(sourcesList: ArrayList<Source>) = mReadPresenter.onGetSourceList(sourcesList)

    /**
     * 隐藏topmenu
     */
    fun dismissTopMenu() = mReadPresenter.dismissTopMenu()

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (Constants.isVolumeTurnover) {
            if (readerWidget.onKeyEvent(event)) {
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean =
            when (keyCode == KeyEvent.KEYCODE_MENU) {
                true -> {
                    mReadPresenter.onKeyDown()
                    true
                }
                else -> super.onKeyDown(keyCode, event)
            }

    override fun onBackPressed() {
        if (read_catalog_mark_drawer.isDrawerOpen(GravityCompat.START)) {
            read_catalog_mark_drawer.closeDrawers()
            return
        }

        if (readerWidget.isAutoRead) {
            readerWidget.stopAutoRead()
            showStopAutoHint()
            return
        }

//        showMenu(false)

        val isFinish = mReadPresenter.onBackPressed()
        if (isFinish && !isFinishing) {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        readerWidget.onResume()
        // 设置全屏
        when (!Constants.isFullWindowRead) {
            true -> window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
            false -> window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        mReadPresenter.onResume()
        if (!TextUtils.isEmpty(ReadState.book.book_id)) {
            option_header.setBookDownLoadState(ReadState.book.book_id)
            CacheManager.listeners.add(this)
        }
    }

    override fun shouldReceiveCacheEvent(): Boolean = false

    override fun shouldShowNightShadow(): Boolean = false

    override fun onPause() {
        super.onPause()
        readerWidget.onPause()
        mReadPresenter.onPause(ReadState.sequence, ReadState.offset)
        CacheManager.listeners.remove(this)
    }

    override fun onTaskStatusChange(book_id: String?) {
        option_header.setBookDownLoadState(book_id)
    }

    override fun onTaskFinish(book_id: String?) {
        option_header.setBookDownLoadState(book_id)
    }

    override fun onTaskFailed(book_id: String?, t: Throwable?) {
    }

    override fun onTaskProgressUpdate(book_id: String?) {
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus && !ReadState.isMenuShow) {
            window.decorView.postDelayed(immersiveRunable, 1500)
        } else if (ReadConfig.animation == ReadViewEnums.Animation.list) {
            window.decorView.systemUiVisibility = FrameActivity.UI_OPTIONS_IMMERSIVE_STICKY
        }
    }

    override fun onStop() {
        super.onStop()
        mReadPresenter.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()

        read_catalog_mark_drawer.removeDrawerListener(mDrawerListener)
//        mCatalogMarkFragment?.let {
//            read_catalog_mark_drawer.removeDrawerListener(it)
//        }

//        readSettingView.recycleResource()

        auto_menu.setOnAutoMemuListener(null)

        mReadPresenter.onDestroy()
//        DataProvider.getInstance().unSubscribe()
        DataProvider.getInstance().clear()
        ReadState.deleteObserver(DataProvider.getInstance())
        readerWidget.onDestroy()
        ReadConfig.unregistObserverAll()
        if (BuildConfig.DEBUG) {
            BookApplication.getRefWatcher().watch(this)
        }

        ReadState.chapterList.clear()
//        DataProvider.getInstance().clear()

//        PlatformSDK.lifecycle().onDestroy()
        DataProvider.getInstance().readingActivity = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mReadPresenter.onSaveInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(mReadPresenter != null && resultCode == Activity.RESULT_OK ){
            mReadPresenter.onActivityResult(requestCode, resultCode, data)
        }
    }



    override fun setAutoSpeed(autoReadSpeed: Double) = readerWidget.setAutoReadSpeed(autoReadSpeed)

    override fun autoStop() {
        readerWidget.stopAutoRead()
        showStopAutoHint()
    }

    //ReadSettingView start
    override fun onReadCatalog() {
        showMenu(false)
        mReadPresenter.onReadCatalog()
        read_catalog_mark_drawer.openDrawer(GravityCompat.START)
    }

    override fun onReadChangeSource() = mReadPresenter.onReadChangeSource()

    override fun onReadCache() = mReadPresenter.onReadCache()

    /**
     * 无NavigationBar, 直接开启自动阅读
     * 有NavigationBar, 在NavigationBar隐藏监听里，开启自动阅读
     * @see setNavigationBarListener
     */
    override fun onReadAuto() {
        isReadAutoOpening = true
        showMenu(false)
        if (!isNavigationBarShow()) {
            isReadAutoOpening = false
            readerWidget.startAutoRead()
        }
    }

    override fun onChangeMode(mode: Int) {
        mReadPresenter.onChangeMode(mode)
    }

    override fun onChangeScreenMode() {
        mReadPresenter.changeScreenMode()
        showMenu(false)
    }

    //目录跳章
    override fun onJumpChapter(sequence: Int, offset: Int) {
        ReadState.sequence = sequence
        ReadState.currentPage = 0
        ReadState.offset = offset
        ReadConfig.jump = true
        read_catalog_mark_drawer.closeDrawers()
        mReadPresenter.onJumpChapter(sequence)
    }

    //上一章
    override fun onJumpPreChapter() {
        if (ReadState.sequence == 0) {
            showToastShort(net.lzbook.kit.R.string.is_first_chapter)
            return
        }
        ReadState.sequence--
        ReadState.currentPage = 0
        ReadState.offset = 0
        ReadConfig.jump = true
        mReadPresenter.onJumpPreChapter()
    }

    //下一章
    override fun onJumpNextChapter() {
        if (ReadState.book?.book_type != 0) {
            showToastShort(net.lzbook.kit.R.string.last_chapter_tip)
            return
        }
        ReadState.sequence++
        ReadState.offset = 0
        ReadState.currentPage = 0
        ReadConfig.jump = true
        mReadPresenter.onJumpNextChapter()
    }

    override fun onReadFeedBack() = mReadPresenter.onReadFeedBack()

    override fun onChageNightMode() {
        mReadPresenter.onChageNightMode()
    }

    //0 滑动 1 仿真 2 平移 3 上下
    override fun changeAnimMode(mode: Int) {

        readerWidget.changeAnimMode(mode)
//        showMenu(false)
    }

    fun goBackToHome() = mReadPresenter.goBackToHome()

    override fun initPresenter(optionPresenter: ReadOptionPresenter?, markPresenter: CatalogMarkPresenter?) {
        mCatalogMarkFragment?.presenter = markPresenter
        option_header.presenter = optionPresenter
    }

    override fun full(isFull: Boolean) {
        if (isFull) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
        }
    }

    override fun initSettingGuide() {
        if (!mSharedPreferencesUtils.getBoolean(mReadPresenter.versionCode.toString() + Constants
                .READING_SETING_GUIDE_TAG)) {
            ll_guide_layout.visibility = View.VISIBLE
            iv_guide_setting_bookmark.visibility = View.VISIBLE
            iv_guide_setting_chapter.visibility = View.VISIBLE
            ll_guide_layout.setOnClickListener {
                mSharedPreferencesUtils.putBoolean(mReadPresenter.versionCode.toString() + Constants.READING_SETING_GUIDE_TAG, true)
                ll_guide_layout.visibility = View.GONE
            }
        }
    }

    override fun setMode() = readSettingView.setMode()

    override fun showAutoMenu(isShow: Boolean) = if (isShow) auto_menu?.visibility = View.VISIBLE else auto_menu?.visibility = View.GONE

    override fun initShowCacheState() = readSettingView.initShowCacheState()

    override fun changeChapter() = readSettingView.changeChapter()

    override fun checkModeChange() = if (isModeChange) setMode() else Unit

    public override fun restoreBrightness() {
        super.restoreBrightness()
    }

    public override fun setReaderDisplayBrightness() {
        super.setReaderDisplayBrightness()
    }

    override fun getAutoMenuShowState(): Boolean = auto_menu.isShown

    override fun showStopAutoHint() {
//        val view = View.inflate(this, R.layout.autoread_textview, null) as TextView
//        val toast = Toast(applicationContext)
//        toast.view = view
//        toast.duration = Toast.LENGTH_SHORT
//        toast.setGravity(Gravity.CENTER, 0, 0)
//        toast.show()
        ToastUtils.showToastNoRepeat("已退出自动阅读")
        auto_menu.visibility = View.GONE
    }

    private val immersiveRunable = Runnable { setUIOptions() }

    override fun showMenu(isShow: Boolean) {
        if (ReadState.isMenuShow != isShow) {
            ReadState.isMenuShow = isShow
//            if (ReadConfig.animation != ReadViewEnums.Animation.list) {
            if (isShow) {
                window.decorView.handler.removeCallbacks(immersiveRunable)
                window.decorView.systemUiVisibility = FrameActivity.UI_OPTIONS_NORMAL
            } else {
                window.decorView.systemUiVisibility = FrameActivity.UI_OPTIONS_IMMERSIVE_STICKY
            }
//            } else {
//                window.decorView.systemUiVisibility = FrameActivity.UI_OPTIONS_LOW_PROFILE
//            }


            readSettingView.showMenu(isShow)
            mReadPresenter.showMenu(isShow)
        }
    }

    fun showChangeSourceDialog() {
        mReadPresenter.changeSource()
    }

    override fun goToBookOver() {
        mReadPresenter.goToBookOver()
    }

    override fun onOriginClick() {
        mReadPresenter.onOriginClick()
    }

    override fun onTransCodingClick() {
        mReadPresenter.onTransCodingClick()
    }

    override fun onAutoReadResume() {
        auto_menu.visibility = View.GONE
    }

    override fun onAutoReadStop() {
        auto_menu.visibility = View.VISIBLE
    }

    var oldchapterId = ""
    override fun addLog() {
        val endTime = System.currentTimeMillis()
        val bookId = book.book_id
        val chapterId = ReadState.chapterId
        val sourceIds = if (Constants.QG_SOURCE == book.site) book.book_id else book.book_source_id
        val channelCode = if (Constants.QG_SOURCE == book.site) "1" else "2"
        val pageCount = ReadState.pageCount.toString()
        val currentPage = (ReadState.currentPage - 1).toString()
        val currentPageContentLength = ReadState.contentLength.toString()
        //按照此顺序传值 当前的book_id，阅读章节，书籍源，章节总页数，当前阅读页，当前页总字数，当前页面来自，开始阅读时间,结束时间,阅读时间,是否有阅读中间退出行为,书籍来源1为青果，2为智能
        StartLogClickUtil.upLoadReadContent(bookId, chapterId, sourceIds, pageCount, currentPage, currentPageContentLength, "2",
                startReadTime.toString(), endTime.toString(), (endTime - startReadTime).toString(), "false", channelCode)
        chapterId?.let {
            if (oldchapterId != it) {
                oldchapterId = it
                sendPVData(bookId, chapterId, sourceIds, channelCode, pageCount)
            }
        }
    }

    private fun sendPVData(bookId: String, chapterId: String?, sourceIds: String, channelCode: String, pageCount: String) {
        Constants.endReadTime = System.currentTimeMillis() / 1000L
        val params = HashMap<String, String>()
        params.put("book_id", bookId)
        params.put("book_source_id", sourceIds)
        params.put("chapter_id", chapterId.toString())
        params.put("channel_code", channelCode)
        params.put("chapter_read", "1")
        params.put("chapter_pages", pageCount)
        params.put("start_time", (startReadTime / 1000L).toString() )
        params.put("end_time", Constants.endReadTime.toString())
        params.put("udid", OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext()))
        params.put("app_package", AppUtils.getPackageName())
        params.put("app_version", AppUtils.getVersionName())
        params.put("app_version_code", AppUtils.getVersionCode().toString())
        params.put("app_channel_id", AppUtils.getChannelId())
        LogEncapManager.getInstance().sendLog(params, "zn_pv")
    }

    private fun getRealScreenSize(): Point {
        val display = windowManager.defaultDisplay
        val displayMetrics = DisplayMetrics()
        val realSize = Point()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealMetrics(displayMetrics)
        } else {
            val c: Class<*>
            try {
                c = Class.forName("android.view.Display")
                val method: Method = c.getMethod("getRealMetrics", DisplayMetrics::class.java)
                method.invoke(display, displayMetrics)
            } catch (e: Exception) {
                displayMetrics.setToDefaults()
                e.printStackTrace()
            }
        }

        realSize.x = displayMetrics.widthPixels
        realSize.y = displayMetrics.heightPixels

        return realSize
    }

    private fun setNavigationBarListener() {
        //NavigationBar显示及收起监听
        val content = findViewById(Window.ID_ANDROID_CONTENT)
        content.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom > oldBottom || right > oldRight) { // NavigationBar收起
                if (isReadAutoOpening) { //开启自动阅读
                    AppLog.e("ReadingActivity", "开始自动阅读")
                    isReadAutoOpening = false
                    readerWidget.startAutoRead()
                }
            }
        }
    }

    private fun isNavigationBarShow(): Boolean {
        val displayMetrics = DisplayMetrics()
        val display = windowManager.defaultDisplay
        display.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        val realSize = getRealScreenSize()
        return realSize.x > width || realSize.y > height
    }

    override fun readOptionHeaderDismiss() {
        option_header.dismissLoadingPage()
    }

    override fun supportSlideBack(): Boolean {
        return false
    }
}
