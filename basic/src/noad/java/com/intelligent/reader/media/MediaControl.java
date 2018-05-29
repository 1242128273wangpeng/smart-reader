package com.intelligent.reader.media;


import com.ding.basic.media.AbsLoadMediaCallback;
import com.ding.basic.media.IMediaControl;
import com.ding.basic.media.MediaConfig;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.TextView;

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

    @Override
    public void init(Application application) {

    }

    @Override
    public void onTerminate() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void setGridStyleAd() {

    }

    @Override
    public int getConfiMediaCount() {
        return 0;
    }

    @Override
    public boolean getAdSwitchByMediaId(String mediaId) {
        return false;
    }

    @Override
    public int getRestMediaMins() {
        return 0;
    }

    @Override
    public void setMediaConfig(MediaConfig config) {
    }

    @Override
    public int getChapterLimit() {
        return 0;
    }

    @Override
    public void loadMedia(Context context, String mediaId, ViewGroup loadView, AbsLoadMediaCallback callback) {
    }

    @Override
    public void loadMedia(Context context, String mediaId, AbsLoadMediaCallback callback) {
    }

    @Override
    public void loadMedia(Context context, String mediaId, int height, int width, AbsLoadMediaCallback callback) {
    }

    @Override
    public void loadSplashMedia(Context context, String mediaId, ViewGroup loadView, AbsLoadMediaCallback callback) {
    }

    @Override
    public void loadBookShelfMedia(Context context, String mediaId, int loadAdCount, AbsLoadMediaCallback callback) {
    }
}
