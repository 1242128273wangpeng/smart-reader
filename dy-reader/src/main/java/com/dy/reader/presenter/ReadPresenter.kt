package com.dy.reader.presenter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.TextUtils
import android.view.InflateException
import android.view.View
import android.widget.Toast
import com.baidu.mobstat.StatService
import com.ding.basic.bean.Book
import com.ding.basic.repository.RequestRepositoryFactory
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dingyue.contract.util.SharedPreUtil
import com.dy.media.MediaControl
import com.dy.media.ReaderRestDialog
import com.dy.reader.R
import com.dy.reader.Reader
import com.dy.reader.activity.ReaderActivity
import com.dy.reader.data.DataProvider
import com.dy.reader.event.EventSetting
import com.dy.reader.fragment.LoadingDialogFragment
import com.dy.reader.help.NovelHelper
import com.dy.reader.helper.AppHelper
import com.dy.reader.page.BatteryView
import com.dy.reader.page.Position
import com.dy.reader.setting.ReaderSettings
import com.dy.reader.setting.ReaderStatus
import com.dy.reader.util.getNotchSize
import com.dy.reader.util.isNotchScreen
import com.dy.reader.util.xiaomiNotch
import com.google.gson.Gson
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.component.service.DownloadService
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.db.help.ChapterDaoHelper
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.SharedPreferencesUtils
import net.lzbook.kit.utils.StatServiceUtils
import org.greenrobot.eventbus.EventBus
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by yuchao on 2017/11/14 0014.
 */
open class ReadPresenter(val act: ReaderActivity) : NovelHelper.OnHelperCallBack {

    private val TAG = ReadPresenter::class.java.simpleName

    var downloadService: DownloadService? = null
    private var mContext: Context = act.applicationContext
    private var sp: SharedPreferences? = null
    private var modeSp: SharedPreferences? = null
    private var isSubed: Boolean = false
    private var screen_moding = false
    private var isFromCover = true
    var myNovelHelper: NovelHelper? = null
    private var is_dot_orientation = false// 横竖屏打点
    var time_text: CharSequence? = null
    var versionCode: Int = 0
        get() = 0
    private var readReference: WeakReference<ReaderActivity>? = null

    var currentThemeMode: String? = null

//    private val handler = Handler(Looper.getMainLooper())

    private val readerRestDialog: ReaderRestDialog? by lazy {
        readReference?.get()?.let {
            ReaderRestDialog(it)
        }
        null
    }

    init {
        readReference = WeakReference(act)
    }

    fun onCreateInit(savedInstanceState: Bundle?) {
        ReaderStatus.startTime = System.currentTimeMillis()/1000L
        ReaderStatus.chapterList.clear()
        DataProvider.clear()
        ReaderSettings.instance.loadParams()

        sp = PreferenceManager.getDefaultSharedPreferences(readReference?.get()?.applicationContext)
//        ReaderSettings.animation_mode = sp?.getInt("page_mode", Constants.PAGE_MODE_DELAULT) ?: Constants.PAGE_MODE_DELAULT
//        ReaderSettings.FULL_SCREEN_READ = sp?.getBoolean("full_screen_read", false) ?: false
//        ReaderSettings.MODE = 0
//        ReaderSettings.MODE = sp?.getInt("content_mode", 51) ?: 51
//        Constants.isSlideUp = ReaderSettings.animation_mode == 3
//        Constants.isVolumeTurnover = sp?.getBoolean("sound_turnover", true) ?: true
        AppLog.e("getAdsStatus", "novel_onCreate")
        versionCode = AppUtils.getVersionCode()
        AppLog.e(TAG, "versionCode: " + versionCode)
//        autoSpeed = ReaderStatus.autoReadSpeed()!!
        myNovelHelper = NovelHelper(readReference?.get())
        myNovelHelper?.setOnHelperCallBack(this)

        // 初始化窗口基本信息
        initWindow()
        setOrientation()
        getSavedState(savedInstanceState)
        if (isFromCover && ReaderSettings.instance.isLandscape) {
            return
        }
        initBookState()
        MediaControl.startRestMedia {
            if (readerRestDialog?.isShowing() == true) {
                return@startRestMedia
            }
            MediaControl.loadRestMedia(readReference?.get(), { view: View? ->
                if (readReference?.get()?.isFinishing == true) return@loadRestMedia
                readerRestDialog?.show(view)
            })
        }
    }

