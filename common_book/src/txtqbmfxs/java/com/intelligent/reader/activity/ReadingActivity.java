package com.intelligent.reader.activity;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.bumptech.glide.Glide;
import com.dingyueads.sdk.Bean.Advertisement;
import com.dingyueads.sdk.Bean.Novel;
import com.dingyueads.sdk.Native.YQNativeAdInfo;
import com.dingyueads.sdk.NativeInit;
import com.dingyueads.sdk.Utils.LogUtils;
import com.intelligent.reader.BuildConfig;
import com.intelligent.reader.R;
import com.intelligent.reader.app.BookApplication;
import com.intelligent.reader.fragment.CatalogMarkFragment;
import com.intelligent.reader.presenter.read.CatalogMarkPresenter;
import com.intelligent.reader.presenter.read.ReadOptionPresenter;
import com.intelligent.reader.read.animation.BitmapManager;
import com.intelligent.reader.read.help.BookHelper;
import com.intelligent.reader.read.help.CallBack;
import com.intelligent.reader.read.help.DrawTextHelper;
import com.intelligent.reader.read.help.IReadDataFactory;
import com.intelligent.reader.read.help.NovelHelper;
import com.intelligent.reader.read.help.ReadDataFactory;
import com.intelligent.reader.read.page.AutoReadMenu;
import com.intelligent.reader.read.page.PageInterface;
import com.intelligent.reader.read.page.PageView;
import com.intelligent.reader.read.page.ReadOptionHeader;
import com.intelligent.reader.read.page.ReadSettingView;
import com.intelligent.reader.read.page.ScrollPageView;
import com.intelligent.reader.receiver.DownBookClickReceiver;
import com.intelligent.reader.util.EventBookStore;

import net.lzbook.kit.ad.OwnNativeAdManager;
import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.component.service.DownloadService;
import net.lzbook.kit.book.view.LoadingPage;
import net.lzbook.kit.book.view.MyDialog;
import net.lzbook.kit.cache.imagecache.ImageCacheManager;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.ChapterErrorBean;
import net.lzbook.kit.data.bean.ReadStatus;
import net.lzbook.kit.data.bean.RequestItem;
import net.lzbook.kit.data.bean.Source;
import net.lzbook.kit.data.db.BookChapterDao;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.repair_books.RepairHelp;
import net.lzbook.kit.request.RequestExecutor;
import net.lzbook.kit.request.RequestFactory;
import net.lzbook.kit.request.UrlUtils;
import net.lzbook.kit.request.own.OtherRequestService;
import net.lzbook.kit.tasks.BaseAsyncTask;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.BaseBookHelper;
import net.lzbook.kit.utils.LoadDataManager;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.ResourceUtil;
import net.lzbook.kit.utils.SharedPreferencesUtils;
import net.lzbook.kit.utils.StatServiceUtils;
import net.lzbook.kit.utils.StatisticManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import iyouqu.theme.ThemeMode;

/**
 * ReadingActivity
 * 小说阅读页
 */
@SuppressLint("InlinedApi")
public class ReadingActivity extends BaseCacheableActivity implements OnClickListener, NovelHelper
        .OnHelperCallBack, CallBack, IReadDataFactory.ReadDataListener, AutoReadMenu.OnAutoMemuListener, ReadSettingView.OnReadSettingListener,
        DownloadService.OnDownloadListener, PageInterface.OnOperationClickListener {
    public static final int MSG_LOAD_CUR_CHAPTER = 0;
    public static final int MSG_LOAD_PRE_CHAPTER = 1;
    public static final int MSG_LOAD_NEXT_CHAPTER = 2;
    public static final int MSG_SEARCH_CHAPTER = 3;
    public static final int MSG_CHANGE_SOURCE = 4;
    public static final int MSG_JUMP_CHAPTER = 6;
    public static final int ERROR = 7;
    public static final int NEED_LOGIN = 8;
    public static final int MSG_SOURCE_CHANGE = 9;
    private static final String TAG = ReadingActivity.class.getSimpleName();
    // 时间
    private final static String mFormat = "k:mm";
    // 手动书签内容限制
    private static final int font_count = 50;
    private static ReadStatus readStatus;
    public DownloadService downloadService;
    public boolean isRestDialogShow = false;
    long stampTime = 0;
    int readLength = 0;
    private Context mContext;
    private PageInterface pageView;
    private ArrayList<Source> sourcesList;
    private boolean isSourceListShow;
    // 系统存储设置
    private SharedPreferences sp;
    private SharedPreferences modeSp;
    private boolean isSubed;
    private TimerRunnable mTicker;
    private Calendar mCalendar;
    private boolean mTimerStopped = false;
    private BookChapterDao bookChapterDao;
    private NovelDownloader mNovelLoader;
    private BookDaoHelper mBookDaoHelper;
    private boolean screen_moding = false;
    private boolean isFromCover = true;
    private NovelHelper myNovelHelper;
    private IReadDataFactory dataFactory;
    private int autoSpeed;
    private AutoReadMenu auto_menu;
    private LayoutInflater inflater;
    private int vipSort;
    private float batteryPercent;
    private ReadSettingView readSettingView;
    private View ll_guide_layout;
    private MyDialog mDialog;
    private boolean is_dot_orientation = false;// 横竖屏打点
    private int current_mode;
    private CharSequence time_text;
    private SharedPreferencesUtils sharedPreferencesUtils;
    private int versionCode;
    private RelativeLayout reading_content;
    private OwnNativeAdManager ownNativeAdManager;
    private boolean isAcvNovelActive = true;
    private Runnable rest_tips_runnable;
    private boolean isRestPress = false;
    private boolean actNovelRunForeground = true;
    private Handler handler = new UiHandler(this);
    /**
     * 接受按下电源键的广播
     */
    private final BroadcastReceiver mPowerOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                /**
                 * 接受在阅读页，监听按下电源键的广播处理
                 */
                if (isAcvNovelActive && handler != null && rest_tips_runnable != null) {
                    isAcvNovelActive = false;
                    handler.removeCallbacks(rest_tips_runnable);
                    rest_tips_runnable = null;
                }
            }
        }
    };
    private StatisticManager statisticManager;
    private boolean isSlideToAuto = false;
    private FrameLayout novel_basePageView;
    //    //转码声明
