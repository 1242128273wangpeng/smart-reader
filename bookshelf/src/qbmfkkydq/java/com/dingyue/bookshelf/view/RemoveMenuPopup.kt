package com.dingyue.bookshelf.view

import android.content.Context
import android.graphics.Color
import android.view.View
import com.dingyue.contract.BasePopup
import android.view.WindowManager
import com.dingyue.bookshelf.R
import kotlinx.android.synthetic.qbmfkkydq.popup_remove_menu.view.*

/**
 * Desc 底部弹出 全选 删除
 * Author zhenxiang
 * 2018\5\15 0015
 */

class RemoveMenuPopup(context: Context, layout: Int = R.layout.popup_remove_menu,
                      width: Int = WindowManager.LayoutParams.MATCH_PARENT,
                      height: Int = WindowManager.LayoutParams.WRAP_CONTENT)
    : BasePopup(context, layout, width, height) {

    private var onDeleteClickListener: (() -> Unit)? = null


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
            contentView.btn_remove_delete.setTextColor(Color.parseColor("#CC2AD1BE"))
        } else {
            val text = context.getString(R.string.delete) + "(" + num + ")"
            contentView.btn_remove_delete.text = text
            contentView.btn_remove_delete.isEnabled = true
            contentView.btn_remove_delete.setTextColor(Color.parseColor("#FF2AD1BE"))
        }
    }

    fun show(view: View) {
        setSelectedNum(0)
        showAtLocation(view)
    }
}