package com.dingyue.bookshelf

import android.content.Context
import android.view.View
import android.view.WindowManager
import com.dingyue.contract.BasePopup
import kotlinx.android.synthetic.txtqbmfyd.popup_bookshelf_bottom_editor.view.*

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/5/11 10:11
 */
class BookShelfRemovePopup(context: Context, layout: Int = R.layout.popup_bookshelf_bottom_editor,
                      width: Int = WindowManager.LayoutParams.MATCH_PARENT,
                      height: Int = WindowManager.LayoutParams.WRAP_CONTENT)
    : BasePopup(context, layout, width, height) {

    private var onDeleteClickListener: (() -> Unit)? = null

    private var onCancelClickListener: (() -> Unit)? = null

    init {

        contentView.rl_remove.isFocusable = true
        contentView.rl_remove.isFocusableInTouchMode = true
        contentView.rl_remove.requestFocus()

        contentView.btn_cancel.setOnClickListener {
            onCancelClickListener?.invoke()
        }

        contentView.btn_delete.setOnClickListener {
            onDeleteClickListener?.invoke()
        }
    }

    fun setOnDeletedClickListener(onConfirmClickListener: () -> Unit) {
        this.onDeleteClickListener = onConfirmClickListener
    }

    fun setOnCancelClickListener(onConfirmClickListener: () -> Unit) {
        this.onCancelClickListener = onConfirmClickListener
    }

    fun setSelectedNum(num: Int) {
        if (num == 0) {
            contentView.btn_delete.text = context.getString(R.string.delete)
            contentView.btn_delete.isEnabled = false
        } else {
            val text = context.getString(R.string.delete) + "(" + num + ")"
            contentView.btn_delete.text = text
            contentView.btn_delete.isEnabled = true
        }
    }

    fun show(view: View) {
        setSelectedNum(0)
        showAsLocation(view)
    }
}