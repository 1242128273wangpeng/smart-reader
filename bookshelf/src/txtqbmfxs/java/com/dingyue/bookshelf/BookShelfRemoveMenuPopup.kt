package com.dingyue.bookshelf

import android.content.Context
import android.view.View
import com.dingyue.contract.BasePopup
import android.view.WindowManager
import kotlinx.android.synthetic.txtqbmfxs.popup_remove_menu.view.*

/**
 * Desc 底部弹出 全选 删除
 * Author zhenxiang
 * 2018\5\15 0015
 */

class BookShelfRemoveMenuPopup(context: Context, layout: Int = R.layout.popup_remove_menu,
                               width: Int = WindowManager.LayoutParams.MATCH_PARENT,
                               height: Int = WindowManager.LayoutParams.WRAP_CONTENT)
    : BasePopup(context, layout, width, height) {

    var onDeleteClickListener: (() -> Unit)? = null

    var onSelectClickListener: ((isSelectAll: Boolean) -> Unit)? = null
    private var isSelectAll = false


    init {

        contentView.rl_container.isFocusable = true
        contentView.rl_container.isFocusableInTouchMode = true
        contentView.rl_container.requestFocus()

        contentView.btn_remove_delete.setOnClickListener {
            onDeleteClickListener?.invoke()
        }

        contentView.btn_remove_select_all.setOnClickListener {
            if(contentView.btn_remove_select_all.text == "全选"){
                contentView.btn_remove_select_all.text = "取消全选"
                isSelectAll = true
            }else{
                isSelectAll = false
                contentView.btn_remove_select_all.text = "全选"
            }
            onSelectClickListener?.invoke(isSelectAll)
        }
    }

    fun setSelectedNum(num: Int,isSelectAll: Boolean) {
        contentView.btn_remove_select_all.text = if (isSelectAll) "取消全选" else "全选"

        if (num == 0) {
            contentView.btn_remove_delete.text = context.getString(R.string.delete)
            contentView.btn_remove_delete.isEnabled = false
        } else {
            val text = context.getString(R.string.delete) + "(" + num + ")"
            contentView.btn_remove_delete.text = text
            contentView.btn_remove_delete.isEnabled = true
        }
    }

    fun show(view: View) {
        setSelectedNum(0,false)
        showAsLocation(view)
    }


}