package com.intelligent.reader.activity

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import com.intelligent.reader.R
import com.intelligent.reader.fragment.CatalogMarkFragment
import com.intelligent.reader.presenter.read.CatalogMarkPresenter
import com.intelligent.reader.presenter.read.ReadOptionPresenter
import com.intelligent.reader.presenter.read.ReadPreInterface
import com.intelligent.reader.presenter.read.ReadPresenter
import com.intelligent.reader.read.DataProvider
import com.intelligent.reader.read.animation.BitmapManager
import com.intelligent.reader.read.help.IReadPageChange
import com.intelligent.reader.read.mode.ReadInfo
import com.intelligent.reader.read.mode.ReadState
import com.intelligent.reader.read.page.AutoReadMenu
import com.intelligent.reader.read.page.PageInterface
import com.intelligent.reader.read.page.ReadSettingView
import com.intelligent.reader.reader.ReaderViewModel
import iyouqu.theme.FrameActivity
import kotlinx.android.synthetic.qbzsydq.act_read.*
import kotlinx.android.synthetic.qbzsydq.reading_page.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.*
import net.lzbook.kit.utils.SharedPreferencesUtils
import java.util.*
import kotlin.properties.Delegates


/**
 * ReadingActivity
 * 小说阅读页
 */
class ReadingActivity : BaseCacheableActivity(), AutoReadMenu.OnAutoMemuListener, ReadSettingView.OnReadSettingListener, ReadPreInterface.View, IReadPageChange {

    // 系统存储设置
    private lateinit var mSharedPreferencesUtils: SharedPreferencesUtils

    private var mCatalogMarkFragment: CatalogMarkFragment? = null

    private lateinit var mReadPresenter: ReadPresenter

