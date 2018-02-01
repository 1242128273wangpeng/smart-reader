package com.intelligent.reader.read.page;

import com.dycm_adsdk.PlatformSDK;
import com.dycm_adsdk.callback.AbstractCallback;
import com.dycm_adsdk.callback.ResultCode;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.List;

/**
 * Created by Xian on 2018/1/30.
 */

public class PageAdContainer extends RelativeLayout {
    private boolean loadStatus = false;

    public PageAdContainer(@NonNull Context context, String type, LayoutParams layoutParams) {
        super(context);

        setLayoutParams(layoutParams);

        PlatformSDK.adapp().dycmNativeAd(context, type,  this, new AbstractCallback() {
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
                                LayoutParams adParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                                adParams.addRule(RelativeLayout.CENTER_VERTICAL);
                                addView(views.get(0), adParams);
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
                            if(!views.isEmpty()) {
                                for (ViewGroup viewGroup : views) {
                                    if (viewGroup != null && viewGroup.getParent() == null) {
                                        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                                        addView(viewGroup, layoutParams);
                                        break;
                                    }
                                }
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
