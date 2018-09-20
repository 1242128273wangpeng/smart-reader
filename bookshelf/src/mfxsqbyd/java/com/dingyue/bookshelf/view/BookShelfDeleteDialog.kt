package com.dingyue.bookshelf.view

import android.app.Activity
import android.view.Gravity
import android.view.View
import com.ding.basic.bean.Book
import com.dingyue.bookshelf.R
import kotlinx.android.synthetic.mfxsqbyd.dialog_bookshelf_delete.*
import net.lzbook.kit.widget.MyDialog


/**
 * Created by qiantao on 2017/11/17 0017
 */
class BookShelfDeleteDialog(private val activity: Activity) {

    private val dialog = MyDialog(activity, R.layout.dialog_bookshelf_delete, Gravity.CENTER)

    private var confirmListener: ((books: ArrayList<Book>?, isOnlyDeleteCache: Boolean) -> Unit)? = null
    private var abrogateListener: (() -> Unit)? = null
    private var books: ArrayList<Book>? = null

    init {


        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        dialog.btn_delete_confirm.setOnClickListener {
            confirmListener?.invoke(books, false)
        }

        dialog.btn_delete_cancel.setOnClickListener {
            dialog.dismiss()
            abrogateListener?.invoke()
        }
    }

    fun setOnConfirmListener(listener: (books: ArrayList<Book>?, isOnlyDeleteCache: Boolean) -> Unit) {
        confirmListener = listener
    }

    fun setOnAbrogateListener(listener: () -> Unit) {
        abrogateListener = listener
    }

    fun show(books: ArrayList<Book>?) {
        if (books == null) return
        this.books = books
        dialog.rl_container.visibility = View.VISIBLE
        dialog.ll_loading_content.visibility = View.GONE
        dialog.show()
    }

    fun showLoading() {
        dialog.rl_container.visibility = View.GONE
        dialog.ll_loading_content.visibility = View.VISIBLE
    }

    fun dismiss() {
        dialog.dismiss()
    }
}