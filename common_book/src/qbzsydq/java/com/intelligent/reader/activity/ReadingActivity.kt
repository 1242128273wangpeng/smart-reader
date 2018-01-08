package com.intelligent.reader.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.view.*
import android.view.ViewGroup.LayoutParams
import android.widget.*
import com.intelligent.reader.R
import com.intelligent.reader.fragment.CatalogMarkFragment
import com.intelligent.reader.presenter.read.*
import com.intelligent.reader.read.DataProvider
import com.intelligent.reader.read.help.IReadPageChange
import com.intelligent.reader.read.help.ReadSeparateHelper
import net.lzbook.kit.data.bean.ReadConfig
import com.intelligent.reader.read.mode.ReadInfo
import com.intelligent.reader.read.mode.ReadState
import net.lzbook.kit.data.bean.ReadViewEnums
import com.intelligent.reader.read.page.*
import com.intelligent.reader.reader.ReaderViewModel
import iyouqu.theme.FrameActivity
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.component.service.DownloadService
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.*
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.SharedPreferencesUtils
import java.util.*
import kotlin.properties.Delegates


/**
 * ReadingActivity
 * 小说阅读页
 */
class ReadingActivity : BaseCacheableActivity(), AutoReadMenu.OnAutoMemuListener, ReadSettingView.OnReadSettingListener, ReadPreInterface.View, IReadPageChange {

    private val mTAG = ReadingActivity::class.java.simpleName
    private var batteryPercent: Float = 0.toFloat()
    var downloadService: DownloadService? = null
    private var pageView: PageInterface? = null
    // 系统存储设置
    private var auto_menu: AutoReadMenu? = null
    private var readSettingView: ReadSettingView? = null
    private var ll_guide_layout: View? = null
    private var sharedPreferencesUtils: SharedPreferencesUtils? = null
    private var reading_content: RelativeLayout? = null

    private var novel_basePageView: ReaderViewWidget? = null
    private var mCatlogMarkDrawer: DrawerLayout? = null
    private var mCatalogMarkFragment: CatalogMarkFragment? = null
    private val startReadTime = System.currentTimeMillis()

    private val mDrawerListener = object : DrawerLayout.DrawerListener {
        override fun onDrawerSlide(drawerView: View, slideOffset: Float) = Unit
        //解锁， 可滑动关闭
        override fun onDrawerOpened(drawerView: View) = mCatlogMarkDrawer?.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNDEFINED)!!

