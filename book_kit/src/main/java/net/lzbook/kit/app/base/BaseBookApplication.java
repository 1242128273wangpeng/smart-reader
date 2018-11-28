package net.lzbook.kit.app.base;

import static net.lzbook.kit.utils.ExtensionsKt.loge;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;
import android.util.DisplayMetrics;

import com.alibaba.android.arouter.launcher.ARouter;
import com.ding.basic.bean.LoginResp;
import com.ding.basic.bean.LoginRespV4;
import com.ding.basic.bean.ToastEvent;
import com.ding.basic.net.Config;
import com.dingyue.statistics.DyStatService;

import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.pointpage.EventPoint;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.ExtensionsKt;
import net.lzbook.kit.utils.OpenUDID;
import net.lzbook.kit.utils.download.CacheManager;
import net.lzbook.kit.utils.encrypt.URLBuilderIntterface;
import net.lzbook.kit.utils.encrypt.v17.URLBuilder;
import net.lzbook.kit.utils.logger.LogcatHelper;
import net.lzbook.kit.utils.toast.ToastUtil;
import net.lzbook.kit.utils.user.UserManager;
import net.lzbook.kit.utils.user.UserManagerV4;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

public abstract class BaseBookApplication extends Application {
    private static BaseBookApplication g_context;
    private static DisplayMetrics dm;
    private static URLBuilderIntterface urlBuilderIntterface;
    protected SharedPreferences sp;

    public static BaseBookApplication getGlobalContext() {
        return g_context;
    }

    public static URLBuilderIntterface getUrlBuilderIntterface() {

        if (urlBuilderIntterface == null) {
            urlBuilderIntterface = new URLBuilder();
        }

        return urlBuilderIntterface;
    }

    public static DisplayMetrics getDisplayMetrics() {
        return dm;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        loge(this, "onCreate");

        if (AppUtils.isMainProcess(this)) {

            CacheManager.INSTANCE.checkService();

            // 设置日志
            DyStatService.setDebugOn(Constants.SHOW_LOG);
            // 统计SDK初始化
            DyStatService.init(this, OpenUDID.getOpenUDIDInContext(this), AppUtils.getChannelId());
            DyStatService.onEvent(EventPoint.SYSTEM_APPINIT);

            Config.INSTANCE.beginInit(this);
        }

        EventBus.getDefault().register(this);


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void obtainToast(ToastEvent event) {
        ToastUtil.INSTANCE.showToastMessage(event.getToast());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        AppUtils.setCustomDensity(null, this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        loge(this, "attachBaseContext");

        Constants.SHOW_LOG = ExtensionsKt.msDebuggAble =
                (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        //分割dex防止方法数过多
        MultiDex.install(this);
        g_context = this;
        initARouter();
        initData();
    }

    private void initData() {
        dm = getResources().getDisplayMetrics();

        Constants.init(BaseBookApplication.this);

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        if (Constants.DEVELOPER_MODE) {
            LogcatHelper.getInstance(this).start();
        } else {
            LogcatHelper.getInstance(this).stop();
        }

        initializeRequestParameters();

    }

    private void initARouter() {
        if (Constants.SHOW_LOG) {
            ARouter.openLog();     // 打印日志
            ARouter.openDebug();   // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
        }
        ARouter.init(g_context);
    }

    private void initializeRequestParameters() {

        Config.INSTANCE.initializeLogger();

        HashMap<String, String> parameters = new HashMap<>();

        String packageName = AppUtils.getPackageName();
        parameters.put("packageName", packageName);

        String version = String.valueOf(AppUtils.getVersionCode());
        parameters.put("version", version);

        String channelId = AppUtils.getChannelId();
        parameters.put("channelId", channelId);

        String os = Constants.APP_SYSTEM_PLATFORM;
        parameters.put("os", os);

        String udid = OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext());
        parameters.put("udid", udid);

        parameters.put("longitude", "0.0");

        parameters.put("latitude", "0.0");

        parameters.put("cityCode", "");

        String loginToken = null;

        if ("cc.quanben.novel".equals(packageName)) {
            LoginRespV4 userInfo = UserManagerV4.INSTANCE.getUser();

            if (null != userInfo) {
                loginToken = userInfo.getToken();
            }
        } else {
            LoginResp userInfo = UserManager.INSTANCE.getMUserInfo();

            if (null != userInfo) {
                loginToken = userInfo.getLogin_token();
            }
        }

        if (loginToken == null) {
            parameters.put("loginToken", "");
        } else {
            parameters.put("loginToken", loginToken);
        }

        Config.INSTANCE.insertRequestParameters(parameters);
    }
}