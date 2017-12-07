package com.intelligent.reader.activity

import com.intelligent.reader.R
import com.intelligent.reader.fragment.CatalogMarkFragment
import com.intelligent.reader.presenter.read.CatalogMarkPresenter
import com.intelligent.reader.presenter.read.ReadOptionPresenter
import com.intelligent.reader.presenter.read.ReadPreInterface
import com.intelligent.reader.presenter.read.ReadPresenter
import com.intelligent.reader.read.help.IReadDataFactory
import com.intelligent.reader.read.page.AutoReadMenu
import com.intelligent.reader.read.page.PageInterface
import com.intelligent.reader.read.page.PageView
import com.intelligent.reader.read.page.ReadOptionHeader
import com.intelligent.reader.read.page.ReadSettingView
import com.intelligent.reader.read.page.ScrollPageView

import net.lzbook.kit.book.component.service.DownloadService
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.ReadStatus
import net.lzbook.kit.data.bean.Source
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.SharedPreferencesUtils

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import iyouqu.theme.FrameActivity

import java.util.ArrayList

/**
 * ReadingActivity
 * 小说阅读页
 */

class ReadingActivity : BaseCacheableActivity(), AutoReadMenu.OnAutoMemuListener, ReadSettingView.OnReadSettingListener, ReadPreInterface.View {
    private val mTAG = ReadingActivity::class.java.simpleName
    var downloadService: DownloadService? = null
    internal var readLength = 0
    private var pageView: PageInterface? = null
    // 系统存储设置
    private var auto_menu: AutoReadMenu? = null
    private var inflater: LayoutInflater? = null
    private var readSettingView: ReadSettingView? = null
    private var ll_guide_layout: View? = null
    private var sharedPreferencesUtils: SharedPreferencesUtils? = null
    private var reading_content: RelativeLayout? = null

    private var novel_basePageView: FrameLayout? = null
    private var mCatlogMarkDrawer: DrawerLayout? = null
    private var mCatalogMarkFragment: CatalogMarkFragment? = null
    private val mDrawerListener = object : DrawerLayout.DrawerListener {
        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

        }

