package com.intelligent.reader.upush

import android.content.Context
import com.umeng.message.IUmengRegisterCallback
import com.umeng.message.PushAgent
import net.lzbook.kit.utils.EVENT_UPDATE_TAG
import net.lzbook.kit.utils.OpenUDID
import net.lzbook.kit.utils.loge
import org.greenrobot.eventbus.EventBus

/**
 * Desc 友盟消息推送注册回调
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/7/16 13:55
 */
class PushRegisterCallback(private val context: Context)
    : IUmengRegisterCallback {


    override fun onSuccess(deviceToken: String?) {
        val udid = OpenUDID.getOpenUDIDInContext(context)
        val pushAgent = PushAgent.getInstance(context)

        //注册成功会返回device token
        loge("deviceToken: $deviceToken")


        //设置别名
        loge("udid: $udid")
        pushAgent.setAlias(udid, "UDID", { isSuccess, message ->
            loge("setAlias：$isSuccess  message: $message")
        })

        //更新标签
        EventBus.getDefault().postSticky(EVENT_UPDATE_TAG)

    }

    override fun onFailure(p0: String?, p1: String?) {
        loge("deviceToken", "$p0  $p1")
    }

}