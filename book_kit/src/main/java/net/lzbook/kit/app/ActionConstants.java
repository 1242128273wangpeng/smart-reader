package net.lzbook.kit.app;

import net.lzbook.kit.utils.AppUtils;

public class ActionConstants {

    private static final String PACKAGE_NAME = AppUtils.getPackageName();

    /***
     * 检查更新完成的Action
     * **/
    public static final String ACTION_CHECK_UPDATE_FINISH = PACKAGE_NAME + ".update_notify";

    /***
     * APP下载完成的Action
     * **/
    public static final String ACTION_DOWNLOAD_APP_SUCCESS = PACKAGE_NAME + "_" + "DownloadIntentService";






    public static final String ACTION_CACHE_COMPLETE_WITH_ERR = "ACTION_CACHE_COMPLETE_WITH_ERR";
	
	public static final String ACTION_CACHE_WAIT_WIFI = "ACTION_CACHE_WAIT_WIFI";
	public static final String ACTION_CACHE_STATUS_CHANGE = "ACTION_CACHE_STATUS_CHANGE";
}
