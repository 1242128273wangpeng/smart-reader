package com.intelligent.reader.app;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.alibaba.sdk.android.feedback.impl.FeedbackAPI;
import com.alibaba.sdk.android.feedback.util.ErrorCode;
import com.alibaba.sdk.android.feedback.util.FeedbackErrorCallback;
import com.dingyue.contract.util.CommonUtil;
import com.dy.media.MediaLifecycle;
import com.dy.reader.Reader;
import com.intelligent.reader.BuildConfig;
import com.intelligent.reader.upush.PushMessageHandler;
import com.intelligent.reader.upush.PushNotificationHandler;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.OpenUDID;

import org.android.agoo.huawei.HuaWeiRegister;
import org.android.agoo.xiaomi.MiPushRegistar;

import java.util.concurrent.Callable;

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

        // 友盟推送初始化
        try {
            String packageName = AppUtils.getPackageName();
            if("cc.remennovel".equals(packageName) ||"cc.kdqbxs.reader".equals(packageName) ){
                ApplicationInfo appInfo = getPackageManager()
                        .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
                String pushSecret = appInfo.metaData.getString("UMENG_PUSH_SECRET");
                if (pushSecret != null) {
                    UMConfigure.init(this, 1, pushSecret);
                    AppLog.e(TAG, "pushSecret: " + pushSecret);
                }

                String xiaomiId = appInfo.metaData.getString("UMENG_PUSH_XIAOMI_ID");
                AppLog.e(TAG, "xiaomiId: " + xiaomiId);

                String xiaomiKey = appInfo.metaData.getString("UMENG_PUSH_XIAOMI_KEY");
                AppLog.e(TAG, "xiaomiKey: " + xiaomiKey);

                // 小米通道
                if (xiaomiId != null && xiaomiKey != null) {
                    MiPushRegistar.register(this, xiaomiId.replace("String", ""),
                            xiaomiKey.replace("String", ""));
                }

                final PushAgent pushAgent = PushAgent.getInstance(this);
                pushAgent.setResourcePackageName("net.lzbook.kit");
                //注册推送服务，每次调用register方法都会回调该接口
                pushAgent.register(new IUmengRegisterCallback() {
                    @Override
                    public void onSuccess(String deviceToken) {
                        //注册成功会返回device token
                        AppLog.e(TAG, "deviceToken: " + deviceToken);
                        String udid = OpenUDID.getOpenUDIDInContext(BookApplication.this);
                        AppLog.e(TAG, "udid: " + udid);
                        pushAgent.setAlias(udid, "UDID", new UTrack.ICallBack() {
                            @Override
                            public void onMessage(boolean isSuccess, String message) {
                                AppLog.e(TAG, "setAlias：" + isSuccess + "  message: " + message);
                            }
                        });
                    }
                    @Override
                    public void onFailure(String s, String s1) {
                        AppLog.e(TAG, "s: " + s + " --- s1: " + s1);
                    }
                });
                pushAgent.setMessageHandler(new PushMessageHandler());
                pushAgent.setNotificationClickHandler(new PushNotificationHandler());

                // 华为通道
                HuaWeiRegister.register(this);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        if (!AppUtils.isMainProcess(this)) {
            return;
        }

        MediaLifecycle.INSTANCE.onAppCreate(this);

        // 自定义ErrorCallback
        FeedbackAPI.addErrorCallback(new FeedbackErrorCallback() {
            @Override
            public void onError(Context context, String errorMessage, ErrorCode code) {
                CommonUtil.showToastMessage("ErrorMessage is: " + errorMessage);
            }
        });
        // Feedback activity的回调
        FeedbackAPI.addLeaveCallback(new Callable() {
            @Override
            public Object call() throws Exception {
                Log.d("DemoApplication", "custom leave callback");
                return null;
            }
        });
        FeedbackAPI.init(this, ReplaceConstants.getReplaceConstants().ALIFEEDBACK_KEY,
                ReplaceConstants.getReplaceConstants().ALIFEEDBACK_SECRET);
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
