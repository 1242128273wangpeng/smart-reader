package com.dy.reader

import android.annotation.SuppressLint
import android.app.Application

@SuppressLint("StaticFieldLeak")
/**
 * @desc Reader Application
 * @author lijun Lee
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/4/23 20:08
 */
object Reader {

    lateinit var context: Application

    fun init(context: Application) {
        this.context = context
    }
}