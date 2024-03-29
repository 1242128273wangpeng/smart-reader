package net.lzbook.kit.utils.upush

import android.content.Context
import com.dingyue.statistics.DyStatService
import com.umeng.message.UmengMessageHandler
import com.umeng.message.entity.UMessage
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.pointpage.EventPoint
import net.lzbook.kit.utils.dynamic.DynamicParameter
import net.lzbook.kit.utils.loge
import net.lzbook.kit.utils.uiThread

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
        DyStatService.onEvent(EventPoint.SYSTEM_PUSHRECEIVE)
    }

    //注意：umeng后台需要发送自定义消息类型
    override fun dealWithCustomMessage(context: Context?, msg: UMessage?) {
        super.dealWithCustomMessage(context, msg)
        uiThread {
            if (msg?.extra?.get("IsDynamicCheck")?.isNotEmpty() == true) {
                DynamicParameter(BaseBookApplication.getGlobalContext()).requestCheck()
            }

        }

    }
}