package net.lzbook.kit.app;

import com.dycm_adsdk.PlatformSDK;
import com.quduquxie.QuInitialization;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.component.service.DownloadService;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.ReadStatus;
import net.lzbook.kit.encrypt.MainExtractorInterface;
import net.lzbook.kit.encrypt.URLBuilderIntterface;
import net.lzbook.kit.encrypt.v17.MainExtractor;
import net.lzbook.kit.encrypt.v17.URLBuilder;
import net.lzbook.kit.error.StatisticUncaughtExceptionHandler;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.DeviceHelper;
import net.lzbook.kit.utils.ExtensionsKt;
import net.lzbook.kit.utils.HttpUtils;
import net.lzbook.kit.utils.LogcatHelper;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;
import android.util.DisplayMetrics;

import java.lang.ref.WeakReference;

import static net.lzbook.kit.utils.ExtensionsKt.loge;


public abstract class BaseBookApplication extends Application {
    public static Context sCtx;
    private static DownloadService downloadService;
    private static BaseBookApplication g_context;
    private static DisplayMetrics dm;
    private static URLBuilderIntterface urlBuilderIntterface;
    protected SharedPreferences sp;
    private WeakReference<ReadStatus> readStatusWeakReference;
    private MainExtractorInterface mainExtractorInterface;

    public static BaseBookApplication getGlobalContext() {
        return g_context;
    }

    public static URLBuilderIntterface getUrlBuilderIntterface() {

        if (urlBuilderIntterface == null) {
            urlBuilderIntterface = new URLBuilder();
        }

        return urlBuilderIntterface;
    }

    public static DownloadService getDownloadService() {

        return downloadService;
    }

    public static void setDownloadService(DownloadService downloadService) {
        BaseBookApplication.downloadService = downloadService;
    }

    public static DisplayMetrics getDisplayMetrics() {
        return dm;
    }

    public ReadStatus getReadStatus() {
        return readStatusWeakReference.get();
    }

    public void setReadStatus(ReadStatus readStatus) {
        readStatusWeakReference = new WeakReference<>(readStatus);
    }

    public MainExtractorInterface getMainExtractorInterface() {
        if (mainExtractorInterface == null) {
            mainExtractorInterface = new MainExtractor();
        }
        return mainExtractorInterface;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.sCtx = this;
        loge(this, "onCreate");
        PlatformSDK.app().onAppCreate(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        this.sCtx = this;
        super.attachBaseContext(base);
        loge(this, "attachBaseContext");

        final Thread.UncaughtExceptionHandler parent = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(new StatisticUncaughtExceptionHandler(parent));



        Constants.SHOW_LOG = ExtensionsKt.msDebuggAble =(getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)!= 0;
        //分割dex防止方法数过多
        MultiDex.install(this);
        HttpUtils.getHttpClient();
        initData();
    }

    private void initData() {
        g_context = this;
        dm = getResources().getDisplayMetrics();

        Constants.init(BaseBookApplication.this);

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        if (Constants.DEVELOPER_MODE) {
            LogcatHelper.getInstance(this).start();
        } else {
            LogcatHelper.getInstance(this).stop();
        }

        DeviceHelper.setContext(this);
        AppUtils.setContext(this);

        QuInitialization.init(this);

        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.APPINIT);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        PlatformSDK.app().onTerminate();
    }
}