//    private TransCodingView novel_option_encode;
//    //原网页
//    private SourcePageView novel_option_source;
    private Resources resources;
    private int isFirstGuide = 0;
    private MyDialog myDialog;
    private RequestFactory requestFactory;
    private int type = -1;
    private String currentThemeMode;

    private int lastMode = -1;
    /**
     * 接受电量改变广播
     */
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                if (pageView != null) {
                    int level = intent.getIntExtra("level", 0);
                    int scale = intent.getIntExtra("scale", 100);
                    batteryPercent = (float) level / (float) scale;
                    pageView.freshBattery(batteryPercent);
                }
            }
        }
    };
    private CacheUpdateReceiver mCacheUpdateReceiver;
    private ReadOptionPresenter mReadOptionPresenter;
    private DrawerLayout mCatlogMarkDrawer;
    private CatalogMarkPresenter mCatalogMarkPresenter;

    private CatalogMarkFragment mCatalogMarkFragment;

    private DrawerLayout.DrawerListener mDrawerListener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(View drawerView) {
            //解锁， 可滑动关闭
            if (mCatlogMarkDrawer != null) {
                mCatlogMarkDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNDEFINED);
            }
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            //锁定不可滑出
            if (mCatlogMarkDrawer != null) {
                mCatlogMarkDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }
        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    };
    private ServiceConnection sc = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadService = ((DownloadService.MyBinder) service).getService();
            BaseBookApplication.setDownloadService(downloadService);
            downloadService.setOnDownloadListener(ReadingActivity.this);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppLog.e(TAG, "onCreate");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        getWindow().getDecorView().setSystemUiVisibility(UI_OPTIONS_IMMERSIVE_STICKY);

        mContext = this;
        this.sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Constants.isFullWindowRead = sp.getBoolean("read_fullwindow", true);
        Constants.PAGE_MODE = sp.getInt("page_mode", 0);
        Constants.FULL_SCREEN_READ = sp.getBoolean("full_screen_read", false);
        Constants.isSlideUp = (Constants.PAGE_MODE == 3);
        Constants.isVolumeTurnover = sp.getBoolean("sound_turnover", true);
        AppLog.e("getAdsStatus", "novel_onCreate");
        versionCode = AppUtils.getVersionCode();
        AppLog.e(TAG, "versionCode: " + versionCode);
        inflater = LayoutInflater.from(getApplicationContext());
        readStatus = new ReadStatus(getApplicationContext());
        (BookApplication.getGlobalContext()).setReadStatus(readStatus);
        autoSpeed = readStatus.autoReadSpeed();
        myNovelHelper = new NovelHelper(this, readStatus, handler);
        myNovelHelper.setOnHelperCallBack(this);
        downloadService = BaseBookApplication.getDownloadService();
        requestFactory = new RequestFactory();

        // 初始化窗口基本信息
        initWindow();

        dataFactory = new ReadDataFactory(getApplicationContext(), this, readStatus, myNovelHelper);
        dataFactory.setReadDataListener(this);

        setOrientation();
        getSavedState(savedInstanceState);

        RepairHelp.showFixMsg(this, readStatus.book, new RepairHelp.FixCallBack() {
            @Override
            public void toDownLoadActivity() {
                Intent intent_download = new Intent(ReadingActivity.this, DownloadManagerActivity.class);
                try {
                    ReadingActivity.this.startActivity(intent_download);
                    ReadingActivity.this.finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        if (isFromCover && Constants.IS_LANDSCAPE) {
            return;
        }


        View main = getLayoutInflater().inflate(R.layout.act_read, null);

        setContentView(main);

        mCatlogMarkDrawer = (DrawerLayout) findViewById(R.id.read_catalog_mark_drawer);

        mCatlogMarkDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mCatlogMarkDrawer.addDrawerListener(mDrawerListener);

        mCatalogMarkPresenter = new CatalogMarkPresenter(readStatus, dataFactory);

        mCatalogMarkFragment = (CatalogMarkFragment) getSupportFragmentManager().findFragmentById(R.id.read_catalog_mark_layout);
        mCatalogMarkPresenter.setView(mCatalogMarkFragment);
        mCatalogMarkFragment.setPresenter(mCatalogMarkPresenter);

//        mCatalogMarkPresenter.loadCatalog(false);

        mCatlogMarkDrawer.addDrawerListener(mCatalogMarkFragment);
        ReadOptionHeader optionHeader = (ReadOptionHeader) findViewById(R.id.option_header);
        mReadOptionPresenter = new ReadOptionPresenter(this, readStatus, dataFactory);
        mReadOptionPresenter.setView(optionHeader);
        optionHeader.setPresenter(mReadOptionPresenter);

        initBookState();
        // 初始化view
        initView();
        // 初始化监听器
        initListener();
        //	开启护眼计时器
        startRestTimer();
        //注册一个监听按下电源键的广播
        registerReceiver(mPowerOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        getBookContent();
        if (BaseBookApplication.getDownloadService() == null) {
            BookHelper.reStartDownloadService();
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.lastMode = -1;
        if (pageView != null) {
            pageView.clear();
        }
        showMenu(false);
        AppLog.d("ReadingActivity", "onNewIntent:");
        this.sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Constants.isFullWindowRead = sp.getBoolean("read_fullwindow", true);
        Constants.PAGE_MODE = sp.getInt("page_mode", 0);
        Constants.isSlideUp = (Constants.PAGE_MODE == 3);
        versionCode = AppUtils.getVersionCode();
        AppLog.e(TAG, "versionCode: " + versionCode);
        inflater = LayoutInflater.from(getApplicationContext());
        if(readStatus != null){
            readStatus.recycleResource();
            readStatus.recycleResourceNew();
        }
        readStatus = new ReadStatus(getApplicationContext());
        (BookApplication.getGlobalContext()).setReadStatus(readStatus);
        autoSpeed = readStatus.autoReadSpeed();
        myNovelHelper = new NovelHelper(this, readStatus, handler);
        myNovelHelper.setOnHelperCallBack(this);

        requestFactory = new RequestFactory();
        if (dataFactory != null) {
            dataFactory.clean();
        }
        dataFactory = new ReadDataFactory(getApplicationContext(), this, readStatus, myNovelHelper);
        dataFactory.setReadDataListener(this);
        // 初始化窗口基本信息
        initWindow();
        setOrientation();
        getSavedState(intent.getExtras());
        if (isFromCover && Constants.IS_LANDSCAPE) {
            return;
        }

//        setContentView(R.layout.act_read);
        mCatlogMarkDrawer = (DrawerLayout) findViewById(R.id.read_catalog_mark_drawer);
        if (mCatlogMarkDrawer == null) {
            //inflate not finish
            finish();
            return;
        }

        mCatlogMarkDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mCatlogMarkDrawer.addDrawerListener(mDrawerListener);

        mCatalogMarkPresenter = new CatalogMarkPresenter(readStatus, dataFactory);

        mCatalogMarkFragment = (CatalogMarkFragment) getSupportFragmentManager().findFragmentById(R.id.read_catalog_mark_layout);
        if (mCatalogMarkFragment == null) {
            //inflate not finish
            finish();
            return;
        }
        mCatalogMarkPresenter.setView(mCatalogMarkFragment);
        mCatalogMarkFragment.setPresenter(mCatalogMarkPresenter);

        mCatlogMarkDrawer.addDrawerListener(mCatalogMarkFragment);

        ReadOptionHeader optionHeader = (ReadOptionHeader) findViewById(R.id.option_header);
        if (optionHeader == null) {
            //inflate not finish
            finish();
            return;
        }
        mReadOptionPresenter = new ReadOptionPresenter(this, readStatus, dataFactory);
        mReadOptionPresenter.setView(optionHeader);
        optionHeader.setPresenter(mReadOptionPresenter);

        initBookState();
        // 初始化view
        initView();
        // 初始化监听器
        initListener();
        //	开启护眼计时器
        startRestTimer();
        //注册一个监听按下电源键的广播
        registerReceiver(mPowerOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        getBookContent();
        if (BaseBookApplication.getDownloadService() == null) {
            BookHelper.reStartDownloadService();
        }

        changeMode(Constants.MODE);
    }

    /**
     * 休息提醒计时器
     */
    private void startRestTimer() {
        final int read_rest_time = sp.getInt("read_rest_time", Constants.read_rest_time / 60000) * 60000;
        /**
         * 增加健壮性判断，当用户选择休息提示为：“永不   ”时，直接return，避免多出修改字段类型为long
         */
        //        if (read_rest_time == Integer.MAX_VALUE * 60000) {
        //            return;
        //        }

        rest_tips_runnable = new Runnable() {
            @Override
            public void run() {
                mDialog = new MyDialog(ReadingActivity.this, R.layout.reading_resttime, Gravity.CENTER, false);
                final ImageView iv_reset_ad = (ImageView) mDialog.findViewById(R.id.iv_reset_ad);
                final ImageView iv_reset_ad_logo = (ImageView) mDialog.findViewById(R.id.iv_reset_ad_logo);
                final ImageView iv_reset_ad_image = (ImageView) mDialog.findViewById(R.id.iv_reset_ad_image);
                LinearLayout ll_reset_layout = (LinearLayout) mDialog.findViewById(R.id.ll_reset_layout);
                ImageView iv_close = (ImageView) mDialog.findViewById(R.id.iv_close);

                iv_reset_ad_image.setVisibility(View.INVISIBLE);

                try {
                    if ("night".equals(ResourceUtil.mode)) {
                        ll_reset_layout.setAlpha(0.6f);
                    } else {
                        ll_reset_layout.setAlpha(1.0f);
                    }
                } catch (NoSuchMethodError e) {
                    e.printStackTrace();
                }

                if (ownNativeAdManager == null) {
                    ownNativeAdManager = OwnNativeAdManager.getInstance(ReadingActivity.this);
                }

                ownNativeAdManager.loadAd(NativeInit.CustomPositionName.REST_POSITION);

                final YQNativeAdInfo nativeADInfo = ownNativeAdManager.getSingleADInfo(NativeInit.CustomPositionName
                        .REST_POSITION);

                iv_close.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDialog.dismiss();

                        if (iv_reset_ad != null) {
                            BitmapDrawable bitmapDrawable = (BitmapDrawable) iv_reset_ad.getDrawable();
                            if (bitmapDrawable != null) {
                                AppLog.e(TAG, "BitmapDrawable != null");
                                Bitmap bitmap = bitmapDrawable.getBitmap();
                                if (bitmap != null && !bitmap.isRecycled()) {
                                    AppLog.e(TAG, "Bitmap != null");
//                                    bitmap.recycle();
                                }
                            }
                        }
                    }
                });

                if (nativeADInfo != null) {
                    final Advertisement advertisement = nativeADInfo.getAdvertisement();
                    if (advertisement != null) {
                        String image_url = advertisement.imageUrl;
                        if (!TextUtils.isEmpty(image_url)) {
                            ImageCacheManager.getInstance().getImageLoader().get(image_url, new ImageLoader
                                    .ImageListener() {
                                @Override
                                public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                                    if (imageContainer != null) {
                                        Bitmap bitmap_icon = imageContainer.getBitmap();
                                        if (bitmap_icon != null) {
                                            iv_reset_ad.setImageBitmap(bitmap_icon);

                                            if ("广点通".equals(advertisement.rationName)) {
                                                iv_reset_ad_logo.setImageResource(R.drawable.icon_ad_gdt);
                                            } else if ("百度".equals(advertisement.rationName)) {
                                                iv_reset_ad_logo.setImageResource(R.drawable.icon_ad_bd);
                                            } else if ("360".equals(advertisement.rationName)) {
                                                iv_reset_ad_logo.setImageResource(R.drawable.icon_ad_360);
                                            } else {
                                                iv_reset_ad_logo.setImageResource(R.drawable.icon_ad_default);
                                            }

                                            iv_reset_ad_image.setVisibility(View.VISIBLE);

                                            StatServiceUtils.statBookEventShow(ReadingActivity.this, StatServiceUtils.type_ad_reset_30);
                                        }
                                    }
                                }

                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                }
                            });
                        }
                        iv_reset_ad.setTag(nativeADInfo);
                        try {
                            if (statisticManager == null) {
                                statisticManager = StatisticManager.getStatisticManager();
                            }
                            Novel novel = dataFactory.transformation();
                            statisticManager.schedulingRequest(ReadingActivity.this, ll_reset_layout, nativeADInfo, novel, StatisticManager
                                    .TYPE_SHOW, NativeInit.ad_position[3]);
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                    }
                    iv_reset_ad.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (view.getTag() != null) {
                                YQNativeAdInfo yqNativeAdInfo = (YQNativeAdInfo) view.getTag();
                                if (yqNativeAdInfo != null) {
                                    try {
                                        if (statisticManager == null) {
                                            statisticManager = StatisticManager.getStatisticManager();
                                        }
                                        Novel novel = dataFactory.transformation();
                                        statisticManager.schedulingRequest(ReadingActivity.this, view, yqNativeAdInfo, novel,
                                                StatisticManager.TYPE_CLICK, NativeInit.ad_position[3]);
                                    } catch (IllegalArgumentException e) {
                                        e.printStackTrace();
                                    }
                                    StatServiceUtils.statBookEventClick(ReadingActivity.this, StatServiceUtils
                                            .type_ad_reset_30);
                                    if (Constants.DEVELOPER_MODE) {
                                        Toast.makeText(ReadingActivity.this, "你点击了广告", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    });
                }
                try {
                    if (nativeADInfo != null) {
                        mDialog.show();
                        isRestDialogShow = true;
                        mDialog.setOnDismissListener(new OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                //								isRestDialogShow = true;

                                if (statisticManager == null) {
                                    statisticManager = StatisticManager.getStatisticManager();
                                }
                                Novel novel = dataFactory.transformation();
                                statisticManager.schedulingRequest(ReadingActivity.this, iv_reset_ad, nativeADInfo, novel, StatisticManager
                                        .TYPE_END, NativeInit.ad_position[3]);

                                if (!isRestPress) {
                                    //									Log.e(TAG, "按下Back键了，屏幕变暗了！");
                                    //									handler.postDelayed(rest_tips_runnable,
                                    // read_rest_time);
                                    if (handler != null) {
                                        handler.removeCallbacks(rest_tips_runnable);
                                        startRestTimer();
                                    }
                                } else {
                                    /**当弹出休息提示对话框时候，用户点击休息一下按钮后，对话框消失，
                                     * 需要重置isRestPress按钮的默认值为false;
                                     * 防止点击休息一下后，在阅读非书架书籍时会弹出添加到书架的对话框，
                                     * 当点击屏幕空白处取消添加到书架对话框继续阅读后，再下次弹出的休息提醒对话框时，
                                     * 如果用户点击继续看后，休息提醒对话框消失，但是计时器不会重新启动的bug
                                     */
                                    isRestPress = false;
                                }
                                if (isRestDialogShow) {
                                    isRestDialogShow = false;
                                }
                            }
                        });
                    } else {
                        if (handler != null) {
                            handler.removeCallbacks(rest_tips_runnable);
                            handler.postDelayed(rest_tips_runnable, 60000);//获取广告null 重新获取
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        if (mDialog == null) {
            handler.postDelayed(rest_tips_runnable, read_rest_time);
        } else if (!mDialog.isShowing()) {
            handler.postDelayed(rest_tips_runnable, read_rest_time);
        }
    }

    /**
     * 处理书籍状态
     */
    private void initBookState() {
        // 判断是否订阅
        mBookDaoHelper = BookDaoHelper.getInstance();
        readStatus.book_id = readStatus.book.book_id;
        isSubed = mBookDaoHelper.isBookSubed(readStatus.book_id);
        AppLog.e(TAG, "初始化书籍状态: " + readStatus.book_id);
        bookChapterDao = new BookChapterDao(getApplicationContext(), readStatus.book_id);
        if (isSubed) {
            readStatus.book = mBookDaoHelper.getBook(readStatus.book_id, 0);
        }
        if (readStatus.sequence < -1) {
            readStatus.sequence = -1;
        } else if (isSubed && readStatus.sequence + 1 > readStatus.book.chapter_count) {
            readStatus.sequence = readStatus.book.chapter_count - 1;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        this.lastMode = -1;
        // 初始化窗口基本信息
        if (pageView != null) {
            pageView.clear();
        }
        initWindow();
        if (mCatlogMarkDrawer == null) {
            setContentView(R.layout.act_read);
        }
        AppLog.e(TAG, "onConfigurationChanged");
        mCatlogMarkDrawer = (DrawerLayout) findViewById(R.id.read_catalog_mark_drawer);

        mCatlogMarkDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mCatlogMarkDrawer.addDrawerListener(mDrawerListener);

        mCatalogMarkPresenter = new CatalogMarkPresenter(readStatus, dataFactory);

        mCatalogMarkFragment = (CatalogMarkFragment) getSupportFragmentManager().findFragmentById(R.id.read_catalog_mark_layout);
        mCatalogMarkPresenter.setView(mCatalogMarkFragment);
        mCatalogMarkFragment.setPresenter(mCatalogMarkPresenter);

        mCatlogMarkDrawer.addDrawerListener(mCatalogMarkFragment);

        ReadOptionHeader optionHeader = (ReadOptionHeader) findViewById(R.id.option_header);
        mReadOptionPresenter = new ReadOptionPresenter(this, readStatus, dataFactory);
        mReadOptionPresenter.setView(optionHeader);
        optionHeader.setPresenter(mReadOptionPresenter);
        initBookState();
        // 初始化view
        initView();
        // 初始化监听器
        initListener();
        getBookContent();
        if (BaseBookApplication.getDownloadService() == null) {
            BookHelper.reStartDownloadService();
        }
        setMode();
        readStatus.chapterCount = readStatus.book.chapter_count;
        // 注册一个接受广播类型
        registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        super.onConfigurationChanged(newConfig);
        if (pageView != null) {
            pageView.freshBattery(batteryPercent);
        }

        changeMode(Constants.MODE);
    }

    private void getSavedState(Bundle savedInstanceState) {
        Bundle bundle = getIntent().getExtras();
        if (savedInstanceState != null) {
            // 从保存状态中获取
            // 章节序
            readStatus.sequence = savedInstanceState.getInt("sequence", 0);
            // 请求类
            RequestItem requestItem = (RequestItem) savedInstanceState.getSerializable(Constants.REQUEST_ITEM);
            if (requestItem != null) {
                readStatus.setRequestItem(requestItem);
            }
            // 书签偏移量
            readStatus.offset = savedInstanceState.getInt("offset", 0);
            // 获取本书
            readStatus.book = (Book) savedInstanceState.getSerializable("book");
            // 获取当前章
            if (dataFactory == null) {
                dataFactory = new ReadDataFactory(getApplicationContext(), this, readStatus, myNovelHelper);
                dataFactory.setReadDataListener(this);
            }
            dataFactory.currentChapter = (Chapter) savedInstanceState.getSerializable("currentChapter");
            currentThemeMode = savedInstanceState.getString("thememode", mThemeHelper.getMode());
            AppLog.e(TAG, "getState1" + readStatus.sequence);
        } else {
            // 从bundle中获取
            readStatus.sequence = bundle.getInt("sequence", 0);
            RequestItem requestItem = (RequestItem) bundle.getSerializable(Constants.REQUEST_ITEM);
            if (requestItem != null) {
                readStatus.setRequestItem(requestItem);
            }
            readStatus.offset = bundle.getInt("offset", 0);
            readStatus.book = (Book) bundle.getSerializable("book");
            readStatus.book_id = (readStatus.book == null ? "" : readStatus.book.book_id);
            currentThemeMode = bundle.getString("thememode", mThemeHelper.getMode());
            AppLog.e(TAG, "getState2" + readStatus.sequence);
        }

        if (readStatus.sequence == -2) {
            readStatus.sequence = -1;
        }
    }

    /*
     * 设置屏幕方向 port land
     */
    private void setOrientation() {
        if (!screen_moding) {
            if (sp.getInt("screen_mode", 3) == Configuration.ORIENTATION_PORTRAIT) {
                if (!is_dot_orientation) {
                    is_dot_orientation = true;
                }
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                Constants.IS_LANDSCAPE = false;
            } else if (sp.getInt("screen_mode", 3) == Configuration.ORIENTATION_LANDSCAPE && this.getResources()
                    .getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                if (!is_dot_orientation) {
                    is_dot_orientation = true;
                }
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                Constants.IS_LANDSCAPE = true;
            } else {
                if (!is_dot_orientation) {
                    is_dot_orientation = true;
                }
            }
        }
    }

    /**
     * 获取书籍内容
     */
    private void getBookContent() {

        NetWorkUtils.NATIVE_AD_TYPE = NetWorkUtils.NATIVE_AD_ERROR;
        dataFactory.getChapterByLoading(ReadingActivity.MSG_LOAD_CUR_CHAPTER, readStatus.sequence);

    }

    @Override
    public void showChangeNetDialog() {
        StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.read_limit);
        if (!isFinishing()) {
            myDialog = new MyDialog(this, R.layout.nonet_read_dialog);
            myDialog.setCanceledOnTouchOutside(false);
            ImageButton nonet_read_bookshelf = (ImageButton) myDialog.findViewById(R.id.nonet_read_backtoshelf);
            ImageButton nonet_read_continue = (ImageButton) myDialog.findViewById(R.id.nonet_read_continue);

            nonet_read_bookshelf.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.rb_click_change_source_read);
                    Intent shelfIntent = new Intent();
                    shelfIntent.setClass(ReadingActivity.this, HomeActivity.class);
                    try {
                        StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.read_limit_bookshelf);
                        Bundle bundle = new Bundle();
                        bundle.putInt(EventBookStore.BOOKSTORE, EventBookStore.TYPE_TO_BOOKSHELF);
                        shelfIntent.putExtras(bundle);
                        startActivity(shelfIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    finish();
                }
            });
            nonet_read_continue.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.rb_click_change_source_ok);
                    if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
                        StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.read_limit_continue);
                        intoSystemSetting();
                    } else {
                        myDialog.dismiss();
                    }

                }
            });
            myDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        finish();

                    }
                    return false;
                }
            });
            if (!myDialog.isShowing()) {
                try {
                    myDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void intoSystemSetting() {
        startActivity(new Intent(Settings.ACTION_SETTINGS));
    }

    /**
     * 初始化窗口基本信息
     */
    private void initWindow() {

        Display display = getWindowManager().getDefaultDisplay();
        Point realSize = new Point();
        display.getRealSize(realSize);

        // 获取屏幕基本信息
        DisplayMetrics dm = getResources().getDisplayMetrics();
        readStatus.screenWidth = realSize.x;
        readStatus.screenHeight = realSize.y;
        readStatus.screenDensity = dm.density;
        readStatus.screenScaledDensity = dm.scaledDensity;
        // 保存字体、亮度、阅读模式
        modeSp = getSharedPreferences("config", MODE_PRIVATE);
        // 设置字体
        if (sp.contains("novel_font_size")) {
            Constants.FONT_SIZE = sp.getInt("novel_font_size", 18);
        } else {
            Constants.FONT_SIZE = 18;
        }
    }

    /**
     * 初始化view
     */
    private void initView() {
        resources = getResources();
        reading_content = (RelativeLayout) findViewById(R.id.reading_content);
        readSettingView = (ReadSettingView) findViewById(R.id.readSettingView);
        readSettingView.setOnReadSettingListener(this);
        int novel_top_margin;
        novel_basePageView = (FrameLayout) findViewById(R.id.novel_basePageView);
        readStatus.novel_basePageView = novel_basePageView;
        if (Constants.isSlideUp) {
            novel_top_margin = getResources().getDimensionPixelOffset(R.dimen.dimen_margin_20);
            pageView = new ScrollPageView(getApplicationContext());
        } else {
            novel_top_margin = getResources().getDimensionPixelOffset(R.dimen.dimen_margin_20);
            pageView = new PageView(getApplicationContext());
        }
        novel_basePageView.removeAllViews();
        novel_basePageView.addView((View) pageView, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        pageView.setReadFactory(dataFactory);
        pageView.init(this, readStatus, myNovelHelper);
        pageView.setCallBack(this);
        pageView.setOnOperationClickListener(this);
        dataFactory.setPageView(pageView);
        myNovelHelper.setPageView(pageView);
        readSettingView.setDataFactory(dataFactory, readStatus, mThemeHelper);
        readSettingView.setCurrentThemeMode(currentThemeMode);
        auto_menu = (AutoReadMenu) findViewById(R.id.auto_menu);
        auto_menu.setOnAutoMemuListener(this);

        ll_guide_layout = findViewById(R.id.ll_guide_layout);
        initGuide();
//        initReadingAd();

        readSettingView.setNovelMode(Constants.MODE);
        readStatus.source_ids = readStatus.book.site;
    }

    private void initGuide() {
        sharedPreferencesUtils = new SharedPreferencesUtils(PreferenceManager.getDefaultSharedPreferences
                (getApplicationContext()));
        if (!sharedPreferencesUtils.getBoolean(versionCode + Constants.READING_GUIDE_TAG)) {
            final ImageView iv_guide_reading = (ImageView) findViewById(R.id.iv_guide_reading);
            ll_guide_layout.setVisibility(View.VISIBLE);
            iv_guide_reading.setVisibility(View.VISIBLE);

            ll_guide_layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    sharedPreferencesUtils.putBoolean(versionCode + Constants.READING_GUIDE_TAG, true);
                    iv_guide_reading.setVisibility(View.GONE);
                    ll_guide_layout.setVisibility(View.GONE);
                }
            });
        }
    }

    /**
     * 首次进入阅读页面 展示广告小图
     */
    private void initReadingAd() {
        if (ownNativeAdManager == null) {
            ownNativeAdManager = OwnNativeAdManager.getInstance(this);
        }
        ownNativeAdManager.setActivity(this);
        if (!Constants.isSlideUp) {
        ownNativeAdManager.loadAdForMiddle(NativeInit.CustomPositionName.READING_MIDDLE_POSITION);
            if (Constants.IS_LANDSCAPE) {
                OwnNativeAdManager.getInstance(this).loadAd(NativeInit.CustomPositionName.SUPPLY_READING_SPACE);
            } else {
                OwnNativeAdManager.getInstance(this).loadAd(NativeInit.CustomPositionName.READING_POSITION);
            }
        }
        if (Constants.isSlideUp && Constants.dy_ad_readPage_slide_switch_new) {
            if (Constants.IS_LANDSCAPE) {
                OwnNativeAdManager.getInstance(this).loadAd(NativeInit.CustomPositionName.LANDSCAPE_SLIDEUP_POPUPAD);
            } else {
                OwnNativeAdManager.getInstance(this).loadAd(NativeInit.CustomPositionName.SLIDEUP_POPUPAD_POSITION);
                OwnNativeAdManager.getInstance(this).loadAd(NativeInit.CustomPositionName.LANDSCAPE_SLIDEUP_POPUPAD);
            }
        }
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        if (downloadService == null) {
            reStartDownloadService(this);
            downloadService = BaseBookApplication.getDownloadService();
        } else {
            downloadService.setOnDownloadListener(this);
        }
        if (downloadService != null)
            downloadService.setOnDownloadListener(this);

//        //转码声明
//        novel_option_encode.setOnClickListener(this);
//        //原网页
//        novel_option_source.setOnClickListener(this);
    }

    private void reStartDownloadService(Activity context) {
        Intent intent = new Intent();
        intent.setClass(context, DownloadService.class);
        context.startService(intent);
        context.bindService(intent, sc, BIND_AUTO_CREATE);
    }

    /**
     * 初始化时间显示
     */
    private void initTime() {
        mTimerStopped = false;
        if (mCalendar == null) {
            mCalendar = Calendar.getInstance();
        }
        mTicker = new TimerRunnable(this);
        mTicker.run();
    }

    /**
     * 刷新页面
     */
    private void refreshPage() {
        //readStatus.isCanDrawFootView = (readStatus.sequence != -1);
//        novel_option_encode.setVisibility(View.VISIBLE);
//        novel_option_source.setVisibility(View.VISIBLE);
        if (readStatus.sequence == -1) {
//            novel_option_encode.setVisibility(View.GONE);
//            novel_option_source.setVisibility(View.GONE);
            readStatus.isCanDrawFootView = false;
        } else {
            readStatus.isCanDrawFootView = true;
        }
    }

    /**
     * 预加载
     */
    private void downloadNovel() {
        if (mNovelLoader != null && mNovelLoader.getStatus() == BaseAsyncTask.Status.RUNNING) {
            mNovelLoader.cancel(true);
        }

        if (mBookDaoHelper.isBookSubed(readStatus.book_id)) {
            int num = BookHelper.CHAPTER_CACHE_COUNT;
            int max = (readStatus.chapterCount - 1) - readStatus.sequence;
            if (max > 0) {
                if (max < num) {
                    num = max;
                }
                mNovelLoader = new NovelDownloader();
                mNovelLoader.execute2(num);
            }
        }

    }

    public void searchChapterCallBack(ArrayList<Source> sourcesList) {
        if (myNovelHelper != null && dataFactory != null && dataFactory.currentChapter != null && !TextUtils.isEmpty(dataFactory.currentChapter
                .curl) && sourcesList != null) {
            //if (readStatus.book.dex == 1 && !TextUtils.isEmpty(dataFactory.currentChapter.curl) && sourcesList != null) {
            myNovelHelper.showSourceDialog(dataFactory, dataFactory.currentChapter.curl, sourcesList);
            /*} else if (readStatus.book.dex == 0 && !TextUtils.isEmpty(dataFactory.currentChapter.curl1) && sourcesList != null) {
                myNovelHelper.showSourceDialog(dataFactory, dataFactory.currentChapter.curl1, sourcesList);*/
            //}
        } else {
            showToastShort("暂无其它来源");
        }
    }

    /**
     * 打开目录页面
     */
    private void openCategoryPage() {
        if (readStatus.isMenuShow) {
            showMenu(false);
        }
        if (mNovelLoader != null && mNovelLoader.getStatus() == BaseAsyncTask.Status.RUNNING) {
            mNovelLoader.cancel(true);
        }
        if (readStatus.book.book_type == 0) {
            Intent intent = new Intent(ReadingActivity.this, CataloguesActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("cover", readStatus.book);
            bundle.putString("book_id", readStatus.book_id);
            AppLog.e(TAG, "OpenCategoryPage: " + readStatus.sequence);
            bundle.putInt("sequence", readStatus.sequence);
            bundle.putBoolean("fromCover", false);
            AppLog.e(TAG, "ReadingActivity: " + readStatus.getRequestItem().toString());
            bundle.putSerializable(Constants.REQUEST_ITEM, readStatus.getRequestItem());
            intent.putExtras(bundle);
            ReadingActivity.this.startActivityForResult(intent, 1);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.novel_read_back:
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.rb_click_back_btn);
                Map<String, String> data2 = new HashMap<>();
                data2.put("type", "1");
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.BACK, data2);
                goBackToHome();
                break;

            case R.id.novel_source_url:
                String url = null;
                if (dataFactory != null && dataFactory.currentChapter != null) {
                    url = UrlUtils.buildContentUrl(dataFactory.currentChapter.curl);
                }
                if (!TextUtils.isEmpty(url)) {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    Map<String, String> data = new HashMap<>();
                    if (readStatus != null) {
                        data.put("bookid", readStatus.book_id);
                    }
                    StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.ORIGINALLINK, data);
                } else {
                    Toast.makeText(this, "无法查看原文链接", Toast.LENGTH_SHORT).show();
                }

                break;
            default:
                break;
        }
    }

    @Override
    public void showDisclaimerActivity() {
        Intent intent = new Intent(this, DisclaimerActivity.class);
        try {
            intent.putExtra("isFromReadingPage", true);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showCatalogActivity(Source source) {
        if (readStatus != null && readStatus.getRequestItem() != null) {
            if (source != null && !TextUtils.isEmpty(source.book_source_id) && readStatus != null && readStatus.book
                    != null) {
                if (mBookDaoHelper != null && mBookDaoHelper.isBookSubed(readStatus.book.book_id)) {
                    Book iBook = mBookDaoHelper.getBook(readStatus.book.book_id, 0);
                    if (!source.book_source_id.equals(iBook.book_source_id)) {
                        //弹出切源提示
                        showChangeSourceNoticeDialog(source);
                        return;
                    }
                }
            }
            intoCatalogActivity(source, false);
        }
    }

    private void showChangeSourceNoticeDialog(final Source source) {
        if (!isFinishing()) {
            myDialog = new MyDialog(this, R.layout.publish_hint_dialog);
            myDialog.setCanceledOnTouchOutside(true);
            Button dialog_cancel = (Button) myDialog.findViewById(R.id.publish_stay);
            dialog_cancel.setText(R.string.book_cover_continue_read_cache);
            Button dialog_confirm = (Button) myDialog.findViewById(R.id.publish_leave);
            dialog_confirm.setText(R.string.book_cover_confirm_change_source);
            TextView dialog_information = (TextView) myDialog.findViewById(R.id.publish_content);
            dialog_information.setText(R.string.book_cover_change_source_prompt);
            dialog_cancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.rb_click_change_source_read);
                    Map<String, String> map1 = new HashMap<String, String>();
                    map1.put("type", "2");
                    StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.READPAGEMORE_PAGE, StartLogClickUtil.READ_SOURCECHANGECONFIRM, map1);

                    dismissDialog();
                }
            });
            dialog_confirm.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.rb_click_change_source_ok);
                    Map<String, String> map2 = new HashMap<String, String>();
                    map2.put("type", "1");
                    StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.READPAGEMORE_PAGE, StartLogClickUtil.READ_SOURCECHANGECONFIRM, map2);

