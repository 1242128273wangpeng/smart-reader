package com.intelligent.reader.view

import android.app.Activity
import android.view.Gravity
import android.widget.Button
import android.widget.TextView
import com.intelligent.reader.R
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.data.bean.Book

/**
 * Created by qiantao on 2017/11/17 0017
 */
class BookDeleteDialog(activity: Activity) {

    private val dialog = MyDialog(activity, R.layout.publish_hint_dialog)

    private val title = dialog.findViewById(R.id.dialog_title) as TextView
    private val content = dialog.findViewById(R.id.publish_content) as TextView
    private val confirm = dialog.findViewById(R.id.publish_leave) as Button
    private val abrogate = dialog.findViewById(R.id.publish_stay) as Button

    private var confirmListener: ((books: ArrayList<Book>?) -> Unit)? = null
    private var abrogateListener: (() -> Unit)? = null
    private var books: ArrayList<Book>? = null

    init {
        title.setText(R.string.prompt)
        content.gravity = Gravity.CENTER
        content.setText(R.string.determine_delete_book_cache)
        confirm.setText(R.string.confirm)
        abrogate.setText(R.string.cancel)
        confirm.setOnClickListener {
            dialog.dismiss()
            confirmListener?.invoke(books)
        }
        abrogate.setOnClickListener {
            dialog.dismiss()
            abrogateListener?.invoke()
        }
    }

    fun setOnConfirmListener(listener: (books: ArrayList<Book>?) -> Unit) {
        confirmListener = listener
    }

    fun setOnAbrogateListener(listener: () -> Unit) {
        abrogateListener = listener
    }

    fun show(books: ArrayList<Book>) {
        this.books = books
        dialog.show()
    }

}