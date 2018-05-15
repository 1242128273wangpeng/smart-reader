package com.intelligent.reader.view

import android.app.Activity
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import com.dingyue.bookshelf.R
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.data.bean.Book

/**
 * Desc 点击底部删除 弹出dialog
 * Author zhenxiang
 * 2018\5\15 0015
 */
class BookDeleteDialog(activity: Activity) {

    private val dialog = MyDialog(activity, R.layout.layout_addshelf_dialog)

    private val title = dialog.findViewById(R.id.dialog_title) as TextView
    private val content = dialog.findViewById(R.id.tv_update_info_dialog) as TextView
    private val checkBox = dialog.findViewById(R.id.cb_hint) as CheckBox
    private val confirm = dialog.findViewById(R.id.bt_ok) as Button
    private val abrogate = dialog.findViewById(R.id.bt_cancel) as Button

    var onConfirmListener: ((books: ArrayList<Book>, isDeleteCacheOnly: Boolean) -> Unit)? = null
    var onCancelListener: (() -> Unit)? = null

    private var books: ArrayList<Book> = ArrayList()

    init {
        title.setText(R.string.prompt)
        content.gravity = 17
        content.setText(R.string.determine_delete_book)
        checkBox.setPadding(0, 0, 0, 0)
        checkBox.setText(R.string.determine_clear_book)
        confirm.setText(R.string.confirm)
        abrogate.setText(R.string.cancel)
        confirm.setOnClickListener {
            dialog.dismiss()
            onConfirmListener?.invoke(books, checkBox.isChecked)
        }
        abrogate.setOnClickListener {
            dialog.dismiss()
            onCancelListener?.invoke()
        }
    }

    fun show(books: ArrayList<Book>) {
        this.books = books
        dialog.show()
        checkBox.isChecked = false
    }

    fun isShow():Boolean{
        return dialog.isShowing
    }

    fun dismiss(){
        dialog.dismiss()
    }
}