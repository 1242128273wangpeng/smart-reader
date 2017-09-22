package com.intelligent.reader.activity;

import com.dingyueads.sdk.db.AdDao;
import com.intelligent.reader.BuildConfig;
import com.intelligent.reader.R;
import com.intelligent.reader.app.BookApplication;
import com.intelligent.reader.fragment.BaseFragment;
import com.intelligent.reader.fragment.BookShelfFragment;
import com.intelligent.reader.fragment.HomeFragment;
import com.intelligent.reader.fragment.WebViewFragment;
import com.intelligent.reader.util.BookShelfRemoveHelper;
import com.intelligent.reader.util.EventBookStore;

import net.lzbook.kit.app.ActionConstants;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.component.service.CheckNovelUpdateService;
import net.lzbook.kit.book.view.NonSwipeViewPager;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.BookEvent;
import net.lzbook.kit.data.bean.RequestItem;
import net.lzbook.kit.request.UrlUtils;
import net.lzbook.kit.utils.ATManager;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.FrameBookHelper;
import net.lzbook.kit.utils.JSInterfaceHelper;
import net.lzbook.kit.utils.LoadDataManager;
import net.lzbook.kit.utils.MD5Utils;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.StatisticManager;
import net.lzbook.kit.utils.ToastUtils;
import net.lzbook.kit.utils.update.ApkUpdateUtils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.webkit.WebView;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * 书架,书城页面
 * Created by q on 2015/9/7.
 */
public class HomeActivity extends BaseCacheableActivity implements BaseFragment.FragmentCallback,
        FrameBookHelper.CancleUpdateCallback, WebViewFragment.FragmentCallback, CheckNovelUpdateService.OnBookUpdateListener {
    private static final String TAG = HomeActivity.class.getSimpleName();
    private final static int BACK = 12;
    private static int BACK_COUNT;
    static Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case BACK:
                    BACK_COUNT = 0;
                    break;
            }
            return true;
        }
    });
    public FrameBookHelper frameHelper;
    IntentFilter filter;
    private HomeFragment mHomeFragment;
    private NonSwipeViewPager viewPager;
    private BookShelfRemoveHelper removeMenuHelper;
    private BookShelfFragment bookView;
    private boolean isClosed = false;
    private ApkUpdateUtils apkUpdateUtils;
    private MyReceiver receiver;
    private LoadDataManager mLoadDataManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
