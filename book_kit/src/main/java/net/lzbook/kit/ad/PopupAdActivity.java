package net.lzbook.kit.ad;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.dingyueads.sdk.Bean.Advertisement;
import com.dingyueads.sdk.Bean.EventPopupAd;
import com.dingyueads.sdk.Native.YQNativeAdInfo;
import com.dingyueads.sdk.NativeInit;
import com.dingyueads.sdk.Utils.LogUtils;

import net.lzbook.kit.R;
import net.lzbook.kit.cache.imagecache.ImageCacheManager;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.ImageUtils;
import net.lzbook.kit.utils.StatisticManager;

import java.util.Random;

import de.greenrobot.event.EventBus;

import static android.view.KeyEvent.KEYCODE_BACK;

public class PopupAdActivity extends Activity implements View.OnClickListener {
    private static String TAG = "SwitchSplashAdActivity";
    private OwnNativeAdManager ownNativeAdManager;
    private YQNativeAdInfo adInfo;
    private int[] layouts = {R.layout.activity_switch_ad1, R.layout.activity_switch_ad2, R.layout.activity_switch_ad3};
    private StatisticManager statisticManager;
    private Handler switchHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            finish();
        }
    };
    private ImageView iv_image;
    private static int MSG_FINISH = 0;
    private static final long CLOSE_TIME = 5000; //自动关闭毫秒数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_switch_ad_default);
        EventBus.getDefault().register(this);
        ownNativeAdManager = OwnNativeAdManager.getInstance(this);
        switchHandler.sendEmptyMessageDelayed(MSG_FINISH, CLOSE_TIME);
        LogUtils.e(TAG, "loadAd");
        ownNativeAdManager.loadAd(NativeInit.CustomPositionName.SLIDEUP_POPUPAD_POSITION);
    }

    public void onEvent(EventPopupAd popupAd) {
        if (NativeInit.CustomPositionName.SLIDEUP_POPUPAD_POSITION.toString().equals(popupAd.type_ad)) {
            showRandomLayout(popupAd.yqNativeAdInfo);
        }
    }

    private void showRandomLayout(YQNativeAdInfo yqNativeAdInfo) {
        LogUtils.e(TAG, "showRandomLayout");
        if (yqNativeAdInfo == null) {
            finish();
            return;
        }
        final Advertisement advertisement = yqNativeAdInfo.getAdvertisement();
        if (advertisement == null || TextUtils.isEmpty(advertisement.imageUrl)) {
            finish();
            return;
        }
        adInfo = yqNativeAdInfo;
        ImageCacheManager.getInstance().getImageLoader().get(advertisement.imageUrl, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                Bitmap bitmap = response.getBitmap();
                if (bitmap == null) {
                    return;
                }
                switchHandler.removeMessages(MSG_FINISH);
                bitmap = ImageUtils.getRoundedCornerBitmap(bitmap, AppUtils.dip2px(PopupAdActivity.this, 10));
                Random r = new Random();
                setContentView(layouts[r.nextInt(layouts.length)]);
                iv_image = (ImageView) findViewById(R.id.ad_image_switch_act);
                iv_image.setImageBitmap(bitmap);
                iv_image.setOnClickListener(PopupAdActivity.this);
                ImageView iv_logo = (ImageView) findViewById(R.id.ad_logo_switch_act);
                if ("广点通".equals(advertisement.rationName)) {
                    iv_logo.setImageResource(R.drawable.icon_ad_gdt_new);
                } else if ("百度".equals(advertisement.rationName)) {
                    iv_logo.setImageResource(R.drawable.icon_ad_bd_new);
                } else if ("360".equals(advertisement.rationName)) {
                    iv_logo.setImageResource(R.drawable.icon_ad_360_new);
                } else {
                    iv_logo.setImageResource(R.drawable.icon_ad_default_new);
                }
                ImageView iv_see = (ImageView) findViewById(R.id.iv_see_switch_act);
                iv_see.setOnClickListener(PopupAdActivity.this);
                ImageView iv_close = (ImageView) findViewById(R.id.iv_close_switch_act);
                iv_close.setOnClickListener(PopupAdActivity.this);
                TextView tv_cancel_ad = (TextView) findViewById(R.id.tv_cancel_ad);
                tv_cancel_ad.setOnClickListener(PopupAdActivity.this);
                if (statisticManager == null) {
                    statisticManager = StatisticManager.getStatisticManager();
                }
                statisticManager.schedulingRequest(PopupAdActivity.this, iv_image, adInfo, null, StatisticManager.TYPE_SHOW, NativeInit.ad_position[11]);
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                LogUtils.e(TAG, "onErrorResponse,err:" + error.toString());
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        LogUtils.e(TAG, "onDestroy");
        try {
            EventBus.getDefault().unregister(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        if (statisticManager == null) {
            statisticManager = StatisticManager.getStatisticManager();
        }
        if (v.getId() == R.id.iv_see_switch_act || v.getId() == R.id.ad_image_switch_act) {
            if (adInfo != null) {
                statisticManager.schedulingRequest(PopupAdActivity.this, v, adInfo, null, StatisticManager.TYPE_CLICK, NativeInit.ad_position[11]);
                finish();
//                statisticManager.schedulingRequest(PopupAdActivity.this, v, adInfo, null, StatisticManager.TYPE_END, NativeInit.ad_position[11]);
            }
        } else if (v.getId() == R.id.iv_close_switch_act || v.getId() == R.id.tv_cancel_ad) {
            finish();
//            statisticManager.schedulingRequest(PopupAdActivity.this, v, adInfo, null, StatisticManager.TYPE_END, NativeInit.ad_position[11]);
        }
    }
}
