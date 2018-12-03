package com.intelligent.reader.activity;

import static android.view.KeyEvent.KEYCODE_BACK;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.ding.basic.RequestRepositoryFactory;
import com.ding.basic.bean.Book;
import com.ding.basic.bean.BookFix;
import com.ding.basic.bean.Chapter;
import com.ding.basic.util.sp.SPKey;
import com.ding.basic.util.sp.SPUtils;
import com.intelligent.reader.R;

import net.lzbook.kit.app.base.BaseBookApplication;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.service.CheckNovelUpdateService;
import net.lzbook.kit.ui.activity.base.FrameActivity;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.ShieldManager;
import net.lzbook.kit.utils.StatServiceUtils;
import net.lzbook.kit.utils.download.CacheManager;
import net.lzbook.kit.utils.dynamic.DynamicParameter;
import net.lzbook.kit.utils.logger.AppLog;
import net.lzbook.kit.utils.router.RouterConfig;
import net.lzbook.kit.utils.user.UserManager;
import net.lzbook.kit.utils.web.WebResourceCache;

import java.lang.ref.WeakReference;
import java.util.List;

import io.reactivex.schedulers.Schedulers;

@Route(path = RouterConfig.SPLASH_ACTIVITY)
public class SplashActivity extends FrameActivity {
    private static String TAG = "SplashActivity";
    private final MHandler handler = new MHandler(this);
    public int initialization_count = 0;
    public int complete_count = 0;
    public ViewGroup ad_view;
    private RequestRepositoryFactory requestRepositoryFactory;
    private List<Book> books;

    public static void checkAndInstallShotCut(Context ctt) {
        if (!queryShortCut(ctt)) {
            installShotCut(ctt);
        }
    }

    public static void installShotCut(Context ctt) {
        // deleteCallShortcut(ctt);
        Intent shortcut = new Intent(
                "com.android.launcher.action.INSTALL_SHORTCUT");

        // 不允许重建
        shortcut.putExtra("duplicate", false);

        // 设置名字
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME,
                ctt.getString(R.string.app_name));

