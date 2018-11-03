package com.dingyue.statistics.log

import android.os.Build
import com.dingyue.statistics.DyStatService
import com.dingyue.statistics.common.CommonParams
import com.dingyue.statistics.common.GlobalContext
import com.dingyue.statistics.dao.LocalLogDao
import com.dingyue.statistics.dao.bean.LocalLog
import com.dingyue.statistics.dao.database.LocalLogDataBase
import com.dingyue.statistics.utils.AppUtil
import com.dingyue.statistics.utils.FileUtil
import com.dingyue.statistics.utils.NetworkUtil
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors

/**
 * Desc AndroidLog存储上传功能
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/19 0019 15:36
 */
object AndroidLogStorage {

    private var isConsumeMajority = false

    private var isConsumeMinority = false

    private var latestMillis = System.currentTimeMillis()

    private var localLogDao = LocalLogDataBase.getInstance(GlobalContext.getGlobalContext()).logDao()

    private const val TAG = "AndroidLog"

    private val dbSingleThread = Executors.newSingleThreadExecutor()

    private val consumeSingleThread = Executors.newSingleThreadExecutor()

    private val logQueue = ConcurrentLinkedQueue<LocalLog>()

    /**
     * 接收log
     */
    fun accept(serverLog: ServerLog) {

        /**
         * 上报字段：如能获取到则记录，获取不到标记空，
         * 必传字段为IMEI、手机型号、系统版本号、屏幕分辨率、运营商、网络类型；
         * 初期只上报page_code='SYSTEM'的点位，其他点位不传。
         */
        val map = serverLog.content
        if (map["page_code"] == "SYSTEM") {
            serverLog.putContent("phone_identity", CommonParams.phoneIdentity)//IMEI（设备串号）
            serverLog.putContent("model", Build.MODEL)//手机型号
            serverLog.putContent("sys_version", Build.VERSION.RELEASE)//系统版本号
            serverLog.putContent("resolution", CommonParams.resolutionRatio)//屏幕分辨率
            serverLog.putContent("mac_addr", AppUtil.getMacAddress(GlobalContext.getGlobalContext()))//MAC地址
            serverLog.putContent("wlan_name", AppUtil.getWLanMacAddress(GlobalContext.getGlobalContext()))//WLAN名称
            serverLog.putContent("operation", CommonParams.operator)//运营商
            serverLog.putContent("website", CommonParams.network)//网络类型
            serverLog.putContent("ip", AppUtil.getIPAddress(GlobalContext.getGlobalContext()))//IP
            serverLog.putContent("blue", AppUtil.bluetoothID)//蓝牙
            serverLog.putContent("citycode", CommonParams.cityCode)//城市信息
            serverLog.putContent("storage", AppUtil.getSDTotalSize(GlobalContext.getGlobalContext()))//内存大小
            serverLog.putContent("storage_used", AppUtil.getSDAvailableSize(GlobalContext.getGlobalContext()))//已使用内存
            serverLog.putContent("cpu", AppUtil.cpuName)//处理器
            serverLog.putContent("core_version", AppUtil.systemInnerVersion)//内核版本
            serverLog.putContent("battery", AppUtil.batteryLevel)//电池电量
            serverLog.putContent("x86_arch", AppUtil.x86)//X86架构
            serverLog.putContent("vpn", AppUtil.isVPNUsed.toString())//是否启用VPN
            serverLog.putContent("meid", AppUtil.getIMEI(GlobalContext.getGlobalContext()))//MEID
            serverLog.putContent("wifi_mac", AppUtil.getWifiMacAddress(GlobalContext.getGlobalContext()))//WiFi-Mac地址
        }

        if (DyStatService.eventToastOpen) { //打点Toast
            DyStatService.toastUtil.postMessage(serverLog.content.toString())
        }
        // 参数校验
        try {
            CommonParams.paramsVerify(serverLog)
        } catch (e: Exception) {
            AppLog.e("error:${e.message}")
            if (DyStatService.needSavePointLog) DyStatService.toastUtil.postMessage(e.message.orEmpty())
            return
        }
        // 写入文本文件
        if (DyStatService.needSavePointLog)
            FileUtil.appendLog("${serverLog.content["logstore"]}.txt", serverLog.content.toString())

        val type = serverLog.eventType
        val localLog = LocalLog(type, serverLog.content)

        when (type) {
            LocalLog.MINORITY -> {//直接存入数据库
                dbSingleThread.execute {
                    AppLog.e(TAG, "store 1 ${LocalLog.MINORITY} logs  localLogDao:[$localLogDao],isConsumeMinority:[$isConsumeMinority]")
                    localLogDao.insertOrReplace(localLog)
                    if (!isConsumeMinority) {
                        isConsumeMinority = true
                        localLogDao.consume(LocalLog.MINORITY)
                    }
                }
            }
            LocalLog.MAJORITY -> {
                logQueue.add(localLog)
                AppLog.e(TAG, "${LocalLog.MAJORITY} 入队列 ${logQueue.size}")
                val currentMillis = System.currentTimeMillis()
                if ((currentMillis - latestMillis) / 1000 >= AndroidLogConfig.CONSUME_TIMEOUT_SEC
                        && !isConsumeMajority) {
                    isConsumeMajority = true
                    AppLog.e(TAG, "${LocalLog.MAJORITY} 超时，入数据库，consume")
                    dbSingleThread.execute {
                        val list = logQueue.asList(logQueue.size)
                        AppLog.e(TAG, "store ${list.size} logs")
                        localLogDao.insertOrReplace(list)

                        localLogDao.consume(LocalLog.MAJORITY)
                    }
                } else if (logQueue.size >= AndroidLogConfig.CACHE_SIZE) {
                    AppLog.e(TAG, "${LocalLog.MAJORITY} 队列满，入数据库")
                    dbSingleThread.execute {
                        val list = logQueue.asList(AndroidLogConfig.CACHE_SIZE)
                        AppLog.e(TAG, "store ${list.size} logs")
                        localLogDao.insertOrReplace(list)

                        val size = localLogDao.getNumberOfRows()
                        AppLog.d(TAG, "total $size logs")
                        if (size >= AndroidLogConfig.DB_SIZE && !isConsumeMajority) {
                            isConsumeMajority = true
                            AppLog.e(TAG, "${LocalLog.MAJORITY} 数据库满，consume")
                            localLogDao.consume(LocalLog.MAJORITY)
                        }
                    }
                }
            }
        }
    }

