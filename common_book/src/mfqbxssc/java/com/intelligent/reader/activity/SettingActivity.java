package com.intelligent.reader.activity;

import static net.lzbook.kit.utils.ExtensionsKt.IS_FROM_PUSH;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.sdk.android.feedback.impl.FeedbackAPI;
import com.bumptech.glide.Glide;
import com.ding.basic.bean.LoginResp;
import com.dingyue.contract.router.RouterConfig;
import com.dingyue.contract.router.RouterUtil;
import com.dingyue.contract.util.CommonUtil;
import com.dy.reader.setting.ReaderSettings;
import com.intelligent.reader.R;
import com.intelligent.reader.util.EventBookStore;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.component.service.DownloadService;
import net.lzbook.kit.book.download.CacheManager;
import net.lzbook.kit.book.view.ConsumeEvent;
import net.lzbook.kit.book.view.MyDialog;
import net.lzbook.kit.book.view.SwitchButton;
import net.lzbook.kit.cache.DataCleanManager;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.constants.SPKeys;
import net.lzbook.kit.data.bean.BookTask;
import net.lzbook.kit.data.bean.ReadConfig;
import net.lzbook.kit.user.Platform;
import net.lzbook.kit.user.UserManager;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.StatServiceUtils;
import net.lzbook.kit.utils.UIHelper;
import net.lzbook.kit.utils.update.ApkUpdateUtils;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import iyouqu.theme.BaseCacheableActivity;
import iyouqu.theme.ThemeMode;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import swipeback.ActivityLifecycleHelper;


@Route(path = RouterConfig.SETTING_ACTIVITY)
public class SettingActivity extends BaseCacheableActivity implements View.OnClickListener, SwitchButton.OnCheckedChangeListener {

