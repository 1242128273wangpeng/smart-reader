package com.dy.media

import android.app.Activity
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
class MediaLifecycle : IMediaLifecycle {

    override fun onAppCreate(application: Application) {}

    override fun onTerminate() {}

    override fun onCreate(activity: Activity, savedInstanceState: Bundle) {}

    override fun onStart() {}

    override fun onRestart() {}

    override fun onResume() {}

    override fun onPause() {}

    override fun onStop() {}

    override fun onDestroy() {}

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {}

    override fun onConfigurationChanged(newConfig: Configuration) {}

    override fun onNewIntent(intent: Intent) {}

    override fun onSaveInstanceState(outState: Bundle) {}

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {}
}