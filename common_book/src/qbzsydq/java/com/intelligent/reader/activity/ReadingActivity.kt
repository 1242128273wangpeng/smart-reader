package com.intelligent.reader.activity

import android.content.Intent
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
import com.intelligent.reader.read.help.IReadPageChange
import com.intelligent.reader.read.mode.ReadInfo
import com.intelligent.reader.read.mode.ReadViewEnums
import com.intelligent.reader.read.page.*
import com.intelligent.reader.reader.ReaderViewModel
import iyouqu.theme.FrameActivity
import net.lzbook.kit.book.component.service.DownloadService
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.NovelLineBean
import net.lzbook.kit.data.bean.ReadStatus
import net.lzbook.kit.data.bean.Source
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.SharedPreferencesUtils
import java.util.*


/**
 * ReadingActivity
 * 小说阅读页
 */
class ReadingActivity : BaseCacheableActivity(), AutoReadMenu.OnAutoMemuListener, ReadSettingView.OnReadSettingListener, ReadPreInterface.View, IReadPageChange {

    private val mTAG = ReadingActivity::class.java.simpleName
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
        mReadPresenter ?: BaseReadPresenter(this).onConfigurationChanged(mCatalogMarkFragment!!, mOptionHeader!!)
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
        readSettingView?.setDataFactory(fac, readStatus!!, mThemeHelper)
        readSettingView?.currentThemeMode = mReadPresenter?.currentThemeMode
        auto_menu = findViewById(R.id.auto_menu) as AutoReadMenu
        auto_menu?.setOnAutoMemuListener(this)

        ll_guide_layout = findViewById(R.id.ll_guide_layout)
        initGuide()

        readSettingView?.setNovelMode(Constants.MODE)
        readStatus?.source_ids = readStatus?.book?.site
        //add ReadInfo
        novel_basePageView?.initReaderViewFactory()
        novel_basePageView?.entrance(ReadInfo(readStatus?.book!!,readStatus!!, ReadViewEnums.Animation.slide))
        novel_basePageView?.setIReadPageChange(this)
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

    public override fun setReaderDisplayBrightness() =  super.setReaderDisplayBrightness()

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
        return when(mReadPresenter?.dispatchKeyEvent(event)){
            true -> true
            else -> super.dispatchKeyEvent(event)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when(keyCode == KeyEvent.KEYCODE_MENU){
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
    }

    override fun shouldReceiveCacheEvent(): Boolean = false

    override fun shouldShowNightShadow(): Boolean = false

    override fun onStart() {
        super.onStart()
        mReadPresenter?.onStart()
    }

    override fun onPause() {
        super.onPause()
        mReadPresenter?.onPause()
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
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (mReadPresenter?.onSaveInstanceState(outState) != null) super.onSaveInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) = mReadPresenter?.onActivityResult(requestCode, resultCode, data)!!

    override fun speedUp() = mReadPresenter?.speedUp()!!

    override fun speedDown() = mReadPresenter?.speedDown()!!

    override fun autoStop() = mReadPresenter?.autoStop()!!

    override fun onReadCatalog() {
        mReadPresenter?.onReadCatalog()
        mCatlogMarkDrawer?.openDrawer(GravityCompat.START)
    }

    override fun onReadChangeSource() = mReadPresenter?.onReadChangeSource()!!

    override fun onReadCache() = mReadPresenter?.onReadCache()!!

    override fun onReadAuto() = mReadPresenter?.onReadAuto()!!

    override fun onChangeMode(mode: Int) = mReadPresenter?.onChangeMode(mode)!!

    override fun onChangeScreenMode() = mReadPresenter?.changeScreenMode()!!

    override fun onRedrawPage() = mReadPresenter?.onRedrawPage()!!

    override fun onJumpChapter() = mReadPresenter?.onJumpChapter()!!

    override fun onJumpPreChapter() = mReadPresenter?.onJumpPreChapter()!!

    override fun onJumpNextChapter() = mReadPresenter?.onJumpNextChapter()!!

    override fun onReadFeedBack() = mReadPresenter?.onReadFeedBack()!!

    override fun onChageNightMode() = mReadPresenter?.onChageNightMode()!!

    fun goBackToHome() = mReadPresenter?.goBackToHome()

    override fun initPresenter(optionPresenter: ReadOptionPresenter?, markPresenter: CatalogMarkPresenter?) {
        mCatalogMarkFragment?.presenter = markPresenter
        mOptionHeader?.presenter = optionPresenter
    }

    override fun setReadStatus(readSta: ReadStatus) {
        readStatus = readSta
    }

    fun showMenu(isShow: Boolean) = mReadPresenter?.showMenu(isShow)!!

    override fun showSetMenu(isShow: Boolean) = readSettingView?.showSetMenu(isShow)!!

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

    override fun setMode() = readSettingView?.setMode()!!

    override fun showAutoMenu(isShow: Boolean) {
        when (isShow) {
            true -> auto_menu?.visibility = View.VISIBLE
            false -> auto_menu?.visibility = View.GONE
        }
    }

    override fun resetPageView(pageView: PageInterface) {
        novel_basePageView?.removeAllViews()
        novel_basePageView?.addView(pageView as View, FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    override fun initShowCacheState() = readSettingView?.initShowCacheState()!!

    override fun changeChapter() = readSettingView?.changeChapter()!!

    override fun checkModeChange() {
        if (isModeChange) setMode()
    }

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
        novel_basePageView?.setLoadChapter(what,chapter,chapterList)
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
        private var readStatus: ReadStatus? = null
    }

    //IReadPageChange
    override fun onLoadChapter(type: ReadViewEnums.MsgType, sequence: Int, isShowLoadPage: Boolean,pageIndex:ReadViewEnums.PageIndex) {
        mReadPresenter?.onLoadChapter(type.Msg,sequence,isShowLoadPage,pageIndex)
    }

    override fun showMenu() {

    }

    override fun loadAD() {

    }
}