    fun loadData(useReadStatus: Boolean = false) {

        act.showLoadingDialog(LoadingDialogFragment.DialogType.LOADING)
        if (useReadStatus) {
            ReaderStatus.book.sequence = ReaderStatus.position.group
            ReaderStatus.book.offset = ReaderStatus.position.offset
        }
        ReaderStatus.prepare(ReaderStatus.book, { flag ->
            if (flag) {
                ReaderStatus.position = DataProvider.queryPosition(ReaderStatus.book.book_id, ReaderStatus.book.sequence, ReaderStatus.book.offset)
                act.showReader()
            }
        })
    }

    fun onNewIntent(intent: Intent) {

        EventBus.getDefault().post(EventSetting(EventSetting.Type.MENU_STATE_CHANGE, false))
        AppLog.d("ReadingActivity", "onNewIntent:")

        // 初始化窗口基本信息
        setOrientation()

        getSavedState(intent.extras)

        if (isFromCover && ReaderSettings.instance.isLandscape) {
            return
        }

        ReaderStatus.clear()
        loadData()
    }

    /**
     * 处理书籍状态
     */
    private fun initBookState() {
        // 判断是否订阅
        isSubed = (RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(ReaderStatus.book.book_id) != null)
        AppLog.e(TAG, "初始化书籍状态: " + ReaderStatus.book.book_id)
        if (isSubed) {
            val sequence = ReaderStatus.book.sequence
            val offset = ReaderStatus.book.offset
            ReaderStatus.book = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(ReaderStatus.book.book_id)!!
            if (sequence != ReaderStatus.book.sequence) {
                // 目录页点击目录进来的sequence并未保存到数据库
                ReaderStatus.book.sequence = sequence
                ReaderStatus.book.offset = offset
            }
        }
        if (ReaderStatus.book.sequence < -1) {
            ReaderStatus.book.sequence = -1
        } else if (isSubed && ReaderStatus.book.sequence + 1 > ReaderStatus.book.chapter_count) {
            ReaderStatus.book.sequence = ReaderStatus.book.chapter_count - 1
        }
    }

    fun onConfigurationChanged() {
        initWindow()

        ReaderStatus.clear()
        loadData(true)
    }

    private fun getSavedState(savedInstanceState: Bundle?) {
        val bundle = readReference?.get()?.intent?.extras
        if (savedInstanceState != null) {
            getReaderState(savedInstanceState)
        } else {
            getReaderState(bundle)
        }
    }

    private fun getReaderState(readerState: Bundle?) {
        readerState?.let {
            ReaderStatus.book = it.getSerializable("book") as Book

            // 更新ReaderStatus中的book对象部分信息：防止换源后，返回阅读页所带的信息和本地存储的信息不一致
            val book = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(ReaderStatus.book.book_id)

            if (book != null) {
                ReaderStatus.book.host = book.host
                ReaderStatus.book.book_id = book.book_id
                ReaderStatus.book.chapter_count = book.chapter_count
                ReaderStatus.book.book_source_id = book.book_source_id
                ReaderStatus.book.book_chapter_id = book.book_chapter_id
            }

            ReaderStatus.book.sequence = it.getInt("sequence", ReaderStatus.book.sequence)
            ReaderStatus.position.group = ReaderStatus.book.sequence
            ReaderStatus.position.offset = ReaderStatus.book.offset
            currentThemeMode = it.getString("thememode", readReference?.get()?.mThemeHelper?.mode)
        }
    }

    /*
     * 设置屏幕方向 port land
     */
    private fun setOrientation() {
        if (!screen_moding) {
            if (sp?.getInt("screen_mode", 3) == Configuration.ORIENTATION_PORTRAIT) {
                if (!is_dot_orientation) {
                    is_dot_orientation = true
                }

                ReaderSettings.instance.isLandscape = false
                readReference?.get()?.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            } else if (sp?.getInt("screen_mode", 3) == Configuration.ORIENTATION_LANDSCAPE && readReference?.get()?.getResources()!!
                    .getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                if (!is_dot_orientation) {
                    is_dot_orientation = true
                }

                ReaderSettings.instance.isLandscape = true
                readReference?.get()?.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            } else {
                if (!is_dot_orientation) {
                    is_dot_orientation = true
                }
            }
        }
    }


