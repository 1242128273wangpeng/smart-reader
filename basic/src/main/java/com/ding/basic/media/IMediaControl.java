package com.ding.basic.media;

import android.app.Application;
import android.content.Context;
import android.view.ViewGroup;

/**
 * @author lijun Lee
 * @desc IMediaControl
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/2/24 14:56
 */

public interface IMediaControl {

    /**
     * 初始化广告SDK
     *
     * @param application application
     */
    void init(Application application);

    /**
     * 释放广告SDK资源
     */
    void onTerminate();

    void onResume();

    void onPause();

    void onDestroy();

    /**
     * 9宫格形式广告
     */
    void setGridStyleAd();

    /**
     * 返回广告数量
     *
     * @return 广告数量
     */
    int getConfiMediaCount();

    /**
     * 获取广告开关
     *
     * @param mediaId mediaId
     * @return 是否开启
     */
    boolean getAdSwitchByMediaId(String mediaId);

    /**
     * 获取休息广告间隔时间
     *
     * @return 分钟
     */
    int getRestMediaMins();

    /**
     * 获取阅读页广告页数展示间隔
     *
     * @return int
     */
    int getChapterLimit();

    /**
     * 配置
     */
    void setMediaConfig(MediaConfig config);

    /**
     * 拉取广告
     *
     * @param context  context
     * @param mediaId  广告位Id
     * @param loadView 承载广告的视图
     * @param callback callback
     */
    void loadMedia(Context context, String mediaId, ViewGroup loadView, AbsLoadMediaCallback callback);

    /**
     * 拉取广告
     *
     * @param context  context
     * @param mediaId  广告位Id
     * @param callback callback
     */
    void loadMedia(Context context, String mediaId, AbsLoadMediaCallback callback);

    /**
     * 拉取广告
     *
     * @param context  context
     * @param mediaId  广告位Id
     * @param height   height
     * @param width    width
     * @param callback callback
     */
    void loadMedia(Context context, String mediaId, int height, int width, AbsLoadMediaCallback callback);

    /**
     * 拉取开屏广告
     *
     * @param context  context
     * @param mediaId  广告位Id
     * @param loadView 承载广告的视图
     * @param callback callback
     */
    void loadSplashMedia(Context context, String mediaId, ViewGroup loadView, AbsLoadMediaCallback callback);

    /**
     * 拉取书架广告
     *
     * @param context     context
     * @param mediaId     广告位Id
     * @param loadAdCount 拉取广告视图数量
     * @param callback    callback
     */
    void loadBookShelfMedia(Context context, String mediaId, int loadAdCount, AbsLoadMediaCallback callback);

    class MediaMarkPosition {
        private static String DEFAULT_POSITION = "NO_AD";
        public static String SPLASH_POSITION = DEFAULT_POSITION;
        public static String SHELF_POSITION = DEFAULT_POSITION;
        public static String SHELF_BOUNDARY = DEFAULT_POSITION;
        public static String SWITCH_SPLASH_POSITION = DEFAULT_POSITION;
        public static String BOOK_END_POSITION = DEFAULT_POSITION;
        public static String REST_POSITION = DEFAULT_POSITION;
        public static String LANDSCAPE_SLIDEUP_POPUPAD = DEFAULT_POSITION;
        public static String SLIDEUP_POPUPAD_POSITION = DEFAULT_POSITION;
        public static String READING_IN_CHAPTER_POSITION = DEFAULT_POSITION;
        public static String SUPPLY_READING_IN_CHAPTER = DEFAULT_POSITION;
        public static String READING_POSITION = DEFAULT_POSITION;
        public static String SUPPLY_READING_SPACE = DEFAULT_POSITION;
        public static String READING_MIDDLE_POSITION = DEFAULT_POSITION;
    }
}
