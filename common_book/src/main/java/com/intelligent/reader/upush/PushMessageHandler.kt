package com.intelligent.reader.upush

import android.content.Context
import com.dingyue.contract.util.CommonUtil
import com.umeng.message.UmengMessageHandler
import com.umeng.message.entity.UMessage
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.dynamic.DynamicParameter
import net.lzbook.kit.utils.loge
import net.lzbook.kit.utils.runOnMain

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

    //注意：umeng后台需要发送自定义消息类型
    override fun dealWithCustomMessage(context: Context?, msg: UMessage?) {
        super.dealWithCustomMessage(context, msg)
        runOnMain {
            CommonUtil.showToastMessage(msg.toString())
            DynamicParameter(BaseBookApplication.getGlobalContext()).requestCheck()
        }

    }
}