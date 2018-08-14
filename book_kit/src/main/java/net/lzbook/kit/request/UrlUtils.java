package net.lzbook.kit.request;

import com.ding.basic.Config;
import com.dingyue.contract.util.SharedPreUtil;

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


    private static SharedPreUtil sharedPreUtil = new SharedPreUtil(0);


    /**
     * 正式地址
     */
    private static String BOOK_NOVEL_DEPLOY_HOST = sharedPreUtil.getString(
            SharedPreUtil.Companion.getAPI_URL()).isEmpty() ?
            ReplaceConstants.getReplaceConstants().BOOK_NOVEL_DEPLOY_HOST
            : sharedPreUtil.getString(SharedPreUtil.Companion.getAPI_URL());
    private static String BOOK_WEBVIEW_HOST = sharedPreUtil.getString(
            SharedPreUtil.Companion.getWEB_URL()).isEmpty() ?
            ReplaceConstants.getReplaceConstants().BOOK_WEBVIEW_HOST
            : sharedPreUtil.getString(SharedPreUtil.Companion.getWEB_URL());


    public static String getBookNovelDeployHost() {
        return BOOK_NOVEL_DEPLOY_HOST;
    }

    public static String getBookWebViewHost() {
        return BOOK_WEBVIEW_HOST;
    }

    /**
     * 测试~注掉赋值部分代码
     */
    public static void setBookNovelDeployHost(String bookNovelDeployHost) {
        if (!TextUtils.isEmpty(bookNovelDeployHost)) {
            if (!BOOK_NOVEL_DEPLOY_HOST.contains("test") && !bookNovelDeployHost.contains("test")) {
                if (sharedPreUtil.getBoolean(SharedPreUtil.START_PARAMS)) {
                    BOOK_NOVEL_DEPLOY_HOST = bookNovelDeployHost;
                }
            }
        }
    }

    public static void setBookWebViewHost(String bookWebViewHost) {

        if (!TextUtils.isEmpty(bookWebViewHost)) {

            if (sharedPreUtil.getBoolean(SharedPreUtil.START_PARAMS)) {
                BOOK_WEBVIEW_HOST = bookWebViewHost;
            }
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
        String webView_host = Config.INSTANCE.loadWebViewHost();

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
