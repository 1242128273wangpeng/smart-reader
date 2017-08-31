package net.lzbook.kit.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class NetWorkUtils {

    public static final int NETWORK_NONE = 0x50;
    public static final int NETWORK_WIFI = 0x51;
    public static final int NETWORK_MOBILE = 0x52;
    public static int NETWORK_TYPE = NETWORK_MOBILE;

    /**
     * for native ad
     **/
    public static final int NATIVE_AD_READY = 1;//加载广告成功
    public static final int NATIVE_AD_ERROR = 2;//加载广告失败
    public static int NATIVE_AD_TYPE = NATIVE_AD_ERROR;

    public static void initNetWorkType(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isAvailable()) {
            NETWORK_TYPE = NETWORK_NONE;
            return;
        }

        int networkInfoType = networkInfo.getType();
        if (networkInfoType == ConnectivityManager.TYPE_MOBILE) {
            NETWORK_TYPE = NETWORK_MOBILE;
        } else if (networkInfoType == ConnectivityManager.TYPE_WIFI) {
            NETWORK_TYPE = NETWORK_WIFI;
        }
    }

    public static int getNetWorkType(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isAvailable()) {
            return NETWORK_NONE;
        }
        int networkInfoType = networkInfo.getType();
        if (networkInfoType == ConnectivityManager.TYPE_MOBILE) {
            return NETWORK_MOBILE;
        } else if (networkInfoType == ConnectivityManager.TYPE_WIFI) {
            return NETWORK_WIFI;
        }
        return NETWORK_NONE;
    }

    public static String getNetWorkTypeNew(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isAvailable()) {
            return "无";
        }
        int networkInfoType = networkInfo.getType();
        if (networkInfoType == ConnectivityManager.TYPE_MOBILE) {
            return getMobileType(networkInfo.getSubtype());
        } else if (networkInfoType == ConnectivityManager.TYPE_WIFI) {
            return "wifi";
        }
        return "无";
    }

    private static String getMobileType(int subType) {
        switch (subType) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "2G"; // ~ 50-100 kbps
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "2G"; // ~ 14-64 kbps
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "2G"; // ~ 50-100 kbps
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "3G"; // ~ 400-1000 kbps
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "3G"; // ~ 600-1400 kbps
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "2G"; // ~ 100 kbps
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "3G"; // ~ 2-14 Mbps
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "3G"; // ~ 700-1700 kbps
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "3G"; // ~ 1-23 Mbps
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "3G"; // ~ 400-7000 kbps
            // NOT AVAILABLE YET IN API LEVEL 7
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "3G"; // ~ 1-2 Mbps
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "3G"; // ~ 5 Mbps
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G"; // ~ 10-20 Mbps
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G"; // ~25 kbps
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G"; // ~ 10+ Mbps
            // Unknown
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return "2G";
            default:
                return "2G";
        }
    }
}
