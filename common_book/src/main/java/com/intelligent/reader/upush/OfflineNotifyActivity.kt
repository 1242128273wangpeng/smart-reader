package com.intelligent.reader.upush

import android.content.Intent
import com.intelligent.reader.activity.SplashActivity
import com.umeng.message.UmengNotifyClickActivity
import net.lzbook.kit.utils.loge
import org.android.agoo.common.AgooConstants
import swipeback.ActivityLifecycleHelper


/**
 * Desc 小米、华为系统通道 离线消息
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/5/28 09:25
 */
class OfflineNotifyActivity : UmengNotifyClickActivity() {

    override fun onMessage(intent: Intent?) {
        super.onMessage(intent)
        val body = intent?.getStringExtra(AgooConstants.MESSAGE_BODY)
        loge("message body: $body")
        val activities = ActivityLifecycleHelper.getActivities()
        if (activities == null || activities.isEmpty()) {
            startActivity(Intent(this, SplashActivity::class.java))
        }
        finish()
    }

}