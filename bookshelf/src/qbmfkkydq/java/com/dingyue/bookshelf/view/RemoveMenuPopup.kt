package com.dingyue.bookshelf.view

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.WindowManager
import com.dingyue.bookshelf.R
import com.dingyue.contract.BasePopup
import kotlinx.android.synthetic.qbmfkkydq.popup_remove_menu.view.*

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/5/11 10:11
 */
class RemoveMenuPopup(context: Context, layout: Int = R.layout.popup_remove_menu,
                      width: Int = WindowManager.LayoutParams.MATCH_PARENT,
                      height: Int = WindowManager.LayoutParams.WRAP_CONTENT)
    : BasePopup(context, layout, width, height) {

    private var onDeleteClickListener: (() -> Unit)? = null

    private var onCancelClickListener: (() -> Unit)? = null

    init {

        contentView.rl_remove_content.isFocusable = true
        contentView.rl_remove_content.isFocusableInTouchMode = true
        contentView.rl_remove_content.requestFocus()

        contentView.btn_remove_delete.setOnClickListener {
            onDeleteClickListener?.invoke()
        }
    }

    fun setOnDeletedClickListener(onConfirmClickListener: () -> Unit) {
        this.onDeleteClickListener = onConfirmClickListener
    }


    fun setSelectedNum(num: Int) {
        if (num == 0) {
            contentView.btn_remove_delete.text = context.getString(R.string.delete)
            contentView.btn_remove_delete.isEnabled = false
            contentView.btn_remove_delete.setTextColor(Color.parseColor("#989898"))
        } else {
            val text = context.getString(R.string.delete) + "(" + num + ")"
            contentView.btn_remove_delete.text = text
            contentView.btn_remove_delete.isEnabled = true
            contentView.btn_remove_delete.setTextColor(Color.parseColor("#42BE54"))
        }
    }

    fun show(view: View) {
        setSelectedNum(0)
        showAtLocation(view)
    }
}