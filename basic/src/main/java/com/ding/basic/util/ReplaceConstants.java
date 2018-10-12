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

    public final static String BUILD_CONFIG_PROVIDER = "/common_book/SwitchADActivity";

    private ReplaceConstants() {
//        APP_PATH = Config.INSTANCE.getSDCARD_PATH() + ResourceUtil.getStringById(R.string.app_path);
//        APP_PATH_BOOK = APP_PATH + ResourceUtil.getStringById(R.string.app_path_book);
//        APP_PATH_CACHE = APP_PATH + ResourceUtil.getStringById(R.string.app_path_cache);
//        APP_PATH_DOWNLOAD = APP_PATH + ResourceUtil.getStringById(R.string.app_path_download);
//        APP_PATH_IMAGE = APP_PATH + ResourceUtil.getStringById(R.string.app_path_image);
//        APP_PATH_LOG = APP_PATH + ResourceUtil.getStringById(R.string.app_path_log);
//        DEFAULT_IMAGE_URL = ResourceUtil.getStringById(R.string.default_image_url);
//
//        DATABASE_NAME = ResourceUtil.getStringById(R.string.database_name);
//
//        BAIDU_STAT_ID = ResourceUtil.getStringById(R.string.baidu_stat_id);
//        PUSH_KEY = ResourceUtil.getStringById(R.string.push_key);
//        ALIFEEDBACK_KEY = ResourceUtil.getStringById(R.string.alifeedback_key);
//        ALIFEEDBACK_SECRET = ResourceUtil.getStringById(R.string.alifeedback_secret);
//
//
//        BOOK_NOVEL_DEPLOY_HOST = ResourceUtil.getStringById(R.string.book_novel_deploy_host);
//        BOOK_WEBVIEW_HOST = ResourceUtil.getStringById(R.string.book_webview_host);
//
//        MICRO_API_HOST = ResourceUtil.getStringById(R.string.micro_api_host);
//        CONTENT_API_HOST = ResourceUtil.getStringById(R.string.content_api_host);

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