    private static final int CODE_REQ_LOGIN = 100;
    private final static int PUSH_TIME_SETTING = 1;
    public static SettingActivity sInstance;
    public static long cacheSize;
    public String TAG = SettingActivity.class.getSimpleName();
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            switch (message.what) {

            }
        }
    };
    protected String currentThemeMode; //是否切换了主题
    ApkUpdateUtils apkUpdateUtils = new ApkUpdateUtils(this);
    private ImageView top_setting_back;
    private MyDialog myDialog;
    private List<RelativeLayout> mRelativeLayoutList;
    private List<TextView> mTextViewList;
    private List<View> mDivider;
    private List<View> mGap;
    private TextView tv_style_change;
    private TextView tv_night_shift;
    private TextView tv_readpage_setting;
    private TextView tv_setting_more;
    private TextView tv_feedback;
    private TextView tv_mark;
    private TextView text_check_update;
    private TextView text_clear_cache;
    private TextView text_disclaimer_statement;
    //第二种布局 登录在左侧
    private TextView top_navigation_title;
    private LinearLayout rl_setting_layout;//背景
    private RelativeLayout rl_style_change;//主题切换
    private SwitchButton bt_night_shift;//夜间模式切换按钮
    private SwitchButton bt_wifi_auto;//wifi下自动缓存
    private RelativeLayout rl_readpage_setting;//阅读页设置
    private RelativeLayout rl_setting_more;//更多设置
    private RelativeLayout rl_feedback;//意见反馈
    private RelativeLayout rl_mark;//评分
    private RelativeLayout checkUpdateGuideRL; // 检查更新
    private RelativeLayout clear_cache_rl;//清除缓存
    private RelativeLayout disclaimer_statement_rl;//免责声明
    private TextView check_update_message; //版本号
    private TextView clear_cache_size;//缓存
    private TextView theme_name;//主题名
    private CacheAsyncTask cacheAsyncTask;
    private boolean isStyleChanged = false;
    private Runnable feedbackRunnable = new Runnable() {
        @Override
        public void run() {
            FeedbackAPI.openFeedbackActivity();
        }
    };


    private RelativeLayout rl_history_setting;
    private RelativeLayout rl_welfare;
    private ImageView img_welfare;
    private TextView tv_history_setting;
    private TextView txt_nickname;
    private TextView txt_userid;
    private ImageView img_head;
    private Button btn_logout;
    private ImageView img_head_background;
    private TextView txt_login_des;
    private boolean flagLoginEnd = true;
    private View mNightShadowView;
    private boolean isFromPush = false;

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        try {
            setContentView(R.layout.act_setting_user);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        // 获取CommunitySDK实例, 参数1为Context类型
        currentThemeMode = mThemeHelper.getMode();
        isStyleChanged = getIntent().getBooleanExtra("isStyleChanged", false);
        initView();
        initListener();
        initData();
        sInstance = this;
        UserManager.INSTANCE.initPlatform(this, null);
    }

    protected void initView() {

        //用于判断是否显示Textview的Drawable
        top_setting_back = (ImageView) findViewById(R.id.top_setting_back);
        rl_style_change = (RelativeLayout) findViewById(R.id.rl_style_change);
        bt_night_shift = (SwitchButton) findViewById(R.id.bt_night_shift);
        bt_wifi_auto = (SwitchButton) findViewById(R.id.bt_wifi_auto);
        rl_readpage_setting = (RelativeLayout) findViewById(R.id.rl_readpage_setting);
        rl_history_setting = (RelativeLayout) findViewById(R.id.rl_history_setting);
        rl_welfare = (RelativeLayout) findViewById(R.id.rl_welfare);
        img_welfare = (ImageView) findViewById(R.id.img_welfare);
        rl_setting_more = (RelativeLayout) findViewById(R.id.rl_setting_more);
        rl_feedback = (RelativeLayout) findViewById(R.id.rl_feedback);
        rl_mark = (RelativeLayout) findViewById(R.id.rl_mark);
        checkUpdateGuideRL = (RelativeLayout) findViewById(R.id.check_update_rl);
        clear_cache_rl = (RelativeLayout) findViewById(R.id.clear_cache_rl);
        disclaimer_statement_rl = (RelativeLayout) findViewById(R.id.disclaimer_statement_rl);
        rl_setting_layout = (LinearLayout) findViewById(R.id.rl_setting_layout);

        theme_name = (TextView) findViewById(R.id.theme_name);
        clear_cache_size = (TextView) findViewById(R.id.check_cache_size);
        check_update_message = (TextView) findViewById(R.id.check_update_message);

        //条目字
        tv_style_change = (TextView) findViewById(R.id.tv_style_change);
        tv_night_shift = (TextView) findViewById(R.id.tv_night_shift);
        tv_readpage_setting = (TextView) findViewById(R.id.tv_readpage_setting);
        tv_history_setting = (TextView) findViewById(R.id.tv_history_setting);
        tv_setting_more = (TextView) findViewById(R.id.tv_setting_more);
        tv_feedback = (TextView) findViewById(R.id.tv_feedback);
        tv_mark = (TextView) findViewById(R.id.tv_mark);
        text_check_update = (TextView) findViewById(R.id.text_check_update);
        text_clear_cache = (TextView) findViewById(R.id.text_clear_cache);
        text_disclaimer_statement = (TextView) findViewById(R.id.text_disclaimer_statement);

        top_navigation_title = (TextView) findViewById(R.id.top_navigation_title);

        txt_nickname = (TextView) findViewById(R.id.txt_nickname);
        txt_userid = (TextView) findViewById(R.id.txt_userid);
        btn_logout = (Button) findViewById(R.id.btn_logout);
        img_head = (ImageView) findViewById(R.id.img_head);
        img_head_background = (ImageView) findViewById(R.id.img_head_background);
        int desid = getResources().getIdentifier("txt_login_des", "id", getPackageName());

        if (desid != 0) {
            txt_login_des = (TextView) findViewById(desid);
        }

        if (mThemeHelper.isNight()) {
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_cli_day_shift);
            tv_night_shift.setText(R.string.mode_day);
            bt_night_shift.setChecked(true);
        } else {
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_cli_night_shift);
            tv_night_shift.setText(R.string.mode_night);
            bt_night_shift.setChecked(false);
        }

        bt_wifi_auto.setChecked(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SPKeys.Setting.AUTO_UPDATE_CAHCE, true));

        startWelfareCenterAnim();
    }


    private void startWelfareCenterAnim() {
        if (img_welfare != null) {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.welfare_center_anim);
            img_welfare.setAnimation(animation);
            animation.start();
        }
    }


    private void showUserInfo() {
        img_head.setClickable(false);
        txt_nickname.setClickable(false);
        txt_userid.setClickable(false);
        LoginResp userInfo = UserManager.INSTANCE.getMUserInfo();
        txt_nickname.setText(userInfo.getNickname());
        txt_userid.setText("ID:" + userInfo.getUid());
        Glide.with(this).load(userInfo.getHead_portrait()).into(img_head);
        findViewById(R.id.rl_logout).setVisibility(View.VISIBLE);

        if (txt_login_des != null) {
            txt_login_des.setVisibility(View.GONE);
        }
    }

    private void hideUserInfo() {
        img_head.setClickable(true);
        txt_nickname.setClickable(true);
        txt_userid.setClickable(true);

        txt_nickname.setText("点击登录");
        txt_userid.setText("登录使用更多功能");


        findViewById(R.id.rl_logout).setVisibility(View.GONE);
        img_head.setImageResource(R.mipmap.default_head);
        if (txt_login_des != null) {
            txt_login_des.setVisibility(View.VISIBLE);
        }
    }

    protected void initListener() {
        if (top_setting_back != null) {
            top_setting_back.setOnClickListener(this);
        }
        if (rl_style_change != null) {
            rl_style_change.setOnClickListener(this);
        }
        if (rl_history_setting != null) {
            rl_history_setting.setOnClickListener(this);
        }
        if (rl_welfare != null) {
            rl_welfare.setOnClickListener(this);
        }
        if (rl_readpage_setting != null) {
            rl_readpage_setting.setOnClickListener(this);
        }
        if (rl_setting_more != null) {
            rl_setting_more.setOnClickListener(this);
        }
        if (rl_feedback != null) {
            rl_feedback.setOnClickListener(this);
        }
        if (rl_mark != null) {
            rl_mark.setOnClickListener(this);
        }
        if (checkUpdateGuideRL != null) {
            checkUpdateGuideRL.setOnClickListener(this);
        }
        if (clear_cache_rl != null) {
            clear_cache_rl.setOnClickListener(this);
        }
        if (disclaimer_statement_rl != null) {
            disclaimer_statement_rl.setOnClickListener(this);
        }
        if (bt_night_shift != null) {
            bt_night_shift.setOnCheckedChangeListener(this);
        }
        if (bt_wifi_auto != null) {
            bt_wifi_auto.setOnCheckedChangeListener(this);
        }
        if (btn_logout != null) {
            findViewById(R.id.rl_logout).setOnClickListener(this);
            btn_logout.setOnClickListener(this);
        }
        if (img_head != null) {
            img_head.setOnClickListener(this);
        }
        if (txt_nickname != null) {
            txt_nickname.setOnClickListener(this);
        }
        if (txt_userid != null) {
            txt_userid.setOnClickListener(this);
        }
    }

    private void initData() {
        CancelTask();
        cacheAsyncTask = new CacheAsyncTask();
        cacheAsyncTask.execute();
        String versionName = AppUtils.getVersionName();
        check_update_message.setText("V" + versionName);
        isFromPush = getIntent().getBooleanExtra(IS_FROM_PUSH, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (UserManager.INSTANCE.isUserLogin()) {
            showUserInfo();
        } else {
            hideUserInfo();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        if (img_welfare != null) {
            img_welfare.clearAnimation();
        }

        try {
            if(handler != null){
                handler.removeCallbacksAndMessages(null);
            }
            setContentView(R.layout.empty);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        super.onDestroy();
        if(myDialog != null && myDialog.isShowing()){
            myDialog.dismiss();
        }
        CancelTask();
        sInstance = null;
    }

    @Override
    public void onClick(View paramView) {

        switch (paramView.getId()) {
            case R.id.rl_setting_more:
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.MORESET);
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_click_more);
                startActivity(new Intent(SettingActivity.this, SettingMoreActivity.class));
                break;
            case R.id.rl_style_change:
//                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_cli_theme_change);
//                startActivity(new Intent(SettingActivity.this, StyleChangeActivity.class));
//                finish();
                break;
            case R.id.check_update_rl:
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.VERSION);
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_click_ver);
                checkUpdate();
                break;
            case R.id.rl_feedback:
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.HELP);
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_click_help);
                handler.removeCallbacks(feedbackRunnable);
                handler.postDelayed(feedbackRunnable, 500);
                break;
            case R.id.rl_mark:
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.COMMENT);
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_click_help);
                try {
                    Uri uri = Uri.parse("market://details?id=" + getPackageName());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    CommonUtil.showToastMessage(R.string.menu_no_market);
                }
                break;

            case R.id.disclaimer_statement_rl:
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.PROCTCOL);
                RouterUtil.INSTANCE.navigation(this, RouterConfig.DISCLAIMER_ACTIVITY);
                break;
            case R.id.rl_history_setting:
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.PERSON_HISTORY);
                EventBus.getDefault().post(new ConsumeEvent(R.id.redpoint_setting_history));
                startActivity(new Intent(SettingActivity.this, FootprintActivity.class));
                break;
            case R.id.rl_welfare:
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.ADPAGE);
                Intent welfareIntent = new Intent();
                welfareIntent.putExtra("url", "https://st.quanbennovel.com/static/welfareCenter/welfareCenter.html");
                welfareIntent.putExtra("title", "福利中心");
                welfareIntent.setClass(SettingActivity.this, WelfareCenterActivity.class);
                startActivity(welfareIntent);
                break;
            case R.id.rl_readpage_setting:
                //阅读页设置
