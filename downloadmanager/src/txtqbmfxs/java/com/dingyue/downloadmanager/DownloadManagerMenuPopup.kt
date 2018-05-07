package com.dingyue.downloadmanager

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import kotlinx.android.synthetic.txtqbmfxs.popup_download_manager_head_menu.view.*
import net.lzbook.kit.utils.SettingItemsHelper

/**
 * Desc 下载管理菜单popup
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/8 0008 11:40
 */
class DownloadManagerMenuPopup(private val context: Context) {

    private val contentView = LayoutInflater.from(context).inflate(R.layout.popup_download_manager_head_menu, null)
    private val popupWindow = PopupWindow(contentView)

    private var onEditClickListener: (() -> Unit)? = null
    private var onTimeSortingClickListener: (() -> Unit)? = null
    private var onRecentReadSortingClickListener: (() -> Unit)? = null

    private val settingItemsHelper = SettingItemsHelper.getSettingHelper(context)

    private val selectedTextColor = Color.parseColor("#87AF4C")
    private val selectTextColor = Color.parseColor("#212832")

    init {
        popupWindow.width = LinearLayout.LayoutParams.WRAP_CONTENT
        popupWindow.height = LinearLayout.LayoutParams.WRAP_CONTENT
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(0x00000000))   //为PopupWindow设置透明背景.
        popupWindow.isOutsideTouchable = false

        contentView.download_head_pop_edit.setOnClickListener {
            popupWindow.dismiss()
            onEditClickListener?.invoke()
        }
        contentView.download_head_pop_sort_time.setOnClickListener {
            popupWindow.dismiss()
            onTimeSortingClickListener?.invoke()
        }
        contentView.download_head_pop_sort_read.setOnClickListener {
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
            contentView.download_head_pop_sort_time.setTextColor(selectTextColor)
            contentView.download_head_pop_sort_read.setTextColor(selectedTextColor)
        } else {
            contentView.download_head_pop_sort_time.setTextColor(selectedTextColor)
            contentView.download_head_pop_sort_read.setTextColor(selectTextColor)
        }
        popupWindow.showAsDropDown(view, 0, 0)
    }
}