package com.ding.basic.util

import com.alibaba.android.arouter.facade.template.IProvider

interface IBuildConfigProvider :IProvider{
    fun getAppPath():String
    fun getBookNovelDeployHost():String
    fun getBookWebviewHost():String
    fun getDatabaseName():String
    fun getBaiduStatId():String
    fun getPushKey():String
    fun getAlifeedbackKey():String
    fun getAlifeedbackSecret():String
    fun getMicroApiHost():String
    fun getContentApiHost():String
    fun getPackageName():String
    fun getVersionCode():Int
    fun getCDNHost(): String
}