package com.dingyue.statistics.common

import android.content.Context
import android.os.Build
import com.dingyue.statistics.exception.LogException
import com.dingyue.statistics.log.ServerLog
import com.dingyue.statistics.utils.AppUtil
import com.dingyue.statistics.utils.NetworkUtil
import com.dingyue.statistics.utils.SharedPreferencesUtil

/**
 * Desc 公共参数维护类 (功能包含参数获取与部分参数更新,以及参数校验)
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/8/27 10:35
 */
object CommonParams {

    const val NULL = "NULL"

    lateinit var udid: String
    var uid = NULL

    var appVersionName: String? = null
    var appVersionCode: String? = null
    var appChannel: String? = null
    var appPackageName: String? = null

    var longitude = NULL
    var latitude = NULL
    var cityInfo = NULL
    var cityCode = NULL
    var locationDetail = NULL

    var network = "none"

    var phoneIdentity: String? = null
    private var vendor: String? = null
    var operator: String? = null
    var resolutionRatio: String? = null

    /**
     * 初始化
     */
    fun init(out_udid: String?, appChannel: String? = null) {
        this.udid = if (!out_udid.isNullOrBlank()) out_udid!! else SelfOpenUDID.getOpenUDIDInContext(GlobalContext.getGlobalContext())
        this.appVersionName = AppUtil.getVersionName(GlobalContext.getGlobalContext())
        this.appVersionCode = AppUtil.getVersionCode(GlobalContext.getGlobalContext())
        this.appChannel = if (appChannel.isNullOrBlank()) AppUtil.getAppChannel(GlobalContext.getGlobalContext()) else appChannel
        this.appPackageName = AppUtil.getPackageName(GlobalContext.getGlobalContext())

        this.phoneIdentity = AppUtil.getIMEI(GlobalContext.getGlobalContext())
        this.vendor = Build.MODEL
        this.operator = AppUtil.getProvidersName(GlobalContext.getGlobalContext())
        this.resolutionRatio = AppUtil.getScreenMetrics(GlobalContext.getGlobalContext())

        this.network = NetworkUtil.getNetworkType(GlobalContext.getGlobalContext())

        // 从SharedPreference中获取位置信息
        val share = SharedPreferencesUtil(GlobalContext.getGlobalContext().getSharedPreferences("log_location", Context.MODE_PRIVATE))
        this.longitude = share.getString("longitude")
        this.latitude = share.getString("latitude")
        this.cityInfo = share.getString("cityInfo")
        this.cityCode = share.getString("cityCode")
        this.locationDetail = share.getString("locationDetail")
    }

    /**
     * 转换成ServerLog
     */
    fun toServerLog(key: PLItemKey): ServerLog {
        val log = ServerLog(key)
        log.putContent("udid", udid)
        log.putContent("uid", uid)

        log.putContent("app_package", appPackageName) // 包名
        log.putContent("app_version", appVersionName) // 版本名称
        log.putContent("app_version_code", appVersionCode) // 版本号
        log.putContent("app_channel_id", appChannel) // 渠道号

        log.putContent("longitude", longitude) // 经度
        log.putContent("latitude", latitude) // 纬度
        log.putContent("city_info", cityInfo) // 城市名称
        log.putContent("location_detail", locationDetail) // 详细地址

        log.putContent("phone_identity", phoneIdentity) // 手机唯一标识符
        log.putContent("vendor", vendor) // 设备信息
        log.putContent("operator", operator) // 运营商
        log.putContent("resolution_ratio", resolutionRatio) // 分辨率

        log.putContent("os", "android") // 手机操作系统
        log.putContent("network", network) // 网络状况

        return log
    }

    /**
     * 转换成HashMap
     */
    fun toHashMap(key: PLItemKey): HashMap<String, String?> {
        val map = HashMap<String, String?>()
        map["project"] = key.project
        map["logstore"] = key.logstore
        map["__time__"] = (System.currentTimeMillis() / 1000).toString()

        map["udid"] = udid
        map["uid"] = uid

        map["app_package"] = appPackageName
        map["app_version"] = appVersionName
        map["app_version_code"] = appVersionCode
        map["app_channel_id"] = appChannel

        map["longitude"] = longitude // 经度
        map["latitude"] = latitude // 纬度
        map["city_info"] = cityInfo // 城市名称
        map["location_detail"] = locationDetail // 详细地址

        map["phone_identity"] = phoneIdentity
        map["vendor"] = vendor
        map["operator"] = operator
        map["resolution_ratio"] = resolutionRatio

        map["os"] = "android"
        map["network"] = network

        return map
    }

    /**
     * 校验参数
     */
    fun paramsVerify(log: ServerLog) {
        if (log.content.isEmpty()) throw LogException("log empty ")

        // 验证必传参数
        if (!log.content.containsKey("udid")) {
            throw LogException("log lost param [udid] ")
        }
        if ((log.content["udid"] as String?).isNullOrBlank() || log.content["udid"] == NULL) {
            throw LogException("param:[udid] can not be null or blank , current value is ${log.content["udid"]}")
        }
        // 检验app_channel_id
        if (!log.content.containsKey("app_channel_id")) {
            throw LogException("log lost param [app_channel_id] ")
        }
        if ((log.content["app_channel_id"] as String?).isNullOrBlank() || log.content["app_channel_id"] == NULL) {
            throw LogException("param:[app_channel_id] can not be null or blank , current value is ${log.content["app_channel_id"]}")
        }
        if (log.content.containsKey("chapter_pages") && log.content["chapter_pages"].toString().toInt() > 100) {
            // 章节PV统计，页数大于100页，异常
            throw LogException("chapter_pages > 100 , please check param:[chapter_pages] ")
        }
        if (log.content.containsKey("start_time")
                && log.content.containsKey("end_time")
                && log.content.containsKey("chapter_pages")
                && (log.content["end_time"].toString().toLong() - log.content["start_time"].toString().toLong()) / log.content["chapter_pages"].toString().toInt() <= 1) {
            // 计算规则，(结束时间-开始时间) / 页数 = 每页阅读时长         时间参数单位：秒
            // 章节PV统计，每页平均阅读时间小于等于1s，视为异常统计
            throw LogException("read too fast , please check ")
        }
    }
}