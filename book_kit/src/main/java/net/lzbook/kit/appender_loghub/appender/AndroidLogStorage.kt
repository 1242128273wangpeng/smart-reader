package net.lzbook.kit.appender_loghub.appender

import com.ding.basic.bean.LocalLog
import com.ding.basic.dao.LocalLogDao
import com.ding.basic.database.LocalLogDataBase
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.ServerLog
import net.lzbook.kit.utils.AppLog
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

    fun accept(serverLog: ServerLog) {
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
                for (localLog in localLogList) {
                    serverLogList.add(ServerLog(localLog.id, localLog.contentJson))
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