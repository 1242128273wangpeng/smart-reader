package com.dingyue.bookshelf.view

import android.content.Context
import android.view.View
import com.dingyue.contract.BasePopup
import android.view.WindowManager
import com.dingyue.bookshelf.R
import kotlinx.android.synthetic.txtqbmfxs.popup_remove_menu.view.*

/**
 * Desc 底部弹出 全选 删除
 * Author zhenxiang
 * 2018\5\15 0015
 */

class RemoveMenuPopup(context: Context, layout: Int = R.layout.popup_remove_menu,
                      width: Int = WindowManager.LayoutParams.MATCH_PARENT,
                      height: Int = WindowManager.LayoutParams.WRAP_CONTENT)
    : BasePopup(context, layout, width, height) {

    var onDeleteClickListener: (() -> Unit)? = null

    var onSelectClickListener: ((isSelectAll: Boolean) -> Unit)? = null
    private var isSelectAll = false


    init {

        contentView.rl_remove_content.isFocusable = true
        contentView.rl_remove_content.isFocusableInTouchMode = true
        contentView.rl_remove_content.requestFocus()

        contentView.btn_remove_delete.setOnClickListener {
            onDeleteClickListener?.invoke()
        }

        contentView.btn_remove_select_all.setOnClickListener {
            if (contentView.btn_remove_select_all.text == context.getString(R.string.select_all)) {
                contentView.btn_remove_select_all.text = context.getString(R.string.cancel_select_all)
                onSelectClickListener?.invoke(true)
            } else {
                contentView.btn_remove_select_all.text = context.getString(R.string.select_all)
                onSelectClickListener?.invoke(false)
            }
        }
    }

    fun setSelectedNum(num: Int) {
        if (num == 0) {
            contentView.btn_remove_delete.text = context.getString(R.string.delete)
            contentView.btn_remove_delete.isEnabled = false
        } else {
            val text = context.getString(R.string.delete) + "(" + num + ")"
            contentView.btn_remove_delete.text = text
            contentView.btn_remove_delete.isEnabled = true
        }
    }

    fun setSelectAllText(text: String) {
        contentView.btn_remove_select_all.text = text
    }

    fun show(view: View) {
        setSelectedNum(0)
        showAtLocation(view)
    }
}