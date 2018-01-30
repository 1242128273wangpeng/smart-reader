package net.lzbook.kit.ad;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.dycm_adsdk.PlatformSDK;
import com.dycm_adsdk.callback.AbstractCallback;
import com.dycm_adsdk.callback.ResultCode;
import com.dycm_adsdk.utils.DyLogUtils;

import net.lzbook.kit.R;
import net.lzbook.kit.utils.ResourceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import static android.view.KeyEvent.KEYCODE_BACK;

public class SwitchSplashAdActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_switch_splash_ad);
        final FrameLayout fm = (FrameLayout) findViewById(R.id.container);
        try {
            if (android.os.Build.VERSION.SDK_INT > 10 && "night".equals(ResourceUtil.mode)) {
                fm.setAlpha(0.6f);
            }
        } catch (NoSuchMethodError e) {
            e.printStackTrace();
        }

        if (PlatformSDK.config().getAdSwitch("11-1")) {
            PlatformSDK.adapp().dycmSplashAd(this, "11-1", fm, new AbstractCallback() {
                @Override
                public void onResult(boolean adswitch,  String jsonResult) {
                    super.onResult(adswitch, jsonResult);
                    if (!adswitch) return;
                    try {
                        JSONObject jsonObject = new JSONObject(jsonResult);
                        if (jsonObject.has("state_code")) {
                            switch (ResultCode.parser(jsonObject.getInt("state_code"))) {
                                case AD_REQ_SUCCESS://广告请求成功
                                    DyLogUtils.dd("AD_REQ_SUCCESS" + jsonResult);
                                    break;
                                case AD_REQ_FAILED://广告请求失败
                                    DyLogUtils.dd("AD_REQ_FAILED" + jsonResult);
                                    SwitchSplashAdActivity.this.finish();
                                    break;
                                case AD_DISMISSED_CODE://开屏页面关闭
                                    SwitchSplashAdActivity.this.finish();
                                    break;
                                case AD_ONCLICKED_CODE://开屏页面点击
                                    DyLogUtils.dd("AD_ONCLICKED_CODE" + jsonResult);
                                    break;
                                case AD_ONTICK_CODE://剩余显示时间
                                    DyLogUtils.dd("AD_ONTICK_CODE" + jsonResult);
                                    break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }

}