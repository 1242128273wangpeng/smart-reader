package com.ding.basic.util;

import com.alibaba.android.arouter.launcher.ARouter;
import com.ding.basic.net.Config;


public class ReplaceConstants {

    public static ReplaceConstants replaceConstants = null;
    public String APP_PATH;
    public String APP_PATH_BOOK;
    public String APP_PATH_CACHE;
    public String APP_PATH_DOWNLOAD;
    public String APP_PATH_IMAGE;
    public String APP_PATH_LOG;
    public String DEFAULT_IMAGE_URL;
    public String DATABASE_NAME;
    public String BAIDU_STAT_ID;
    public String PUSH_KEY;
    public String ALIFEEDBACK_KEY;
    public String ALIFEEDBACK_SECRET;


    public String BOOK_NOVEL_DEPLOY_HOST;
    public String BOOK_WEBVIEW_HOST;

    public String MICRO_API_HOST;
    public String CONTENT_API_HOST;
    public String CDN_HOST;

    public final static String BUILD_CONFIG_PROVIDER = "/common_book/BuildConfigProvider";

    private ReplaceConstants() {

        IBuildConfigProvider buildConfigProvider =
                (IBuildConfigProvider) ARouter.getInstance().build(
                        BUILD_CONFIG_PROVIDER).navigation();

        APP_PATH = Config.INSTANCE.getSDCARD_PATH() + buildConfigProvider.getAppPath();
        APP_PATH_BOOK = APP_PATH + "/book/";
        APP_PATH_CACHE = APP_PATH + "/cache/";
        APP_PATH_DOWNLOAD = APP_PATH + "/download/";
        APP_PATH_IMAGE = APP_PATH + "/image/";
        APP_PATH_LOG = APP_PATH + "/log/";

        DEFAULT_IMAGE_URL = "http://image.book.easou.com/i/default/cover.jpg";

        DATABASE_NAME = buildConfigProvider.getDatabaseName();

        PUSH_KEY = buildConfigProvider.getPushKey();
        BAIDU_STAT_ID = buildConfigProvider.getBaiduStatId();
        ALIFEEDBACK_KEY = buildConfigProvider.getAlifeedbackKey();
        ALIFEEDBACK_SECRET = buildConfigProvider.getAlifeedbackSecret();

        BOOK_WEBVIEW_HOST = buildConfigProvider.getBookWebviewHost();
        BOOK_NOVEL_DEPLOY_HOST = buildConfigProvider.getBookNovelDeployHost();
        CDN_HOST = buildConfigProvider.getCDNHost();

        MICRO_API_HOST = buildConfigProvider.getMicroApiHost();
        CONTENT_API_HOST = buildConfigProvider.getContentApiHost();
    }

    public static ReplaceConstants getReplaceConstants() {
        if (replaceConstants == null) {
            replaceConstants = new ReplaceConstants();
        }
        return replaceConstants;
    }
}