//                    DownloadService.clearTask(readStatus.book_id);
//                    BaseBookHelper.removeChapterCacheFile(readStatus.book_id);
//                    BaseBookHelper.delDownIndex(ReadingActivity.this, readStatus.book_id);
                    dismissDialog();
                    intoCatalogActivity(source, true);
                }
            });

            myDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    myDialog.dismiss();
                }
            });
            if (!myDialog.isShowing()) {
                try {
                    myDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void intoCatalogActivity(Source source, boolean b) {
        if (readStatus != null && readStatus.getRequestItem() != null) {
            readStatus.firstChapterCurl = "";
            dataFactory.currentChapter = null;

            RequestItem requestItem = new RequestItem();
            requestItem.book_id = source.book_id;
            requestItem.book_source_id = source.book_source_id;
            requestItem.host = source.host;
            requestItem.name = readStatus.bookName;
            requestItem.author = readStatus.bookAuthor;
            requestItem.dex = source.dex;

            Iterator<LinkedHashMap.Entry<String, String>> iterator = source.source.entrySet().iterator();
            ArrayList<String> list = new ArrayList<>();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                String value = entry.getValue();
                list.add(value);
            }
            if (list.size() > 0) {
                requestItem.parameter = list.get(0);
            }
            if (list.size() > 1) {
                requestItem.extra_parameter = list.get(1);
            }


            readStatus.setRequestItem(requestItem);
            //readStatus.requestConfig = BookApplication.getGlobalContext().getSourceConfig(requestItem.host);


            BookDaoHelper bookDaoHelper = BookDaoHelper.getInstance();
            if (bookDaoHelper.isBookSubed(source.book_id)) {
                Book iBook = bookDaoHelper.getBook(source.book_id, 0);
                iBook.book_source_id = requestItem.book_source_id;
                iBook.site = requestItem.host;
                iBook.parameter = requestItem.parameter;
                iBook.extra_parameter = requestItem.extra_parameter;
                iBook.last_updatetime_native = source.update_time;
                iBook.dex = source.dex;
                bookDaoHelper.updateBook(iBook);
                readStatus.book = iBook;
                if (b) {
                    BookChapterDao bookChapterDao = new BookChapterDao(ReadingActivity.this, source.book_id);
                    BookHelper.deleteAllChapterCache(source.book_id, 0, bookChapterDao.getCount());
                    DownloadService.clearTask(source.book_id);
                    BaseBookHelper.delDownIndex(this, source.book_id);
                    bookChapterDao.deleteBookChapters(0);

                }
            } else {
                Book iBook = readStatus.book;
                iBook.book_source_id = source.book_source_id;
                iBook.site = source.host;
                iBook.dex = source.dex;
                iBook.parameter = requestItem.parameter;
                iBook.extra_parameter = requestItem.extra_parameter;
                readStatus.book = iBook;
            }
            dataFactory.chapterList.clear();
            openCategoryPage();
        }
    }

    private void dismissDialog() {
        if (myDialog != null && myDialog.isShowing()) {
            myDialog.dismiss();
        }
    }

    @Override
    public void deleteBook() {
        if (mBookDaoHelper.isBookSubed(readStatus.book_id)) {
            mBookDaoHelper.deleteBook(readStatus.book_id);
        }
        finish();
    }

    @Override
    public void openAutoReading(boolean open) {
        onReadAuto();
    }

    @Override
    public void addBookShelf(boolean isAddShelf) {
        if (isAddShelf && mBookDaoHelper != null && readStatus.book != null) {
            readStatus.book.sequence = readStatus.sequence;
            readStatus.book.offset = readStatus.offset;
            readStatus.book.sequence_time = System.currentTimeMillis();
            readStatus.book.last_updateSucessTime = System.currentTimeMillis();
            readStatus.book.readed = 1;
            if (dataFactory != null) {
                if (dataFactory.chapterList != null && dataFactory.chapterList.size() > 0) {
                    Chapter chapter = dataFactory.chapterList.get(dataFactory.chapterList.size() - 1);
                    readStatus.book.extra_parameter = chapter.extra_parameter;
                }
                bookChapterDao.insertBookChapter(dataFactory.chapterList);
            }
            Boolean succeed = mBookDaoHelper.insertBook(readStatus.book);
            Toast.makeText(ReadingActivity.this, succeed ? R.string.reading_add_succeed : R.string.reading_add_fail,
                    Toast.LENGTH_SHORT).show();
        }
        Map<String, String> map1 = new HashMap<String, String>();
        if(readStatus.book != null){
            map1.put("bookid", readStatus.book.book_id);
        }
        if(dataFactory != null && dataFactory.currentChapter != null){
            map1.put("chapterid", dataFactory.currentChapter.chapter_id);
        }
        if(isAddShelf ){
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.POPUPSHELFADD, map1);
        }else{
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.POPUPSHELFADDCANCLE, map1);
        }

        goBackToHome();
    }

    /**
     * 打开切源页面
     */
    private void openSourcePage() {
        if (readStatus.sequence == -1) {
            Toast.makeText(this, R.string.read_changesource_tip, Toast.LENGTH_SHORT).show();
            return;
        }
        if (Constants.QG_SOURCE.equals(readStatus.requestItem.host)) {
            Toast.makeText(this, "该小说暂无其他来源！", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isSourceListShow) {
            isSourceListShow = false;
        } else {
            if (Constants.QG_SOURCE.equals(readStatus.getRequestItem().host) || Constants.QG_SOURCE.equals(readStatus.getRequestItem().host)) {
                return;
            }
            showMenu(false);
            LoadingPage loadingPage = dataFactory.getCustomLoadingPage();
            loadingPage.loading(new Callable<Void>() {
                @Override
                public Void call() throws Exception {

                    OtherRequestService.requestBookSourceChange(dataFactory.mHandler, ReadingActivity.MSG_SEARCH_CHAPTER, -144, readStatus.book_id);
                    return null;
                }
            });
            dataFactory.loadingError(loadingPage);
        }
    }

    private LoadingPage getCustomLoadingPage() {
        LoadingPage loadingPage = new LoadingPage(this, LoadingPage.setting_result);
        loadingPage.setCustomBackgroud();
        return loadingPage;
    }

    /**
     * 开始下载
     */
    private void startDownLoad() {
        if (!isSubed) {
            boolean succeed = mBookDaoHelper.insertBook(readStatus.book);
            if (succeed) {
                isSubed = true;
            } else {
                return;
            }
        }
        if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
            showToastShort("网络不给力，请稍后再试");
            return;
        }
        myNovelHelper.clickDownload(this, (Book) readStatus.book, Math.max(readStatus.sequence, 0));
    }

    /**
     * 横屏切换
     */
    private void changeScreenMode() {
        showMenu(false);
        screen_moding = true;
        Editor screen_mode = sp.edit();
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.rb_click_portrait_btn);
            Map<String, String> data = new HashMap<>();
            data.put("type", "2");
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.HPMODEL, data);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            screen_mode.putInt("screen_mode", Configuration.ORIENTATION_PORTRAIT);
            Constants.IS_LANDSCAPE = false;
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.rb_click_landscape_btn);
            Map<String, String> data = new HashMap<>();
            data.put("type", "1");
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.HPMODEL, data);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            isFromCover = false;
            screen_mode.putInt("screen_mode", Configuration.ORIENTATION_LANDSCAPE);
            Constants.IS_LANDSCAPE = true;
        }
        screen_mode.apply();
    }

    @Override
    public void restoreBrightness() {
        super.restoreBrightness();
    }

    @Override
    public void setReaderDisplayBrightness() {
        super.setReaderDisplayBrightness();
    }

    public void changeSourceCallBack() {

        if (pageView == null) {
            return;
        }
        readStatus.currentPage = 1;
        readStatus.offset = 0;
        myNovelHelper.isShown = false;
        myNovelHelper.getChapterContent(this, dataFactory.currentChapter, readStatus.book,
                false);
        refreshPage();
        isSourceListShow = false;
        if (Constants.isSlideUp) {
            pageView.getChapter(false);
        } else {
            pageView.drawCurrentPage();
            pageView.drawNextPage();
        }
        downloadNovel();
    }

    /**
     * 跳章
     */
    public void jumpChapterCallBack() {
        if (dataFactory == null || readStatus == null || myNovelHelper == null) {
            return;
        }
        dataFactory.nextChapter = null;
        readStatus.sequence = readStatus.novel_progress;
        readStatus.offset = 0;
        myNovelHelper.isShown = false;
        myNovelHelper.getChapterContent(this, dataFactory.currentChapter, readStatus.book,
                false);
        readStatus.currentPage = 1;
        refreshPage();
        if (pageView == null) {
            return;
        }
        if (Constants.isSlideUp) {
            pageView.getChapter(false);
        } else {
            pageView.drawCurrentPage();
            pageView.drawNextPage();
        }
        downloadNovel();
    }

    /**
     * 清空屏幕
     */
    public void clearOtherPanel() {
        isSourceListShow = false;
    }

    /**
     * 隐藏topmenu
     */
    public void dismissTopMenu() {
        if (mReadOptionPresenter != null)
            mReadOptionPresenter.getView().show(false);
        full(true);
    }

    /*
     * 显示隐藏菜单
     */
    public void showMenu(boolean isShow) {
        if (readSettingView == null || pageView == null) {
            return;
        }
        if (pageView.isAutoReadMode() && isShow) {
            return;
        }
        clearOtherPanel();
        if (isShow) {
            full(false);
            changeMarkState();
            mReadOptionPresenter.getView().show(true);
            readSettingView.showSetMenu(true);
            readStatus.isMenuShow = true;
            initSettingGuide();
        } else {
            full(true);
            readStatus.isMenuShow = false;
            mReadOptionPresenter.getView().show(false);
            readSettingView.showSetMenu(false);
            readStatus.isMenuShow = false;
        }
    }

    // 全屏切换
    private void full(boolean enable) {
        if (!Constants.isFullWindowRead) {
            return;
        }
        if (enable) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
    }

    private void initSettingGuide() {
        if (sharedPreferencesUtils != null && !sharedPreferencesUtils.getBoolean(versionCode + Constants
                .READING_SETING_GUIDE_TAG)) {

//            ll_guide_layout.setBackgroundColor(getResources().getColor(R.color.color_black_c4000000));
            ll_guide_layout.setVisibility(View.VISIBLE);


            final ImageView iv_guide_setting_bookmark = (ImageView) findViewById(R.id.iv_guide_setting_bookmark);

            iv_guide_setting_bookmark.setVisibility(View.VISIBLE);

            ll_guide_layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

//                    if (isFirstGuide == 0) {
//
//                        if (iv_guide_setting_bookmark != null && View.GONE == iv_guide_setting_bookmark.getVisibility()) {
//                            iv_guide_setting_bookmark.setVisibility(View.VISIBLE);
//                        }
//                    }else{
                    sharedPreferencesUtils.putBoolean(versionCode + Constants.READING_SETING_GUIDE_TAG, true);
                    ll_guide_layout.setVisibility(View.GONE);
//                    }
                    isFirstGuide++;
                }
            });
        }
    }

    /**
     * mode 设定文件
     * void 返回类型
     * 切换夜间模式
     */
    private void changeMode(int mode) {
        if (this.lastMode == -1) {
            this.lastMode = mode;
        } else {
            if (this.lastMode == mode) {
                return;
            }else{
                this.lastMode = mode;
            }
        }

        this.current_mode = mode;
        AppLog.e(TAG, "ChangeMode : " + mode);
        Editor editor = modeSp.edit();
        if (mode == 61) {
            if ("light".equals(ResourceUtil.mode)) {
                editor.putString("mode", "night");
                ResourceUtil.mode = "night";
                editor.apply();
                setMode();
            }
        } else {
            if ("night".equals(ResourceUtil.mode)) {
                editor.putString("mode", "light");
                ResourceUtil.mode = "light";
                editor.apply();
                setMode();
            }
        }
        AppLog.e(TAG, "mode : " + mode);
        switch (mode) {
            case 51:
                setTextColor(getResources().getColor(R.color.reading_text_color_first));
                setPageBackColor(getResources().getColor(R.color.reading_backdrop_first));

//                novel_option_encode.setColor(getResources().getColor(R.color.reading_text_color_first));
//                novel_option_source.setColor(getResources().getColor(R.color.reading_text_color_first));

                setBackground();
                setBatteryBackground(R.drawable.reading_batty_day);
                break;
            case 52:
                setTextColor(getResources().getColor(R.color.reading_text_color_second));
                setPageBackColor(getResources().getColor(R.color.reading_backdrop_second));

//                novel_option_encode.setColor(getResources().getColor(R.color.reading_text_color_second));
//                novel_option_source.setColor(getResources().getColor(R.color.reading_text_color_second));

                setBackground();
                setBatteryBackground(R.drawable.reading_batty_eye);
                break;
            case 53:
                setTextColor(getResources().getColor(R.color.reading_text_color_third));
                setPageBackColor(getResources().getColor(R.color.reading_backdrop_third));

//                novel_option_encode.setColor(getResources().getColor(R.color.reading_text_color_third));
//                novel_option_source.setColor(getResources().getColor(R.color.reading_text_color_third));

                setBackground();
                setBatteryBackground(R.drawable.reading_batty_4);
                break;
            case 54:
                setTextColor(getResources().getColor(R.color.reading_text_color_fourth));
                setPageBackColor(getResources().getColor(R.color.reading_backdrop_fourth));

//                novel_option_encode.setColor(getResources().getColor(R.color.reading_text_color_fourth));
//                novel_option_source.setColor(getResources().getColor(R.color.reading_text_color_fourth));

                setBackground();
                setBatteryBackground(R.drawable.reading_batty_5);
                break;
            case 55:
                setTextColor(getResources().getColor(R.color.reading_text_color_fifth));
                setPageBackColor(getResources().getColor(R.color.reading_backdrop_fifth));

                setBatteryBackground(R.drawable.reading_batty_night);

//                novel_option_encode.setColor(getResources().getColor(R.color.reading_text_color_fifth));
//                novel_option_source.setColor(getResources().getColor(R.color.reading_text_color_fifth));

                setBackground();
                /*int screenBrightness = sp.getInt("screen_bright", -1);
                if (screenBrightness < 10) {
                    setScreenBrightness(this, 40);
                    readSettingView.setBrightProgressBar(40);
                }*/
                break;
            case 56:
                setTextColor(getResources().getColor(R.color.reading_text_color_sixth));
                setPageBackColor(getResources().getColor(R.color.reading_backdrop_sixth));

//                novel_option_encode.setColor(getResources().getColor(R.color.reading_text_color_sixth));
//                novel_option_source.setColor(getResources().getColor(R.color.reading_text_color_sixth));

                setBackground();
                setBatteryBackground(R.drawable.reading_batty_night2);
                break;
            case 61:
                setTextColor(getResources().getColor(R.color.reading_text_color_night));
                setPageBackColor(getResources().getColor(R.color.reading_backdrop_night));

//                novel_option_encode.setColor(getResources().getColor(R.color.reading_text_color_night));
//                novel_option_source.setColor(getResources().getColor(R.color.reading_text_color_night));

                setBackground();
                setBatteryBackground(R.drawable.reading_batty_night2);
                break;
            default:
                setTextColor(getResources().getColor(R.color.reading_text_color_first));
                setPageBackColor(Color.parseColor("#C2B282"));

//                novel_option_encode.setColor(getResources().getColor(R.color.reading_text_color_first));
//                novel_option_source.setColor(getResources().getColor(R.color.reading_text_color_first));

                setBackground();
                setBatteryBackground(R.drawable.reading_batty_day);

        }
    }

    private void setTextColor(int color) {
        pageView.setTextColor(color);
    }

    private void setBackground() {
        pageView.setBackground();
    }

    private void setBatteryBackground(int resourceId) {
        pageView.changeBatteryBg(resourceId);
    }

    private void setPageBackColor(int color) {
        pageView.setPageBackColor(color);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // 小说音量键翻页
        if (Constants.isVolumeTurnover) {
            if (pageView != null && pageView.setKeyEvent(event)) {
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (pageView != null && pageView.isAutoReadMode()) {
                if (auto_menu.isShown()) {
                    auto_menu.setVisibility(View.GONE);
                    pageView.setisAutoMenuShowing(false);
                    pageView.resumeAutoRead();
                } else {
                    pageView.pauseAutoRead();
                    auto_menu.setVisibility(View.VISIBLE);
                }
            } else {
                if (readStatus.isMenuShow) {
                    showMenu(false);
                } else {
                    showMenu(true);
                }
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {

        if (mCatlogMarkDrawer != null && mCatlogMarkDrawer.isDrawerOpen(GravityCompat.START)) {
            mCatlogMarkDrawer.closeDrawers();
            return;
        }

        if (isSourceListShow) {
            isSourceListShow = false;
            return;
        }

        if (pageView != null && auto_menu != null && auto_menu.isShown()) {
            auto_menu.setVisibility(View.GONE);
            pageView.setisAutoMenuShowing(false);
            pageView.resumeAutoRead();
            return;
        }
        if (pageView != null && pageView.isAutoReadMode()) {
            autoStop();
            return;
        }
        // 显示菜单
        if (readStatus != null && readStatus.isMenuShow) {
            showMenu(false);
            return;
        }

        if (mBookDaoHelper != null && readStatus != null) {
            isSubed = mBookDaoHelper.isBookSubed(readStatus.book_id);
        }

        if (mBookDaoHelper != null && !isSubed) {
            try {
                if (myNovelHelper != null) {
                    myNovelHelper.showAndBookShelfDialog();
                }
            } catch (InflateException e) {
                e.printStackTrace();
            }
            return;
        }
        goBackToHome();
        if (!isFinishing()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void setMode() {
        if (readSettingView != null) {
            readSettingView.setMode();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppLog.d("ReadingActivity", "onResume:" + Constants.isFullWindowRead);

        // 注册一个接受广播类型
        registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        // 设置全屏
        if (!Constants.isFullWindowRead) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        } else {
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams
//                    .FLAG_LAYOUT_INSET_DECOR);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        if (isFromCover && Constants.IS_LANDSCAPE) {
            return;
        }
        if (isModeChange()) {
            setMode();
        }
        int content_mode = sp.getInt("content_mode", 51);
        if (content_mode == 25 && readSettingView != null) {
            //readSettingView.setSystemLightLowest();
        }
        if (isSubed) {
            readStatus.book = mBookDaoHelper.getBook(readStatus.book_id, 0);
        }
        readStatus.isInMobiViewClicking = false;
        if (pageView != null) {
            pageView.resumeAutoRead();
        }

        readStatus.chapterCount = readStatus.book.chapter_count;


        int lock = sp.getInt("lock_screen_time", 5);
        if (lock == Integer.MAX_VALUE) {
            Constants.screenOffTimeout = lock;
        } else {
            Constants.screenOffTimeout = lock * 60 * 1000;
        }
        setScreenOffTimeout(Constants.screenOffTimeout);
        if (!actNovelRunForeground && !isRestDialogShow) {
            actNovelRunForeground = true;
            startRestTimer();
        }
        if (!isAcvNovelActive && !isRestDialogShow) {
            isAcvNovelActive = true;
            startRestTimer();
        }

        if (dataFactory != null && readStatus != null && Constants.isNetWorkError) {
            Constants.isNetWorkError = false;
            dataFactory.getChapterByLoading(ReadingActivity.MSG_LOAD_CUR_CHAPTER, readStatus.sequence);
        }
    }

    @Override
    public boolean shouldReceiveCacheEvent() {
        return false;
    }

    @Override
    public boolean shouldShowNightShadow() {
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //EventBus.getDefault().register(this);
        if (mCacheUpdateReceiver == null) {
            mCacheUpdateReceiver = new CacheUpdateReceiver(this);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BaseCacheableActivity.ACTION_CACHE_COMPLETE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mCacheUpdateReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isFromCover = false;
        if (isSubed) {
            if (readStatus.book.book_type == 0) {
                myNovelHelper.saveBookmark(dataFactory.chapterList, readStatus.book_id, readStatus.sequence,
                        readStatus.offset, mBookDaoHelper);
                // 统计阅读章节数
                SharedPreferencesUtils spUtils = new SharedPreferencesUtils(PreferenceManager
                        .getDefaultSharedPreferences(this));
                spUtils.putInt("readed_count", Constants.readedCount);
            }
        }
        if (pageView != null) {
            pageView.pauseAutoRead();
        }
        readLength = 0;

    }

    boolean isFirstVisiable = true;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (isFirstVisiable && hasFocus) {
            isFirstVisiable = false;
            initReadingAd();
        }

        if(hasFocus){
            getWindow().getDecorView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getWindow().getDecorView().setSystemUiVisibility(UI_OPTIONS_IMMERSIVE_STICKY);
                }
            }, 1500);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (pageView != null) {
            pageView.removeAdView();
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mCacheUpdateReceiver);

        if (actNovelRunForeground && handler != null && rest_tips_runnable != null) {
            actNovelRunForeground = false;
            handler.removeCallbacks(rest_tips_runnable);
            rest_tips_runnable = null;
        }
        //EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        if (mNovelLoader != null && mNovelLoader.getStatus() == BaseAsyncTask.Status.RUNNING) {
            mNovelLoader.cancel(true);
        }

        if (mCatlogMarkDrawer != null) {
            mCatlogMarkDrawer.removeDrawerListener(mDrawerListener);
            if (mCatalogMarkFragment != null)
                mCatlogMarkDrawer.removeDrawerListener(mCatalogMarkFragment);
        }

        if (readStatus != null && dataFactory != null && dataFactory.currentChapter != null && readStatus.requestItem != null) {
            //按照此顺序传值 当前的book_id，阅读章节，书籍源，章节总页数，当前阅读页，当前页总字数，当前页面来自，开始阅读时间,结束时间,阅读时间,是否有阅读中间退出行为,书籍来源1为青果，2为智能
            StartLogClickUtil.upLoadReadContent(readStatus.book_id, dataFactory.currentChapter.chapter_id + "", readStatus.source_ids, readStatus.pageCount + "",
                    readStatus.currentPage + "", readStatus.currentPageConentLength + "", readStatus.requestItem.fromType + "",
                    readStatus.startReadTime + "", System.currentTimeMillis() + "", System.currentTimeMillis() - readStatus.startReadTime + "", "false", readStatus.requestItem.channel_code + "");

        }
        AppLog.e(TAG, "onDestroy");
        readStatus.isMenuShow = false;
        if (mNovelLoader != null && mNovelLoader.getStatus() == BaseAsyncTask.Status.RUNNING) {
            mNovelLoader.cancel(true);
        }

        if (mBatInfoReceiver != null) {
            try {
                unregisterReceiver(mBatInfoReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 注销监听按下电源键的广播
         */
        if (mPowerOffReceiver != null) {
            try {
                unregisterReceiver(mPowerOffReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mTimerStopped = true;

        if (this.sp != null) {
            this.sp = null;
        }

        if (this.modeSp != null) {
            this.modeSp = null;
        }

        if (pageView != null) {
            pageView.setCallBack(null);
            pageView.clear();
            pageView = null;
        }

        if (myNovelHelper != null) {
            myNovelHelper.setOnHelperCallBack(null);
            myNovelHelper.clear();
        }

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        if (novel_basePageView != null) {
            novel_basePageView.removeAllViews();
            novel_basePageView = null;
        }

        if (readSettingView != null) {
            readSettingView.recycleResource();
            readSettingView = null;
        }

        if (auto_menu != null) {
            auto_menu.setOnAutoMemuListener(null);
            auto_menu.recycleResource();
            auto_menu = null;
        }

        if (reading_content != null) {
            reading_content.removeAllViews();
            reading_content = null;
        }

        if (ll_guide_layout != null) {
            ll_guide_layout = null;
        }

        if (ownNativeAdManager != null) {
//            ownNativeAdManager.recycleResourceFromReading(NativeInit.CustomPositionName.CHANGE_SOURCE_POSITION.toString());
            ownNativeAdManager.recycleResourceFromReading(NativeInit.CustomPositionName.READING_MIDDLE_POSITION.toString());
            ownNativeAdManager.recycleResourceFromReading(NativeInit.CustomPositionName.READING_POSITION.toString());
            ownNativeAdManager.recycleResourceFromReading(NativeInit.CustomPositionName.READING_IN_CHAPTER_POSITION.toString());
            ownNativeAdManager.recycleResourceFromReading(NativeInit.CustomPositionName.REST_POSITION.toString());
            ownNativeAdManager.recycleResourceFromReading(NativeInit.CustomPositionName.SUPPLY_READING_IN_CHAPTER.toString());
            ownNativeAdManager.recycleResourceFromReading(NativeInit.CustomPositionName.SUPPLY_READING_SPACE.toString());
            ownNativeAdManager.recycleResourceFromReading(NativeInit.CustomPositionName.SLIDEUP_POPUPAD_POSITION.toString());
            ownNativeAdManager.recycleResourceFromReading(NativeInit.CustomPositionName.LANDSCAPE_SLIDEUP_POPUPAD.toString());
            ownNativeAdManager.removeHandler();
        }

        Glide.get(this).clearMemory();

        if (readStatus != null) {
            readStatus.recycleResource();
            readStatus.recycleResourceNew();
        }

        if (dataFactory != null) {
            dataFactory.setReadDataListener(null);
            if (dataFactory.mHandler != null) {
                dataFactory.mHandler.removeCallbacksAndMessages(null);
            }
            dataFactory.clean();
        }

        BitmapManager.getInstance().clearBitmap();

        DrawTextHelper.clean();

        super.onDestroy();

        if (BuildConfig.DEBUG) {
            BookApplication.getRefWatcher().watch(this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // 保存书签状态
        try {
            outState.putInt("sequence", readStatus.sequence);
            outState.putInt("nid", readStatus.nid);
            outState.putInt("offset", readStatus.offset);
            outState.putSerializable("book", readStatus.book);
            if (dataFactory != null && dataFactory.currentChapter != null) {
                outState.putSerializable("currentChapter", dataFactory.currentChapter);
            }
            outState.putString("thememode", mThemeHelper.getMode());
            super.onSaveInstanceState(outState);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    private void goToBookOver() {
        if (isFinishing()) {
            return;
        }
        Intent intent = new Intent(ReadingActivity.this, BookEndActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.REQUEST_ITEM, readStatus.getRequestItem());
        bundle.putString("bookName", readStatus.bookName);
        bundle.putString("book_id", readStatus.book_id);
        bundle.putString("book_category", readStatus.book.category);
        bundle.putSerializable("book", readStatus.book);
        bundle.putString("thememode", currentThemeMode);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (data != null) {// 更新目录后，重新获取chapterList
                if (requestCode == 1) {
                    showMenu(false);
                }
                Bundle bundle = data.getExtras();
                readStatus.sequence = bundle.getInt("sequence");
                readStatus.offset = bundle.getInt("offset", 0);
                readStatus.book = (Book) bundle.getSerializable("book");
                RequestItem requestItem = (RequestItem) bundle.getSerializable(Constants.REQUEST_ITEM);
                AppLog.e(TAG, "onActivityResult: " + requestItem.toString());
                if (!readStatus.source_ids.contains(readStatus.book.site)) {
                    readStatus.source_ids += "`" + readStatus.book.site;
                }

                AppLog.e(TAG, "from" + readStatus.requestItem.fromType + "===");
                if (requestItem != null) {
                    readStatus.setRequestItem(requestItem);
                    //readStatus.requestConfig = BookApplication.getGlobalContext().getSourceConfig(requestItem.host);
                }
                if (dataFactory.chapterList != null) {
                    dataFactory.chapterList.clear();
                }
                myNovelHelper.isShown = false;
                readStatus.currentPage = 1;
                dataFactory.nextChapter = null;
                dataFactory.preChapter = null;
                readStatus.requestItem.fromType = 1;//打点 书籍封面（0）/书架（1）/上一页翻页（2）
                if (Constants.QG_SOURCE.equals(readStatus.book.site)) {
                    requestItem.channel_code = 1;
                } else {
                    requestItem.channel_code = 2;
                }
                getBookContent();
            }
        }
    }

    @Override
    public void notificationCallBack(Notification preNTF, String book_id) {
        PendingIntent pending = null;
        Intent intent = null;
        if (!book_id.equals(-1 + "")) {
            intent = new Intent(this, DownBookClickReceiver.class);
            intent.setAction(DownBookClickReceiver.action);
            intent.putExtra("book_id", book_id);
            pending = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            intent = new Intent(this, DownloadManagerActivity.class);
            pending = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        preNTF.contentIntent = pending;
    }

    @Override
    public void jumpNextChapter() {
        if (readStatus.isMenuShow) {
            showMenu(false);
            return;
        }
        dataFactory.next();
        pageView.drawCurrentPage();
    }

    @Override
    public void onShowMenu(boolean isShow) {
        showMenu(isShow);
    }

    @Override
    public void onCancelPage() {
        dataFactory.restore();
        refreshPage();
    }

    @Override
    public void onResize() {
        AppLog.e("ReadingActivity", "onResize");
        if (dataFactory.currentChapter != null && readStatus.book != null) {
            myNovelHelper.getChapterContent(this, dataFactory.currentChapter, readStatus
                    .book, true);
            refreshPage();
        }
    }

    @Override
    public void freshPage() {
        refreshPage();
    }

    @Override
    public void gotoOver() {
        goToBookOver();
    }

    @Override
    public void showToast(int str) {
        showToastShort(str);
    }

    @Override
    public void downLoadNovelMore() {
        downloadNovel();
    }

    @Override
    public void initBookStateDeal() {
        //        initPageMode();// 翻页模式
        // 加载字体、亮度、阅读模式信息
        if (readSettingView != null) {
            readSettingView.initShowCacheState();
        }
        // 初始化时间显示
        refreshPage();
        initTime();

        // 刷新页面
        // 刷新内容显示
        // 启动预加载
        downloadNovel();
    }

    @Override
    public void onShowAutoMenu(boolean show) {
        if (show) {
            auto_menu.setVisibility(View.VISIBLE);
            if (pageView != null) {
                pageView.pauseAutoRead();
            }
        } else {
            auto_menu.setVisibility(View.GONE);
            if (pageView != null) {
                pageView.resumeAutoRead();
            }
        }
    }

    @Override
    public void speedUp() {
        readStatus.setAutoReadSpeed(++autoSpeed);
        autoSpeed = readStatus.autoReadSpeed();
    }

    @Override
    public void speedDown() {
        readStatus.setAutoReadSpeed(--autoSpeed);
        autoSpeed = readStatus.autoReadSpeed();
    }

    @Override
    public void autoStop() {
        if (pageView != null) {
            pageView.exitAutoRead();
        }

        if (isSlideToAuto) {
            PageInterface temp;
            Constants.isSlideUp = true;
            novel_basePageView.removeAllViews();
            temp = pageView;
            pageView = new ScrollPageView(this);
            novel_basePageView.addView((View) pageView, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));

            pageView.init(this, readStatus, myNovelHelper);
            pageView.setCallBack(this);
            pageView.setReadFactory(dataFactory);
            dataFactory.setPageView(pageView);
            myNovelHelper.setPageView(pageView);
            pageView.freshTime(time_text);
            pageView.freshBattery(batteryPercent);
            changeMode(current_mode);

            if (temp != null) {
                temp.clear();
            }
        }
        TextView view = (TextView) inflater.inflate(R.layout.autoread_textview, null);
        Toast toast = new Toast(getApplicationContext());
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        auto_menu.setVisibility(View.GONE);
    }

    private void pauseAutoReadHandler() {
        pageView.pauseAutoRead();
    }

    private void resumeAutoReadHandler() {
        pageView.resumeAutoRead();
    }

    @Override
    public void changeChapter() {
        if (readSettingView != null) {
            readSettingView.changeChapter();
        }
        changeMarkState();
    }

    @Override
    public void onReadCatalog() {
//        openCategoryPage();
        if (readStatus.isMenuShow) {
            showMenu(false);
        }
        if (mNovelLoader != null && mNovelLoader.getStatus() == BaseAsyncTask.Status.RUNNING) {
            mNovelLoader.cancel(true);
        }

        mCatlogMarkDrawer.openDrawer(GravityCompat.START);

        Map<String, String> data = new HashMap<>();
        if (readStatus != null) {
            data.put("bookid", readStatus.book_id);
        }
        if (dataFactory != null && dataFactory.currentChapter != null) {
            data.put("chapterid", dataFactory.currentChapter.book_id);
        }
        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.CATALOG, data);
    }

    @Override
    public void onReadChangeSource() {
        if (Book.isOnlineType(readStatus.book.book_type)) {
            openSourcePage();
        }
    }

    @Override
    public void onReadCache() {
        if (Book.isOnlineType(readStatus.book.book_type)) {
            startDownLoad();
        }
    }

    @Override
    public void onReadAuto() {
        if (System.currentTimeMillis() - stampTime < 1000) {
            return;
        }
        stampTime = System.currentTimeMillis();
        isSlideToAuto = Constants.isSlideUp;
        if (Constants.isSlideUp) {
            PageInterface temp;
            Constants.isSlideUp = false;
            novel_basePageView.removeAllViews();
            temp = pageView;
            pageView = new PageView(this);
            novel_basePageView.addView((View) pageView, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));
            pageView.init(this, readStatus, myNovelHelper);
            pageView.setCallBack(this);
            pageView.setReadFactory(dataFactory);
            dataFactory.setPageView(pageView);
            myNovelHelper.setPageView(pageView);
            pageView.freshTime(time_text);
            pageView.freshBattery(batteryPercent);
            pageView.drawCurrentPage();
            changeMode(current_mode);
            if (temp != null) {
                temp.clear();
            }
        }
        pageView.startAutoRead();
        showMenu(false);
        showMenu(false);
    }

    @Override
    public void onChangeMode(int mode) {
        changeMode(mode);
    }

    @Override
    public void onChangeScreenMode() {
        changeScreenMode();
    }

    public void addTextLength(int l) {
        readLength += l;
    }

    @Override
    public void onRedrawPage() {
        if (pageView instanceof ScrollPageView && ((ScrollPageView) pageView).tempChapter != null) {
            myNovelHelper.getChapterContent(this, ((ScrollPageView) pageView).tempChapter,
                    readStatus.book, true);
        } else {
            myNovelHelper.getChapterContent(this, dataFactory.currentChapter, readStatus
                    .book, true);
        }
        refreshPage();
        pageView.drawCurrentPage();
        pageView.drawNextPage();
        pageView.getChapter(true);
    }

    @Override
    public void onJumpChapter() {
        dataFactory.getChapterByLoading(ReadingActivity.MSG_JUMP_CHAPTER, readStatus.novel_progress);
    }

    @Override
    public void onJumpPreChapter() {
        readStatus.currentPage = 1;
        dataFactory.toChapterStart = true;
        dataFactory.previous();
        if (Constants.isSlideUp) {
            pageView.getChapter(false);
        } else {
            pageView.drawCurrentPage();
            pageView.drawNextPage();
        }
        changeMarkState();

        if (!pageView.isAutoReadMode()) {
            Constants.manualReadedCount++;
            dataFactory.dealManualDialogShow();
        }

        Map<String, String> data = new HashMap<>();
        if (readStatus != null) {
            data.put("bookid", readStatus.book_id);
        }
        if (dataFactory != null && dataFactory.currentChapter != null) {
            data.put("chapterid", dataFactory.currentChapter.chapter_id);
        }
        data.put("type", "1");
        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.CHAPTERTURN, data);

    }

    @Override
    public void onJumpNextChapter() {
        readStatus.currentPage = readStatus.pageCount;
        dataFactory.next();
        if (Constants.isSlideUp) {
            pageView.getChapter(false);
        } else {
            pageView.drawCurrentPage();
            pageView.drawNextPage();
        }
        changeMarkState();

        if (!pageView.isAutoReadMode()) {
            Constants.manualReadedCount++;
            dataFactory.dealManualDialogShow();
        }

        Map<String, String> data = new HashMap<>();
        if (readStatus != null) {
            data.put("bookid", readStatus.book_id);
        }
        if (dataFactory != null && dataFactory.currentChapter != null) {
            data.put("chapterid", dataFactory.currentChapter.chapter_id);
        }
        data.put("type", "2");
        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.CHAPTERTURN, data);

    }

    @Override
    public void onReadFeedBack() {
        if (!isFinishing()) {
//            final Map<String, String> data = new HashMap<>();
            Book book ;
            if (readStatus.sequence == -1) {
                showToastShort("请到错误章节反馈");
                return;
            }
            if(readStatus != null && readStatus.book != null){
                book = readStatus.book;
//                data.put("bookid",book.book_id);
            }
            myDialog = new MyDialog(this, R.layout.dialog_feedback);
            myDialog.setCanceledOnTouchOutside(true);
            TextView dialog_title = (TextView) myDialog.findViewById(R.id.dialog_title);
            dialog_title.setText(R.string.read_bottom_feedback);
            LinearLayout checkboxsParent = (LinearLayout) myDialog.findViewById(R.id.feedback_checkboxs_parent);
            final CheckBox[] checkboxs = new CheckBox[7];
            RelativeLayout[] relativeLayouts = new RelativeLayout[7];
            int index = 0;
            for (int i = 0; i < checkboxsParent.getChildCount(); i++) {
                RelativeLayout relativeLayout = (RelativeLayout) checkboxsParent.getChildAt(i);
                relativeLayouts[i] = relativeLayout;
                relativeLayouts[i].setTag(i);
                for (int j = 0; j < relativeLayout.getChildCount(); j++) {
                    View v = relativeLayout.getChildAt(j);
                    if (v instanceof CheckBox) {
                        checkboxs[index] = (CheckBox) v;
                        index++;
                    }
                }
            }

            if (Constants.IS_LANDSCAPE) {
                myDialog.findViewById(R.id.sv_feedback).getLayoutParams().height = getResources().getDimensionPixelOffset(R.dimen.dimen_view_height_160);
            } else {
                myDialog.findViewById(R.id.sv_feedback).getLayoutParams().height = ScrollView.LayoutParams.WRAP_CONTENT;
            }

            for (RelativeLayout relativeLayout : relativeLayouts) {
                relativeLayout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (CheckBox checkBox : checkboxs) {
                            checkBox.setChecked(false);
                        }
                        checkboxs[(int) v.getTag()].setChecked(true);
                    }
                });
            }
            Button submitButton = (Button) myDialog.findViewById(R.id.feedback_submit);
            submitButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    StatServiceUtils.statAppBtnClick(ReadingActivity.this, StatServiceUtils.rb_click_feedback_submit);
                    for (int n = 0; n < checkboxs.length; n++) {
                        if (checkboxs[n].isChecked()) {
                            type = n + 1;
                        }
                    }
                    if (type == -1) {
                        showToastShort("请选择错误类型");
                    } else {
//                        data.put("type", "1");
//						StartLogClickUtil.upLoadEventLog(ReadingActivity.this, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.REPAIRDEDIALOGUE, data);
                        submitFeedback(type);
                        dismissDialog();
                        type = -1;
                    }
                }
            });

            Button cancelImage = (Button) myDialog.findViewById(R.id.feedback_cancel);
            cancelImage.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

//                    data.put("type", "2");
//                    StartLogClickUtil.upLoadEventLog(ReadingActivity.this, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.REPAIRDEDIALOGUE, data);
                    dismissDialog();
                }
            });

            if (!myDialog.isShowing()) {
                try {
                    myDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onChageNightMode() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Editor edit = sharedPreferences.edit();
        Map<String, String> data = new HashMap<>();

        if (mThemeHelper.isNight()) {
            //夜间模式只有一种背景， 不能存储
//            edit.putInt("current_night_mode", Constants.MODE);
            Constants.MODE = sharedPreferences.getInt("current_light_mode", 51);
            mThemeHelper.setMode(ThemeMode.THEME1);
            data.put("type", "2");
            StartLogClickUtil.upLoadEventLog(getApplicationContext(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.NIGHTMODE1, data);
        } else {
            edit.putInt("current_light_mode", Constants.MODE);
//            Constants.MODE = sharedPreferences.getInt("current_night_mode", 61);
            //夜间模式只有一种背景
            Constants.MODE = 61;
            mThemeHelper.setMode(ThemeMode.NIGHT);
            data.put("type", "1");
            StartLogClickUtil.upLoadEventLog(getApplicationContext(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.NIGHTMODE1, data);
        }
        edit.putInt("content_mode", Constants.MODE);
        edit.apply();
        changeMode(Constants.MODE);
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

    private void submitFeedback(int type) {
        if (NetWorkUtils.getNetWorkType(this) == NetWorkUtils.NETWORK_NONE) {
            showToastShort("网络异常");
            return;
        }
        ChapterErrorBean chapterErrorBean = new ChapterErrorBean();
        Book book = readStatus.book;
        chapterErrorBean.bookName = getEncode(book.name);
        chapterErrorBean.author = getEncode(book.author);
        chapterErrorBean.channelCode = Constants.QG_SOURCE.equals(book.site) ? "1" : "2";
        BookChapterDao bookChapterDao = new BookChapterDao(this, book.book_id);
        Chapter currChapter = bookChapterDao.getChapterBySequence(readStatus.sequence);
        if (currChapter == null) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showToastShort("已发送");
                }
            }, 1000);
            return;
        }
        chapterErrorBean.bookSourceId = TextUtils.isEmpty(currChapter.book_source_id) ? book.book_source_id : currChapter.book_source_id;
        chapterErrorBean.chapterId = TextUtils.isEmpty(currChapter.chapter_id) ? "" : currChapter.chapter_id;
        chapterErrorBean.chapterName = getEncode(currChapter.chapter_name);
        chapterErrorBean.host = currChapter.site;
        chapterErrorBean.serial = currChapter.sequence;
        chapterErrorBean.type = type;
        String curl = currChapter.curl;
        if (!TextUtils.isEmpty(curl)) {
            if (curl.contains("/V1/book/")) {
                String s = book.book_id + "/";
                int start = curl.indexOf(s) + s.length();
                int end = curl.indexOf("/", start);
                chapterErrorBean.bookChapterId = curl.substring(start, end);
                AppLog.i(TAG, "chapterErrorBean.bookChapterId = " + chapterErrorBean.bookChapterId);
            }
        }
        if (TextUtils.isEmpty(chapterErrorBean.bookChapterId)) {
            chapterErrorBean.bookChapterId = "";
        }
        if (TextUtils.isEmpty(chapterErrorBean.host)) {
            chapterErrorBean.host = "";
        }
        AppLog.i(TAG, "chapterErrorBean = " + chapterErrorBean.toString());
        LoadDataManager loadDataManager = new LoadDataManager(this);
        loadDataManager.submitBookError(chapterErrorBean);
        StartLogClickUtil.upLoadChapterError(chapterErrorBean);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showToastShort("已发送");
            }
        }, 1000);
    }

    private String getEncode(String content) {
        if (TextUtils.isEmpty(content)) {
            return "";
        }
        try {
            return URLEncoder.encode(content, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void changeMarkState() {
        if(mReadOptionPresenter!=null){
            mReadOptionPresenter.updateStatus();
        }
    }

    public void goBackToHome() {
        if (!currentThemeMode.equals(mThemeHelper.getMode())) {
            Intent themIntent = new Intent(ReadingActivity.this, HomeActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(EventBookStore.BOOKSTORE, EventBookStore.TYPE_TO_SWITCH_THEME);
            themIntent.putExtras(bundle);
            startActivity(themIntent);
            overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
            finish();
        } else {
            if (isTaskRoot()) {
                Intent intent = new Intent(this, SplashActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }

    @Override
    public void onOriginClick() {
        String url = null;
        if (dataFactory != null && dataFactory.currentChapter != null) {
            url = UrlUtils.buildContentUrl(dataFactory.currentChapter.curl);
        }
        if (!TextUtils.isEmpty(url)) {
            Uri uri = Uri.parse(url.trim());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(intent);
            }catch (Exception e){
                e.printStackTrace();
            }
            Map<String, String> data = new HashMap<>();
            if (readStatus != null) {
                data.put("bookid", readStatus.book_id);
            }
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.ORIGINALLINK, data);
        } else {
            Toast.makeText(this, "无法查看原文链接", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTransCodingClick() {
        showDisclaimerActivity();
    }

    private static class TimerRunnable implements Runnable {
        private WeakReference<ReadingActivity> actReference;

        TimerRunnable(ReadingActivity act) {
            actReference = new WeakReference<>(act);
        }

        @Override
        public void run() {
            ReadingActivity readingActivity = actReference.get();
            if (readingActivity == null) {
                return;
            }
            if (readingActivity.mTimerStopped || readingActivity.pageView == null) {
                return;
            }
            readingActivity.mCalendar.setTimeInMillis(System.currentTimeMillis());
            try {
                if (readingActivity.pageView != null) {
                    readingActivity.time_text = DateFormat.format(mFormat, readingActivity.mCalendar);
                    readingActivity.pageView.freshTime(readingActivity.time_text);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            long now = SystemClock.uptimeMillis();
            long next = now + (30000 - now % 1000);
            readingActivity.handler.postAtTime(readingActivity.mTicker, next);
        }
    }

    static class CacheUpdateReceiver extends BroadcastReceiver {

        private final WeakReference<Activity> mActivityWeakReference;

        public CacheUpdateReceiver(Activity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.e("CacheUpdateReceiver", "onReceive");
            final Book book = (Book) intent.getSerializableExtra(Constants.REQUEST_ITEM);
            if (book == null)
                return;

            if (!Constants.QG_SOURCE.equals(book.site)) {

                if (mActivityWeakReference.get() != null && readStatus.book.book_id.equals(book.book_id)) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("sequence", readStatus.sequence);
                    bundle.putInt("offset", readStatus.offset);
                    bundle.putSerializable("book", readStatus.book);
                    bundle.putSerializable(Constants.REQUEST_ITEM, readStatus.requestItem);
                    Intent fresh = new Intent(mActivityWeakReference.get(), ReadingActivity.class);
                    fresh.putExtras(bundle);
                    fresh.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    mActivityWeakReference.get().startActivity(fresh);
                }
            }
        }
    }

    static class UiHandler extends Handler {
        private WeakReference<ReadingActivity> actReference;

        UiHandler(ReadingActivity act) {
            actReference = new WeakReference<>(act);
        }

        public void handleMessage(android.os.Message msg) {
            ReadingActivity activity = actReference.get();
            if (activity == null) {
                return;
            }
            switch (msg.what) {
                case 0:
                    break;
                case 1:
                    activity.pauseAutoReadHandler();
                    break;
                case 2:
                    activity.resumeAutoReadHandler();
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    if (activity.readStatus.sequence != -1) {
                        activity.openSourcePage();
                    }
                    break;
                case RequestExecutor.REQUEST_BOOK_SOURCE_SUCCESS:
                    activity.sourcesList = (ArrayList<Source>) msg.obj;
                    break;
                case RequestExecutor.REQUEST_BOOK_SOURCE_ERROR:
                    break;
                default:
                    break;
            }
        }
    }

    private class NovelDownloader extends BaseAsyncTask<Integer, Void, Void> {

        @Override
        protected void onPostExecute(Void result) {
            if (isCancelled() || isFinishing())
                return;
            super.onPostExecute(result);
        }

        @Override
        protected Void doInBackground(Integer... params) {
            if(dataFactory!=null){
                ArrayList<Chapter> chapterList = (ArrayList<Chapter>) dataFactory.chapterList.clone();
                if (chapterList == null) {
                    return null;
                }
                int size = chapterList.size();
                if(readStatus!=null){
                    for (int i = readStatus.sequence + 1; i < (readStatus.sequence + params[0] + 1) && i < size; i++) {
                        Chapter c = chapterList.get(i);
                        if (c == null) {
                            return null;
                        }
                        try {
                            AppLog.e(TAG, "预加载： " + c.toString());
                            c = requestFactory.requestExecutor(readStatus.getRequestItem()).requestSingleChapter
                                    (readStatus.book.dex, mBookDaoHelper, bookChapterDao, c);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (i == (readStatus.sequence + 1)) {
                            if (dataFactory != null){
                                dataFactory.nextChapter = c;
                            }
                        }
                        if (isCancelled()) {
                            break;
                        }
                    }
                }
            }

            return null;
        }
    }
}
