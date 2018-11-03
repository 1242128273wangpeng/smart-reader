package com.dingyue.statistics.utils

import android.Manifest
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.telephony.TelephonyManager
import android.text.format.Formatter
import android.util.DisplayMetrics
import android.view.WindowManager
import com.dingyue.statistics.common.GlobalContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*


object AppUtil {
    /**
     * 获取内核版本
     */
    val systemInnerVersion: String
        get() {
            return if (Build.DISPLAY.contains(Build.VERSION.INCREMENTAL)) {
                Build.DISPLAY
            } else {
                Build.VERSION.INCREMENTAL
            }
        }

    /**
     * X86架构
     */
    val x86: String
        get() = Build.CPU_ABI

    /**
     * 电池电量
     */
    //API 21 之后用 BATTERY_SERVICE 主动去获取电量
    val batteryLevel: String
        get() {
            var level = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val batteryManager = GlobalContext.getGlobalContext().getSystemService(Context.BATTERY_SERVICE) as BatteryManager?
                if (batteryManager != null) {
                    level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                }
            } else {
                val batteryInfoIntent = GlobalContext.getGlobalContext()
                        .registerReceiver(null,
                                IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                level = batteryInfoIntent?.getIntExtra("level", 0) ?: 0
            }
            return level.toString() + "%"
        }

