package com.intelligent.reader.read.help;

import net.lzbook.kit.constants.Constants;

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

        Constants.READ_PARAGRAPH_SPACE = 1.0f;
        Constants.READ_CONTENT_PAGE_TOP_SPACE = 45;
        Constants.READ_CONTENT_PAGE_LEFT_SPACE = 20;
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
        Constants.MODE = index;
        editor.putInt("content_mode", Constants.MODE);
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
        editor.putInt("novel_font_size", Constants.FONT_SIZE);
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

    public int getDefaultChangeMode() {
        int default_change_mode = 61;
        switch (Constants.MODE) {
            case 51:
                default_change_mode = 61;
                break;
            case 52:
                default_change_mode = 56;
                break;
            case 53:
                default_change_mode = 57;
                break;
            case 54:
                default_change_mode = 55;
                break;
            case 55:
                default_change_mode = 54;
                break;
            case 56:
                default_change_mode = 52;
                break;
            case 57:
                default_change_mode = 53;
                break;
            case 58:
                default_change_mode = 51;
                break;
            case 59:
                default_change_mode = 61;
                break;
            case 60:
                default_change_mode = 61;
                break;
            case 61:
                default_change_mode = 61;
                break;
        }
        return default_change_mode;
    }
}
