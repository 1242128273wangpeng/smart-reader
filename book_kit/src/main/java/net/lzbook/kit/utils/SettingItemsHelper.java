package net.lzbook.kit.utils;

import net.lzbook.kit.base.BaseBookApplication;
import net.lzbook.kit.bean.SettingItems;
import net.lzbook.kit.utils.logger.AppLog;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * <设置选项数据通过文件方式操作>
 */
public class SettingItemsHelper {
    public static SettingItemsHelper settingHelper;
    public SettingItems settings;
    public String openBookPush = "settings_push";
    public String openUmengPush = "umeng_push";
    public String openPushSound = "push_sound";
    public String setPushTime = "push_time";
    public String pushTimeStartH = "push_time_start_hour";
    public String pushTimeStartMin = "push_time_start_minute";
    public String pushTimeStopH = "push_time_stop_hour";
    public String pushTimeStopMin = "push_time_stop_minute";
    public String speedMode = "speed_mode";
    public String volumeTurnover = "sound_turnover";
    public String followSystemBrightness = "auto_brightness";
    public String appBrightness = "screen_bright";
    public String noPicMode = "not_net_img_mode";
    public String autoDownLoad = "auto_download_wifi";
    public String booklistSortType = "booklist_sort_type";
    Context context;
    SharedPreferences preferences;

    private SettingItemsHelper(Context context) {
        this.context = context;
        settings = new SettingItems();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public synchronized static SettingItemsHelper getSettingHelper(Context context) {
        if (settingHelper == null) {
            settingHelper = new SettingItemsHelper(context);
        }
        return settingHelper;
    }

    public SettingItems getValues() {//给内存数据赋值
        settings.isBookUpdatePush = getBoolean(openBookPush, true);
        settings.isUmengPush = getBoolean(openUmengPush, true);
        settings.isSoundOpen = getBoolean(openPushSound, true);
        settings.isSetPushTime = getBoolean(setPushTime, false);

        settings.pushTimeStartH = getInt(pushTimeStartH, 7);
        settings.pushTimeStartMin = getInt(pushTimeStartMin, 0);
        settings.pushTimeStopH = getInt(pushTimeStopH, 23);
        settings.pushTimeStopMin = getInt(pushTimeStopMin, 0);

        settings.isFollowSystemBrightness = getBoolean(followSystemBrightness, true);
        settings.appBrightness = getInt(appBrightness, -1);
        if (BaseBookApplication.getGlobalContext().getPackageName().equals("cn.qbmfkkydq.reader")){
            // 全本免费快看阅读器默认排序方式为添加顺序
            settings.booklist_sort_type = getInt(booklistSortType, 2);
        }else{
            settings.booklist_sort_type = getInt(booklistSortType, 0);
        }
        settings.isVolumeTurnover = getBoolean(volumeTurnover, true);
        AppLog.d("SettingItemsHelper", settings.toString());
        return settings;
    }

    private boolean getBoolean(String key, boolean defaultValue) {
        if (preferences != null) {
            try {
                return preferences.getBoolean(key, defaultValue);
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean putBoolean(final String key, final boolean value) {
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(key, value);
            if (editor.commit() && settings != null) {
                getValues();//修改文件成功后，修改内存
                return true;
            }
        }
        return false;
    }

    private int getInt(String key, int defaultValue) {
        if (preferences != null) {
            try {
                return preferences.getInt(key, defaultValue);
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public boolean putInt(final String key, final int value) {
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(key, value);
            if (editor.commit() && settings != null) {
                getValues();//修改文件成功后，修改内存
                return true;
            }
        }
        return false;
    }

    public void recycleResource() {
        if (this.context != null) {
            this.context = null;
        }

        if (this.preferences != null) {
            this.preferences = null;
        }
    }
}
