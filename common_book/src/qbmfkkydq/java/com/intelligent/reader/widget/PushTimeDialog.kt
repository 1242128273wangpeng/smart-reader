package com.intelligent.reader.widget

import android.app.Activity
import android.view.Gravity
import android.view.ViewGroup
import com.intelligent.reader.R
import kotlinx.android.synthetic.qbmfkkydq.dialog_push_time.*
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.data.bean.SettingItems

/**
 * Desc 推送时间设置
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/2 0002 14:09
 */
class PushTimeDialog(val activity: Activity) {

    private val dialog = MyDialog(activity, R.layout.dialog_push_time, Gravity.CENTER)

    private var confirmListener: ((startHour: Int, startMinute: Int,
                                   stopHour: Int, stopMinute: Int) -> Unit)? = null

    init {

        val window = dialog.window

        val params = window.attributes
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
//        window.setWindowAnimations(R.style.BottomPopupDialog)

        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        dialog.btn_confirm.setOnClickListener {
            val startHour = dialog.np_start_hour.value
            val startMinute = dialog.np_start_minute.value
            val stopHour = dialog.np_stop_hour.value
            val stopMinute = dialog.np_stop_minute.value
            confirmListener?.invoke(startHour, startMinute, stopHour, stopMinute)
            dialog.dismiss()
        }
        dialog.btn_cancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.np_start_hour.setOnValueChangedListener { picker, oldVal, newVal ->
            resetStopTime()
        }

        dialog.np_start_minute.setOnValueChangedListener { picker, oldVal, newVal ->
            if (oldVal == 59 && newVal == 0) {
                if (dialog.np_start_hour.value == 23) {
                    dialog.np_start_hour.value = 0
                } else {
                    dialog.np_start_hour.value = dialog.np_start_hour.value + 1
                }
            }
            resetStopTime()
        }

        dialog.np_stop_hour.setOnValueChangedListener { picker, oldVal, newVal ->
            resetStartTime()
        }

        dialog.np_stop_minute.setOnValueChangedListener { picker, oldVal, newVal ->
            if (oldVal == 59 && newVal == 0) {
                if (dialog.np_stop_hour.value == 23) {
                    dialog.np_stop_hour.value = 0
                } else {
                    dialog.np_stop_hour.value = dialog.np_stop_hour.value + 1
                }
            }
            resetStartTime()
        }
    }

    private fun resetStopTime() {
        val startTotalMinute = dialog.np_start_hour.value * 60 + dialog.np_start_minute.value
        val stopTotalMinute = dialog.np_stop_hour.value * 60 + dialog.np_stop_minute.value
        if (startTotalMinute > stopTotalMinute) {
            dialog.np_stop_hour.value = dialog.np_start_hour.value
            dialog.np_stop_minute.value = dialog.np_start_minute.value
        }
    }

    private fun resetStartTime() {
        val startTotalMinute = dialog.np_start_hour.value * 60 + dialog.np_start_minute.value
        val stopTotalMinute = dialog.np_stop_hour.value * 60 + dialog.np_stop_minute.value
        if (stopTotalMinute < startTotalMinute) {
            dialog.np_start_hour.value = dialog.np_stop_hour.value
            dialog.np_start_minute.value = dialog.np_stop_minute.value
        }
    }

    fun setOnConfirmListener(listener: (startHour: Int, startMinute: Int,
                                        stopHour: Int, stopMinute: Int) -> Unit) {
        confirmListener = listener
    }

    fun show(settingItems: SettingItems) {
        dialog.np_start_hour.value = settingItems.pushTimeStartH
        dialog.np_start_minute.value = settingItems.pushTimeStartMin
        dialog.np_stop_hour.value = settingItems.pushTimeStopH
        dialog.np_stop_minute.value = settingItems.pushTimeStopMin
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }
}