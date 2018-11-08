package net.lzbook.kit.utils.upush

import android.content.Context
import com.dingyue.statistics.DyStatService
import com.umeng.message.UmengNotificationClickHandler
import com.umeng.message.entity.UMessage
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.pointpage.EventPoint
import net.lzbook.kit.utils.loge
import net.lzbook.kit.utils.openPushActivity
import net.lzbook.kit.utils.parsePushClickLog

/**
 * Desc 友盟消息推送 通知处理
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/5/30 11:51
 */
class PushNotificationHandler : UmengNotificationClickHandler() {

    //打开 App
    override fun launchApp(context: Context?, msg: UMessage?) {
        super.launchApp(context, msg)
        val data = msg.parsePushClickLog()
        DyStatService.onEvent(EventPoint.SYSTEM_PUSHCLICK, mapOf("type" to "1"))// 1表示点击
    }

    //忽略通知
    override fun dismissNotification(context: Context?, msg: UMessage?) {
        super.dismissNotification(context, msg)
        DyStatService.onEvent(EventPoint.SYSTEM_PUSHCLICK, mapOf("type" to "2"))// 2表示取消
    }

    override fun openActivity(context: Context?, msg: UMessage?) {
        loge("msg.extra: ${msg?.extra}")
        val data = msg.parsePushClickLog()
        DyStatService.onEvent(EventPoint.SYSTEM_PUSHCLICK, mapOf("type" to "1"))// 1表示点击

        if (msg?.activity?.trim()?.isNotEmpty() == true) {
            context?.openPushActivity(msg)
        }
    }

    override fun openUrl(context: Context?, msg: UMessage?) {
        super.openUrl(context, msg)
        val data = msg.parsePushClickLog()
        DyStatService.onEvent(EventPoint.SYSTEM_PUSHCLICK, mapOf("type" to "1"))// 1表示点击
    }
}