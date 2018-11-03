package com.dingyue.statistics.remote

import com.dingyue.statistics.log.LogGroup

/**
 * Desc 网络隔离层接口
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/8/23 14:48
 */
interface IUploadLogRO {

    fun postAppEvent(log: LogGroup)

    fun postInstallAppList(log: LogGroup)

    fun postAppCrash(crash: LogGroup)

    fun postReadContent(readContent: LogGroup)

    fun postFeedback(feedback: LogGroup)

    fun postUserLog(userLog: LogGroup)
}