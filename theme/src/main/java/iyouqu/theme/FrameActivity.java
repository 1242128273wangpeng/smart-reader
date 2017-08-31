package iyouqu.theme;

import com.baidu.mobstat.StatService;

import net.lzbook.kit.ad.OwnNativeAdManager;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.utils.ATManager;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.ResourceUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.annotation.AttrRes;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class FrameActivity extends AppCompatActivity {
    protected String TAG = "FrameActivity";
    protected static boolean isActive;
    private Toast toast;
    private String mode;
    // 全局亮度
    public static int mSystemBrightness = 0;
    private static boolean isSystemAutoBrightness;
    private static SharedPreferences sp;
    // 屏幕超时时间
    public static int systemLockTime;
    protected final static int commonLockTime = 5 * 60 * 1000;

    //检测自身是不是前台运行app
    private boolean isCurrentRunningForeground = true;

    //记录切换出去的时间
    private static long outTime;
    private static long inTime;

    public ThemeHelper mThemeHelper;

    @SuppressLint("NewApi")
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        initThemeHelper();
        initTheme();
        ATManager.addActivity(this);
        StatusBarCompat.compat(this, getStatusBarColorId());
    }

    public @AttrRes int getStatusBarColorId(){
        return R.attr.color_statusBar;
    }

    @Override
    public void onBackPressed() {
        Map<String, String> data = new HashMap<>();
        data.put("type","2");
        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.BACK, data);
        finish();
    }

    /**
     * 初始化主题助手
     */
    private void initThemeHelper() {
        if(mThemeHelper == null){
            mThemeHelper =  new ThemeHelper(this);
        }
    }

    /**
     * 初始化当前主题
     */
    private void initTheme() {
        if(mThemeHelper.isNight()){
            setTheme(R.style.Night);
        }else if (mThemeHelper.isTheme1()){
            setTheme(R.style.Theme1);
        }else {
            setTheme(R.style.Theme1);
            mThemeHelper.setMode(ThemeMode.THEME1);
        }
    }

    /**
     * 是否修改模式
     */
    protected boolean isModeChange() {
        boolean change = !ResourceUtil.mode.equals(mode);
        if (change) {
            this.mode = ResourceUtil.mode;
        }
        return change;
    }

    /**
     * 修改模式
     */
    protected void setMode() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        StatService.onResume(getApplicationContext());
        if (!isActive) {
            isActive = true;
            setDisplayState();// 得到系统亮度，设置应用亮度
        }
        if (sp != null && !sp.getBoolean("auto_brightness", true)) {
            int screenbright = sp.getInt("screen_bright", -1);
            if (screenbright >= 0) {
                setScreenBrightness(this, 20 + screenbright);
            } else if (mSystemBrightness >= 20) {
                setScreenBrightness(this, mSystemBrightness);
            } else {
                setScreenBrightness(this, 20);
            }
        } else {
            setScreenBrightness(this, -1);
        }
        setScreenOffTimeout(commonLockTime);
    }

    protected void setDisplayState() {
        // 全局亮度
        sp = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        boolean autoBrightness = sp.getBoolean("auto_brightness", true);
        if (!autoBrightness) {
            setReaderDisplayBrightness();
        }
        systemLockTime = getSysetemScreenOffTimeout();
        AppLog.d(TAG, "systemLockTime " + systemLockTime);
    }

    protected void setReaderDisplayBrightness() {
        isSystemAutoBrightness = isSystemAutoBrightness(this);
        AppLog.d(TAG, isSystemAutoBrightness + "");
        if (isSystemAutoBrightness) {
            stopAutoBrightness(this);
        }
        mSystemBrightness = getScreenBrightness(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isAppOnForeground()) {
            isActive = false;
            restoreSystemDisplayState();
        }
        isCurrentRunningForeground = isAppOnForeground();
        if (!isCurrentRunningForeground) {
            outTime = System.currentTimeMillis();
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.HOME);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!isCurrentRunningForeground) {
            inTime =System.currentTimeMillis();
            Map<String, String> data = new HashMap<>();
            data.put("time", String.valueOf(inTime-outTime));
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.ACTIVATE, data);
        }
        if (!isCurrentRunningForeground&&!Constants.isHideAD&& Constants.isShowSwitchSplashAd&& NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
            boolean isShowSwitchSplash = inTime-outTime> Constants.switchSplash_ad_sec*1000;
            if(isShowSwitchSplash){
                OwnNativeAdManager.toSwitchAdActivity(this);
            }
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * 还原系统亮度
     */
    public void restoreSystemDisplayState() {
        boolean autoBrightness = sp.getBoolean("auto_brightness", true);
        if (!autoBrightness) {
            restoreBrightness();
        }
        setScreenOffTimeout(systemLockTime);
    }

    protected void restoreBrightness() {
        setScreenBrightness(this, -1);
        if (isSystemAutoBrightness) {
            startAutoBrightness(this);
        }
    }

    /**
     * 停止自动亮度调节
     */
    public void stopAutoBrightness(Activity activity) {
        try {
            Settings.System.putInt(activity.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开启亮度自动调节
     */
    public void startAutoBrightness(Activity activity) {
        try {
            Settings.System.putInt(activity.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        StatService.onPause(getApplicationContext());
    }

    /**
     * 程序是否在前台运行
     */
    public boolean isAppOnForeground() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(ACTIVITY_SERVICE);
        String packageName = getApplicationContext().getPackageName();
        List<RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;
        for (RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 操作
        return true;
    }

    public void showToastShort(String s) {
        if (TextUtils.isEmpty(s)) {
            return;
        }
        if (toast == null) {
            toast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
        } else {
            toast.setText(s);
        }
        if (toast != null) {
            toast.show();
        }
    }

    public void showToastShort(int resId) {
        if (TextUtils.isEmpty(String.valueOf(resId))) {
            return;
        }
        if (toast == null) {
            toast = Toast.makeText(this, resId, Toast.LENGTH_SHORT);
        } else {
            toast.setText(resId);
        }
        if (toast != null) {
            toast.show();
        }
    }

    public void showToastLong(String s) {
        if (TextUtils.isEmpty(s)) {
            return;
        }
        if (toast == null) {
            toast = Toast.makeText(this, s, Toast.LENGTH_LONG);
        } else {
            toast.setText(s);
        }
        if (toast != null)
            toast.show();
    }

    public void showToastLong(int resId) {
        if (TextUtils.isEmpty(String.valueOf(resId))) {
            return;
        }
        if (toast == null) {
            toast = Toast.makeText(this, resId, Toast.LENGTH_LONG);
        } else {
            toast.setText(resId);
        }
        if (toast != null)
            toast.show();
    }

    public String getVersion() {
        String version = "0.0.0";
        PackageManager pm = getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    public String getVersionCode() {
        String versionCode = "";
        PackageManager pm = getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
            versionCode = String.valueOf(packageInfo.versionCode);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 获取屏幕的亮度
     */
    public int getScreenBrightness(Activity activity) {
        int nowBrightnessValue = 0;
        // 如果系统没有保存屏幕亮度值，则取当前屏幕亮度
        ContentResolver resolver = activity.getContentResolver();
        try {
            nowBrightnessValue = Settings.System.getInt(
                    resolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        AppLog.d(TAG, nowBrightnessValue
                + "===nowBrightnessValue===getScreenBrightness");
        return nowBrightnessValue;
    }

    /**
     * 判断系统是否开启了自动亮度调节
     */
    public boolean isSystemAutoBrightness(Activity activity) {
        boolean automicBrightness = false;
        try {
            automicBrightness = Settings.System.getInt(
                    activity.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        return automicBrightness;
    }

    /**
     * 保存当前的屏幕亮度值，并使之生效
     */
    public void setScreenBrightness(Activity activity, int paramInt) {
        Window localWindow = activity.getWindow();
        WindowManager.LayoutParams localLayoutParams = localWindow
                .getAttributes();
        localLayoutParams.screenBrightness = paramInt / 255.0F;
        localWindow.setAttributes(localLayoutParams);
    }

    /**
     * 设置屏幕超时时间
     */
    public void setScreenOffTimeout(int time) {
        AppLog.d(TAG, "setScreenOffTimeout time " + time);
        try {
            if (time == Integer.MAX_VALUE) {
                //屏幕常亮
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                Settings.System.putInt(this.getContentResolver(),
                        Settings.System.SCREEN_OFF_TIMEOUT, time);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取屏幕超时时间
     */
    private int getSysetemScreenOffTimeout() {
        int timeout = 0;
        try {
            timeout = Settings.System.getInt(this.getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        return timeout;
    }

    @Override
    public void finish() {
        super.finish();
        ATManager.removeActivity(this);
        toast = null;
    }
}
