package net.lzbook.kit.app;

import com.quduquxie.QuInitialization;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.component.service.DownloadService;
import net.lzbook.kit.cache.imagecache.ImageCacheManager;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.data.bean.ReadStatus;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.net.volley.VolleyRequestManager;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.DeviceHelper;
import net.lzbook.kit.utils.ExtensionsKt;
import net.lzbook.kit.utils.HttpUtils;
import net.lzbook.kit.utils.LogcatHelper;
import net.lzbook.kit.utils.UpdateJarUtil;
import net.xxx.yyy.go.spider.MainExtractorInterface;
import net.xxx.yyy.go.spider.URLBuilderIntterface;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;
import android.util.DisplayMetrics;
import android.util.Log;

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
            try {
                UpdateJarUtil.initJar(g_context);
                if (urlBuilderIntterface != null) {
                    AppLog.e("DEX2", "DEX init success !!!!!!!!!!");
                } else {
                    AppLog.e("DEX2", "DEX init failure !!!!!!!!!!");
                    UpdateJarUtil.resetJar(g_context);
                }
            } catch (Exception e) {
                UpdateJarUtil.resetJar(g_context);
                AppLog.e("DEX2", "DEX init failure !!!!!!!!!!");
                e.printStackTrace();
            }

        }

        return urlBuilderIntterface;
    }

    public void setUrlBuilderIntterface(URLBuilderIntterface urlBuilderIntterface) {
        this.urlBuilderIntterface = urlBuilderIntterface;
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
        return mainExtractorInterface;
    }

    public void setMainExtractorInterface(MainExtractorInterface mainExtractorInterface) {
        this.mainExtractorInterface = mainExtractorInterface;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.sCtx = this;
        loge(this, "onCreate");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        loge(this, "attachBaseContext");

        ExtensionsKt.msDebuggAble = (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        Log.e("BaseBookApplication", "ExtensionsKt.logable = " + ExtensionsKt.msDebuggAble);
        //分割dex防止方法数过多
        MultiDex.install(this);
        if (Constants.DEVELOPER_MODE) {
            // 监控
            // StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
            // StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
            // crash 处理
            // Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
        }

        HttpUtils.getHttpClient();
        initData();
    }

    private void initData() {
        g_context = this;
        dm = getResources().getDisplayMetrics();
        initCache();
        Constants.init(BaseBookApplication.this);

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        BookDaoHelper.getInstance(g_context);
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

    private void initCache() {
        VolleyRequestManager.init(this);
        int DISK_IMAGECACHE_SIZE = 1024 * 1024;
        ImageCacheManager.getInstance().init(this, ReplaceConstants.getReplaceConstants().APP_PATH_IMAGE, DISK_IMAGECACHE_SIZE, ImageCacheManager
                .ImageCacheType.COMPLEX);
    }
}