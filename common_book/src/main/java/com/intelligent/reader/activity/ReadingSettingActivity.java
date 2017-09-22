package com.intelligent.reader.activity;

import com.intelligent.reader.R;
import com.intelligent.reader.read.help.ReadSettingHelper;
import com.intelligent.reader.read.page.PreviewPageView;

import net.lzbook.kit.book.view.SwitchButton;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.SettingItems;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.ResourceUtil;
import net.lzbook.kit.utils.SettingItemsHelper;
import net.lzbook.kit.utils.StatServiceUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.text.NumberFormat;

import iyouqu.theme.FrameActivity;

/**
 * 阅读页设置
 */
public class ReadingSettingActivity extends FrameActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener, SwitchButton.OnCheckedChangeListener {
    public boolean isCustomReadingSpace;
    public Context context;
    private SharedPreferences sharedPreferences;
    //预览界面
    private FrameLayout reading_setting_preview;
    private ImageView reading_setting_back;
    private ImageView reading_setting_reduce_text;
    private ImageView reading_setting_increase_text;
    private TextView reading_setting_text_size;
    private RadioGroup reading_setting_backdrop_group;
    private RadioGroup reading_setting_row_spacing_group;
    private RadioButton reading_spacing_0_2;
    private RadioButton reading_spacing_0_5;
    private RadioButton reading_spacing_1_0;
    private RadioButton reading_spacing_1_5;
    private RadioGroup reading_setting_animation_group;
    private RadioGroup reading_setting_direction_group;
    private SwitchButton reading_brightness_width_system;
    private SwitchButton reading_setting_volume_flip;
    private SettingItemsHelper settingsHelper;
    private SettingItems settings;
    private boolean autoBrightness;
    private int previewHeight = 400;
    private PreviewPageView previewPageView;

