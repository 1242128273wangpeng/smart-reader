package com.intelligent.reader.view

import android.app.Activity
import android.view.Gravity
import com.intelligent.reader.R
import kotlinx.android.synthetic.txtqbmfxs.dialog_push_time.*
import net.lzbook.kit.bean.SettingItems
import net.lzbook.kit.widget.MyDialog


/**
 * Desc 推送时间设置
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/7/18 0002 14:09
 */
class PushTimeDialog(val activity: Activity) {

    private val dialog = MyDialog(activity, R.layout.dialog_push_time, Gravity.CENTER)

    private var confirmListener: ((startHour: Int, startMinute: Int,
                                   stopHour: Int, stopMinute: Int) -> Unit)? = null

    init {

        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        dialog.txt_confirm.setOnClickListener {
            val startHour = dialog.time_picker.currentStartHour
            val startMinute = dialog.time_picker.currentStartMinute
            val stopHour = dialog.time_picker.currentStopHour
            val stopMinute = dialog.time_picker.currentStopMinute
            confirmListener?.invoke(startHour, startMinute, stopHour, stopMinute)
            dialog.dismiss()
        }
        dialog.txt_cancel.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun setOnConfirmListener(listener: (startHour: Int, startMinute: Int,
                                        stopHour: Int, stopMinute: Int) -> Unit) {
        confirmListener = listener
    }

    fun show(settingItems: SettingItems) {
        dialog.time_picker.currentStartHour = settingItems.pushTimeStartH
        dialog.time_picker.currentStartMinute = settingItems.pushTimeStartMin
        dialog.time_picker.currentStopHour = settingItems.pushTimeStopH
        dialog.time_picker.currentStopMinute = settingItems.pushTimeStopMin
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }
}