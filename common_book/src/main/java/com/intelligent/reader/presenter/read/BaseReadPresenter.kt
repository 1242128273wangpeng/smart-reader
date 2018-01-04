package com.intelligent.reader.presenter.read

import android.app.Activity
import android.app.Notification
import android.app.PendingIntent
import android.content.*
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Point
import android.net.Uri
import android.os.*
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v4.content.LocalBroadcastManager
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.InflateException
import android.view.KeyEvent
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import com.intelligent.reader.R
import com.intelligent.reader.activity.*
import com.intelligent.reader.app.BookApplication
import com.intelligent.reader.cover.BookCoverLocalRepository
import com.intelligent.reader.cover.BookCoverOtherRepository
import com.intelligent.reader.cover.BookCoverQGRepository
import com.intelligent.reader.cover.BookCoverRepositoryFactory
import com.intelligent.reader.fragment.CatalogMarkFragment
import com.intelligent.reader.presenter.IPresenter
import com.intelligent.reader.read.animation.BitmapManager
import com.intelligent.reader.read.help.BookHelper
import com.intelligent.reader.read.help.CallBack
import com.intelligent.reader.read.help.NovelHelper
import net.lzbook.kit.data.bean.ReadConfig

import net.lzbook.kit.data.bean.ReadViewEnums
import com.intelligent.reader.read.page.*
import com.intelligent.reader.reader.ReaderOwnRepository
import com.intelligent.reader.reader.ReaderRepositoryFactory
import com.intelligent.reader.reader.ReaderViewModel
import com.intelligent.reader.receiver.DownBookClickReceiver
import com.intelligent.reader.util.EventBookStore
import iyouqu.theme.ThemeMode
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.component.service.DownloadService
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.*
import net.lzbook.kit.data.db.BookChapterDao
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.net.custom.service.NetService
import net.lzbook.kit.repair_books.RepairHelp
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.utils.*
import java.io.UnsupportedEncodingException
import java.lang.ref.WeakReference
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.Callable

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * Created by yuchao on 2017/11/14 0014.
 */
