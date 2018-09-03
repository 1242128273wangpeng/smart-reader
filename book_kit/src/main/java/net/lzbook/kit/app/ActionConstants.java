package net.lzbook.kit.app;

import net.lzbook.kit.utils.AppUtils;

public class ActionConstants {

    private static final String PACKAGE_NAME = AppUtils.getPackageName();

    /***
     * 书架添加默认书籍成功
     * **/
    public static final String ACTION_ADD_DEFAULT_SHELF = PACKAGE_NAME + "_add_default_shelf";

    /***
     * 检查更新完成的Action
     * **/
    public static final String ACTION_CHECK_UPDATE_FINISH = PACKAGE_NAME + ".update_notify";

    /***
     * APP下载完成的Action
     * **/
    public static final String ACTION_DOWNLOAD_APP_SUCCESS = PACKAGE_NAME + "_" + "DownloadIntentService";

    /***
     * 青果书籍状态检查成功的Action
     * **/
    public static final String ACTION_CHECK_QING_STATE_SUCCESS = PACKAGE_NAME + "_check_qing_state_success";

    /***
     * 改变夜间模式的Action
     * **/
    public static final String ACTION_CHANGE_NIGHT_MODE = PACKAGE_NAME + "_change_night_mode";


    /***
     * 下载书籍完成
     * **/
    public static final String ACTION_DOWNLOAD_BOOK_FINISH = PACKAGE_NAME + ".download_finish";

    /***
     * 下载书籍锁定
     * **/
    public static final String ACTION_DOWNLOAD_BOOK_LOCKED = PACKAGE_NAME + ".download_locked";

    public static final String ACTION_USER_LOGIN_INVALID = "ACTION_USER_LOGIN_INVALID";// 登录无效

    public static final String ACTION_CACHE_COMPLETE_WITH_ERR = "ACTION_CACHE_COMPLETE_WITH_ERR";
	
	public static final String ACTION_CACHE_WAIT_WIFI = "ACTION_CACHE_WAIT_WIFI";
	public static final String ACTION_CACHE_STATUS_CHANGE = "ACTION_CACHE_STATUS_CHANGE";
}
