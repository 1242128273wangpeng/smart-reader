package net.lzbook.kit.constants;

import com.alibaba.android.arouter.launcher.ARouter;
import com.ding.basic.net.Config;
import com.ding.basic.util.IBuildConfigProvider;

public class ReplaceConstants {

    public static ReplaceConstants replaceConstants = null;
    public String APP_PATH;
    public String APP_PATH_BOOK;
    public String APP_PATH_CACHE;
    public String APP_PATH_DOWNLOAD;
    public String APP_PATH_IMAGE;
    public String APP_PATH_LOG;
    public String DEFAULT_IMAGE_URL;
    public String BOOK_NOVEL_DEPLOY_HOST;
    public String BOOK_WEBVIEW_HOST;
    public String DATABASE_NAME;
    public String BAIDU_STAT_ID;
    public String PUSH_KEY;
    public String ALIFEEDBACK_KEY;
    public String ALIFEEDBACK_SECRET;

    public final static String BUILD_CONFIG_PROVIDER = "/common_book/BuildConfigProvider";

    private ReplaceConstants() {
        IBuildConfigProvider buidCofig=(IBuildConfigProvider) ARouter.getInstance().build(BUILD_CONFIG_PROVIDER).navigation();
        APP_PATH = Config.INSTANCE.getSDCARD_PATH() + buidCofig.getAppPath();
        APP_PATH_BOOK = APP_PATH +"/book/";
        APP_PATH_CACHE = APP_PATH + "/cache/";
        APP_PATH_DOWNLOAD = APP_PATH + "/download/";
        APP_PATH_IMAGE = APP_PATH + "/image/";
        APP_PATH_LOG = APP_PATH + "/log/";
        DEFAULT_IMAGE_URL = "http://image.book.easou.com/i/default/cover.jpg";

        BOOK_NOVEL_DEPLOY_HOST =buidCofig.getBookNovelDeployHost();
        BOOK_WEBVIEW_HOST =buidCofig.getBookWebviewHost();

        DATABASE_NAME = buidCofig.getDatabaseName();

        BAIDU_STAT_ID = buidCofig.getBaiduStatId();
        PUSH_KEY = buidCofig.getPushKey();
        ALIFEEDBACK_KEY = buidCofig.getAlifeedbackKey();
        ALIFEEDBACK_SECRET = buidCofig.getAlifeedbackSecret();
    }

    public static ReplaceConstants getReplaceConstants() {
        if (replaceConstants == null)
            replaceConstants = new ReplaceConstants();
        return replaceConstants;
    }
}
