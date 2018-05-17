package com.intelligent.reader.view

import android.app.Activity
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import com.dingyue.bookshelf.R
import kotlinx.android.synthetic.zsmfqbxs.dialog_bookshelf_delete.*
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.data.bean.Book

/**
 * Desc 点击底部删除 弹出dialog
 * Author zhenxiang
 * 2018\5\15 0015
 */
class BookShelfDeleteDialog(activity: Activity) {

    private val dialog = MyDialog(activity, R.layout.dialog_bookshelf_delete)


    var onConfirmListener: ((books: ArrayList<Book>, isDeleteCacheOnly: Boolean) -> Unit)? = null
    var onCancelListener: (() -> Unit)? = null

    private var books: ArrayList<Book> = ArrayList()

    init {
        dialog.txt_delete_title.setText(R.string.prompt)
        dialog.ckb_delete_cache.setPadding(0, 0, 0, 0)
        dialog.ckb_delete_cache.setText(R.string.determine_clear_book)
        dialog.btn_dialog_confirm.setText(R.string.confirm)
        dialog.btn_dialog_cancel.setText(R.string.cancel)
        dialog.btn_dialog_confirm.setOnClickListener {
            dialog.dismiss()
            onConfirmListener?.invoke(books, dialog.ckb_delete_cache.isChecked)
        }
        dialog.btn_dialog_cancel.setOnClickListener {
            dialog.dismiss()
            onCancelListener?.invoke()
        }
    }

    fun show(books: ArrayList<Book>) {
        this.books = books
        dialog.show()
        dialog.ckb_delete_cache.isChecked = false
    }

    fun isShow(): Boolean {
        return dialog.isShowing
    }

    fun dismiss(){
        dialog.dismiss()
    }
}