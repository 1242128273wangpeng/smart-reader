package com.intelligent.reader.activity;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.ding.basic.RequestRepositoryFactory;
import com.ding.basic.bean.Book;
import com.ding.basic.bean.BookFix;
import com.ding.basic.bean.Chapter;
import com.ding.basic.config.ParameterConfig;
import com.ding.basic.net.RequestSubscriber;
import com.ding.basic.util.sp.SPKey;
import com.ding.basic.util.sp.SPUtils;
import com.dy.media.MediaCode;
import com.dy.media.MediaControl;
import com.dy.media.MediaLifecycle;
import com.google.gson.Gson;
import com.intelligent.reader.BuildConfig;
import com.intelligent.reader.R;
import com.intelligent.reader.util.GenderHelper;
import com.orhanobut.logger.Logger;

import net.lzbook.kit.app.base.BaseBookApplication;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.service.CheckNovelUpdateService;
import net.lzbook.kit.ui.activity.GuideActivity;
import net.lzbook.kit.ui.activity.base.FrameActivity;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.ShieldManager;
import net.lzbook.kit.utils.StatServiceUtils;
import net.lzbook.kit.utils.download.CacheManager;
import net.lzbook.kit.utils.dynamic.DynamicParameter;
import net.lzbook.kit.utils.logger.AppLog;
import net.lzbook.kit.utils.router.RouterConfig;
import net.lzbook.kit.utils.user.UserManager;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import static android.view.KeyEvent.KEYCODE_BACK;

@Route(path = RouterConfig.SPLASH_ACTIVITY)
public class SplashActivity extends FrameActivity implements GenderHelper.GenderSelectedListener {
    private static String TAG = "SplashActivity";
    private final MHandler handler = new MHandler(this);
    public int initialization_count = 0;
    public int complete_count = 0;
    public ViewGroup ad_view;

    private TextView txt_upgrade;
    private ProgressBar progress_upgrade;
    private List<Book> books;
    private RequestRepositoryFactory requestFactory;

