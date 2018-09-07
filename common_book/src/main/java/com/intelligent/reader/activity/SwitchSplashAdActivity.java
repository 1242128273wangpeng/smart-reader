package com.intelligent.reader.activity;

import static android.view.KeyEvent.KEYCODE_BACK;

import static net.lzbook.kit.utils.ExtensionsKt.msMainLooperHandler;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.dingyue.contract.router.RouterConfig;
import com.dy.media.MediaCode;
import com.dy.media.MediaControl;
import com.dy.media.MediaLifecycle;
import com.intelligent.reader.R;


import kotlin.Unit;
import kotlin.jvm.functions.Function1;

@Route(path = RouterConfig.SWITCH_AD_ACTIVITY)
public class SwitchSplashAdActivity extends Activity {

    private boolean canBack = false;
    FrameLayout fm = null;
    LinearLayout linerClose = null;

    private boolean loadedAD = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        canBack = false;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_switch_splash_ad);
        fm = (FrameLayout) findViewById(R.id.container);
        linerClose = (LinearLayout) findViewById(R.id.ll_close_switch);
        setVisibility(fm, false);
        linerClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });



        msMainLooperHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                canBack = true;
            }
        }, 8000);
    }

    long[] mHits = new long[2];

    public void click() {
        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
        mHits[mHits.length - 1] = SystemClock.uptimeMillis();
        if (mHits[0] > SystemClock.uptimeMillis() - 500) {
            finish();
        }
    }

    private void setVisibility(View view, boolean isShow) {
        if (isShow) {
            view.setBackgroundColor(Color.parseColor("#ffffffff"));
        } else {
            view.setBackgroundColor(Color.parseColor("#00ffffff"));
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        int width = getWindow().getDecorView().getWidth();
        int height = getWindow().getDecorView().getHeight();
        if (hasFocus && !loadedAD && height > width) {
            loadedAD = true;
            MediaControl.INSTANCE.loadSwitchScreenMedia(this, fm, new Function1<Integer, Unit>() {
                @Override
                public Unit invoke(Integer resultCode) {
                    switch (resultCode) {
                        case MediaCode.MEDIA_SUCCESS: //广告请求成功
                            setVisibility(fm, true);
                            linerClose.setVisibility(View.VISIBLE);
                            break;
                        case MediaCode.MEDIA_FAILED: //广告请求失败
                            SwitchSplashAdActivity.this.finish();
                            break;
                        case MediaCode.MEDIA_DISMISS: //开屏页面关闭
                            SwitchSplashAdActivity.this.finish();
                            break;
                    }
                    return null;
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MediaLifecycle.INSTANCE.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MediaLifecycle.INSTANCE.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (canBack && keyCode == KEYCODE_BACK) {
            click();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaLifecycle.INSTANCE.onDestroy();
    }
}