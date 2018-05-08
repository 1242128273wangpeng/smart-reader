package net.lzbook.kit.router

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
    fun navigation(path: String) {
        ARouter.getInstance().build(path).navigation()
    }

    /**
     * 构建标准的路由请求
     */
    fun navigation(path: String, bundle: Bundle) {
        ARouter.getInstance().build(path).with(bundle).navigation()
    }

    fun navigation(path: String, flags: Int) {
        ARouter.getInstance()
                .build(path)
                .withFlags(flags)
                .navigation()
    }

    fun navigation(path: String, bundle: Bundle, flags: Int) {
        ARouter.getInstance()
                .build(path)
                .with(bundle)
                .withFlags(flags)
                .navigation()
    }
}