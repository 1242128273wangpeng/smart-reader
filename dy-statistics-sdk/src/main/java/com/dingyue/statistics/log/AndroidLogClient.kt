package com.dingyue.statistics.log

import com.dingyue.statistics.common.PLItemKey
import com.dingyue.statistics.remote.IUploadLogRO
import com.dingyue.statistics.aliyun.AliyunUploadLogROImpl

import java.util.HashMap
import java.util.concurrent.ConcurrentLinkedQueue

object AndroidLogClient {

    // 定义发送队列
    private val send_queue = ConcurrentLinkedQueue<LogGroup>()
    private var senderThread: LogSendThread
    private var uploadLogRO: IUploadLogRO

    init {
        senderThread = LogSendThread()
        senderThread.start()
        uploadLogRO = AliyunUploadLogROImpl()
        //        uploadLogRO = new TestUploadROImpl();
    }

    fun putLog(logList: List<ServerLog>?) {
        try {
            //按照project和logstore分组，所以log中必须包含project和logstore两个key
            val map = HashMap<String, LogGroup>()
            if (logList != null && logList.isNotEmpty()) {
                for (i in logList.indices) {
                    if (!logList[i].content.containsKey("project") || !logList[i].content.containsKey("logstore")) {
                        continue
                    }
                    val project = logList[i].content["project"] as String
                    val logstore = logList[i].content["logstore"] as String
                    val unikey = project + "_" + logstore
                    if (!map.containsKey(unikey)) {
                        val logGroup = LogGroup("", "", project, logstore)
                        map[unikey] = logGroup
                    }
                    map[unikey]?.putLog(logList[i])
                    AppLog.e("ad", logList[i].content["project"] as String)
                }
                if (!map.values.isEmpty()) {
                    for (logGroup in map.values) {
                        send_queue.add(logGroup)
                        senderThread.wakeUp()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    // 开启一个线程专门用来发送数据
    private class LogSendThread : Thread() {
        private var loop = true
        private var isSleeping = false
        private val obj = java.lang.Object()

        override fun run() {
            while (loop) {
                isSleeping = false
                if (send_queue.isEmpty()) {
                    isSleeping = true
                    synchronized(obj) {
                        try {
                            obj.wait()
                        } catch (e: InterruptedException) {
                        }
                    }
                    continue
                }
                val logGroup = send_queue.poll()
                val project = logGroup.project
                val logstore = logGroup.logstore
                try {
                    if (project != null && logstore != null) {
                        // 根据不同类型(ZN_USER,ZN_APP_EVENT等)  调用不同上传接口
                        when (logstore) {
                            PLItemKey.ZN_APP_EVENT.logstore -> uploadLogRO.postAppEvent(logGroup)
                            PLItemKey.ZN_APP_APPSTORE.logstore -> uploadLogRO.postInstallAppList(logGroup)
                            PLItemKey.ZN_USER.logstore -> uploadLogRO.postUserLog(logGroup)
                            PLItemKey.ZN_PV.logstore -> uploadLogRO.postReadContent(logGroup)
                            PLItemKey.ZN_APP_FEEDBACK.logstore -> uploadLogRO.postFeedback(logGroup)
                        }
                    }
                    // 是否要有重试功能.......
                } catch (e: Exception) {
                    e.printStackTrace()
                    AndroidLogStorage.consumeFail(logGroup.logs)
                }
            }
        }

        fun stopThread() {
            loop = false
            synchronized(obj) {
                obj.notify()
            }
        }

        fun wakeUp() {
            synchronized(obj) {
                obj.notify()
            }
        }
    }

}
