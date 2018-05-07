package com.dingyue.downloadmanager

import android.content.Context
import android.view.View
import android.view.WindowManager
import kotlinx.android.synthetic.qbzsydq.popup_download_manager_editor.view.*

/**
 * Desc 底部删除弹窗
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/4/25 0025 16:39
 */
class RemoveMenuPopup(context: Context, layout: Int = R.layout.popup_download_manager_editor,
                      width: Int = WindowManager.LayoutParams.MATCH_PARENT,
                      height: Int = WindowManager.LayoutParams.WRAP_CONTENT)
    : BasePopup(context, layout, width, height) {

    private var onDeleteClickListener: (() -> Unit)? = null

    private var onSelectAllClickListener: ((isSelectAll: Boolean) -> Unit)? = null

    init {

        contentView.rl_remove.isFocusable = true
        contentView.rl_remove.isFocusableInTouchMode = true
        contentView.rl_remove.requestFocus()

        contentView.btn_select_all.setOnClickListener {
            if (contentView.btn_select_all.text == context.getString(R.string.select_all)) {
                contentView.btn_select_all.text = context.getString(R.string.select_all_cancel)
                onSelectAllClickListener?.invoke(true)
            } else {
                contentView.btn_select_all.text = context.getString(R.string.select_all)
                onSelectAllClickListener?.invoke(false)
            }
        }

        contentView.btn_delete.setOnClickListener {
            onDeleteClickListener?.invoke()
        }
    }

    fun setOnDeletedClickListener(onConfirmClickListener: () -> Unit) {
        this.onDeleteClickListener = onConfirmClickListener
    }

    fun setOnSelectAllClickListener(onSelectAllClickListener: (isSelectAll: Boolean) -> Unit) {
        this.onSelectAllClickListener = onSelectAllClickListener
    }

    fun setSelectedNum(num: Int) {
        if (num == 0) {
            contentView.btn_delete.text = context.getString(R.string.delete_cache)
        } else {
            val text = context.getString(R.string.delete_cache) + "(" + num + ")"
            contentView.btn_delete.text = text
        }
    }

    fun setSelectAllText(text: String) {
        contentView.btn_select_all.text = text
    }

    fun show(view: View) {
        setSelectedNum(0)
        contentView.btn_select_all.text = context.getString(R.string.select_all)
        showAsLocation(view)
    }
}