    private ReadSettingHelper readSettingHelper;

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_reading_setting);
        context = this;
        readSettingHelper = new ReadSettingHelper(getApplicationContext());
        initView();
    }

    private void initView() {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //阅读页翻页模式
        Constants.PAGE_MODE = sharedPreferences.getInt("page_mode", 0);

        //自动阅读
        boolean isAutoBrightness = sharedPreferences.getBoolean("auto_brightness", true);
        if (isAutoBrightness) {
            autoBrightness = true;
        } else {
            autoBrightness = false;
        }

        //预览界面
        reading_setting_preview = (FrameLayout) findViewById(R.id.reading_setting_preview);
        //返回
        reading_setting_back = (ImageView) findViewById(R.id.reading_setting_back);
        //阅读页字体大小设置
        reading_setting_reduce_text = (ImageView) findViewById(R.id.reading_setting_reduce_text);
        reading_setting_increase_text = (ImageView) findViewById(R.id.reading_setting_increase_text);
        reading_setting_text_size = (TextView) findViewById(R.id.reading_setting_text_size);
        //阅读页背景设置
        reading_setting_backdrop_group = (RadioGroup) findViewById(R.id.reading_setting_backdrop_group);
        //阅读页行距设置
        reading_setting_row_spacing_group = (RadioGroup) findViewById(R.id.reading_setting_row_spacing_group);
        reading_spacing_0_2 = (RadioButton) findViewById(R.id.reading_spacing_0_2);
        reading_spacing_0_5 = (RadioButton) findViewById(R.id.reading_spacing_0_5);
        reading_spacing_1_0 = (RadioButton) findViewById(R.id.reading_spacing_1_0);
        reading_spacing_1_5 = (RadioButton) findViewById(R.id.reading_spacing_1_5);
        //阅读页动画设置
        reading_setting_animation_group = (RadioGroup) findViewById(R.id.reading_setting_animation_group);
        //阅读页屏幕方向
        reading_setting_direction_group = (RadioGroup) findViewById(R.id.reading_setting_direction_group);

        reading_brightness_width_system = (SwitchButton) findViewById(R.id.reading_brightness_width_system);
        //音量键翻页
        reading_setting_volume_flip = (SwitchButton) findViewById(R.id.reading_setting_volume_flip);

        reading_setting_back.setOnClickListener(this);
        reading_setting_reduce_text.setOnClickListener(this);
        reading_setting_increase_text.setOnClickListener(this);
        reading_setting_backdrop_group.setOnCheckedChangeListener(this);
        reading_setting_row_spacing_group.setOnCheckedChangeListener(this);
        reading_setting_animation_group.setOnCheckedChangeListener(this);
        reading_setting_direction_group.setOnCheckedChangeListener(this);
        reading_brightness_width_system.setOnCheckedChangeListener(this);
        reading_setting_volume_flip.setOnCheckedChangeListener(this);

        settingsHelper = SettingItemsHelper.getSettingHelper(getApplicationContext());
        settings = settingsHelper.getValues();
        changeVolumeStatus(settings.isVolumeTurnover);

        // 横竖屏切换 跟随系统亮度时,保持亮度一致
        if (autoBrightness) {
            openSystemLight();
        }

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        previewPageView = new PreviewPageView(getApplicationContext(), getResources(), (int) (displayMetrics.widthPixels - 24 * displayMetrics.scaledDensity), previewHeight);

        initShowCacheState();
        isCustomSpaceSet();
        initPageMode();
        setFontSize();
        setOrientation();

        initPreview();
    }

    private void initPreview() {

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        reading_setting_preview.addView(previewPageView, new FrameLayout.LayoutParams((int) (displayMetrics.widthPixels - 24 * displayMetrics.scaledDensity), previewHeight));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reading_setting_reduce_text:
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_read_size_add);
                decreaseFont();
                break;
            case R.id.reading_setting_increase_text:
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_read_size_dec);
                increaseFont();
                break;
            case R.id.reading_setting_back:
                finish();
                break;
        }
    }

    /*
     * 初始化屏幕方向
     */
    private void setOrientation() {
        if (sharedPreferences.getInt("screen_mode", 1) == Configuration.ORIENTATION_PORTRAIT) {
            reading_setting_direction_group.check(R.id.reading_direction_portrait);
            Constants.IS_LANDSCAPE = false;
        } else if (sharedPreferences.getInt("screen_mode", 1) == Configuration.ORIENTATION_LANDSCAPE && this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            reading_setting_direction_group.check(R.id.reading_direction_landscape);
            Constants.IS_LANDSCAPE = true;
        }
    }

    private void changeVolumeStatus(boolean status) {
        reading_setting_volume_flip.setChecked(status);
        settingsHelper.putBoolean(settingsHelper.volumeTurnover, status);
        Constants.isVolumeTurnover = status;
    }

    private void changeSystemLight() {
        if (autoBrightness) {
            closeSystemLight();
        } else {
            openSystemLight();
        }
    }

    private void initPageMode() {
        if (Constants.PAGE_MODE == 0) {
            reading_setting_animation_group.check(R.id.reading_animation_slide);
        } else if (Constants.PAGE_MODE == 1) {
            reading_setting_animation_group.check(R.id.reading_animation_simulation);
        } else if (Constants.PAGE_MODE == 2) {
            reading_setting_animation_group.check(R.id.reading_animation_translation);
        }
    }

    /**
     * 打开系统亮度
     */
    private void openSystemLight() {
        setBrightnessBackground(true);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean("auto_brightness", true);
        edit.apply();

        if (!isActive) {
            isActive = true;
            setDisplayState();// 得到系统亮度，设置应用亮度
        }
        setScreenBrightness(this, -1);
        setScreenOffTimeout(commonLockTime);
    }

    /**
     * 关闭系统亮度
     */
    private void closeSystemLight() {
        setBrightnessBackground(false);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean("auto_brightness", false);
        edit.apply();
        if (!isActive) {
            isActive = true;
            setDisplayState();// 得到系统亮度，设置应用亮度
        }
        if (sharedPreferences != null && !sharedPreferences.getBoolean("auto_brightness", true)) {
            int screenbright = sharedPreferences.getInt("screen_bright", -1);
            if (screenbright >= 0) {
                setScreenBrightness(this, 20 + screenbright);
            } else if (mSystemBrightness >= 20) {
                setScreenBrightness(this, mSystemBrightness);
            } else {
                setScreenBrightness(this, 20);
            }
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.reading_backdrop_first:
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_read_bg_01);
                setNovelMode(51);
                break;
            case R.id.reading_backdrop_second:
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_read_bg_02);
                setNovelMode(52);
                break;
            case R.id.reading_backdrop_third:
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_read_bg_03);
                setNovelMode(53);
                break;
            case R.id.reading_backdrop_fourth:
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_read_bg_04);
                setNovelMode(54);
                break;
            case R.id.reading_backdrop_fifth:
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_read_bg_05);
                setNovelMode(55);
                break;
            case R.id.reading_backdrop_sixth:
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_read_bg_06);
                setNovelMode(56);
                break;
            case R.id.reading_backdrop_seventh:
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_read_bg_07);
                setNovelMode(57);
                break;
            case R.id.reading_backdrop_eighth:
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_read_bg_08);
                setNovelMode(58);
                break;
            case R.id.reading_backdrop_ninth:
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_read_bg_09);
                setNovelMode(59);
                break;
            case R.id.reading_backdrop_tenth:
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_read_bg_10);
                setNovelMode(60);
                break;
            case R.id.reading_backdrop_night:
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_read_bg_10);
                setNovelMode(61);
                break;
            case R.id.reading_spacing_0_2:
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_read_row_1);
                if (reading_spacing_0_2.isChecked()) {
                    Constants.READ_INTERLINEAR_SPACE = 0.2f;
                    setInterLinearSpaceMode();
                }
                break;
            case R.id.reading_spacing_0_5:
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_read_row_2);
                if (reading_spacing_0_5.isChecked()) {
                    Constants.READ_INTERLINEAR_SPACE = 0.5f;
                    setInterLinearSpaceMode();
                }
                break;
            case R.id.reading_spacing_1_0:
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_read_row_3);
                if (reading_spacing_1_0.isChecked()) {
                    Constants.READ_INTERLINEAR_SPACE = 1.0f;
                    setInterLinearSpaceMode();
                }
                break;
            case R.id.reading_spacing_1_5:
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_read_row_4);
                if (reading_spacing_1_5.isChecked()) {
                    Constants.READ_INTERLINEAR_SPACE = 1.5f;
                    setInterLinearSpaceMode();
                }
                break;
            case R.id.reading_animation_slide:
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_read_slide);
                changePageMode(0);
                break;
            case R.id.reading_animation_translation:
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_read_trans);
                changePageMode(2);
                break;
            case R.id.reading_animation_simulation:
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_read_simul);
                changePageMode(1);
                break;
            case R.id.reading_direction_portrait:
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_read_scr_ver);
                changeScreenMode(R.id.reading_direction_portrait);
                break;
            case R.id.reading_direction_landscape:
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_read_scr_land);
                changeScreenMode(R.id.reading_direction_landscape);
                break;
            default:
                break;
        }
    }

    // 根据页间距默认值判断是否为自定义间距
    private void isCustomSpaceSet() {

        NumberFormat numFormat = NumberFormat.getNumberInstance();
        numFormat.setMaximumFractionDigits(2);
        Constants.READ_INTERLINEAR_SPACE = sharedPreferences.getInt("read_interlinear_space", 3) * 0.1f;//阅读页行间距
        try {
            Constants.READ_INTERLINEAR_SPACE = Float.valueOf(numFormat.format(Constants.READ_INTERLINEAR_SPACE));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        AppLog.d("ReadSettingView", "Constants.READ_INTERLINEAR_SPACE " + Constants.READ_INTERLINEAR_SPACE);
        Constants.READ_PARAGRAPH_SPACE = sharedPreferences.getInt("read_paragraph_space", 10) * 0.1f;//阅读页段间距
        try {
            Constants.READ_PARAGRAPH_SPACE = Float.valueOf(numFormat.format(Constants.READ_PARAGRAPH_SPACE));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        Constants.READ_CONTENT_PAGE_TOP_SPACE = sharedPreferences.getInt("read_content_page_top_space", 45); //阅读页内容上下页边距
        Constants.READ_CONTENT_PAGE_LEFT_SPACE = sharedPreferences.getInt("read_content_page_left_space", 20); //阅读页内容页左右边距

        // 老版本左右边距修正
        if (Constants.READ_CONTENT_PAGE_LEFT_SPACE != 20) {
            Constants.READ_CONTENT_PAGE_LEFT_SPACE = 20;
            sharedPreferences.edit().putInt("read_content_page_left_space", 20).apply();
        }

        AppLog.d("ReadSettingView", "isCustomSpaceSetted_Constants.READ_INTERLINEAR_SPACE " + Constants.READ_INTERLINEAR_SPACE);
        if (Constants.READ_INTERLINEAR_SPACE == 0.5f || Constants.READ_INTERLINEAR_SPACE == 0.2f || Constants.READ_INTERLINEAR_SPACE == 1.0f || Constants.READ_INTERLINEAR_SPACE == 1.5f) {
            AppLog.d("ReadSettingView", "READ_CONTENT_PAGE_LEFT_SPACE—— " + Constants.READ_CONTENT_PAGE_LEFT_SPACE);
            AppLog.d("ReadSettingView", "READ_CONTENT_PAGE_TOP_SPACE—— " + Constants.READ_CONTENT_PAGE_TOP_SPACE);
            AppLog.d("ReadSettingView", "READ_PARAGRAPH_SPACE—— " + Constants.READ_PARAGRAPH_SPACE);
            if (Constants.READ_CONTENT_PAGE_LEFT_SPACE == 20 && Constants.READ_CONTENT_PAGE_TOP_SPACE == 45
                    && Constants.READ_PARAGRAPH_SPACE == 1.0f) {

                isCustomReadingSpace = false;
                switchSpaceState();
            } else {
                isCustomReadingSpace = true;
                reading_setting_row_spacing_group.clearCheck();
            }
        } else {
            isCustomReadingSpace = true;
            reading_setting_row_spacing_group.clearCheck();
        }
    }

    /**
     * 初始化阅读模式字体
     */
    public void initShowCacheState() {
        int content_mode = sharedPreferences.getInt("content_mode", 51);
        if ("night".equals(ResourceUtil.mode)) {
            if (content_mode < 50) {
                setNovelMode(54);
            } else {
                setNovelMode(content_mode);
            }
        } else {
            setNovelMode(content_mode);
        }
    }

    /**
     * 横屏切换
     */
    private void changeScreenMode(int id) {
        SharedPreferences.Editor screen_mode = sharedPreferences.edit();

        if (id == R.id.reading_direction_landscape) {
            screen_mode.putInt("screen_mode", Configuration.ORIENTATION_LANDSCAPE);
            Constants.IS_LANDSCAPE = true;
        } else if (id == R.id.reading_direction_portrait) {
            Constants.IS_LANDSCAPE = false;
            screen_mode.putInt("screen_mode", Configuration.ORIENTATION_PORTRAIT);
        }
        screen_mode.apply();
    }

    /**
     * 翻页模式：0 滑动 1 仿真 2 平移 3 上下
     */
    private void changePageMode(int mode) {
        SharedPreferences.Editor page_editor = sharedPreferences.edit();
        page_editor.putInt("page_mode", mode);
        page_editor.apply();

        Constants.PAGE_MODE = mode;
    }

    public void setInterLinearSpaceMode() {
        switchSpaceState();

        if (sharedPreferences == null) {
            this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("read_content_page_left_space", 20);
        editor.putInt("read_content_page_top_space", 45);
        editor.putInt("read_paragraph_space", 8);
        editor.putBoolean("is_reading_custom_space", false);

        editor.apply();

        Constants.READ_PARAGRAPH_SPACE = 1.0f;
        Constants.READ_CONTENT_PAGE_TOP_SPACE = 45;
        Constants.READ_CONTENT_PAGE_LEFT_SPACE = 20;
        isCustomReadingSpace = false;

        if (previewPageView != null) {
            previewPageView.drawPreview();
        }
    }

    // 单选切换行间距
    private void switchSpaceState() {

        if (sharedPreferences == null) {
            this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (Constants.READ_INTERLINEAR_SPACE == 0.2f) {
            reading_setting_row_spacing_group.check(R.id.reading_spacing_0_2);
            editor.putInt("read_interlinear_space", 2);
            editor.apply();

        } else if (Constants.READ_INTERLINEAR_SPACE == 0.3f) {
            reading_setting_row_spacing_group.check(R.id.reading_spacing_0_5);
            editor.putInt("read_interlinear_space", 3);
            editor.apply();

        } else if (Constants.READ_INTERLINEAR_SPACE == 0.4f) {
            reading_setting_row_spacing_group.check(R.id.reading_spacing_1_0);
            editor.putInt("read_interlinear_space", 4);
            editor.apply();

        } else if (Constants.READ_INTERLINEAR_SPACE == 0.5f) {
            reading_setting_row_spacing_group.check(R.id.reading_spacing_1_5);
            editor.putInt("read_interlinear_space", 5);
            editor.apply();

        }
    }

    private void restoreBright() {
        setBrightnessBackground();
        AppLog.e("restoreBrightness", "isSystemAutoBrightness:" + autoBrightness);
        int brightness = sharedPreferences.getInt("screen_bright", -1);
        if (autoBrightness) {
            openSystemLight();
        }
    }

    public void setBrightnessBackground(boolean autoBrightness) {
        this.autoBrightness = autoBrightness;
        // 这里做改变系统亮度的按钮背景
        setBrightBtn();
    }

    private void setBrightnessBackground() {
        setBrightnessBackground(autoBrightness);
    }

    private void setBrightBtn() {
        if ("light".equals(ResourceUtil.mode)) {
            if (autoBrightness) {
                reading_brightness_width_system.setChecked(true);
            } else {
                reading_brightness_width_system.setChecked(false);
            }
        } else {
            if (autoBrightness) {
                reading_brightness_width_system.setChecked(true);
            } else {
                reading_brightness_width_system.setChecked(false);
            }
        }
    }

    public void setNovelMode(int index) {
        if (Constants.MODE == 55 || Constants.MODE == 58) {
            restoreBright();
        }
        switch (index) {
            case 51:
                readSettingHelper.setReadMode(index);
                changeMode(51);
                reading_setting_backdrop_group.check(R.id.reading_backdrop_first);
                break;
            case 52:
                readSettingHelper.setReadMode(index);
                changeMode(52);
                reading_setting_backdrop_group.check(R.id.reading_backdrop_second);
                break;
            case 53:
                readSettingHelper.setReadMode(index);
                changeMode(53);
                reading_setting_backdrop_group.check(R.id.reading_backdrop_third);
                break;
            case 54:
                readSettingHelper.setReadMode(index);
                changeMode(54);
                reading_setting_backdrop_group.check(R.id.reading_backdrop_fourth);
                break;
            case 55:
                readSettingHelper.setReadMode(index);
                changeMode(55);
                reading_setting_backdrop_group.check(R.id.reading_backdrop_fifth);
                break;
            case 56:
                readSettingHelper.setReadMode(index);
                changeMode(56);
                reading_setting_backdrop_group.check(R.id.reading_backdrop_sixth);
                break;
            case 61:
                readSettingHelper.setReadMode(index);
                changeMode(61);
                reading_setting_backdrop_group.check(R.id.reading_backdrop_night);
                break;
            default:
                break;
        }
    }

    private void changeMode(int mode) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (mode == 1 || mode == 4 || mode == 7 || mode == 9) {
            if ("light".equals(ResourceUtil.mode)) {
                editor.putString("mode", "night");
                ResourceUtil.mode = "night";
                editor.apply();
            }
        } else {
            if ("night".equals(ResourceUtil.mode)) {
                editor.putString("mode", "light");
                ResourceUtil.mode = "light";
                editor.apply();
            }
        }
        if (mode == 51) {
            if (previewPageView != null) {
                previewPageView.setTextColor(getResources().getColor(R.color.reading_text_color_first));
            }
        } else if (mode == 52) {
            if (previewPageView != null) {
                previewPageView.setTextColor(getResources().getColor(R.color.reading_text_color_second));
            }
        } else if (mode == 52) {
            if (previewPageView != null) {
                previewPageView.setTextColor(getResources().getColor(R.color.reading_text_color_third));
            }
        } else if (mode == 54) {
            if (previewPageView != null) {
                previewPageView.setTextColor(getResources().getColor(R.color.reading_text_color_fourth));
            }
        } else if (mode == 55) {
            if (previewPageView != null) {
                previewPageView.setTextColor(getResources().getColor(R.color.reading_text_color_fifth));
            }
        } else if (mode == 56) {
            if (previewPageView != null) {
                previewPageView.setTextColor(getResources().getColor(R.color.reading_text_color_sixth));
            }
        } else if (mode == 61) {
            if (previewPageView != null) {
                previewPageView.setTextColor(getResources().getColor(R.color.reading_text_color_night));
            }
        } else {
            if (previewPageView != null) {
                previewPageView.setTextColor(getResources().getColor(R.color.reading_text_color_first));
            }
        }

        if (previewPageView != null) {
            previewPageView.drawPreview();
        }
    }

    /**
     * 减小字体
     */
    private void decreaseFont() {
        if (Constants.FONT_SIZE > 10) {
            if (Constants.FONT_SIZE == 30) {
                reading_setting_increase_text.setEnabled(true);
            }
            Constants.FONT_SIZE -= 2;
            if (Constants.FONT_SIZE <= 10) {
                reading_setting_reduce_text.setEnabled(false);
            }
            setFontSize();

            readSettingHelper.saveFontSize();
        }
    }

    /**
     * 增大字体
     */
    private void increaseFont() {
        if (Constants.FONT_SIZE < 30) {
            if (Constants.FONT_SIZE == 10) {
                reading_setting_reduce_text.setEnabled(true);
            }
            Constants.FONT_SIZE += 2;
            if (Constants.FONT_SIZE >= 30) {
                reading_setting_increase_text.setEnabled(false);
            }
            setFontSize();

            readSettingHelper.saveFontSize();
        }
    }

    private void setFontSize() {
        if (reading_setting_text_size != null) {
            reading_setting_text_size.setText(String.valueOf(Constants.FONT_SIZE));
        }

        if (previewPageView != null) {
            previewPageView.drawPreview();
        }
    }

    @Override
    public void onCheckedChanged(SwitchButton view, boolean isChecked) {
        if (view == null) {
            return;
        }
        switch (view.getId()) {
            case R.id.reading_brightness_width_system:
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_read_Bri_sys);
                changeSystemLight();
                break;
            case R.id.reading_setting_volume_flip:
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_read_volu_tur);
                boolean isVolumeTurnover = !settings.isVolumeTurnover;
                changeVolumeStatus(isVolumeTurnover);
                break;
        }
    }
}
