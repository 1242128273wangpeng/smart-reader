package com.dingyue.bookshelf.view

import android.content.Context
import android.view.View
import android.view.WindowManager
import com.dingyue.bookshelf.R
import com.dingyue.contract.BasePopup
import kotlinx.android.synthetic.qbzsydq.popup_remove_menu.view.*

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

    var onSelectAllClickListener: ((isSelectAll: Boolean) -> Unit)? = null

    var onDeleteClickListener: (() -> Unit)? = null

    init {

        contentView.ll_content.isFocusable = true
        contentView.ll_content.isFocusableInTouchMode = true
        contentView.ll_content.requestFocus()

        contentView.btn_delete.setOnClickListener {
            onDeleteClickListener?.invoke()
        }

        contentView.btn_select_all.setOnClickListener {
            if (contentView.btn_select_all.text == context.getString(R.string.select_all)) {
                onSelectAllClickListener?.invoke(true)
                contentView.btn_select_all.text = context.getString(R.string.select_all_cancel)
            } else {
                onSelectAllClickListener?.invoke(false)
                contentView.btn_select_all.text = context.getString(R.string.select_all)
            }
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
        contentView.btn_select_all.text = context.getString(R.string.select_all)
        showAsLocation(view)
    }

}