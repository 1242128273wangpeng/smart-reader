package com.intelligent.reader.view

import android.app.Activity
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.support.v4.app.NotificationManagerCompat
import android.view.Gravity
import com.intelligent.reader.R
import kotlinx.android.synthetic.txtqbdzs.dialog_push_setting.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.view.MyDialog

/**
 * Desc 推送时间设置
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/7/18 0002 14:09
 */
class PushSettingDialog(val activity: Activity) : LifecycleObserver {

    private val dialog = MyDialog(activity, R.layout.dialog_push_setting, Gravity.CENTER)

    var openPushListener: (() -> Unit)? = null

    init {

        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        dialog.img_close.setOnClickListener {
            dialog.dismiss()
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.PAGE_SHELF,
                    StartLogClickUtil.POPUPCLOSE)
        }

        dialog.txt_open_push.setOnClickListener {
            openPushListener?.invoke()
        }

    }

    fun show() {
        dialog.show()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun dismiss() {
        val isNotifyEnable = NotificationManagerCompat.from(activity)
                .areNotificationsEnabled()
        if (isNotifyEnable && dialog.isShowing) {
            dialog.dismiss()
        }
    }
}