        override fun onDrawerOpened(drawerView: View) {
            //解锁， 可滑动关闭
            if (mCatlogMarkDrawer != null) {
                mCatlogMarkDrawer!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNDEFINED)
            }
        }

        override fun onDrawerClosed(drawerView: View) {
            //锁定不可滑出
            if (mCatlogMarkDrawer != null) {
                mCatlogMarkDrawer!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }

        override fun onDrawerStateChanged(newState: Int) {

        }
    }
    private var mReadPresenter: ReadPresenter? = null
    private var mOptionHeader: ReadOptionHeader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLog.e(mTAG, "onCreate")
        window.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        window.decorView.systemUiVisibility = FrameActivity.UI_OPTIONS_IMMERSIVE_STICKY
        inflater = LayoutInflater.from(applicationContext)

        mReadPresenter = ReadPresenter(this)
        mReadPresenter!!.onCreateInit(savedInstanceState)

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        inflater = LayoutInflater.from(applicationContext)

        if (mReadPresenter == null) {
            mReadPresenter = ReadPresenter(this)
        }
        mReadPresenter!!.onNewIntent(intent)

    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (mCatlogMarkDrawer == null) {
            setContentView(R.layout.act_read)
        }
        mCatlogMarkDrawer = findViewById(R.id.read_catalog_mark_drawer) as DrawerLayout

        mCatlogMarkDrawer!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        mCatlogMarkDrawer!!.addDrawerListener(mDrawerListener)
        mCatalogMarkFragment = supportFragmentManager.findFragmentById(R.id.read_catalog_mark_layout) as CatalogMarkFragment
        mCatlogMarkDrawer!!.addDrawerListener(mCatalogMarkFragment!!)
        mOptionHeader = findViewById(R.id.option_header) as ReadOptionHeader

        if (mReadPresenter == null) {
            mReadPresenter = ReadPresenter(this)
        }
        mReadPresenter!!.onConfigurationChanged(mCatalogMarkFragment!!, mOptionHeader!!)
    }

    override fun initView(fac: IReadDataFactory) {
        reading_content = findViewById(R.id.reading_content) as RelativeLayout
        readSettingView = findViewById(R.id.readSettingView) as ReadSettingView
        readSettingView!!.setOnReadSettingListener(this)
        novel_basePageView = findViewById(R.id.novel_basePageView) as FrameLayout
        if (Constants.isSlideUp) {
            pageView = ScrollPageView(applicationContext)
        } else {
            pageView = PageView(applicationContext)
        }
        novel_basePageView!!.removeAllViews()
        novel_basePageView!!.addView(pageView as View?, FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT))
        mReadPresenter!!.initData(pageView!!)
        readSettingView!!.setDataFactory(fac, readStatus!!, mThemeHelper)
        readSettingView!!.currentThemeMode = mReadPresenter!!.currentThemeMode
        auto_menu = findViewById(R.id.auto_menu) as AutoReadMenu
        auto_menu!!.setOnAutoMemuListener(this)

        ll_guide_layout = findViewById(R.id.ll_guide_layout)
        initGuide()

        readSettingView!!.setNovelMode(Constants.MODE)
        readStatus!!.source_ids = readStatus!!.book.site

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

    fun freshPage() {
        if (mReadPresenter != null) {
            mReadPresenter!!.freshPage()
        }
    }

    fun searchChapterCallBack(sourcesList: ArrayList<Source>) {
        if (mReadPresenter != null) {
            mReadPresenter!!.searchChapterCallBack(sourcesList)
        }
    }

    public override fun restoreBrightness() {
        super.restoreBrightness()
    }

    public override fun setReaderDisplayBrightness() {
        super.setReaderDisplayBrightness()
    }

    fun changeSourceCallBack() {
        if (mReadPresenter != null) {
            mReadPresenter!!.changeSourceCallBack()
        }
    }

    /**
     * 跳章
     */
    fun jumpChapterCallBack() {
        if (mReadPresenter != null) {
            mReadPresenter!!.jumpChapterCallBack()
        }
    }

    /**
     * 隐藏topmenu
     */
    fun dismissTopMenu() {
        if (mReadPresenter != null)
            mReadPresenter!!.dismissTopMenu()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (mReadPresenter != null) {
            if (mReadPresenter!!.dispatchKeyEvent(event)) {
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (mReadPresenter != null) {
                mReadPresenter!!.onKeyDown()
            }
            return true
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {

        if (mCatlogMarkDrawer != null && mCatlogMarkDrawer!!.isDrawerOpen(GravityCompat.START)) {
            mCatlogMarkDrawer!!.closeDrawers()
            return
        }

        var isFinish = false

        if (mReadPresenter != null) {
            isFinish = mReadPresenter!!.onBackPressed()
        }

        if (isFinish && !isFinishing) {
            super.onBackPressed()
        }
    }


    override fun onResume() {
        super.onResume()
        AppLog.d("ReadingActivity", "onResume:" + Constants.isFullWindowRead)

        // 设置全屏
        if (!Constants.isFullWindowRead) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
        } else {
            //            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams
            //                    .FLAG_LAYOUT_INSET_DECOR);
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        if (mReadPresenter != null) {
            mReadPresenter!!.onResume()
        }

    }

    override fun shouldReceiveCacheEvent(): Boolean {
        return false
    }

    override fun shouldShowNightShadow(): Boolean {
        return false
    }

    override fun onStart() {
        super.onStart()
        if (mReadPresenter != null) {
            mReadPresenter!!.onStart()
        }
    }

    override fun onPause() {
        super.onPause()
        if (mReadPresenter != null) {
            mReadPresenter!!.onPause()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (mReadPresenter != null) {
            mReadPresenter!!.onWindowFocusChanged(hasFocus)
        }

        if (hasFocus) {
            window.decorView.postDelayed({ window.decorView.systemUiVisibility = FrameActivity.UI_OPTIONS_IMMERSIVE_STICKY }, 1500)
        }
    }

    override fun onStop() {
        super.onStop()
        if (mReadPresenter == null) {
            mReadPresenter!!.onStop()
        }
    }

    override fun onDestroy() {

        if (mCatlogMarkDrawer != null) {
            mCatlogMarkDrawer!!.removeDrawerListener(mDrawerListener)
            if (mCatalogMarkFragment != null)
                mCatlogMarkDrawer!!.removeDrawerListener(mCatalogMarkFragment!!)
        }

        AppLog.e(mTAG, "onDestroy")


        if (novel_basePageView != null) {
            novel_basePageView!!.removeAllViews()
            novel_basePageView = null
        }

        if (readSettingView != null) {
            readSettingView!!.recycleResource()
            readSettingView = null
        }

        if (auto_menu != null) {
            auto_menu!!.setOnAutoMemuListener(null)
            auto_menu!!.recycleResource()
            auto_menu = null
        }

        if (reading_content != null) {
            reading_content!!.removeAllViews()
            reading_content = null
        }

        if (ll_guide_layout != null) {
            ll_guide_layout = null
        }

        if (mReadPresenter != null) {
            mReadPresenter!!.onDestroy()
        }
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        var state: Bundle? = null
        if (mReadPresenter != null) {
            state = mReadPresenter!!.onSaveInstanceState(outState)
        }
        if (state != null) {
            super.onSaveInstanceState(outState)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (mReadPresenter != null) {
            mReadPresenter!!.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun speedUp() {
        if (mReadPresenter != null) {
            mReadPresenter!!.speedUp()
        }
    }

    override fun speedDown() {
        if (mReadPresenter != null) {
            mReadPresenter!!.speedDown()
        }
    }

    override fun autoStop() {
        if (mReadPresenter != null) {
            mReadPresenter!!.autoStop()
        }
    }

    override fun onReadCatalog() {
        if (mReadPresenter != null) {
            mReadPresenter!!.onReadCatalog()
        }
        mCatlogMarkDrawer!!.openDrawer(GravityCompat.START)
    }

    override fun onReadChangeSource() {
        if (mReadPresenter != null) {
            mReadPresenter!!.onReadChangeSource()
        }
    }

    override fun onReadCache() {
        if (mReadPresenter != null) {
            mReadPresenter!!.onReadCache()
        }
    }

    override fun onReadAuto() {
        if (mReadPresenter != null) {
            mReadPresenter!!.onReadAuto()
        }
    }

    override fun onChangeMode(mode: Int) {
        if (mReadPresenter != null) {
            mReadPresenter!!.onChangeMode(mode)
        }
    }

    override fun onChangeScreenMode() {
        if (mReadPresenter != null) {
            mReadPresenter!!.changeScreenMode()
        }
    }

    fun addTextLength(l: Int) {
        readLength += l
    }

    override fun onRedrawPage() {
        if (mReadPresenter != null) {
            mReadPresenter!!.onRedrawPage()
        }
    }

    override fun onJumpChapter() {
        if (mReadPresenter != null) {
            mReadPresenter!!.onJumpChapter()
        }
    }

    override fun onJumpPreChapter() {
        if (mReadPresenter != null) {
            mReadPresenter!!.onJumpPreChapter()
        }
    }

    override fun onJumpNextChapter() {
        if (mReadPresenter != null) {
            mReadPresenter!!.onJumpNextChapter()
        }
    }

    override fun onReadFeedBack() {
        if (mReadPresenter != null) {
            mReadPresenter!!.onReadFeedBack()
        }
    }

    override fun onChageNightMode() {
        if (mReadPresenter != null) {
            mReadPresenter!!.onChageNightMode()
        }
    }

    fun goBackToHome() {
        if (mReadPresenter != null) {
            mReadPresenter!!.goBackToHome()
        }
    }

    override fun initPresenter(optionPresenter: ReadOptionPresenter?, markPresenter: CatalogMarkPresenter?) {
        mCatalogMarkFragment!!.presenter = markPresenter
        mOptionHeader!!.presenter = optionPresenter
    }

    override fun setReadStatus(readSta: ReadStatus) {
        readStatus = readSta
    }

    fun showMenu(isShow: Boolean) {
        if (mReadPresenter != null) {
            mReadPresenter!!.showMenu(isShow)
        }
    }

    override fun showSetMenu(isShow: Boolean) {
        if (readSettingView != null)
            readSettingView!!.showSetMenu(isShow)
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
        if (sharedPreferencesUtils != null && !sharedPreferencesUtils!!.getBoolean(mReadPresenter!!.versionCode.toString() + Constants
                .READING_SETING_GUIDE_TAG)) {
            ll_guide_layout!!.visibility = View.VISIBLE

            val iv_guide_setting_bookmark = findViewById(R.id.iv_guide_setting_bookmark) as ImageView

            iv_guide_setting_bookmark.visibility = View.VISIBLE

            ll_guide_layout!!.setOnClickListener {
                sharedPreferencesUtils!!.putBoolean(mReadPresenter!!.versionCode.toString() + Constants.READING_SETING_GUIDE_TAG, true)
                ll_guide_layout!!.visibility = View.GONE
            }
        }
    }

    override fun setMode() {
        if (readSettingView != null) {
            readSettingView!!.setMode()
        }
    }

    override fun showAutoMenu(isShow: Boolean) {
        if (auto_menu != null) {
            if (isShow) {
                auto_menu!!.visibility = View.VISIBLE
            } else {
                auto_menu!!.visibility = View.GONE
            }
        }
    }

    override fun resetPageView(pageView: PageInterface) {
        if (novel_basePageView != null) {
            novel_basePageView!!.removeAllViews()
            novel_basePageView!!.addView(pageView as View, FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT))
        }
    }

    override fun initShowCacheState() {
        if (readSettingView != null) {
            readSettingView!!.initShowCacheState()
        }
    }

    override fun changeChapter() {
        if (readSettingView != null) {
            readSettingView!!.changeChapter()
        }
    }

    override fun checkModeChange() {
        if (isModeChange) {
            setMode()
        }
    }

    override fun getAutoMenuShowState(): Boolean {
        if (auto_menu != null) {
            return auto_menu!!.isShown
        }
        return false
    }

    override fun showStopAutoHint() {
        val view = inflater!!.inflate(R.layout.autoread_textview, null) as TextView
        val toast = Toast(applicationContext)
        toast.view = view
        toast.duration = Toast.LENGTH_SHORT
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
        auto_menu!!.visibility = View.GONE
    }

    override fun initCatlogView() {
        val main = layoutInflater.inflate(R.layout.act_read, null)
        setContentView(main)
        mCatlogMarkDrawer = findViewById(R.id.read_catalog_mark_drawer) as DrawerLayout

        mCatlogMarkDrawer!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        mCatlogMarkDrawer!!.addDrawerListener(mDrawerListener)

        mCatalogMarkFragment = supportFragmentManager.findFragmentById(R.id.read_catalog_mark_layout) as CatalogMarkFragment
        mOptionHeader = findViewById(R.id.option_header) as ReadOptionHeader

        mCatlogMarkDrawer!!.addDrawerListener(mCatalogMarkFragment!!)

        if (mReadPresenter != null) {
            mReadPresenter!!.initCatalogPresenter(mCatalogMarkFragment!!, mOptionHeader!!)
        }
    }

    override fun onNewInitView(): Boolean {
        mCatlogMarkDrawer = findViewById(R.id.read_catalog_mark_drawer) as DrawerLayout
        if (mCatlogMarkDrawer == null) {
            finish()
            return false
        }

        mCatlogMarkDrawer!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        mCatlogMarkDrawer!!.addDrawerListener(mDrawerListener)

        mCatalogMarkFragment = supportFragmentManager.findFragmentById(R.id.read_catalog_mark_layout) as CatalogMarkFragment
        if (mCatalogMarkFragment == null) {
            finish()
            return false
        }
        mOptionHeader = findViewById(R.id.option_header) as ReadOptionHeader

        if (mOptionHeader == null) {
            finish()
            return false
        }
        mCatlogMarkDrawer!!.addDrawerListener(mCatalogMarkFragment!!)


        if (mReadPresenter != null) {
            mReadPresenter!!.initCatalogPresenter(mCatalogMarkFragment!!, mOptionHeader!!)
        }
        return true
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
}
