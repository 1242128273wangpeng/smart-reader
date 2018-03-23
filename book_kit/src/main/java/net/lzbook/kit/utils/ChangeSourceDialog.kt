package net.lzbook.kit.utils

import android.app.Activity
import android.view.View
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.dialog_change_source.*
import net.lzbook.kit.R
import net.lzbook.kit.book.view.MyDialog

/**
 * Created by qiantao on 2017/11/17 0017
 */
class ChangeSourceDialog(private val activity: Activity) {

    private val dialog = MyDialog(activity, R.layout.dialog_change_source)

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
        }
        dialog.btn_cancel.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun setOnConfirmListener(listener: () -> Unit) {
        confirmListener = listener
    }

    fun show() {
        dialog.txt_dialog_title.text = activity.getString(R.string.prompt)
        dialog.txt_dialog_information.visibility = View.VISIBLE
        dialog.view_divider.visibility = View.VISIBLE
        dialog.ll_btn.visibility = View.VISIBLE
        dialog.pgbar_loading.visibility = View.GONE
        dialog.show()
    }

    fun showLoading() {
        dialog.txt_dialog_title.text = activity.getString(R.string.tip_cleaning_cache)
        dialog.txt_dialog_information.visibility = View.INVISIBLE
        dialog.view_divider.visibility = View.INVISIBLE
        dialog.ll_btn.visibility = View.INVISIBLE
        dialog.pgbar_loading.visibility = View.VISIBLE
    }

    fun dismiss() {
        dialog.dismiss()
    }

}