        // 设置图标
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(ctt, R.drawable.icon));

        // 设置意图和快捷方式关联程序
        Intent intent = new Intent(ctt, ctt.getClass())
                .setAction(Intent.ACTION_MAIN);
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);

        // 发送广播
        ctt.sendBroadcast(shortcut);

    }

    public static boolean queryShortCut(Context ctt) {
        boolean isInstallShortcut = false;
        try {
            final ContentResolver cr = ctt.getContentResolver();
            final String AUTHORITY = "com.android.launcher2.settings";
            final String AUTHORITY2 = "com.android.launcher.settings";
            Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/favorites?notify=true");
            Cursor c = cr.query(CONTENT_URI, new String[]{"title", "iconResource"}, "title=?",
                    new String[]{"kdqbxs"}, null);// title表示应用名称。
            if (c != null && c.getCount() > 0) {
                isInstallShortcut = true;
            } else {
                CONTENT_URI = Uri.parse("content://" + AUTHORITY2 + "/favorites?notify=true");
                Cursor c2 = cr.query(CONTENT_URI, new String[]{"title", "iconResource"}, "title=?",
                        new String[]{ctt.getString(R.string.app_name)},
                        null);// title表示应用名称。
                if (c2 != null && c2.getCount() > 0) {
                    isInstallShortcut = true;
                }
            }
        } catch (Exception e) {
            AppLog.d(TAG, "queryShortCut error " + e);
            e.printStackTrace();
        }
        return isInstallShortcut;
    }

    private void initGuide() {
        final int versionCode = AppUtils.getVersionCode();
        boolean firstGuide = PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext()).getBoolean(versionCode + "first_guide",
                true);

        AppLog.e(TAG, "initGuide: " + firstGuide);
        Constants.is_wifi_auto_download = PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext()).getBoolean("auto_download_wifi",
                false);
        Constants.book_list_sort_type = PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext()).getInt("booklist_sort_type", 0);
        gotoActivity();
    }

    private void gotoActivity() {

        Intent intent = new Intent();
        intent.setClass(SplashActivity.this, HomeActivity.class);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        finish();
    }

    private void initShield() {
        ShieldManager shieldManager = new ShieldManager(getApplicationContext());
        shieldManager.startAchieveUserLocation();
    }

    @Override
    public void onCreate(Bundle paramBundle) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(paramBundle);

        if (!isTaskRoot()) {
            if (getIntent().hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(
                    getIntent().getAction())) {
                finish();
                return;
            }
        }

        requestRepositoryFactory = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext());

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);


        try {
            setContentView(R.layout.act_splash);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ad_view = findViewById(R.id.ad_view);
        complete_count = 0;
        initialization_count = 0;

        initializeDataFusion();

        startInitTask();
        // 安装快捷方式
        new InstallShotCutTask().execute();

        StatServiceUtils.statAppBtnClick(getApplication(), StatServiceUtils.app_start);
        if (UserManager.INSTANCE.isUserLogin()) {
            StatServiceUtils.statAppBtnClick(getApplication(), StatServiceUtils.user_login_succeed);
        }
    }

    private void requestWebViewConfig() {
        insertDisposable(RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).requestWebViewConfig()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(result -> {
                    if (result != null && result.checkResultAvailable()) {
                        String url = result.getData();
                        if (url != null && !url.isEmpty()) {

                            SPUtils.INSTANCE.insertPrivateSharedString(SPKey.WEB_VIEW_HOST, url);

                            WebResourceCache webResourceCache = WebResourceCache.Companion.loadWebResourceCache();
                            webResourceCache.checkLocalResourceFile(url);

                        }
                    }
                })
        );
    }

    private void initializeDataFusion() {

        books = requestRepositoryFactory.loadBooks();

        if (books != null) {
            for (Book book : books) {

                // 旧版本BookFix表等待目录修复的书迁移到book表
                BookFix bookFix = requestRepositoryFactory.loadBookFix(book.getBook_id());
                if (bookFix != null && bookFix.getFix_type() == 2
                        && bookFix.getList_version() > book.getList_version()) {
                    book.setList_version_fix(bookFix.getList_version());
                    requestRepositoryFactory.updateBook(book);
                    requestRepositoryFactory.deleteBookFix(book.getBook_id());
                }
            }

        }
    }


    private void startInitTask() {
        // 初始化任务
        InitTask initTask = new InitTask();
        initTask.execute();
    }


    private boolean isGo = true;

    private void initSplashAd() {
        handler.sendEmptyMessageDelayed(0, 1000);
    }

    //初始化广告开关
    private void initAdSwitch() {
        if (!Constants.dy_ad_switch) {
            Constants.isHideAD = true;
            return;
        }

        //判断是否展示广告
//        if (sharedPreUtil != null) {
        long limited_time = SPUtils.INSTANCE.getDefaultSharedLong(
                SPKey.AD_LIMIT_TIME_DAY, 0L);
        if (limited_time == 0) {
            limited_time = System.currentTimeMillis();
            try {
                SPUtils.INSTANCE.putDefaultSharedLong(SPKey.AD_LIMIT_TIME_DAY,
                        limited_time);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        AppLog.e(TAG, "Limited_Time : " + limited_time);
        AppLog.e(TAG, "Current_Time : " + System.currentTimeMillis());
        AppLog.e(TAG, "AD_Limited_day : " + Constants.ad_limit_time_day);

        int user_index = SPUtils.INSTANCE.getDefaultSharedInt(SPKey.USER_NEW_INDEX, 0);
        boolean init_ad = false;

        if (user_index == 0) {
            if (!SPUtils.INSTANCE.getDefaultSharedBoolean(SPKey.ADD_DEFAULT_BOOKS,
                    false)) {
                SPUtils.INSTANCE.putDefaultSharedInt(SPKey.USER_NEW_INDEX, 1);
                init_ad = true;
            } else {
                init_ad = false;
                //------------新壳没有广告写死为True--------------老壳请直接赋值为false!!!!
                Constants.isHideAD = !Constants.new_app_ad_switch;
            }
        } else if (user_index == 1) {
            if (SPUtils.INSTANCE.getDefaultSharedBoolean(SPKey.ADD_DEFAULT_BOOKS,
                    false)) {
                init_ad = true;
            }
        } else {
            init_ad = false;
            //------------新壳没有广告写死为True--------------老壳请直接赋值为false!!!!
            Constants.isHideAD = !Constants.new_app_ad_switch;
        }

        if (init_ad) {
            int ad_limit_time_day = SPUtils.INSTANCE.getDefaultSharedInt(
                    SPKey.USER_NEW_AD_LIMIT_DAY, 0);
            if (ad_limit_time_day == 0 || Constants.ad_limit_time_day != ad_limit_time_day) {
                ad_limit_time_day = Constants.ad_limit_time_day;
                SPUtils.INSTANCE.putDefaultSharedInt(SPKey.USER_NEW_AD_LIMIT_DAY,
                        ad_limit_time_day);
            }

            if (limited_time + (ad_limit_time_day * (Constants.DEVELOPER_MODE
                    ? Constants.read_rest_time : Constants.one_day_time)) > System
                    .currentTimeMillis()) {
                Constants.isHideAD = true;
            } else {
                SPUtils.INSTANCE.putDefaultSharedInt(SPKey.USER_NEW_INDEX, 2);
                //------------新壳没有广告写死为True--------------老壳请直接赋值为false!!!!
                Constants.isHideAD = !Constants.new_app_ad_switch;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }

    public class MHandler extends Handler {

        private WeakReference<SplashActivity> weakReference;

        MHandler(SplashActivity splashActivity) {
            weakReference = new WeakReference<>(splashActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SplashActivity splashActivity = weakReference.get();
            if (splashActivity == null) {
                return;
            }
            switch (msg.what) {
                case 0:
                    AppLog.e(TAG, "handler执行");
                    splashActivity.initGuide();
                    break;
                case 1:
                    if (isGo) {
                        AppLog.e(TAG, "handler执行111");
                        splashActivity.initGuide();
                    }
                    break;
            }
        }
    }

    public class InitTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            // 2 动态参数
            try {
                DynamicParameter dynamicParameter = new DynamicParameter(getApplicationContext());
                dynamicParameter.setDynamicParameter();
            } catch (Exception e) {
                e.printStackTrace();
            }

            boolean b = SPUtils.INSTANCE.getDefaultSharedBoolean(Constants.UPDATE_CHAPTER_SOURCE_ID,
                    false);

            if (!b) {
                List<Book> bookOnlineList = requestRepositoryFactory.loadBooks();
                if (bookOnlineList != null && bookOnlineList.size() > 0) {
                    for (int i = 0; i < bookOnlineList.size(); i++) {
                        Book iBook = bookOnlineList.get(i);
                        if (!TextUtils.isEmpty(iBook.getBook_id())) {
                            Chapter lastChapter = requestRepositoryFactory.queryLastChapter(
                                    iBook.getBook_id());
                            if (lastChapter != null) {
                                lastChapter.setBook_source_id(iBook.getBook_source_id());
                                requestRepositoryFactory.updateChapterBySequence(iBook.getBook_id(),
                                        lastChapter);
                            }
                        }
                    }
                }
                PreferenceManager.getDefaultSharedPreferences(
                        getApplicationContext()).edit().putBoolean(
                        Constants.UPDATE_CHAPTER_SOURCE_ID, true).apply();
            }


            //请求广告
            initAdSwitch();
            // 5 初始化屏蔽
            try {
                initShield();
            } catch (Exception e) {
                e.printStackTrace();
            }

            initSplashAd();

            // 6 其他信息初始化
            try {
                // 统计阅读章节数
                if (Constants.readedCount == 0) {
                    Constants.readedCount = SPUtils.INSTANCE.getDefaultSharedInt(
                            SPKey.READED_CONT, 0);
                }

                //
                DisplayMetrics dm = new DisplayMetrics();
                SplashActivity.this.getWindowManager().getDefaultDisplay().getMetrics(dm);
                SPUtils.INSTANCE.putDefaultSharedInt(SPKey.SCREEN_WIDTH, dm.widthPixels);
                SPUtils.INSTANCE.putDefaultSharedInt(SPKey.SCREEN_HEIGHT, dm.heightPixels);
                AppUtils.initDensity(getApplicationContext());

                // 判断是否小说推送，检查小说是否更新
                boolean isStarPush = SPUtils.INSTANCE.getDefaultSharedBoolean(
                        SPKey.SETTINGS_PUSH, true);
                if (isStarPush) {
                    CheckNovelUpdateService.startChkUpdService(getApplicationContext());
                }

            } catch (Exception exception) {
                exception.printStackTrace();
            }

            //开启缓存服务
            CacheManager.INSTANCE.checkService();

            requestWebViewConfig();

            return null;
        }
    }

    // 安装快捷方式任务
    class InstallShotCutTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            boolean create = SPUtils.INSTANCE.getDefaultSharedBoolean(SPKey.CREATE_SHOTCUT,
                    false);
            if (!create) {
                checkAndInstallShotCut(SplashActivity.this);
                SPUtils.INSTANCE.putDefaultSharedBoolean(SPKey.CREATE_SHOTCUT, true);
            }
            return null;
        }

    }

    @Override
    public boolean shouldShowNightShadow() {
        return false;
    }

    @Override
    public boolean supportSlideBack() {
        return false;
    }
}