open class BaseReadPresenter(act: ReadingActivity) : IPresenter<ReadPreInterface.View>, NovelHelper.OnHelperCallBack, CallBack,
        PageInterface.OnOperationClickListener, DownloadService.OnDownloadListener, ReaderViewModel.ReadDataListener {

    //获取章节信息
    override fun getChapter(what: Int, sequence: Int) {
        getChapterByLoading(what, sequence)
    }

    /**
     * 翻页到下一章的处理
     */
    override fun nextChapterCallBack(drawCurrent: Boolean) {
        Constants.readedCount++
        mReaderViewModel?.preChapter = mReaderViewModel?.currentChapter
        mReaderViewModel?.currentChapter = mReaderViewModel?.nextChapter
        mReaderViewModel?.nextChapter = null
        mReaderViewModel?.readStatus!!.sequence++
        mReaderViewModel?.readStatus?.offset = 0
        myNovelHelper?.isShown = false
        myNovelHelper?.getChapterContent(readReference?.get(), mReaderViewModel?.currentChapter, mReaderViewModel?.readStatus?.book)
        mReaderViewModel?.readStatus?.currentPage = 1
        pageView?.drawNextPage()
        if (drawCurrent) {
            pageView?.drawCurrentPage()
        }
        pageView?.getNextChapter()
        if (mReaderViewModel?.mReadDataListener != null) {
            mReaderViewModel?.mReadDataListener?.downLoadNovelMore()
            downloadNovel()
        }
        if (mReaderViewModel?.mReadDataListener != null) {
            mReaderViewModel?.mReadDataListener?.freshPage()
            mReaderViewModel?.mReadDataListener?.changeChapter()
        }
        mReaderViewModel?.readStatus?.isLoading = false
    }

    /**
     * 翻页到上一章的处理
     */
    override fun preChapterCallBack(drawCurrent: Boolean) {
        Constants.readedCount++
        mReaderViewModel?.nextChapter = mReaderViewModel!!.currentChapter
        mReaderViewModel?.currentChapter = mReaderViewModel!!.preChapter
        mReaderViewModel?.preChapter = null

        mReaderViewModel?.readStatus!!.sequence--
        mReaderViewModel?.readStatus?.offset = 0
        myNovelHelper?.isShown = false
        myNovelHelper?.getChapterContent(readReference?.get(), mReaderViewModel?.currentChapter, mReaderViewModel?.readStatus?.book)
        if (mReaderViewModel?.toChapterStart != false) {
            mReaderViewModel?.readStatus?.currentPage = 1
        } else {
            mReaderViewModel?.readStatus?.currentPage = mReaderViewModel?.readStatus?.pageCount
        }
        mReaderViewModel?.toChapterStart = false
        pageView?.drawNextPage()
        if (drawCurrent) {
            pageView?.drawCurrentPage()
        }
        pageView?.getPreChapter()
        if (mReaderViewModel?.mReadDataListener != null) {
            mReaderViewModel?.mReadDataListener?.freshPage()
            mReaderViewModel?.mReadDataListener?.changeChapter()
        }
        mReaderViewModel?.readStatus?.isLoading = false

        if (mReaderViewModel?.readStatus?.currentPage == mReaderViewModel?.readStatus?.pageCount) {
        }
    }

    var disposable: ArrayList<Disposable> = ArrayList()
    protected val TAG = BaseReadPresenter::class.java.simpleName

    override var view: ReadPreInterface.View? = act

    val MSG_LOAD_CUR_CHAPTER = 0
    val MSG_LOAD_PRE_CHAPTER = 1
    val MSG_LOAD_NEXT_CHAPTER = 2
    val MSG_SEARCH_CHAPTER = 3
    val MSG_CHANGE_SOURCE = 4
    val MSG_JUMP_CHAPTER = 6
    val ERROR = 7
    val NEED_LOGIN = 8
    val MSG_SOURCE_CHANGE = 9
    // 手动书签内容限制
    protected val font_count = 50
    protected var readStatus: ReadStatus? = null
    var downloadService: DownloadService? = null
    var isRestDialogShow = false
    var stampTime: Long = 0
    protected var mContext: Context? = null
    protected var pageView: PageInterface? = null
    protected var sourcesList: ArrayList<Source>? = null
    protected var isSourceListShow: Boolean = false
    // 系统存储设置
    protected var sp: SharedPreferences? = null
    protected var modeSp: SharedPreferences? = null
    protected var isSubed: Boolean = false
    protected var mCalendar: Calendar? = null
    protected var mTimerStopped = false
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

    //    private int lastMode = -1;
    protected var loadingPage: LoadingPage? = null


    protected var isSlideToAuto = false
    protected var resources: Resources? = null
    protected var myDialog: MyDialog? = null
    protected var type = -1
    var currentThemeMode: String? = null
        get() = null
    private var lastMode = -1

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
        Constants.FULL_SCREEN_READ = sp?.getBoolean("full_screen_read", false) ?: false
        Constants.isSlideUp = Constants.PAGE_MODE == 3
        Constants.isVolumeTurnover = sp?.getBoolean("sound_turnover", true) ?: true
        AppLog.e("getAdsStatus", "novel_onCreate")
        versionCode = AppUtils.getVersionCode()
        AppLog.e(TAG, "versionCode: " + versionCode)
        readStatus = ReadStatus(readReference?.get()?.applicationContext)
        BookApplication.getGlobalContext().readStatus = readStatus
        view?.setReadStatus(readStatus!!)
        autoSpeed = readStatus?.autoReadSpeed()!!
        myNovelHelper = NovelHelper(readReference?.get(), readStatus)
        myNovelHelper?.setOnHelperCallBack(this)
        downloadService = BaseBookApplication.getDownloadService()

        // 初始化窗口基本信息
        initWindow()
        setOrientation()
        getSavedState(savedInstanceState)
        RepairHelp.showFixMsg(readReference?.get(), readStatus?.book, RepairHelp.FixCallBack {
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
        if (isFromCover && Constants.IS_LANDSCAPE) {
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

    }

    fun initCatalogPresenter(catalogMarkFragment: CatalogMarkFragment, optionHeader: ReadOptionHeader) {
        mCatalogMarkPresenter = CatalogMarkPresenter(readStatus!!, mReaderViewModel!!)
        mCatalogMarkPresenter?.view = catalogMarkFragment

        mReadOptionPresenter = ReadOptionPresenter(readReference?.get() as Activity, readStatus!!, mReaderViewModel!!)
        mReadOptionPresenter?.view = optionHeader

        view?.initPresenter(mReadOptionPresenter, mCatalogMarkPresenter)
    }

    fun onNewIntent(intent: Intent) {
        this.lastMode = -1
        pageView?.clear()
        showMenu(false)
        AppLog.d("ReadingActivity", "onNewIntent:")
        this.sp = PreferenceManager.getDefaultSharedPreferences(readReference?.get()?.applicationContext)
        Constants.isFullWindowRead = sp?.getBoolean("read_fullwindow", true) ?: true
        Constants.PAGE_MODE = sp?.getInt("page_mode", 0) ?: 0
        Constants.isSlideUp = Constants.PAGE_MODE == 3
        versionCode = AppUtils.getVersionCode()
        AppLog.e(TAG, "versionCode: " + versionCode)
        if (readStatus != null) {
            readStatus?.recycleResource()
        }
        readStatus = ReadStatus(readReference?.get()?.getApplicationContext())
        BookApplication.getGlobalContext().readStatus = readStatus
        view?.setReadStatus(readStatus!!)
        autoSpeed = readStatus!!.autoReadSpeed()
        myNovelHelper = NovelHelper(readReference?.get(), readStatus)
        myNovelHelper?.setOnHelperCallBack(this)

        // 初始化窗口基本信息
        initWindow()
        setOrientation()
        getSavedState(intent.extras)
        if (isFromCover && Constants.IS_LANDSCAPE) {
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
        getBookContent()
        if (BaseBookApplication.getDownloadService() == null) {
            BookHelper.reStartDownloadService()
        }
        changeMode(Constants.MODE)
    }

    /**
     * 处理书籍状态
     */
    private fun initBookState() {
        // 判断是否订阅
        mBookDaoHelper = BookDaoHelper.getInstance()
        readStatus?.book_id = readStatus?.book?.book_id
        isSubed = mBookDaoHelper!!.isBookSubed(readStatus?.book_id)
        AppLog.e(TAG, "初始化书籍状态: " + readStatus?.book_id)
        bookChapterDao = BookChapterDao(readReference?.get()?.applicationContext, readStatus!!.book_id)
        if (isSubed) {
            readStatus!!.book = mBookDaoHelper!!.getBook(readStatus!!.book_id, 0)
        }
        if (readStatus?.sequence!! < -1) {
            readStatus?.sequence = -1
        } else if (isSubed && readStatus!!.sequence + 1 > readStatus!!.book.chapter_count) {
            readStatus?.sequence = readStatus?.book?.chapter_count!! - 1
        }
    }

    fun onConfigurationChanged(catalogMarkFragment: CatalogMarkFragment, optionHeader: ReadOptionHeader) {
        this.lastMode = -1
        // 初始化窗口基本信息
        pageView?.clear()
        initWindow()
        AppLog.e(TAG, "onConfigurationChanged")

        mCatalogMarkPresenter = CatalogMarkPresenter(readStatus!!, mReaderViewModel!!)
        mCatalogMarkPresenter?.view = catalogMarkFragment

        mReadOptionPresenter = ReadOptionPresenter(readReference?.get()!!, readStatus!!, mReaderViewModel!!)
        mReadOptionPresenter?.view = optionHeader

        view?.initPresenter(mReadOptionPresenter, mCatalogMarkPresenter)

        initBookState()
        // 初始化view
        view?.initView(mReaderViewModel!!)
        // 初始化监听器
        initListener()
        getBookContent()
        if (BaseBookApplication.getDownloadService() == null) {
            BookHelper.reStartDownloadService()
        }
        setMode()
        readStatus?.chapterCount = readStatus?.book?.chapter_count

        changeMode(Constants.MODE)
    }

    private fun getSavedState(savedInstanceState: Bundle?) {
        val bundle = readReference?.get()?.intent?.extras
        if (savedInstanceState != null) {
            // 从保存状态中获取
            // 章节序
            readStatus?.sequence = savedInstanceState.getInt("sequence", 0)
            // 请求类
            val requestItem = savedInstanceState.getSerializable(Constants.REQUEST_ITEM) as RequestItem?
            if (requestItem != null) {
                readStatus!!.setRequestItem(requestItem)
            }
            // 书签偏移量
            readStatus!!.offset = savedInstanceState.getInt("offset", 0)
            // 获取本书
            readStatus!!.book = savedInstanceState.getSerializable("book") as Book?
            // 获取当前章
            mReaderViewModel?.mReadDataListener = this
            mReaderViewModel?.readStatus = readStatus

            mReaderViewModel?.currentChapter = savedInstanceState.getSerializable("currentChapter") as Chapter?
            currentThemeMode = savedInstanceState.getString("thememode", readReference?.get()?.mThemeHelper?.getMode())
            AppLog.e(TAG, "getState1" + readStatus!!.sequence)
        } else {
            // 从bundle中获取
            readStatus?.sequence = bundle?.getInt("sequence", 0)
            ReadConfig.sequence = bundle?.getInt("sequence", 0)?:0
            val requestItem = bundle?.getSerializable(Constants.REQUEST_ITEM)
            if (requestItem != null) {
                readStatus!!.setRequestItem(requestItem as RequestItem)
            }
            readStatus?.offset = bundle?.getInt("offset", 0)
            readStatus?.book = bundle?.getSerializable("book") as Book?
            readStatus?.book_id = if (readStatus!!.book == null) "" else readStatus!!.book.book_id
            currentThemeMode = bundle?.getString("thememode", readReference?.get()?.mThemeHelper?.getMode())
            ReadConfig.bookName = readStatus?.book?.name
            AppLog.e(TAG, "getState2" + readStatus!!.sequence)
        }

        if (readStatus?.sequence == -2) {
            readStatus?.sequence = -1
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
                Constants.IS_LANDSCAPE = false
            } else if (sp?.getInt("screen_mode", 3) == Configuration.ORIENTATION_LANDSCAPE && readReference?.get()?.getResources()!!
                    .getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                if (!is_dot_orientation) {
                    is_dot_orientation = true
                }
                readReference?.get()?.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                Constants.IS_LANDSCAPE = true
            } else {
                if (!is_dot_orientation) {
                    is_dot_orientation = true
                }
            }
        }
    }

    /**
     * 获取书籍内容
     */
    private fun getBookContent() {

        NetWorkUtils.NATIVE_AD_TYPE = NetWorkUtils.NATIVE_AD_ERROR
//        dataFactory?.getChapterByLoading(ReadingActivity.MSG_LOAD_CUR_CHAPTER, readStatus!!.sequence)
//        getChapterByLoading(ReadingActivity.MSG_LOAD_CUR_CHAPTER, readStatus!!.sequence)

    }


    //ReadDataFactory
    //LoadingPage
    fun getCustomLoadingPage() {
        var curl = ""
        if (mReaderViewModel != null && mReaderViewModel!!.readStatus != null && mReaderViewModel!!.readStatus!!.sequence == -1) {
            curl = mReaderViewModel?.readStatus!!.firstChapterCurl
            //dataFactory
        } else if (mReaderViewModel?.currentChapter != null && !TextUtils.isEmpty(mReaderViewModel!!.currentChapter!!.curl)) {
            //if (readStatus.book.dex == 1 && !TextUtils.isEmpty(currentChapter.curl)) {
            curl = mReaderViewModel?.currentChapter!!.curl
            /*} else if (readStatus.book.dex == 0 && !TextUtils.isEmpty(currentChapter.curl1)) {
                curl = currentChapter.curl1;
            }*/
        }
        if (loadingPage == null) {
            loadingPage = LoadingPage(readReference?.get(), true, curl, LoadingPage.setting_result)
        }
        loadingPage?.setCustomBackgroud()
    }

    /**
     * 获取章节内容
     */
    fun getChapterByLoading(what: Int, sequence: Int) {
        var sequence = sequence

        if (sequence < -1) {
            sequence = -1
        } else if (mReaderViewModel!!.chapterList != null && mReaderViewModel!!.chapterList!!.size > 0 && sequence + 1 > mReaderViewModel!!.chapterList!!.size) {
            sequence = mReaderViewModel!!.chapterList!!.size - 1
        }
        val temp_sequence = sequence
        getCustomLoadingPage()
        loadingPage?.loading(Callable<Void> {
            val requestItem = mReaderViewModel!!.readStatus!!.getRequestItem()
            if (requestItem != null) {
                if (mReaderViewModel?.chapterList == null || mReaderViewModel?.chapterList?.isEmpty()!!) {
                    mReaderViewModel?.setBookChapterViewCallback(object : ReaderViewModel.BookChapterViewCallback {
                        override fun onChapterList(result: List<Chapter>) {
                            if (readReference?.get() != null && !readReference?.get()?.isFinishing!!) {
                                val chapterList = result as ArrayList<Chapter>
                                sendChapter(what, requestItem, temp_sequence, chapterList, ReadViewEnums.PageIndex.current)
                            }
                        }

                        override fun onFail(msg: String) {
                            if (loadingPage != null) {
                                loadingPage!!.onError()
                            }
                        }
                    })
                    mReaderViewModel?.getChapterList(requestItem)
                } else {
                    sendChapter(what, requestItem, temp_sequence, mReaderViewModel?.chapterList, ReadViewEnums.PageIndex.current)
                }
            }
            null
        })
    }


    open fun sendChapter(what: Int, requestItem: RequestItem, temp_sequence: Int, chapterList: ArrayList<Chapter>?, pageIndex: ReadViewEnums.PageIndex) {
        try {
            if (chapterList == null) {
                return
            } else {
                mReaderViewModel?.chapterList = chapterList
                mReaderViewModel?.readStatus?.book?.extra_parameter = requestItem.extra_parameter
            }
            mReaderViewModel?.readStatus?.chapterCount = chapterList.size

            if (temp_sequence == -1) {
                val result = Chapter()
                result.chapter_name = ""
                result.content = ""
                //                mHandler.obtainMessage(what, result).sendToTarget();
                obtainWhat(what, result, pageIndex)
                setLoadingCurl(500)
                return
            }

            AppLog.e("ReadDataFactory", "ReadDataFactory: " + temp_sequence + " : " + mReaderViewModel!!.readStatus!!.book_id)
            val result = chapterList[temp_sequence]
            synchronized(this) {
                mReaderViewModel?.requestSingleChapter(requestItem.host, result, object : ReaderViewModel.BookSingleChapterCallback {
                    override fun onPayChapter(chapter: Chapter) {
                        loadingPage?.onSuccess()
                        obtainWhat(what, chapter, pageIndex)
                    }

                    override fun onFail(msg: String) {}
                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

    /**
     * loading 页面显示原网页地址
     */
    @Throws(InterruptedException::class)
    private fun setLoadingCurl(time: Int) {
        if (mReaderViewModel!!.chapterList != null && !mReaderViewModel!!.chapterList!!.isEmpty()) {
            val firstChapter = mReaderViewModel?.chapterList?.get(0)
            if (firstChapter != null) {
                //if (readStatus.book.dex == 0) {
                mReaderViewModel!!.readStatus!!.firstChapterCurl = firstChapter.curl
                /*} else if (readStatus.book.dex == 1) {
                    readStatus.firstChapterCurl = firstChapter.curl1;
                }*/

                loadingPage!!.setNovelSource(mReaderViewModel!!.readStatus!!.firstChapterCurl)
                Thread.sleep(time.toLong())
            }
        }
    }

    open fun obtainWhat(what: Int, chapter: Chapter, pageIndex: ReadViewEnums.PageIndex) {
        if (mReaderViewModel == null) {
            return
        }
        if (loadingPage != null) {
            loadingPage?.onSuccess()
        }
        when (what) {
            ReadingActivity.MSG_LOAD_CUR_CHAPTER -> {
                //                            loadCurrentChapter(m);
                mReaderViewModel?.currentChapter = chapter
                if (mReaderViewModel?.readStatus != null && mReaderViewModel?.currentChapter != null && mReaderViewModel?.currentChapter?.sequence != -1) {
                    mReaderViewModel?.readStatus?.sequence = mReaderViewModel?.currentChapter?.sequence
                }
                initBookCallBack()
            }
            ReadingActivity.MSG_LOAD_NEXT_CHAPTER -> {
                //                            loadNextChapter(m);
                mReaderViewModel?.nextChapter = chapter
                nextChapterCallBack(true)
            }
            ReadingActivity.MSG_LOAD_PRE_CHAPTER -> {
                //                            loadPreChapter(m);
                mReaderViewModel?.preChapter = chapter
                preChapterCallBack(true)
            }
            ReadingActivity.MSG_JUMP_CHAPTER -> {
                //                            loadJumpChapter(m);
                mReaderViewModel?.currentChapter = chapter
                jumpChapterCallBack()
            }
        }
        Constants.startReadTime = System.currentTimeMillis() / 1000L
    }

    /**
     * 打开书籍取得书签章节内容后的处理
     */
    private fun initBookCallBack() {
        Constants.readedCount++
        if (mReaderViewModel?.chapterList == null) {
            return
        }
        // 章节数
        if (mReaderViewModel?.readStatus != null) {
            mReaderViewModel?.readStatus?.chapterCount = mReaderViewModel?.chapterList?.size
            if (mReaderViewModel?.chapterList != null && !mReaderViewModel?.chapterList?.isEmpty()!!) {
                val firstChapter = mReaderViewModel?.chapterList?.get(0)
                if (firstChapter != null) {
                    mReaderViewModel?.readStatus?.firstChapterCurl = firstChapter.curl
                }
            }
        }

        // 初始化章节内容
        if (myNovelHelper != null) {
            myNovelHelper!!.getChapterContent(readReference?.get(), mReaderViewModel?.currentChapter, mReaderViewModel?.readStatus?.book)
        }
        // 刷新页面
        if (mReaderViewModel?.mReadDataListener != null) {
            mReaderViewModel?.mReadDataListener!!.freshPage()
        }

        if (pageView != null) {
            pageView?.drawCurrentPage()
            pageView?.drawNextPage()
            pageView?.getChapter(true)
        }
        mReaderViewModel?.mReadDataListener?.initBookStateDeal()
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
            readStatus?.screenWidth = realSize.x
            readStatus?.screenHeight = realSize.y
            ReadConfig.screenWidth = realSize.x
            ReadConfig.screenHeight = realSize.y
        } else {
            readStatus?.screenWidth = dm.widthPixels
            readStatus?.screenHeight = dm.heightPixels
            ReadConfig.screenWidth = dm.widthPixels
            ReadConfig.screenHeight = dm.heightPixels
        }

        readStatus?.screenDensity = dm.density
        readStatus?.screenScaledDensity = dm.scaledDensity
        // 保存字体、亮度、阅读模式
        modeSp = readReference?.get()?.getSharedPreferences("config", Context.MODE_PRIVATE)
        // 设置字体
        if (sp?.contains("novel_font_size")!!) {
            Constants.FONT_SIZE = sp?.getInt("novel_font_size", 18) ?: 18
        } else {
            Constants.FONT_SIZE = 18
        }

        //ViewModel
        mReaderViewModel = ReaderViewModel(ReaderRepositoryFactory.getInstance(ReaderOwnRepository.getInstance()), BookCoverRepositoryFactory.getInstance(BookCoverOtherRepository.getInstance(NetService.userService),
                BookCoverQGRepository.getInstance(OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext())), BookCoverLocalRepository.getInstance(BaseBookApplication.getGlobalContext())))

        //换源监听
        mReaderViewModel?.setReaderBookSourceViewCallback(object : ReaderViewModel.ReaderBookSourceViewCallback {
            override fun onBookSource(sourceItem: SourceItem) {
                searchChapterCallBack(sourceItem.sourceList)
                loadingPage?.onSuccess()
            }

            override fun onBookSourceFail(msg: String?) {
                loadingPage?.onSuccess()
            }
        })
        mReaderViewModel?.mReadDataListener = this
        mReaderViewModel?.readStatus = readStatus
    }

    /**
     * 初始化view
     */
    fun initData(v: PageInterface) {
//        pageView = v
//        resources = readReference?.get()?.resources
//        pageView?.setViewModel(mReaderViewModel)
//        pageView?.init(readReference?.get(), readStatus, myNovelHelper)
//        pageView?.setCallBack(this)
//        pageView?.setOnOperationClickListener(this)
//        myNovelHelper?.setPageView(pageView)
    }

    /**
     * 初始化监听器
     */
    private fun initListener() {
        if (downloadService == null) {
            reStartDownloadService(readReference?.get()!!)
            downloadService = BaseBookApplication.getDownloadService()
        } else {
            downloadService!!.setOnDownloadListener(this)
        }
    }

    private fun reStartDownloadService(context: Activity) {
        val intent = Intent()
        intent.setClass(context, DownloadService::class.java)
        context.startService(intent)
        context.bindService(intent, sc, Context.BIND_AUTO_CREATE)
    }

    /**
     * 初始化时间显示
     */
    public fun initTime() {
        mTimerStopped = false
        if (mCalendar == null) {
            mCalendar = Calendar.getInstance()
        }

//周期发送
        time_text = DateFormat.format("k:mm", mCalendar)
        view?.freshTime(time_text)
        val now = SystemClock.uptimeMillis()
        val time = Observable.interval(1, TimeUnit.MINUTES)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                        mCalendar?.timeInMillis = System.currentTimeMillis()
                        time_text = DateFormat.format("k:mm", mCalendar)
                        view?.freshTime(time_text)
                })
        disposable.add(time)
    }

    /**
     * 刷新页面
     */
    private fun refreshPage() {
        readStatus?.isCanDrawFootView = readStatus?.sequence != -1
    }

    /**
     * 预加载
     */
    private fun downloadNovel() {

        if (mBookDaoHelper!!.isBookSubed(readStatus!!.book_id)) {
            var num = BookHelper.CHAPTER_CACHE_COUNT
            val max = readStatus!!.chapterCount - 1 - readStatus!!.sequence
            if (max > 0) {
                if (max < num) {
                    num = max
                }

                //预加载
                val size = mReaderViewModel!!.chapterList!!.size
                var i = readStatus!!.sequence + 1
                while (i < readStatus!!.sequence + num + 1 && i < size) {
                    val c = mReaderViewModel?.chapterList!![i]
                    if (c != null) {
                        AppLog.e(TAG, "预加载： " + c.toString())
                        val finalI = i
                        mReaderViewModel!!.requestSingleChapter(readStatus!!.getRequestItem().host, c, object : ReaderViewModel.BookSingleChapterCallback {
                            override fun onPayChapter(chapter: Chapter) {
                                if (finalI == readStatus!!.sequence + 1) {
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

    fun searchChapterCallBack(sourcesList: ArrayList<Source>?) {
        if (myNovelHelper != null && mReaderViewModel != null && mReaderViewModel!!.currentChapter != null && !TextUtils.isEmpty(mReaderViewModel!!.currentChapter!!.curl) && sourcesList != null) {
            //if (readStatus.book.dex == 1 && !TextUtils.isEmpty(dataFactory.currentChapter.curl) && sourcesList != null) {
            myNovelHelper?.showSourceDialog(mReaderViewModel, mReaderViewModel?.currentChapter?.curl, sourcesList)
            /*} else if (readStatus.book.dex == 0 && !TextUtils.isEmpty(dataFactory.currentChapter.curl1) && sourcesList != null) {
                myNovelHelper.showSourceDialog(dataFactory, dataFactory.currentChapter.curl1, sourcesList);*/
            //}
        } else {
            readReference?.get()?.showToastShort("暂无其它来源")
        }
    }

    /**
     * 打开目录页面
     */
    private fun openCategoryPage() {
        if (readStatus!!.isMenuShow) {
            showMenu(false)
        }
        if (readStatus!!.book.book_type == 0) {
            val intent = Intent(readReference?.get(), CataloguesActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable("cover", readStatus!!.book)
            bundle.putString("book_id", readStatus!!.book_id)
            AppLog.e(TAG, "OpenCategoryPage: " + readStatus!!.sequence)
            bundle.putInt("sequence", readStatus!!.sequence)
            bundle.putBoolean("fromCover", false)
            AppLog.e(TAG, "ReadingActivity: " + readStatus!!.getRequestItem().toString())
            bundle.putSerializable(Constants.REQUEST_ITEM, readStatus!!.getRequestItem())
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
        if (readStatus != null && readStatus!!.getRequestItem() != null) {
            if (source != null && !TextUtils.isEmpty(source.book_source_id) && readStatus != null && readStatus!!.book != null) {
                if (mBookDaoHelper != null && mBookDaoHelper!!.isBookSubed(readStatus!!.book.book_id)) {
                    val iBook = mBookDaoHelper!!.getBook(readStatus!!.book.book_id, 0)
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
        if (readStatus != null && readStatus!!.getRequestItem() != null) {
            readStatus?.firstChapterCurl = ""
            mReaderViewModel?.currentChapter = null

            val requestItem = RequestItem()
            requestItem.book_id = source.book_id
            requestItem.book_source_id = source.book_source_id
            requestItem.host = source.host
            requestItem.name = readStatus!!.bookName
            requestItem.author = readStatus!!.bookAuthor
            requestItem.dex = source.dex

            val iterator = source.source.entries.iterator()
            val list = ArrayList<String>()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val value = entry.value
                list.add(value)
            }
            if (list.size > 0) {
                requestItem.parameter = list[0]
            }
            if (list.size > 1) {
                requestItem.extra_parameter = list[1]
            }


            readStatus!!.setRequestItem(requestItem)
            //readStatus.requestConfig = BookApplication.getGlobalContext().getSourceConfig(requestItem.host);

            val bookDaoHelper = BookDaoHelper.getInstance()
            if (bookDaoHelper.isBookSubed(source.book_id)) {
                val iBook = bookDaoHelper.getBook(source.book_id, 0)
                iBook.book_source_id = requestItem.book_source_id
                iBook.site = requestItem.host
                iBook.parameter = requestItem.parameter
                iBook.extra_parameter = requestItem.extra_parameter
                iBook.last_updatetime_native = source.update_time
                iBook.dex = source.dex
                bookDaoHelper.updateBook(iBook)
                readStatus!!.book = iBook
                if (b) {
                    val bookChapterDao = BookChapterDao(readReference?.get(), source.book_id)
                    BookHelper.deleteAllChapterCache(source.book_id, 0, bookChapterDao.count)
                    DownloadService.clearTask(source.book_id)
                    BaseBookHelper.delDownIndex(readReference?.get(), source.book_id)
                    bookChapterDao.deleteBookChapters(0)

                }
            } else {
                val iBook = readStatus!!.book
                iBook.book_source_id = source.book_source_id
                iBook.site = source.host
                iBook.dex = source.dex
                iBook.parameter = requestItem.parameter
                iBook.extra_parameter = requestItem.extra_parameter
                readStatus!!.book = iBook
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
        if (mBookDaoHelper!!.isBookSubed(readStatus!!.book_id)) {
            mBookDaoHelper!!.deleteBook(readStatus!!.book_id)
        }
        readReference?.get()?.finish()
    }

    override fun openAutoReading(open: Boolean) = onReadAuto()
    override fun changSource() {
        openSourcePage()
    }
    override fun addBookShelf(isAddShelf: Boolean) {
        if (isAddShelf && mBookDaoHelper != null && readStatus!!.book != null) {
            readStatus!!.book.sequence = readStatus!!.sequence
            readStatus!!.book.offset = readStatus!!.offset
            readStatus!!.book.sequence_time = System.currentTimeMillis()
            readStatus!!.book.last_updateSucessTime = System.currentTimeMillis()
            readStatus!!.book.readed = 1
            if (mReaderViewModel != null) {
                if (mReaderViewModel?.chapterList != null && mReaderViewModel!!.chapterList!!.size > 0) {
                    val chapter = mReaderViewModel!!.chapterList!![mReaderViewModel!!.chapterList!!.size - 1]
                    readStatus!!.book.extra_parameter = chapter.extra_parameter
                }
                bookChapterDao?.insertBookChapter(mReaderViewModel?.chapterList)
            }
            val succeed = mBookDaoHelper?.insertBook(readStatus!!.book)
            Toast.makeText(readReference?.get(), if (succeed!!) R.string.reading_add_succeed else R.string.reading_add_fail,
                    Toast.LENGTH_SHORT).show()
        }
        val map1 = HashMap<String, String>()
        if (readStatus!!.book != null) {
            map1.put("bookid", readStatus!!.book.book_id)
        }
        if (mReaderViewModel != null && mReaderViewModel?.currentChapter != null) {
            map1.put("chapterid", mReaderViewModel!!.currentChapter!!.chapter_id)
        }
        if (isAddShelf) {
            StartLogClickUtil.upLoadEventLog(readReference?.get(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.POPUPSHELFADD, map1)
        } else {
            StartLogClickUtil.upLoadEventLog(readReference?.get(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.POPUPSHELFADDCANCLE, map1)
        }

        goBackToHome()
    }

    /**
     * 打开切源页面
     */
    private fun openSourcePage() {
        if (readStatus!!.sequence == -1) {
            Toast.makeText(readReference?.get(), R.string.read_changesource_tip, Toast.LENGTH_SHORT).show()
            return
        }
        if (Constants.QG_SOURCE == readStatus!!.requestItem.host) {
            Toast.makeText(readReference?.get(), "该小说暂无其他来源！", Toast.LENGTH_SHORT).show()
            return
        }
        if (isSourceListShow) {
            isSourceListShow = false
        } else {
            if (Constants.QG_SOURCE == readStatus!!.getRequestItem().host || Constants.QG_SOURCE == readStatus!!.getRequestItem().host) {
                return
            }
            showMenu(false)
            getCustomLoadingPage()
            loadingPage?.loading {
                mReaderViewModel!!.getBookSource(readStatus!!.book_id)
//                OtherRequestService.requestBookSourceChange(dataFactory?.mHandler, ReadingActivity.MSG_SEARCH_CHAPTER, -144, readStatus!!.book_id)
                null
            }
//            dataFactory?.loadingError(loadingPage)
        }
    }

    /**
     * 开始下载
     */
    private fun startDownLoad() {
        if (!isSubed) {
            val succeed = mBookDaoHelper?.insertBook(readStatus!!.book)!!
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
        myNovelHelper?.clickDownload(readReference?.get(), readStatus!!.book as Book, Math.max(readStatus!!.sequence, 0))
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
            Constants.IS_LANDSCAPE = false
        } else if (act.resources!!.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.rb_click_landscape_btn)
            val data = HashMap<String, String>()
            data.put("type", "1")
            StartLogClickUtil.upLoadEventLog(readReference?.get(), StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.HPMODEL, data)
            act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            isFromCover = false
            screen_mode.putInt("screen_mode", Configuration.ORIENTATION_LANDSCAPE)
            Constants.IS_LANDSCAPE = true
        }
        screen_mode.apply()
    }

    fun changeSourceCallBack() {
        if (pageView == null) {
            return
        }
        readStatus!!.currentPage = 1
        readStatus!!.offset = 0
        myNovelHelper?.isShown = false
        myNovelHelper?.getChapterContent(readReference?.get(), mReaderViewModel?.currentChapter, readStatus!!.book)
        refreshPage()
        isSourceListShow = false
        if (Constants.isSlideUp) {
            pageView?.getChapter(false)
        } else {
            pageView?.drawCurrentPage()
            pageView?.drawNextPage()
        }
        downloadNovel()
    }

    /**
     * 跳章
     */
    fun jumpChapterCallBack() {
        Constants.readedCount++
        if (mReaderViewModel == null || readStatus == null || myNovelHelper == null) {
            return
        }
        mReaderViewModel?.nextChapter = null
        readStatus?.sequence = readStatus!!.novel_progress
        readStatus?.offset = 0
        myNovelHelper?.isShown = false
        myNovelHelper?.getChapterContent(readReference?.get(), mReaderViewModel!!.currentChapter, readStatus!!.book)
        readStatus?.currentPage = 1
        refreshPage()
        if (pageView == null) {
            return
        }
        if (Constants.isSlideUp) {
            pageView?.getChapter(false)
        } else {
            pageView?.drawCurrentPage()
            pageView?.drawNextPage()
        }
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

    /*
     * 显示隐藏菜单
     */
    fun showMenu(isShow: Boolean) {
//        if (pageView == null) {
//            return
//        }
//        if (pageView!!.isAutoReadMode && isShow) {
//            return
//        }
        clearOtherPanel()
        if (isShow) {
            full(false)
            changeMarkState()
            mReadOptionPresenter?.view?.show(true)
            view?.showSetMenu(isShow)
            readStatus!!.isMenuShow = true
            view?.initSettingGuide()
        } else {
            full(true)
            readStatus!!.isMenuShow = false
            mReadOptionPresenter!!.view!!.show(false)
            view?.showSetMenu(isShow)
            readStatus!!.isMenuShow = false
        }
    }

    // 全屏切换
    private fun full(enable: Boolean) {
        if (!Constants.isFullWindowRead) {
            return
        }
        view?.full(enable)
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
        AppLog.e(TAG, "mode : " + mode)
        when (mode) {
            51 -> {
                setTextColor(readReference?.get()?.getResources()!!.getColor(R.color.reading_text_color_first))
                setPageBackColor(readReference?.get()?.getResources()!!.getColor(R.color.reading_backdrop_first))
                setBackground()
                setBatteryBackground(R.drawable.reading_batty_day)
            }
            52 -> {
                setTextColor(readReference?.get()?.getResources()!!.getColor(R.color.reading_text_color_second))
                setPageBackColor(readReference?.get()?.getResources()!!.getColor(R.color.reading_backdrop_second))

                setBackground()
                setBatteryBackground(R.drawable.reading_batty_eye)
            }
            53 -> {
                setTextColor(readReference?.get()?.getResources()!!.getColor(R.color.reading_text_color_third))
                setPageBackColor(readReference?.get()?.getResources()!!.getColor(R.color.reading_backdrop_third))

                setBackground()
                setBatteryBackground(R.drawable.reading_batty_4)
            }
            54 -> {
                setTextColor(readReference?.get()?.getResources()!!.getColor(R.color.reading_text_color_fourth))
                setPageBackColor(readReference?.get()?.getResources()!!.getColor(R.color.reading_backdrop_fourth))

                setBackground()
                setBatteryBackground(R.drawable.reading_batty_5)
            }
            55 -> {
                setTextColor(readReference?.get()?.getResources()!!.getColor(R.color.reading_text_color_fifth))
                setPageBackColor(readReference?.get()?.getResources()!!.getColor(R.color.reading_backdrop_fifth))

                setBatteryBackground(R.drawable.reading_batty_night)

                setBackground()
            }
            56 -> {
                setTextColor(readReference?.get()?.getResources()!!.getColor(R.color.reading_text_color_sixth))
                setPageBackColor(readReference?.get()?.getResources()!!.getColor(R.color.reading_backdrop_sixth))

                setBackground()
                setBatteryBackground(R.drawable.reading_batty_night2)
            }
            61 -> {
                setTextColor(readReference?.get()?.resources!!.getColor(R.color.reading_text_color_night))
                setPageBackColor(readReference?.get()?.resources!!.getColor(R.color.reading_backdrop_night))

                setBackground()
                setBatteryBackground(R.drawable.reading_batty_night2)
            }
            else -> {
                setTextColor(readReference?.get()?.resources!!.getColor(R.color.reading_text_color_first))
                setPageBackColor(Color.parseColor("#C2B282"))

                setBackground()
                setBatteryBackground(R.drawable.reading_batty_day)
            }
        }
        view?.setBackground()
    }

    private fun setTextColor(color: Int) {
        pageView?.setTextColor(color)
    }

    private fun setBackground() = pageView?.setBackground()

    private fun setBatteryBackground(resourceId: Int) = pageView?.changeBatteryBg(resourceId)

    private fun setPageBackColor(color: Int) = pageView?.setPageBackColor(color)

    fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // 小说音量键翻页
        if (Constants.isVolumeTurnover) {
            if (pageView != null && pageView!!.setKeyEvent(event)) {
                return true
            }
        }
        return false
    }

    fun onKeyDown() {
        if (pageView != null && view != null && pageView!!.isAutoReadMode) {
            if (view!!.getAutoMenuShowState()) {
                view!!.showAutoMenu(false)
                pageView!!.setisAutoMenuShowing(false)
                pageView!!.resumeAutoRead()
            } else {
                pageView!!.pauseAutoRead()
                view!!.showAutoMenu(true)
            }
        } else {
            if (readStatus!!.isMenuShow) {
                showMenu(false)
            } else {
                showMenu(true)
            }
        }
    }

    fun onBackPressed(): Boolean {

        if (isSourceListShow) {
            isSourceListShow = false
            return false
        }

        if (pageView != null && auto_menu != null && view != null && view!!.getAutoMenuShowState()) {
            view?.showAutoMenu(false)
            pageView?.setisAutoMenuShowing(false)
            pageView?.resumeAutoRead()
            return false
        }
        if (pageView != null && pageView!!.isAutoReadMode) {
            autoStop()
            return false
        }
        // 显示菜单
        if (readStatus != null && readStatus!!.isMenuShow) {
            showMenu(false)
            return false
        }

        if (mBookDaoHelper != null && readStatus != null) {
            isSubed = mBookDaoHelper!!.isBookSubed(readStatus!!.book_id)
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
        if (isFromCover && Constants.IS_LANDSCAPE) {
            return
        }
        view?.checkModeChange()

        val content_mode = sp!!.getInt("content_mode", 51)
        if (isSubed) {
            readStatus!!.book = mBookDaoHelper!!.getBook(readStatus!!.book_id, 0)
        }
        pageView?.resumeAutoRead()

        readStatus!!.chapterCount = readStatus!!.book.chapter_count


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

        if (readStatus != null && Constants.isNetWorkError) {
            Constants.isNetWorkError = false
            getChapterByLoading(ReadingActivity.MSG_LOAD_CUR_CHAPTER, readStatus!!.sequence)
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
            if (readStatus!!.book.book_type == 0) {
                myNovelHelper?.saveBookmark( readStatus?.book_id, mCurPageSequence,
                        mCurPageOffset, mBookDaoHelper)
                // 统计阅读章节数
                val spUtils = SharedPreferencesUtils(PreferenceManager
                        .getDefaultSharedPreferences(readReference?.get()))
                spUtils.putInt("readed_count", Constants.readedCount)
            }
        }
        pageView?.pauseAutoRead()

    }

    fun onWindowFocusChanged(hasFocus: Boolean) {
    }

    fun onStop() {
        LocalBroadcastManager.getInstance(readReference?.get()).unregisterReceiver(mCacheUpdateReceiver)
        actNovelRunForeground = false
    }

    fun onDestroy() {
//        if (mNovelLoader != null && mNovelLoader!!.status == AsyncTask.Status.RUNNING) {
//            mNovelLoader!!.cancel(true)
//        }

        if (readStatus != null && mReaderViewModel != null && mReaderViewModel!!.currentChapter != null && readStatus!!.requestItem != null) {
            //按照此顺序传值 当前的book_id，阅读章节，书籍源，章节总页数，当前阅读页，当前页总字数，当前页面来自，开始阅读时间,结束时间,阅读时间,是否有阅读中间退出行为,书籍来源1为青果，2为智能
            StartLogClickUtil.upLoadReadContent(readStatus!!.book_id, mReaderViewModel!!.currentChapter!!.chapter_id + "", readStatus!!.source_ids, readStatus!!.pageCount.toString() + "",
                    readStatus!!.currentPage.toString() + "", readStatus!!.currentPageConentLength.toString() + "", readStatus!!.requestItem.fromType.toString() + "",
                    readStatus!!.startReadTime.toString() + "", System.currentTimeMillis().toString() + "", (System.currentTimeMillis() - readStatus!!.startReadTime).toString() + "", "false", readStatus!!.requestItem.channel_code.toString() + "")

        }
        AppLog.e(TAG, "onDestroy")
        readStatus!!.isMenuShow = false
//        if (mNovelLoader != null && mNovelLoader!!.status == AsyncTask.Status.RUNNING) {
//            mNovelLoader?.cancel(true)
//        }

        mTimerStopped = true

        if (this.sp != null) {
            this.sp = null
        }

        if (this.modeSp != null) {
            this.modeSp = null
        }

        if (pageView != null) {
            pageView!!.setCallBack(null)
            pageView!!.clear()
            pageView = null
        }

        if (myNovelHelper != null) {
            myNovelHelper!!.setOnHelperCallBack(null)
            myNovelHelper!!.clear()
        }

        Glide.get(readReference?.get()).clearMemory()

        if (readStatus != null) {
            readStatus!!.recycleResource()
        }

        if (mReaderViewModel != null) {
            mReaderViewModel!!.mReadDataListener = null


            mReaderViewModel!!.clean()
        }

        BitmapManager.getInstance().clearBitmap()

        //
        for (d in disposable) {
            d.dispose()
        }
        disposable.clear()
    }

    fun onSaveInstanceState(outState: Bundle): Bundle? {
        // 保存书签状态
        try {
            outState.putInt("sequence", readStatus!!.sequence)
            outState.putInt("nid", readStatus!!.nid)
            outState.putInt("offset", readStatus!!.offset)
            outState.putSerializable("book", readStatus!!.book)
            if (mReaderViewModel != null && mReaderViewModel?.currentChapter != null) {
                outState.putSerializable("currentChapter", mReaderViewModel?.currentChapter)
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

        val intent = Intent(readReference?.get(), BookEndActivity::class.java)
        val bundle = Bundle()
        bundle.putSerializable(Constants.REQUEST_ITEM, readStatus!!.getRequestItem())
        bundle.putString("bookName", readStatus!!.book.name)
        bundle.putString("book_id", readStatus!!.book_id)
        bundle.putString("book_category", readStatus!!.book.category)
        bundle.putSerializable("book", readStatus!!.book)
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
                readStatus!!.sequence = bundle.getInt("sequence")
                readStatus!!.offset = bundle.getInt("offset", 0)
                readStatus!!.book = bundle.getSerializable("book") as Book?
                val requestItem = bundle.getSerializable(Constants.REQUEST_ITEM) as RequestItem?
                AppLog.e(TAG, "onActivityResult: " + requestItem.toString())
                if (!readStatus!!.source_ids.contains(readStatus!!.book.site)) {
                    readStatus!!.source_ids += "`" + readStatus!!.book.site
                }

                AppLog.e(TAG, "from" + readStatus!!.requestItem.fromType + "===")
                if (requestItem != null) {
                    readStatus!!.setRequestItem(requestItem)
                    //readStatus.requestConfig = BookApplication.getGlobalContext().getSourceConfig(requestItem.host);
                }
                if (mReaderViewModel!!.chapterList != null) {
                    mReaderViewModel!!.chapterList!!.clear()
                }
                myNovelHelper?.isShown = false
                readStatus!!.currentPage = 1
                mReaderViewModel?.nextChapter = null
                mReaderViewModel?.preChapter = null
                readStatus!!.requestItem.fromType = 1//打点 书籍封面（0）/书架（1）/上一页翻页（2）
                if (Constants.QG_SOURCE == readStatus!!.book.site) {
                    requestItem?.channel_code = 1
                } else {
                    requestItem?.channel_code = 2
                }
                getBookContent()
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
        if (readStatus!!.isMenuShow) {
            showMenu(false)
            return
        }
        mReaderViewModel?.next()
        pageView?.drawCurrentPage()
    }

    override fun onShowMenu(isShow: Boolean) {
        showMenu(isShow)
    }

    override fun onCancelPage() {
        mReaderViewModel?.restore()
        refreshPage()
    }

    override fun onResize() {
        AppLog.e("ReadingActivity", "onResize")
        if (mReaderViewModel?.currentChapter != null && readStatus!!.book != null) {
            myNovelHelper?.getChapterContent(readReference?.get(), mReaderViewModel?.currentChapter, readStatus!!.book)
            refreshPage()
        }
    }

    override fun freshPage() {
        refreshPage()
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
        refreshPage()
        initTime()

        // 刷新页面
        // 刷新内容显示
        // 启动预加载
        downloadNovel()
    }

    override fun onShowAutoMenu(show: Boolean) {
        view?.showAutoMenu(show)
        if (show) {
            pageView?.pauseAutoRead()
        } else {
            pageView?.resumeAutoRead()
        }
    }

    fun speedUp() {
        readStatus!!.setAutoReadSpeed(++autoSpeed)
        autoSpeed = readStatus!!.autoReadSpeed()
    }

    fun speedDown() {
        readStatus!!.setAutoReadSpeed(--autoSpeed)
        autoSpeed = readStatus!!.autoReadSpeed()
    }

    fun autoStop() {
        pageView?.exitAutoRead()

        if (isSlideToAuto) {
            val temp: PageInterface?
            Constants.isSlideUp = true
            temp = pageView
            pageView = ScrollPageView(readReference?.get())
            view?.resetPageView(pageView!!)
            pageView?.init(readReference?.get(), readStatus, myNovelHelper)
            pageView?.setCallBack(this)
            pageView?.setViewModel(mReaderViewModel)
            myNovelHelper?.setPageView(pageView)
            pageView?.freshTime(time_text)
//            pageView?.freshBattery(batteryPercent)
            changeMode(current_mode)

            temp?.clear()
        }
        view?.showStopAutoHint()
    }

    private fun pauseAutoReadHandler() {
        pageView?.pauseAutoRead()
    }

    private fun resumeAutoReadHandler() {
        pageView?.resumeAutoRead()
    }

    override fun changeChapter() {
        view?.changeChapter()
        changeMarkState()
    }

    fun onReadCatalog() {
        //        openCategoryPage();
        if (readStatus!!.isMenuShow) {
            showMenu(false)
        }
//        if (mNovelLoader != null && mNovelLoader!!.status == AsyncTask.Status.RUNNING) {
//            mNovelLoader!!.cancel(true)
//        }
        val data = HashMap<String, String>()
        if (readStatus != null) {
            data.put("bookid", readStatus!!.book_id)
        }
        if (mReaderViewModel != null && mReaderViewModel?.currentChapter != null) {
            data.put("chapterid", mReaderViewModel!!.currentChapter!!.book_id)
        }
        StartLogClickUtil.upLoadEventLog(readReference?.get(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.CATALOG, data)
    }

    fun onReadChangeSource() {
        if (Book.isOnlineType(readStatus!!.book.book_type)) {
            openSourcePage()
        }
    }

    fun onReadCache() {
        if (Book.isOnlineType(readStatus!!.book.book_type)) {
            startDownLoad()
        }
    }

    fun onReadAuto() {
        if (System.currentTimeMillis() - stampTime < 1000) {
            return
        }
        stampTime = System.currentTimeMillis()
        isSlideToAuto = Constants.isSlideUp
        if (Constants.isSlideUp) {
            val temp: PageInterface?
            Constants.isSlideUp = false
            temp = pageView
            pageView = PageView(readReference?.get())
            view?.resetPageView(pageView!!)
            pageView!!.init(readReference?.get(), readStatus, myNovelHelper)
            pageView!!.setCallBack(this)
            pageView!!.setViewModel(mReaderViewModel)
            myNovelHelper?.setPageView(pageView)
            pageView!!.freshTime(time_text)
//            pageView!!.freshBattery(batteryPercent)
            pageView!!.drawCurrentPage()
            changeMode(current_mode)
            temp?.clear()
        }
        pageView?.startAutoRead()
        showMenu(false)
    }

    fun onChangeMode(mode: Int) {
        changeMode(mode)
    }

    fun onRedrawPage() {
        if (pageView is ScrollPageView && (pageView as ScrollPageView).tempChapter != null) {
            myNovelHelper?.getChapterContent(readReference?.get(), (pageView as ScrollPageView).tempChapter, readStatus!!.book)
        } else {
            myNovelHelper?.getChapterContent(readReference?.get(), mReaderViewModel?.currentChapter, readStatus!!.book)
        }
        refreshPage()
        pageView?.drawCurrentPage()
        pageView?.drawNextPage()
        pageView?.getChapter(true)
    }

    fun onJumpChapter() {
        getChapterByLoading(ReadingActivity.MSG_JUMP_CHAPTER, readStatus!!.novel_progress)
    }

    fun onJumpPreChapter() {
        readStatus!!.currentPage = 1
        mReaderViewModel!!.toChapterStart = true
//        mReaderViewModel!!.previous()
//        if (Constants.isSlideUp) {
//            pageView!!.getChapter(false)
//        } else {
//            pageView!!.drawCurrentPage()
//            pageView!!.drawNextPage()
//        }
        changeMarkState()

        if (!pageView!!.isAutoReadMode) {
            Constants.manualReadedCount++
            dealManualDialogShow()
        }

        val data = HashMap<String, String>()
        if (readStatus != null) {
            data.put("bookid", readStatus!!.book_id)
        }
        if (mReaderViewModel != null && mReaderViewModel!!.currentChapter != null) {
            data.put("chapterid", mReaderViewModel!!.currentChapter!!.chapter_id)
        }
        data.put("type", "1")
        StartLogClickUtil.upLoadEventLog(readReference?.get(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.CHAPTERTURN, data)

    }

    fun onJumpNextChapter() {
        readStatus!!.currentPage = readStatus!!.pageCount
//        mReaderViewModel!!.next()
//        if (Constants.isSlideUp) {
//            pageView?.getChapter(false)
//        } else {
//            pageView!!.drawCurrentPage()
//            pageView!!.drawNextPage()
//        }
        changeMarkState()

//        if (!pageView!!.isAutoReadMode()) {
            Constants.manualReadedCount++
            dealManualDialogShow()
//        }

        val data = HashMap<String, String>()
        if (readStatus != null) {
            data.put("bookid", readStatus!!.book_id)
        }
        if (mReaderViewModel != null && mReaderViewModel!!.currentChapter != null) {
            data.put("chapterid", mReaderViewModel!!.currentChapter!!.chapter_id)
        }
        data.put("type", "2")
        StartLogClickUtil.upLoadEventLog(readReference?.get(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.CHAPTERTURN, data)

    }

    fun onReadFeedBack() {
        if (readReference != null && readReference!!.get() != null && !readReference!!.get()!!.isFinishing()) {
            if (readStatus!!.sequence == -1) {
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

            if (Constants.IS_LANDSCAPE) {
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
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(readReference?.get()?.getApplicationContext()?.getApplicationContext())
        val edit = sharedPreferences.edit()
        val data = HashMap<String, String>()

        if (readReference?.get()?.mThemeHelper!!.isNight()) {
            //夜间模式只有一种背景， 不能存储
            //            edit.putInt("current_night_mode", Constants.MODE);
            Constants.MODE = sharedPreferences.getInt("current_light_mode", 51)
            readReference?.get()?.mThemeHelper?.setMode(ThemeMode.THEME1)
            data.put("type", "2")
            StartLogClickUtil.upLoadEventLog(readReference?.get()?.getApplicationContext(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.NIGHTMODE1, data)
        } else {
            edit.putInt("current_light_mode", Constants.MODE)
            //            Constants.MODE = sharedPreferences.getInt("current_night_mode", 61);
            //夜间模式只有一种背景
            Constants.MODE = 61
            readReference?.get()?.mThemeHelper?.setMode(ThemeMode.NIGHT)
            data.put("type", "1")
            StartLogClickUtil.upLoadEventLog(readReference?.get()?.getApplicationContext(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.NIGHTMODE1, data)
        }
        edit.putInt("content_mode", Constants.MODE)
        edit.apply()
        changeMode(Constants.MODE)
        //        Intent intent = new Intent(this, ReadingActivity.class);
        //        Bundle bundle = new Bundle();
        //        bundle.putInt("sequence", readStatus.sequence);
        //        bundle.putInt("offset", readStatus.offset);
        //        bundle.putSerializable("book", readStatus.book);
        //        bundle.putSerializable(Constants.REQUEST_ITEM, readStatus.requestItem);
        //        bundle.putString("thememode", currentThemeMode);
        //        intent.putExtras(bundle);
        //        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //        startActivity(intent);
        //        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
        //        finish();
    }

    private fun submitFeedback(type: Int) {
        if (NetWorkUtils.getNetWorkType(readReference?.get()) == NetWorkUtils.NETWORK_NONE) {
            readReference?.get()?.showToastShort("网络异常")
            return
        }
        val chapterErrorBean = ChapterErrorBean()
        val book = readStatus!!.book
        chapterErrorBean.bookName = getEncode(book.name)
        chapterErrorBean.author = getEncode(book.author)
        chapterErrorBean.channelCode = if (Constants.QG_SOURCE == book.site) "1" else "2"
        val bookChapterDao = BookChapterDao(readReference?.get(), book.book_id)
        val currChapter = bookChapterDao.getChapterBySequence(readStatus!!.sequence)
        if (currChapter == null) {
            val time = Observable.timer(1000, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        readReference?.get()?.showToastShort("已发送")
                    })
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
                })
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

    override fun onOriginClick() {
        var url: String? = null
        if (mReaderViewModel != null && mReaderViewModel!!.currentChapter != null) {
            url = UrlUtils.buildContentUrl(mReaderViewModel!!.currentChapter!!.curl)
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
            if (readStatus != null) {
                data.put("bookid", readStatus!!.book_id)
            }
            StartLogClickUtil.upLoadEventLog(readReference?.get(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.ORIGINALLINK, data)
        } else {
            Toast.makeText(readReference?.get(), "无法查看原文链接", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onTransCodingClick() {
        showDisclaimerActivity()
    }

    inner class CacheUpdateReceiver(weak: WeakReference<ReadingActivity>) : BroadcastReceiver() {
        private val mActivityWeakReference: WeakReference<ReadingActivity> = weak
        override fun onReceive(context: Context, intent: Intent) {
            val book = intent.getSerializableExtra(Constants.REQUEST_ITEM) as Book
            if (Constants.QG_SOURCE != book.site) {
                if (mActivityWeakReference.get() != null && readStatus!!.book.book_id == book.book_id) {
                    val bundle = Bundle()
                    bundle.putInt("sequence", readStatus!!.sequence)
                    bundle.putInt("offset", readStatus!!.offset)
                    bundle.putSerializable("book", readStatus!!.book)
                    bundle.putSerializable(Constants.REQUEST_ITEM, readStatus!!.requestItem)
                    val fresh = Intent(mActivityWeakReference.get(), ReadingActivity::class.java)
                    fresh.putExtras(bundle)
                    fresh.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    mActivityWeakReference?.get()?.startActivity(fresh)
                }
            }
        }
    }
}