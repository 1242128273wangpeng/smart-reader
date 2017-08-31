package com.intelligent.reader.read.help;

import android.app.Activity;

/**
 * 阅读页设置接口
 **/

public interface ReadSettingInterface {

    //设置行距
    void setRowSpacing(int spacing);

    void saveLinearSpace();

    void setScreenBrightness(Activity activity, int brightness);

    void closeAutoBrightness();

    void saveBrightness(int brightness);

    void setReadMode(int index);

    void savePageAnimation(int mode);

    void saveFontSize();

    void closeBrightnessWithSystem();

}
