package com.intelligent.reader.activity;


import com.intelligent.reader.R;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.utils.StatServiceUtils;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import iyouqu.theme.ThemeMode;

public class StyleChangeActivity extends BaseCacheableActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {
    protected String currentThemeMode; //是否切换了主题
    private ImageView imageView_styleChange;
    private TextView textView_congfirm;
    private RadioGroup radioGroup_styleChange;
    private ImageView imageView_back_styleChange;
    private ThemeMode themeMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_style_change);
        currentThemeMode = mThemeHelper.getMode();
        initView();
        initListener();
    }

    private void initView() {
        imageView_back_styleChange = (ImageView) findViewById(R.id.imageView_back_styleChange);
        imageView_styleChange = (ImageView) findViewById(R.id.imageView_styleChange);
        textView_congfirm = (TextView) findViewById(R.id.textView_confirm_styleChange);
        radioGroup_styleChange = (RadioGroup) findViewById(R.id.radioGroup_styleChange);
        initStyleChangeCheck();
    }

    private void initStyleChangeCheck() {
        if (mThemeHelper.isTheme1()) {
            radioGroup_styleChange.check(R.id.radioButton_styleChange_1);
            imageView_styleChange.setImageResource(R.drawable.theme_pic_1);
            themeMode = ThemeMode.THEME1;
        } else {
            themeMode = ThemeMode.NIGHT;
            imageView_styleChange.setImageResource(R.drawable.switch_mode_night);
        }
    }

    private void initListener() {
        if (radioGroup_styleChange != null) {
            radioGroup_styleChange.setOnCheckedChangeListener(this);
        }
        if (textView_congfirm != null) {
            textView_congfirm.setOnClickListener(this);
        }
        if (imageView_back_styleChange != null) {
            imageView_back_styleChange.setOnClickListener(this);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.radioButton_styleChange_1:
                imageView_styleChange.setImageResource(R.drawable.theme_pic_1);
                radioGroup_styleChange.check(R.id.radioButton_styleChange_1);
                themeMode = ThemeMode.THEME1;
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.me_set_cli_theme1);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.textView_confirm_styleChange:
                if (themeMode != null) {
                    mThemeHelper.setMode(themeMode);
                }
                goBackToHome();
                break;
            case R.id.imageView_back_styleChange:
                Map<String, String> data = new HashMap<>();
                data.put("type", "1");
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.BACK, data);
                finish();
                break;
        }
    }

    private void goBackToHome() {
        if (!currentThemeMode.equals(mThemeHelper.getMode())) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor edit = sharedPreferences.edit();
            if (ThemeMode.NIGHT.equals(currentThemeMode)) {
                edit.putInt("current_night_mode", Constants.MODE);
            }
            if (mThemeHelper.isNight()) {
                Constants.MODE = sharedPreferences.getInt("current_night_mode", 61);
            } else {
                Constants.MODE = sharedPreferences.getInt("current_light_mode", 51);
            }
            edit.putInt("content_mode", Constants.MODE);
            edit.apply();
            //通过Intent传递是否切换了主题的信息给设置页面
            Intent intent = new Intent(StyleChangeActivity.this, SettingActivity.class);
            intent.putExtra("isStyleChanged", true);
            startActivity(intent);
            if (SettingActivity.sInstance != null && !SettingActivity.sInstance.isFinishing()) {
                SettingActivity.sInstance.finish();
            }
        }
        finish();
    }
}
