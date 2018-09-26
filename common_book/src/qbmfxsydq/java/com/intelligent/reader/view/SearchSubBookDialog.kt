package com.intelligent.reader.view

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.intelligent.reader.R
import kotlinx.android.synthetic.qbmfxsydq.dialog_search_subbook.*
import net.lzbook.kit.ui.widget.MyDialog

/**
 * Function：
 *
 * Created by JoannChen on 2018/7/30 0030 16:16
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
class SearchSubBookDialog(activity: Activity) {
    private var activity: Activity? = null

    private val dialog = MyDialog(activity, R.layout.dialog_search_subbook)


    var onConfirmListener: ((name: String, author: String) -> Unit)? = null
    var onCancelListener: (() -> Unit)? = null


    init {
        this.activity = activity
        dialog.btn_confirm.setOnClickListener {
            onConfirmListener?.invoke(dialog.edt_bookname.text.toString().trim(), dialog.edt_bookauthor.text.toString().trim())
        }

        dialog.btn_cancel.setOnClickListener {
            onCancelListener?.invoke()
            dialog.dismiss()
        }

        dialog.img_close.setOnClickListener {
            dialog.dismiss()
        }

        dialog.img_clear_name.setOnClickListener {
            dialog.edt_bookname.setText("")
        }
        dialog.img_clear_author.setOnClickListener {
            dialog.edt_bookauthor.setText("")
        }

        dialog.edt_bookname.addTextChangedListener(TextChange())
        dialog.edt_bookauthor.addTextChangedListener(TextChange())
    }

    fun show() {

        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)
        dialog.ll_container_result.visibility = View.GONE
        dialog.ll_container.visibility = View.VISIBLE
        dialog.img_close.visibility = View.GONE
        dialog.show()
    }



    fun showResult(){
        dialog.ll_container_result.visibility = View.VISIBLE
        dialog.ll_container.visibility = View.GONE
        dialog.img_close.visibility = View.VISIBLE
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()

    }

    fun setBookName(bookName:String){
        dialog.edt_bookname.setText(bookName)
        dialog.edt_bookname.setSelection(bookName.length)
    }
    fun isShow(): Boolean {
        return dialog.isShowing
    }

    fun dismiss(){
        dialog.dismiss()
    }

    inner class TextChange : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

        }
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            if (dialog.edt_bookname.text.length > 0 && dialog.edt_bookauthor.text.length > 0) {
                dialog.btn_confirm.isClickable = true
                dialog.btn_confirm.setBackgroundResource(R.drawable.search_sub_book_no_press_bg)
            } else {
                dialog.btn_confirm.isClickable = false
                dialog.btn_confirm.setBackgroundResource(R.drawable.search_sub_book_press_bg)
            }

            if (dialog.edt_bookauthor.text.length > 0) {
                dialog.img_clear_author.visibility = View.VISIBLE
            } else {
                dialog.img_clear_author.visibility = View.GONE
            }

            if (dialog.edt_bookname.text.length > 0) {
                dialog.img_clear_name.visibility = View.VISIBLE
            } else {
                dialog.img_clear_name.visibility = View.GONE
            }
        }

        override fun afterTextChanged(editable: Editable) {

        }
    }
}