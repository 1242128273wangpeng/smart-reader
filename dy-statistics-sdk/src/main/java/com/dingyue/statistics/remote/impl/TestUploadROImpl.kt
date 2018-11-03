package com.dingyue.statistics.remote.impl

import com.dingyue.statistics.log.AndroidLogStorage
import com.dingyue.statistics.log.LogGroup
import com.dingyue.statistics.remote.IUploadLogRO

/**
 * Desc
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/8/31 09:55
 */
class TestUploadROImpl:IUploadLogRO {
    override fun postAppEvent(log: LogGroup) {
        AndroidLogStorage.consumeSuccess(log.logs)
    }

    override fun postInstallAppList(log: LogGroup) {
        AndroidLogStorage.consumeSuccess(log.logs)
    }

    override fun postAppCrash(crash: LogGroup) {
        AndroidLogStorage.consumeSuccess(crash.logs)
    }

    override fun postReadContent(readContent: LogGroup) {
        AndroidLogStorage.consumeSuccess(readContent.logs)
    }

    override fun postFeedback(feedback: LogGroup) {
        AndroidLogStorage.consumeSuccess(feedback.logs)
    }

    override fun postUserLog(userLog: LogGroup) {
        AndroidLogStorage.consumeSuccess(userLog.logs)
    }
}