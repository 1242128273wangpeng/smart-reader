package com.dingyue.downloadmanager

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import kotlinx.android.synthetic.txtqbdzs.popup_download_manager_more.view.*
import net.lzbook.kit.utils.SettingItemsHelper

/**
 * Desc 下载管理菜单popup
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/8 0008 11:40
 */
class DownloadManagerMenuPopup(context: Context) {

    private val contentView = LayoutInflater.from(context).inflate(R.layout.popup_download_manager_more, null)
    private val popupWindow = PopupWindow(contentView)

    private var onEditClickListener: (() -> Unit)? = null
    private var onTimeSortingClickListener: (() -> Unit)? = null
    private var onRecentReadSortingClickListener: (() -> Unit)? = null

    private val settingItemsHelper = SettingItemsHelper.getSettingHelper(context)

    private val selectTextColor = Color.parseColor("#212832")
    private val selectedTextColor = Color.parseColor("#3ECC96")

    init {
        popupWindow.width = LinearLayout.LayoutParams.WRAP_CONTENT
        popupWindow.height = LinearLayout.LayoutParams.WRAP_CONTENT
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(0x00000000))
        popupWindow.isOutsideTouchable = false

        contentView.txt_edit.setOnClickListener {
            popupWindow.dismiss()
            onEditClickListener?.invoke()
        }
        contentView.txt_sort_update.setOnClickListener {
            popupWindow.dismiss()
            onTimeSortingClickListener?.invoke()
        }
        contentView.txt_sort_read.setOnClickListener {
            popupWindow.dismiss()
            onRecentReadSortingClickListener?.invoke()
        }
    }

    fun setOnEditClickListener(listener: (() -> Unit)) {
        onEditClickListener = listener
    }

    fun setOnTimeSortingClickListener(listener: (() -> Unit)) {
        onTimeSortingClickListener = listener
    }

    fun setOnRecentReadSortingClickListener(listener: (() -> Unit)) {
        onRecentReadSortingClickListener = listener
    }

    fun show(view: View) {
        if (settingItemsHelper.values.booklist_sort_type == 0) {
            contentView.txt_sort_update.setTextColor(selectTextColor)
            contentView.txt_sort_read.setTextColor(selectedTextColor)
        } else {
            contentView.txt_sort_update.setTextColor(selectedTextColor)
            contentView.txt_sort_read.setTextColor(selectTextColor)
        }
        popupWindow.showAsDropDown(view, 0, 0)
    }
}