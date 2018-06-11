package com.dy.reader.dialog

import android.app.Activity
import android.view.Gravity
import android.widget.FrameLayout
import com.dy.reader.R
import kotlinx.android.synthetic.txtqbmfyd.dialog_reader_add_shelf.*
import net.lzbook.kit.book.view.MyDialog

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/6/9 14:24
 */
class ReaderAddShelfDialog (activity: Activity) {

    private val dialog = MyDialog(activity, R.layout.dialog_reader_add_shelf, Gravity.BOTTOM)

    var cancelListener: (() -> Unit)? = null
    var confirmListener: (() -> Unit)? = null

    init {

        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        val window = dialog.window
        val layoutParams = window.attributes

        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
        layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT

        window.attributes = layoutParams
        window.setWindowAnimations(R.style.bottom_popup_dialog)

        dialog.fl_add_shelf_content.viewTreeObserver.addOnGlobalLayoutListener {
            dialog.nsv_add_shelf.layoutParams.height = dialog.fl_add_shelf_content.height
            dialog.fl_add_shelf_content.requestLayout()
        }

        dialog.btn_cancel.setOnClickListener {
            cancelListener?.invoke()
        }

        dialog.btn_confirm.setOnClickListener {
            confirmListener?.invoke()
        }
    }

    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }
}