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
     * 构建标准的路由请求
     */
    fun navigation(activity: Activity, path: String) {
        ARouter.getInstance()
                .build(path)
                .navigation(activity)
    }
    /**
     * 构建携带动画的路由请求
     */

    fun navigationWithTransition(activity: Activity,path: String,enterAnim:Int=
    android.R.anim.slide_in_left,exitAnim:Int=android.R.anim.slide_out_right){
        ARouter.getInstance()
                .build(path)
                .withTransition(enterAnim,exitAnim)
                .navigation(activity)
    }

    /**
     * 构建标准的路由请求
     */
    @JvmStatic
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