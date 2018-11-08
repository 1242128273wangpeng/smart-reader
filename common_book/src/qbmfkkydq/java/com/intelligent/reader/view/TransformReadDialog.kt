package com.intelligent.reader.view

import android.app.Activity
import android.view.Gravity
import android.widget.FrameLayout
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.intelligent.reader.R
import kotlinx.android.synthetic.qbmfkkydq.dialog_transform_read.*
import net.lzbook.kit.ui.widget.MyDialog

/**
 * Function：书籍封面转码阅读提示框
 *
 * Created by JoannChen on 2018/6/16 0016 10:24
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
class TransformReadDialog(val activity: Activity) : MyDialog(activity, R.layout.dialog_transform_read, Gravity.CENTER) {

    private var cancelListener: (() -> Unit)? = null
    private var continueListener: (() -> Unit)? = null

    init {

        val layoutParams = window.attributes

        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
        layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT

        window.attributes = layoutParams

        setCanceledOnTouchOutside(true)
        setCancelable(true)

        txt_transform_read_continue.setOnClickListener {
            val isChecked = ckb_not_show_next_time.isChecked
            SPUtils.putDefaultSharedBoolean(SPKey.NOT_SHOW_NEXT_TIME, isChecked)
            continueListener?.invoke()
        }

        txt_transform_read_cancel.setOnClickListener {
            dismiss()
            cancelListener?.invoke()
        }

        ll_not_show_next_time.setOnClickListener {
            val oldChecked = ckb_not_show_next_time.isChecked
            ckb_not_show_next_time.isChecked = !oldChecked
        }

        ckb_not_show_next_time.isChecked = true
    }

    fun insertCancelListener(listener: () -> Unit) {
        cancelListener = listener
    }

    fun insertContinueListener(listener: () -> Unit) {
        continueListener = listener
    }

}