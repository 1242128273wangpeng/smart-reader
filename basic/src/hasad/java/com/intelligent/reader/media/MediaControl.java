package com.intelligent.reader.media;


import com.ding.basic.media.AbsLoadMediaCallback;
import com.ding.basic.media.IMediaControl;
import com.ding.basic.media.MediaConfig;
import com.dycm_adsdk.PlatformSDK;
import com.dycm_adsdk.callback.AbstractCallback;
import com.dycm_adsdk.callback.ResultCode;
import com.dycm_adsdk.constant.AdMarkPostion;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.Context;
import android.view.ViewGroup;

import java.util.List;

/**
 * @author lijun Lee
 * @desc 有广告内容提供器
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/2/24 14:56
 */

public class MediaControl implements IMediaControl {

    private MediaControl() {
        MediaMarkPosition.SPLASH_POSITION = AdMarkPostion.SPLASH_POSITION;
        MediaMarkPosition.SHELF_POSITION = AdMarkPostion.SHELF_POSITION;
        MediaMarkPosition.SHELF_BOUNDARY = AdMarkPostion.SHELF_BOUNDARY;
        MediaMarkPosition.SWITCH_SPLASH_POSITION = AdMarkPostion.SWITCH_SPLASH_POSITION;
        MediaMarkPosition.BOOK_END_POSITION = AdMarkPostion.BOOK_END_POSITION;
        MediaMarkPosition.REST_POSITION = AdMarkPostion.REST_POSITION;
        MediaMarkPosition.LANDSCAPE_SLIDEUP_POPUPAD = AdMarkPostion.LANDSCAPE_SLIDEUP_POPUPAD;
        MediaMarkPosition.SLIDEUP_POPUPAD_POSITION = AdMarkPostion.SLIDEUP_POPUPAD_POSITION;
        MediaMarkPosition.READING_IN_CHAPTER_POSITION = AdMarkPostion.READING_IN_CHAPTER_POSITION;
        MediaMarkPosition.SUPPLY_READING_IN_CHAPTER = AdMarkPostion.SUPPLY_READING_IN_CHAPTER;
        MediaMarkPosition.READING_POSITION = AdMarkPostion.READING_POSITION;
        MediaMarkPosition.SUPPLY_READING_SPACE = AdMarkPostion.SUPPLY_READING_SPACE;
        MediaMarkPosition.READING_MIDDLE_POSITION = AdMarkPostion.READING_MIDDLE_POSITION;
    }

    private static class MediaControlHolder {
        private static final MediaControl INSTANCE = new MediaControl();
    }

    public static MediaControl getInstance() {
        return MediaControlHolder.INSTANCE;
    }

    @Override
    public void init(Application application) {
        PlatformSDK.app().onAppCreate(application);
    }

    @Override
    public void onTerminate() {
        PlatformSDK.app().onTerminate();
    }

    @Override
    public void onResume() {
        PlatformSDK.lifecycle().onResume();
    }

    @Override
    public void onPause() {
        PlatformSDK.lifecycle().onPause();
    }

    @Override
    public void onDestroy() {
        PlatformSDK.lifecycle().onDestroy();
    }

    @Override
    public void setGridStyleAd() {
        PlatformSDK.config().setBookShelfGrid(true);
    }

    @Override
    public int getConfiMediaCount() {
        return PlatformSDK.config().getAdCount();
    }

    @Override
    public boolean getAdSwitchByMediaId(String mediaId) {
        return PlatformSDK.config().getAdSwitch(mediaId);
    }

    @Override
    public int getRestMediaMins() {
        return PlatformSDK.config().getRestAd_sec();
    }

    @Override
    public int getChapterLimit() {
        return PlatformSDK.config().getChapter_limit();
    }

    @Override
    public void setMediaConfig(MediaConfig config) {
        if (config != null) {
            PlatformSDK.config().setAd_userid(config.getUserId());
            PlatformSDK.config().setChannel_code(config.getChannelCode());
            PlatformSDK.config().setCityCode(config.getCityCode());
            PlatformSDK.config().setCityName(config.getCityName());
            PlatformSDK.config().setLatitude(config.getLatitude());
            PlatformSDK.config().setLongitude(config.getLongitude());
        }
    }

    @Override
    public void loadMedia(Context context, String mediaId, ViewGroup loadView, final AbsLoadMediaCallback callback) {
        PlatformSDK.adapp().dycmNativeAd(context, mediaId, loadView, new MediaCallback(callback));
    }

    @Override
    public void loadMedia(Context context, String mediaId, AbsLoadMediaCallback callback) {
        PlatformSDK.adapp().dycmNativeAd(context, mediaId, null, new MediaCallback(callback));
    }

    @Override
    public void loadMedia(Context context, String mediaId, int height, int width, AbsLoadMediaCallback callback) {
        PlatformSDK.adapp().dycmNativeAd(context, mediaId, height, width, new MediaCallback(callback));
    }

    @Override
    public void loadSplashMedia(Context context, String mediaId, ViewGroup loadView, AbsLoadMediaCallback callback) {
        PlatformSDK.adapp().dycmSplashAd(context, mediaId, loadView, new MediaCallback(callback));
    }

    @Override
    public void loadBookShelfMedia(Context context, String mediaId, int loadAdCount, AbsLoadMediaCallback callback) {
        PlatformSDK.adapp().dycmNativeAd(context, mediaId, null, new MediaCallback(callback), loadAdCount);
    }

    /**
     * My MediaCallback
     */
    private class MediaCallback extends AbstractCallback {

        private AbsLoadMediaCallback mAbsLoadMediaCallback;

        MediaCallback(AbsLoadMediaCallback absLoadMediaCallback) {
            this.mAbsLoadMediaCallback = absLoadMediaCallback;
        }

        @Override
        public void onResult(boolean adSwitch, List<ViewGroup> views, String jsonResult) {
            super.onResult(adSwitch, views, jsonResult);
            parseResultData(adSwitch, views, jsonResult, mAbsLoadMediaCallback);
        }

        @Override
        public void onResult(boolean adSwitch, String jsonResult) {
            super.onResult(adSwitch, jsonResult);
            parseResultData(adSwitch, null, jsonResult, mAbsLoadMediaCallback);
        }

        private void parseResultData(boolean adSwitch, List<ViewGroup> views, String jsonResult, AbsLoadMediaCallback absLoadMediaCallback) {
            try {
                JSONObject jsonObject = new JSONObject(jsonResult);
                if (jsonObject.has("state_code")) {
                    switch (ResultCode.parser(jsonObject.getInt("state_code"))) {
                        case AD_REQ_SUCCESS:
                            if (views != null) {
                                absLoadMediaCallback.onResult(adSwitch, views.get(0));
                                absLoadMediaCallback.onResult(adSwitch, views);
                            } else {
                                absLoadMediaCallback.onResult(adSwitch);
                            }
                            break;
                        case AD_REPAIR_SUCCESS:
                            absLoadMediaCallback.onRepairResult(adSwitch, views);
                            break;
                        case AD_REQ_FAILED:
                            absLoadMediaCallback.onFailed();
                            break;
                        case AD_DISMISSED_CODE:
                            absLoadMediaCallback.onMediaDismissed();
                            break;
                        default:
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                absLoadMediaCallback.onFailed();
            }
        }
    }
}
