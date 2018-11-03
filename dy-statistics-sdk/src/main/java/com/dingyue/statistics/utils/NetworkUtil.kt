package com.dingyue.statistics.utils

import android.content.Context
import android.net.ConnectivityManager
import android.telephony.TelephonyManager

/**
 * Desc 网络连接辅助类
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/8/28 15:10
 */
object NetworkUtil {

    /**
     * 获取网络类型 wifi、4G、3G、2G or none
     * @param context
     * @return
     */
    fun getNetworkType(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo == null || !networkInfo.isAvailable) return "none"
        val networkInfoType = networkInfo.type
        if (networkInfoType == ConnectivityManager.TYPE_MOBILE) {
            return getMobileType(networkInfo.subtype)
        } else if (networkInfoType == ConnectivityManager.TYPE_WIFI) {
            return "wifi"
        }
        return "none"
    }

    /**
     * 判断网络是否连接
     * @param context
     * @return
     */
    fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isAvailable
    }

    private fun getMobileType(subType: Int): String {
        when (subType) {
            TelephonyManager.NETWORK_TYPE_1xRTT -> return "2G" // ~ 50-100 kbps
            TelephonyManager.NETWORK_TYPE_CDMA -> return "2G" // ~ 14-64 kbps
            TelephonyManager.NETWORK_TYPE_EDGE -> return "2G" // ~ 50-100 kbps
            TelephonyManager.NETWORK_TYPE_EVDO_0 -> return "3G" // ~ 400-1000 kbps
            TelephonyManager.NETWORK_TYPE_EVDO_A -> return "3G" // ~ 600-1400 kbps
            TelephonyManager.NETWORK_TYPE_GPRS -> return "2G" // ~ 100 kbps
            TelephonyManager.NETWORK_TYPE_HSDPA -> return "3G" // ~ 2-14 Mbps
            TelephonyManager.NETWORK_TYPE_HSPA -> return "3G" // ~ 700-1700 kbps
            TelephonyManager.NETWORK_TYPE_HSUPA -> return "3G" // ~ 1-23 Mbps
            TelephonyManager.NETWORK_TYPE_UMTS -> return "3G" // ~ 400-7000 kbps
        // NOT AVAILABLE YET IN API LEVEL 7
            TelephonyManager.NETWORK_TYPE_EHRPD -> return "3G" // ~ 1-2 Mbps
            TelephonyManager.NETWORK_TYPE_EVDO_B -> return "3G" // ~ 5 Mbps
            TelephonyManager.NETWORK_TYPE_HSPAP -> return "3G" // ~ 10-20 Mbps
            TelephonyManager.NETWORK_TYPE_IDEN -> return "2G" // ~25 kbps
            TelephonyManager.NETWORK_TYPE_LTE -> return "4G" // ~ 10+ Mbps
        // Unknown
            TelephonyManager.NETWORK_TYPE_UNKNOWN -> return "2G"
            else -> return "2G"
        }
    }
}
