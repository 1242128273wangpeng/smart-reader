package com.intelligent.reader.upush

import android.content.Context
import com.umeng.message.UmengMessageHandler
import com.umeng.message.entity.UMessage
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.utils.loge

/**
 * Desc 友盟推送消息处理
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/5/28 16:50
 */
class PushMessageHandler : UmengMessageHandler() {

    override fun dealWithNotificationMessage(context: Context?, msg: UMessage?) {
        super.dealWithNotificationMessage(context, msg)
        loge("msg: ${msg?.text}")
        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.SYSTEM_PAGE,
                StartLogClickUtil.PUSHRECEIVE)
    }
}