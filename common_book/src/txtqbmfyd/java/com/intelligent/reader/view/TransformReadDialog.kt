package com.intelligent.reader.view

import android.app.Activity
import android.view.Gravity
import android.widget.FrameLayout
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.intelligent.reader.R
import kotlinx.android.synthetic.txtqbmfyd.dialog_transform_read.*
import net.lzbook.kit.ui.widget.MyDialog

/**
 * Desc 确认弹出框
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/1 0002 15:09
 */
class TransformReadDialog(val activity: Activity) {
    private val dialog = MyDialog(activity, R.layout.dialog_transform_read, Gravity.BOTTOM)

    private var cancelListener: (() -> Unit)? = null
    private var continueListener: (() -> Unit)? = null

    init {

        val window = dialog.window
        val layoutParams = window.attributes

        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
        layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT

        window.attributes = layoutParams

        window.setWindowAnimations(R.style.BottomPopupDialog)

        dialog.rootLayout.viewTreeObserver.addOnGlobalLayoutListener {
            dialog.nightShadowView.layoutParams.height = dialog.rootLayout.height
            dialog.rootLayout.requestLayout()
        }

        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        dialog.txt_transform_read_continue.setOnClickListener {
            SPUtils.editDefaultShared {
                val isChecked = dialog.ckb_not_show_next_time.isChecked
                putBoolean(SPKey.NOT_SHOW_NEXT_TIME, isChecked)
            }
            continueListener?.invoke()
        }

        dialog.ll_not_show_next_time.setOnClickListener {
            val oldChecked = dialog.ckb_not_show_next_time.isChecked
            dialog.ckb_not_show_next_time.isChecked = !oldChecked
        }

        dialog.txt_transform_read_cancel.setOnClickListener {
            dialog.dismiss()
            cancelListener?.invoke()
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