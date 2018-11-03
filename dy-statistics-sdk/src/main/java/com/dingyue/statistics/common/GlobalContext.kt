package com.dingyue.statistics.common

import android.app.Application
import android.content.Context

/**
 * Desc SDK全局 context
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/8/23 11:21
 */
object GlobalContext {

    private var application: Application? = null

    fun setGlobalContext(app: Application) {
        this.application = app
    }

    @JvmStatic
    fun getGlobalContext(): Context {
        if (application == null) throw NullPointerException("GlobalContext must invoke method setGlobalContext when init")
        return application!!.applicationContext
    }
}