    /**
     * 判读是否启动VPN
     */
    val isVPNUsed: Boolean
        get() {
            try {
                val niList = NetworkInterface.getNetworkInterfaces()
                if (niList != null) {
                    for (networkInterface in Collections.list(niList)) {
                        if (!networkInterface.isUp || networkInterface.interfaceAddresses.size == 0) {
                            continue
                        }
                        if ("tun0" == networkInterface.name || "ppp0" == networkInterface.name) {
                            return true
                        }
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }

            return false
        }

    //如果连接的是移动网络
    private val localIpAddress: String?
        get() {
            try {
                val nilist = Collections.list(
                        NetworkInterface.getNetworkInterfaces())
                for (ni in nilist) {
                    val ialist = Collections.list(ni.inetAddresses)
                    for (address in ialist) {
                        if (!address.isLoopbackAddress) {
                            return address.hostAddress
                        }
                    }
                }
            } catch (e: SocketException) {
                e.printStackTrace()
            }
            return null
        }

    /**
     * 获取蓝牙ID
     */
    val bluetoothID: String
        get() {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            return if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
                bluetoothAdapter.address
            } else ""

        }

    /**
     * 获取CPU型号
     */
    val cpuName: String?
        get() {
            try {
                val fr = FileReader("/proc/cpuinfo")
                val br = BufferedReader(fr)
                val text = br.readLine()
                val array = text.split(":\\s+".toRegex(), 2).toTypedArray()
                return array[1]
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }

    /**
     * 获取运营商信息
     */
    fun getProvidersName(context: Context?): String {
        var providersName = "NULL"
        if (context != null) {
            if (checkPermission(context)) {
                try {
                    val telephonyManager = context.getSystemService(
                            Context.TELEPHONY_SERVICE) as TelephonyManager? ?: return  providersName
                    val subscriberId = telephonyManager.subscriberId
                    if (subscriberId != null) {
                        if (subscriberId.startsWith("46000") || subscriberId.startsWith("46002")
                                || subscriberId.startsWith("46007")) {
                            providersName = "中国移动"
                        } else if (subscriberId.startsWith("46001")) {
                            providersName = "中国联通"
                        } else if (subscriberId.startsWith("46003")) {
                            providersName = "中国电信"
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
        return providersName
    }

    /**
     * 获取IMEI（设备串号）
     */
    fun getIMEI(context: Context): String {
        var deviceId = ""
        try {
            val telephonyManager = context.getSystemService(
                    Context.TELEPHONY_SERVICE) as TelephonyManager?
            deviceId = if (telephonyManager != null) telephonyManager.deviceId else ""
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return deviceId
    }

    /**
     * 获取无线局域网 WLAN MAC Address
     */
    fun getWLanMacAddress(context: Context): String {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager? ?: return ""
            var wifiName =  cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).extraInfo
            if (wifiName.startsWith("\"")) {
                wifiName = wifiName.substring(1, wifiName.length)
            }
            if (wifiName.endsWith("\"")) {
                wifiName = wifiName.substring(0, wifiName.length - 1)
            }
            return wifiName
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    /**
     * 获取MAC地址
     */
    fun getMacAddress(context: Context): String {
        try {
            val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager?
            return if (wm != null) wm.connectionInfo.macAddress else ""
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    /**
     * 获取在WiFi环境下，获取当前连接路由器的Mac地址
     */
    fun getWifiMacAddress(context: Context): String {
        try {
            val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager?
            return if (wm != null) wm.connectionInfo.bssid else ""
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }


    /**
     * 获取IP地址
     */
    fun getIPAddress(context: Context): String {
        var ip = ""
        val conMann = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager? ?: return ip
        val mobileNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        val wifiNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_WIFI)

        if (mobileNetworkInfo != null && mobileNetworkInfo.isConnected) {//移动网络
            ip = localIpAddress.orEmpty()
        } else if (wifiNetworkInfo != null && wifiNetworkInfo.isConnected) {//wifi网络
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager?
            if (wifiManager != null) {
                val wifiInfo = wifiManager.connectionInfo
                val ipAddress = wifiInfo.ipAddress
                ip = getWifiIPAddress(ipAddress)
            }
        }
        return ip
    }

    // 如果连接的是WI-FI网络
    private fun getWifiIPAddress(ipInt: Int): String {
        val sb = StringBuilder()
        sb.append(ipInt and 0xFF).append(".")
        sb.append(ipInt shr 8 and 0xFF).append(".")
        sb.append(ipInt shr 16 and 0xFF).append(".")
        sb.append(ipInt shr 24 and 0xFF)
        return sb.toString()
    }

    /**
     * 获得SD卡总大小
     */
    fun getSDTotalSize(context: Context): String {
        try {
            val path = Environment.getExternalStorageDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSize.toLong()
            val totalBlocks = stat.blockCount.toLong()
            return Formatter.formatFileSize(context, blockSize * totalBlocks)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    /**
     * 获得sd卡剩余容量，即可用大小
     */
    fun getSDAvailableSize(context: Context): String {
        try {
            val path = Environment.getExternalStorageDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSize.toLong()
            val availableBlocks = stat.availableBlocks.toLong()
            return Formatter.formatFileSize(context, blockSize * availableBlocks)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    /**
     * 获取屏幕分辨率
     */
    fun getScreenMetrics(context: Context): String {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager? ?: return ""
        val dm = DisplayMetrics()
        wm.defaultDisplay?.getMetrics(dm)
        val mScreenWidth = dm.widthPixels
        val mScreenHeight = dm.heightPixels
        return mScreenWidth.toString() + " * " + mScreenHeight
    }

    //获取用户手机安装的所有app列表
    fun scanLocalInstallAppList(packageManager: PackageManager): String {
        val sb = StringBuilder()
        try {
            val packageInfos = packageManager.getInstalledPackages(0)
            for (i in packageInfos.indices) {
                val packageInfo = packageInfos[i]
                //过滤掉系统app
                if (ApplicationInfo.FLAG_SYSTEM and packageInfo.applicationInfo.flags <= 0) {
                    if (i == packageInfos.size) {
                        sb.append(packageInfo.applicationInfo.loadLabel(packageManager))
                    } else {
                        sb.append(packageInfo.applicationInfo.loadLabel(packageManager)).append("`")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return sb.toString()
    }

    /**
     * 获取包名
     */
    fun getPackageName(context: Context?): String {
        return if (context != null) context.packageName else ""
    }

    /**
     * 获取版本名称
     */
    fun getVersionName(context: Context?): String {
        if (context == null) return ""
        try {
            return context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return ""
    }

    /**
     * 获取版本号
     */
    fun getVersionCode(context: Context?): String {
        if (context == null) return ""
        try {
            return context.packageManager.getPackageInfo(context.packageName, 0).versionCode.toString()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return ""
    }

    /**
     * 获取渠道
     */
    fun getAppChannel(context: Context?): String {
        if (context == null) return ""
        try {
            return context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
                    .metaData.getString("DY_APP_CHANNEL", "")
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return ""
    }

    /**
     * 检查权限
     */
    private fun checkPermission(context: Context?): Boolean {
        var flag = false
        if (context != null) {
            try {
                val pm = context.packageManager
                flag = PackageManager.PERMISSION_GRANTED == pm.checkPermission(Manifest.permission.READ_PHONE_STATE, context.packageName)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return flag
    }

    /**
     * 判断时间是否是同一天
     */
    fun isSameDay(firstTime: Long, secondTime: Long): Boolean {
        val pre = Calendar.getInstance()
        val predate = Date(secondTime)
        pre.time = predate

        val cal = Calendar.getInstance()
        val date = Date(firstTime)
        cal.time = date

        if (cal.get(Calendar.YEAR) == pre.get(Calendar.YEAR)) {
            val sameDay = cal.get(Calendar.DAY_OF_YEAR) - pre.get(Calendar.DAY_OF_YEAR)
            if (sameDay == 0) {
                return true
            }
        }

        return false
    }

    fun loadUserApplicationList(context: Context): String {
        try {
            val packageManager = context.packageManager
            val packages = packageManager.getInstalledPackages(0)
            if (packages.size > 0) {
                val appInfoList = JSONArray()
                var appInfo: JSONObject

                var usageStatsList: List<UsageStats>? = null

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                    val usageStatsManager = context.getSystemService(
                            Context.USAGE_STATS_SERVICE) as UsageStatsManager?

                    val calendar = Calendar.getInstance()
                    val endTime = calendar.timeInMillis

                    calendar.add(Calendar.DAY_OF_MONTH, -12)
                    val startTime = calendar.timeInMillis

                    if (usageStatsManager != null) {
                        usageStatsList = usageStatsManager.queryUsageStats(
                                UsageStatsManager.INTERVAL_MONTHLY, startTime, endTime)
                    }

                    for (packageInfo in packages) {
                        if (ApplicationInfo.FLAG_SYSTEM and packageInfo.applicationInfo.flags <= 0) {
                            appInfo = JSONObject()

                            packageInfo.applicationInfo.loadLabel(packageManager)

                            val packageName = packageInfo.packageName

                            //应用名称
                            appInfo.put("app_name", packageInfo.applicationInfo.loadLabel(packageManager).toString().replace(":", "").replace("`", ""))
                            //应用包名
                            appInfo.put("app_package_name", packageName.replace(":", "").replace("`", ""))
                            //应用安装时间
                            appInfo.put("app_install_time", packageInfo.firstInstallTime)
                            //应用最近一次更新时间
                            appInfo.put("app_last_update_time", packageInfo.lastUpdateTime)

                            if (usageStatsList != null && usageStatsList.isNotEmpty()) {
                                for (usageStats in usageStatsList) {
                                    val usagePackageName = usageStats.packageName

                                    if (usagePackageName == packageName) {
                                        //应用近1月总运行时长
                                        appInfo.put("app_last_month_run_time", usageStats.totalTimeInForeground)
                                        appInfo.put("app_last_month_used_time", usageStats.lastTimeUsed)
                                    }

                                    try {
                                        val field = usageStats.javaClass.getDeclaredField("mLaunchCount")
                                        if (field != null) {
                                            //应用近1月启动次数
                                            appInfo.put("app_last_month_start_num", field.getInt(usageStats))
                                        }
                                    } catch (exception: Exception) {
                                        exception.printStackTrace()
                                    }

                                }
                            }
                            appInfoList.put(appInfo)
                        }
                    }
                }

                return "app_infos:$appInfoList"
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        return ""
    }
}
