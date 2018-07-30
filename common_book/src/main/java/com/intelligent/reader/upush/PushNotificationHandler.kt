package com.intelligent.reader.upush

import android.content.Context
import com.umeng.message.UmengNotificationClickHandler
import com.umeng.message.entity.UMessage
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.utils.loge
import net.lzbook.kit.utils.openPushActivity

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
        val data = mapOf(Pair("type", "1"))
        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.SYSTEM_PAGE,
                StartLogClickUtil.PUSHCLICK, data)

    }

    //忽略通知
    override fun dismissNotification(context: Context?, msg: UMessage?) {
        super.dismissNotification(context, msg)
        val data = mapOf(Pair("type", "2"))
        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.SYSTEM_PAGE,
                StartLogClickUtil.PUSHCLICK, data)
    }

    override fun openActivity(context: Context?, msg: UMessage?) {
        loge("msg.extra: ${msg?.extra}")
        if (msg?.activity?.trim()?.isNotEmpty() == true) {
            context?.openPushActivity(msg)
        }
    }

}