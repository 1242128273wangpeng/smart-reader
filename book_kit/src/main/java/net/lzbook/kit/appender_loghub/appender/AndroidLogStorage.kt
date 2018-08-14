package net.lzbook.kit.appender_loghub.appender

import android.content.Context
import com.ding.basic.bean.LocalLog
import com.ding.basic.dao.LocalLogDao
import com.ding.basic.database.LocalLogDataBase
import com.dingyue.contract.util.CommonUtil
import com.dingyue.contract.util.SharedPreUtil
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.ServerLog
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.NetWorkUtils
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Desc AndroidLog存储上传功能
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/19 0019 15:36
 */
class AndroidLogStorage {

    private var isConsumeMajority = false

    private var isConsumeMinority = false

    private var latestMillis = System.currentTimeMillis()

    private var localLogDao: LocalLogDao? = null

    fun accept(serverLog: ServerLog, context: Context) {

        /**
         * 上报字段：如能获取到则记录，获取不到标记空，
         * 必传字段为IMEI、手机型号、系统版本号、屏幕分辨率、运营商、网络类型；
         * 初期只上报page_code='SYSTEM'的点位，其他点位不传。
         */
        val map = serverLog.content
        if (map["page_code"] == "SYSTEM") {
            serverLog.putContent("phone_identity", AppUtils.getIMEI(context))//IMEI（设备串号）
            serverLog.putContent("model", AppUtils.getPhoneModel())//手机型号
            serverLog.putContent("sys_version", AppUtils.getRelease())//系统版本号
            serverLog.putContent("resolution", AppUtils.getScreenMetrics(context))//屏幕分辨率
            serverLog.putContent("mac_addr", AppUtils.getMacAddress(context))//MAC地址
            serverLog.putContent("wlan_name", AppUtils.getWLanMacAddress(context))//WLAN名称
            serverLog.putContent("operation", AppUtils.getProvidersName(context))//运营商
            serverLog.putContent("website", AppUtils.getNetState(context))//网络类型
            serverLog.putContent("ip", AppUtils.getIPAddress(context))//IP
            serverLog.putContent("blue", AppUtils.getBluetoothID())//蓝牙
            serverLog.putContent("citycode", Constants.cityCode)//城市信息
            serverLog.putContent("storage", AppUtils.getSDTotalSize(context))//内存大小
            serverLog.putContent("storage_used", AppUtils.getSDAvailableSize(context))//已使用内存
            serverLog.putContent("cpu", AppUtils.getCpuName())//处理器
            serverLog.putContent("core_version", AppUtils.getSystemInnerVersion())//内核版本
            serverLog.putContent("battery", AppUtils.getBatteryLevel())//电池电量
            serverLog.putContent("x86_arch", AppUtils.getX86())//X86架构
            serverLog.putContent("vpn", AppUtils.getIsVPNUsed().toString())//是否启用VPN
            serverLog.putContent("meid", AppUtils.getIMEI(context))//MEID
            serverLog.putContent("wifi_mac", AppUtils.getWifiMacAddress(context))//WiFi-Mac地址

        }

        val sp = SharedPreUtil(SharedPreUtil.SHARE_ONLINE_CONFIG)
        if (sp.getBoolean(SharedPreUtil.SHOW_TOAST_LOG, false)) { //打点Toast
            CommonUtil.showToastMessage(serverLog.content.toString())
        }

        AppLog.e("Joann", "1、IMEI（设备串号）:${AppUtils.getIMEI(context)}\n" +
                "2、手机型号:${AppUtils.getPhoneModel()}\n" +
                "3、系统版本号:${AppUtils.getRelease()}\n" +
                "4、屏幕分辨率:${AppUtils.getScreenMetrics(context)}\n" +
                "5、MAC地址:${AppUtils.getMacAddress(context)}\n" +
                "6、WLAN名称:${AppUtils.getWLanMacAddress(context)}\n" +
                "7、运营商:${AppUtils.getProvidersName(context)}\n" +
                "8、网络类型:${AppUtils.getNetState(context)}\n" +
                "9、IP:${AppUtils.getIPAddress(context)}\n" +
                "10、蓝牙:${AppUtils.getBluetoothID()}\n" +
                "11、城市信息:${Constants.cityCode}\n" +
                "12、内存大小:${AppUtils.getSDTotalSize(context)}\n" +
                "13、已使用内存:${AppUtils.getSDAvailableSize(context)}\n" +
                "14、处理器:${AppUtils.getCpuName()}\n" +
                "15、内核版本:${AppUtils.getSystemInnerVersion()}\n" +
                "18、是否启用VPN:${AppUtils.getIsVPNUsed()}\n" +

                "16、电池电量:${AppUtils.getBatteryLevel()}\n" +
                "17、X86架构:${AppUtils.getX86()}\n" +
                "19、MEID:${AppUtils.getIMEI(context)}\n" +
                "20、WiFi-Mac地址:${AppUtils.getWifiMacAddress(context)}")

        val type = serverLog.eventType
        val localLog = LocalLog(type, serverLog.content)
        if (localLogDao == null) {
            localLogDao = LocalLogDataBase.getInstance(BaseBookApplication.getGlobalContext()).logDao()
        }
        when (type) {
            LocalLog.MINORITY -> {//直接存入数据库
                dbSingleThread.execute {
                    AppLog.e(TAG, "store 1 ${LocalLog.MINORITY} logs")
                    localLogDao?.insertOrReplace(localLog)
                    if (!isConsumeMinority) {
                        isConsumeMinority = true
                        localLogDao?.consume(LocalLog.MINORITY)
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
                        localLogDao?.insertOrReplace(list)

                        localLogDao?.consume(LocalLog.MAJORITY)
                    }
                } else if (logQueue.size >= AndroidLogConfig.CACHE_SIZE) {
                    AppLog.e(TAG, "${LocalLog.MAJORITY} 队列满，入数据库")
                    dbSingleThread.execute {
                        val list = logQueue.asList(AndroidLogConfig.CACHE_SIZE)
                        AppLog.e(TAG, "store ${list.size} logs")
                        localLogDao?.insertOrReplace(list)

                        val size = localLogDao?.getNumberOfRows() ?: 0
                        AppLog.d(TAG, "total $size logs")
                        if (size >= AndroidLogConfig.DB_SIZE && !isConsumeMajority) {
                            isConsumeMajority = true
                            AppLog.e(TAG, "${LocalLog.MAJORITY} 数据库满，consume")
                            localLogDao?.consume(LocalLog.MAJORITY)
                        }
                    }
                }
            }
        }
    }

