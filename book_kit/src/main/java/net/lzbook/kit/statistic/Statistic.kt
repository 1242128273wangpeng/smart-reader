package net.lzbook.kit.statistic

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.statistic.alilog.LOGClient
import net.lzbook.kit.statistic.alilog.LogGroup
import net.lzbook.kit.statistic.model.IAliLogModel
import net.lzbook.kit.utils.subscribekt
import java.lang.Exception
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by xian on 2017/7/3.
 */
private val endPoint = "cn-shanghai.log.aliyuncs.com"// Endpoint
private val accessKeyId = "LTAIHv56dMm1Dd5Z" // 使用您的阿里云访问密钥  AccessKeyId
private val accessKeySecret = "30hIE7U1i6D4azaCwsWnFWS19G4yAb" // 使用您的阿里云访问密钥

private val clients: ConcurrentHashMap<String, LOGClient> = ConcurrentHashMap()

private fun getLOGClient(log: IAliLogModel): LOGClient {
    var logClient = clients[log.project]
    if (logClient == null) {
        logClient = LOGClient(endPoint, accessKeyId, accessKeySecret, log.project)
        clients.put(log.project, logClient)
    }
    return logClient
}


fun alilog(log: IAliLogModel) {
    Observable.create<Boolean> {
        subcribe ->
        try {
            val logGroup = LogGroup()
            logGroup.PutLog(log.toLog())
            getLOGClient(log).PostLog(logGroup, log.logStore)
            subcribe.onComplete()
        } catch (e: Exception) {
            subcribe.onError(e)
        }
    }.subscribeOn(Schedulers.io())
            .doOnComplete {
                println("alilog complete")
            }
            .subscribekt(
                    onError = {
                        e ->
                        e.printStackTrace()
                    }
            )

}