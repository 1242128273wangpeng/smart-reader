package com.intelligent.reader.view

import android.app.Activity
import android.view.Gravity
import android.widget.CheckBox
import android.widget.TextView
import com.intelligent.reader.R
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.data.bean.Book

/**
 * Created by qiantao on 2017/11/22 0022
 */
class DownloadDeleteDialog(activity: Activity) {
    val dialog = MyDialog(activity, R.layout.layout_addshelf_dialog)
    val title = dialog.findViewById(R.id.dialog_title) as TextView
    val info = dialog.findViewById(R.id.tv_update_info_dialog) as TextView
    val hint = dialog.findViewById(R.id.cb_hint) as CheckBox
    val cancel = dialog.findViewById(R.id.bt_cancel) as TextView
    val confirm = dialog.findViewById(R.id.bt_ok) as TextView

    private var checkListener: (() -> Unit)? = null
    private var confirmListener: ((books: ArrayList<Book>?, isDeleteBookOfShelf: Boolean) -> Unit)? = null
    private val cancelListener: (() -> Unit)? = null

    private var books: ArrayList<Book>? = null

    init {
        title.setText(R.string.prompt)
        info.text = "你确定要删除缓存吗？"
        info.gravity = Gravity.CENTER
        hint.gravity = Gravity.CENTER
        hint.text = "同时从书架中删除"
        cancel.setText(R.string.cancel)
        confirm.setText(R.string.confirm)
        hint.setOnClickListener {
            checkListener?.invoke()
        }
        cancel.setOnClickListener {
            dialog.dismiss()
            cancelListener?.invoke()
        }
        confirm.setOnClickListener {
            dialog.dismiss()
            confirmListener?.invoke(books, hint.isChecked)
        }
    }

    fun show(books: ArrayList<Book>) {
        this.books = books
        dialog.show()
        hint.isChecked = false
    }

    fun setCheckListener(listener: () -> Unit) {
        checkListener = listener
    }

    fun setConfirmListener(listener: (books: ArrayList<Book>?, isDeleteBookOfShelf: Boolean) -> Unit) {
        confirmListener = listener
    }

    fun setCancelListener(listener: () -> Unit) {
        checkListener = listener
    }
}