    private val startReadTime = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        read_catalog_mark_drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        mReadPresenter.onNewIntent(intent)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        read_catalog_mark_drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        mReadPresenter.onConfigurationChanged(mCatalogMarkFragment, option_header, readerWidget.childCount)
        ReadConfig.IS_LANDSCAPE = (newConfig.orientation != Configuration.ORIENTATION_PORTRAIT)
    }

    override fun initView(fac: ReaderViewModel) {
        readSettingView.setOnReadSettingListener(this)
        readSettingView.setDataFactory(fac, readStatus, mThemeHelper)
        readSettingView.currentThemeMode = mReadPresenter.currentThemeMode

        read_catalog_mark_drawer.addDrawerListener(mDrawerListener)
        mCatalogMarkFragment?.let {
            read_catalog_mark_drawer.addDrawerListener(it)
        }

        readerWidget.removeAllViews()
        BitmapManager.getInstance().setSize(readStatus.screenWidth, readStatus.screenHeight)
        auto_menu.setOnAutoMemuListener(this)

        readStatus.source_ids = readStatus.book?.site
        DataProvider.getInstance().context = this
        ReadState.book = readStatus.book
        //add ReadInfo
        ReadConfig.animation = when (Constants.PAGE_MODE) {
            1 -> ReadViewEnums.Animation.curl
            2 -> ReadViewEnums.Animation.shift
            3 -> ReadViewEnums.Animation.list
            else -> ReadViewEnums.Animation.slide
        }

        readerWidget.initReaderViewFactory()
        readerWidget.entrance(ReadInfo(readStatus.book!!, readStatus, ReadConfig.animation))
        readerWidget.setIReadPageChange(this)
        readSettingView.setNovelMode(ReadConfig.MODE)

        initGuide()
    }

    private fun initGuide() {
        if (!mSharedPreferencesUtils.getBoolean(mReadPresenter.versionCode.toString() + Constants.READING_GUIDE_TAG)) {
            ll_guide_layout!!.visibility = View.VISIBLE
            iv_guide_reading.visibility = View.VISIBLE
            ll_guide_layout!!.setOnClickListener {
                mSharedPreferencesUtils.putBoolean(mReadPresenter.versionCode.toString() + Constants.READING_GUIDE_TAG, true)
                iv_guide_reading.visibility = View.GONE
                ll_guide_layout!!.visibility = View.GONE
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

    fun freshPage() = mReadPresenter.freshPage()

    //自动阅读
    fun dealManualDialogShow() = mReadPresenter.dealManualDialogShow()

    fun searchChapterCallBack(sourcesList: ArrayList<Source>) = mReadPresenter.searchChapterCallBack(sourcesList)

    fun changeSourceCallBack() = mReadPresenter.changeSourceCallBack()

    /**
     * 跳章
     */
    fun jumpChapterCallBack() = mReadPresenter.jumpChapterCallBack()

    /**
     * 隐藏topmenu
     */
    fun dismissTopMenu() = mReadPresenter.dismissTopMenu()

    override fun dispatchKeyEvent(event: KeyEvent): Boolean =
            when (mReadPresenter.dispatchKeyEvent(event)) {
                true -> true
                else -> super.dispatchKeyEvent(event)
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

        readSettingView.dismissMenu()

        val isFinish = mReadPresenter.onBackPressed()
        if (isFinish && !isFinishing) {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        // 设置全屏
        when (!Constants.isFullWindowRead) {
            true -> window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
            false -> window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        mReadPresenter.onResume()
    }

    override fun shouldReceiveCacheEvent(): Boolean = false

    override fun shouldShowNightShadow(): Boolean = false

    override fun onStart() {
        super.onStart()
        mReadPresenter.onStart()
    }

    override fun onPause() {
        super.onPause()
        mReadPresenter.onPause(ReadState.sequence, ReadState.offset)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.postDelayed({ window.decorView.systemUiVisibility = FrameActivity.UI_OPTIONS_IMMERSIVE_STICKY }, 1500)
        }
    }

    override fun onStop() {
        super.onStop()
        mReadPresenter.onStop()
    }

    override fun onDestroy() {
        read_catalog_mark_drawer.removeDrawerListener(mDrawerListener)
        mCatalogMarkFragment?.let {
            read_catalog_mark_drawer.removeDrawerListener(it)
        }

        readSettingView.recycleResource()

        auto_menu.setOnAutoMemuListener(null)
        auto_menu.recycleResource()

        mReadPresenter.onDestroy()
        DataProvider.getInstance().unSubscribe()
        DataProvider.getInstance().relase()
        ReadConfig.unregistObserverAll()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mReadPresenter.onSaveInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) = mReadPresenter.onActivityResult(requestCode, resultCode, data)

    override fun speedUp() = mReadPresenter.speedUp()

    override fun speedDown() = mReadPresenter.speedDown()

    override fun autoStop() {}

    //ReadSettingView start
    override fun onReadCatalog() {
        mReadPresenter.onReadCatalog()
        read_catalog_mark_drawer.openDrawer(GravityCompat.START)
    }

    override fun onReadChangeSource() = mReadPresenter.onReadChangeSource()

    override fun onReadCache() = mReadPresenter.onReadCache()

    override fun onReadAuto() {}

    override fun onChangeMode(mode: Int) {
        mReadPresenter.onChangeMode(mode)
//        readerWidget.setBackground()
    }

    override fun onChangeScreenMode() {
        mReadPresenter.changeScreenMode()
        readSettingView.dismissMenu()
    }

    //目录跳章
    override fun onJumpChapter(sequence: Int, offset: Int) {
        ReadState.sequence = sequence
        ReadState.currentPage = 0
        ReadState.offset = offset
        ReadConfig.jump = true
        read_catalog_mark_drawer.closeDrawers()
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
        if (((mode == 3) and (ReadConfig.animation != ReadViewEnums.Animation.list)) or ((ReadConfig.animation == ReadViewEnums.Animation.list) and (mode != 3))) {
            readerWidget.changeAnimMode(mode)
            readerWidget.entrance(ReadInfo(readStatus.book, readStatus, ReadConfig.animation))
            readerWidget.setIReadPageChange(this)
        }
        readerWidget.changeAnimMode(mode)
        readSettingView.dismissMenu()
    }

    //ReadSettingView end
    fun goBackToHome() = mReadPresenter.goBackToHome()

    override fun initPresenter(optionPresenter: ReadOptionPresenter?, markPresenter: CatalogMarkPresenter?) {
        mCatalogMarkFragment?.presenter = markPresenter
        option_header.presenter = optionPresenter
    }

    override fun setReadStatus(readSta: ReadStatus) {
        readStatus = readSta
    }

    override fun showSetMenu(isShow: Boolean) {

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
            ll_guide_layout.visibility = View.VISIBLE
            iv_guide_setting_bookmark.visibility = View.VISIBLE
            ll_guide_layout.setOnClickListener {
                mSharedPreferencesUtils.putBoolean(mReadPresenter.versionCode.toString() + Constants.READING_SETING_GUIDE_TAG, true)
                ll_guide_layout.visibility = View.GONE
            }
        }
    }

    override fun setMode() = readSettingView.setMode()

    override fun showAutoMenu(isShow: Boolean) = if (isShow) auto_menu?.visibility = View.VISIBLE else auto_menu?.visibility = View.GONE

    override fun resetPageView(pageView: PageInterface) {
    }

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
        val view = View.inflate(this, R.layout.autoread_textview, null) as TextView
        val toast = Toast(applicationContext)
        toast.view = view
        toast.duration = Toast.LENGTH_SHORT
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
        auto_menu.visibility = View.GONE
    }

    companion object {
        private var readStatus: ReadStatus by Delegates.notNull()
    }

    override fun showMenu(isShow: Boolean) {
        if (isShow) {
            if (!readSettingView.isChecked()) {
                readSettingView.showMenu()
            }
        } else {
            readSettingView.dismissMenu()
        }
        mReadPresenter.showMenu(isShow)
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

    override fun addLog() {
        val book = intent.getSerializableExtra("book") as Book
        val endTime = System.currentTimeMillis()
        val bookId = book.book_id
        val chapterId = ReadState.chapterId
        val sourceIds = book.book_source_id
        val channelCode = when (book.site) {
            Constants.QG_SOURCE -> "1"
            else -> "2"
        }
        val pageCount = ReadState.pageCount.toString()
        val currentPage = (ReadState.currentPage - 1).toString()
        val currentPageContentLength = ReadState.contentLength.toString()
        //按照此顺序传值 当前的book_id，阅读章节，书籍源，章节总页数，当前阅读页，当前页总字数，当前页面来自，开始阅读时间,结束时间,阅读时间,是否有阅读中间退出行为,书籍来源1为青果，2为智能
        StartLogClickUtil.upLoadReadContent(bookId, chapterId, sourceIds, pageCount, currentPage, currentPageContentLength, "2",
                startReadTime.toString(), endTime.toString(), (endTime - startReadTime).toString(), "false", channelCode)
    }

    override fun readOptionHeaderDismiss() {
        option_header.dismissLoadingPage()
    }
}
