package com.intelligent.reader.upush

import android.content.Context
import com.umeng.message.UmengNotificationClickHandler
import com.umeng.message.entity.UMessage
import net.lzbook.kit.appender_loghub.StartLogClickUtil

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

}