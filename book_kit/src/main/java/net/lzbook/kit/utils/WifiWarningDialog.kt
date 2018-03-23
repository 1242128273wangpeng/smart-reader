package net.lzbook.kit.utils

import android.app.Activity
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.dialog_wifi_warning.*
import net.lzbook.kit.R
import net.lzbook.kit.book.view.MyDialog

/**
 * Created by qiantao on 2017/11/17 0017
 */
class WifiWarningDialog( activity: Activity) {

    private val dialog = MyDialog(activity, R.layout.dialog_wifi_warning)

    private var confirmListener: (() -> Unit)? = null

    init {

        val window = dialog.window
        val layoutParams = window.attributes
        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
        layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT
        window.attributes = layoutParams
        window.setWindowAnimations(R.style.BottomPopupDialog)

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