package com.intelligent.reader.presenter.read

import android.app.Activity
import android.app.Notification
import android.app.PendingIntent
import android.content.*
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Point
import android.net.Uri
import android.os.*
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v4.content.LocalBroadcastManager
import android.text.TextUtils
import android.view.*
import android.widget.*
import com.dycm_adsdk.PlatformSDK
import com.dycm_adsdk.callback.AbstractCallback
import com.dycm_adsdk.callback.ResultCode
import com.intelligent.reader.R
import com.intelligent.reader.activity.*
import com.intelligent.reader.cover.BookCoverLocalRepository
import com.intelligent.reader.cover.BookCoverOtherRepository
import com.intelligent.reader.cover.BookCoverQGRepository
import com.intelligent.reader.cover.BookCoverRepositoryFactory
import com.intelligent.reader.fragment.CatalogMarkFragment
import com.intelligent.reader.presenter.IPresenter
import com.intelligent.reader.read.DataProvider
import com.intelligent.reader.read.help.BookHelper
import com.intelligent.reader.read.help.NovelHelper
import com.intelligent.reader.read.mode.ReadState
import com.intelligent.reader.read.page.AutoReadMenu
import com.intelligent.reader.read.page.ReadOptionHeader
import com.intelligent.reader.reader.ReaderOwnRepository
import com.intelligent.reader.reader.ReaderRepositoryFactory
import com.intelligent.reader.reader.ReaderViewModel
import com.intelligent.reader.receiver.DownBookClickReceiver
import com.intelligent.reader.util.EventBookStore
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import iyouqu.theme.ThemeMode
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.component.service.DownloadService
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.*
import net.lzbook.kit.data.db.BookChapterDao
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.net.custom.service.NetService
import net.lzbook.kit.repair_books.RepairHelp
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.utils.*
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.lang.ref.WeakReference
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * Created by yuchao on 2017/11/14 0014.
 */
