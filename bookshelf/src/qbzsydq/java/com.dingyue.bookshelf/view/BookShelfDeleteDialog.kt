package com.dingyue.bookshelf.view

import android.app.Activity
import android.view.View
import com.ding.basic.bean.Book
import com.dingyue.bookshelf.R
import kotlinx.android.synthetic.qbzsydq.dialog_bookshelf_delete.*
import net.lzbook.kit.ui.widget.MyDialog


/**
 * Created by qiantao on 2017/11/17 0017
 */
class BookShelfDeleteDialog(activity: Activity) {

    private val dialog = MyDialog(activity, R.layout.dialog_bookshelf_delete)


    var onConfirmListener: ((books: ArrayList<Book>, isDeleteCacheOnly: Boolean) -> Unit)? = null
    var onCancelListener: (() -> Unit)? = null

    private var books: ArrayList<Book> = ArrayList()

    init {
        dialog.btn_delete_confirm.setOnClickListener {
            showLoading()
            onConfirmListener?.invoke(books, dialog.ckb_delete_cache.isChecked)
        }
        dialog.btn_delete_cancel.setOnClickListener {
            dialog.dismiss()
            onCancelListener?.invoke()
        }
    }

    fun show(books: ArrayList<Book>) {
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.ll_delete_content.visibility = View.VISIBLE
        dialog.ll_loading_content.visibility = View.GONE
        this.books.clear()
        this.books.addAll(books)
        dialog.show()
        dialog.ckb_delete_cache.isChecked = false
    }

    private fun showLoading() {
        dialog.ll_delete_content.visibility = View.GONE
        dialog.ll_loading_content.visibility = View.VISIBLE
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
    }

    fun dismiss() {
        dialog.dismiss()
    }

}