//            setTheme(R.style.Theme4);
            setContentView(R.layout.activity_main2);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        initView();
        initData();
        //ownUpdate
        //注册广播接收器
        receiver = new MyReceiver();
        filter = new IntentFilter();
        filter.addAction(ActionConstants.DOWN_APP_SUCCESS_ACTION);
        HomeActivity.this.registerReceiver(receiver, filter);

        apkUpdateUtils = new ApkUpdateUtils(this);
        try {
            apkUpdateUtils.getApkUpdateInfo(this, handler, "HomeActivity");
        } catch (Exception e) {
            e.printStackTrace();
        }
        initPositon();
        checckUrlIsTest();
        EventBus.getDefault().register(this);
    }

    private void checckUrlIsTest() {
        if (UrlUtils.BOOK_NOVEL_DEPLOY_HOST.contains("test") || UrlUtils.BOOK_WEBVIEW_HOST.contains("test")) {
            ToastUtils.showToastNoRepeat("请注意！！请求的是测试地址！！！");
        }
    }


    private void initPositon() {
        Intent intent = getIntent();
        int position;
        if (intent != null) {
            if (intent.hasExtra(EventBookStore.BOOKSTORE)) {
                position = intent.getIntExtra(EventBookStore.BOOKSTORE, 0);
                if (mHomeFragment != null) {
                    mHomeFragment.setTabSelected(position);
                }
            } else {
                int intExtra = intent.getIntExtra(EventBookStore.BOOKSTORE, EventBookStore
                        .TYPE_ERROR);
                if (intExtra != EventBookStore.TYPE_ERROR) {
                    if (!isFinishing()) {
                        if (mHomeFragment != null) {
                            mHomeFragment.setTabSelected(intExtra);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void receiveUpdateCallBack(Notification preNTF) {
        Intent intent = new Intent(this, HomeActivity.class);
        PendingIntent pending = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        preNTF.contentIntent = pending;
    }

    /**
     * 打开安装包文件
     */
    public void setup(String filePath) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        String type = "application/vnd.android.package-archive";
        intent.setDataAndType(Uri.fromFile(new File(filePath)), type);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int position = 0;
        if (intent != null && intent.hasExtra("position")) {
            position = intent.getIntExtra("position", 0);
            if (mHomeFragment != null) {
                mHomeFragment.viewPager.setCurrentItem(position);
            }
        } else {
            if (intent != null) {//for bookend
                int intExtra = intent.getIntExtra(EventBookStore.BOOKSTORE, EventBookStore
                        .TYPE_ERROR);
                if (intExtra != EventBookStore.TYPE_ERROR) {
                    if (!isFinishing()) {
                        if (mHomeFragment != null) {
                            mHomeFragment.setTabSelected(intExtra);
                        }
                    }
                }
            }
        }
    }

    private void initView() {
        if (mHomeFragment == null) {
            mHomeFragment = new HomeFragment();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, mHomeFragment)
                .commit();
    }

    protected void initData() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor edit = sharedPreferences.edit();
        //获取阅读页背景
        if (sharedPreferences.getInt("content_mode", 51) < 50) {
            Constants.MODE = 51;
            edit.putInt("content_mode", Constants.MODE);
            edit.putInt("current_light_mode", Constants.MODE);
            edit.apply();
        } else {
            Constants.MODE = sharedPreferences.getInt("content_mode", 51);
        }

        //判断用户是否是当日首次打开应用
        long first_time = sharedPreferences.getLong(Constants.TODAY_FIRST_OPEN_APP, 0);
        AppLog.e("BaseBookApplication", "first_time=" + first_time);
        long currentTime = System.currentTimeMillis();
        boolean b = AppUtils.isToday(first_time, currentTime);
        if (b) {
            //用户非首次打开
            Constants.is_user_today_first = false;
        } else {
            //用户首次打开，记录当前时间
            Constants.is_user_today_first = true;
            sharedPreferences.edit().putLong(Constants.TODAY_FIRST_OPEN_APP, currentTime).apply();
            sharedPreferences.edit().putBoolean(Constants.IS_UPLOAD, false).apply();
            new GetAppList().execute();
        }
        AppLog.e("BaseBookApplication", "Constants.is_user_today_first=" + Constants.is_user_today_first);

        mLoadDataManager = new LoadDataManager(this);
        Constants.upload_userinformation = sharedPreferences.getBoolean(Constants.IS_UPLOAD, false);

        final int premVersionCode = Constants.preVersionCode;
        final int currentVersionCode = AppUtils.getVersionCode();

        if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
            //
            if (!Constants.upload_userinformation || premVersionCode != currentVersionCode) {
                // 获取用户基础数据
                StatisticManager.getStatisticManager().sendUserData();

                Constants.upload_userinformation = true;
                Constants.preVersionCode = currentVersionCode;
                sharedPreferences.edit().putBoolean(Constants.IS_UPLOAD, Constants.upload_userinformation).apply();
            }
        }


        if (Constants.is_user_today_first) {
            // 老用户更新书架书籍的完结/连载状态,和dex值
            mLoadDataManager.updateShelfBooks();

            // 用户第一次启动时删掉物料表中的信息
            new Thread() {
                @Override
                public void run() {
                    try {
                        AdDao.getInstance(HomeActivity.this).deleteAdMaterial();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (viewPager != null && viewPager.getCurrentItem() != 0 && mHomeFragment != null) {
                mHomeFragment.setTabSelected(0);
                return true;
            } else if (removeMenuHelper != null && removeMenuHelper.dismissRemoveMenu()) {

                return true;
            } else {
                doubleClickFinish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void onPause() {
        try {
            super.onPause();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (frameHelper != null) {
            frameHelper.onPauseAction();
        }
    }

    /**
     * 接收默认书籍的加载完成刷新
     */
    public void onEvent(BookEvent event) {
        if (event.getMsg().equals(BookEvent.DEFAULTBOOK_UPDATED)) {
            if (mLoadDataManager != null)
                mLoadDataManager.updateShelfBooks();
        } else if (event.getMsg().equals(BookEvent.PULL_BOOK_STATUS)) {
            if (bookView != null) {
                bookView.updateBook();
            }
        }
    }

    /**
     * 两次返回键退出
     */
    private void doubleClickFinish() {
        BACK_COUNT++;
        if (BACK_COUNT == 1) {
            showToastLong(R.string.mian_click_tiwce_exit);
        } else if (BACK_COUNT > 1 && !isClosed) {
            isClosed = true;
            restoreSystemDisplayState();
            ATManager.exitClient();
            finish();
        }
        Message message = handler.obtainMessage(0);
        message.what = BACK;
        handler.sendMessageDelayed(message, 2000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void getViewPager(ViewPager pager) {
        this.viewPager = (NonSwipeViewPager) pager;
    }

    @Override
    public void getRemoveMenuHelper(BookShelfRemoveHelper helper) {
        this.removeMenuHelper = helper;
    }

    @Override
    public void getFrameBookRankView(Fragment bookView) {
        this.bookView = (BookShelfFragment) bookView;
    }

    @Override
    public void frameHelper() {
        if (frameHelper == null) {
            frameHelper = new FrameBookHelper(getApplicationContext(), HomeActivity.this);
        }
        frameHelper.setCancleUpdate(this);
    }

    @Override
    public void getAllCheckedState(boolean isAllChecked) {
    }

    @Override
    public void getMenuShownState(boolean state) {
        if (mHomeFragment != null)
            mHomeFragment.onMenuShownState(state);
    }

    @Override
    public void setSelectTab(int index) {
        if (mHomeFragment != null) {
            mHomeFragment.setTabSelected(index);
        }
    }

    @Override
    public void restoreSystemState() {
        restoreSystemDisplayState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HomeActivity.this.unregisterReceiver(receiver);
        if (frameHelper != null) {
            frameHelper.restoreState();
            frameHelper = null;
        }
        mHomeFragment = null;
        removeMenuHelper = null;
        viewPager = null;
        bookView = null;
        mHomeFragment = null;
        try {
            setContentView(R.layout.empty);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        if (BuildConfig.DEBUG) {
            BookApplication.getRefWatcher().watch(this);
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void webJsCallback(JSInterfaceHelper jsInterfaceHelper) {


        jsInterfaceHelper.setOnEnterAppClick(new JSInterfaceHelper.onEnterAppClick() {

            @Override
            public void doEnterApp(String name) {
                AppLog.e(TAG, "doEnterApp");

            }
        });
        jsInterfaceHelper.setOnSearchClick(new JSInterfaceHelper.onSearchClick() {
            @Override
            public void doSearch(String keyWord, String search_type, String filter_type, String filter_word, String sort_type) {
                try {
                    Intent intent = new Intent();
                    intent.setClass(HomeActivity.this, SearchBookActivity.class);
                    intent.putExtra("word", keyWord);
                    intent.putExtra("search_type", search_type);
                    intent.putExtra("filter_type", filter_type);
                    intent.putExtra("filter_word", filter_word);
                    intent.putExtra("sort_type", sort_type);
                    startActivity(intent);
                    AppLog.i(TAG, "enterSearch success");
                } catch (Exception e) {
                    AppLog.e(TAG, "Search failed");
                    e.printStackTrace();
                }
            }
        });
        jsInterfaceHelper.setOnAnotherWebClick(new JSInterfaceHelper.onAnotherWebClick() {

            @Override
            public void doAnotherWeb(String url, String name) {
                AppLog.e(TAG, "doAnotherWeb");
                try {
                    Intent intent = new Intent();
                    intent.setClass(HomeActivity.this, FindBookDetail.class);
                    intent.putExtra("url", url);
                    intent.putExtra("title", name);
                    startActivity(intent);
                    AppLog.e(TAG, "EnterAnotherWeb");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        jsInterfaceHelper.setOnOpenAd(new JSInterfaceHelper.onOpenAd() {

            @Override
            public void doOpenAd(String url) {
                AppLog.e(TAG, "doOpenAd");

            }
        });
        jsInterfaceHelper.setOnEnterCover(new JSInterfaceHelper.onEnterCover() {

            @Override
            public void doCover(final String host, final String book_id, final String book_source_id, final String name, final String author, final
            String parameter, final String extra_parameter) {
                AppLog.e(TAG, "doCover");

                RequestItem requestItem = new RequestItem();
                requestItem.book_id = book_id;
                requestItem.book_source_id = book_source_id;
                requestItem.host = host;
                requestItem.name = name;
                requestItem.author = author;
                requestItem.parameter = parameter;
                requestItem.extra_parameter = extra_parameter;

                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), CoverPageActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.REQUEST_ITEM, requestItem);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });


        jsInterfaceHelper.setOnEnterCategory(new JSInterfaceHelper.onEnterCategory() {

            @Override
            public void doCategory(final int gid, final int nid, final String name, final int lastSort) {
                AppLog.e(TAG, "doCategory");
            }
        });


    }

    @Override
    public String startLoad(WebView webView, String url) {
        return url;
    }

    /**
     * 获取广播数据
     *
     * @author jiqinlin
     */
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            int count = bundle.getInt("count");
            String filePath = bundle.getString("filePath");
            String downloadLink = bundle.getString("downloadLink");
            String md5 = bundle.getString("md5");
            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            if (count == 100) {
                AppLog.e("--------------->", MD5Utils.getFileMD5(new File(filePath)));
                if (MD5Utils.getFileMD5(new File(filePath)).equalsIgnoreCase(md5)) {
                    setup(filePath);
                } else {
                    Intent errorIntent = new Intent();
                    errorIntent.setClass(context, DownloadErrorActivity.class);
                    errorIntent.putExtra("downloadLink", downloadLink);
                    errorIntent.putExtra("md5", md5);
                    errorIntent.putExtra("fileName", fileName);
                    errorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(errorIntent);
                }
            }
        }
    }

    // 获取用户app列表
    class GetAppList extends AsyncTask<Void, Integer, String> {


        @Override
        protected String doInBackground(Void... params) {
            String appListInfo = AppUtils.scanLocalInstallAppList(HomeActivity.this.getPackageManager());
            return appListInfo;
        }

        @Override
        protected void onPostExecute(String s) {

            StartLogClickUtil.upLoadApps(HomeActivity.this, s);
        }
    }
}
