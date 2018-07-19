package com.intelligent.reader.upush

import android.content.Context
import com.dingyue.contract.util.SharedPreUtil
import com.umeng.message.IUmengRegisterCallback
import com.umeng.message.PushAgent
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.OpenUDID
import net.lzbook.kit.utils.loge
import net.lzbook.kit.utils.updateTags

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
        loge("deviceToken: " + deviceToken)


        //设置别名
        loge("udid: " + udid)
        pushAgent.setAlias(udid, "UDID", { isSuccess, message ->
            loge("setAlias：$isSuccess  message: $message")
        })

        //更新标签
        val share = SharedPreUtil(SharedPreUtil.SHARE_DEFAULT)
        val latestUpdateTime = share.getLong(SharedPreUtil.PUSH_TAG_LATEST_UPDATE_TIME, 0)
        val currentTime = System.currentTimeMillis()
        val isSameDay = AppUtils.isToday(latestUpdateTime, currentTime)
        if (!isSameDay) {
            pushAgent.updateTags(context, udid) { isSuccess ->
                if (isSuccess) {
                    share.putLong(SharedPreUtil.PUSH_TAG_LATEST_UPDATE_TIME, currentTime)
                }
            }
        }

    }

    override fun onFailure(p0: String?, p1: String?) {

    }

}