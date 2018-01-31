package com.intelligent.reader.read.page;

import com.dycm_adsdk.PlatformSDK;
import com.dycm_adsdk.callback.AbstractCallback;
import com.dycm_adsdk.callback.ResultCode;

import net.lzbook.kit.data.bean.ReadConfig;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.List;

/**
 * Created by Xian on 2018/1/30.
 */

public class PageAdContainer extends FrameLayout {
    private boolean loadStatus = false;

    public PageAdContainer(@NonNull Context context, String type, LayoutParams layoutParams) {
        super(context);

        setBackgroundColor(Color.CYAN);
        setLayoutParams(layoutParams);

        PlatformSDK.adapp().dycmNativeAd(context, type,  ReadConfig.INSTANCE.getScreenHeight(),
                ReadConfig.INSTANCE.getScreenWidth(), new AbstractCallback() {
            @Override
            public void onResult(boolean adswitch, List<ViewGroup> views, String jsonResult) {
                if (!adswitch) {
                    return;
                }
                try {
                    JSONObject jsonObject = new JSONObject(jsonResult);
                    if (jsonObject.has("state_code")) {
                        if (ResultCode.AD_REQ_SUCCESS.equals(ResultCode.parser(jsonObject.getInt("state_code")))) {

                            loadStatus = true;
                            if(!views.isEmpty() && views.get(0).getParent() == null) {
                                addView(views.get(0));
                            }
                        } else {
                            loadStatus = true;
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public PageAdContainer(@NonNull Context context, String type, int width, int height) {
        super(context);

        setBackgroundColor(Color.GREEN);

        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        PlatformSDK.adapp().dycmNativeAd(context, type, height, width, new AbstractCallback() {
            @Override
            public void onResult(boolean adswitch, List<ViewGroup> views, String jsonResult) {
                if (!adswitch) {
                    return;
                }
                try {
                    JSONObject jsonObject = new JSONObject(jsonResult);
                    if (jsonObject.has("state_code")) {
                        if (ResultCode.AD_REQ_SUCCESS.equals(ResultCode.parser(jsonObject.getInt("state_code")))) {

                            loadStatus = true;
                            if(!views.isEmpty() && views.get(0).getParent() == null) {
                                addView(views.get(0));
                            }
                        } else {
                            loadStatus = true;
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
