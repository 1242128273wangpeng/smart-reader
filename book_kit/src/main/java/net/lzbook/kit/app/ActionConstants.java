package net.lzbook.kit.app;

import net.lzbook.kit.utils.AppUtils;

/**
 * Created by Administrator on 2016/11/17 0017.
 */
public class ActionConstants {

    public static final String PackageName = AppUtils.getPackageName();
    public static final String DOWN_APP_SUCCESS_ACTION = PackageName + "_" + "DownloadIntentService";

    public static final String ACTION_CACHE_COMPLETE_WITH_ERR = "ACTION_CACHE_COMPLETE_WITH_ERR";
	
	public static final String ACTION_CACHE_WAIT_WIFI = "ACTION_CACHE_WAIT_WIFI";
	public static final String ACTION_CACHE_STATUS_CHANGE = "ACTION_CACHE_STATUS_CHANGE";
}
