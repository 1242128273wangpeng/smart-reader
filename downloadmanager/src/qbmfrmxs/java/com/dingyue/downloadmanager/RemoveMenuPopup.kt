package com.dingyue.downloadmanager

import android.content.Context
import android.view.View
import android.view.WindowManager
import com.dingyue.contract.BasePopup
import kotlinx.android.synthetic.qbmfrmxs.popup_download_manager_editor.view.*

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
        showAtLocation(view)
    }
}