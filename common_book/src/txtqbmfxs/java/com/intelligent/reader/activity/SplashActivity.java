package com.intelligent.reader.activity;

import static android.view.KeyEvent.KEYCODE_BACK;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.ding.basic.bean.Book;
import com.ding.basic.bean.Chapter;
import com.ding.basic.repository.RequestRepositoryFactory;
import com.dingyue.contract.router.RouterConfig;
import com.dingyue.contract.util.SharedPreUtil;
import com.dy.media.MediaCode;
import com.dy.media.MediaControl;
import com.dy.media.MediaLifecycle;
import com.intelligent.reader.R;
import com.intelligent.reader.app.BookApplication;
import com.intelligent.reader.util.DynamicParamter;
import com.intelligent.reader.util.GenderHelper;
import com.intelligent.reader.util.ShieldManager;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.component.service.CheckNovelUpdateService;
import net.lzbook.kit.book.download.CacheManager;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.db.help.ChapterDaoHelper;
import net.lzbook.kit.user.UserManager;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.StatServiceUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import iyouqu.theme.FrameActivity;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

@Route(path = RouterConfig.SPLASH_ACTIVITY)
public class SplashActivity extends FrameActivity implements GenderHelper.onGenderSelectedListener {
    private final MHandler handler = new MHandler(this);
    private SharedPreUtil sharedPreUtil;
    public int initialization_count = 0;
    public int complete_count = 0;
    public ViewGroup ad_view;
    private boolean isIniting;

    // 开屏选男女
    private boolean mStepInFlag;

    @Override
    public void genderSelected() {
        mStepInFlag = true;
        initData();
    }

