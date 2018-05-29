package com.intelligent.reader.activity;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.dingyue.contract.util.SharedPreUtil;
import com.intelligent.reader.R;
import com.intelligent.reader.app.BookApplication;
import com.intelligent.reader.util.DynamicParamter;

import net.lzbook.kit.book.component.service.CheckNovelUpdateService;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.db.BookChapterDao;
import net.lzbook.kit.data.db.BookDaoHelper;
import com.dingyue.contract.router.RouterConfig;
import net.lzbook.kit.user.UserManager;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.ShieldManager;
import net.lzbook.kit.utils.StatServiceUtils;

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

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import iyouqu.theme.FrameActivity;

import static android.view.KeyEvent.KEYCODE_BACK;

@Route(path = RouterConfig.SPLASH_ACTIVITY)
public class SplashActivity extends FrameActivity {
    private static String TAG = "SplashActivity";
    private final MHandler handler = new MHandler(this);
    public int initialization_count = 0;
    public int complete_count = 0;
    public ViewGroup ad_view;
    private SharedPreUtil sharedPreUtil;

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
            Cursor c = cr.query(CONTENT_URI, new String[]{"title", "iconResource"}, "title=?", new String[]{"kdqbxs"}, null);// title表示应用名称。
            if (c != null && c.getCount() > 0) {
                isInstallShortcut = true;
            } else {
                CONTENT_URI = Uri.parse("content://" + AUTHORITY2 + "/favorites?notify=true");
                Cursor c2 = cr.query(CONTENT_URI, new String[]{"title", "iconResource"}, "title=?", new String[]{ctt.getString(R.string.app_name)},
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
        boolean firstGuide = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(versionCode + "first_guide",
                true);

        AppLog.e(TAG, "initGuide: " + firstGuide);
        Constants.is_wifi_auto_download = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("auto_download_wifi",
                false);
        Constants.book_list_sort_type = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("booklist_sort_type", 0);
        gotoActivity(versionCode, firstGuide);
    }

    private void gotoActivity(int versionCode, boolean firstGuide) {

//        //2017.8.25阅读页改版上线时,因缺少文案,除五步替壳外.其他壳临时限制不显示开屏引导
//        if (firstGuide && !"cc.kdqbxs.reader".equals(AppUtils.getPackageName())) {
//            firstGuide = false;
//            Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
//            editor.putBoolean(versionCode + "first_guide", false);
//            editor.apply();
//        }

//        if (firstGuide) {
//            Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
//            editor.putBoolean(versionCode + "first_guide", false);
//            editor.apply();
//
//            try {
//                Intent intent = new Intent();
//                intent.setClass(SplashActivity.this, GuideActivity.class);
//                intent.putExtra("fromA", "Loading");
//                startActivity(intent);
//                finish();
//            } catch (ActivityNotFoundException e) {
//                e.printStackTrace();
//            } catch (SecurityException e) {
//                e.printStackTrace();
//            }
//        } else {
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
//        }
    }

//    private void initBook() {
//        LoadDataManager loadDataManager = new LoadDataManager(getApplicationContext());
//        if (!sharePreferenceUtils.getBoolean(Constants.ADD_DEFAULT_BOOKS)) {
//            // 首次安装新用户添加默认书籍
//            loadDataManager.addDefaultBooks();
//        }
//    }

    private void initShield() {
        ShieldManager shieldManager = new ShieldManager(getApplicationContext(), sharedPreUtil);
        shieldManager.startAchieveUserLocation();
    }

    @Override
    public void onCreate(Bundle paramBundle) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(paramBundle);

        if (!isTaskRoot()) {
            if (getIntent().hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(getIntent().getAction())) {
                finish();
                return;
            }
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        try {
            setContentView(R.layout.act_splash);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ad_view = (ViewGroup) findViewById(R.id.ad_view);
        complete_count = 0;
        initialization_count = 0;
        AppLog.e("oncreat", "oncreat go");
        sharedPreUtil = new SharedPreUtil(SharedPreUtil.Companion.getSHARE_DEFAULT());

        // 初始化任务
        InitTask initTask = new InitTask();
        initTask.execute();

        // 安装快捷方式
        new InstallShotCutTask().execute();

        StatServiceUtils.statAppBtnClick(getApplication(), StatServiceUtils.app_start);
        if (UserManager.INSTANCE.isUserLogin()) {
            StatServiceUtils.statAppBtnClick(getApplication(), StatServiceUtils.user_login_succeed);
        }

    }

    private void initSplashAd() {
//        if (ad_view != null) {
//            if (Constants.isHideAD){
//                AppLog.e(TAG, "Limited AD display!");
        handler.sendEmptyMessage(0);
//                return;
//            }
//            PlatformSDK.adapp().dycmSplashAd(this,"10-1",ad_view, new AbstractCallback() {
//                @Override
//                public void onResult(boolean adswitch, String jsonResult) {
//                    if (adswitch) {
//                        try {
//                            JSONObject jsonObject = new JSONObject(jsonResult);
//                            if (jsonObject.has("state_code")) {
//                                switch (ResultCode.parser(jsonObject.getInt("state_code"))) {
//                                    case AD_REQ_SUCCESS://广告请求成功
//                                        DyLogUtils.dd("AD_REQ_SUCCESS" + jsonResult);
//                                        break;
//                                    case AD_REQ_FAILED://广告请求失败
//                                        DyLogUtils.dd("AD_REQ_FAILED" + jsonResult);
//                                        handler.sendEmptyMessage(0);
//                                        break;
//                                    case AD_DISMISSED_CODE://开屏页面关闭
//                                        handler.sendEmptyMessage(0);
//                                        break;
//                                    case AD_ONCLICKED_CODE://开屏页面点击
//                                        DyLogUtils.dd("AD_ONCLICKED_CODE" + jsonResult);
//                                        break;
//                                    case AD_ONTICK_CODE://剩余显示时间
//                                        DyLogUtils.dd("AD_ONTICK_CODE" + jsonResult);
//                                        break;
//                                }
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    } else {
//                        handler.sendEmptyMessage(0);
//                    }
//                }
//            });
//    }
    }

    //初始化广告开关
    private void initAdSwitch() {
        if (!Constants.dy_ad_switch) {
            Constants.isHideAD = true;
            return;
        }

        //判断是否展示广告
        if (sharedPreUtil != null) {
            long limited_time = sharedPreUtil.getLong(SharedPreUtil.Companion.getAD_LIMIT_TIME_DAY(), 0L);
            if (limited_time == 0) {
                limited_time = System.currentTimeMillis();
                try {
                    sharedPreUtil.putLong(SharedPreUtil.Companion.getAD_LIMIT_TIME_DAY(), limited_time);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            AppLog.e(TAG, "Limited_Time : " + limited_time);
            AppLog.e(TAG, "Current_Time : " + System.currentTimeMillis());
            AppLog.e(TAG, "AD_Limited_day : " + Constants.ad_limit_time_day);

            int user_index = sharedPreUtil.getInt(SharedPreUtil.Companion.getUSER_NEW_INDEX(), 0);
            boolean init_ad = false;

            if (user_index == 0) {
                if (!sharedPreUtil.getBoolean(SharedPreUtil.Companion.getADD_DEFAULT_BOOKS(), false)) {
                    sharedPreUtil.putInt(SharedPreUtil.Companion.getUSER_NEW_INDEX(), 1);
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
                if (sharedPreUtil.getBoolean(SharedPreUtil.Companion.getADD_DEFAULT_BOOKS(), false)) {
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
                int ad_limit_time_day = sharedPreUtil.getInt(SharedPreUtil.Companion.getUSER_NEW_AD_LIMIT_DAY(), 0);
                if (ad_limit_time_day == 0 || Constants.ad_limit_time_day != ad_limit_time_day) {
                    ad_limit_time_day = Constants.ad_limit_time_day;
                    sharedPreUtil.putInt(SharedPreUtil.Companion.getUSER_NEW_AD_LIMIT_DAY(), ad_limit_time_day);
                }

                if (limited_time + (ad_limit_time_day * (Constants.DEVELOPER_MODE ? Constants.read_rest_time : Constants.one_day_time)) > System
                        .currentTimeMillis()) {
                    Constants.isHideAD = true;
                } else {
                    sharedPreUtil.putInt(SharedPreUtil.Companion.getUSER_NEW_INDEX(), 2);
                    //------------新壳没有广告写死为True--------------老壳请直接赋值为false!!!!
                    if (Constants.new_app_ad_switch) {
                        Constants.isHideAD = false;
                    } else {
                        Constants.isHideAD = true;
                    }
                }
            }
        } else {
            //------------新壳没有广告写死为True--------------老壳请直接赋值为false!!!!
            if (Constants.new_app_ad_switch) {
                Constants.isHideAD = false;
            } else {
                Constants.isHideAD = true;
            }
        }
        //强制关闭广告
//        Constants.isHideAD = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
//        PlatformSDK.lifecycle().onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        PlatformSDK.lifecycle().onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onDestroy() {
//        try {
//            setContentView(R.layout.empty);
//        } catch (Resources.NotFoundException e) {
//            e.printStackTrace();
//        }
//
//        if (context != null) {
//            context = null;
//        }

        AppLog.e(TAG, "ondestory执行");
//        PlatformSDK.lifecycle().onDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KEYCODE_BACK || super.onKeyDown(keyCode, event);
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
                    UserManager.INSTANCE.initPlatform(splashActivity, null);
                    AppLog.e(TAG, "handler执行");
                    splashActivity.initGuide();
                    break;
            }
        }

    }

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

            if(sharedPreUtil == null){
                sharedPreUtil = new SharedPreUtil(SharedPreUtil.Companion.getSHARE_DEFAULT());
            }
            boolean b = sharedPreUtil.getBoolean(SharedPreUtil.Companion.getUPDATE_CHAPTER_SOURCE_ID());
            if (!b) {
                BookDaoHelper bookDaoHelper = BookDaoHelper.getInstance();
                ArrayList<Book> bookOnlineList = bookDaoHelper.getBooksOnLineList();
                for (int i = 0; i < bookOnlineList.size(); i++) {
                    Book iBook = bookOnlineList.get(i);
                    if (!TextUtils.isEmpty(iBook.book_id)) {
                        BookChapterDao bookChapterDao = new BookChapterDao(BookApplication.getGlobalContext(), iBook.book_id);
                        Chapter lastChapter = bookChapterDao.getLastChapter();
                        if (lastChapter != null) {
                            lastChapter.book_source_id = iBook.book_source_id;
                            bookChapterDao.updateBookCurrentChapter(lastChapter, lastChapter.sequence);
                        }
                    }
                }
                sharedPreUtil.putBoolean(SharedPreUtil.Companion.getUPDATE_CHAPTER_SOURCE_ID(), true);
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

            // 4 加载默认书籍
//            try {
//                if (NetWorkUtils.getNetWorkType(BaseBookApplication.getGlobalContext()) != NetWorkUtils.NETWORK_NONE) {
//                    initBook();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }


            // 6 其他信息初始化
            try {
                // 统计阅读章节数
                if (Constants.readedCount == 0) {
                    Constants.readedCount = sharedPreUtil.getInt(SharedPreUtil.Companion.getREADED_CONT());
                }

                //
                DisplayMetrics dm = new DisplayMetrics();
                SplashActivity.this.getWindowManager().getDefaultDisplay().getMetrics(dm);
                sharedPreUtil.putInt(SharedPreUtil.Companion.getSCREEN_WIDTH(),dm.widthPixels);
                sharedPreUtil.putInt(SharedPreUtil.Companion.getSCREEN_HEIGHT(),dm.heightPixels);
//                BookApplication.getGlobalContext().setDisplayMetrics(dm);
                AppUtils.initDensity(getApplicationContext());

                // 判断是否小说推送，检查小说是否更新
                boolean isStarPush = sharedPreUtil.getBoolean(SharedPreUtil.Companion.getSETTINGS_PUSH(), true);
                if (isStarPush) {
                    CheckNovelUpdateService.startChkUpdService(getApplicationContext());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    // 安装快捷方式任务
    class InstallShotCutTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            if(sharedPreUtil == null){
                sharedPreUtil = new SharedPreUtil(SharedPreUtil.Companion.getSHARE_DEFAULT());
            }
            boolean create = sharedPreUtil.getBoolean(SharedPreUtil.Companion.getCREATE_SHOTCUT(), false);
            if (!create) {
                checkAndInstallShotCut(SplashActivity.this);
                sharedPreUtil.putBoolean(SharedPreUtil.Companion.getCREATE_SHOTCUT(), true);
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
