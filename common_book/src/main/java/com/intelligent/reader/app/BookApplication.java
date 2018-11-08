package com.intelligent.reader.app;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.alibaba.sdk.android.feedback.impl.FeedbackAPI;
import com.baidu.mobstat.StatService;
import com.dingyue.contract.util.CommonUtil;
import com.dy.media.MediaConfig;
import com.dy.media.MediaLifecycle;
import com.dy.reader.Reader;
import com.intelligent.reader.BuildConfig;
import com.intelligent.reader.upush.PushMessageHandler;
import com.intelligent.reader.upush.PushNotificationHandler;
import com.intelligent.reader.upush.PushRegisterCallback;
import com.reyun.tracking.sdk.Tracking;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.PushAgent;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.OpenUDID;

import org.android.agoo.huawei.HuaWeiRegister;
import org.android.agoo.xiaomi.MiPushRegistar;

import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import swipeback.ActivityLifecycleHelper;


public class BookApplication extends BaseBookApplication {

    private static RefWatcher sRefWatcher;

    public static RefWatcher getRefWatcher() {
        return sRefWatcher;
    }

    private static final String TAG = "BookApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        Reader.INSTANCE.init(this);

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }

        if (AppUtils.isMainProcess(this)) {

            MediaConfig.INSTANCE.setAd_userid(
                    OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext()));
            MediaConfig.INSTANCE.setChannel_code(AppUtils.getChannelId());
            MediaLifecycle.INSTANCE.onAppCreate(this);

//            //防止定位不回掉导致缺失id
//            MediaConfig.INSTANCE.setAd_userid(OpenUDID.getOpenUDIDInContext(BaseBookApplication
// .getGlobalContext()));
//            MediaConfig.INSTANCE.setChannel_code(AppUtils.getChannelId());

            StatService.setAppKey(ReplaceConstants.getReplaceConstants().BAIDU_STAT_ID);
            StatService.setAppChannel(this, AppUtils.getChannelId(), true);

            if (BuildConfig.DEBUG) {
                if (!BuildConfig.IS_LEAKCANARY_DISABLE) {
                    sRefWatcher = LeakCanary.install(this);
                } else {
                    sRefWatcher = RefWatcher.DISABLED;
                }
            }
            registerActivityLifecycleCallbacks(ActivityLifecycleHelper.build());
            setRxJavaErrorHandler();
        }
        registerPushAgent();
        initHandler.sendEmptyMessageDelayed(1, 1500);
    }

    private Handler initHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (AppUtils.isMainProcess(BookApplication.this)) {
                    // 自定义ErrorCallback
                    FeedbackAPI.addErrorCallback(
                            (context, errorMessage, code) -> CommonUtil.showToastMessage(
                                    "ErrorMessage is: " + errorMessage));
                    // Feedback activity的回调
                    FeedbackAPI.addLeaveCallback(() -> {
                        Log.d("DemoApplication", "custom leave callback");
                        return null;
                    });

                    FeedbackAPI.init(BookApplication.this,
                            ReplaceConstants.getReplaceConstants().ALIFEEDBACK_KEY,
                            ReplaceConstants.getReplaceConstants().ALIFEEDBACK_SECRET);
                }

                try {
                    ApplicationInfo appInfo = getPackageManager()
                            .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);


                    String reyunAppKey = appInfo.metaData.getString("REYUN_APPKEY");
                    AppLog.e("reyun", reyunAppKey);
                    Tracking.initWithKeyAndChannelId(BaseBookApplication.getGlobalContext(),
                            reyunAppKey, AppUtils.getChannelId());


                    // 友盟推送初始化
                    if (!AppUtils.hasUPush()) return;

                    String xiaomiId = appInfo.metaData.getString("UMENG_PUSH_XIAOMI_ID");
                    AppLog.e(TAG, "xiaomiId: " + xiaomiId);

                    String xiaomiKey = appInfo.metaData.getString("UMENG_PUSH_XIAOMI_KEY");
                    AppLog.e(TAG, "xiaomiKey: " + xiaomiKey);

                    // 小米通道
                    if (xiaomiId != null && xiaomiKey != null) {
                        MiPushRegistar.register(BookApplication.this,
                                xiaomiId.replace("String", ""),
                                xiaomiKey.replace("String", ""));
                    }

                    // 华为通道
                    HuaWeiRegister.register(BookApplication.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };


    private void registerPushAgent() {
        if (!AppUtils.hasUPush()) return;

        try {
            ApplicationInfo appInfo = getPackageManager()
                    .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            AppLog.e(TAG, "register umeng push agent");
            String umengAppkey = appInfo.metaData.getString("UMENG_APPKEY");
            String pushSecret = appInfo.metaData.getString("UMENG_PUSH_SECRET");
            if (pushSecret != null) {
                UMConfigure.init(BookApplication.this, umengAppkey, AppUtils.getChannelId(),
                        1, pushSecret);
                AppLog.e(TAG, "pushSecret: " + pushSecret);
            }
            final PushAgent pushAgent = PushAgent.getInstance(BookApplication.this);
            pushAgent.setResourcePackageName("net.lzbook.kit");
            //注册推送服务
            pushAgent.register(new PushRegisterCallback(BookApplication.this));
            //消息送达处理
            pushAgent.setMessageHandler(new PushMessageHandler());
            //消息点击处理
            pushAgent.setNotificationClickHandler(new PushNotificationHandler());
            //最多显示3条通知
            pushAgent.setDisplayNotificationNumber(3);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * RxJava2 当取消订阅后(dispose())，RxJava抛出的异常后续无法接收(此时后台线程仍在跑，可能会抛出IO等异常),
     * 全部由RxJavaPlugin接收，需要提前设置ErrorHandler
     * 详情：http://engineering.rallyhealth
     * .com/mobile/rxjava/reactive/2017/03/15/migrating-to-rxjava-2.html#Error
     * Handling
     */
    private void setRxJavaErrorHandler() {
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                AppLog.e("DataProvider", " throwable :" + throwable.getMessage());
            }
        });
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (AppUtils.isMainProcess(this)) {
            MediaLifecycle.INSTANCE.onTerminate();
        }
    }
}
