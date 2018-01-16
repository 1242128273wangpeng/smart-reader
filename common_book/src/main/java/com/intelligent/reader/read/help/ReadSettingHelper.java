package com.intelligent.reader.read.help;

import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.ReadConfig;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.WindowManager;

/**
 * 阅读页设置实现类
 **/
public class ReadSettingHelper implements ReadSettingInterface {

    private Context context;
    private SharedPreferences sharedPreferences;

    public ReadSettingHelper(Context context) {
        this.context = context;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public void setRowSpacing(int spacing) {
        if (sharedPreferences == null) {
            this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("read_interlinear_space", spacing);
        editor.apply();
    }

    @Override
    public void saveLinearSpace() {
        if (sharedPreferences == null) {
            this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("read_content_page_left_space", 20);
        editor.putInt("read_content_page_top_space", 45);
        editor.putInt("read_paragraph_space", 10);
        editor.putBoolean("is_reading_custom_space", false);

        editor.apply();

        ReadConfig.INSTANCE.setREAD_PARAGRAPH_SPACE(1.0f);
        ReadConfig.INSTANCE.setREAD_CONTENT_PAGE_TOP_SPACE(45);
        ReadConfig.INSTANCE.setREAD_CONTENT_PAGE_LEFT_SPACE(20);
    }

    /**
     * 保存当前的屏幕亮度值，并使之生效
     */
    @Override
    public void setScreenBrightness(Activity activity, int brightness) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        Window window = activity.getWindow();
        WindowManager.LayoutParams localLayoutParams = window.getAttributes();
        localLayoutParams.screenBrightness = (float) brightness / 255.0F;
        window.setAttributes(localLayoutParams);
    }

    @Override
    public void closeAutoBrightness() {
        if (sharedPreferences == null) {
            this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean("auto_brightness", false);
        edit.apply();
    }

    @Override
    public void saveBrightness(int brightness) {
        if (sharedPreferences == null) {
            this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (editor != null) {
            // 保存应用设置的屏幕亮度值
            editor.putInt("screen_bright", brightness);
            editor.apply();
        }
    }

    @Override
    public void setReadMode(int index) {
        if (sharedPreferences == null) {
            this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        ReadConfig.INSTANCE.setMODE(index);
        editor.putInt("content_mode", ReadConfig.INSTANCE.getMODE());
        editor.apply();
    }

    @Override
    public void savePageAnimation(int mode) {
        if (sharedPreferences == null) {
            this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
        SharedPreferences.Editor page_editor = sharedPreferences.edit();
        page_editor.putInt("page_mode", mode);
        page_editor.apply();
    }

    @Override
    public void saveFontSize() {
        if (sharedPreferences == null) {
            this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
        // 保存字体
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("novel_font_size", ReadConfig.INSTANCE.getFONT_SIZE());
        editor.apply();
    }

    @Override
    public void closeBrightnessWithSystem() {
        if (sharedPreferences == null) {
            this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean("auto_brightness", false);
        edit.apply();
    }
}
