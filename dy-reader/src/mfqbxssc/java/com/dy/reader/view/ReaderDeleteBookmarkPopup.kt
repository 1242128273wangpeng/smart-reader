package com.dy.reader.view

import android.content.Context
import android.view.WindowManager
import com.dingyue.contract.BasePopup
import com.dy.reader.R

import kotlinx.android.synthetic.qbzsydq.popup_catalog_mark_delete.view.*

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/6/9 09:54
 */
class ReaderDeleteBookmarkPopup(context: Context, layout: Int = R.layout.popup_catalog_mark_delete,
                                width: Int = WindowManager.LayoutParams.WRAP_CONTENT,
                                height: Int = WindowManager.LayoutParams.WRAP_CONTENT)
    : BasePopup(context, layout, width, height) {

    var deleteBookmarkListener: (() -> Unit)? = null

    var clearBookmarkListener: (() -> Unit)? = null

    var dismissList: (() -> Unit)? = null

    init {

        contentView.ll_catalog_mark_content.requestFocus()

        contentView.txt_delete_mark.setOnClickListener {
            deleteBookmarkListener?.invoke()
        }

        contentView.txt_clear_mark.setOnClickListener {
            clearBookmarkListener?.invoke()
        }

        this.insertDismissListener(android.widget.PopupWindow.OnDismissListener {
            dismissList?.invoke()
        })
    }
}