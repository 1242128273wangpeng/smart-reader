package com.dingyue.bookshelf.view

import android.app.Activity
import android.view.View
import com.ding.basic.bean.Book
import com.dingyue.bookshelf.R
import kotlinx.android.synthetic.zsmfqbxs.dialog_bookshelf_delete.*
import net.lzbook.kit.book.view.MyDialog

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
        dialog.txt_delete_title.setText(R.string.bookshelf_delete_dialog_title)
        dialog.ckb_delete_cache.setPadding(0, 0, 0, 0)
        dialog.ckb_delete_cache.setText(R.string.bookshelf_delete_dialog_check)
        dialog.btn_dialog_confirm.setText(R.string.confirm)
        dialog.btn_dialog_cancel.setText(R.string.cancel)
        dialog.btn_dialog_confirm.setOnClickListener {
            showLoading()
            onConfirmListener?.invoke(books, dialog.ckb_delete_cache.isChecked)
        }
        dialog.btn_dialog_cancel.setOnClickListener {
            dialog.dismiss()
            onCancelListener?.invoke()
        }
    }

    fun show(books: ArrayList<Book>) {
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.ll_delete_content.visibility = View.VISIBLE
        dialog.ll_delete_loading.visibility = View.GONE
        this.books.clear()
        this.books.addAll(books)
        dialog.show()
        dialog.ckb_delete_cache.isChecked = false

    }

    fun showLoading() {
        dialog.ll_delete_content.visibility = View.GONE
        dialog.ll_delete_loading.visibility = View.VISIBLE
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
    }


    fun isShow(): Boolean {
        return dialog.isShowing
    }

    fun dismiss(){
        dialog.dismiss()
    }
}