package com.dingyue.bookshelf.view

import android.content.Context
import android.view.View
import android.view.WindowManager
import com.dingyue.bookshelf.R
import kotlinx.android.synthetic.qbmfxsydq.popup_remove_menu.view.*
import net.lzbook.kit.ui.widget.base.BasePopup

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

        contentView.rl_remove_content.isFocusable = true
        contentView.rl_remove_content.isFocusableInTouchMode = true
        contentView.rl_remove_content.requestFocus()

        contentView.btn_remove_delete.setOnClickListener {
            onDeleteClickListener?.invoke()
        }

        contentView.btn_remove_select_all.setOnClickListener {
            if (contentView.btn_remove_select_all.text == context.getString(R.string.select_all)) {
                onSelectAllClickListener?.invoke(true)
                contentView.btn_remove_select_all.text = context.getString(R.string.cancel_select_all)
            } else {
                onSelectAllClickListener?.invoke(false)
                contentView.btn_remove_select_all.text = context.getString(R.string.select_all)
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
        contentView.btn_remove_select_all.text = context.getString(R.string.select_all)
        showAtLocation(view)
    }

}