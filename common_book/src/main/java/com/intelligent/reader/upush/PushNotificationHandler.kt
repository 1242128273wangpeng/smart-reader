package com.intelligent.reader.upush

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.dingyue.contract.router.BookRouter
import com.intelligent.reader.activity.CoverPageActivity
import com.intelligent.reader.activity.FindBookDetail
import com.intelligent.reader.activity.SettingActivity
import com.umeng.message.UmengNotificationClickHandler
import com.umeng.message.entity.UMessage
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.utils.loge

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
        super.openActivity(context, msg)
        loge("msg.extra: ${msg?.extra}")
    }

    companion object {
        const val ACTION_KEY = "action_key"
        const val ACTION_COVER = "action_cover"
        const val ACTION_H5 = "action_h5"
        const val ACTION_SETTING = "action_setting"

        const val BOOK_AUTHOR = "author"
        const val BOOK_ID = "book_id"
        const val BOOK_SOURCE_ID = "book_source_id"
        const val BOOK_CHAPTER_ID = "book_chapter_id"


        const val H5_TITLE = "title"
        const val H5_URL = "url"
    }

}