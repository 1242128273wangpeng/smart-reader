package com.intelligent.reader.activity;

import com.intelligent.reader.R;
import com.intelligent.reader.view.CustomDialog;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.view.SwitchButton;
import net.lzbook.kit.book.view.TimePicker;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.SettingItems;
import net.lzbook.kit.utils.SettingItemsHelper;
import net.lzbook.kit.utils.StatServiceUtils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import iyouqu.theme.BaseCacheableActivity;

public class SettingMoreActivity extends BaseCacheableActivity implements View.OnClickListener, SwitchButton.OnCheckedChangeListener {

    private final static int PUSH_TIME_SETTING = 1;
    public String TAG = SettingMoreActivity.class.getSimpleName();
    private ImageView btnBack;
    private TextView title;
    private TextView setting_more_back;
    private RelativeLayout push;
    private SwitchButton push_checkbox;
    private RelativeLayout push_sound;
    private SwitchButton push_sound_checkbox;
    private RelativeLayout push_time;
    private SwitchButton push_time_checkbox;
    private TextView push_time_setting_text;
    private RelativeLayout bookshelf_sort_update_time;
    private ImageView bookshelf_sort_update_time_checkbox;
    //设置帮助类
    private SettingItemsHelper settingItemsHelper;
    private SettingItems settingItems;
    private View time_dialog;
    private TimePicker time_picker;
    private Context mContext;

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.act_setting_more);
        mContext = this;
        initView();
        initData();
        initListener();
    }

    protected void initView() {



        btnBack = (ImageView) findViewById(R.id.btn_left_setting);
        title = (TextView) findViewById(R.id.tv_title_name);

        //推送
        push = (RelativeLayout) findViewById(R.id.push);
        push_checkbox = (SwitchButton) findViewById(R.id.push_message_checkbox);
        //推送声音
        push_sound = (RelativeLayout) findViewById(R.id.push_sound);
        push_sound_checkbox = (SwitchButton) findViewById(R.id.push_sound_checkbox);
        //推送时间
        push_time = (RelativeLayout) findViewById(R.id.push_time);
        push_time_checkbox = (SwitchButton) findViewById(R.id.push_time_checkbox);
        //推送时间设置
        push_time_setting_text = (TextView) findViewById(R.id.push_time_setting_text);
        //书架按时间排序
        //书架按更新时间排序
        bookshelf_sort_update_time = (RelativeLayout) findViewById(R.id.bookshelf_sort_update_time);
        bookshelf_sort_update_time_checkbox = (ImageView) findViewById(R.id.bookshelf_sort_update_time_checkbox);
        time_dialog = LayoutInflater.from(this).inflate(R.layout.view_custom_dialog_push_time_setting, null);
        time_picker = (TimePicker) time_dialog.findViewById(R.id.timepicker);


    }

    private void initData() {
        settingItemsHelper = SettingItemsHelper.getSettingHelper(getApplicationContext());
        settingItems = settingItemsHelper.getValues();
        push_checkbox.setChecked(settingItems.isPush);
        push_sound_checkbox.setChecked(settingItems.isSoundOpen);
        push_time_checkbox.setChecked(settingItems.isSetPushTime);
        if (push_time_checkbox.isChecked()) {
            push_time_setting_text.setEnabled(true);
            initPushTime();
            push_time_setting_text.setVisibility(View.VISIBLE);
            setPushTime2Show();
        } else {
            push_time_setting_text.setEnabled(false);
            push_time_setting_text.setVisibility(View.VISIBLE);
        }

    }

    protected void initListener() {

        if (setting_more_back != null) {
            setting_more_back.setOnClickListener(this);
        }

        if (push != null) {
            push.setOnClickListener(this);
        }

        if (push_checkbox != null) {
            push_checkbox.setOnCheckedChangeListener(this);
        }

        if (push_sound != null) {
            push_sound.setOnClickListener(this);
        }

        if (push_sound_checkbox != null) {
            push_sound_checkbox.setOnCheckedChangeListener(this);
        }

        if (push_time != null) {
            push_time.setOnClickListener(this);
        }

        if (push_time_checkbox != null) {
            push_time_checkbox.setOnCheckedChangeListener(this);
        }

        if (push_time_setting_text != null) {
            push_time_setting_text.setOnClickListener(this);
        }



        if (bookshelf_sort_update_time != null) {
            bookshelf_sort_update_time.setOnClickListener(this);
        }
        if (btnBack != null) {
            btnBack.setOnClickListener(this);
        }
        if (bookshelf_sort_update_time_checkbox != null) {
            bookshelf_sort_update_time_checkbox.setOnClickListener(this);
        }
        //        if(tv_feedback !=null){
        //            tv_feedback.setOnClickListener(this);
        //        }
    }

    @Override
    public void onClick(View paramView) {

        switch (paramView.getId()) {

            case R.id.btn_left_setting:
                Map<String, String> data = new HashMap<>();
                data.put("type", "1");
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.MORESET_PAGE, StartLogClickUtil.BACK, data);
                finish();
                break;

            case R.id.push_time_setting_text:
                showDialog(PUSH_TIME_SETTING);
                break;

            case R.id.bookshelf_sort_time:
            case R.id.bookshelf_sort_time_checkbox:
                StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.me_set_cli_shelf_rak_time);
                settingItemsHelper.putInt(settingItemsHelper.booklistSortType, 0);
                Constants.book_list_sort_type = 0;
                break;

            case R.id.bookshelf_sort_update_time:
            case R.id.bookshelf_sort_update_time_checkbox:
                StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.me_set_cli_shelf_rank_up);
                settingItemsHelper.putInt(settingItemsHelper.booklistSortType, 1);
                Constants.book_list_sort_type = 1;
                break;
            case R.id.push_message_checkbox:

                StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.me_set_click_more_push);
                changePushStatus(push_checkbox.isChecked());

                break;

            default:
                break;
        }
    }

    private void changePushStatus(boolean status) {
        push_checkbox.setChecked(status);

        push_sound.setEnabled(status);
        if (status) {
            push_sound_checkbox.setEnabled(status);
            push_sound_checkbox.setChecked(status);
        } else {
            push_sound_checkbox.setChecked(status);
            push_sound_checkbox.setEnabled(status);
        }

        push_time.setEnabled(status);
        if (status) {
            push_time_checkbox.setEnabled(status);
            push_time_checkbox.setChecked(status);
        } else {
            push_time_checkbox.setChecked(status);
            push_time_checkbox.setEnabled(status);
        }

        settingItemsHelper.putBoolean(settingItemsHelper.openPush, status);
        setPushSound(status, status);
        setPushTime(status, status);
    }

    private void changePushSoundStatus(boolean status) {
        push_sound_checkbox.setChecked(status);
    }

    private void changePushTimeStatus(boolean status) {
        push_time_checkbox.setChecked(status);
    }

    private void setPushSound(boolean isChecked, boolean isStartPush) {
        if (isChecked) {
            if (isStartPush) {
                settingItemsHelper.putBoolean(settingItemsHelper.openPushSound, true);
            } else {
                settingItemsHelper.putBoolean(settingItemsHelper.openPushSound, false);
            }

        } else {
            settingItemsHelper.putBoolean(settingItemsHelper.openPushSound, isStartPush);
        }
    }

    private void setPushTime(boolean isChecked, boolean isStartPush) {
        if (isChecked) {
            if (isStartPush) {
                settingItemsHelper.putBoolean(settingItemsHelper.setPushTime, true);
                push_time_setting_text.setEnabled(true);
                push_time_setting_text.setVisibility(View.VISIBLE);
                initPushTime();
                setPushTime2Show();
            } else {
                settingItemsHelper.putBoolean(settingItemsHelper.setPushTime, false);
                push_time_setting_text.setEnabled(false);
                push_time_setting_text.setVisibility(View.VISIBLE);
                push_time_setting_text.setText("全天推送");
            }

        } else {
            settingItemsHelper.putBoolean(settingItemsHelper.setPushTime, isStartPush);
            push_time_setting_text.setEnabled(false);
            push_time_setting_text.setVisibility(View.VISIBLE);
            push_time_setting_text.setText("无推送");
        }

    }



    private void initPushTime() {
        if (time_picker != null) {
            time_picker.setCurrentStartHour(settingItems.pushTimeStartH);
            time_picker.setCurrentStartMinute(settingItems.pushTimeStartMin);
            time_picker.setCurrentStopHour(settingItems.pushTimeStopH);
            time_picker.setCurrentStopMinute(settingItems.pushTimeStopMin);
        }
    }

    private void setPushTime2Show() {
        if (settingItems.pushTimeStartMin >= 10) {
            if (settingItems.pushTimeStopMin >= 10) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("推送时间：")
                        .append(settingItems.pushTimeStartH).append(":")
                        .append(settingItems.pushTimeStartMin).append("-")
                        .append(settingItems.pushTimeStopH).append(":")
                        .append(settingItems.pushTimeStopMin);
                push_time_setting_text.setText(stringBuilder.toString());

            } else {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("推送时间：")
                        .append(settingItems.pushTimeStartH).append(":")
                        .append(settingItems.pushTimeStartMin).append("-")
                        .append(settingItems.pushTimeStopH).append(":0")
                        .append(settingItems.pushTimeStopMin);
                push_time_setting_text.setText(stringBuilder.toString());
            }
        } else {
            if (settingItems.pushTimeStopMin >= 10) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("推送时间：")
                        .append(settingItems.pushTimeStartH).append(":0")
                        .append(settingItems.pushTimeStartMin).append("-")
                        .append(settingItems.pushTimeStopH).append(":")
                        .append(settingItems.pushTimeStopMin);
                push_time_setting_text.setText(stringBuilder.toString());
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("推送时间：")
                        .append(settingItems.pushTimeStartH).append(":0")
                        .append(settingItems.pushTimeStartMin).append("-")
                        .append(settingItems.pushTimeStopH).append(":0")
                        .append(settingItems.pushTimeStopMin);
                push_time_setting_text.setText(stringBuilder.toString());
            }
        }
    }

    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case PUSH_TIME_SETTING:
                CustomDialog.Builder customBuilder = new CustomDialog.Builder(this);
                customBuilder.setTitle("时间设置");
                customBuilder
                        .setContentView(this.time_dialog)
                        .setNegativeButton("取消",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        dialog.dismiss();
                                    }
                                }
                        )
                        .setPositiveButton("设置",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        dialog.dismiss();
                                        // 获取时间，写文件
                                        int startHour = time_picker
                                                .getCurrentStartHour();
                                        int startMin = time_picker
                                                .getCurrentStartMinute();
                                        int stopHour = time_picker
                                                .getCurrentStopHour();
                                        int stopMin = time_picker
                                                .getCurrentStopMinute();

                                        settingItemsHelper.putInt(
                                                settingItemsHelper.pushTimeStartH,
                                                startHour);
                                        settingItemsHelper.putInt(
                                                settingItemsHelper.pushTimeStartMin,
                                                startMin);
                                        settingItemsHelper.putInt(
                                                settingItemsHelper.pushTimeStopH,
                                                stopHour);
                                        settingItemsHelper.putInt(
                                                settingItemsHelper.pushTimeStopMin,
                                                stopMin);

                                        setPushTime2Show();
                                    }
                                }
                        );
                dialog = customBuilder.create();

                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        time_picker.setCurrentStartHour(settingItems.pushTimeStartH);
                        time_picker.setCurrentStartMinute(settingItems.pushTimeStartMin);

                        time_picker.setCurrentStopHour(settingItems.pushTimeStopH);
                        time_picker.setCurrentStopMinute(settingItems.pushTimeStopMin);
                    }
                });

                break;
        }
        return dialog;

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                setResult(67);
                this.finish();
                break;
            default:
                break;
        }
    }

    public void setPushStatus(boolean pushStatus) {
        push_checkbox.setChecked(pushStatus);

        push_sound.setEnabled(pushStatus);
        push_sound_checkbox.setChecked(pushStatus);
        push_sound_checkbox.setEnabled(pushStatus);

        push_time.setEnabled(pushStatus);
        push_time_checkbox.setChecked(pushStatus);
        push_time_checkbox.setEnabled(pushStatus);

        settingItemsHelper.putBoolean(settingItemsHelper.openPush, pushStatus);
        if (pushStatus) {
            setPushSound(settingItems.isSoundOpen, pushStatus);
            setPushTime(settingItems.isSetPushTime, pushStatus);
        } else {
            setPushSound(pushStatus, pushStatus);
            setPushTime(pushStatus, pushStatus);
        }
    }

    @Override
    protected void onDestroy() {

        if (time_picker != null) {
            time_picker = null;
        }

        if (time_dialog != null) {
            time_dialog = null;
        }

        try {
            setContentView(R.layout.empty);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

    @Override
    public void onCheckedChanged(SwitchButton view, boolean isChecked) {
        if (view == null) {
            return;
        }
        switch (view.getId()) {
            case R.id.push_message_checkbox:
                StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.me_set_click_more_push);
                changePushStatus(push_checkbox.isChecked());
                Map<String, String> params1 = new HashMap<>();
                params1.put("type", push_checkbox.isChecked() ? "1" : "2");
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.MORESET_PAGE, StartLogClickUtil.PUSHSET, params1);
                break;

            case R.id.push_sound_checkbox:
                Map<String, String> params = new HashMap<>();
                params.put("type", push_sound_checkbox.isChecked() ? "1" : "2");
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.MORESET_PAGE, StartLogClickUtil.PUSHAUDIO, params);
                StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.me_set_cli_more_push_voi);
                changePushSoundStatus(push_sound_checkbox.isChecked());
                setPushSound(push_checkbox.isChecked(), push_sound_checkbox.isChecked());
                break;

            case R.id.push_time_checkbox:
                StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.me_set_cli_more_push_time);
                changePushTimeStatus(push_time_checkbox.isChecked());
                setPushTime(push_checkbox.isChecked(), push_time_checkbox.isChecked());
                break;
        }
    }
}
