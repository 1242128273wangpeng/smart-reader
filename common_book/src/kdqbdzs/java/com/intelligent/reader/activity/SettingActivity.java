package com.intelligent.reader.activity;

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
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.feedback.impl.FeedbackAPI;
import com.intelligent.reader.R;
import com.intelligent.reader.util.EventBookStore;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.component.service.DownloadService;
import net.lzbook.kit.book.view.MyDialog;
import net.lzbook.kit.book.view.SwitchButton;
import net.lzbook.kit.cache.DataCleanManager;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.BookTask;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.StatServiceUtils;
import net.lzbook.kit.utils.UIHelper;
import net.lzbook.kit.utils.update.ApkUpdateUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import iyouqu.theme.StatusBarCompat;
import iyouqu.theme.ThemeMode;


public class SettingActivity extends BaseCacheableActivity implements View.OnClickListener, SwitchButton.OnCheckedChangeListener {

    private final static int PUSH_TIME_SETTING = 1;
    private static final int LOGIN_SUCCESS = 0x20;
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
    TypedValue themeName = new TypedValue();//分割块颜色
    private ImageView btnBack;
    private ImageView top_setting_back;
    private MyDialog myDialog;//清除缓存对话框
    private RelativeLayout user_login_layout;
    private View is_show_drawable;
    private List<RelativeLayout> mRelativeLayoutList;
    private List<TextView> mTextViewList;
    private List<View> mDivider;
    private List<View> mGap;
    private TextView tv_readpage_bbs;
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
    private RelativeLayout top_navigation_bg;
    private ImageView icon_more_left;
    private TextView tv_login_info_detail_left;
    private TextView tv_login_info_left;
    private TextView top_navigation_title;
    private ImageView iv_mine_image_left;
    private RelativeLayout user_login_layout_left;
    private LinearLayout rl_setting_layout;//背景
    private RelativeLayout rl_readpage_bbs;//论坛
    private RelativeLayout rl_style_change;//主题切换
    private ImageView iv_mine_image;
    private TextView tv_login_info;
    private SwitchButton bt_night_shift;//夜间模式切换按钮
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
    private boolean isActivityPause = false;
    private boolean isStyleChanged = false;
    private Runnable feedbackRunnable = new Runnable() {
        @Override
        public void run() {
            FeedbackAPI.openFeedbackActivity();
        }
    };

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
    }

    protected void initView() {

        //用于判断是否显示Textview的Drawable
        is_show_drawable = findViewById(R.id.is_show_drawable);
        top_navigation_bg = (RelativeLayout) findViewById(R.id.top_navigation_bg);
        icon_more_left = (ImageView) findViewById(R.id.icon_more_left);
        btnBack = (ImageView) findViewById(R.id.setting_back);
        top_setting_back = (ImageView) findViewById(R.id.top_setting_back);
        user_login_layout = (RelativeLayout) findViewById(R.id.user_login_layout);
        iv_mine_image = (ImageView) findViewById(R.id.iv_mine_image);
        tv_login_info = (TextView) findViewById(R.id.tv_login_info);
        iv_mine_image = (ImageView) findViewById(R.id.iv_mine_image);
        tv_login_info = (TextView) findViewById(R.id.tv_login_info);
        iv_mine_image_left = (ImageView) findViewById(R.id.iv_mine_image_left);
        user_login_layout_left = (RelativeLayout) findViewById(R.id.user_login_layout_left);

        rl_readpage_bbs = (RelativeLayout) findViewById(R.id.rl_readpage_bbs);
        rl_style_change = (RelativeLayout) findViewById(R.id.rl_style_change);
        bt_night_shift = (SwitchButton) findViewById(R.id.bt_night_shift);
        rl_readpage_setting = (RelativeLayout) findViewById(R.id.rl_readpage_setting);
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
        tv_readpage_bbs = (TextView) findViewById(R.id.tv_readpage_bbs);
        tv_style_change = (TextView) findViewById(R.id.tv_style_change);
        tv_night_shift = (TextView) findViewById(R.id.tv_night_shift);
        tv_readpage_setting = (TextView) findViewById(R.id.tv_readpage_setting);
        tv_setting_more = (TextView) findViewById(R.id.tv_setting_more);
        tv_feedback = (TextView) findViewById(R.id.tv_feedback);
        tv_mark = (TextView) findViewById(R.id.tv_mark);
        text_check_update = (TextView) findViewById(R.id.text_check_update);
        text_clear_cache = (TextView) findViewById(R.id.text_clear_cache);
        text_disclaimer_statement = (TextView) findViewById(R.id.text_disclaimer_statement);

        tv_login_info_left = (TextView) findViewById(R.id.tv_login_info_left);
        tv_login_info_detail_left = (TextView) findViewById(R.id.tv_login_info_detail_left);
        top_navigation_title = (TextView) findViewById(R.id.top_navigation_title);

        //字体颜色
        mTextViewList = new ArrayList<>();
        TextView[] tvNum = new TextView[]{tv_readpage_bbs, tv_style_change, tv_night_shift, tv_readpage_setting, tv_setting_more,
                tv_feedback, tv_mark, text_check_update, text_clear_cache, text_disclaimer_statement, tv_login_info_left};

        for (TextView textView : tvNum) {
            mTextViewList.add(textView);
        }

        //条目背景
        mRelativeLayoutList = new ArrayList<>();
        RelativeLayout[] rlNum = new RelativeLayout[]{rl_readpage_bbs, rl_style_change, rl_readpage_setting, rl_setting_more,
                rl_feedback, rl_mark, checkUpdateGuideRL, clear_cache_rl, disclaimer_statement_rl};

        for (RelativeLayout relativeLayout : rlNum) {
            mRelativeLayoutList.add(relativeLayout);
        }


        //15条分割线 和 3个gap
        mDivider = new ArrayList<>();
        View[] viewNum = new View[]{findViewById(R.id.v_divider), findViewById(R.id.v_divider1), findViewById(R.id.v_divider2), findViewById(R.id.v_divider3), findViewById(R.id.v_divider4), findViewById(R.id.v_divider5), findViewById(R.id.v_divider6),
                findViewById(R.id.v_divider7), findViewById(R.id.v_divider8), findViewById(R.id.v_divider9), findViewById(R.id.v_divider10), findViewById(R.id.v_divider11), findViewById(R.id.v_divider12), findViewById(R.id.v_divider13),
                findViewById(R.id.v_divider14), findViewById(R.id.v_divider15), findViewById(R.id.v_divider16)};

        for (View view : viewNum) {
            mDivider.add(view);
        }

        mGap = new ArrayList<>();
        mGap.add(findViewById(R.id.v_gap1));
        mGap.add(findViewById(R.id.v_gap2));
        mGap.add(findViewById(R.id.v_gap3));
        mGap.add(findViewById(R.id.v_gap4));

        if (mThemeHelper.isNight()) {
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_cli_day_shift);
            tv_night_shift.setText(R.string.mode_day);
            bt_night_shift.setChecked(true);
        } else {
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_cli_night_shift);
            tv_night_shift.setText(R.string.mode_night);
            bt_night_shift.setChecked(false);
        }
