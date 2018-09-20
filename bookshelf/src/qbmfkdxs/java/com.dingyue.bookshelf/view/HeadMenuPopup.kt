package com.dingyue.bookshelf.view

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.v4.widget.PopupWindowCompat.showAsDropDown
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import com.dingyue.bookshelf.R

import kotlinx.android.synthetic.qbmfkdxs.popup_head_menu.view.*
import net.lzbook.kit.base.BasePopup

/**
 * Desc 顶部菜单弹窗
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/5/11 0011 16:27
 */
class HeadMenuPopup(context: Context, layout: Int = R.layout.popup_head_menu,
                    width: Int = WindowManager.LayoutParams.WRAP_CONTENT,
                    height: Int = WindowManager.LayoutParams.WRAP_CONTENT)
    : BasePopup(context, layout, width, height){

    var onDownloadManagerClickListener: (() -> Unit)? = null

    var onBookSortingClickListener: (() -> Unit)? = null

    init {
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = false

        contentView.ll_download_manager.setOnClickListener {
            popupWindow.dismiss()
            onDownloadManagerClickListener?.invoke()
        }
        contentView.ll_book_sorting.setOnClickListener {
            popupWindow.dismiss()
            onBookSortingClickListener?.invoke()
        }

    }

    fun show(view: View) {
        showAsDropDown(view, 0, -(view.height + 30))
    }

}