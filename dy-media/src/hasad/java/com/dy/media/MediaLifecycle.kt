package com.dy.media

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import com.dycm_adsdk.PlatformSDK

/**
 * Desc 广告生命周期
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/6/7 11:39
 */
object MediaLifecycle : IMediaLifecycle {

    override fun onAppCreate(application: Application) {
        PlatformSDK.app().onAppCreate(application)
    }

    override fun onTerminate() {
        PlatformSDK.app().onTerminate()
    }

    override fun onCreate(activity: Activity, savedInstanceState: Bundle) {
        PlatformSDK.lifecycle()?.onCreate(activity, savedInstanceState)
    }

    override fun onStart() {
        PlatformSDK.lifecycle()?.onStart()
    }

    override fun onRestart() {
        PlatformSDK.lifecycle()?.onRestart()
    }

    override fun onResume() {
        PlatformSDK.lifecycle()?.onResume()
    }

    override fun onPause() {
        PlatformSDK.lifecycle()?.onPause()
    }

    override fun onStop() {
        PlatformSDK.lifecycle()?.onStop()
    }

    override fun onDestroy() {
        PlatformSDK.lifecycle()?.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        PlatformSDK.lifecycle()?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        PlatformSDK.lifecycle()?.onConfigurationChanged(newConfig)
    }

    override fun onNewIntent(intent: Intent) {
        PlatformSDK.lifecycle()?.onNewIntent(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        PlatformSDK.lifecycle()?.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        PlatformSDK.lifecycle()?.onRestoreInstanceState(savedInstanceState)
    }
}