//        //初始化主题名
//        getTheme().resolveAttribute(R.attr.theme_name, themeName, true);
//        theme_name.setText(getResources().getText(themeName.resourceId));
    }

    protected void initListener() {
        if (btnBack != null) {
            btnBack.setOnClickListener(this);
        }
        if (top_setting_back != null) {
            top_setting_back.setOnClickListener(this);
        }
        if (user_login_layout != null) {
            user_login_layout.setOnClickListener(this);
        }
        if (iv_mine_image != null) {
            iv_mine_image.setOnClickListener(this);
        }
        if (tv_login_info != null) {
            tv_login_info.setOnClickListener(this);
        }
        if (rl_style_change != null) {
            rl_style_change.setOnClickListener(this);
        }
        if (rl_readpage_bbs != null) {
            rl_readpage_bbs.setOnClickListener(this);
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
        if (tv_login_info != null) {
            tv_login_info.setOnClickListener(this);
        }
        if (user_login_layout_left != null) {
            user_login_layout_left.setOnClickListener(this);
        }
    }

    private void initData() {
        CancelTask();
        cacheAsyncTask = new CacheAsyncTask();
        cacheAsyncTask.execute();
        String versionName = AppUtils.getVersionName();
        check_update_message.setText("V" + versionName);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityPause = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityPause = true;
    }

    @Override
    protected void onDestroy() {
        try {
            setContentView(R.layout.empty);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        super.onDestroy();
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
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_cli_theme_change);
                startActivity(new Intent(SettingActivity.this, StyleChangeActivity.class));
//                finish();
                break;
            case R.id.tv_login_info:
                Toast.makeText(getApplicationContext(), R.string.enter_community, Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_mine_image:
            case R.id.user_login_layout_left:
                Toast.makeText(getApplicationContext(), R.string.enter_community, Toast.LENGTH_SHORT).show();
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
                    showToastShort(R.string.menu_no_market);
                }
                break;

            case R.id.disclaimer_statement_rl:
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.PROCTCOL);
                Intent intent = new Intent(this, DisclaimerActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_readpage_setting:
                //阅读页设置
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_click_read);
                startActivity(new Intent(SettingActivity.this, ReadingSettingActivity.class));
                break;
            case R.id.rl_readpage_bbs:
                Toast.makeText(getApplicationContext(), R.string.enter_community, Toast.LENGTH_SHORT).show();
                break;
            case R.id.clear_cache_rl://清除缓存的处理
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.CACHECLEAR);
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_cli_clear_cache);
                clearCacheDialog();
                break;

            case R.id.top_setting_back:
            case R.id.setting_back:
                Map<String, String> data = new HashMap<>();
                data.put("type", "1");
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.BACK, data);
                goBackToHome();
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

                            DownloadService downloadService = BaseBookApplication.getDownloadService();
                            if (downloadService != null) {
                                ArrayList<BookTask> bookTasks = downloadService.cancelAll();
                                if (bookTasks != null) {
                                    for (BookTask task : bookTasks) {
                                        downloadService.dellTask(task.book_id);
                                    }
                                }
                            }

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
        super.onBackPressed();
        goBackToHome();
    }

    public void goBackToHome() {
        if (!currentThemeMode.equals(mThemeHelper.getMode()) || isStyleChanged) {
            Intent themIntent = new Intent(SettingActivity.this, HomeActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(EventBookStore.BOOKSTORE, EventBookStore.TYPE_TO_SWITCH_THEME);
            themIntent.putExtras(bundle);
            startActivity(themIntent);
        } else {
            finish();
        }
    }

    private void dismissDialog() {
        if (myDialog != null && myDialog.isShowing()) {
            myDialog.dismiss();
        }
    }


    private void nightShift() {
        mThemeHelper.showAnimation(this);
        StatusBarCompat.compat(this);
    }

    private void CancelTask() {
        if (cacheAsyncTask != null) {
            cacheAsyncTask.cancel(true);
            cacheAsyncTask = null;
        }
    }

    //夜间模式切换按钮的回调
    @Override
    public void onCheckedChanged(SwitchButton view, boolean isChecked) {
        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.NIGHTMODE);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor edit = sharedPreferences.edit();
        if (isChecked) {
            tv_night_shift.setText(R.string.mode_day);
            edit.putInt("current_light_mode", Constants.MODE);
            Constants.MODE = 61;
            mThemeHelper.setMode(ThemeMode.NIGHT);
        } else {
            tv_night_shift.setText(R.string.mode_night);
            edit.putInt("current_night_mode", Constants.MODE);
            Constants.MODE = sharedPreferences.getInt("current_light_mode", 51);
            mThemeHelper.setMode(ThemeMode.THEME1);
        }
        edit.putInt("content_mode", Constants.MODE);
        edit.apply();
        nightShift(isChecked, true);
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