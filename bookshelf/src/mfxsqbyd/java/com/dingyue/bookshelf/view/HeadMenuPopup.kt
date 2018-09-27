package com.dingyue.bookshelf.view

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.dingyue.bookshelf.R

import kotlinx.android.synthetic.mfxsqbyd.popup_head_menu.view.*
import net.lzbook.kit.ui.widget.base.BasePopup

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

        contentView.txt_download_manager.setOnClickListener {
            dismiss()
            downloadListener?.invoke()
        }

        contentView.txt_book_sorting.setOnClickListener {
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
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        popupWindow.showAtLocation(view, Gravity.TOP,location[0],location[1])
    }
}