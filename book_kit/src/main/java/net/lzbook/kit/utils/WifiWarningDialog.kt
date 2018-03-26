package net.lzbook.kit.utils

import android.app.Activity
import android.view.Gravity
import kotlinx.android.synthetic.main.dialog_wifi_warning.*
import net.lzbook.kit.R
import net.lzbook.kit.book.view.MyDialog

/**
 * Created by qiantao on 2017/11/17 0017
 */
class WifiWarningDialog( activity: Activity) {

    private val dialog = MyDialog(activity, R.layout.dialog_wifi_warning, Gravity.BOTTOM)

    private var confirmListener: (() -> Unit)? = null

    init {

        val window = dialog.window
        window.setWindowAnimations(R.style.BottomPopupDialog)

        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        dialog.btn_confirm.setOnClickListener {
            confirmListener?.invoke()
            dialog.dismiss()
        }
        dialog.btn_cancel.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun setOnConfirmListener(listener: () -> Unit) {
        confirmListener = listener
    }

    fun show() {
        dialog.show()
    }

}