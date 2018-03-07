package com.intelligent.reader.widget

import android.app.Activity
import android.widget.FrameLayout
import com.intelligent.reader.R
import kotlinx.android.synthetic.txtqbmfyd.dialog_delete_book.*
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.data.bean.Book

/**
 * Created by qiantao on 2017/11/17 0017
 */
class BookDeleteDialog(activity: Activity) {

    private val dialog = MyDialog(activity, R.layout.dialog_delete_book)

    private var confirmListener: ((books: ArrayList<Book>?, isOnlyDeleteCache: Boolean) -> Unit)? = null
    private var abrogateListener: (() -> Unit)? = null
    private var books: ArrayList<Book>? = null

    init {

        val window = dialog.window
        val layoutParams = window.attributes
        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
        layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT
        window.attributes = layoutParams
        window.setWindowAnimations(R.style.BottomPopupDialog)

        dialog.btn_confirm.setOnClickListener {
            dialog.dismiss()
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
        this.books = books
        dialog.ckb_delete_cache.isChecked = false
        dialog.show()
    }

}