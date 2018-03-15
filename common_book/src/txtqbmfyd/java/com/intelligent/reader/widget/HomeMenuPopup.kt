package com.intelligent.reader.widget

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.intelligent.reader.R
import kotlinx.android.synthetic.txtqbmfyd.popup_home_menu.view.*

/**
 * Desc 请描述这个文件
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/6 0006 10:36
 */
class HomeMenuPopup(context: Context) {

    private val contentView = LayoutInflater.from(context).inflate(R.layout.popup_home_menu, null)
    private val popupWindow = PopupWindow(contentView)

    private var downloadListener: (() -> Unit)? = null
    private var sortingListener: (() -> Unit)? = null

    init {
        popupWindow.width = LinearLayout.LayoutParams.WRAP_CONTENT
        popupWindow.height = LinearLayout.LayoutParams.WRAP_CONTENT
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(0x00000000))   //为PopupWindow设置透明背景.
        popupWindow.isOutsideTouchable = false

        contentView.ll_download_manager.setOnClickListener {
            popupWindow.dismiss()
            downloadListener?.invoke()
        }
        contentView.ll_book_sorting.setOnClickListener {
            popupWindow.dismiss()
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
        popupWindow.showAsDropDown(view, 0, 0)
    }
}