    fun checkManualDialogShow() {
        Constants.manualReadedCount++
        if (Constants.manualReadedCount != 0 && !Constants.isSlideUp) {
            if (Constants.manualReadedCount == Constants.manualTip) {
                AppLog.d("IReadDataFactory", "显示自动阅读提醒")
                if (myNovelHelper != null) {
                    myNovelHelper?.showHintAutoReadDialog()
                }
            }
        }
    }

    /**
     * 初始化窗口基本信息
     */
    private fun initWindow() {

        val display = readReference?.get()?.getWindowManager()!!.getDefaultDisplay()
        val realSize = Point()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(realSize)
        } else {
            display.getSize(realSize)
        }

        val dm = readReference?.get()?.getResources()!!.getDisplayMetrics()
        AppHelper.screenWidth = realSize.x
        AppHelper.screenHeight = realSize.y
        AppHelper.screenDensity = dm.density
        AppHelper.screenScaledDensity = dm.scaledDensity

        if(isNotchScreen(Reader.context)){
            if(xiaomiNotch(Reader.context) && ReaderSettings.instance.isLandscape){
                AppHelper.screenWidth -= getNotchSize(Reader.context)
            }
        }

        // 保存字体、亮度、阅读模式
        modeSp = readReference?.get()?.getSharedPreferences("config", Context.MODE_PRIVATE)
//        // 设置字体
//        if (sp?.contains("novel_font_size") == true) {
//            ReaderSettings.fontSize = sp?.getInt("novel_font_size", 18) ?: 18
//        } else {
//            ReaderSettings.fontSize = 18
//        }

    }

    override fun openAutoReading(open: Boolean) {
        ReaderSettings.instance.isAutoReading = true
    }

    override fun addBookShelf(isAddShelf: Boolean) {
        if (isAddShelf && ReaderStatus.book != null) {
            ReaderStatus.book.sequence = ReaderStatus.position.group
            ReaderStatus.book.offset = ReaderStatus.position.offset
            ReaderStatus.book.last_read_time = System.currentTimeMillis()
            ReaderStatus.book.last_update_success_time = System.currentTimeMillis()
            ReaderStatus.book.readed = 1

            if (ReaderStatus.chapterList != null) {
                val helper = ChapterDaoHelper.loadChapterDataProviderHelper(BaseBookApplication.getGlobalContext(), ReaderStatus.book.book_id)
                helper.deleteAllChapters()
                helper.insertOrUpdateChapter(ReaderStatus.chapterList)
                ReaderStatus.book.chapter_count = helper.getCount()

                if (ReaderStatus.chapterList.size > 1) {
                    ReaderStatus.book.last_chapter = ReaderStatus.chapterList[ReaderStatus.chapterList.size - 1]
                }
            }
            val succeed = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).insertBook(ReaderStatus.book)

            Toast.makeText(readReference?.get(), if (succeed > 0) R.string.reading_add_succeed else R.string.reading_add_fail,
                    Toast.LENGTH_SHORT).show()
        }
        val map1 = HashMap<String, String>()
        if (ReaderStatus.book != null) {
            map1.put("bookid", ReaderStatus.book.book_id!!)
        }
        if (ReaderStatus.currentChapter != null) {
            map1.put("chapterid", ReaderStatus.currentChapter!!.chapter_id!!)
        }
        if (isAddShelf) {
            StartLogClickUtil.upLoadEventLog(readReference?.get(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.POPUPSHELFADD, map1)
        } else {
            StartLogClickUtil.upLoadEventLog(readReference?.get(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.POPUPSHELFADDCANCLE, map1)
        }

        goBackToHome()
    }

    /**
     * 横屏切换
     */
    fun changeScreenMode() {
        screen_moding = true
        val screen_mode = sp!!.edit()
        val act = readReference?.get()
        if (act === null) {
            return
        }
        if (act.resources!!.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.rb_click_portrait_btn)
            val data = HashMap<String, String>()
            data.put("type", "2")
            StartLogClickUtil.upLoadEventLog(readReference?.get(), StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.HPMODEL, data)
            ReaderSettings.instance.isLandscape = false
            act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            screen_mode.putInt("screen_mode", Configuration.ORIENTATION_PORTRAIT)
        } else if (act.resources!!.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.rb_click_landscape_btn)
            val data = HashMap<String, String>()
            data.put("type", "1")
            StartLogClickUtil.upLoadEventLog(readReference?.get(), StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.HPMODEL, data)
            ReaderSettings.instance.isLandscape = true
            act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            isFromCover = false
            screen_mode.putInt("screen_mode", Configuration.ORIENTATION_LANDSCAPE)
        }
        screen_mode.apply()
    }


    fun onBackPressed(): Boolean {

//        // 显示菜单
//        if (ReaderStatus.isMenuShow) {
//            showMenu(false)
//            return true
//        }

        if (!TextUtils.isEmpty(ReaderStatus.book.book_id)) {
            isSubed = (RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(ReaderStatus.book.book_id) != null)
        }

        if (!isSubed) {
            try {
                myNovelHelper?.showAndBookShelfDialog()
            } catch (e: InflateException) {
                e.printStackTrace()
            }

            return false
        }
        goBackToHome()
        return true
    }

    fun onResume() {
        if (!ReaderSettings.instance.isAutoBrightness) {
            setScreenBrightness(ReaderSettings.instance.screenBrightness)
        }
        StatService.onResume(act)
    }

    fun setScreenBrightness(brightness: Int) {
        if (act == null || act.isFinishing) {
            return
        }

        val window = act.window
        val localLayoutParams = window.attributes
        localLayoutParams.screenBrightness = brightness.toFloat() / 255.0f
        window.attributes = localLayoutParams
    }


    /**
     * 开启亮度自动调节
     */
    fun startAutoBrightness() {
        setScreenBrightness(-1)
    }


    fun onPause() {
        isFromCover = false
        if (isSubed) {
            myNovelHelper?.saveBookmark(ReaderStatus.book.book_id, ReaderStatus.position.group,
                    ReaderStatus.position.offset)
            // 统计阅读章节数
            val spUtils = SharedPreferencesUtils(PreferenceManager
                    .getDefaultSharedPreferences(readReference?.get()))
            spUtils.putInt("readed_count", Constants.readedCount)
        }


        // 保存正在阅读的书
        val sp = SharedPreUtil(SharedPreUtil.SHARE_DEFAULT)

        val book = ReaderStatus.book
        book.sequence = ReaderStatus.position.group
        book.offset = ReaderStatus.position.offset
        book.chapter_count = ReaderStatus.chapterCount
        book.last_read_time = System.currentTimeMillis()
        book.readed = 1
        RequestRepositoryFactory.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).updateBook(book)

        val books = Gson().toJson(book)
        sp.putString(SharedPreUtil.CURRENT_READ_BOOK, books)

        StatService.onPause(act)
    }

    fun onStop() {
        ReaderSettings.instance.save()
    }

    fun onDestroy() {

        BatteryView.clean()
        ReaderStatus.position = Position(book_id = "")

        MediaControl.stopRestMedia()

        try {
            if (readerRestDialog != null && readerRestDialog!!.isShowing()) {
                readerRestDialog?.dismiss()
            }
        } catch (e: Exception) {

        }

    }

    fun goToBookEnd() {
        if (readReference == null || readReference!!.get() == null || readReference!!.get()!!.isFinishing) {
            return
        }

        if (ReaderStatus.position.group != ReaderStatus.chapterList.size - 1) {
            return
        }

        val bundle = Bundle()
        bundle.putSerializable("book", ReaderStatus.book)
        bundle.putString("book_id", ReaderStatus.book.book_id)
        bundle.putString("book_name", ReaderStatus.book.name)
        bundle.putString("chapter_id", ReaderStatus.chapterId)
        RouterUtil.navigation(act, RouterConfig.BOOK_END_ACTIVITY, bundle)
    }


    fun goBackToHome() {
        val act = readReference?.get() ?: return
        if (!currentThemeMode.equals(act.mThemeHelper.getMode())) {
            val bundle = Bundle()
            bundle.putInt("type_event", 0)
            RouterUtil.navigation(act, RouterConfig.HOME_ACTIVITY, bundle)
            act.finish()
        } else {
            if (act.isTaskRoot) {
                RouterUtil.navigation(act, RouterConfig.SPLASH_ACTIVITY, Intent.FLAG_ACTIVITY_CLEAR_TASK)

            }
            act.finish()
        }
    }

    fun updateOriginLog() {
        val data = HashMap<String, String>()
        data.put("bookid", ReaderStatus.book.book_id)
        StartLogClickUtil.upLoadEventLog(readReference?.get(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.ORIGINALLINK, data)
    }
}