//                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_click_read);
//                startActivity(new Intent(SettingActivity.this, ReadingSettingActivity.class));
                break;
            case R.id.clear_cache_rl://清除缓存的处理
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.CACHECLEAR);
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_cli_clear_cache);
                clearCacheDialog();
                break;

            case R.id.top_setting_back:
                Map<String, String> data = new HashMap<>();
                data.put("type", "1");
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.BACK, data);
                goBackToHome(false);
                break;
            case R.id.img_head:
            case R.id.txt_nickname:
            case R.id.txt_userid:
                loginDialog();
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.LOGIN);
                break;
            case R.id.btn_logout:
            case R.id.rl_logout:
                logoutDialog();
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.LOGOUT);
                break;
            default:
                break;
        }
    }

    private void checkUpdate() {
        try {
            apkUpdateUtils.getApkUpdateInfo(this, handler, "SettingActivity");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logoutDialog() {
        if (myDialog != null && myDialog.isShowing()) {
            myDialog.dismiss();
        }
        myDialog = new MyDialog(this, R.layout.publish_hint_dialog);
        myDialog.setCanceledOnTouchOutside(false);
        myDialog.setCancelable(false);
        myDialog.setCanceledOnTouchOutside(true);//设置点击dialog外面对话框消失
        final Button sure = (Button) myDialog.findViewById(R.id.publish_stay);
        final Button cancel = (Button) myDialog.findViewById(R.id.publish_leave);
        final TextView publish_content = (TextView) myDialog.findViewById(R.id.publish_content);

        publish_content.setText(R.string.tips_logout);
        sure.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissDialog();
            }
        });
        cancel.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissDialog();
                if (UserManager.INSTANCE.isUserLogin()) {
                    UserManager.INSTANCE.logout(null);

                    hideUserInfo();
                }
            }
        });
        myDialog.show();
    }

    private void loginDialog() {
        if (myDialog != null && myDialog.isShowing()) {
            myDialog.dismiss();
        }
        myDialog = new MyDialog(this, R.layout.login_dialog);
        myDialog.setCancelable(false);
        myDialog.setCanceledOnTouchOutside(true);
        LinearLayout qqLogin = (LinearLayout) myDialog.findViewById(R.id.ll_qq);
        LinearLayout wxLogin = (LinearLayout) myDialog.findViewById(R.id.ll_wx);

        qqLogin.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissDialog();
                if (flagLoginEnd) {
                    flagLoginEnd = false;
                    showProgressDialog();
                    UserManager.INSTANCE.login(SettingActivity.this, Platform.QQ, new Function1<LoginResp, Unit>() {
                        @Override
                        public Unit invoke(LoginResp loginResp) {
                            dismissDialog();
                            flagLoginEnd = true;
                            onLoginSucess();
                            return null;
                        }
                    }, new Function1<String, Unit>() {
                        @Override
                        public Unit invoke(String s) {
                            dismissDialog();
                            flagLoginEnd = true;
                            return null;
                        }
                    });
                }
            }
        });
        wxLogin.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissDialog();
                if (!UserManager.INSTANCE.isPlatformEnable(Platform.WECHAT)) {
                    CommonUtil.showToastMessage("请安装微信后重试");
                    return;
                }
                if (flagLoginEnd) {
                    flagLoginEnd = false;
                    showProgressDialog();
                    UserManager.INSTANCE.login(SettingActivity.this, Platform.WECHAT, new Function1<LoginResp, Unit>() {
                        @Override
                        public Unit invoke(LoginResp loginResp) {
                            dismissDialog();
                            flagLoginEnd = true;
                            onLoginSucess();
                            return null;
                        }
                    }, new Function1<String, Unit>() {
                        @Override
                        public Unit invoke(String s) {
                            dismissDialog();
                            flagLoginEnd = true;
                            return null;
                        }
                    });
                }
            }
        });
        myDialog.show();
    }

    private void showProgressDialog() {
        if (myDialog != null && myDialog.isShowing()) {
            myDialog.dismiss();
        }

        myDialog = new MyDialog(this, R.layout.publish_hint_dialog);
        myDialog.setCancelable(false);
        myDialog.setCanceledOnTouchOutside(true);

        myDialog.findViewById(R.id.publish_content).setVisibility(View.GONE);
        ((TextView) (myDialog.findViewById(R.id.dialog_title))).setText(R.string.tips_login);
        myDialog.findViewById(R.id.change_source_bottom).setVisibility(View.GONE);
        myDialog.findViewById(R.id.progress_del).setVisibility(View.VISIBLE);
        myDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                flagLoginEnd = true;
            }
        });
        myDialog.show();
    }

    private void clearCacheDialog() {
        if (!isFinishing()) {
            myDialog = new MyDialog(this, R.layout.publish_hint_dialog);
            myDialog.setCanceledOnTouchOutside(false);
            myDialog.setCancelable(false);
            myDialog.setCanceledOnTouchOutside(true);//设置点击dialog外面对话框消失
            final Button btn_cancle_clear_cache = (Button) myDialog.findViewById(R.id.publish_stay);
            final Button btn_confirm_clear_cache = (Button) myDialog.findViewById(R.id.publish_leave);
            final TextView publish_content = (TextView) myDialog.findViewById(R.id.publish_content);
            final TextView dialog_title = (TextView) myDialog.findViewById(R.id.dialog_title);
            publish_content.setText(R.string.tip_clear_cache);
            btn_cancle_clear_cache.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismissDialog();
                }
            });
            btn_confirm_clear_cache.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    publish_content.setVisibility(View.GONE);
                    dialog_title.setText(R.string.tip_cleaning_cache);
                    myDialog.findViewById(R.id.change_source_bottom).setVisibility(View.GONE);

                    myDialog.findViewById(R.id.progress_del).setVisibility(View.VISIBLE);
                    //添加清除缓存的处理
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();

                            CacheManager.INSTANCE.removeAll();

                            UIHelper.clearAppCache();
                            DataCleanManager.clearAllCache(getApplicationContext());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    dismissDialog();
                                    clear_cache_size.setText("0B");
                                }
                            });
                        }
                    }.start();

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

    @Override
    public void onBackPressed() {
        goBackToHome(true);
    }

    public void goBackToHome(boolean isPageBack) {
        if (!currentThemeMode.equals(mThemeHelper.getMode()) || isStyleChanged) {
            if (getSwipeBackHelper() == null || !getSwipeBackHelper().isSliding()) {//滑动返回已结束
                onThemeSwitch();
            }
        } else {
            if(isPageBack){
                super.onBackPressed();
            }else{
                finish();
            }
        }
    }

    @Override
    public void onSlideFinishAnimEnd() {
        super.onSlideFinishAnimEnd();
        if (!currentThemeMode.equals(mThemeHelper.getMode()) || isStyleChanged) {
            onThemeSwitch();
        }
    }

    private void onThemeSwitch() {
        Intent themIntent = new Intent(SettingActivity.this, HomeActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(EventBookStore.BOOKSTORE, EventBookStore.TYPE_TO_SWITCH_THEME);
        themIntent.putExtras(bundle);
        startActivity(themIntent);
    }

    private void dismissDialog() {
        if (myDialog != null && myDialog.isShowing()) {
            myDialog.dismiss();
        }
    }


    //夜间模式切换按钮的回调
    @Override
    public void onCheckedChanged(SwitchButton view, boolean isChecked) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor edit = sharedPreferences.edit();
        if (view.getId() == R.id.bt_night_shift) {
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.NIGHTMODE);
            ReaderSettings.Companion.getInstance().initValues();
            if (isChecked) {
                tv_night_shift.setText(R.string.mode_day);
                ReaderSettings.Companion.getInstance().setReadLightThemeMode(ReaderSettings.Companion.getInstance().getReadThemeMode());
                ReaderSettings.Companion.getInstance().setReadThemeMode(61);
                mThemeHelper.setMode(ThemeMode.NIGHT);
            } else {
                tv_night_shift.setText(R.string.mode_night);
                ReaderSettings.Companion.getInstance().setReadThemeMode(ReaderSettings.Companion.getInstance().getReadLightThemeMode());
                mThemeHelper.setMode(ThemeMode.THEME1);
            }
            ReaderSettings.Companion.getInstance().save();
            nightShift(isChecked, true);
        } else if (view.getId() == R.id.bt_wifi_auto) {
            edit.putBoolean(SPKeys.Setting.AUTO_UPDATE_CAHCE, isChecked);
            edit.apply();
            Map<String, String> data = new HashMap<>();
            data.put("type", isChecked ? "1" : "0");
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.WIFI_AUTOCACHE, data);
        }
    }

    private void CancelTask() {
        if (cacheAsyncTask != null) {
            cacheAsyncTask.cancel(true);
            cacheAsyncTask = null;
        }
    }

    private void onLoginSucess() {
        if (UserManager.INSTANCE.isUserLogin()) {
            showUserInfo();
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.user_login_succeed);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        UserManager.INSTANCE.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void finish() {
        super.finish();
        //离线消息 跳转到主页
        if (isFromPush && ActivityLifecycleHelper.getActivities().size() <= 1) {
            startActivity(new Intent(this, SplashActivity.class));
        }
    }

    @Override
    public boolean supportSlideBack() {
        return ActivityLifecycleHelper.getActivities().size() > 1;
    }

    private class CacheAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            String result = "0B";
            try {
                result = DataCleanManager.getTotalCacheSize(getApplicationContext());
                SettingActivity.cacheSize = DataCleanManager.internalCacheSize;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            clear_cache_size.setText(result);
        }

    }

}