        //锁定不可滑出
        override fun onDrawerClosed(drawerView: View) = mCatlogMarkDrawer?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)!!

        override fun onDrawerStateChanged(newState: Int) = Unit
    }
    private var mReadPresenter: ReadPresenter? = null
    private var mOptionHeader: ReadOptionHeader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLog.e(mTAG, "onCreate")
        window.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        window.decorView.systemUiVisibility = FrameActivity.UI_OPTIONS_IMMERSIVE_STICKY
        initReadPresenter()
        mReadPresenter?.onCreateInit(savedInstanceState)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        initReadPresenter()
        mReadPresenter ?: BaseReadPresenter(this).onNewIntent(intent)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (mCatlogMarkDrawer == null) {
            setContentView(R.layout.act_read)
        }
        mCatlogMarkDrawer = findViewById(R.id.read_catalog_mark_drawer) as DrawerLayout
        mCatlogMarkDrawer?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        mCatlogMarkDrawer?.addDrawerListener(mDrawerListener)
        mCatalogMarkFragment = supportFragmentManager.findFragmentById(R.id.read_catalog_mark_layout) as CatalogMarkFragment
        mCatlogMarkDrawer?.addDrawerListener(mCatalogMarkFragment!!)
        mOptionHeader = findViewById(R.id.option_header) as ReadOptionHeader
        mReadPresenter?.onConfigurationChanged(mCatalogMarkFragment!!, mOptionHeader!!)
        // 注册一个电量广播
        registerReceiver(mBatInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
//        pageView?.freshBattery(batteryPercent)
    }

    override fun initView(fac: ReaderViewModel) {
        reading_content = findViewById(R.id.reading_content) as RelativeLayout
        readSettingView = findViewById(R.id.readSettingView) as ReadSettingView
        readSettingView?.setOnReadSettingListener(this)
        novel_basePageView = findViewById(R.id.novel_basePageView) as ReaderViewWidget
        pageView = if (Constants.isSlideUp) {
            ScrollPageView(applicationContext)
        } else {
            PageView(applicationContext)
        }
        novel_basePageView?.removeAllViews()
//        novel_basePageView?.addView(pageView as View?, FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
//                LayoutParams.MATCH_PARENT))

        mReadPresenter?.initData(pageView!!)
        readSettingView?.setDataFactory(fac, readStatus, mThemeHelper)
        readSettingView?.currentThemeMode = mReadPresenter?.currentThemeMode
        auto_menu = findViewById(R.id.auto_menu) as AutoReadMenu
        auto_menu?.setOnAutoMemuListener(this)

        ll_guide_layout = findViewById(R.id.ll_guide_layout)
        initGuide()

        readStatus.source_ids = readStatus.book?.site
        DataProvider.getInstance().context = this
        //add ReadInfo
        ReadConfig.animation = when (Constants.PAGE_MODE) {
            1 -> ReadViewEnums.Animation.curl
            2 -> ReadViewEnums.Animation.shift
            3 -> ReadViewEnums.Animation.list
            else -> ReadViewEnums.Animation.slide
        }
        novel_basePageView?.initReaderViewFactory()
        novel_basePageView?.entrance(ReadInfo(readStatus.book!!, readStatus, ReadConfig.animation))
        novel_basePageView?.setIReadPageChange(this)
        readSettingView?.setNovelMode(Constants.MODE)
    }

    private fun initReadPresenter() {
        if (mReadPresenter == null) mReadPresenter = ReadPresenter(this)
    }

    private fun initGuide() {
        sharedPreferencesUtils = SharedPreferencesUtils(PreferenceManager.getDefaultSharedPreferences(applicationContext))
        if (!sharedPreferencesUtils!!.getBoolean(mReadPresenter!!.versionCode.toString() + Constants.READING_GUIDE_TAG)) {
            val iv_guide_reading = findViewById(R.id.iv_guide_reading) as ImageView
            ll_guide_layout!!.visibility = View.VISIBLE
            iv_guide_reading.visibility = View.VISIBLE
            ll_guide_layout!!.setOnClickListener {
                sharedPreferencesUtils!!.putBoolean(mReadPresenter!!.versionCode.toString() + Constants.READING_GUIDE_TAG, true)
                iv_guide_reading.visibility = View.GONE
                ll_guide_layout!!.visibility = View.GONE
            }
        }
    }

    fun freshPage() = mReadPresenter?.freshPage()

    //自动阅读
    fun dealManualDialogShow() = mReadPresenter?.dealManualDialogShow()

    fun searchChapterCallBack(sourcesList: ArrayList<Source>) = mReadPresenter?.searchChapterCallBack(sourcesList)

    public override fun restoreBrightness() = super.restoreBrightness()

    public override fun setReaderDisplayBrightness() = super.setReaderDisplayBrightness()

    fun changeSourceCallBack() = mReadPresenter?.changeSourceCallBack()

    /**
     * 跳章
     */
    fun jumpChapterCallBack() = mReadPresenter?.jumpChapterCallBack()

    /**
     * 隐藏topmenu
     */
    fun dismissTopMenu() = mReadPresenter?.dismissTopMenu()

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return when (mReadPresenter?.dispatchKeyEvent(event)) {
            true -> true
            else -> super.dispatchKeyEvent(event)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode == KeyEvent.KEYCODE_MENU) {
            true -> {
                mReadPresenter?.onKeyDown()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onBackPressed() {
        if (mCatlogMarkDrawer!!.isDrawerOpen(GravityCompat.START)) {
            mCatlogMarkDrawer?.closeDrawers()
            return
        }
        val isFinish = mReadPresenter?.onBackPressed() ?: false
        if (isFinish && !isFinishing) {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        AppLog.d("ReadingActivity", "onResume:" + Constants.isFullWindowRead)
        // 设置全屏
        when (!Constants.isFullWindowRead) {
            true -> window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
            false -> window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        mReadPresenter?.onResume()
        // 注册一个电量广播类型
        registerReceiver(mBatInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        novel_basePageView?.onResume()
    }

    override fun shouldReceiveCacheEvent(): Boolean = false

    override fun shouldShowNightShadow(): Boolean = false

    override fun onStart() {
        super.onStart()
        mReadPresenter?.initTime()
        mReadPresenter?.onStart()
    }

    override fun onPause() {
        super.onPause()
        mReadPresenter?.onPause(ReadState.sequence,ReadState.offset)
        novel_basePageView?.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        mReadPresenter?.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.postDelayed({ window.decorView.systemUiVisibility = FrameActivity.UI_OPTIONS_IMMERSIVE_STICKY }, 1500)
        }
    }

    override fun onStop() {
        super.onStop()
        mReadPresenter?.onStop()
    }

    override fun onDestroy() {
        mCatlogMarkDrawer?.removeDrawerListener(mDrawerListener)
        mCatlogMarkDrawer?.removeDrawerListener(mCatalogMarkFragment!!)

        AppLog.e(mTAG, "onDestroy")

        novel_basePageView?.removeAllViews()
        novel_basePageView = null

        readSettingView?.recycleResource()
        readSettingView = null

        auto_menu?.setOnAutoMemuListener(null)
        auto_menu?.recycleResource()
        auto_menu = null

        reading_content?.removeAllViews()
        reading_content = null
        ll_guide_layout = null

        mReadPresenter?.onDestroy()
        DataProvider.getInstance().unSubscribe()
        DataProvider.getInstance().chapterMap.clear()
        DataProvider.getInstance().chapterSeparate.clear()
        try {
            unregisterReceiver(mBatInfoReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) = if (mReadPresenter?.onSaveInstanceState(outState) != null) super.onSaveInstanceState(outState) else Unit

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) = mReadPresenter?.onActivityResult(requestCode, resultCode, data)?:Unit

    override fun speedUp() = mReadPresenter?.speedUp()?:Unit

    override fun speedDown() = mReadPresenter?.speedDown()?:Unit

    override fun autoStop() = mReadPresenter?.autoStop()?:Unit
    //ReadSettingView start
    override fun onReadCatalog() {
        mReadPresenter?.onReadCatalog()
        mCatlogMarkDrawer?.openDrawer(GravityCompat.START)
    }

    override fun onReadChangeSource() = mReadPresenter?.onReadChangeSource()?:Unit

    override fun onReadCache() = mReadPresenter?.onReadCache()?:Unit

    override fun onReadAuto() = mReadPresenter?.onReadAuto()?:Unit

    override fun onChangeMode(mode: Int) {
        mReadPresenter?.onChangeMode(mode)
        novel_basePageView?.setBackground()
    }

    override fun onChangeScreenMode() = mReadPresenter?.changeScreenMode()?:Unit

    override fun onRedrawPage() = novel_basePageView?.onRedrawPage()?:Unit

    override fun onJumpChapter() = Unit
    //目录跳章
    fun onJumpChapter(intent: Intent) {
        readStatus.sequence = intent.getIntExtra("sequence", 0)
        readStatus.book = intent.getSerializableExtra("book") as Book?
//        novel_basePageView?.entrance(ReadInfo(readStatus.book!!, readStatus, animation))
        ReadState.sequence = intent.getIntExtra("sequence", 0)
        novel_basePageView?.onJumpChapter(ReadState.sequence)
        mCatlogMarkDrawer?.closeDrawers()
    }
    //上一章
    override fun onJumpPreChapter() {
        if (ReadState.sequence == 0) {
            showToastShort(net.lzbook.kit.R.string.is_first_chapter)
            return
        }
        mReadPresenter?.onJumpPreChapter()?:Unit
//        readStatus.currentPage = 1
//        readStatus.offset = 0
//        readStatus.sequence--
//        novel_basePageView?.entrance(ReadInfo(readStatus.book!!, readStatus, animation))
        novel_basePageView?.onJumpChapter(--ReadState.sequence)
    }
    //下一章
    override fun onJumpNextChapter() {
        if (readStatus.book?.book_type != 0) {
            showToastShort(net.lzbook.kit.R.string.last_chapter_tip)
            return
        }
        mReadPresenter?.onJumpNextChapter()!!
        readStatus.currentPage = 1
        readStatus.offset = 0
        readStatus.sequence++
//        novel_basePageView?.entrance(ReadInfo(readStatus.book!!, readStatus, animation))
        novel_basePageView?.onJumpChapter(++ReadState.sequence)
    }

    override fun onReadFeedBack() = mReadPresenter?.onReadFeedBack()?:Unit

    override fun onChageNightMode() = mReadPresenter?.onChageNightMode()?:Unit
    //0 滑动 1 仿真 2 平移 3 上下
    override fun changeAnimMode(mode: Int) {
        if (((mode == 3) and (ReadConfig.animation != ReadViewEnums.Animation.list)) or ((ReadConfig.animation == ReadViewEnums.Animation.list) and (mode != 3))) {
            novel_basePageView?.changeAnimMode(mode)
            novel_basePageView?.entrance(ReadInfo(readStatus.book, readStatus, ReadConfig.animation))
            novel_basePageView?.setIReadPageChange(this)
        }
        novel_basePageView?.changeAnimMode(mode)
    }

    //ReadSettingView end
    fun goBackToHome() = mReadPresenter?.goBackToHome()

    override fun initPresenter(optionPresenter: ReadOptionPresenter?, markPresenter: CatalogMarkPresenter?) {
        mCatalogMarkFragment?.presenter = markPresenter
        mOptionHeader?.presenter = optionPresenter
    }

    override fun setReadStatus(readSta: ReadStatus) {
        readStatus = readSta
    }

    override fun showSetMenu(isShow: Boolean) = readSettingView?.showSetMenu(isShow)?:Unit

    override fun full(isFull: Boolean) {
        if (isFull) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
        }
    }

    override fun initSettingGuide() {
        if (sharedPreferencesUtils != null && sharedPreferencesUtils?.getBoolean(mReadPresenter?.versionCode.toString() + Constants
                .READING_SETING_GUIDE_TAG) == false) {
            ll_guide_layout?.visibility = View.VISIBLE
            val iv_guide_setting_bookmark = findViewById(R.id.iv_guide_setting_bookmark) as ImageView
            iv_guide_setting_bookmark.visibility = View.VISIBLE
            ll_guide_layout?.setOnClickListener {
                sharedPreferencesUtils?.putBoolean(mReadPresenter?.versionCode.toString() + Constants.READING_SETING_GUIDE_TAG, true)
                ll_guide_layout?.visibility = View.GONE
            }
        }
    }

    override fun setMode() = readSettingView?.setMode()?:Unit

    override fun showAutoMenu(isShow: Boolean) = if (isShow) auto_menu?.visibility = View.VISIBLE else auto_menu?.visibility = View.GONE

    override fun resetPageView(pageView: PageInterface) {
        novel_basePageView?.removeAllViews()
        novel_basePageView?.addView(pageView as View, FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    override fun initShowCacheState() = readSettingView?.initShowCacheState()?:Unit

    override fun changeChapter() = readSettingView?.changeChapter()?:Unit

    override fun checkModeChange() =  if (isModeChange) setMode() else Unit

    override fun getAutoMenuShowState(): Boolean = auto_menu?.isShown ?: false

    override fun showStopAutoHint() {
        val view = View.inflate(this, R.layout.autoread_textview, null) as TextView
        val toast = Toast(applicationContext)
        toast.view = view
        toast.duration = Toast.LENGTH_SHORT
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
        auto_menu?.visibility = View.GONE
    }

    override fun initCatlogView() {
        setContentView(R.layout.act_read)
        mCatlogMarkDrawer = findViewById(R.id.read_catalog_mark_drawer) as DrawerLayout
        mCatlogMarkDrawer?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        mCatlogMarkDrawer?.addDrawerListener(mDrawerListener)
        mCatalogMarkFragment = supportFragmentManager.findFragmentById(R.id.read_catalog_mark_layout) as CatalogMarkFragment
        mOptionHeader = findViewById(R.id.option_header) as ReadOptionHeader
        mCatlogMarkDrawer?.addDrawerListener(mCatalogMarkFragment!!)
        mReadPresenter?.initCatalogPresenter(mCatalogMarkFragment!!, mOptionHeader!!)
    }

    override fun onNewInitView(): Boolean {
        mCatlogMarkDrawer = findViewById(R.id.read_catalog_mark_drawer) as DrawerLayout?
        if (mCatlogMarkDrawer == null || mCatalogMarkFragment == null || mOptionHeader == null) {
            finish()
            return false
        }
        mCatlogMarkDrawer?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        mCatlogMarkDrawer?.addDrawerListener(mDrawerListener)
        mCatalogMarkFragment = supportFragmentManager.findFragmentById(R.id.read_catalog_mark_layout) as CatalogMarkFragment
        mOptionHeader = findViewById(R.id.option_header) as ReadOptionHeader
        mCatlogMarkDrawer?.addDrawerListener(mCatalogMarkFragment!!)
        mReadPresenter?.initCatalogPresenter(mCatalogMarkFragment!!, mOptionHeader!!)
        return true
    }

    override fun loadChapterSuccess(what: Int, chapter: Chapter, chapterList: ArrayList<ArrayList<NovelLineBean>>) {
        novel_basePageView?.setLoadChapter(what, chapter, chapterList)
    }

    companion object {
        val MSG_LOAD_CUR_CHAPTER = 0
        val MSG_LOAD_PRE_CHAPTER = 1
        val MSG_LOAD_NEXT_CHAPTER = 2
        val MSG_SEARCH_CHAPTER = 3
        val MSG_CHANGE_SOURCE = 4
        val MSG_JUMP_CHAPTER = 6
        val ERROR = 7
        val NEED_LOGIN = 8
        private var readStatus: ReadStatus by Delegates.notNull<ReadStatus>()
    }

    //IReadPageChange
    override fun onLoadChapter(type: ReadViewEnums.MsgType, sequence: Int, isShowLoadPage: Boolean, pageIndex: ReadViewEnums.PageIndex)= mReadPresenter?.onLoadChapter(type.Msg, sequence, isShowLoadPage, pageIndex)?:Unit

    override fun showMenu(isShow: Boolean) = mReadPresenter?.showMenu(isShow)?:Unit

    override fun goToBookOver() = mReadPresenter?.goToBookOver()?:Unit

    override fun onOriginClick() = mReadPresenter?.onOriginClick()?:Unit

    override fun onTransCodingClick() = mReadPresenter?.onTransCodingClick()?:Unit

    override fun addLog() {
        val book = intent.getSerializableExtra("book") as Book
        val endTime = System.currentTimeMillis()
        val bookId = book.book_id
        val chapterId = DataProvider.getInstance().chapterMap[ReadState.sequence]?.chapter_id
        val sourceIds = book.book_source_id
        val channelCode = when(book.site) {
            Constants.QG_SOURCE -> "1"
            else -> "2"
        }
        val pageCount = ReadState.pageCount.toString()
        val currentPage = (ReadState.currentPage -1).toString()
        val currentPageContentLength = ReadState.contentLength.toString()
        //按照此顺序传值 当前的book_id，阅读章节，书籍源，章节总页数，当前阅读页，当前页总字数，当前页面来自，开始阅读时间,结束时间,阅读时间,是否有阅读中间退出行为,书籍来源1为青果，2为智能
        StartLogClickUtil.upLoadReadContent(bookId,chapterId, sourceIds, pageCount,currentPage,currentPageContentLength, "2",
                startReadTime.toString() , endTime.toString(), (endTime - startReadTime).toString(), "false", channelCode)
    }

    override fun freshTime(time_text: CharSequence?) = novel_basePageView?.freshTime(time_text)?:Unit

    override fun setBackground() = novel_basePageView?.setBackground()?:Unit

    //广播
    /**
     * 接受电量改变广播
     */
    private val mBatInfoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra("level", 0)
                val scale = intent.getIntExtra("scale", 100)
                batteryPercent = level.toFloat() / scale.toFloat()
                novel_basePageView?.freshBattery(batteryPercent)
            }
        }
    }
}
