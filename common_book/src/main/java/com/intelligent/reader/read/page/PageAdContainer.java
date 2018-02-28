package com.intelligent.reader.read.page;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import static net.lzbook.kit.utils.ExtensionsKt.msMainLooperHandler;

/**
 * Created by Xian on 2018/1/30.
 */

public class PageAdContainer extends RelativeLayout {

    private Runnable mRunnable = null;

//    private static class BigAdCallback extends AbstractCallback {
//        WeakReference<RelativeLayout> refLayout;
//
//        BigAdCallback(RelativeLayout layout) {
//            refLayout = new WeakReference<RelativeLayout>(layout);
//        }
//
//        @Override
//        public void onResult(boolean adswitch, List<ViewGroup> views, String jsonResult) {
//            if (!adswitch) {
//                return;
//            }
//            try {
//                JSONObject jsonObject = new JSONObject(jsonResult);
//                if (jsonObject.has("state_code")) {
//                    if (ResultCode.AD_REQ_SUCCESS.equals(ResultCode.parser(jsonObject.getInt("state_code")))) {
//
//                        if (!views.isEmpty() && views.get(0).getParent() == null) {
//                            LayoutParams adParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//                            adParams.addRule(RelativeLayout.CENTER_VERTICAL);
//                            RelativeLayout layout = refLayout.get();
//                            if (layout != null) {
//                                layout.addView(views.get(0), adParams);
//                            }
//                        }
//                    } else {
//                    }
//                }
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//    }

//    private static class SmallAdCallback extends AbstractCallback {
//        WeakReference<RelativeLayout> refLayout;
//
//        SmallAdCallback(RelativeLayout layout) {
//            refLayout = new WeakReference<RelativeLayout>(layout);
//        }
//
//        @Override
//        public void onResult(boolean adswitch, List<ViewGroup> views, String jsonResult) {
//            if (!adswitch) {
//                return;
//            }
//            try {
//                JSONObject jsonObject = new JSONObject(jsonResult);
//                if (jsonObject.has("state_code")) {
//                    if (ResultCode.AD_REQ_SUCCESS.equals(ResultCode.parser(jsonObject.getInt("state_code")))) {
//
//                        if (!views.isEmpty()) {
//                            for (ViewGroup viewGroup : views) {
//                                if (viewGroup != null && viewGroup.getParent() == null) {
//                                    LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//                                    RelativeLayout layout = refLayout.get();
//                                    if (layout != null) {
//                                        layout.addView(viewGroup, layoutParams);
//                                    }
//                                    break;
//                                }
//                            }
//                        }
//                    } else {
//                    }
//                }
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    public PageAdContainer(@NonNull final Context context, final String type, LayoutParams layoutParams, boolean force) {
        super(context);

        setLayoutParams(layoutParams);
        mRunnable = new Runnable() {
            @Override
            public void run() {
//                PlatformSDK.adapp().dycmNativeAd(context, type, PageAdContainer.this, new BigAdCallback(PageAdContainer.this));
            }
        };

        if(force){
            load();
        }
    }

    public PageAdContainer(@NonNull final Context context, final String type, final int width, final int height, boolean force) {
        super(context);

        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mRunnable = new Runnable() {
            @Override
            public void run() {
//                PlatformSDK.adapp().dycmNativeAd(context, type, height, width, new SmallAdCallback(PageAdContainer.this));
            }
        };
        if(force){
            load();
        }
    }

    public void load() {
        if (mRunnable != null) {
            msMainLooperHandler.post(mRunnable);
            mRunnable = null;
        }
    }
}
