package com.dingyue.bookshelf.view

import android.content.Context
import android.view.View
import android.view.WindowManager
import com.dingyue.bookshelf.R
import com.dingyue.contract.BasePopup
import kotlinx.android.synthetic.qbmfkdxs.popup_remove_menu.view.*

/**
 * Desc 书架编辑弹窗
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/5/12 0012 15:22
 */
class RemoveMenuPopup(context: Context, layout: Int = R.layout.popup_remove_menu,
                      width: Int = WindowManager.LayoutParams.MATCH_PARENT,
                      height: Int = WindowManager.LayoutParams.WRAP_CONTENT)
    : BasePopup(context, layout, width, height) {

    var onDeleteClickListener: (() -> Unit)? = null

    init {

        contentView.ll_content.isFocusable = true
        contentView.ll_content.isFocusableInTouchMode = true
        contentView.ll_content.requestFocus()

        contentView.btn_delete.setOnClickListener {
            onDeleteClickListener?.invoke()
        }

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
        showAtLocation(view)
    }

}