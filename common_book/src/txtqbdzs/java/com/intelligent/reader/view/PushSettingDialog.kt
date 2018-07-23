package com.intelligent.reader.view

import android.app.Activity
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
class PushSettingDialog(val activity: Activity) {

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

    fun dismiss() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }
}