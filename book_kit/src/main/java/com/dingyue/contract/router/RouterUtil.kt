package com.dingyue.contract.router

import android.app.Activity
import android.os.Bundle

import com.alibaba.android.arouter.launcher.ARouter

/**
 * Created on 2018/4/19.
 * Created by crazylei.
 */
object RouterUtil {

    /**
     * 仅在使用协议页面进入可以打开调试模式
     */
    var isFormDisclaimerPage = "isFormDisclaimerPage"

    /**
     * 构建标准的路由请求
     */
    fun navigation(activity: Activity, path: String) {
        ARouter.getInstance()
                .build(path)
                .navigation(activity)
    }

    /**
     * 构建标准的路由请求
     */
    fun navigation(activity: Activity, path: String, bundle: Bundle) {
        ARouter.getInstance()
                .build(path)
                .with(bundle)
                .navigation(activity)
    }

    fun navigationWithCode(activity: Activity, path: String, bundle: Bundle, code: Int) {
        ARouter.getInstance()
                .build(path)
                .with(bundle)
                .navigation(activity, code)
    }

    fun navigation(activity: Activity, path: String, flags: Int) {
        ARouter.getInstance()
                .build(path)
                .withFlags(flags)
                .navigation(activity)
    }

    fun navigation(activity: Activity, path: String, bundle: Bundle, flags: Int) {
        ARouter.getInstance()
                .build(path)
                .with(bundle)
                .withFlags(flags)
                .navigation(activity)
    }
}