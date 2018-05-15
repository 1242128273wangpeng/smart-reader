package com.dingyue.bookshelf.view

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.dingyue.bookshelf.R
import kotlinx.android.synthetic.qbmfkdxs.popup_head_menu.view.*

/**
 * Desc 顶部菜单弹窗
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/5/11 0011 16:27
 */
class HeadMenuPopup(activity: Activity) {

    private val contentView = LayoutInflater.from(activity).inflate(R.layout.popup_head_menu, null)
    private val popupWindow = PopupWindow(contentView)

    var onDownloadManagerClickListener: (() -> Unit)? = null

    var onBookSortingClickListener: (() -> Unit)? = null

    init {
        popupWindow.width = LinearLayout.LayoutParams.WRAP_CONTENT
        popupWindow.height = LinearLayout.LayoutParams.WRAP_CONTENT
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(0x00000000))
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
        popupWindow.showAsDropDown(view, 0, -(view.height + 30))
    }

}