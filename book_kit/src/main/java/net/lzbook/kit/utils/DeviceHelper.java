package net.lzbook.kit.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telephony.TelephonyManager;

import java.util.Locale;

//获取设备相关信息
public class DeviceHelper {

    private static Context mContext;
    private static TelephonyManager mTelephonyManager;

    public static void setContext(Context context) {
        mContext = context;
    }

    public static String getImsi() {
        String imsi = "";
        imsi = getTelephonyManager().getSubscriberId();
        return imsi;
    }

    private static TelephonyManager getTelephonyManager() {
        if (mTelephonyManager == null)
            mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        return mTelephonyManager;
    }

    public static String getProviderName() {
        String provider = "";
        String imsi = getImsi();
        // IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。
        if (imsi != null) {

            if (imsi.startsWith("46000") || imsi.startsWith("46002") || imsi.startsWith("46007")) {
                provider = "mobile";//移动
            } else if (imsi.startsWith("46001")) {
                provider = "unicom";//联通
            } else if (imsi.startsWith("46003")) {
                provider = "telecom";//电信
            }
        }

        return provider;
    }

    public static String getLanguage() {
        String language = "";
        Locale locale = mContext.getResources().getConfiguration().locale;
        if (locale != null) {
            language = locale.getLanguage();
        }
        return language;
    }

    public static int getVersionCode() {
        int versionCode = 0;
        PackageManager packageManager = mContext.getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo;
        try {
            if (packageManager != null) {
                packInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
                if (packInfo != null) {
                    versionCode = packInfo.versionCode;
                }
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            AppLog.e("DeviceHelper", "getVersionCode error" + e);
        }
        return versionCode;
    }

    public static String getAppVersion() {
        // 获取packagemanager的实例
        PackageManager packageManager = mContext.getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo;
        String versionName = "";
        try {
            if (packageManager != null) {
                packInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
                if (packInfo != null) {
                    versionName = packInfo.versionName;
                }
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            AppLog.e("DeviceHelper", "getAppVersion error" + e);
        }
        return versionName;
    }
}
