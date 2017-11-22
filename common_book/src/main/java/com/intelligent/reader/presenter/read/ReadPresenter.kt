package com.intelligent.reader.presenter.read

import android.app.Activity
import android.app.Notification
import android.app.PendingIntent
import android.content.*
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import net.lzbook.kit.ad.OwnNativeAdManager
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.data.db.BookChapterDao
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.*
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v4.content.LocalBroadcastManager
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.*
import android.widget.*
import com.android.volley.VolleyError
import com.android.volley.toolbox.ImageLoader
import com.bumptech.glide.Glide
import com.dingyueads.sdk.Native.YQNativeAdInfo
import com.dingyueads.sdk.NativeInit
import com.dingyueads.sdk.Utils.LogUtils
import com.intelligent.reader.R
import com.intelligent.reader.activity.*
import com.intelligent.reader.app.BookApplication
import com.intelligent.reader.fragment.CatalogMarkFragment
import com.intelligent.reader.presenter.IPresenter
import com.intelligent.reader.read.animation.BitmapManager
import com.intelligent.reader.read.help.*
import com.intelligent.reader.read.page.*
import com.intelligent.reader.receiver.DownBookClickReceiver
import com.intelligent.reader.util.EventBookStore
import iyouqu.theme.ThemeMode
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.component.service.DownloadService
import net.lzbook.kit.cache.imagecache.ImageCacheManager
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.*
import net.lzbook.kit.repair_books.RepairHelp
import net.lzbook.kit.request.RequestExecutor
import net.lzbook.kit.request.RequestFactory
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.request.own.OtherRequestService
import net.lzbook.kit.tasks.BaseAsyncTask
import net.lzbook.kit.utils.*
import java.io.UnsupportedEncodingException
import java.lang.ref.WeakReference
import java.net.URLEncoder
import java.util.*


/**
 * Created by yuchao on 2017/11/14 0014.
 */
class ReadPresenter : IPresenter<ReadPreInterface.View>, NovelHelper.OnHelperCallBack, IReadDataFactory.ReadDataListener, CallBack, PageInterface.OnOperationClickListener, DownloadService.OnDownloadListener {

    private val TAG = ReadPresenter::class.java!!.getSimpleName()

    override var view: ReadPreInterface.View? = null

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
    private val font_count = 50
    private var readStatus: ReadStatus? = null
    var downloadService: DownloadService? = null
    var isRestDialogShow = false
    var stampTime: Long = 0
    private var mContext: Context? = null
    private var pageView: PageInterface? = null
    private var sourcesList: ArrayList<Source>? = null
    private var isSourceListShow: Boolean = false
    // 系统存储设置
    private var sp: SharedPreferences? = null
    private var modeSp: SharedPreferences? = null
    private var isSubed: Boolean = false
    private var mTicker: TimerRunnable? = null
    private var mCalendar: Calendar? = null
    private var mTimerStopped = false
    private var bookChapterDao: BookChapterDao? = null
    private var mNovelLoader: NovelDownloader? = null
    private var mBookDaoHelper: BookDaoHelper? = null
    private var screen_moding = false
    private var isFromCover = true
    private var myNovelHelper: NovelHelper? = null
    private var dataFactory: IReadDataFactory? = null
    private var autoSpeed: Int = 0
    private var auto_menu: AutoReadMenu? = null
    private var batteryPercent: Float = 0.toFloat()
    private var mDialog: MyDialog? = null
    private var is_dot_orientation = false// 横竖屏打点
    private var current_mode: Int = 0
    private var time_text: CharSequence? = null
    var versionCode: Int = 0
        get() = 0
    private var ownNativeAdManager: OwnNativeAdManager? = null
    private var isAcvNovelActive = true
    private var rest_tips_runnable: Runnable? = null
    private var isRestPress = false
    private var actNovelRunForeground = true
    private var readReference: WeakReference<ReadingActivity>? = null

    //    private int lastMode = -1;
    private val handler = UiHandler(this)

    constructor(act: ReadingActivity) {
        readReference = WeakReference(act)
        view = act as ReadPreInterface.View
    }

