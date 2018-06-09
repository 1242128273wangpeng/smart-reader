package com.dy.reader.dialog

import android.app.Activity
import android.view.Gravity
import com.dy.reader.R
import kotlinx.android.synthetic.qbzsydq.dialog_reader_auto_read.*
import net.lzbook.kit.book.view.MyDialog

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/6/9 14:24
 */
class ReaderAutoReadDialog (activity: Activity) {

    private val dialog = MyDialog(activity, R.layout.dialog_reader_auto_read, Gravity.BOTTOM)

    var cancelListener: (() -> Unit)? = null
    var confirmListener: (() -> Unit)? = null
    var receiverPromptListener: ((checked: Boolean) -> Unit)? = null

    init {

        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        dialog.btn_cancel.setOnClickListener {
            cancelListener?.invoke()
        }

        dialog.btn_confirm.setOnClickListener {
            confirmListener?.invoke()
        }

        dialog.ckb_reader_auto_read.setOnCheckedChangeListener { _, isChecked ->
            receiverPromptListener?.invoke(isChecked)
        }
    }

    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }
}