    fun clear() {
        AppLog.e(TAG, "clear：入数据库，consume")
        dbSingleThread.execute {
            val list = logQueue.asList(logQueue.size)

            AppLog.e(TAG, "store ${list.size} logs")
            if (list.isNotEmpty()) localLogDao.insertOrReplace(list)

            //清除过期七天的数据
            val minTimMillis = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            localLogDao.deleteOutOfDate(minTimMillis)

            if (!isConsumeMinority) {
                isConsumeMinority = true
                localLogDao.consume(LocalLog.MINORITY)
            }
            if (!isConsumeMajority) {
                isConsumeMajority = true
                localLogDao.consume(LocalLog.MAJORITY)
            }
        }
    }

    fun consumeSuccess(serverLogList: List<ServerLog>) {
        dbSingleThread.execute {
            val localList: ArrayList<LocalLog> = ArrayList()
            for (serverLog in serverLogList) {
                val localLog = LocalLog(serverLog.id, serverLog.eventType, serverLog.content)
                localList.add(localLog)
            }
            // 删除数据
            localLogDao.delete(localList)
            AppLog.e(TAG, "consume success, delete ${serverLogList.size} logs")
        }
        //更新消费时间
        latestMillis = System.currentTimeMillis()
        resetConsumeState(serverLogList)
    }

    fun consumeFail(serverLogList: List<ServerLog>) {
        AppLog.w(TAG, "consume fail")
        //更新消费时间
        latestMillis = System.currentTimeMillis()
        resetConsumeState(serverLogList)
    }

    private fun LocalLogDao.consume(type: String) {
        val localLogList = query(type)
        AppLog.d("consume", "consume: size=${localLogList.size}")
        if (localLogList.isEmpty() || !NetworkUtil.isNetworkConnected(GlobalContext.getGlobalContext())) {
            latestMillis = System.currentTimeMillis()
            if (type == LocalLog.MAJORITY) {
                isConsumeMajority = false
            } else {
                isConsumeMinority = false
            }
            AppLog.d(TAG, "not consume, reset $type state")
        } else {
            consumeSingleThread.execute {
                AppLog.d(TAG, "consuming ${localLogList.size} $type logs")
                val serverLogList: ArrayList<ServerLog> = ArrayList()
                try {
                    localLogList.mapTo(serverLogList) { ServerLog(it.id, type, it.contentJson.orEmpty()) }
                } catch (e: OutOfMemoryError) {
                    e.printStackTrace()
                }
                AndroidLogClient.putLog(serverLogList)
            }
        }
    }

    private fun resetConsumeState(logList: List<ServerLog>) {
        logList.forEach {
            AppLog.d("resetConsumeState eventType:[${it.eventType}]")
            if (it.eventType == LocalLog.MINORITY) {
                isConsumeMinority = false
            } else if (it.eventType == LocalLog.MAJORITY) {
                isConsumeMajority = false
            }
            if (!isConsumeMinority && !isConsumeMajority) return//返回了整个函数
        }
    }


    @Synchronized
    private fun ConcurrentLinkedQueue<LocalLog>.asList(size: Int): ArrayList<LocalLog> {
        val list = ArrayList<LocalLog>()
        for (i in 0 until size) {
            val log = this.poll()
            if (log != null) {
                list.add(log)
            }
        }
        return list
    }

}