    private boolean isIniting;
    // 开屏选男女
    private boolean mStepInFlag;

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
        gotoActivity(versionCode, false);
    }

    private void gotoActivity(int versionCode, boolean firstGuide) {
        if (firstGuide) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
                    getApplicationContext()).edit();
            editor.putBoolean(versionCode + "first_guide", false);
            editor.apply();
            try {
                Intent intent = new Intent();
                intent.setClass(SplashActivity.this, GuideActivity.class);
                intent.putExtra("fromA", "Loading");
                startActivity(intent);
                finish();
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        } else {
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
        requestFactory = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
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

        requestFactory.requestAuthAccess(null);

        String bookDBName = ReplaceConstants.getReplaceConstants().DATABASE_NAME;
        File bookDBFile = getDatabasePath(bookDBName);

        String[] databaseList = databaseList();
        List<String> chapterDBList = new ArrayList<>();
        for (String name : databaseList) {
            if (name.startsWith("book_chapter_") && !name.contains("journal")) {
                chapterDBList.add(name.replace("book_chapter_", ""));
            }
        }

        if (bookDBFile.exists()) {
            setContentView(R.layout.act_splash_upgradedb);
            TextView txt_name = findViewById(R.id.txt_name);
            txt_name.setText(R.string.app_name);
            upgradeBookDB(bookDBName, chapterDBList);
        } else {
            initGenderOrData();
        }
    }

    private void onUpgradeProgress(int progress) {
        if (txt_upgrade == null) {
            txt_upgrade = findViewById(R.id.txt_upgrade);
        }
        if (progress_upgrade == null) {
            progress_upgrade = findViewById(R.id.progress_upgrade);
            progress_upgrade.setMax(100);
        }

        if (NetWorkUtils.getNetWorkType(this) == NetWorkUtils.NETWORK_NONE) {
            txt_upgrade.setText(getString(R.string.db_upgrade_nonet, progress));
        } else {
            txt_upgrade.setText(getString(R.string.db_upgrade_hasnet, progress));
        }
        progress_upgrade.setProgress(progress);
    }

    private void upgradeBookDB(String bookDBName, final List<String> chapterDBList) {
        float percent = 1;
        if (!chapterDBList.isEmpty()) {
            percent = 0.2F;
        }

        final float weight = percent;

        requestFactory.upgradeBookDBFromOld(bookDBName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        onUpgradeProgress((int) (integer * weight));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        if (BuildConfig.CHANNEL_NAME.equals("DEBUG")) {
                            Toast.makeText(SplashActivity.this, "upgradeBookDB error \r\n"
                                    + Log.getStackTraceString(throwable), Toast.LENGTH_LONG).show();
                            for (int i = 0; i < 1000; i++) {
                                Toast.makeText(SplashActivity.this, "升级数据库失败, 请把手机给开发同学!!!",
                                        Toast.LENGTH_LONG).show();
                            }
                            //拷贝旧的数据到sdcard, 给开发小伙伴
                            copyOldDB2SD();
                        }

                        Map<String, String> data = new HashMap<>();
                        data.put("status", "2");
                        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext()
                                , StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.UPDATE, data);

                        deleteOldDB();
                        initGenderOrData();
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        upgradeChapterDB(chapterDBList, 1 - weight);
                    }
                });
    }

    private void upgradeChapterDB(List<String> chapterDBList, final Float weight) {
        if (!chapterDBList.isEmpty()) {
           requestFactory.upgradeChapterDBFromOld(chapterDBList)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Integer>() {
                        @Override
                        public void accept(Integer integer) throws Exception {
                            onUpgradeProgress((int) (100 * (1 - weight) + integer * weight));
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            throwable.printStackTrace();
                            if (BuildConfig.CHANNEL_NAME.equals("DEBUG")) {
                                Toast.makeText(SplashActivity.this, "upgradeChapterDB error \r\n"
                                                + Log.getStackTraceString(throwable),
                                        Toast.LENGTH_LONG).show();
                                for (int i = 0; i < 1000; i++) {
                                    Toast.makeText(SplashActivity.this, "升级数据库失败, 请把手机给开发同学!!!",
                                            Toast.LENGTH_LONG).show();
                                }
                                //拷贝旧的数据到sdcard, 给开发小伙伴
                                copyOldDB2SD();
                            }
                            Map<String, String> data = new HashMap<>();
                            data.put("status", "2");
                            StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext()
                                    , StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.UPDATE,
                                    data);
                            //删除之前的数据库
                            deleteOldDB();
                            initGenderOrData();
                        }
                    }, new Action() {
                        @Override
                        public void run() throws Exception {
                            Map<String, String> data = new HashMap<>();
                            data.put("status", "1");
                            StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext()
                                    , StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.UPDATE,
                                    data);
                            //删除之前的数据库
                            deleteOldDB();
                            initGenderOrData();
                        }
                    });
        } else {
            Map<String, String> data = new HashMap<>();
            data.put("status", "1");
            StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext()
                    , StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.UPDATE, data);
            //删除之前的数据库
            deleteOldDB();
            initGenderOrData();
        }
    }

    private void copyOldDB2SD() {
        String[] strings = databaseList();
        new File("/sdcard/novel").mkdirs();
        for (String db : strings) {
            kotlin.io.FilesKt.copyTo(getDatabasePath(db)
                    , new File("/sdcard/novel/" + db),
                    true
                    , 64 * 1024
            );
        }
    }

    private void deleteOldDB() {
        //TODO 测试数据库升级时, 注释掉方法体
        String[] strings = databaseList();

        for (String name : strings) {
            if (name.startsWith("book_chapter_")) {
                deleteDatabase(name);
            }
        }

        deleteDatabase(ReplaceConstants.getReplaceConstants().DATABASE_NAME);
    }

    private void doOnCreate() {
        try {
            setContentView(R.layout.act_splash);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ad_view = findViewById(R.id.ad_view);
        isIniting = true;
        complete_count = 0;
        initialization_count = 0;

        updateBookLastChapter();

        initializeDataFusion();

        checkBookChapterCount();

        // 安装快捷方式
        new InstallShotCutTask().execute();

        StatServiceUtils.statAppBtnClick(getApplication(), StatServiceUtils.app_start);
        if (UserManager.INSTANCE.isUserLogin()) {
            StatServiceUtils.statAppBtnClick(getApplication(), StatServiceUtils.user_login_succeed);
        }
    }

    private void initializeDataFusion() {

        books = requestFactory.loadBooks();

        if (books != null) {

            List<Book> upBooks = new ArrayList<>();

            for (Book book : books) {
                if (TextUtils.isEmpty(book.getBook_chapter_id())) {
                    upBooks.add(book);
                }

                // 旧版本BookFix表等待目录修复的书迁移到book表
                BookFix bookFix = requestFactory.loadBookFix(book.getBook_id());
                if (bookFix != null && bookFix.getFix_type() == 2 && bookFix.getList_version() > book.getList_version()) {
                    book.setList_version_fix(bookFix.getList_version());
                    requestFactory.updateBook(book);
                    requestFactory.deleteBookFix(book.getBook_id());
                }
            }

            if (upBooks.isEmpty()) {
                Logger.e("开屏页更新书籍book_chapter_id等信息的接口不执行,书架没有书籍要修复 ");

                startInitTask();
                return;
            }

            Gson gson = new Gson();

            Logger.i("initializeDataFusion Json: " + gson.toJson(upBooks));

            RequestBody checkBody = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8")
                    , gson.toJson(upBooks));

            requestFactory.requestBookShelfUpdate(checkBody, new RequestSubscriber<Boolean>() {
                        @Override
                        public void requestResult(Boolean result) {
                            if (result) {
                                Logger.i("数据融合，书架信息升级成功！");
                            } else {
                                Logger.e("数据融合，书架信息升级异常！");
                            }
                            startInitTask();
                        }

                        @Override
                        public void requestError(@NotNull String message) {
                            Logger.e("数据融合，书架信息升级异常！");
                            startInitTask();
                        }
                    });
        } else {
            startInitTask();
        }
    }

    private void startInitTask() {
        // 初始化任务
        InitTask initTask = new InitTask();
        initTask.execute();
    }


    /**
     * 检查章节数是否为0
     * 解决阅读进度不更新的问题
     */
    private void checkBookChapterCount(){

        boolean isCheckChapterCount = SPUtils.INSTANCE.getDefaultSharedBoolean(SPKey.CHECK_CHAPTER_COUNT, false);

        if (!isCheckChapterCount) {

            List<Book> bookList = requestFactory.loadBooks();

            if (bookList != null && bookList.size() > 0) {
                for (Book book : bookList) {

                    if (book.getChapter_count() <= 0 && !TextUtils.isEmpty(book.getBook_id())) {
                        int chapterCount = requestFactory.getCount(book.getBook_id());
                        book.setChapter_count(chapterCount);

                        requestFactory.updateBook(book);
                    }
                }
            }
            SPUtils.INSTANCE.putDefaultSharedBoolean(SPKey.CHECK_CHAPTER_COUNT, true);
        }

    }

    /***
     * 数据融合二期修改缓存逻辑，升级时同步本地最新章节信息到Book表
     * 只针对全本追书阅读器
     * **/
    private void updateBookLastChapter() {

        boolean isDataBaseRemark = SPUtils.INSTANCE.getDefaultSharedBoolean(
                SPKey.Companion.getDATABASE_REMARK(), false);

        if (!isDataBaseRemark) {

            List<Book> bookList = requestFactory.loadBooks();

            if (bookList != null && bookList.size() > 0) {

                for (Book book : bookList) {

                    Chapter lastChapter = requestFactory.queryLastChapter(book.getBook_id());

                    if (lastChapter != null && !TextUtils.isEmpty(lastChapter.getChapter_id())) {
                        book.setLast_chapter(lastChapter);

                        requestFactory.updateBook(book);
                    }
                }
            }
            SPUtils.INSTANCE.putDefaultSharedBoolean(SPKey.Companion.getDATABASE_REMARK(), true);
        }
    }

    private boolean isGo = true;

    private void initSplashAd() {
        if (ad_view == null) return;
        if (Constants.isHideAD) {
            AppLog.e(TAG, "Limited AD display!");
            handler.sendEmptyMessage(0);
            return;
        }
        if (isGo) {
            handler.sendEmptyMessageDelayed(1, 3000);
        }
        MediaControl.INSTANCE.loadSplashMedia(this, ad_view, new Function1<Integer, Unit>() {
            @Override
            public Unit invoke(Integer resultCode) {
                switch (resultCode) {
                    case MediaCode.MEDIA_SUCCESS: //广告请求成功
                        isGo = false;
                        AppLog.e(TAG, "time");
                        break;
                    case MediaCode.MEDIA_FAILED: //广告请求失败
                        handler.sendEmptyMessage(0);
                        break;
                    case MediaCode.MEDIA_DISMISS: //开屏页面关闭
                        handler.sendEmptyMessage(0);
                        break;
                    case MediaCode.MEDIA_DISABLE: //无开屏广告
                        handler.sendEmptyMessage(0);
                        break;
                }
                return null;
            }
        });
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

             /*
             * FIXME  user_index
             * 0: 新用户：无广告
             * 1：新用户：两天内无广告
             * 2：老用户：显示广告
             */
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
                    if (Constants.new_app_ad_switch) {
                        Constants.isHideAD = false;
                    } else {
                        Constants.isHideAD = true;
                    }
                }
            } else if (user_index == 1) {
                if (SPUtils.INSTANCE.getDefaultSharedBoolean(SPKey.ADD_DEFAULT_BOOKS,
                        false)) {
                    init_ad = true;
                }
            } else {
                init_ad = false;
                //------------新壳没有广告写死为True--------------老壳请直接赋值为false!!!!
                if (Constants.new_app_ad_switch) {
                    Constants.isHideAD = false;
                } else {
                    Constants.isHideAD = true;
                }
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
                    if (Constants.new_app_ad_switch) {
                        Constants.isHideAD = false;
                    } else {
                        Constants.isHideAD = true;
                    }
                }
            }