    static class MHandler extends Handler {

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
                    AppLog.e("handler执行");
                    splashActivity.initGuide();
                    break;
            }
        }

    }

    private boolean initChooseGender() {
        AppUtils.initDensity(getApplicationContext());
        int isChooseGender = PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext()).getInt("gender",
                Constants.NONE);
        return isChooseGender == Constants.NONE;
    }

    private void initGuide() {
        final int versionCode = AppUtils.getVersionCode();
        boolean firstGuide = PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext()).getBoolean(versionCode + "first_guide",
                true);

        AppLog.e("initGuide: " + firstGuide);
        Constants.is_wifi_auto_download = PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext()).getBoolean("auto_download_wifi",
                false);
        Constants.book_list_sort_type = PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext()).getInt("booklist_sort_type", 0);
        gotoActivity(versionCode, firstGuide);
    }

    @Override
    public void onCreate(Bundle paramBundle) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(paramBundle);
        //防止从应用市场启动时从点击home后再打开应用重启的问题
        if (!isTaskRoot()) {
            if (getIntent().hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(
                    getIntent().getAction())) {
                finish();
                return;
            }
        }

        //隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 仅支持Api为19以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }


        try {
            setContentView(R.layout.act_splash);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ad_view = findViewById(R.id.ad_view);

        if (initChooseGender()) {
            FrameLayout frameLayout = findViewById(R.id.content_frame);
            View view = LayoutInflater.from(this).inflate(R.layout.gender_splash, null);
            if (view != null) {
                frameLayout.addView(view);
                final GenderHelper genderHelper = new GenderHelper(view);
                genderHelper.setOnGenderSelectedListener(SplashActivity.this);
                final TextView tvStepIn = view.findViewById(R.id.tv_step_in);
                tvStepIn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tvStepIn.setText("努力加载中...");
                        tvStepIn.setClickable(false);
                        genderHelper.jumpAnimation();
                        mStepInFlag = true;
                        Constants.SGENDER = Constants.SDEFAULT;
                        initData();
                    }
                });
            } else {
                mStepInFlag = true;
                initData();
            }
        } else {
            initData();
        }
    }

    private void initData() {
        if (isIniting) {
            return;
        }
        isIniting = true;
        complete_count = 0;
        initialization_count = 0;
        Context context = this;
        if (sharedPreUtil == null) {
            sharedPreUtil = new SharedPreUtil(SharedPreUtil.Companion.getSHARE_DEFAULT());
        }

        // 初始化任务
        InitTask initTask = new InitTask();
        initTask.execute();
        // 安装快捷方式
        new InstallShotCutTask().execute();

        StatServiceUtils.statAppBtnClick(context, StatServiceUtils.app_start);
        if (UserManager.INSTANCE.isUserLogin()) {
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.user_login_succeed);
        }
    }

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
            if (c == null) return false;
            if (c.getCount() > 0) {
                isInstallShortcut = true;
            } else {
                CONTENT_URI = Uri.parse("content://" + AUTHORITY2 + "/favorites?notify=true");
                Cursor c2 = cr.query(CONTENT_URI, new String[]{"title", "iconResource"}, "title=?",
                        new String[]{ctt.getString(R.string.app_name)},
                        null);// title表示应用名称。
                if (c2 == null) return false;
                if (c2.getCount() > 0) {
                    isInstallShortcut = true;
                }
                c2.close();
            }
            c.close();
        } catch (Exception e) {
            AppLog.d("queryShortCut error " + e);
            e.printStackTrace();
        }
        return isInstallShortcut;
    }

    private void gotoActivity(int versionCode, boolean firstGuide) {
        if (firstGuide) {
            Editor editor = PreferenceManager.getDefaultSharedPreferences(
                    getApplicationContext()).edit();
            editor.putBoolean(versionCode + "first_guide", false);
            editor.apply();
            editor.putString(Constants.SERARCH_HOT_WORD_YOUHUA,
                    getResources().getString(R.string.hotwords_local));
            editor.apply();

            try {
                Intent intent = new Intent();
                intent.setClass(SplashActivity.this, HomeActivity.class);
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
        ShieldManager shieldManager = new ShieldManager(getApplicationContext(), sharedPreUtil);
        shieldManager.startAchieveUserLocation();
    }


    /**
     * 初始化任务
     */
    public class InitTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {


            // 2 动态参数
            try {
                DynamicParamter dynamicParameter = new DynamicParamter(getApplicationContext());
                dynamicParameter.setDynamicParamter();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (sharedPreUtil == null) {
                sharedPreUtil = new SharedPreUtil(SharedPreUtil.Companion.getSHARE_DEFAULT());
            }

            boolean b = sharedPreUtil.getBoolean(Constants.UPDATE_CHAPTER_SOURCE_ID, false);

            if (!b) {
                List<Book> bookOnlineList =
                        RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                                BaseBookApplication.getGlobalContext()).loadBooks();
                if (bookOnlineList != null && bookOnlineList.size() > 0) {
                    for (int i = 0; i < bookOnlineList.size(); i++) {
                        Book iBook = bookOnlineList.get(i);
                        if (!TextUtils.isEmpty(iBook.getBook_id())) {
                            ChapterDaoHelper bookChapterDao =
                                    ChapterDaoHelper.Companion.loadChapterDataProviderHelper(
                                            BookApplication.getGlobalContext(), iBook.getBook_id());
                            Chapter lastChapter = bookChapterDao.queryLastChapter();
                            if (lastChapter != null) {
                                lastChapter.setBook_source_id(iBook.getBook_source_id());
                                bookChapterDao.updateChapterBySequence(lastChapter);
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
                    Constants.readedCount = sharedPreUtil.getInt(
                            SharedPreUtil.Companion.getREADED_CONT());
                }

                //
                DisplayMetrics dm = new DisplayMetrics();
                SplashActivity.this.getWindowManager().getDefaultDisplay().getMetrics(dm);
                sharedPreUtil.putInt(SharedPreUtil.Companion.getSCREEN_WIDTH(), dm.widthPixels);
                sharedPreUtil.putInt(SharedPreUtil.Companion.getSCREEN_HEIGHT(), dm.heightPixels);
                AppUtils.initDensity(getApplicationContext());

                // 判断是否小说推送，检查小说是否更新
                boolean isStarPush = sharedPreUtil.getBoolean(
                        SharedPreUtil.Companion.getSETTINGS_PUSH(), true);
                if (isStarPush) {
                    CheckNovelUpdateService.startChkUpdService(getApplicationContext());
                }

            } catch (Exception exception) {
                exception.printStackTrace();
            }

            //开屏选男女
            if (mStepInFlag) {
                Map<String, String> gender = new HashMap<>();
                gender.put("type", String.valueOf(Constants.SGENDER));
                StartLogClickUtil.upLoadEventLog(SplashActivity.this, StartLogClickUtil.SYSTEM_PAGE,
                        StartLogClickUtil.PREFERENCE, gender);
            }

            //开启缓存服务
            CacheManager.INSTANCE.checkService();

            return null;
        }
    }


    /**
     * 安装快捷方式任务
     */
    class InstallShotCutTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            if (sharedPreUtil == null) {
                sharedPreUtil = new SharedPreUtil(SharedPreUtil.Companion.getSHARE_DEFAULT());
            }
            boolean create = sharedPreUtil.getBoolean(SharedPreUtil.Companion.getCREATE_SHOTCUT(),
                    false);
            if (!create) {
                checkAndInstallShotCut(SplashActivity.this);
                sharedPreUtil.putBoolean(SharedPreUtil.Companion.getCREATE_SHOTCUT(), true);
            }
            return null;
        }

    }


    /**
     * 初始化广告开关
     */
    private void initAdSwitch() {
        if (!Constants.dy_ad_switch) {
            Constants.isHideAD = true;
            return;
        }

        //判断是否展示广告
        if (sharedPreUtil != null) {
            long limited_time = sharedPreUtil.getLong(
                    SharedPreUtil.Companion.getAD_LIMIT_TIME_DAY(), 0L);
            if (limited_time == 0) {
                limited_time = System.currentTimeMillis();
                try {
                    sharedPreUtil.putLong(SharedPreUtil.Companion.getAD_LIMIT_TIME_DAY(),
                            limited_time);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            AppLog.e(TAG, "Limited_Time : " + limited_time);
            AppLog.e(TAG, "Current_Time : " + System.currentTimeMillis());
            AppLog.e(TAG, "AD_Limited_day : " + Constants.ad_limit_time_day);

            int user_index = sharedPreUtil.getInt(SharedPreUtil.Companion.getUSER_NEW_INDEX(), 0);
            boolean init_ad = false;

            /*
             * FIXME  user_index
             * 0: 新用户：无广告
             * 1：新用户：两天内无广告
             * 2：老用户：显示广告
             */
            switch (user_index) {
                case 0:
                    if (!sharedPreUtil.getBoolean(SharedPreUtil.Companion.getADD_DEFAULT_BOOKS(),
                            false)) {
                        sharedPreUtil.putInt(SharedPreUtil.Companion.getUSER_NEW_INDEX(), 1);
                        init_ad = true;
                    } else {
                        init_ad = false;
                        //------------新壳没有广告写死为True--------------老壳请直接赋值为false!!!!
                        Constants.isHideAD = !Constants.new_app_ad_switch;
                    }
                    break;
                case 1:
                    if (sharedPreUtil.getBoolean(SharedPreUtil.Companion.getADD_DEFAULT_BOOKS(),
                            false)) {
                        init_ad = true;
                    }
                    break;
                case 2:
                    init_ad = false;
                    //------------新壳没有广告写死为True--------------老壳请直接赋值为false!!!!
                    Constants.isHideAD = !Constants.new_app_ad_switch;
                    break;
            }

            if (init_ad) {
                int ad_limit_time_day = sharedPreUtil.getInt(
                        SharedPreUtil.Companion.getUSER_NEW_AD_LIMIT_DAY(), 0);
                if (ad_limit_time_day == 0 || Constants.ad_limit_time_day != ad_limit_time_day) {
                    ad_limit_time_day = Constants.ad_limit_time_day;
                    sharedPreUtil.putInt(SharedPreUtil.Companion.getUSER_NEW_AD_LIMIT_DAY(),
                            ad_limit_time_day);
                }

                if (limited_time + (ad_limit_time_day * (Constants.DEVELOPER_MODE
                        ? Constants.read_rest_time : Constants.one_day_time)) > System
                        .currentTimeMillis()) {
                    Constants.isHideAD = true;
                } else {
                    sharedPreUtil.putInt(SharedPreUtil.Companion.getUSER_NEW_INDEX(), 2);
                    //------------新壳没有广告写死为True--------------老壳请直接赋值为false!!!!
                    Constants.isHideAD = !Constants.new_app_ad_switch;
                }
            }
        } else {
            //------------新壳没有广告写死为True--------------老壳请直接赋值为false!!!!
            Constants.isHideAD = !Constants.new_app_ad_switch;
        }
    }

    /**
     * 初始化启动页广告
     */
    private void initSplashAd() {
        if (ad_view == null) return;
        if (Constants.isHideAD) {
            AppLog.e(TAG, "Limited AD display!");
            handler.sendEmptyMessage(0);
            return;
        }

        MediaControl.INSTANCE.loadSplashMedia(this, ad_view, new Function1<Integer, Unit>() {
            @Override
            public Unit invoke(Integer resultCode) {
                switch (resultCode) {
                    case MediaCode.MEDIA_SUCCESS: //广告请求成功
                        AppLog.e("time");
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


    @Override
    public boolean shouldShowNightShadow() {
        return false;
    }

    @Override
    public boolean supportSlideBack() {
        return false;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KEYCODE_BACK || super.onKeyDown(keyCode, event);
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

        MediaLifecycle.INSTANCE.onDestroy();

        super.onDestroy();
    }
}