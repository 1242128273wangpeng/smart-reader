package net.lzbook.kit.ui.activity.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.annotation.NonNull;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.view.LayoutInflaterFactory;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.StateSet;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.dingyue.statistics.DyStatService;
import com.umeng.message.PushAgent;

import net.lzbook.kit.R;
import net.lzbook.kit.app.base.BaseBookApplication;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.pointpage.EventPoint;
import net.lzbook.kit.service.DynamicService;
import net.lzbook.kit.utils.ATManager;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.ResourceUtil;
import net.lzbook.kit.utils.logger.AppLog;
import net.lzbook.kit.utils.router.RouterConfig;
import net.lzbook.kit.utils.router.RouterUtil;
import net.lzbook.kit.utils.swipeback.SwipeBackHelper;
import net.lzbook.kit.utils.theme.StateListListenerDrawable;
import net.lzbook.kit.utils.theme.ThemeHelper;
import net.lzbook.kit.utils.theme.ThemeMode;
import net.lzbook.kit.utils.theme.statusbar.impl.FlymeHelper;
import net.lzbook.kit.utils.theme.statusbar.impl.MIUIHelper;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public abstract class FrameActivity extends AppCompatActivity implements SwipeBackHelper.SlideBackManager,
        SwipeBackHelper.SlideAnimListener, LifecycleOwner {
    protected final static int commonLockTime = 5 * 60 * 1000;
    public static final float ALPHA_FADE_TO = 0.3F;
    // 全局亮度
    public static int mSystemBrightness = 0;
    // 屏幕超时时间
    public static int systemLockTime;
    protected static boolean isActive;
    private static boolean isSystemAutoBrightness;
    private static SharedPreferences sp;
    //记录切换出去的时间
    private static long outTime;
    private static long inTime;
    private static long checkDynamicParameterTime;
    public ThemeHelper mThemeHelper;
    protected String TAG = "FrameActivity";
    protected View mNightShadowView;
    private String mode;
    //检测自身是不是前台运行app
    private boolean isCurrentRunningForeground = true;
    private boolean isFirst = true;
    private boolean isDarkStatusBarText = false;
    private String packageName;

    public static int UI_OPTIONS_IMMERSIVE_STICKY = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    public static int UI_OPTIONS_NORMAL = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_VISIBLE;

    private SwipeBackHelper swipeBackHelper;


    public boolean isMIUISupport = false;
    public boolean isFlymeSupport = false;

    private LifecycleRegistry lifecycleRegistry;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @SuppressLint("NewApi")
    public void onCreate(Bundle paramBundle) {
        LayoutInflaterCompat.setFactory(getLayoutInflater(), new LayoutInflaterFactory() {
            @Override
            public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
                return createViewWithPressState(parent, name, context, attrs);
            }
        });
        super.onCreate(paramBundle);

        lifecycleRegistry = new LifecycleRegistry(this);
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);

        if (isFirst) {
            hasGetPackageName();
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            UI_OPTIONS_IMMERSIVE_STICKY = getWindow().getDecorView().getSystemUiVisibility();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (isDarkStatusBarText || shouldLightStatusBase()) {
                isMIUISupport = new MIUIHelper().setStatusBarLightMode(this, true);
                isFlymeSupport = new FlymeHelper().setStatusBarLightMode(this, true);
                if (isMIUISupport || isFlymeSupport) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                }
            } else {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            if (isDarkStatusBarText || shouldLightStatusBase()) {
                getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        initThemeHelper();
        initTheme();
        //设置屏幕适配属性
        AppUtils.setCustomDensity(this,BaseBookApplication.getGlobalContext());
        AppUtils.initNormalWindow(this);
        //友盟推送
        if (AppUtils.hasUPush()) {
            final WeakReference<FrameActivity> weakReference=new WeakReference(FrameActivity.this);
            new AsyncTask(){
                @Override
                protected Object doInBackground(Object[] objects) {
                    if(weakReference!=null&&weakReference.get()!=null) {
                        PushAgent.getInstance(weakReference.get()).onAppStart();
                    }
                    return null;
                }
            }.execute();

        }

    }

    public SwipeBackHelper getSwipeBackHelper() {
        return swipeBackHelper;
    }

    protected View createViewWithPressState(View parent, String name, Context context, AttributeSet attrs) {
        View view = getDelegate().createView(parent, name, context, attrs);

        if (view != null && hasItemStateAttr(attrs)) {

            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.item_state);
            boolean changeAlpha = typedArray.getBoolean(R.styleable.item_state_onPressChangeAlpha, false);

            typedArray.recycle();


            Drawable background = view.getBackground();

            if (changeAlpha && parent != null && parent instanceof ViewGroup) {
                ((ViewGroup) parent).setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
            }


            if (changeAlpha) {

                final View tempView = view;
                StateListDrawable stateListDrawable = new StateListListenerDrawable(new Function2<Boolean, Boolean, Unit>() {
                    @Override
                    public Unit invoke(Boolean pressed, Boolean enable) {
                        if (pressed || !enable) {
                            tempView.setAlpha(ALPHA_FADE_TO);
                        } else {
                            tempView.setAlpha(1.0F);
                        }

                        return null;
                    }
                });

                if (background != null) {
                    stateListDrawable.addState(StateSet.WILD_CARD, background);
                }

                background = stateListDrawable;
            }


            ViewCompat.setBackground(view, background);
        }

        return view;
    }

    private boolean hasItemStateAttr(AttributeSet attrs) {
        int count = attrs.getAttributeCount();
        for (int i = 0; i < count; i++) {
            if ("onPressChangeAlpha".equals(attrs.getAttributeName(i))) {
                return true;
            }
            if ("onPressBackground".equals(attrs.getAttributeName(i))) {
                return true;
            }
            if ("onUnableBackground".equals(attrs.getAttributeName(i))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        Map<String, String> data = new HashMap<>();
        data.put("type", "2");
        DyStatService.onEvent(EventPoint.SYSTEM_BACK, data);

        if (swipeBackHelper != null && swipeBackHelper.isSliding()) return;//滑动返回未结束

        super.onBackPressed();
    }

    public void hasGetPackageName() {
        packageName = AppUtils.getPackageName();
        isFirst = false;

        if (!TextUtils.isEmpty(packageName) &&
                (packageName.equals("cc.kdqbxs.reader")
                        || packageName.equals("cn.txtqbmfyd.reader")
                        || packageName.equals("cn.qbmfrmxs.reader")
                        || packageName.equals("cc.quanbennovel")// 今日多看
                        || packageName.equals("cc.remennovel")// 智胜电子书替
                        || packageName.equals("cn.mfxsqbyd.reader"))) {// 免费小说全本阅读
            isDarkStatusBarText = true;
        } else {
            isDarkStatusBarText = false;
        }
    }

    public boolean shouldLightStatusBase(){
        return false;
    }

    /**
     * 初始化主题助手
     */

    private void initThemeHelper() {
        if (mThemeHelper == null) {
            mThemeHelper = ThemeHelper.getInstance(this);
        }
    }

    /**
     * 初始化当前主题
     */
    private void initTheme() {
        if (mThemeHelper.isNight()) {
            mThemeHelper.setMode(ThemeMode.NIGHT);
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


    public void nightShift(boolean flag, boolean animate) {
        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
        if (flag) {
            if (mNightShadowView == null) {
                mNightShadowView = new View(this);
                mNightShadowView.setBackgroundColor(Color.BLACK);
                mNightShadowView.setAlpha(Constants.NIGHT_SHADOW_ALPHA);
                mNightShadowView.setClickable(false);
                mNightShadowView.setFocusable(false);
                mNightShadowView.setId(R.id.night_shadow_view);

            }

            if (mNightShadowView.getParent() == null) {
                if (animate)
                    mNightShadowView.setAlpha(0f);
                decorView.addView(mNightShadowView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                if (animate) {
                    ViewPropertyAnimator animator = mNightShadowView.animate();
                    animator.alpha(0.55f);
                    animator.setDuration(300);
                    animator.start();
                }
            }

        } else if (mNightShadowView != null) {
            decorView.removeView(mNightShadowView);
        }


    }

    public boolean shouldShowNightShadow() {
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);

        if (shouldShowNightShadow())
            nightShift(mThemeHelper.isNight(), false);

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

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP);

        if (!isAppOnForeground()) {
            checkDynamicParameterTime = System.currentTimeMillis();
            isActive = false;
            DyStatService.onEvent(EventPoint.SYSTEM_HOME);
            isCurrentRunningForeground = false;
            restoreSystemDisplayState();
        }
        if (!Constants.isHideAD && Constants.isShowSwitchSplashAd && NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
            isCurrentRunningForeground = isAppOnForeground();
            if (!isCurrentRunningForeground) {
                outTime = System.currentTimeMillis();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START);

        if (!isCurrentRunningForeground) {
            inTime = System.currentTimeMillis();
            Map<String, String> data = new HashMap<>();
            data.put("time", String.valueOf(inTime - outTime));
            DyStatService.onEvent(EventPoint.SYSTEM_ACTIVATE, data);
            if (inTime - checkDynamicParameterTime > Constants.checkDynamicTime) {
                DynamicService.keepService(BaseBookApplication.getGlobalContext());
            }
        }

        if (!isCurrentRunningForeground && !Constants.isHideAD && Constants.isShowSwitchSplashAd && NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE && !AppUtils.isNeedAdControl(Constants.ad_control_other)) {
            boolean isShowSwitchSplash = inTime - outTime > Constants.switchSplash_ad_sec * 1000;
            outTime = System.currentTimeMillis();
            if (isShowSwitchSplash) {
                getWindow().getDecorView().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        RouterUtil.INSTANCE.navigation(FrameActivity.this, RouterConfig.SWITCH_AD_ACTIVITY);
                    }
                }, 200);
            }
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        try {
            super.onSaveInstanceState(outState);
        }catch (Throwable e){
            e.printStackTrace();
            AppLog.e("onSaveInstanceState:"+e.getMessage());
        }
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

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE);

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

        if (isTaskRoot()) {
            overridePendingTransition(R.anim.slide_left_in, 0);
        } else {
            overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
        }
    }

    @Override
    protected void onDestroy() {
        if (swipeBackHelper != null) {
            swipeBackHelper.finishSwipeImmediately();
            swipeBackHelper = null;
        }

        super.onDestroy();

        deleteDisposable();

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);

        ATManager.removeActivity(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (swipeBackHelper == null) {
            swipeBackHelper = new SwipeBackHelper(this, this);
        }
        return swipeBackHelper.processTouchEvent(ev) || super.dispatchTouchEvent(ev);
    }

    @NonNull
    @Override
    public Activity getSlideActivity() {
        return this;
    }

    @Override
    public boolean supportSlideBack() {
        return true;
    }

    @Override
    public boolean canBeSlideBack() {
        return true;
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_in);
    }

    @Override
    public void onSlideAnimStart() {

    }

    @Override
    public void onSlideCancelAnimEnd() {

    }

    @Override
    public void onSlideFinishAnimEnd() {

    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }

    /**
     * 隐藏输入法键盘
     */
    public void hideKeyboard() {
        InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void insertDisposable(Disposable disposable) {
        if (compositeDisposable != null) {
            compositeDisposable.add(disposable);
        }
    }

    public void deleteDisposable() {
        compositeDisposable.dispose();
    }

    /**
     * 设置 app 字体不跟随系统字体设置改变
     */
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration configuration = res.getConfiguration();
        if (configuration.fontScale != 1.0f) {
            configuration.fontScale = 1.0f;
            res.updateConfiguration(configuration, res.getDisplayMetrics());
        }
        return res;
    }
}
