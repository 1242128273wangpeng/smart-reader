package net.lzbook.kit.appender_loghub.appender

import com.google.gson.Gson
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.ServerLog
import net.lzbook.kit.data.greendao.dao.ServerLogDao
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.NetWorkUtils
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/**
 * Desc AndroidLog存储上传功能
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2017/11/9
 */
class AndroidLogStorage private constructor() {

    private val tag = "AndroidLog"

    private val logDao: ServerLogDao? by lazy {
        BaseBookApplication.getDaoSession()?.serverLogDao
    }

    private val majorityLogQuery = logDao?.queryBuilder()
            ?.where(ServerLogDao.Properties.EventType.eq(ServerLog.MAJORITY))
            ?.limit(AndroidLogConfig.DB_SIZE)
            ?.build()

    private val minorityLogQuery = logDao?.queryBuilder()
            ?.where(ServerLogDao.Properties.EventType.eq(ServerLog.MINORITY))
            ?.limit(AndroidLogConfig.DB_SIZE)
            ?.build()

    private val allLogQuery = logDao?.queryBuilder()?.build()

    private var isConsumeMajority = false

    private var isConsumeMinority = false

    private var latestMillis = System.currentTimeMillis()

    @Synchronized
    fun accept(serverLog: ServerLog) {

        serverLog.timeStamp = System.currentTimeMillis().toString()
        serverLog.contentJson = Gson().toJson(serverLog.GetContent())

        if (logDao == null) return
        when (serverLog.eventType) {
            ServerLog.MINORITY -> {//直接存入数据库
                AppLog.e(tag, "${ServerLog.MINORITY} 入数据库")

                dbSingleThread.execute {
                    AppLog.e(tag, "store 1 logs")
                    logDao?.insertOrReplace(serverLog)
                    if (!isConsumeMinority) {
                        isConsumeMinority = true
                        consume(ServerLog.MINORITY)
                    }
                }
            }
            ServerLog.MAJORITY -> {
                logQueue.add(serverLog)
                AppLog.e(tag, "${ServerLog.MAJORITY} 入队列 ${logQueue.size}")
                val currentMillis = System.currentTimeMillis()
                if ((currentMillis - latestMillis) / 1000 >= AndroidLogConfig.CONSUME_TIMEOUT_SEC
                        && !isConsumeMajority) {
                    isConsumeMajority = true
                    AppLog.e(tag, "${ServerLog.MAJORITY} 超时，入数据库，consume")
                    dbSingleThread.execute {
                        val list = logQueue.asList(logQueue.size)
                        AppLog.e(tag, "store ${list.size} logs")
                        logDao?.insertOrReplaceInTx(list)

                        consume(ServerLog.MAJORITY)
                    }
                } else if (logQueue.size >= AndroidLogConfig.CACHE_SIZE) {
                    AppLog.e(tag, "${ServerLog.MAJORITY} 队列满，入数据库")
                    dbSingleThread.execute {
                        val list = logQueue.asList(AndroidLogConfig.CACHE_SIZE)
                        AppLog.e(tag, "store ${list.size} logs")
                        logDao?.insertOrReplaceInTx(list)

                        if (allLogQuery == null) return@execute
                        val size = allLogQuery.forCurrentThread().listLazyUncached().count()
                        AppLog.d(tag, "total $size logs")
                        if (size >= AndroidLogConfig.DB_SIZE && !isConsumeMajority) {
                            isConsumeMajority = true
                            AppLog.e(tag, "${ServerLog.MAJORITY} 数据库满，consume")
                            consume(ServerLog.MAJORITY)
                        }
                    }
                }
            }
        }
    }


    fun clear() {
        if (logDao == null) return
        AppLog.e(tag, "clear：入数据库，consume")
        dbSingleThread.execute {
            val list = logQueue.asList(logQueue.size)

            AppLog.e(tag, "store ${list.size} logs")
            if (list.isNotEmpty()) logDao?.insertOrReplaceInTx(list)

            //清除过期七天的数据
            val minTimMillis = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            val deprecatedList = logDao?.queryBuilder()
                    ?.where(ServerLogDao.Properties.TimeStamp.lt(minTimMillis))
                    ?.build()
                    ?.list()
            logDao?.deleteInTx(deprecatedList)

            if (!isConsumeMinority) {
                isConsumeMinority = true
                consume(ServerLog.MINORITY)
            }
            if (!isConsumeMajority) {
                isConsumeMajority = true
                consume(ServerLog.MAJORITY)
            }
        }
    }

    fun consumeSuccess(logList: List<ServerLog>) {
        if (logDao == null) return
        dbSingleThread.execute {
            // 删除数据
            logDao?.deleteInTx(logList)
            AppLog.e(tag, "consume success, delete ${logList.size} logs")
        }
        //更新消费时间
        updateLatestMillis()
        resetConsumeState(logList)
    }

    fun consumeFail(logList: List<ServerLog>) {
        AppLog.w(tag, "consume fail")
        //更新消费时间
        updateLatestMillis()
        resetConsumeState(logList)
    }

    private fun consume(eventType: String) {
        if (majorityLogQuery == null || minorityLogQuery == null) return
        val logList =
                if (eventType == ServerLog.MAJORITY)
                    majorityLogQuery.forCurrentThread().listLazyUncached()
                else
                    minorityLogQuery.forCurrentThread().listLazyUncached()
        if (logList.isEmpty() || NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
            updateLatestMillis()
            if (eventType == ServerLog.MAJORITY) {
                isConsumeMajority = false
            } else {
                isConsumeMinority = false
            }
            AppLog.d(tag, "not consume, reset $eventType state")
        } else {
            consumeSingleThread.execute {
                AppLog.e(tag, "consuming ${logList.size} $eventType logs")
                AndroidLogClient.putLog(logList)
            }
        }
    }

    private fun resetConsumeState(logList: List<ServerLog>) {
        logList.forEach {
            if (it.eventType == ServerLog.MINORITY) {
                isConsumeMinority = false
            } else if (it.eventType == ServerLog.MAJORITY) {
                isConsumeMajority = false
            }
            if (!isConsumeMinority && !isConsumeMajority) return//返回了整个函数
        }
    }

    private fun updateLatestMillis() {
        latestMillis = System.currentTimeMillis()
    }

    @Synchronized
    private fun ConcurrentLinkedQueue<ServerLog>.asList(size: Int): ArrayList<ServerLog> {
        val list = ArrayList<ServerLog>()
        for (i in 0 until size) {
            val log = this.poll()
            if (log != null) {
                list.add(log)
            }
        }
        return list
    }


    companion object {

        private val dbSingleThread: ExecutorService = Executors.newSingleThreadExecutor()

        private val consumeSingleThread = Executors.newSingleThreadExecutor()

        private val logQueue = ConcurrentLinkedQueue<ServerLog>()

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