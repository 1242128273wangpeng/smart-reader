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

import com.dy.media.MediaCode;
import com.dy.media.MediaControl;
import com.dy.media.MediaLifecycle;
import com.intelligent.reader.R;


import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class SwitchSplashAdActivity extends Activity {

    private boolean canBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        canBack = false;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_switch_splash_ad);
        final FrameLayout fm = (FrameLayout) findViewById(R.id.container);
        final LinearLayout linerClose = (LinearLayout) findViewById(R.id.ll_close_switch);
        setVisibility(fm, false);
        linerClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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