package com.dingyue.statistics.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Environment
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.TextUtils

import com.dingyue.statistics.input.MultiInputStreamHelper
import com.dingyue.statistics.log.AppLog
import com.dingyue.statistics.utils.FileUtil

import java.io.File
import java.util.UUID

/**
 * 调用者未传递udid 或者udid为空时，SDK内部维护udid
 */
object SelfOpenUDID {

    // 用户唯一标志
    const val PREF_KEY = "openuuid"
    const val COMMON_PREFS = "uuid_prefs"
    const val FILE_NAME = "uuid.text"
    private val TAG = SelfOpenUDID::class.java.simpleName
    private var openUdid: String? = null

    private var SDCARD_PATH: String? = null

    private val cachePath: String by lazy {
        val isSdCard = Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

        SDCARD_PATH = if (!isSdCard) "mnt/sdcard" else Environment.getExternalStorageDirectory().absolutePath

        SDCARD_PATH + File.separator + "udid"
    }

    private fun syncContext(mContext: Context) {
        if (openUdid == null) {
            // 从 SharedPreferences中获取
            val mPreferences = mContext.getSharedPreferences(COMMON_PREFS, Context.MODE_PRIVATE)
            var keyInPref = mPreferences.getString(PREF_KEY, null)
            val filePath = cachePath + File.separator + FILE_NAME
            if (keyInPref == null) {
                val file = File(filePath)
                if (file.exists()) {
                    // 从文件中获取
                    val bytes = FileUtil.readBytes(filePath)
                    if (bytes != null) {
                        keyInPref = String(MultiInputStreamHelper.encrypt(bytes))
                        //应用内存被清理后, 需要恢复id
                        mPreferences.edit().putString(PREF_KEY, keyInPref).apply()
                    }
                }
            }

            if (keyInPref == null) {
                openUdid = getUniqueId(mContext)
                val e = mPreferences.edit()
                e.putString(PREF_KEY, openUdid)
                e.apply()

                FileUtil.writeByteFile(filePath, MultiInputStreamHelper.encrypt(openUdid!!.toByteArray()))
            } else {
                openUdid = keyInPref
            }

            if (!File(filePath).exists()) {
                FileUtil.writeByteFile(filePath, MultiInputStreamHelper.encrypt(openUdid!!.toByteArray()))
            }
            AppLog.d(TAG, "openUdid= " + openUdid!!)
        }
    }

    fun getOpenUDIDInContext(context: Context): String {
        syncContext(context)
        return openUdid.orEmpty()
    }

    private fun getUniqueId(context: Context): String {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        var tmDevice: String? = null
        var androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        var macAd: String?
        var serialnum: String? = null
        val wifiMan = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (PackageManager.PERMISSION_GRANTED == context.packageManager
                        .checkPermission(Manifest.permission.READ_PHONE_STATE,
                                context.packageName)) {
            tmDevice = tm.deviceId
        }
        macAd = wifiMan.connectionInfo.macAddress
        try {
            val c = Class.forName("android.os.SystemProperties")
            val get = c.getMethod("get", String::class.java, String::class.java)
            serialnum = get.invoke(c, "ro.serialno", "unknown") as String
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (tmDevice == null || TextUtils.isEmpty(tmDevice) && androidId == null || TextUtils.isEmpty(androidId) && macAd == null || TextUtils.isEmpty(macAd) && serialnum == null || TextUtils.isEmpty(serialnum)) {
            AppLog.e(TAG, "uniqueId= " + UUID.randomUUID().toString())
            return UUID.randomUUID().toString()
        }
        tmDevice = "IEMI:$tmDevice"
        androidId = "ANDROID_ID:" + androidId!!
        macAd = "MAC:" + macAd!!
        serialnum = "SERIAL:" + serialnum!!
        val deviceUuid = UUID(androidId.hashCode().toLong(), (tmDevice.hashCode() or macAd.hashCode() or serialnum.hashCode()).toLong())
        val uniqueId = deviceUuid.toString()
        AppLog.e(TAG, "uniqueId= $uniqueId")
        return uniqueId
    }
}
