package com.dingyue.bookshelf

import android.app.Activity
import android.view.Gravity
import android.view.View
import kotlinx.android.synthetic.txtqbmfyd.dialog_delete_book.*
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.data.bean.Book

/**
 * Created by qiantao on 2017/11/17 0017
 */
class BookDeleteDialog(private val activity: Activity) {

    private val dialog = MyDialog(activity, R.layout.dialog_delete_book, Gravity.BOTTOM)

    private var confirmListener: ((books: ArrayList<Book>?, isOnlyDeleteCache: Boolean) -> Unit)? = null
    private var abrogateListener: (() -> Unit)? = null
    private var books: ArrayList<Book>? = null

    init {

        val window = dialog.window
        window.setWindowAnimations(R.style.BottomPopupDialog)

        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        dialog.btn_confirm.setOnClickListener {
            confirmListener?.invoke(books, dialog.ckb_delete_cache.isChecked)
        }
        dialog.btn_cancel.setOnClickListener {
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
        dialog.txt_dialog_title.text = activity.getString(R.string.prompt)
        dialog.txt_dialog_information.visibility = View.VISIBLE
        dialog.ckb_delete_cache.visibility = View.VISIBLE
        dialog.view_divider.visibility = View.VISIBLE
        dialog.ll_btn.visibility = View.VISIBLE
        dialog.pgbar_loading.visibility = View.GONE
        this.books = books
        dialog.ckb_delete_cache.isChecked = false
        dialog.show()
    }

    fun showLoading() {
        dialog.txt_dialog_title.text = activity.getString(R.string.tip_cleaning_cache)
        dialog.txt_dialog_information.visibility = View.INVISIBLE
        dialog.ckb_delete_cache.visibility = View.INVISIBLE
        dialog.view_divider.visibility = View.INVISIBLE
        dialog.ll_btn.visibility = View.INVISIBLE
        dialog.pgbar_loading.visibility = View.VISIBLE
    }

    fun dismiss() {
        dialog.dismiss()
    }

}