package net.lzbook.kit.ui.widget

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.dialog_confirm_layout.*
import net.lzbook.kit.R

/**
 * Desc 确认弹出框
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/1 0002 15:09
 */
class ConfirmDialog(val activity: Activity, val gravity: Int = Gravity.BOTTOM) {

    private val dialog = MyDialog(activity, R.layout.dialog_confirm_layout, gravity)

    private var confirmListener: (() -> Unit)? = null
    private var cancelListener: (() -> Unit)? = null

    init {

        val window = dialog.window
        val layoutParams = window.attributes
        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
        layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT
        window.attributes = layoutParams

        if (gravity == Gravity.BOTTOM) {
            window.setWindowAnimations(R.style.BottomPopupDialog)
        }
        dialog.rootLayout.viewTreeObserver.addOnGlobalLayoutListener {
            dialog.nightShadowView.layoutParams.height = dialog.rootLayout.height
            dialog.rootLayout.requestLayout()
        }
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        dialog.btn_confirm.setOnClickListener {
            confirmListener?.invoke()
        }
        dialog.btn_cancel.setOnClickListener {
            dialog.dismiss()
            cancelListener?.invoke()
        }
    }

    fun setTitle(title: String) {
        dialog.txt_dialog_title.text = title
    }

    fun setContent(content: String) {
        dialog.txt_dialog_information.text = content
    }

    fun setConfirmName(name: String) {
        dialog.btn_confirm.text = name
    }

    fun setOnCancelName(name: String) {
        dialog.btn_cancel.text = name
    }

    fun setOnConfirmListener(listener: () -> Unit) {
        confirmListener = listener
    }

    fun setOnCancelListener(listener: () -> Unit) {
        cancelListener = listener
    }

    fun show() {
//        dialog.txt_dialog_title.text = activity.getString(R.string.prompt)
        dialog.txt_dialog_information.visibility = View.VISIBLE
        dialog.view_divider.visibility = View.VISIBLE
        dialog.ll_btn.visibility = View.VISIBLE
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }

}