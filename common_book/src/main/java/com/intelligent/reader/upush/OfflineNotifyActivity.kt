package com.intelligent.reader.upush

import android.content.Intent
import android.text.TextUtils
import com.google.gson.Gson
import com.intelligent.reader.activity.SplashActivity
import com.umeng.message.UmengNotifyClickActivity
import com.umeng.message.entity.UMessage
import net.lzbook.kit.utils.loge
import org.android.agoo.common.AgooConstants
import org.json.JSONObject
import swipeback.ActivityLifecycleHelper
import java.util.Map


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

        //自定义参数解析
        val msg = UMessage(JSONObject(body))
        if (msg.activity?.trim()?.isNotEmpty() == true) {
            val openActivity = Intent()
            openActivity.putExtra(msg)
            loge("umsg.activity: ${msg.activity}")
            openActivity.setClassName(this, msg.activity)
            openActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(openActivity)
        } else {
            loge("umsg.activity 为空")
            val activities = ActivityLifecycleHelper.getActivities()
            if (activities == null || activities.isEmpty()) {
                startActivity(Intent(this, SplashActivity::class.java))
            }
        }
        finish()
    }


    private fun Intent.putExtra(msg: UMessage) {
        if (msg.extra != null) {
            val it = msg.extra.entries.iterator()

            while (it.hasNext()) {
                val entry = it.next() as MutableMap.MutableEntry<*, *>
                val key = entry.key as String
                val value = entry.value as String
                putExtra(key, value)
            }
            putExtra(IS_FROM_OFFLINE, true)
        }
    }

    companion object {
        @JvmField
        val IS_FROM_OFFLINE = "is_from_offline"
    }

}