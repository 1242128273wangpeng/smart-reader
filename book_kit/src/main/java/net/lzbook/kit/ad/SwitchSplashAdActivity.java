package net.lzbook.kit.ad;

import com.dingyueads.sdk.NativeInit;

import net.lzbook.kit.R;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.utils.ResourceUtil;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import static android.view.KeyEvent.KEYCODE_BACK;

public class SwitchSplashAdActivity extends Activity {
    private static final int SHOW_CLOSE_BT = 1;
    private static String TAG = "SwitchSplashAdActivity";
    SwitchHandler mHandler;
    private ImageView mCloseBt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_switch_splash_ad);
        FrameLayout fm = (FrameLayout) findViewById(R.id.container);
        try {
            if (android.os.Build.VERSION.SDK_INT > 10 && "night".equals(ResourceUtil.mode)) {
                fm.setAlpha(0.6f);
            }
        } catch (NoSuchMethodError e) {
            e.printStackTrace();
        }
        mCloseBt = (ImageView) findViewById(R.id.close_iv);
        if (mHandler == null) {
            mHandler = new SwitchHandler(this);
        }
        mHandler.sendEmptyMessageDelayed(SHOW_CLOSE_BT, Constants.show_switchSplash_ad_close * 1000);

        mCloseBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        OwnNativeAdManager.InitSplashAd(this, fm, mHandler, 0, NativeInit.CustomPositionName.SWITCH_SPLASH_POSITION);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }


    class SwitchHandler extends Handler {

        private WeakReference<SwitchSplashAdActivity> weakReference;

        SwitchHandler(SwitchSplashAdActivity splashActivity) {
            weakReference = new WeakReference<>(splashActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            SwitchSplashAdActivity activity = weakReference.get();
            if (activity == null) {
                return;
            }
            switch (msg.what) {
                case 0:
                    activity.finish();
                    break;
                case SHOW_CLOSE_BT:
                    if (Constants.IS_LANDSCAPE) {
                        mCloseBt.setVisibility(View.GONE);
                    } else {
                        mCloseBt.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        }
    }

}
