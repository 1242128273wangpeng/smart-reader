package com.dingyue.downloadmanager

import android.content.Context
import android.view.View
import android.view.WindowManager
import com.dingyue.contract.BasePopup
import kotlinx.android.synthetic.mfqbxssc.popup_download_manager_editor.view.*

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

    init {

        contentView.btn_delete.setOnClickListener {
            onDeleteClickListener?.invoke()
        }
    }

    fun setOnDeletedClickListener(onConfirmClickListener: () -> Unit) {
        this.onDeleteClickListener = onConfirmClickListener
    }

    fun setSelectedNum(num: Int) {
        if (num == 0) {
            contentView.btn_delete.text = context.getString(R.string.delete_cache)
        } else {
            val text = context.getString(R.string.delete_cache) + "(" + num + ")"
            contentView.btn_delete.text = text
        }
    }

    fun show(view: View) {
        setSelectedNum(0)
        showAtLocation(view)
    }
}