    fun clear() {
        if (localLogDao == null) {
            localLogDao = LocalLogDataBase.getInstance(BaseBookApplication.getGlobalContext()).logDao()
        }
        AppLog.e(TAG, "clear：入数据库，consume")
        dbSingleThread.execute {
            val list = logQueue.asList(logQueue.size)

            AppLog.e(TAG, "store ${list.size} logs")
            if (list.isNotEmpty()) localLogDao?.insertOrReplace(list)

            //清除过期七天的数据
            val minTimMillis = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            localLogDao?.deleteOutOfDate(minTimMillis)

            if (!isConsumeMinority) {
                isConsumeMinority = true
                localLogDao?.consume(LocalLog.MINORITY)
            }
            if (!isConsumeMajority) {
                isConsumeMajority = true
                localLogDao?.consume(LocalLog.MAJORITY)
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
            localLogDao?.delete(localList)
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
        if (localLogList.isEmpty() || NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
            latestMillis = System.currentTimeMillis()
            if (type == LocalLog.MAJORITY) {
                isConsumeMajority = false
            } else {
                isConsumeMinority = false
            }
            AppLog.d(TAG, "not consume, reset $type state")
        } else {
            consumeSingleThread.execute {
                AppLog.e(TAG, "consuming ${localLogList.size} $type logs")
                val serverLogList: ArrayList<ServerLog> = ArrayList()
                try {
                    /*for (localLog in localLogList) {
                        serverLogList.add(ServerLog(localLog.id, localLog.contentJson))
                    }*/

                    Integer.valueOf("10")
                    Integer.parseInt("10")
                    localLogList.mapTo(serverLogList) { ServerLog(it.id, it.contentJson) }
                } catch (e: OutOfMemoryError) {
                    e.printStackTrace()
                }
                AndroidLogClient.putLog(serverLogList)
            }
        }
    }

    private fun resetConsumeState(logList: List<ServerLog>) {
        logList.forEach {
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

    companion object {

        private const val TAG = "AndroidLog"

        private val dbSingleThread: ExecutorService = Executors.newSingleThreadExecutor()

        private val consumeSingleThread = Executors.newSingleThreadExecutor()

        private val logQueue = ConcurrentLinkedQueue<LocalLog>()

        @Volatile
        private var logStorage: AndroidLogStorage? = null

        @JvmStatic
        fun getInstance(): AndroidLogStorage {
            if (logStorage == null) {
                synchronized(AndroidLogStorage::class.java) {
                    if (logStorage == null) {
                        logStorage = AndroidLogStorage()
                    }
                }
            }
            return logStorage!!
        }

    }
}