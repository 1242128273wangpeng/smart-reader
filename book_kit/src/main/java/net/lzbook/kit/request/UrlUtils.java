package net.lzbook.kit.request;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.OpenUDID;
import net.lzbook.kit.encrypt.URLBuilderIntterface;

import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class UrlUtils {


    //正式线上API，域名例子：api.wubutianxia.com
    public static String BOOK_NOVEL_DEPLOY_HOST = ReplaceConstants.getReplaceConstants().BOOK_NOVEL_DEPLOY_HOST;
    //正式线上webview地址，域名例子：bookwebview.wubutianxia.com
    public static String BOOK_WEBVIEW_HOST = ReplaceConstants.getReplaceConstants().BOOK_WEBVIEW_HOST;

    public static String BOOK_CONTENT;


    private UrlUtils() {

    }

    public static String getBookNovelDeployHost() {
        return BOOK_NOVEL_DEPLOY_HOST;
    }

    public static void setBookNovelDeployHost(String bookNovelDeployHost) {
        if (!TextUtils.isEmpty(bookNovelDeployHost)) {
            BOOK_NOVEL_DEPLOY_HOST = bookNovelDeployHost;
        }
    }

    public static String getBookWebviewHost() {
        return BOOK_WEBVIEW_HOST;
    }

    public static void setBookWebviewHost(String bookWebviewHost) {
        if (!TextUtils.isEmpty(bookWebviewHost)) {
            BOOK_WEBVIEW_HOST = bookWebviewHost;
        }
    }

    public static String buildUrl(String uriTag, Map<String, String> params) {
        if (uriTag == null) {
            return null;
        }
        String novel_host = BOOK_NOVEL_DEPLOY_HOST;

        String channelId = AppUtils.getChannelId();
        String version = String.valueOf(AppUtils.getVersionCode());
        String packageName = AppUtils.getPackageName();
        String os = Constants.APP_SYSTEM_PLATFORM;
        String udid = OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext());
        String longitude = Constants.longitude + "";
        String latitude = Constants.latitude + "";
        String cityCode = Constants.cityCode;


        params.put("packageName", packageName);
        params.put("version", version);
        params.put("channelId", channelId);
        params.put("os", os);
        params.put("udid", udid);
        params.put("longitude", longitude);
        params.put("latitude", latitude);
        params.put("cityCode", cityCode);
        BaseBookApplication globalContext = BaseBookApplication.getGlobalContext();
        if (globalContext != null) {
            URLBuilderIntterface urlBuilderIntterface = globalContext.getUrlBuilderIntterface();
            if (urlBuilderIntterface != null) {
                String url = urlBuilderIntterface.buildUrl(novel_host, uriTag, params);
                return url;
            }
        }
        return null;
    }

    public static String buildWebUrl(String uriTag, Map<String, String> params) {
        if (uriTag == null) {
            return null;
        }
        String webView_host = BOOK_WEBVIEW_HOST;

        String channelId = AppUtils.getChannelId();
        String version = String.valueOf(AppUtils.getVersionCode());
        String packageName = AppUtils.getPackageName();
        String os = Constants.APP_SYSTEM_PLATFORM;
        String udid = OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext());
        String longitude = Constants.longitude + "";
        String latitude = Constants.latitude + "";
        String cityCode = Constants.cityCode;

        params.put("packageName", packageName);
        params.put("version", version);
        params.put("channelId", channelId);
        params.put("os", os);
        params.put("udid", udid);
        params.put("longitude", longitude);
        params.put("latitude", latitude);
        params.put("cityCode", cityCode);

        BaseBookApplication globalContext = BaseBookApplication.getGlobalContext();
        if (globalContext != null) {
            URLBuilderIntterface urlBuilderIntterface = globalContext.getUrlBuilderIntterface();
            if (urlBuilderIntterface != null) {
                String url = urlBuilderIntterface.buildUrl(webView_host, uriTag, params);
                return url;
            }
        }
        return null;
    }


    public static String buildDynamicParamasUrl(String uriTag, Map<String, String> params) {
        if (uriTag == null) {
            return null;
        }
        String dynamicHost = BOOK_NOVEL_DEPLOY_HOST;

        String channelId = AppUtils.getChannelId();
        String version = String.valueOf(AppUtils.getVersionCode());
        String packageName = AppUtils.getPackageName();
        String os = Constants.APP_SYSTEM_PLATFORM;
        String udid = OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext());

        params.put("udid", udid);
        params.put("channelId", channelId);
        params.put("packageName", packageName);
        params.put("os", os);
        params.put("version", version);
        BaseBookApplication globalContext = BaseBookApplication.getGlobalContext();
        if (globalContext != null) {
            URLBuilderIntterface urlBuilderIntterface = globalContext.getUrlBuilderIntterface();
            if (urlBuilderIntterface != null) {
                String url = urlBuilderIntterface.buildUrl(dynamicHost, uriTag, params);
                Log.e("updateUrl", url);
                return url;
            }
        }
        return null;
    }


    public static String buildContentUrl(String url) {
        if (url == null) {
            return null;
        }

        Map<String, String> params = new HashMap<>();
        String channelId = AppUtils.getChannelId();
        String version = String.valueOf(AppUtils.getVersionCode());
        String packageName = AppUtils.getPackageName();
        String os = Constants.APP_SYSTEM_PLATFORM;
        String udid = OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext());

        params.put("packageName", packageName);
        params.put("version", version);
        params.put("channelId", channelId);
        params.put("os", os);
        params.put("udid", udid);

        BaseBookApplication globalContext = BaseBookApplication.getGlobalContext();
        if (globalContext != null) {
            URLBuilderIntterface urlBuilderIntterface = globalContext.getUrlBuilderIntterface();
            if (urlBuilderIntterface != null) {
                String urls = urlBuilderIntterface.buildContentUrl(url, params);
                return urls;
            }
        }
        return null;
    }

    public static String buildDownBookUrl(String uriTag, Map<String, String> params) {
        if (uriTag == null) {
            return null;
        }
        String novel_host = BOOK_NOVEL_DEPLOY_HOST;

        String channelId = AppUtils.getChannelId();
        String version = String.valueOf(AppUtils.getVersionCode());
        String packageName = AppUtils.getPackageName();
        String os = Constants.APP_SYSTEM_PLATFORM;
        String udid = OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext());

        params.put("packageName", packageName);
        params.put("version", version);
        params.put("channelId", channelId);
        params.put("os", os);
        params.put("udid", udid);

        BaseBookApplication globalContext = BaseBookApplication.getGlobalContext();
        if (globalContext != null) {
            URLBuilderIntterface urlBuilderIntterface = globalContext.getUrlBuilderIntterface();
            if (urlBuilderIntterface != null) {
                String urls = urlBuilderIntterface.buildUrl(novel_host, uriTag, params);
                return urls;
            }
        }
        return null;
    }

    public static Map<String, String> getUrlParams(String param) {
        Map<String, String> map = new HashMap<String, String>();
        if ("".equals(param) || null == param) {
            return map;
        }
        String[] params = param.split("&");
        for (int i = 0; i < params.length; i++) {
            String[] p = params[i].split("=");
            if (p.length == 2) {
                map.put(p[0], p[1]);
            } else if (p.length == 1) {
                map.put(p[0], "");
            }
        }
        return map;
    }

    public static Map<String, String> getDataParams(String param) {
        Map<String, String> map = new HashMap<String, String>();
        if ("".equals(param) || null == param) {
            return map;
        }
        String[] params = param.split("#");
        for (int i = 0; i < params.length; i++) {

            int index = params[i].indexOf("=");
            String[] p = params[i].split(String.valueOf(params[i].charAt(index)));

            if (p.length >= 2) {
                map.put(p[0], p[1]);
            } else if (p.length == 1) {
                map.put(p[0], "");
            }
        }
        return map;
    }
}
