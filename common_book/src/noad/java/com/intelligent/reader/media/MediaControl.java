package com.lee.example;


import android.app.Application;
import android.util.Log;

/**
 * @author lijun Lee
 * @desc 无广告内容提供器
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/2/24 14:56
 */

public class MediaControl implements IMediaControl {

    private MediaControl() {
    }

    private static class MediaControlHolder {
        private static final MediaControl INSTANCE = new MediaControl();
    }

    public static MediaControl getInstance() {

        return MediaControlHolder.INSTANCE;

    }

    public void initAdSDK(Application application) {
    }

    @Override
    public void loadAd() {
        Log.e("loadAd", "noAd : ");
    }
}
