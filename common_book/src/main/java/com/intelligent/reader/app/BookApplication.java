package com.intelligent.reader.app;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.sdk.android.feedback.impl.FeedbackAPI;
import com.alibaba.sdk.android.feedback.util.ErrorCode;
import com.alibaba.sdk.android.feedback.util.FeedbackErrorCallback;
import com.dingyue.contract.util.CommonUtil;
import com.dycm_adsdk.PlatformSDK;
import com.intelligent.reader.BuildConfig;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;

import java.util.concurrent.Callable;

import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import swipeback.ActivityLifecycleHelper;


public class BookApplication extends BaseBookApplication {

    private static RefWatcher sRefWatcher;

    public static RefWatcher getRefWatcher() {
        return sRefWatcher;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (AppUtils.isMainProcess(this)) {
            // 新版广告SDK
            PlatformSDK.app().onAppCreate(this);
        }

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }

        // 自定义ErrorCallback
        FeedbackAPI.addErrorCallback(new FeedbackErrorCallback() {
            @Override
            public void onError(Context context, String errorMessage, ErrorCode code) {
                CommonUtil.showToastMessage("ErrorMessage is: " + errorMessage, 0L);
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
        FeedbackAPI.init(this, ReplaceConstants.getReplaceConstants().ALIFEEDBACK_KEY, ReplaceConstants.getReplaceConstants().ALIFEEDBACK_SECRET);
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
     * RxJava2 当取消订阅后(dispose())，RxJava抛出的异常后续无法接收(此时后台线程仍在跑，可能会抛出IO等异常),全部由RxJavaPlugin接收，需要提前设置ErrorHandler
     * 详情：http://engineering.rallyhealth.com/mobile/rxjava/reactive/2017/03/15/migrating-to-rxjava-2.html#Error Handling
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
            PlatformSDK.app().onTerminate();
        }
    }
}
