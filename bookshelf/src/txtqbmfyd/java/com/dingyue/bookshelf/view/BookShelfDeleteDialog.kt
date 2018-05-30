package com.dingyue.bookshelf.view

import android.app.Activity
import android.view.Gravity
import android.view.View
import com.ding.basic.bean.Book
import com.dingyue.bookshelf.R
import kotlinx.android.synthetic.txtqbmfyd.dialog_bookshelf_delete.*
import net.lzbook.kit.book.view.MyDialog

/**
 * Created by qiantao on 2017/11/17 0017
 */
class BookShelfDeleteDialog(private val activity: Activity) {

    private val dialog = MyDialog(activity, R.layout.dialog_bookshelf_delete, Gravity.BOTTOM)

    private var confirmListener: ((books: ArrayList<Book>?, isOnlyDeleteCache: Boolean) -> Unit)? = null
    private var abrogateListener: (() -> Unit)? = null
    private var books: ArrayList<Book>? = null

    init {

        val window = dialog.window
        window.setWindowAnimations(R.style.BottomPopupDialog)

        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        dialog.btn_delete_confirm.setOnClickListener {
            confirmListener?.invoke(books, dialog.ckb_delete_cache.isChecked)
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

    fun show(books: ArrayList<Book>) {
        this.books = books
        dialog.txt_delete_title.text = activity.getString(R.string.bookshelf_delete_dialog_title)
        dialog.txt_delete_prompt.visibility = View.VISIBLE
        dialog.ckb_delete_cache.visibility = View.VISIBLE
        dialog.view_divider.visibility = View.VISIBLE
        dialog.ll_delete_option.visibility = View.VISIBLE
        dialog.pgbar_delete_loading.visibility = View.GONE
        dialog.ckb_delete_cache.isChecked = false
        dialog.show()
    }

    fun showLoading() {
        dialog.txt_delete_title.text = activity.getString(R.string.bookshelf_delete_dialog_loading_prompt)
        dialog.txt_delete_prompt.visibility = View.INVISIBLE
        dialog.ckb_delete_cache.visibility = View.INVISIBLE
        dialog.view_divider.visibility = View.INVISIBLE
        dialog.ll_delete_option.visibility = View.INVISIBLE
        dialog.pgbar_delete_loading.visibility = View.VISIBLE
    }

    fun dismiss() {
        dialog.dismiss()
    }
}