    /**
     * 接受按下电源键的广播
     */
    private val mPowerOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (Intent.ACTION_SCREEN_OFF == action) {
                /**
                 * 接受在阅读页，监听按下电源键的广播处理
                 */
                if (isAcvNovelActive && handler != null && rest_tips_runnable != null) {
                    isAcvNovelActive = false
                    handler.removeCallbacks(rest_tips_runnable)
                    rest_tips_runnable = null
                }
            }
        }
    }
    private var statisticManager: StatisticManager? = null
    private var isSlideToAuto = false
    private var resources: Resources? = null
    private var myDialog: MyDialog? = null
    private var requestFactory: RequestFactory? = null
    private var type = -1
    var currentThemeMode: String? = null
        get() = null
    private var lastMode = -1

    /**
     * 接受电量改变广播
     */
    private val mBatInfoReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                if (pageView != null) {
                    val level = intent.getIntExtra("level", 0)
                    val scale = intent.getIntExtra("scale", 100)
                    batteryPercent = level.toFloat() / scale.toFloat()
                    pageView?.freshBattery(batteryPercent)
                }
            }
        }
    }
    private var mCacheUpdateReceiver: CacheUpdateReceiver? = null
    private var mReadOptionPresenter: ReadOptionPresenter? = null
    private var mCatalogMarkPresenter: CatalogMarkPresenter? = null
    private val sc = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName) {}

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            downloadService = (service as DownloadService.MyBinder).service
            BaseBookApplication.setDownloadService(downloadService)
            downloadService?.setOnDownloadListener(this@ReadPresenter)
        }
    }

    fun onCreateInit(savedInstanceState: Bundle?) {
        AppLog.e(TAG, "onCreate")

        sp = PreferenceManager.getDefaultSharedPreferences(readReference?.get()?.getApplicationContext())
        Constants.isFullWindowRead = sp!!.getBoolean("read_fullwindow", true)
        Constants.PAGE_MODE = sp!!.getInt("page_mode", 0)
        Constants.FULL_SCREEN_READ = sp!!.getBoolean("full_screen_read", false)
        Constants.isSlideUp = Constants.PAGE_MODE == 3
        Constants.isVolumeTurnover = sp!!.getBoolean("sound_turnover", true)
        AppLog.e("getAdsStatus", "novel_onCreate")
        versionCode = AppUtils.getVersionCode()
        AppLog.e(TAG, "versionCode: " + versionCode)
        readStatus = ReadStatus(readReference?.get()?.getApplicationContext())
        BookApplication.getGlobalContext().readStatus = readStatus
        view?.setReadStatus(readStatus!!)
        autoSpeed = readStatus!!.autoReadSpeed()
        myNovelHelper = NovelHelper(readReference?.get(), readStatus, handler)
        myNovelHelper?.setOnHelperCallBack(this)
        downloadService = BaseBookApplication.getDownloadService()
        requestFactory = RequestFactory()

        // 初始化窗口基本信息
        initWindow()
        dataFactory = ReadDataFactory(readReference?.get(), readReference?.get(), readStatus, myNovelHelper)
        dataFactory?.setReadDataListener(this)

        setOrientation()
        getSavedState(savedInstanceState)

        RepairHelp.showFixMsg(readReference?.get(), readStatus!!.book, RepairHelp.FixCallBack {
            if (readReference != null && readReference!!.get() != null && !readReference!!.get()!!.isFinishing) {
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

        view!!.initCatlogView()

        initBookState()
        // 初始化view
        view?.initView(dataFactory!!)
        // 初始化监听器
        initListener()
        //	开启护眼计时器
        startRestTimer()
        //注册一个监听按下电源键的广播
        readReference?.get()?.registerReceiver(mPowerOffReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))
        getBookContent()
        if (BaseBookApplication.getDownloadService() == null) {
            BookHelper.reStartDownloadService()
        }

    }

    fun initCatalogPresenter(catalogMarkFragment: CatalogMarkFragment, optionHeader: ReadOptionHeader) {
        mCatalogMarkPresenter = CatalogMarkPresenter(readStatus!!, dataFactory!!)
        mCatalogMarkPresenter!!.view = catalogMarkFragment

        mReadOptionPresenter = ReadOptionPresenter(readReference?.get() as Activity, readStatus!!, dataFactory!!)
        mReadOptionPresenter!!.view = optionHeader

        view?.initPresenter(mReadOptionPresenter, mCatalogMarkPresenter)
    }

    fun onNewIntent(intent: Intent) {
        this.lastMode = -1
        pageView?.clear()
        showMenu(false)
        AppLog.d("ReadingActivity", "onNewIntent:")
        this.sp = PreferenceManager.getDefaultSharedPreferences(readReference?.get()?.getApplicationContext())
        Constants.isFullWindowRead = sp!!.getBoolean("read_fullwindow", true)
        Constants.PAGE_MODE = sp!!.getInt("page_mode", 0)
        Constants.isSlideUp = Constants.PAGE_MODE == 3
        versionCode = AppUtils.getVersionCode()
        AppLog.e(TAG, "versionCode: " + versionCode)
        if (readStatus != null) {
            readStatus!!.recycleResource()
            readStatus!!.recycleResourceNew()
        }
        readStatus = ReadStatus(readReference?.get()?.getApplicationContext())
        BookApplication.getGlobalContext().readStatus = readStatus
        view?.setReadStatus(readStatus!!)
        autoSpeed = readStatus!!.autoReadSpeed()
        myNovelHelper = NovelHelper(readReference?.get(), readStatus, handler)
        myNovelHelper?.setOnHelperCallBack(this)

        requestFactory = RequestFactory()
        dataFactory?.clean()
        dataFactory = ReadDataFactory(readReference?.get()?.getApplicationContext(), readReference?.get(), readStatus, myNovelHelper)
        dataFactory?.setReadDataListener(this)
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
        view?.initView(dataFactory!!)
        // 初始化监听器
        initListener()
        //	开启护眼计时器
        startRestTimer()
        //注册一个监听按下电源键的广播
        readReference?.get()?.registerReceiver(mPowerOffReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))
        getBookContent()
        if (BaseBookApplication.getDownloadService() == null) {
            BookHelper.reStartDownloadService()
        }

        changeMode(Constants.MODE)
    }

    /**
     * 休息提醒计时器
     */
    private fun startRestTimer() {
        val read_rest_time = sp!!.getInt("read_rest_time", Constants.read_rest_time / 60000) * 60000
        /**
         * 增加健壮性判断，当用户选择休息提示为：“永不   ”时，直接return，避免多出修改字段类型为long
         */
        //        if (read_rest_time == Integer.MAX_VALUE * 60000) {
        //            return;
        //        }

        rest_tips_runnable = Runnable {
            mDialog = MyDialog(readReference?.get(), R.layout.reading_resttime, Gravity.CENTER, false)
            val iv_reset_ad = mDialog!!.findViewById(R.id.iv_reset_ad) as ImageView
            val iv_reset_ad_logo = mDialog!!.findViewById(R.id.iv_reset_ad_logo) as ImageView
            val iv_reset_ad_image = mDialog!!.findViewById(R.id.iv_reset_ad_image) as ImageView
            val ll_reset_layout = mDialog!!.findViewById(R.id.ll_reset_layout) as LinearLayout
            val iv_close = mDialog!!.findViewById(R.id.iv_close) as ImageView

            iv_reset_ad_image.visibility = View.INVISIBLE

            try {
                if ("night" == ResourceUtil.mode) {
                    ll_reset_layout.alpha = 0.6f
                } else {
                    ll_reset_layout.alpha = 1.0f
                }
            } catch (e: NoSuchMethodError) {
                e.printStackTrace()
            }

            if (ownNativeAdManager == null) {
                ownNativeAdManager = OwnNativeAdManager.getInstance(readReference?.get())
            }

            ownNativeAdManager?.loadAd(NativeInit.CustomPositionName.REST_POSITION)

            val nativeADInfo = ownNativeAdManager?.getSingleADInfo(NativeInit.CustomPositionName
                    .REST_POSITION)

            iv_close.setOnClickListener {
                mDialog?.dismiss()

                if (iv_reset_ad != null) {
                    val bitmapDrawable = iv_reset_ad.drawable as BitmapDrawable
                    if (bitmapDrawable != null) {
                        AppLog.e(TAG, "BitmapDrawable != null")
                        val bitmap = bitmapDrawable.bitmap
                        if (bitmap != null && !bitmap.isRecycled) {
                            AppLog.e(TAG, "Bitmap != null")
                            //                                    bitmap.recycle();
                        }
                    }
                }
            }

            if (nativeADInfo != null) {
                val advertisement = nativeADInfo.advertisement
                if (advertisement != null) {
                    val image_url = advertisement.imageUrl
                    if (!TextUtils.isEmpty(image_url)) {
                        ImageCacheManager.getInstance().imageLoader.get(image_url, object : ImageLoader.ImageListener {
                            override fun onResponse(imageContainer: ImageLoader.ImageContainer?, b: Boolean) {
                                if (imageContainer != null) {
                                    val bitmap_icon = imageContainer.bitmap
                                    if (bitmap_icon != null) {
                                        iv_reset_ad.setImageBitmap(bitmap_icon)

                                        if ("广点通" == advertisement.rationName) {
                                            iv_reset_ad_logo.setImageResource(R.drawable.icon_ad_gdt)
                                        } else if ("百度" == advertisement.rationName) {
                                            iv_reset_ad_logo.setImageResource(R.drawable.icon_ad_bd)
                                        } else if ("360" == advertisement.rationName) {
                                            iv_reset_ad_logo.setImageResource(R.drawable.icon_ad_360)
                                        } else {
                                            iv_reset_ad_logo.setImageResource(R.drawable.icon_ad_default)
                                        }

                                        iv_reset_ad_image.visibility = View.VISIBLE

                                        StatServiceUtils.statBookEventShow(readReference?.get(), StatServiceUtils.type_ad_reset_30)
                                    }
                                }
                            }

                            override fun onErrorResponse(volleyError: VolleyError) {}
                        })
                    }
                    iv_reset_ad.tag = nativeADInfo
                    try {
                        if (statisticManager == null) {
                            statisticManager = StatisticManager.getStatisticManager()
                        }
                        val novel = dataFactory?.transformation()
                        statisticManager!!.schedulingRequest(readReference?.get(), ll_reset_layout, nativeADInfo, novel, StatisticManager
                                .TYPE_SHOW, NativeInit.ad_position[3])
                    } catch (e: IllegalArgumentException) {
                        e.printStackTrace()
                    }

                }
                iv_reset_ad.setOnClickListener { view ->
                    if (view.tag != null) {
                        val yqNativeAdInfo = view.tag as YQNativeAdInfo
                        if (yqNativeAdInfo != null) {
                            try {
                                if (statisticManager == null) {
                                    statisticManager = StatisticManager.getStatisticManager()
                                }
                                val novel = dataFactory?.transformation()
                                statisticManager!!.schedulingRequest(readReference?.get(), view, yqNativeAdInfo, novel,
                                        StatisticManager.TYPE_CLICK, NativeInit.ad_position[3])
                            } catch (e: IllegalArgumentException) {
                                e.printStackTrace()
                            }

                            StatServiceUtils.statBookEventClick(readReference?.get(), StatServiceUtils
                                    .type_ad_reset_30)
                            if (Constants.DEVELOPER_MODE) {
                                Toast.makeText(readReference?.get(), "你点击了广告", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            try {
                if (nativeADInfo != null) {
                    mDialog?.show()
                    isRestDialogShow = true
                    mDialog?.setOnDismissListener(DialogInterface.OnDismissListener {
                        //								isRestDialogShow = true;

                        if (statisticManager == null) {
                            statisticManager = StatisticManager.getStatisticManager()
                        }
                        val novel = dataFactory?.transformation()
                        statisticManager!!.schedulingRequest(readReference?.get(), iv_reset_ad, nativeADInfo, novel, StatisticManager
                                .TYPE_END, NativeInit.ad_position[3])

                        if (!isRestPress) {
                            //									Log.e(TAG, "按下Back键了，屏幕变暗了！");
                            //									handler.postDelayed(rest_tips_runnable,
                            // read_rest_time);
                            if (handler != null) {
                                handler.removeCallbacks(rest_tips_runnable)
                                startRestTimer()
                            }
                        } else {
                            /**当弹出休息提示对话框时候，用户点击休息一下按钮后，对话框消失，
                             * 需要重置isRestPress按钮的默认值为false;
                             * 防止点击休息一下后，在阅读非书架书籍时会弹出添加到书架的对话框，
                             * 当点击屏幕空白处取消添加到书架对话框继续阅读后，再下次弹出的休息提醒对话框时，
                             * 如果用户点击继续看后，休息提醒对话框消失，但是计时器不会重新启动的bug
                             */

                            isRestPress = false
                        }
                        if (isRestDialogShow) {
                            isRestDialogShow = false
                        }
                    })
                } else {
                    if (handler != null) {
                        handler.removeCallbacks(rest_tips_runnable)
                        handler.postDelayed(rest_tips_runnable, 60000)//获取广告null 重新获取
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (mDialog == null) {
            handler.postDelayed(rest_tips_runnable, read_rest_time.toLong())
        } else if (!mDialog!!.isShowing) {
            handler.postDelayed(rest_tips_runnable, read_rest_time.toLong())
        }
    }

    /**
     * 处理书籍状态
     */
    private fun initBookState() {
        // 判断是否订阅
        mBookDaoHelper = BookDaoHelper.getInstance()
        readStatus!!.book_id = readStatus!!.book.book_id
        isSubed = mBookDaoHelper!!.isBookSubed(readStatus!!.book_id)
        AppLog.e(TAG, "初始化书籍状态: " + readStatus!!.book_id)
        bookChapterDao = BookChapterDao(readReference?.get()?.getApplicationContext(), readStatus!!.book_id)
        if (isSubed) {
            readStatus!!.book = mBookDaoHelper!!.getBook(readStatus!!.book_id, 0)
        }
        if (readStatus!!.sequence < -1) {
            readStatus!!.sequence = -1
        } else if (isSubed && readStatus!!.sequence + 1 > readStatus!!.book.chapter_count) {
            readStatus!!.sequence = readStatus!!.book.chapter_count - 1
        }
    }

    fun onConfigurationChanged(catalogMarkFragment: CatalogMarkFragment, optionHeader: ReadOptionHeader) {
        this.lastMode = -1
        // 初始化窗口基本信息
        pageView?.clear()
        initWindow()
        AppLog.e(TAG, "onConfigurationChanged")

        mCatalogMarkPresenter = CatalogMarkPresenter(readStatus!!, dataFactory!!)
        mCatalogMarkPresenter!!.view = catalogMarkFragment

        mReadOptionPresenter = ReadOptionPresenter(readReference?.get()!!, readStatus!!, dataFactory!!)
        mReadOptionPresenter!!.view = optionHeader

        view?.initPresenter(mReadOptionPresenter, mCatalogMarkPresenter)

        initBookState()
        // 初始化view
        view?.initView(dataFactory!!)
        // 初始化监听器
        initListener()
        getBookContent()
        if (BaseBookApplication.getDownloadService() == null) {
            BookHelper.reStartDownloadService()
        }
        setMode()
        readStatus!!.chapterCount = readStatus!!.book.chapter_count
        // 注册一个接受广播类型
        readReference?.get()?.registerReceiver(mBatInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        pageView?.freshBattery(batteryPercent)

        changeMode(Constants.MODE)
    }

    private fun getSavedState(savedInstanceState: Bundle?) {
        val bundle = readReference?.get()?.getIntent()!!.getExtras()
        if (savedInstanceState != null) {
            // 从保存状态中获取
            // 章节序
            readStatus!!.sequence = savedInstanceState.getInt("sequence", 0)
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
            if (dataFactory == null) {
                dataFactory = ReadDataFactory(readReference?.get()?.getApplicationContext(), readReference?.get(), readStatus, myNovelHelper)
                dataFactory?.setReadDataListener(this)
            }
            dataFactory?.currentChapter = savedInstanceState.getSerializable("currentChapter") as Chapter?
            currentThemeMode = savedInstanceState.getString("thememode", readReference?.get()?.mThemeHelper?.getMode())
            AppLog.e(TAG, "getState1" + readStatus!!.sequence)
        } else {
            // 从bundle中获取
            readStatus!!.sequence = bundle.getInt("sequence", 0)
            val requestItem = bundle.getSerializable(Constants.REQUEST_ITEM)
            if (requestItem != null) {
                readStatus!!.setRequestItem(requestItem as RequestItem)
            }
            readStatus!!.offset = bundle.getInt("offset", 0)
            readStatus!!.book = bundle.getSerializable("book") as Book?
            readStatus!!.book_id = if (readStatus!!.book == null) "" else readStatus!!.book.book_id
            currentThemeMode = bundle.getString("thememode", readReference?.get()?.mThemeHelper?.getMode())
            AppLog.e(TAG, "getState2" + readStatus!!.sequence)
        }

        if (readStatus!!.sequence == -2) {
            readStatus!!.sequence = -1
        }
    }

    /*
     * 设置屏幕方向 port land
     */
    private fun setOrientation() {
        if (!screen_moding) {
            if (sp!!.getInt("screen_mode", 3) == Configuration.ORIENTATION_PORTRAIT) {
                if (!is_dot_orientation) {
                    is_dot_orientation = true
                }
                readReference?.get()?.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                Constants.IS_LANDSCAPE = false
            } else if (sp!!.getInt("screen_mode", 3) == Configuration.ORIENTATION_LANDSCAPE && readReference?.get()?.getResources()!!
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
        dataFactory?.getChapterByLoading(ReadingActivity.MSG_LOAD_CUR_CHAPTER, readStatus!!.sequence)

    }

    override fun showChangeNetDialog() {
        var act = readReference?.get()
        if (act == null) {
            return
        }
        StatServiceUtils.statAppBtnClick(act, StatServiceUtils.read_limit)
        if (!act!!.isFinishing()) {
            myDialog = MyDialog(act, R.layout.nonet_read_dialog)
            myDialog!!.setCanceledOnTouchOutside(false)
            val nonet_read_bookshelf = myDialog!!.findViewById(R.id.nonet_read_backtoshelf) as ImageButton
            val nonet_read_continue = myDialog!!.findViewById(R.id.nonet_read_continue) as ImageButton

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
            if (!myDialog!!.isShowing) {
                try {
                    myDialog!!.show()
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
            readStatus!!.screenWidth = realSize.x
            readStatus!!.screenHeight = realSize.y
        } else {
            readStatus!!.screenWidth = dm.widthPixels
            readStatus!!.screenHeight = dm.heightPixels
        }

        readStatus!!.screenDensity = dm.density
        readStatus!!.screenScaledDensity = dm.scaledDensity
        // 保存字体、亮度、阅读模式
        modeSp = readReference?.get()?.getSharedPreferences("config", Context.MODE_PRIVATE)
        // 设置字体
        if (sp!!.contains("novel_font_size")) {
            Constants.FONT_SIZE = sp!!.getInt("novel_font_size", 18)
        } else {
            Constants.FONT_SIZE = 18
        }
    }

    /**
     * 初始化view
     */
    fun initData(v: PageInterface) {
        pageView = v
        resources = readReference?.get()?.getResources()
        pageView?.setReadFactory(dataFactory)
        pageView?.init(readReference?.get(), readStatus, myNovelHelper)
        pageView?.setCallBack(this)
        pageView?.setOnOperationClickListener(this)
        dataFactory?.setPageView(pageView)
        myNovelHelper?.setPageView(pageView)
    }


    /**
     * 首次进入阅读页面 展示广告小图
     */
    private fun initReadingAd() {
        if (readReference == null || readReference!!.get() == null) {
            return
        }

        if (ownNativeAdManager == null) {
            ownNativeAdManager = OwnNativeAdManager.getInstance(readReference?.get())
        }
        ownNativeAdManager?.setActivity(readReference?.get())
        if (!Constants.isSlideUp) {
            ownNativeAdManager?.loadAdForMiddle(NativeInit.CustomPositionName.READING_MIDDLE_POSITION)
            if (Constants.IS_LANDSCAPE) {
                OwnNativeAdManager.getInstance(readReference?.get()).loadAd(NativeInit.CustomPositionName.SUPPLY_READING_SPACE)
            } else {
                OwnNativeAdManager.getInstance(readReference?.get()).loadAd(NativeInit.CustomPositionName.READING_POSITION)
            }
        }
        if (Constants.isSlideUp && Constants.dy_ad_readPage_slide_switch_new) {
            if (Constants.IS_LANDSCAPE) {
                OwnNativeAdManager.getInstance(readReference?.get()).loadAd(NativeInit.CustomPositionName.LANDSCAPE_SLIDEUP_POPUPAD)
            } else {
                OwnNativeAdManager.getInstance(readReference?.get()).loadAd(NativeInit.CustomPositionName.SLIDEUP_POPUPAD_POSITION)
                OwnNativeAdManager.getInstance(readReference?.get()).loadAd(NativeInit.CustomPositionName.LANDSCAPE_SLIDEUP_POPUPAD)
            }
        }
    }

    /**
     * 初始化监听器
     */
    private fun initListener() {
        if (downloadService == null) {
            reStartDownloadService(readReference?.get()!!)
            downloadService = BaseBookApplication.getDownloadService()
        }
        if (downloadService != null)
            downloadService!!.setOnDownloadListener(this)
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
    private fun initTime() {
        mTimerStopped = false
        if (mCalendar == null) {
            mCalendar = Calendar.getInstance()
        }
        mTicker = TimerRunnable(this)
        mTicker?.run()
    }

    /**
     * 刷新页面
     */
    private fun refreshPage() {
        //readStatus.isCanDrawFootView = (readStatus.sequence != -1);
        if (readStatus!!.sequence == -1) {
            readStatus!!.isCanDrawFootView = false
        } else {
            readStatus!!.isCanDrawFootView = true
        }
    }

    /**
     * 预加载
     */
    private fun downloadNovel() {
        if (mNovelLoader != null && mNovelLoader!!.status == AsyncTask.Status.RUNNING) {
            mNovelLoader?.cancel(true)
        }

        if (mBookDaoHelper!!.isBookSubed(readStatus!!.book_id)) {
            var num = BookHelper.CHAPTER_CACHE_COUNT
            val max = readStatus!!.chapterCount - 1 - readStatus!!.sequence
            if (max > 0) {
                if (max < num) {
                    num = max
                }
                mNovelLoader = NovelDownloader()
                mNovelLoader?.execute2(num)
            }
        }

    }

    fun searchChapterCallBack(sourcesList: ArrayList<Source>?) {
        if (myNovelHelper != null && dataFactory != null && dataFactory!!.currentChapter != null && !TextUtils.isEmpty(dataFactory!!.currentChapter
                .curl) && sourcesList != null) {
            //if (readStatus.book.dex == 1 && !TextUtils.isEmpty(dataFactory.currentChapter.curl) && sourcesList != null) {
            myNovelHelper?.showSourceDialog(dataFactory, dataFactory?.currentChapter?.curl, sourcesList)
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
        if (mNovelLoader != null && mNovelLoader!!.status == AsyncTask.Status.RUNNING) {
            mNovelLoader!!.cancel(true)
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
            readStatus!!.firstChapterCurl = ""
            dataFactory?.currentChapter = null

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
            dataFactory?.chapterList?.clear()
            openCategoryPage()
        }
    }

    private fun dismissDialog() {
        if (myDialog != null && myDialog!!.isShowing) {
            myDialog!!.dismiss()
        }
    }

    override fun deleteBook() {
        if (mBookDaoHelper!!.isBookSubed(readStatus!!.book_id)) {
            mBookDaoHelper!!.deleteBook(readStatus!!.book_id)
        }
        readReference?.get()?.finish()
    }

    override fun openAutoReading(open: Boolean) {
        onReadAuto()
    }

    override fun addBookShelf(isAddShelf: Boolean) {
        if (isAddShelf && mBookDaoHelper != null && readStatus!!.book != null) {
            readStatus!!.book.sequence = readStatus!!.sequence
            readStatus!!.book.offset = readStatus!!.offset
            readStatus!!.book.sequence_time = System.currentTimeMillis()
            readStatus!!.book.last_updateSucessTime = System.currentTimeMillis()
            readStatus!!.book.readed = 1
            if (dataFactory != null) {
                if (dataFactory?.chapterList != null && dataFactory!!.chapterList!!.size > 0) {
                    val chapter = dataFactory!!.chapterList[dataFactory!!.chapterList!!.size - 1]
                    readStatus!!.book.extra_parameter = chapter.extra_parameter
                }
                bookChapterDao?.insertBookChapter(dataFactory?.chapterList)
            }
            val succeed = mBookDaoHelper?.insertBook(readStatus!!.book)
            Toast.makeText(readReference?.get(), if (succeed!!) R.string.reading_add_succeed else R.string.reading_add_fail,
                    Toast.LENGTH_SHORT).show()
        }
        val map1 = HashMap<String, String>()
        if (readStatus!!.book != null) {
            map1.put("bookid", readStatus!!.book.book_id)
        }
        if (dataFactory != null && dataFactory?.currentChapter != null) {
            map1.put("chapterid", dataFactory!!.currentChapter!!.chapter_id)
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
            val loadingPage = dataFactory?.getCustomLoadingPage()
            loadingPage?.loading {
                OtherRequestService.requestBookSourceChange(dataFactory?.mHandler, ReadingActivity.MSG_SEARCH_CHAPTER, -144, readStatus!!.book_id)
                null
            }
            dataFactory?.loadingError(loadingPage)
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
        if (act!!.getResources()!!.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.rb_click_portrait_btn)
            val data = HashMap<String, String>()
            data.put("type", "2")
            StartLogClickUtil.upLoadEventLog(readReference?.get(), StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.HPMODEL, data)
            act!!.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            screen_mode.putInt("screen_mode", Configuration.ORIENTATION_PORTRAIT)
            Constants.IS_LANDSCAPE = false
        } else if (act!!.getResources()!!.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.rb_click_landscape_btn)
            val data = HashMap<String, String>()
            data.put("type", "1")
            StartLogClickUtil.upLoadEventLog(readReference?.get(), StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.HPMODEL, data)
            act!!.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
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
        myNovelHelper?.getChapterContent(readReference?.get(), dataFactory?.currentChapter, readStatus!!.book,
                false)
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

        if (dataFactory == null || readStatus == null || myNovelHelper == null) {
            return
        }
        dataFactory!!.nextChapter = null
        readStatus!!.sequence = readStatus!!.novel_progress
        readStatus!!.offset = 0
        myNovelHelper!!.isShown = false
        myNovelHelper!!.getChapterContent(readReference?.get(), dataFactory!!.currentChapter, readStatus!!.book,
                false)
        readStatus!!.currentPage = 1
        refreshPage()
        if (pageView == null) {
            return
        }
        if (Constants.isSlideUp) {
            pageView!!.getChapter(false)
        } else {
            pageView!!.drawCurrentPage()
            pageView!!.drawNextPage()
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
        if (mReadOptionPresenter != null)
            mReadOptionPresenter!!.view!!.show(false)
        full(true)
    }

    /*
     * 显示隐藏菜单
     */
    fun showMenu(isShow: Boolean) {
        if (pageView == null) {
            return
        }
        if (pageView!!.isAutoReadMode && isShow) {
            return
        }
        clearOtherPanel()
        if (isShow) {
            full(false)
            changeMarkState()
            mReadOptionPresenter!!.view!!.show(true)
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
                setTextColor(readReference?.get()?.getResources()!!.getColor(R.color.reading_text_color_night))
                setPageBackColor(readReference?.get()?.getResources()!!.getColor(R.color.reading_backdrop_night))

                setBackground()
                setBatteryBackground(R.drawable.reading_batty_night2)
            }
            else -> {
                setTextColor(readReference?.get()?.getResources()!!.getColor(R.color.reading_text_color_first))
                setPageBackColor(Color.parseColor("#C2B282"))

                setBackground()
                setBatteryBackground(R.drawable.reading_batty_day)
            }
        }
    }

    private fun setTextColor(color: Int) {
        pageView?.setTextColor(color)
    }

    private fun setBackground() {
        pageView?.setBackground()
    }

    private fun setBatteryBackground(resourceId: Int) {
        pageView?.changeBatteryBg(resourceId)
    }

    private fun setPageBackColor(color: Int) {
        pageView?.setPageBackColor(color)
    }

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

        // 注册一个接受广播类型
        readReference?.get()?.registerReceiver(mBatInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        // 设置全屏
        if (isFromCover && Constants.IS_LANDSCAPE) {
            return
        }
        view?.checkModeChange()

        val content_mode = sp!!.getInt("content_mode", 51)
        if (isSubed) {
            readStatus!!.book = mBookDaoHelper!!.getBook(readStatus!!.book_id, 0)
        }
        readStatus!!.isInMobiViewClicking = false
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
            startRestTimer()
        }
        if (!isAcvNovelActive && !isRestDialogShow) {
            isAcvNovelActive = true
            startRestTimer()
        }

        if (dataFactory != null && readStatus != null && Constants.isNetWorkError) {
            Constants.isNetWorkError = false
            dataFactory?.getChapterByLoading(ReadingActivity.MSG_LOAD_CUR_CHAPTER, readStatus!!.sequence)
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

    fun onPause() {
        isFromCover = false
        if (isSubed) {
            if (readStatus!!.book.book_type == 0) {
                myNovelHelper?.saveBookmark(dataFactory?.chapterList, readStatus?.book_id, readStatus!!.sequence,
                        readStatus!!.offset, mBookDaoHelper)
                // 统计阅读章节数
                val spUtils = SharedPreferencesUtils(PreferenceManager
                        .getDefaultSharedPreferences(readReference?.get()))
                spUtils.putInt("readed_count", Constants.readedCount)
            }
        }
        pageView?.pauseAutoRead()

    }

    internal var isFirstVisiable = true

    fun onWindowFocusChanged(hasFocus: Boolean) {
        if (isFirstVisiable && hasFocus) {
            isFirstVisiable = false
            initReadingAd()
        }
    }

    fun onStop() {
        pageView?.removeAdView()
        LocalBroadcastManager.getInstance(readReference?.get()).unregisterReceiver(mCacheUpdateReceiver)

        if (actNovelRunForeground && handler != null && rest_tips_runnable != null) {
            actNovelRunForeground = false
            handler.removeCallbacks(rest_tips_runnable)
            rest_tips_runnable = null
        }
    }

    fun onDestroy() {
        if (mNovelLoader != null && mNovelLoader!!.status == AsyncTask.Status.RUNNING) {
            mNovelLoader!!.cancel(true)
        }

        if (readStatus != null && dataFactory != null && dataFactory!!.currentChapter != null && readStatus!!.requestItem != null) {
            //按照此顺序传值 当前的book_id，阅读章节，书籍源，章节总页数，当前阅读页，当前页总字数，当前页面来自，开始阅读时间,结束时间,阅读时间,是否有阅读中间退出行为,书籍来源1为青果，2为智能
            StartLogClickUtil.upLoadReadContent(readStatus!!.book_id, dataFactory!!.currentChapter.chapter_id + "", readStatus!!.source_ids, readStatus!!.pageCount.toString() + "",
                    readStatus!!.currentPage.toString() + "", readStatus!!.currentPageConentLength.toString() + "", readStatus!!.requestItem.fromType.toString() + "",
                    readStatus!!.startReadTime.toString() + "", System.currentTimeMillis().toString() + "", (System.currentTimeMillis() - readStatus!!.startReadTime).toString() + "", "false", readStatus!!.requestItem.channel_code.toString() + "")

        }
        AppLog.e(TAG, "onDestroy")
        readStatus!!.isMenuShow = false
        if (mNovelLoader != null && mNovelLoader!!.status == AsyncTask.Status.RUNNING) {
            mNovelLoader?.cancel(true)
        }

        if (mBatInfoReceiver != null) {
            try {
                readReference?.get()?.unregisterReceiver(mBatInfoReceiver)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        /**
         * 注销监听按下电源键的广播
         */
        if (mPowerOffReceiver != null) {
            try {
                readReference?.get()?.unregisterReceiver(mPowerOffReceiver)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
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

        handler?.removeCallbacksAndMessages(null)


        if (ownNativeAdManager != null) {
            //            ownNativeAdManager.recycleResourceFromReading(NativeInit.CustomPositionName.CHANGE_SOURCE_POSITION.toString());
            ownNativeAdManager!!.recycleResourceFromReading(NativeInit.CustomPositionName.READING_MIDDLE_POSITION.toString())
            ownNativeAdManager!!.recycleResourceFromReading(NativeInit.CustomPositionName.READING_POSITION.toString())
            ownNativeAdManager!!.recycleResourceFromReading(NativeInit.CustomPositionName.READING_IN_CHAPTER_POSITION.toString())
            ownNativeAdManager!!.recycleResourceFromReading(NativeInit.CustomPositionName.REST_POSITION.toString())
            ownNativeAdManager!!.recycleResourceFromReading(NativeInit.CustomPositionName.SUPPLY_READING_IN_CHAPTER.toString())
            ownNativeAdManager!!.recycleResourceFromReading(NativeInit.CustomPositionName.SUPPLY_READING_SPACE.toString())
            ownNativeAdManager!!.recycleResourceFromReading(NativeInit.CustomPositionName.SLIDEUP_POPUPAD_POSITION.toString())
            ownNativeAdManager!!.recycleResourceFromReading(NativeInit.CustomPositionName.LANDSCAPE_SLIDEUP_POPUPAD.toString())
            ownNativeAdManager!!.removeHandler()
        }

        Glide.get(readReference?.get()).clearMemory()

        if (readStatus != null) {
            readStatus!!.recycleResource()
            readStatus!!.recycleResourceNew()
        }

        if (dataFactory != null) {
            dataFactory!!.setReadDataListener(null)
            if (dataFactory!!.mHandler != null) {
                dataFactory!!.mHandler.removeCallbacksAndMessages(null)
            }
            dataFactory!!.clean()
        }

        BitmapManager.getInstance().clearBitmap()

        DrawTextHelper.clean()
    }

    fun onSaveInstanceState(outState: Bundle): Bundle? {
        // 保存书签状态
        try {
            outState.putInt("sequence", readStatus!!.sequence)
            outState.putInt("nid", readStatus!!.nid)
            outState.putInt("offset", readStatus!!.offset)
            outState.putSerializable("book", readStatus!!.book)
            if (dataFactory != null && dataFactory?.currentChapter != null) {
                outState.putSerializable("currentChapter", dataFactory?.currentChapter)
            }
            outState.putString("thememode", readReference?.get()?.mThemeHelper?.getMode())
            return outState
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
        return null

    }

    private fun goToBookOver() {
        if (readReference == null || readReference!!.get() == null || readReference!!.get()!!.isFinishing()) {
            return
        }

        val intent = Intent(readReference?.get(), BookEndActivity::class.java)
        val bundle = Bundle()
        bundle.putSerializable(Constants.REQUEST_ITEM, readStatus!!.getRequestItem())
        bundle.putString("bookName", readStatus!!.bookName)
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
                if (dataFactory!!.chapterList != null) {
                    dataFactory!!.chapterList.clear()
                }
                myNovelHelper?.isShown = false
                readStatus!!.currentPage = 1
                dataFactory?.nextChapter = null
                dataFactory?.preChapter = null
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
            intent!!.action = DownBookClickReceiver.action
            intent.putExtra("book_id", book_id)
            pending = PendingIntent.getBroadcast(readReference?.get()?.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            intent = Intent(readReference?.get(), DownloadManagerActivity::class.java)
            pending = PendingIntent.getActivity(readReference?.get()?.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        preNTF.contentIntent = pending
    }

    override fun jumpNextChapter() {
        if (readStatus!!.isMenuShow) {
            showMenu(false)
            return
        }
        dataFactory?.next()
        pageView?.drawCurrentPage()
    }

    override fun onShowMenu(isShow: Boolean) {
        showMenu(isShow)
    }

    override fun onCancelPage() {
        dataFactory?.restore()
        refreshPage()
    }

    override fun onResize() {
        AppLog.e("ReadingActivity", "onResize")
        if (dataFactory?.currentChapter != null && readStatus!!.book != null) {
            myNovelHelper?.getChapterContent(readReference?.get(), dataFactory?.currentChapter, readStatus!!
                    .book, true)
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
            pageView!!.init(readReference?.get(), readStatus, myNovelHelper)
            pageView!!.setCallBack(this)
            pageView!!.setReadFactory(dataFactory)
            dataFactory?.setPageView(pageView)
            myNovelHelper?.setPageView(pageView)
            pageView!!.freshTime(time_text)
            pageView!!.freshBattery(batteryPercent)
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
        if (mNovelLoader != null && mNovelLoader!!.status == AsyncTask.Status.RUNNING) {
            mNovelLoader!!.cancel(true)
        }
        val data = HashMap<String, String>()
        if (readStatus != null) {
            data.put("bookid", readStatus!!.book_id)
        }
        if (dataFactory != null && dataFactory?.currentChapter != null) {
            data.put("chapterid", dataFactory!!.currentChapter.book_id)
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
            pageView!!.setReadFactory(dataFactory)
            dataFactory?.setPageView(pageView)
            myNovelHelper?.setPageView(pageView)
            pageView!!.freshTime(time_text)
            pageView!!.freshBattery(batteryPercent)
            pageView!!.drawCurrentPage()
            changeMode(current_mode)
            temp?.clear()
        }
        pageView?.startAutoRead()
        showMenu(false)
        showMenu(false)
    }

    fun onChangeMode(mode: Int) {
        changeMode(mode)
    }

    fun onRedrawPage() {
        if (pageView is ScrollPageView && (pageView as ScrollPageView)!!.tempChapter != null) {
            myNovelHelper!!.getChapterContent(readReference?.get(), (pageView as ScrollPageView)!!.tempChapter,
                    readStatus!!.book, true)
        } else {
            myNovelHelper!!.getChapterContent(readReference?.get(), dataFactory!!.currentChapter, readStatus
            !!.book, true)
        }
        refreshPage()
        pageView!!.drawCurrentPage()
        pageView!!.drawNextPage()
        pageView!!.getChapter(true)
    }

    fun onJumpChapter() {
        dataFactory!!.getChapterByLoading(ReadingActivity.MSG_JUMP_CHAPTER, readStatus!!.novel_progress)
    }

    fun onJumpPreChapter() {
        readStatus!!.currentPage = 1
        dataFactory!!.toChapterStart = true
        dataFactory!!.previous()
        if (Constants.isSlideUp) {
            pageView!!.getChapter(false)
        } else {
            pageView!!.drawCurrentPage()
            pageView!!.drawNextPage()
        }
        changeMarkState()

        if (!pageView!!.isAutoReadMode()) {
            Constants.manualReadedCount++
            dataFactory!!.dealManualDialogShow()
        }

        val data = HashMap<String, String>()
        if (readStatus != null) {
            data.put("bookid", readStatus!!.book_id)
        }
        if (dataFactory != null && dataFactory!!.currentChapter != null) {
            data.put("chapterid", dataFactory!!.currentChapter.chapter_id)
        }
        data.put("type", "1")
        StartLogClickUtil.upLoadEventLog(readReference?.get(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.CHAPTERTURN, data)

    }

    fun onJumpNextChapter() {
        readStatus!!.currentPage = readStatus!!.pageCount
        dataFactory!!.next()
        if (Constants.isSlideUp) {
            pageView!!.getChapter(false)
        } else {
            pageView!!.drawCurrentPage()
            pageView!!.drawNextPage()
        }
        changeMarkState()

        if (!pageView!!.isAutoReadMode()) {
            Constants.manualReadedCount++
            dataFactory!!.dealManualDialogShow()
        }

        val data = HashMap<String, String>()
        if (readStatus != null) {
            data.put("bookid", readStatus!!.book_id)
        }
        if (dataFactory != null && dataFactory!!.currentChapter != null) {
            data.put("chapterid", dataFactory!!.currentChapter.chapter_id)
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
            handler.postDelayed({ readReference?.get()?.showToastShort("已发送") }, 1000)
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
        handler.postDelayed({ readReference?.get()?.showToastShort("已发送") }, 1000)
    }

    private fun getEncode(content: String): String {
        if (TextUtils.isEmpty(content)) {
            return ""
        }
        try {
            return URLEncoder.encode(content, "utf-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        return ""
    }

    private fun changeMarkState() {
        mReadOptionPresenter?.updateStatus()
    }

    fun goBackToHome() {
        val act = readReference?.get()
        if (act == null) {
            return
        }
        if (!currentThemeMode.equals(act.mThemeHelper.getMode())) {
            val themIntent = Intent(readReference?.get(), HomeActivity::class.java)
            val bundle = Bundle()
            bundle.putInt(EventBookStore.BOOKSTORE, EventBookStore.TYPE_TO_SWITCH_THEME)
            themIntent.putExtras(bundle)
            act.startActivity(themIntent)
            act.overridePendingTransition(R.anim.activity_in, R.anim.activity_out)
            act.finish()
        } else {
            if (act.isTaskRoot()) {
                val intent = Intent(act, SplashActivity::class.java)
                act.startActivity(intent)
            }
            act.finish()
        }
    }

    override fun onOriginClick() {
        var url: String? = null
        if (dataFactory != null && dataFactory!!.currentChapter != null) {
            url = UrlUtils.buildContentUrl(dataFactory!!.currentChapter.curl)
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

    private class TimerRunnable internal constructor(pre: ReadPresenter) : Runnable {
        private val readReference: WeakReference<ReadPresenter>

        init {
            readReference = WeakReference(pre)
        }

        override fun run() {
            val readPre = readReference.get() ?: return
            if (readPre.mTimerStopped || readPre.pageView == null) {
                return
            }
            readPre.mCalendar?.timeInMillis = System.currentTimeMillis()
            try {
                if (readPre.pageView != null) {
                    readPre.time_text = DateFormat.format("k:mm", readPre.mCalendar)
                    readPre.pageView?.freshTime(readPre.time_text)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val now = SystemClock.uptimeMillis()
            val next = now + (30000 - now % 1000)
            readPre.handler.postAtTime(readPre.mTicker, next)
        }
    }

    inner class CacheUpdateReceiver(weak: WeakReference<ReadingActivity>) : BroadcastReceiver() {

        private val mActivityWeakReference: WeakReference<ReadingActivity>

        init {
            mActivityWeakReference = weak
        }

        override fun onReceive(context: Context, intent: Intent) {
            LogUtils.e("CacheUpdateReceiver", "onReceive")
            val book = intent.getSerializableExtra(Constants.REQUEST_ITEM) as Book ?: return

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

    internal class UiHandler(readPres: ReadPresenter) : Handler() {
        private val actReference: WeakReference<ReadPresenter>

        init {
            actReference = WeakReference(readPres)
        }

        override fun handleMessage(msg: android.os.Message) {
            val readPres = actReference.get() ?: return
            when (msg.what) {
                0 -> {
                }
                1 -> readPres.pauseAutoReadHandler()
                2 -> readPres.resumeAutoReadHandler()
                3 -> {
                }
                4 -> {
                }
                5 -> if (readPres.readStatus?.sequence != -1) {
                    readPres.openSourcePage()
                }
                RequestExecutor.REQUEST_BOOK_SOURCE_SUCCESS -> readPres.sourcesList = msg.obj as ArrayList<Source>
                RequestExecutor.REQUEST_BOOK_SOURCE_ERROR -> {
                }
                else -> {
                }
            }
        }
    }

    private inner class NovelDownloader : BaseAsyncTask<Int?, Void?, Void?>() {

        override fun onPostExecute(result: Void?) {
            if (isCancelled || readReference == null || readReference?.get() == null || readReference!!.get()!!.isFinishing())
                return
            super.onPostExecute(result)
        }

        protected override fun doInBackground(vararg params: Int?): Void? {
            if (dataFactory != null) {
                val chapterList = dataFactory?.chapterList?.clone() as ArrayList<Chapter> ?: return null
                val size = chapterList.size
                if (readStatus != null) {
                    var i = readStatus!!.sequence + 1
                    while (i < readStatus!!.sequence + params[0]!! + 1 && i < size) {
                        var c: Chapter? = chapterList[i] ?: return null
                        try {
                            AppLog.e(TAG, "预加载： " + c.toString())
                            c = requestFactory!!.requestExecutor(readStatus!!.getRequestItem()).requestSingleChapter(readStatus!!.book.dex, mBookDaoHelper, bookChapterDao, c)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        if (i == readStatus!!.sequence + 1) {
                            if (dataFactory != null) {
                                dataFactory!!.nextChapter = c
                            }
                        }
                        if (isCancelled) {
                            break
                        }
                        i++
                    }
                }
            }

            return null
        }
    }
}