open class BaseReadPresenter(val act: ReadingActivity) : IPresenter<ReadPreInterface.View>, NovelHelper.OnHelperCallBack,
        DownloadService.OnDownloadListener, ReaderViewModel.ReadDataListener {

    var disposable: ArrayList<Disposable> = ArrayList()
    protected val TAG = BaseReadPresenter::class.java.simpleName

    override var view: ReadPreInterface.View? = act

    val MSG_SEARCH_CHAPTER = 3
    val ERROR = 7
    // 手动书签内容限制
    var downloadService: DownloadService? = null
    var isRestDialogShow = false
    var stampTime: Long = 0
    protected var mContext: Context = act.applicationContext
    //    protected var pageView: PageInterface? = null
    protected var sourcesList: ArrayList<Source>? = null
    protected var isSourceListShow: Boolean = false
    // 系统存储设置
    protected var sp: SharedPreferences? = null
    protected var modeSp: SharedPreferences? = null
    protected var isSubed: Boolean = false
    protected var bookChapterDao: BookChapterDao? = null
    protected var mBookDaoHelper: BookDaoHelper? = null
    protected var screen_moding = false
    protected var isFromCover = true
    //    private var myNovelHelper: NovelHelper? = null
    var myNovelHelper: NovelHelper? = null
    protected var autoSpeed: Int = 0
    protected var auto_menu: AutoReadMenu? = null
    protected var is_dot_orientation = false// 横竖屏打点
    var current_mode: Int = 0
    var time_text: CharSequence? = null
    var versionCode: Int = 0
        get() = 0
    protected var isAcvNovelActive = true
    protected var isRestPress = false
    protected var actNovelRunForeground = true
    protected var readReference: WeakReference<ReadingActivity>? = null
    var mReaderViewModel: ReaderViewModel? = null


    protected var isSlideToAuto = false
    protected var resources: Resources? = null
    protected var myDialog: MyDialog? = null
    protected var type = -1
    var currentThemeMode: String? = null
    private var lastMode = -1

    private val handler = Handler(Looper.getMainLooper())

    init {
        readReference = WeakReference(act)
    }

    private var mCacheUpdateReceiver: CacheUpdateReceiver? = null
    private var mReadOptionPresenter: ReadOptionPresenter? = null
    private var mCatalogMarkPresenter: CatalogMarkPresenter? = null
    private val sc = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName) {}

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            downloadService = (service as DownloadService.MyBinder).service
            BaseBookApplication.setDownloadService(downloadService)
            downloadService?.setOnDownloadListener(this@BaseReadPresenter)
        }
    }

    fun onCreateInit(savedInstanceState: Bundle?) {
        AppLog.e(TAG, "onCreate")

        sp = PreferenceManager.getDefaultSharedPreferences(readReference?.get()?.applicationContext)
        Constants.isFullWindowRead = sp?.getBoolean("read_fullwindow", true) ?: true
        Constants.PAGE_MODE = sp?.getInt("page_mode", 0) ?: 0
        ReadConfig.FULL_SCREEN_READ = sp?.getBoolean("full_screen_read", false) ?: false
        Constants.isSlideUp = Constants.PAGE_MODE == 3
        Constants.isVolumeTurnover = sp?.getBoolean("sound_turnover", true) ?: true
        AppLog.e("getAdsStatus", "novel_onCreate")
        versionCode = AppUtils.getVersionCode()
        AppLog.e(TAG, "versionCode: " + versionCode)
//        autoSpeed = ReadState.autoReadSpeed()!!
        myNovelHelper = NovelHelper(readReference?.get())
        myNovelHelper?.setOnHelperCallBack(this)
        downloadService = BaseBookApplication.getDownloadService()

        // 初始化窗口基本信息
        initWindow()
        setOrientation()
        getSavedState(savedInstanceState)
        RepairHelp.showFixMsg(readReference?.get(), ReadState?.book, {
            if (readReference != null && readReference?.get() != null && !readReference?.get()!!.isFinishing) {
                val intent_download = Intent(readReference?.get(), DownloadManagerActivity::class.java)
                try {
                    readReference?.get()?.startActivity(intent_download)
                    readReference?.get()?.finish()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
        if (isFromCover && ReadConfig.IS_LANDSCAPE) {
            return
        }
        view?.initCatlogView()
        initBookState()
        // 初始化view
        view?.initView(mReaderViewModel!!)
        // 初始化监听器
        initListener()
//        getBookContent()
        if (BaseBookApplication.getDownloadService() == null) {
            BookHelper.reStartDownloadService()
        }
        startRestInterval()
    }

    fun initCatalogPresenter(catalogMarkFragment: CatalogMarkFragment?, optionHeader: ReadOptionHeader) {
        mCatalogMarkPresenter = CatalogMarkPresenter()
        mCatalogMarkPresenter?.view = catalogMarkFragment

        mReadOptionPresenter = ReadOptionPresenter(readReference?.get() as Activity, mReaderViewModel!!)
        mReadOptionPresenter?.view = optionHeader

        view?.initPresenter(mReadOptionPresenter, mCatalogMarkPresenter)
    }

    fun onNewIntent(intent: Intent) {
        this.lastMode = -1
//        pageView?.clear()
        showMenu(false)
        AppLog.d("ReadingActivity", "onNewIntent:")
//        this.sp = PreferenceManager.getDefaultSharedPreferences(readReference?.get()?.applicationContext)
        Constants.isFullWindowRead = sp?.getBoolean("read_fullwindow", true) ?: true
        Constants.PAGE_MODE = sp?.getInt("page_mode", 0) ?: 0
        Constants.isSlideUp = Constants.PAGE_MODE == 3
        versionCode = AppUtils.getVersionCode()
        AppLog.e(TAG, "versionCode: " + versionCode)
//        autoSpeed = ReadState.autoReadSpeed()
        myNovelHelper = NovelHelper(readReference?.get())
        myNovelHelper?.setOnHelperCallBack(this)

        // 初始化窗口基本信息
        initWindow()
        setOrientation()
        getSavedState(intent.extras)
        if (isFromCover && ReadConfig.IS_LANDSCAPE) {
            return
        }

        if (!view!!.onNewInitView()) {
            return
        }

        initBookState()
        // 初始化view
        view?.initView(mReaderViewModel!!)
        // 初始化监听器
        initListener()
        if (BaseBookApplication.getDownloadService() == null) {
            BookHelper.reStartDownloadService()
        }
        changeMode(ReadConfig.MODE)
    }

    /**
     * 处理书籍状态
     */
    private fun initBookState() {
        // 判断是否订阅
        mBookDaoHelper = BookDaoHelper.getInstance()
        isSubed = mBookDaoHelper!!.isBookSubed(ReadState.book_id)
        AppLog.e(TAG, "初始化书籍状态: " + ReadState.book_id)
        bookChapterDao = BookChapterDao(readReference?.get()?.applicationContext, ReadState.book_id)
        if (isSubed) {
            ReadState.book = mBookDaoHelper!!.getBook(ReadState.book_id, 0)
        }
        if (ReadState.sequence < -1) {
            ReadState.sequence = -1
        } else if (isSubed && ReadState.sequence + 1 > ReadState.book.chapter_count) {
            ReadState.sequence = ReadState.book.chapter_count - 1
        }
    }

    fun onConfigurationChanged(catalogMarkFragment: CatalogMarkFragment?, optionHeader: ReadOptionHeader, count: Int) {
        this.lastMode = -1
        // 初始化窗口基本信息
        initWindow()
        AppLog.e(TAG, "onConfigurationChanged")

        mCatalogMarkPresenter = CatalogMarkPresenter()
        mCatalogMarkPresenter?.view = catalogMarkFragment

        mReadOptionPresenter = ReadOptionPresenter(readReference?.get()!!, mReaderViewModel!!)
        mReadOptionPresenter?.view = optionHeader

        view?.initPresenter(mReadOptionPresenter, mCatalogMarkPresenter)

        initBookState()
        // 初始化view
        if (count == 0) {
            view?.initView(mReaderViewModel!!)
        } else {
            //重绘屏幕
//            view?.onChangedScreen()
        }

        // 初始化监听器
        initListener()
        if (BaseBookApplication.getDownloadService() == null) {
            BookHelper.reStartDownloadService()
        }
        setMode()
        ReadState.chapterCount = ReadState.book?.chapter_count

        changeMode(ReadConfig.MODE)
    }

    private fun getSavedState(savedInstanceState: Bundle?) {
        val bundle = readReference?.get()?.intent?.extras
        if (savedInstanceState != null) {
            // 获取本书
            // 获取当前章
            getReaderState(savedInstanceState)

            mReaderViewModel?.mReadDataListener = this

            AppLog.e(TAG, "getState1" + ReadState.sequence)
        } else {
            getReaderState(bundle)

            ReadState.book_id = if (ReadState.book == null) "" else ReadState.book.book_id
            AppLog.e(TAG, "getState2" + ReadState.sequence)
        }

        if (ReadState.sequence == -2) {
            ReadState.sequence = -1
        }
    }

    private fun getReaderState(readerState: Bundle?) {
        readerState?.let {
            ReadState.sequence = it.getInt("sequence", 0)

            // 书签偏移量
            ReadState.offset = it.getInt("offset", 0)
            // 获取本书
            ReadState.book = it.getSerializable("book") as Book

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
                readReference?.get()?.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                ReadConfig.IS_LANDSCAPE = false
            } else if (sp?.getInt("screen_mode", 3) == Configuration.ORIENTATION_LANDSCAPE && readReference?.get()?.getResources()!!
                    .getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                if (!is_dot_orientation) {
                    is_dot_orientation = true
                }
                readReference?.get()?.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                ReadConfig.IS_LANDSCAPE = true
            } else {
                if (!is_dot_orientation) {
                    is_dot_orientation = true
                }
            }
        }
    }


    fun dealManualDialogShow() {
        AppLog.d("IReadDataFactory", "Constants.manualReadedCount " + Constants.manualReadedCount)
        if (Constants.manualReadedCount != 0) {
            if (Constants.manualReadedCount == Constants.manualTip) {
                AppLog.d("IReadDataFactory", "显示自动阅读提醒")
                if (myNovelHelper != null) {
                    myNovelHelper?.showHintAutoReadDialog()
                }
            }
        }
    }


    override fun showChangeNetDialog() {
        val act: ReadingActivity? = readReference?.get() ?: return
        StatServiceUtils.statAppBtnClick(act, StatServiceUtils.read_limit)
        if (!act?.isFinishing!!) {
            myDialog = MyDialog(act, R.layout.nonet_read_dialog)
            myDialog?.setCanceledOnTouchOutside(false)
            val nonet_read_bookshelf = myDialog?.findViewById(R.id.nonet_read_backtoshelf) as ImageButton
            val nonet_read_continue = myDialog?.findViewById(R.id.nonet_read_continue) as ImageButton

            nonet_read_bookshelf.setOnClickListener {
                StatServiceUtils.statAppBtnClick(act, StatServiceUtils.rb_click_change_source_read)
                val shelfIntent = Intent()
                shelfIntent.setClass(act, HomeActivity::class.java)
                try {
                    StatServiceUtils.statAppBtnClick(act, StatServiceUtils.read_limit_bookshelf)
                    val bundle = Bundle()
                    bundle.putInt(EventBookStore.BOOKSTORE, EventBookStore.TYPE_TO_BOOKSHELF)
                    shelfIntent.putExtras(bundle)
                    act.startActivity(shelfIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                act.finish()
            }
            nonet_read_continue.setOnClickListener {
                StatServiceUtils.statAppBtnClick(act, StatServiceUtils.rb_click_change_source_ok)
                if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
                    StatServiceUtils.statAppBtnClick(act, StatServiceUtils.read_limit_continue)
                    intoSystemSetting()
                } else {
                    myDialog!!.dismiss()
                }
            }
            myDialog!!.setOnKeyListener { dialogInterface, keyCode, keyEvent ->
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    act.finish()

                }
                return@setOnKeyListener false
            }
            if (!myDialog?.isShowing!!) {
                try {
                    myDialog?.show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun intoSystemSetting() {
        readReference?.get()?.startActivity(Intent(Settings.ACTION_SETTINGS))
    }

    /**
     * 初始化窗口基本信息
     */
    private fun initWindow() {

        val display = readReference?.get()?.getWindowManager()!!.getDefaultDisplay()
        val realSize = Point()
        val dm = readReference?.get()?.getResources()!!.getDisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(realSize)
            ReadConfig.screenWidth = realSize.x
            ReadConfig.screenHeight = realSize.y
        } else {
            ReadConfig.screenWidth = dm.widthPixels
            ReadConfig.screenHeight = dm.heightPixels
        }
        ReadConfig.screenDensity = dm.density
        ReadConfig.screenScaledDensity = dm.scaledDensity
        // 保存字体、亮度、阅读模式
        modeSp = readReference?.get()?.getSharedPreferences("config", Context.MODE_PRIVATE)
        // 设置字体
        if (sp?.contains("novel_font_size")!!) {
            ReadConfig.FONT_SIZE = sp?.getInt("novel_font_size", 18) ?: 18
        } else {
            ReadConfig.FONT_SIZE = 18
        }

        //ViewModel
        mReaderViewModel = ReaderViewModel(ReaderRepositoryFactory.getInstance(ReaderOwnRepository.getInstance()), BookCoverRepositoryFactory.getInstance(BookCoverOtherRepository.getInstance(NetService.userService),
                BookCoverQGRepository.getInstance(OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext())), BookCoverLocalRepository.getInstance(BaseBookApplication.getGlobalContext())))

        //换源监听
        mReaderViewModel?.setReaderBookSourceViewCallback(object : ReaderViewModel.ReaderBookSourceViewCallback {
            override fun onBookSource(sourceItem: SourceItem) {
                searchChapterCallBack(sourceItem.sourceList)
                view?.readOptionHeaderDismiss()
            }

            override fun onBookSourceFail(msg: String?) {
                view?.readOptionHeaderDismiss()
            }
        })
        mReaderViewModel?.mReadDataListener = this
    }

    /**
     * 初始化监听器
     */
    private fun initListener() {
        if (downloadService == null) {
            reStartDownloadService()
            downloadService = BaseBookApplication.getDownloadService()
        } else {
            downloadService!!.setOnDownloadListener(this)
        }
    }

    private fun reStartDownloadService() {
        val intent = Intent()
        intent.setClass(mContext, DownloadService::class.java)
        mContext.startService(intent)
        mContext.bindService(intent, sc, Context.BIND_AUTO_CREATE)
    }

    /**
     * 预加载
     */
    private fun downloadNovel() {
        if (mBookDaoHelper!!.isBookSubed(ReadState.book_id)) {
            var num = BookHelper.CHAPTER_CACHE_COUNT
            val max = ReadState.chapterCount - 1 - ReadState.sequence
            if (max > 0) {
                if (max < num) {
                    num = max
                }

                //预加载
                val size = mReaderViewModel!!.chapterList!!.size
                var i = ReadState.sequence + 1
                while (i < ReadState.sequence + num + 1 && i < size) {
                    val c = mReaderViewModel?.chapterList!![i]
                    if (c != null) {
                        AppLog.e(TAG, "预加载： " + c.toString())
                        val finalI = i
                        mReaderViewModel!!.requestSingleChapter(ReadState.book.site, c, object : ReaderViewModel.BookSingleChapterCallback {
                            override fun onPayChapter(chapter: Chapter) {
                                if (finalI == ReadState.sequence + 1) {
                                    mReaderViewModel!!.nextChapter = c
                                }
                            }

                            override fun onFail(msg: String) {}
                        })
                    }
                    i++
                }
            }
        }

    }

    //换源回调
    fun searchChapterCallBack(sourcesList: ArrayList<Source>?) {
        if (sourcesList?.isNotEmpty() == true) {
            myNovelHelper?.showSourceDialog(ReadState.currentChapter?.curl, sourcesList)
        } else {
            readReference?.get()?.showToastShort("暂无其它来源")
        }
    }

    /**
     * 打开目录页面
     */
    private fun openCategoryPage() {
        if (ReadState.isMenuShow) {
            showMenu(false)
        }
        if (ReadState.book.book_type == 0) {
            val intent = Intent(readReference?.get(), CataloguesActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable("cover", ReadState.book)
            bundle.putString("book_id", ReadState.book_id)
            AppLog.e(TAG, "OpenCategoryPage: " + ReadState.sequence)
            bundle.putInt("sequence", ReadState.sequence)
            bundle.putBoolean("fromCover", false)
            AppLog.e(TAG, "ReadingActivity: " + ReadState.requestItem.toString())
            bundle.putSerializable(Constants.REQUEST_ITEM, ReadState.requestItem)
            intent.putExtras(bundle)
            readReference?.get()?.startActivityForResult(intent, 1)
        }
    }

    override fun showDisclaimerActivity() {
        val intent = Intent(readReference?.get(), DisclaimerActivity::class.java)
        try {
            intent.putExtra("isFromReadingPage", true)
            readReference?.get()?.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun showCatalogActivity(source: Source?) {
        if (ReadState.requestItem != null) {
            if (source != null && !TextUtils.isEmpty(source.book_source_id) && ReadState.book != null) {
                if (mBookDaoHelper != null && mBookDaoHelper!!.isBookSubed(ReadState.book.book_id)) {
                    val iBook = mBookDaoHelper!!.getBook(ReadState.book.book_id, 0)
                    if (source.book_source_id != iBook.book_source_id) {
                        //弹出切源提示
                        showChangeSourceNoticeDialog(source)
                        return
                    }
                }
            }
            intoCatalogActivity(source!!, false)
        }
    }

    private fun showChangeSourceNoticeDialog(source: Source) {
        if (readReference != null && readReference!!.get() != null && !readReference!!.get()!!.isFinishing()) {
            myDialog = MyDialog(readReference!!.get(), R.layout.publish_hint_dialog)
            myDialog!!.setCanceledOnTouchOutside(true)
            val dialog_cancel = myDialog!!.findViewById(R.id.publish_stay) as Button
            dialog_cancel.setText(R.string.book_cover_continue_read_cache)
            val dialog_confirm = myDialog!!.findViewById(R.id.publish_leave) as Button
            dialog_confirm.setText(R.string.book_cover_confirm_change_source)
            val dialog_information = myDialog!!.findViewById(R.id.publish_content) as TextView
            dialog_information.setText(R.string.book_cover_change_source_prompt)
            dialog_cancel.setOnClickListener {
                StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.rb_click_change_source_read)
                val map1 = HashMap<String, String>()
                map1.put("type", "2")
                StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.READPAGEMORE_PAGE, StartLogClickUtil.READ_SOURCECHANGECONFIRM, map1)

                dismissDialog()
            }
            dialog_confirm.setOnClickListener {
                StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.rb_click_change_source_ok)
                val map2 = HashMap<String, String>()
                map2.put("type", "1")
                StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.READPAGEMORE_PAGE, StartLogClickUtil.READ_SOURCECHANGECONFIRM, map2)
                dismissDialog()
                intoCatalogActivity(source, true)
            }

            myDialog!!.setOnCancelListener { myDialog!!.dismiss() }
            if (!myDialog!!.isShowing) {
                try {
                    myDialog!!.show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }

    private fun intoCatalogActivity(source: Source, b: Boolean) {
        if (ReadState.requestItem != null) {

            val bookDaoHelper = BookDaoHelper.getInstance()
            if (bookDaoHelper.isBookSubed(source.book_id)) {
                val iBook = bookDaoHelper.getBook(source.book_id, 0)
                iBook.book_source_id = source.book_source_id
                iBook.site = source.host
                iBook.last_updatetime_native = source.update_time
                iBook.dex = source.dex
                bookDaoHelper.updateBook(iBook)
                ReadState.book = iBook
                if (b) {
                    val bookChapterDao = BookChapterDao(readReference?.get(), source.book_id)
                    BookHelper.deleteAllChapterCache(source.book_id, 0, bookChapterDao.count)
                    DownloadService.clearTask(source.book_id)
                    BaseBookHelper.delDownIndex(readReference?.get(), source.book_id)
                    bookChapterDao.deleteBookChapters(0)

                }
            } else {
                val iBook = ReadState.book
                iBook.book_source_id = source.book_source_id
                iBook.site = source.host
                iBook.dex = source.dex
                iBook.parameter = ReadState.book.parameter
                iBook.extra_parameter = ReadState.book.extra_parameter
                ReadState.book = iBook
            }
            mReaderViewModel?.chapterList?.clear()
            openCategoryPage()
        }
    }

    private fun dismissDialog() {
        if (myDialog?.isShowing!!) {
            myDialog!!.dismiss()
        }
    }

    override fun deleteBook() {
        if (mBookDaoHelper!!.isBookSubed(ReadState.book_id)) {
            mBookDaoHelper!!.deleteBook(ReadState.book_id)
        }
        readReference?.get()?.finish()
    }

    override fun openAutoReading(open: Boolean) = onReadAuto()
    override fun changSource() {
//        openSourcePage()
    }

    override fun addBookShelf(isAddShelf: Boolean) {
        if (isAddShelf && mBookDaoHelper != null && ReadState.book != null) {
            ReadState.book.sequence = ReadState.sequence
            ReadState.book.offset = ReadState.offset
            ReadState.book.sequence_time = System.currentTimeMillis()
            ReadState.book.last_updateSucessTime = System.currentTimeMillis()
            ReadState.book.readed = 1
            if (mReaderViewModel != null) {
                if (mReaderViewModel?.chapterList != null && mReaderViewModel!!.chapterList!!.size > 0) {
                    val chapter = mReaderViewModel!!.chapterList!![mReaderViewModel!!.chapterList!!.size - 1]
                    ReadState.book.extra_parameter = chapter.extra_parameter
                }
                bookChapterDao?.insertBookChapter(mReaderViewModel?.chapterList)
            }
            val succeed = mBookDaoHelper?.insertBook(ReadState.book)
            Toast.makeText(readReference?.get(), if (succeed!!) R.string.reading_add_succeed else R.string.reading_add_fail,
                    Toast.LENGTH_SHORT).show()
        }
        val map1 = HashMap<String, String>()
        if (ReadState.book != null) {
            map1.put("bookid", ReadState.book.book_id)
        }
        if (mReaderViewModel != null && ReadState.currentChapter != null) {
            map1.put("chapterid", ReadState.currentChapter!!.chapter_id)
        }
        if (isAddShelf) {
            StartLogClickUtil.upLoadEventLog(readReference?.get(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.POPUPSHELFADD, map1)
        } else {
            StartLogClickUtil.upLoadEventLog(readReference?.get(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.POPUPSHELFADDCANCLE, map1)
        }

        goBackToHome()
    }

    /**
     * 开始下载
     */
    private fun startDownLoad() {
        if (!isSubed) {
            val succeed = mBookDaoHelper?.insertBook(ReadState.book)!!
            if (succeed) {
                isSubed = true
            } else {
                return
            }
        }
        if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
            readReference?.get()?.showToastShort("网络不给力，请稍后再试")
            return
        }
        myNovelHelper?.clickDownload(readReference?.get(), ReadState.book, Math.max(ReadState.sequence, 0))
    }

    /**
     * 横屏切换
     */
    fun changeScreenMode() {
        showMenu(false)
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
            act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            screen_mode.putInt("screen_mode", Configuration.ORIENTATION_PORTRAIT)
        } else if (act.resources!!.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.rb_click_landscape_btn)
            val data = HashMap<String, String>()
            data.put("type", "1")
            StartLogClickUtil.upLoadEventLog(readReference?.get(), StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.HPMODEL, data)
            act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            isFromCover = false
            screen_mode.putInt("screen_mode", Configuration.ORIENTATION_LANDSCAPE)
        }
        screen_mode.apply()
    }

    fun changeSourceCallBack() {

        ReadState.currentPage = 1
        ReadState.offset = 0
        myNovelHelper?.isShown = false
        myNovelHelper?.getChapterContent(readReference?.get(), ReadState.currentChapter, ReadState.book)
        isSourceListShow = false

        downloadNovel()
    }

    /**
     * 跳章
     */
    fun jumpChapterCallBack() {
        Constants.readedCount++
        if (mReaderViewModel == null || myNovelHelper == null) {
            return
        }
        mReaderViewModel?.nextChapter = null
        ReadState.offset = 0
        myNovelHelper?.isShown = false
        myNovelHelper?.getChapterContent(readReference?.get(), ReadState.currentChapter, ReadState.book)
        ReadState.currentPage = 1

        downloadNovel()
    }

    /**
     * 清空屏幕
     */
    fun clearOtherPanel() {
        isSourceListShow = false
    }

    /**
     * 隐藏topmenu
     */
    fun dismissTopMenu() {
        mReadOptionPresenter?.view?.show(false)
        full(true)
    }

    // 全屏切换
    private fun full(enable: Boolean) {
        if (!Constants.isFullWindowRead) {
            return
        }
        view?.full(enable)
    }

    /*
     * 显示隐藏菜单
     */
    fun showMenu(isShow: Boolean) {
        clearOtherPanel()
        if (isShow) {
            full(false)
            changeMarkState()
            mReadOptionPresenter?.view?.show(true)
            view?.initSettingGuide()
        } else {
            full(true)
            mReadOptionPresenter!!.view!!.show(false)
        }
    }

    /**
     * mode 设定文件
     * void 返回类型
     * 切换夜间模式
     */
    private fun changeMode(mode: Int) {
        if (this.lastMode == -1) {
            this.lastMode = mode
        } else {
            if (this.lastMode == mode) {
                return
            } else {
                this.lastMode = mode
            }
        }

        this.current_mode = mode
        AppLog.e(TAG, "ChangeMode : " + mode)
        val editor = modeSp?.edit()
        if (mode == 61) {
            if ("light" == ResourceUtil.mode) {
                editor?.putString("mode", "night")
                ResourceUtil.mode = "night"
                editor?.apply()
                setMode()
            }
        } else {
            if ("night" == ResourceUtil.mode) {
                editor?.putString("mode", "light")
                ResourceUtil.mode = "light"
                editor?.apply()
                setMode()
            }
        }
    }



    fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // 小说音量键翻页
        if (Constants.isVolumeTurnover) {
//            if (pageView != null && pageView!!.setKeyEvent(event)) {
//                return true
//
        }
        return false
    }

    fun onKeyDown() {

    }

    fun onBackPressed(): Boolean {

        if (isSourceListShow) {
            isSourceListShow = false
            return false
        }

        // 显示菜单
        if (ReadState.isMenuShow) {
            showMenu(false)
            return true
        }

        if (mBookDaoHelper != null) {
            isSubed = mBookDaoHelper!!.isBookSubed(ReadState.book_id)
        }

        if (mBookDaoHelper != null && !isSubed) {
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

    fun setMode() {
        view?.setMode()
    }

    fun onResume() {

        // 设置全屏
        if (isFromCover && ReadConfig.IS_LANDSCAPE) {
            return
        }
        view?.checkModeChange()

        if (isSubed) {
            ReadState.book = mBookDaoHelper!!.getBook(ReadState.book_id, 0)
        }

        ReadState.chapterCount = ReadState.book.chapter_count


        val lock = sp!!.getInt("lock_screen_time", 5)
        if (lock == Integer.MAX_VALUE) {
            Constants.screenOffTimeout = lock
        } else {
            Constants.screenOffTimeout = lock * 60 * 1000
        }
        readReference?.get()?.setScreenOffTimeout(Constants.screenOffTimeout)
        if (!actNovelRunForeground && !isRestDialogShow) {
            actNovelRunForeground = true
        }
        if (!isAcvNovelActive && !isRestDialogShow) {
            isAcvNovelActive = true
        }

    }

    fun onStart() {
        if (mCacheUpdateReceiver == null) {
            mCacheUpdateReceiver = CacheUpdateReceiver(readReference!!)
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(BaseCacheableActivity.ACTION_CACHE_COMPLETE)
        LocalBroadcastManager.getInstance(readReference?.get()).registerReceiver(mCacheUpdateReceiver, intentFilter)
    }

    fun onPause(mCurPageSequence: Int, mCurPageOffset: Int) {
        isFromCover = false
        if (isSubed) {
            if (ReadState.book.book_type == 0) {
                myNovelHelper?.saveBookmark(ReadState.book_id, mCurPageSequence,
                        mCurPageOffset, mBookDaoHelper)
                // 统计阅读章节数
                val spUtils = SharedPreferencesUtils(PreferenceManager
                        .getDefaultSharedPreferences(readReference?.get()))
                spUtils.putInt("readed_count", Constants.readedCount)
            }
        }

    }

    fun onStop() {
        LocalBroadcastManager.getInstance(readReference?.get()).unregisterReceiver(mCacheUpdateReceiver)
        actNovelRunForeground = false
    }

    fun onDestroy() {

        handler.removeCallbacksAndMessages(null)

        for (d in disposable) {
            d.dispose()
        }
        disposable.clear()
    }

    fun onSaveInstanceState(outState: Bundle): Bundle? {
        // 保存书签状态
        try {
            outState.putInt("sequence", ReadState.sequence)
            outState.putInt("offset", ReadState.offset)
            outState.putSerializable("book", ReadState.book)
            if (mReaderViewModel != null && ReadState.currentChapter != null) {
                outState.putSerializable("currentChapter", ReadState.currentChapter)
            }
            outState.putString("thememode", readReference?.get()?.mThemeHelper?.getMode())
            return outState
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
        return null

    }

    fun goToBookOver() {
        if (readReference == null || readReference!!.get() == null || readReference!!.get()!!.isFinishing) {
            return
        }

        if (ReadState.sequence != ReadState.chapterList.size - 1) {
            return
        }

        val intent = Intent(readReference?.get(), BookEndActivity::class.java)
        val bundle = Bundle()
        bundle.putSerializable(Constants.REQUEST_ITEM, ReadState.requestItem)
        bundle.putString("bookName", ReadState.book.name)
        bundle.putString("book_id", ReadState.book_id)
        bundle.putString("book_category", ReadState.book.category)
        bundle.putSerializable("book", ReadState.book)
        bundle.putString("thememode", currentThemeMode)
        intent.putExtras(bundle)
        readReference?.get()?.startActivity(intent)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {// 更新目录后，重新获取chapterList
                if (requestCode == 1) {
                    showMenu(false)
                }
                val bundle = data.extras
                ReadState.sequence = bundle.getInt("sequence")
                ReadState.offset = bundle.getInt("offset", 0)
                ReadState.book = bundle.getSerializable("book") as Book
                val requestItem = bundle.getSerializable(Constants.REQUEST_ITEM) as RequestItem
                AppLog.e(TAG, "onActivityResult: " + requestItem.toString())
                ReadState.book.book_source_id = requestItem.book_source_id

                AppLog.e(TAG, "from" + ReadState.requestItem.fromType + "===")

                if (mReaderViewModel!!.chapterList != null) {
                    mReaderViewModel!!.chapterList!!.clear()
                }
                myNovelHelper?.isShown = false
                ReadState.currentPage = 1
                mReaderViewModel?.nextChapter = null
                mReaderViewModel?.preChapter = null
                ReadState.requestItem.fromType = 1//打点 书籍封面（0）/书架（1）/上一页翻页（2）
                if (Constants.QG_SOURCE == ReadState.book.site) {
                    requestItem?.channel_code = 1
                } else {
                    requestItem?.channel_code = 2
                }
                val intent = Intent(act, ReadingActivity::class.java)
                val extras = Bundle()
                extras.putInt("sequence", ReadState.sequence)
                extras.putInt("offset", ReadState.offset)
                extras.putSerializable("book", ReadState.book)
                extras.putSerializable(Constants.REQUEST_ITEM, requestItem)
                intent.putExtras(extras)
                act.startActivity(intent)
            }
        }
    }

    override fun notificationCallBack(preNTF: Notification, book_id: String) {
        if (readReference == null || readReference!!.get() == null) {
            return
        }
        var pending: PendingIntent? = null
        var intent: Intent? = null
        if (book_id != (-1).toString() + "") {
            intent = Intent(readReference?.get(), DownBookClickReceiver::class.java)
            intent.action = DownBookClickReceiver.action
            intent.putExtra("book_id", book_id)
            pending = PendingIntent.getBroadcast(readReference?.get()?.applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            intent = Intent(readReference?.get(), DownloadManagerActivity::class.java)
            pending = PendingIntent.getActivity(readReference?.get()?.applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        preNTF.contentIntent = pending
    }

    override fun jumpNextChapter() {
        if (ReadState.isMenuShow) {
            showMenu(false)
            return
        }
    }



    override fun gotoOver() {
        goToBookOver()
    }

    override fun showToast(str: Int) {
        readReference?.get()?.showToastShort(str)
    }

    override fun downLoadNovelMore() {
        downloadNovel()
    }

    override fun initBookStateDeal() {
        //        initPageMode();// 翻页模式
        // 加载字体、亮度、阅读模式信息
        view?.initShowCacheState()
        // 初始化时间显示

        // 刷新页面
        // 刷新内容显示
        // 启动预加载
        downloadNovel()
    }

    override fun changeChapter() {
        view?.changeChapter()
        changeMarkState()
    }

    fun onReadCatalog() {
        if (ReadState.isMenuShow) {
            showMenu(false)
        }
        val data = HashMap<String, String>()

        ReadState.book?.let {
            data.put("bookid", it.book_id)
        }
        ReadState.chapterId?.let {
            data.put("chapterid", it)
        }

        StartLogClickUtil.upLoadEventLog(readReference?.get(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.CATALOG, data)
    }

    fun onReadChangeSource() {
        if (Book.isOnlineType(ReadState.book.book_type)) {
//            openSourcePage()
        }
    }

    fun onReadCache() {
        if (Book.isOnlineType(ReadState.book.book_type)) {
            startDownLoad()
        }
    }

    fun onReadAuto() {
        if (System.currentTimeMillis() - stampTime < 1000) {
            return
        }
        stampTime = System.currentTimeMillis()
        isSlideToAuto = Constants.isSlideUp

//        pageView?.startAutoRead()
        showMenu(false)
    }

    fun onChangeMode(mode: Int) {
        changeMode(mode)
    }


    fun onJumpPreChapter() {

        changeMarkState()

        Constants.manualReadedCount++
        dealManualDialogShow()

        jumpNextChapterLog(1)
    }

    fun onJumpNextChapter() {

        changeMarkState()

        Constants.manualReadedCount++
        dealManualDialogShow()

        jumpNextChapterLog(2)
    }

    private fun jumpNextChapterLog(type: Int) {
        val data = HashMap<String, String>()
        ReadState.book?.let {
            data.put("bookid", it.book_id)
        }
        ReadState.chapterId?.let {
            data.put("chapterid", it)
        }
        data.put("type", type.toString())
        StartLogClickUtil.upLoadEventLog(readReference?.get(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.CHAPTERTURN, data)
    }

    fun onReadFeedBack() {
        if (readReference != null && readReference!!.get() != null && !readReference!!.get()!!.isFinishing()) {
            if (ReadState.sequence == -1) {
                readReference?.get()?.showToastShort("请到错误章节反馈")
                return
            }
            myDialog = MyDialog(readReference?.get(), R.layout.dialog_feedback)
            myDialog!!.setCanceledOnTouchOutside(true)
            val dialog_title = myDialog!!.findViewById(R.id.dialog_title) as TextView
            dialog_title.setText(R.string.read_bottom_feedback)
            val checkboxsParent = myDialog!!.findViewById(R.id.feedback_checkboxs_parent) as LinearLayout
            val checkboxs = arrayOfNulls<CheckBox>(7)
            val relativeLayouts = arrayOfNulls<RelativeLayout>(7)
            var index = 0
            for (i in 0..checkboxsParent.childCount - 1) {
                val relativeLayout = checkboxsParent.getChildAt(i) as RelativeLayout
                relativeLayouts[i] = relativeLayout
                relativeLayouts[i]!!.setTag(i)
                for (j in 0..relativeLayout.childCount - 1) {
                    val v = relativeLayout.getChildAt(j)
                    if (v is CheckBox) {
                        checkboxs[index] = v
                        index++
                    }
                }
            }

            if (ReadConfig.IS_LANDSCAPE) {
                myDialog!!.findViewById(R.id.sv_feedback).layoutParams.height = readReference?.get()?.getResources()!!.getDimensionPixelOffset(R.dimen.dimen_view_height_160)
            } else {
                myDialog!!.findViewById(R.id.sv_feedback).layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT
            }

            for (relativeLayout in relativeLayouts) {
                relativeLayout!!.setOnClickListener(View.OnClickListener { v ->
                    for (checkBox in checkboxs) {
                        checkBox!!.setChecked(false)
                    }
                    checkboxs[v.tag as Int]!!.setChecked(true)
                })
            }
            val submitButton = myDialog!!.findViewById(R.id.feedback_submit) as Button
            submitButton.setOnClickListener {
                StatServiceUtils.statAppBtnClick(readReference?.get()?.getApplicationContext(), StatServiceUtils.rb_click_feedback_submit)
                for (n in checkboxs.indices) {
                    if (checkboxs[n]!!.isChecked()) {
                        type = n + 1
                    }
                }
                if (type == -1) {
                    readReference?.get()?.showToastShort("请选择错误类型")
                } else {
                    //                        data.put("type", "1");
                    //						StartLogClickUtil.upLoadEventLog(ReadingActivity.this, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.REPAIRDEDIALOGUE, data);
                    submitFeedback(type)
                    dismissDialog()
                    type = -1
                }
            }

            val cancelImage = myDialog!!.findViewById(R.id.feedback_cancel) as Button
            cancelImage.setOnClickListener {
                //                    data.put("type", "2");
                //                    StartLogClickUtil.upLoadEventLog(ReadingActivity.this, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.REPAIRDEDIALOGUE, data);
                dismissDialog()
            }

            if (!myDialog!!.isShowing) {
                try {
                    myDialog!!.show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }

    fun onChageNightMode() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(readReference?.get()?.applicationContext)
        val edit = sharedPreferences.edit()
        val data = HashMap<String, String>()

        if (readReference?.get()?.mThemeHelper!!.isNight) {
            //夜间模式只有一种背景， 不能存储
            //            edit.putInt("current_night_mode", ReadConfig.MODE);
            ReadConfig.MODE = sharedPreferences.getInt("current_light_mode", 51)
            readReference?.get()?.mThemeHelper?.setMode(ThemeMode.THEME1)
            data.put("type", "2")
            StartLogClickUtil.upLoadEventLog(readReference?.get()?.getApplicationContext(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.NIGHTMODE1, data)
        } else {
            edit.putInt("current_light_mode", ReadConfig.MODE)
            //            ReadConfig.MODE = sharedPreferences.getInt("current_night_mode", 61);
            //夜间模式只有一种背景
            ReadConfig.MODE = 61
            readReference?.get()?.mThemeHelper?.setMode(ThemeMode.NIGHT)
            data.put("type", "1")
            StartLogClickUtil.upLoadEventLog(readReference?.get()?.getApplicationContext(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.NIGHTMODE1, data)
        }
        edit.putInt("content_mode", ReadConfig.MODE)
        edit.apply()
        changeMode(ReadConfig.MODE)
    }

    private fun submitFeedback(type: Int) {
        if (NetWorkUtils.getNetWorkType(readReference?.get()) == NetWorkUtils.NETWORK_NONE) {
            readReference?.get()?.showToastShort("网络异常")
            return
        }
        val chapterErrorBean = ChapterErrorBean()
        val book = ReadState.book
        chapterErrorBean.bookName = getEncode(book.name)
        chapterErrorBean.author = getEncode(book.author)
        chapterErrorBean.channelCode = if (Constants.QG_SOURCE == book.site) "1" else "2"
        val bookChapterDao = BookChapterDao(readReference?.get(), book.book_id)
        val currChapter = bookChapterDao.getChapterBySequence(ReadState.sequence)
        if (currChapter == null) {
            val time = Observable.timer(1000, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        readReference?.get()?.showToastShort("已发送")
                    }, { e -> e.printStackTrace() })
            disposable.add(time)
//            handler.postDelayed({ readReference?.get()?.showToastShort("已发送") }, 1000)
            return
        }
        chapterErrorBean.bookSourceId = if (TextUtils.isEmpty(currChapter.book_source_id)) book.book_source_id else currChapter.book_source_id
        chapterErrorBean.chapterId = if (TextUtils.isEmpty(currChapter.chapter_id)) "" else currChapter.chapter_id
        chapterErrorBean.chapterName = getEncode(currChapter.chapter_name)
        chapterErrorBean.host = currChapter.site
        chapterErrorBean.serial = currChapter.sequence
        chapterErrorBean.type = type
        val curl = currChapter.curl
        if (!TextUtils.isEmpty(curl)) {
            if (curl.contains("/V1/book/")) {
                val s = book.book_id + "/"
                val start = curl.indexOf(s) + s.length
                val end = curl.indexOf("/", start)
                chapterErrorBean.bookChapterId = curl.substring(start, end)
                AppLog.i(TAG, "chapterErrorBean.bookChapterId = " + chapterErrorBean.bookChapterId)
            }
        }
        if (TextUtils.isEmpty(chapterErrorBean.bookChapterId)) {
            chapterErrorBean.bookChapterId = ""
        }
        if (TextUtils.isEmpty(chapterErrorBean.host)) {
            chapterErrorBean.host = ""
        }
        AppLog.i(TAG, "chapterErrorBean = " + chapterErrorBean.toString())
        val loadDataManager = LoadDataManager(readReference?.get())
        loadDataManager.submitBookError(chapterErrorBean)
        StartLogClickUtil.upLoadChapterError(chapterErrorBean)
        val time = Observable.timer(1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    readReference?.get()?.showToastShort("已发送")
                }, { e -> e.printStackTrace() })
        disposable.add(time)
    }

    private fun getEncode(content: String): String {
        if (!TextUtils.isEmpty(content)) {
            try {
                return URLEncoder.encode(content, "utf-8")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        }
        return ""
    }

    private fun changeMarkState() {
        mReadOptionPresenter?.updateStatus()
    }

    fun goBackToHome() {
        val act = readReference?.get() ?: return
        if (!currentThemeMode.equals(act.mThemeHelper.getMode())) {
            val themIntent = Intent(readReference?.get(), HomeActivity::class.java)
            val bundle = Bundle()
            bundle.putInt(EventBookStore.BOOKSTORE, EventBookStore.TYPE_TO_SWITCH_THEME)
            themIntent.putExtras(bundle)
            act.startActivity(themIntent)
//            act.overridePendingTransition(R.anim.activity_in, R.anim.activity_out)
            act.finish()
        } else {
            if (act.isTaskRoot) {
                val intent = Intent(act, SplashActivity::class.java)
                act.startActivity(intent)
            }
            act.finish()
        }
    }

    fun onOriginClick() {
        var url: String? = null
        if (mReaderViewModel != null && ReadState.currentChapter != null) {
            url = UrlUtils.buildContentUrl(ReadState.currentChapter!!.curl)
        }
        if (!TextUtils.isEmpty(url)) {
            val uri = Uri.parse(url!!.trim { it <= ' ' })
            val intent = Intent(Intent.ACTION_VIEW, uri)
            try {
                readReference?.get()?.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val data = HashMap<String, String>()
            data.put("bookid", ReadState.book_id)
            StartLogClickUtil.upLoadEventLog(readReference?.get(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.ORIGINALLINK, data)
        } else {
            Toast.makeText(readReference?.get(), "无法查看原文链接", Toast.LENGTH_SHORT).show()
        }
    }

    fun onTransCodingClick() {
        showDisclaimerActivity()
    }

    inner class CacheUpdateReceiver(weak: WeakReference<ReadingActivity>) : BroadcastReceiver() {
        private val mActivityWeakReference: WeakReference<ReadingActivity> = weak
        override fun onReceive(context: Context, intent: Intent) {
            val book = intent.getSerializableExtra(Constants.REQUEST_ITEM) as Book
            if (Constants.QG_SOURCE != book.site) {
                if (mActivityWeakReference.get() != null && ReadState.book.book_id == book.book_id) {
                    val bundle = Bundle()
                    bundle.putInt("sequence", ReadState.sequence)
                    bundle.putInt("offset", ReadState.offset)
                    bundle.putSerializable("book", ReadState.book)
                    bundle.putSerializable(Constants.REQUEST_ITEM, ReadState.requestItem)
                    val fresh = Intent(mActivityWeakReference.get(), ReadingActivity::class.java)
                    fresh.putExtras(bundle)
                    fresh.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    mActivityWeakReference.get()?.startActivity(fresh)
                }
            }
        }
    }

    var intervalRunnable: Runnable? = null
    fun startRestInterval() {
        val runtime = if (PlatformSDK.config().switch_sec == 0) {
            30.times(60000).toLong()
        } else {
            PlatformSDK.config().switch_sec.times(60000).toLong()
        }
        if (intervalRunnable == null) {
            intervalRunnable = Runnable {
                restAd()
                handler.postDelayed(intervalRunnable, runtime)
            }
            handler.postDelayed(intervalRunnable, runtime)
        }
    }

    var timeRunnable: Runnable? = null
    fun startRestTimer() {
        if (timeRunnable == null) {
            timeRunnable = Runnable {
                restAd()
            }
            var runtime: Long = PlatformSDK.config().restAd_sec.times(60000).toLong()
            handler.postDelayed(timeRunnable, runtime)
            handler.postDelayed({
                handler.removeCallbacksAndMessages(timeRunnable)
                timeRunnable = null
            }, runtime.plus(100))
        }
    }

    var mDialog: MyDialog? = null

    fun restAd() {
        if (mDialog != null && mDialog!!.isShowing) {
            return
        }
        PlatformSDK.adapp().dycmNativeAd(readReference?.get(), "3-1", null, object : AbstractCallback() {
            override fun onResult(adswitch: Boolean, views: List<ViewGroup>?, jsonResult: String?) {
                super.onResult(adswitch, views, jsonResult)
                if (!adswitch) return
                try {
                    val jsonObject = JSONObject(jsonResult)
                    if (jsonObject.has("state_code")) {
                        when (ResultCode.parser(jsonObject.getInt("state_code"))) {
                            ResultCode.AD_REQ_SUCCESS -> {
                                mDialog = MyDialog(readReference?.get(), R.layout.reading_resttime, Gravity.CENTER, false)
                                mDialog?.let {
                                    val rest_ad = it.findViewById(R.id.rest_ad) as RelativeLayout//容器
                                    it.findViewById(R.id.iv_close).setOnClickListener { mDialog?.dismiss() }
                                    //广告 3-1
                                    rest_ad.addView(views?.get(0))
                                    rest_ad.postInvalidate()
                                    mDialog?.show()
                                }
                            }
                            ResultCode.AD_REQ_FAILED -> {
                            }
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }
}