//        } else {
//            //------------新壳没有广告写死为True--------------老壳请直接赋值为false!!!!
//            if (Constants.new_app_ad_switch) {
//                Constants.isHideAD = false;
//            } else {
//                Constants.isHideAD = true;
//            }
//        }
        //强制关闭广告
//        Constants.isHideAD = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        MediaLifecycle.INSTANCE.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MediaLifecycle.INSTANCE.onPause();
    }

    @Override
    protected void onDestroy() {

        try {
            handler.removeCallbacksAndMessages(null);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        MediaLifecycle.INSTANCE.onDestroy();

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

            boolean b = SPUtils.INSTANCE.getDefaultSharedBoolean(Constants.UPDATE_CHAPTER_SOURCE_ID, false);

            if (!b) {
                List<Book> bookOnlineList = requestFactory.loadBooks();
                if (bookOnlineList != null && bookOnlineList.size() > 0) {
                    for (int i = 0; i < bookOnlineList.size(); i++) {
                        Book iBook = bookOnlineList.get(i);
                        if (!TextUtils.isEmpty(iBook.getBook_id())) {
                            Chapter lastChapter = requestFactory.queryLastChapter(iBook.getBook_id());
                            if (lastChapter != null) {
                                lastChapter.setBook_source_id(iBook.getBook_source_id());
                                requestFactory.updateChapterBySequence(iBook.getBook_id(), lastChapter);
                            }
                        }
                    }
                }
                PreferenceManager.getDefaultSharedPreferences(
                        getApplicationContext()).edit().putBoolean(
                        Constants.UPDATE_CHAPTER_SOURCE_ID, true).apply();
            }

            UserManager.INSTANCE.initPlatform(SplashActivity.this, null);

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
                            SPKey.READED_CONT,0);
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

    private void initGenderOrData() {
        if (initChooseGender()) {
            FrameLayout frameLayout = findViewById(R.id.content_frame);
            frameLayout.removeAllViews();
            View view = LayoutInflater.from(this).inflate(R.layout.view_splash_gender, null);
            if (view != null) {
                frameLayout.addView(view);
                final GenderHelper genderHelper = new GenderHelper(view);
                genderHelper.insertGenderSelectedListener(SplashActivity.this);
                final TextView txt_gender_skip = view.findViewById(R.id.txt_gender_skip);
                txt_gender_skip.setOnClickListener(v -> {

                    Map<String, String> data = new HashMap<>();
                    data.put("type", "0");
                    StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                            StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.PREFERENCE, data);

                    txt_gender_skip.setText("努力加载中...");
                    txt_gender_skip.setClickable(false);
                    genderHelper.jumpAnimation();
                    SPUtils.INSTANCE.putDefaultSharedInt(SPKey.GENDER_TAG, Constants.SDEFAULT);
                    mStepInFlag = true;
                    ParameterConfig.INSTANCE.setGENDER_TYPE(
                            ParameterConfig.INSTANCE.getGENDER_DEFAULT());
                    doOnCreate();
                });
            } else {
                mStepInFlag = true;
                doOnCreate();
            }
        } else {
            doOnCreate();
        }
    }

    private boolean initChooseGender() {
        AppUtils.initDensity(getApplicationContext());
        ParameterConfig.INSTANCE.setGENDER_TYPE(SPUtils.INSTANCE.getDefaultSharedInt(SPKey.GENDER_TAG,
                ParameterConfig.INSTANCE.getGENDER_NONE()));
        return ParameterConfig.INSTANCE.getGENDER_TYPE()
                == ParameterConfig.INSTANCE.getGENDER_NONE();
    }

    @Override
    public void genderSelected() {
        mStepInFlag = true;
        doOnCreate();
    }
}