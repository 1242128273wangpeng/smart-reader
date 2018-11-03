package com.dingyue.statistics.aliyun

import com.dingyue.statistics.common.PLItemKey
import com.dingyue.statistics.log.LogGroup
import com.dingyue.statistics.remote.IUploadLogRO

/**
 * Desc 阿里云上传实现类
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/8/24 11:36
 */
class AliyunUploadLogROImpl : IUploadLogRO {

    private val endPoint = "cn-shenzhen.log.aliyuncs.com"// Endpoint
    private val znUserEndPoint = "cn-shanghai.log.aliyuncs.com"// zn_user EndPoint
    private val accessKeyId = "LTAIHv56dMm1Dd5Z" // 使用您的阿里云访问密钥
    private val accessKeySecret = "30hIE7U1i6D4azaCwsWnFWS19G4yAb" // 使用您的阿里云访问密钥

    private val normalClient by lazy { LogClient(endPoint, accessKeyId, accessKeySecret) }
    private val baseClient by lazy { LogClient(znUserEndPoint, accessKeyId, accessKeySecret) }

    override fun postAppEvent(log: LogGroup) {
        normalClient.setProject(PLItemKey.ZN_APP_EVENT.project)
        normalClient.postLog(log, PLItemKey.ZN_APP_EVENT.logstore)
    }

    override fun postInstallAppList(log: LogGroup) {
        normalClient.setProject(PLItemKey.ZN_APP_APPSTORE.project)
        normalClient.postLog(log, PLItemKey.ZN_APP_APPSTORE.logstore)
    }

    override fun postAppCrash(crash: LogGroup) {

    }

    override fun postReadContent(readContent: LogGroup) {
        baseClient.setProject(PLItemKey.ZN_PV.project)
        baseClient.postLog(readContent, PLItemKey.ZN_PV.logstore)
    }

    override fun postFeedback(feedback: LogGroup) {
        normalClient.setProject(PLItemKey.ZN_APP_FEEDBACK.project)
        normalClient.postLog(feedback, PLItemKey.ZN_APP_FEEDBACK.logstore)
    }

    override fun postUserLog(userLog: LogGroup) {
        baseClient.setProject(PLItemKey.ZN_USER.project)
        baseClient.postLog(userLog, PLItemKey.ZN_USER.logstore)
    }
}