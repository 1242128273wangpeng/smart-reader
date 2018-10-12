package com.intelligent.reader.view

import android.app.Activity
import android.view.Gravity
import android.widget.FrameLayout
import com.ding.basic.util.editShared
import com.ding.basic.util.getSharedBoolean
import com.dingyue.contract.util.SharedPreUtil
import com.intelligent.reader.R
import kotlinx.android.synthetic.main.dialog_read_source.*
import net.lzbook.kit.book.view.MyDialog

/**
 * Function：书籍封面转码阅读提示框
 *
 * Created by JoannChen on 2018/6/16 0016 10:24
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
class TransformReadDialog(val activity: Activity) {
    private val dialog = MyDialog(activity, R.layout.dialog_read_source, Gravity.CENTER)

    private var cancelListener: (() -> Unit)? = null
    private var continueListener: (() -> Unit)? = null

    init {

        val window = dialog.window
        val layoutParams = window.attributes

        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
        layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT

        window.attributes = layoutParams

        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        dialog.txt_transform_read_continue.setOnClickListener {
            activity.editShared {
                val isChecked = dialog.ckb_not_show_next_time.isChecked
                putBoolean(SharedPreUtil.NOT_SHOW_NEXT_TIME, isChecked)
            }
            continueListener?.invoke()
        }
        dialog.txt_transform_read_cancel.setOnClickListener {
            dialog.dismiss()
            cancelListener?.invoke()
        }
        dialog.ll_not_show_next_time.setOnClickListener {
            val oldChecked = dialog.ckb_not_show_next_time.isChecked
            dialog.ckb_not_show_next_time.isChecked = !oldChecked
        }
    }

    fun insertCancelListener(listener: () -> Unit) {
        cancelListener = listener
    }

    fun insertContinueListener(listener: () -> Unit) {
        continueListener = listener
    }

    fun show() {
        dialog.ckb_not_show_next_time.isChecked = false
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }
    fun isShow(): Boolean{
        return dialog.isShowing
    }
}