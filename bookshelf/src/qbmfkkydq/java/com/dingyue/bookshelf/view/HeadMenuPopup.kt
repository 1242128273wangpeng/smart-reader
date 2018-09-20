package com.dingyue.bookshelf.view

import android.content.Context
import android.view.View
import android.view.WindowManager
import com.dingyue.bookshelf.R

import kotlinx.android.synthetic.qbmfkkydq.popup_head_menu.view.*
import net.lzbook.kit.base.BasePopup

/**
 * Desc 请描述这个文件
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/6 0006 10:36
 */
class HeadMenuPopup(context: Context, layout: Int = R.layout.popup_head_menu,
                    width: Int = WindowManager.LayoutParams.WRAP_CONTENT,
                    height: Int = WindowManager.LayoutParams.WRAP_CONTENT)
    : BasePopup(context, layout, width, height) {

    private var downloadListener: (() -> Unit)? = null
    private var sortingListener: (() -> Unit)? = null

    init {
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = false

        contentView.ll_download_manager.setOnClickListener {
            dismiss()
            downloadListener?.invoke()
        }

        contentView.ll_book_sorting.setOnClickListener {
            dismiss()
            sortingListener?.invoke()
        }

    }

    fun setOnDownloadClickListener(listener: (() -> Unit)) {
        downloadListener = listener
    }

    fun setOnSortingClickListener(listener: (() -> Unit)) {
        sortingListener = listener
    }

    fun show(view: View) {
        showAsDropDown(view)
    }
}