package com.dingyue.statistics.common

/**
 * Desc 崩溃日志捕捉
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/8/23 11:09
 */
class CrashHandler : Thread.UncaughtExceptionHandler {


    override fun uncaughtException(thread: Thread?, throwable: Throwable?) {

    }
}