package net.lzbook.kit.ui.widget

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_login_invalid.*
import net.lzbook.kit.R

/**
 * Desc 登录失效弹窗
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/4/16 0016 11:32
 */
class LoginInvalidDialog(context: Context) {

    private val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_login_invalid, null)

    private val dialog = Dialog(context, net.lzbook.kit.R.style.update_dialog)

    private var confirmListener: (() -> Unit)? = null

    private var cancelListener: (() -> Unit)? = null

    init {
        dialog.setContentView(contentView)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        val window = dialog.window
        val params = window.attributes
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        window.attributes = params
        dialog.btn_confirm.setOnClickListener {
            confirmListener?.invoke()
            dialog.dismiss()
        }
        dialog.btn_cancel.setOnClickListener {
            cancelListener?.invoke()
            dialog.dismiss()
        }
    }

    fun setOnConfirmListener(confirmListener: () -> Unit) {
        this.confirmListener = confirmListener
    }

    fun setOnCancelListener(cancelListener: () -> Unit) {
        this.cancelListener = cancelListener
    }

    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }

    fun isShowing() = dialog.isShowing

}