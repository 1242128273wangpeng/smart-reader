package com.dingyue.statistics.log

import android.util.Log
import com.dingyue.statistics.DyStatService


/**
 * Log打印工具类
 */
object AppLog {

    private const val TAG = "AppLog"

    /**
     * 是否显示日志
     */
    private val showLog = DyStatService.sdkDebug

    /**
     * 一般信息
     * tag
     * msg
     */
    fun i(tag: String, msg: String) {
        if (showLog)
            Log.i(tag, msg)
    }

    /**
     * 错误信息
     * msg
     */
    fun e(msg: String) {
        if (showLog)
            Log.e(TAG, msg)
    }

    /**
     * 错误信息
     * tag
     * msg
     */
    fun e(tag: String, msg: String) {
        if (showLog)
            Log.e(tag, msg)
    }

    /**
     * 错误信息
     * tag
     * msg
     * tr
     */
    fun e(tag: String, msg: String, tr: Throwable) {
        if (showLog)
            Log.e(tag, msg, tr)
    }

    /**
     * 警告信息.
     * msg
     */
    fun w(msg: String) {
        if (showLog)
            Log.w(TAG, msg)
    }

    /**
     * 警告信息.
     * tag
     * msg
     */
    fun w(tag: String, msg: String) {
        if (showLog)
            Log.w(tag, msg)
    }

    /**
     * 警告信息.
     * tag
     * msg
     */
    fun w(tag: String, msg: String, tr: Throwable) {
        if (showLog)
            Log.w(tag, msg, tr)
    }

    /**
     * debug信息.
     * msg
     */
    fun d(msg: String) {
        if (showLog)
            Log.d(TAG, msg)
    }

    /**
     * debug信息.
     * tag
     * msg
     */
    fun d(tag: String, msg: String) {
        if (showLog)
            Log.d(tag, msg)
    }

    /**
     * 详细信息
     * tag
     * msg
     */
    fun v(tag: String, msg: String) {
        if (showLog)
            Log.v(tag, msg)
    }
}
