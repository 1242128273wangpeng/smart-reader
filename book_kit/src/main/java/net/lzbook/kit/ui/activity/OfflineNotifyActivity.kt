package net.lzbook.kit.ui.activity

import android.content.Intent
import com.umeng.message.UmengNotifyClickActivity
import com.umeng.message.entity.UMessage
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.utils.loge
import net.lzbook.kit.utils.openPushActivity
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.swipeback.ActivityLifecycleHelper
import org.android.agoo.common.AgooConstants
import org.json.JSONObject


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
            val data = mapOf(Pair("type", "1"))
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SYSTEM_PAGE,
                    StartLogClickUtil.PUSHCLICK, data)

            openPushActivity(msg)
        } else {
            loge("umsg.activity 为空")

            val data = mapOf(Pair("type", "1"))
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SYSTEM_PAGE,
                    StartLogClickUtil.PUSHCLICK, data)

            val activities = ActivityLifecycleHelper.getActivities()
            if (activities == null || activities.isEmpty()) {
//                startActivity(Intent(this, SplashActivity::class.java))
                RouterUtil.navigation(this, RouterConfig.SPLASH_ACTIVITY)
            }
        }
        finish()
    }

}