package com.dingyue.statistics

import android.app.Application
import android.content.Context
import com.dingyue.statistics.common.*
import com.dingyue.statistics.dao.bean.LocalLog
import com.dingyue.statistics.log.AndroidLogStorage
import com.dingyue.statistics.log.AppLog
import com.dingyue.statistics.utils.AppUtil
import com.dingyue.statistics.utils.FormatUtil
import com.dingyue.statistics.utils.SharedPreferencesUtil
import com.dingyue.statistics.utils.ToastUtil
import java.util.*
import java.util.concurrent.Executors

/**
 * Desc 统计功能对外提供的API
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/8/23 10:36
 */
object DyStatService {

    @JvmField
    var eventToastOpen = false
    @JvmField
    var sdkDebug = true
    @JvmField
    var needSavePointLog = false // 是否需要保存打点记录到本地文件

    lateinit var toastUtil: ToastUtil

    private var crashOpen = false
    private var statOpen = true
    private val prePageList = ArrayList<String>()
    private val statThread by lazy { Executors.newFixedThreadPool(5) }
    private val sharedUtil by lazy { SharedPreferencesUtil(GlobalContext.getGlobalContext().getSharedPreferences("log_config", Context.MODE_PRIVATE)) }

    /**
     * SDK初始化
     * @param application 启动的application
     * @param udid 外部维护的udid,调用者传null标识由SDK内部维护udid
     */
    @JvmStatic
    @JvmOverloads
    fun init(application: Application, udid: String? = null, appChannel: String? = null) {
        // 设置全局Context
        GlobalContext.setGlobalContext(application)
        // 设置异常捕捉
        if (crashOpen) Thread.setDefaultUncaughtExceptionHandler(CrashHandler())
        // appChannel 为DEBUG时，打开写入日志文件操作
        needSavePointLog = "DEBUG" == appChannel
        // 初始化Toast辅助类
        toastUtil = ToastUtil()
        // 设置通用参数
        CommonParams.init(udid, appChannel)
        // 上传用户数据
        sendZnUserLog()
        // 已安装App列表
        upLoadApps()
    }

    /**
     * 设置登录用户Id
     */
    @JvmStatic
    fun setLoginUserId(userId: String) {
        CommonParams.uid = userId
    }

    /**
     * 关闭写入文件操作
     */
    @JvmStatic
    fun disableSaveLogToFile() {
        needSavePointLog = false
    }


    /**
     * 设置位置信息
     * @param longitude 经度
     * @param latitude 纬度
     * @param cityInfo 城市名称
     * @param cityCode 城市编码
     * @param locationDetail 详细地址
     */
    @JvmStatic
    fun setLocationInfo(longitude: Double, latitude: Double, cityInfo: String, cityCode: String, locationDetail: String) {
        AppLog.e("xxxxx", "setLocationInfo longitude:[$longitude] latitude:[$latitude] cityInfo:[$cityInfo] cityCode:[$cityCode] locationDetail:[$locationDetail]")
        // 更新内存数据
        CommonParams.longitude = longitude.toString()
        CommonParams.latitude = latitude.toString()
        CommonParams.cityInfo = cityInfo
        CommonParams.cityCode = cityCode
        CommonParams.locationDetail = locationDetail
        // 更新sharedPreference数据
        val share = SharedPreferencesUtil(GlobalContext.getGlobalContext().getSharedPreferences("log_location", Context.MODE_PRIVATE))
        share.putString("longitude", longitude.toString())
        share.putString("latitude", latitude.toString())
        share.putString("cityInfo", cityInfo)
        share.putString("cityCode", cityCode)
        share.putString("locationDetail", locationDetail)
    }

    /**
     * 设置debug开关
     */
    @JvmStatic
    fun setDebugOn(debug: Boolean) {
        this.sdkDebug = debug
    }

    /**
     * 打开crash收集开关
     */
    @JvmStatic
    fun openCrashCollect(open: Boolean) {
        this.crashOpen = open
    }

    /**
     * 打开点位toast提示
     */
    @JvmStatic
    fun openEventToast(open: Boolean) {
        this.eventToastOpen = open
    }

    /**
     * 关闭统计服务
     */
    @JvmStatic
    fun closeStatService() {
        statOpen = false
    }

    /**
     * 打点统计
     * @param point 点位标识
     * @param extraParam 额外参数
     */
    @JvmStatic
    @JvmOverloads
    fun onEvent(point: PointIdentifyInterface, extraParam: Map<String, String>? = null) {
        onEvent(point.getPageCode(), point.getIdentificationCode(), extraParam)
    }

    /**
     * 打点统计
     * @param pageAndIdentify 格式 pageCode_identify
     * @param extraParam 额外参数
     */
    @JvmStatic
    @JvmOverloads
    fun onEvent(pageAndIdentify: String, extraParam: Map<String, String>? = null) {
        // 验证 参数格式  pageCode_identify
        val i = pageAndIdentify.indexOf("_")
        if (i == -1) {
            toastUtil.postMessage("pageAndIdentify 参数格式不正确，请检查 ")
        } else {
            // 获取pageCode和identify
            onEvent(pageAndIdentify.substring(0, i), pageAndIdentify.substring(i + 1), extraParam)
        }
    }

