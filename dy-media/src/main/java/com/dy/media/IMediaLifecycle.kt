package com.dy.media

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle

/**
 * Desc 请描述这个文件
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/6/7 11:36
 */
interface IMediaLifecycle {

    fun onAppCreate(application: Application)

    fun onTerminate()

    fun onCreate(activity: Activity, savedInstanceState: Bundle)

    fun onStart()

    fun onRestart()

    fun onResume()

    fun onPause()

    fun onStop()

    fun onDestroy()

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent)

    fun onConfigurationChanged(newConfig: Configuration)

    fun onNewIntent(intent: Intent)

    fun onSaveInstanceState(outState: Bundle)

    fun onRestoreInstanceState(savedInstanceState: Bundle)

}