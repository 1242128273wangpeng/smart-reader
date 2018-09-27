package net.lzbook.kit.utils.webview;

import android.util.Log;

import com.ding.basic.net.Config;

import net.lzbook.kit.app.base.BaseBookApplication;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.utils.encrypt.URLBuilderIntterface;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.OpenUDID;

import java.util.HashMap;
import java.util.Map;

public class UrlUtils {

    public static String buildUrl(String uriTag, Map<String, String> params) {
        if (uriTag == null) {
            return null;
        }
        String novel_host = Config.INSTANCE.loadRequestAPIHost();

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
                return urlBuilderIntterface.buildUrl(novel_host, uriTag, params);
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
        String dynamicHost = Config.INSTANCE.loadRequestAPIHost();

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
                return urlBuilderIntterface.buildContentUrl(url, params);
            }
        }
        return null;
    }

    public static String buildDownBookUrl(String uriTag, Map<String, String> params) {
        if (uriTag == null) {
            return null;
        }
        String novel_host = Config.INSTANCE.loadRequestAPIHost();

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
                return urlBuilderIntterface.buildUrl(novel_host, uriTag, params);
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
        for (String param1 : params) {
            String[] p = param1.split("=");
            if (p.length == 2) {
                map.put(p[0], p[1]);
            } else if (p.length == 1) {
                map.put(p[0], "");
            }
        }
        return map;
    }

    public static Map<String, String> getDataParams(String param) {
        Map<String, String> map = new HashMap<>();
        if ("".equals(param) || null == param) {
            return map;
        }
        String[] params = param.split("#");
        for (String param1 : params) {

            int index = param1.indexOf("=");
            String[] p = param1.split(String.valueOf(param1.charAt(index)));

            if (p.length >= 2) {
                map.put(p[0], p[1]);
            } else if (p.length == 1) {
                map.put(p[0], "");
            }
        }
        return map;
    }
}