    /**
     * 打点统计
     * @param pageCode 页面标识
     * @param identify 点位标识
     * @param extraParam 附件参数  可为null
     */
    @JvmStatic
    @JvmOverloads
    fun onEvent(pageCode: String, identify: String, extraParam: Map<String, String>? = null) {
        if (!statOpen) {
            return
        }
        statThread.execute {
            // 获取通用参数
            val log = CommonParams.toServerLog(PLItemKey.ZN_APP_EVENT)

            // 追加特有参数
            log.putContent("page_code", pageCode) // 页面标识
            log.putContent("code", identify) //点击事件唯一标识
            log.putContent("pre_page_code", getPrePageCode(pageCode)) // 前一页标识
            log.putContent("log_time", System.currentTimeMillis().toString())//日志产生时间（毫秒数）

            //事件对应的额外的参数部分
            if (extraParam != null) {
                log.putContent("data", FormatUtil.forMatMap(extraParam))
            }
            if (identify == "APPINIT") log.eventType = LocalLog.MINORITY // App启动点位，立即上传日志

            AppLog.e(PLItemKey.ZN_APP_EVENT.key, log.content.toString())
            AndroidLogStorage.accept(log)
        }
    }

    /**
     * 阅读PV统计 （下一章时调用统计）
     * @param startReadTime 开始阅读时间
     * @param bookId 图书id
     * @param chapterId 章节id
     * @param sourceIds 图书来源
     * @param channelCode 渠道号
     * @param pageCount 页数
     */
    @JvmStatic
    fun sendPVData(startReadTime: Long, bookId: String, chapterId: String, sourceIds: String, channelCode: String, pageCount: Int) {
        if (!statOpen) {
            return
        }
        statThread.execute {
            // 获取通用参数
            val log = CommonParams.toServerLog(PLItemKey.ZN_PV)

            // 追加特有参数
            log.putContent("book_id", bookId)
            log.putContent("book_source_id", sourceIds)
            log.putContent("chapter_id", chapterId)
            log.putContent("channel_code", channelCode)
            log.putContent("chapter_read", "1")
            log.putContent("chapter_pages", pageCount.toString())
            log.putContent("start_time", (startReadTime / 1000L).toString())
            log.putContent("end_time", (System.currentTimeMillis() / 1000L).toString())

            AppLog.e(PLItemKey.ZN_PV.key, log.content.toString())
            AndroidLogStorage.accept(log)
        }
    }

    /**
     * 错误章节反馈 外部调用需要将章节实体类转化为map
     * @param errorParams 错误参数
     */
    @JvmStatic
    fun onChapterError(errorParams: Map<String, String>) {
        if (!statOpen) {
            return
        }
        statThread.execute {
            // 获取通用参数
            val log = CommonParams.toServerLog(PLItemKey.ZN_APP_FEEDBACK)

            // 追加特有参数
            if (!errorParams.isEmpty()) {
                errorParams.map { log.putContent(it.key, it.value) }
            }
            log.putContent("cityCode", CommonParams.cityCode)
            log.putContent("packageName", CommonParams.appPackageName)
            log.putContent("version", CommonParams.appVersionName)
            log.putContent("version_code", CommonParams.appVersionCode)
            log.putContent("channelId", CommonParams.appChannel)

            AppLog.e(PLItemKey.ZN_APP_FEEDBACK.key, log.content.toString())
            AndroidLogStorage.accept(log)
        }
    }

    /**
     * 清理无效数据
     */
    @JvmStatic
    fun clearInvalidData() {
        AndroidLogStorage.clear()
    }


    /**
     * 上传App列表
     */
    private fun upLoadApps() {
        if (!statOpen) {
            return
        }
        statThread.execute {
            if (AppUtil.isSameDay(sharedUtil.getLong("upload_app_time"), System.currentTimeMillis())) return@execute // 每天上传一次
            // 获取通用参数
            val log = CommonParams.toServerLog(PLItemKey.ZN_APP_APPSTORE)

            // 追加特有参数
            log.putContent("apps", AppUtil.scanLocalInstallAppList(GlobalContext.getGlobalContext().packageManager))
            log.putContent("data", AppUtil.loadUserApplicationList(GlobalContext.getGlobalContext()))
            log.putContent("time", System.currentTimeMillis().toString())

            AppLog.e(PLItemKey.ZN_APP_APPSTORE.key, log.content.toString())
            AndroidLogStorage.accept(log)
            sharedUtil.putLong("upload_app_time", System.currentTimeMillis()) // 记录上传时间
        }
    }

    /**
     * 上传用户信息
     */
    private fun sendZnUserLog() {
        if (!statOpen) {
            return
        }
        statThread.execute {
            if (AppUtil.isSameDay(sharedUtil.getLong("upload_user_time"), System.currentTimeMillis())) return@execute // 每天上传一次
            // 获取通用参数
            val log = CommonParams.toServerLog(PLItemKey.ZN_USER)

            // 追加特有参数 无

            AppLog.e(PLItemKey.ZN_USER.key, log.content.toString())
            AndroidLogStorage.accept(log)
            sharedUtil.putLong("upload_user_time", System.currentTimeMillis()) // 记录上传时间
        }
    }

    /**
     * 获取prePageCode
     */
    @Synchronized
    private fun getPrePageCode(pageCode: String): String {
        if (prePageList.size == 0 || (prePageList.size > 0 && prePageList[prePageList.size - 1] != pageCode)) {
            prePageList.add(pageCode)
            removePre(prePageList)
        }
        return if (prePageList.size != 0) {
            prePageList.map { AppLog.e(it) }
            if (prePageList.size > 1) {
                prePageList[prePageList.size - 2]
            } else {
                prePageList[prePageList.size - 1]
            }
        } else ""
    }

    /**
     * 移除prePageCode
     */
    private fun removePre(prePageList: MutableList<String>) {
        if (prePageList.size > 6) {
            for (i in 0..1) {
                prePageList.remove(prePageList[i])
            }
        }
    }
}


