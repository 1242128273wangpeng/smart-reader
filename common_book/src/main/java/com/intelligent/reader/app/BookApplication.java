package com.intelligent.reader.app;

import com.ak.android.shell.AKAD;
import com.alibaba.sdk.android.feedback.impl.FeedbackAPI;
import com.alibaba.sdk.android.feedback.util.ErrorCode;
import com.alibaba.sdk.android.feedback.util.FeedbackErrorCallback;
import com.intelligent.reader.BuildConfig;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.constants.ReplaceConstants;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.Callable;


public class BookApplication extends BaseBookApplication {

    private static RefWatcher sRefWatcher;

    public static RefWatcher getRefWatcher() {
        return sRefWatcher;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }

        // 自定义ErrorCallback
        FeedbackAPI.addErrorCallback(new FeedbackErrorCallback() {
            @Override
            public void onError(Context context, String errorMessage, ErrorCode code) {
                Toast.makeText(context, "ErrMsg is: " + errorMessage, Toast.LENGTH_SHORT).show();
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
        FeedbackAPI.init(this, ReplaceConstants.getReplaceConstants().ALIFEEDBACK_KEY);
        if (BuildConfig.DEBUG) {
            if (!BuildConfig.IS_LEAKCANARY_DISABLE) {
                sRefWatcher = LeakCanary.install(this);
            } else {
                sRefWatcher = RefWatcher.DISABLED;
            }
        }
        // 初始化SDK,传入上下文
        // 打印DebugLog开关,上线前建议改成false
        // 请求测试广告开关,上线前务必改成false
        AKAD.initSdk(this, false, false);
    }
}
