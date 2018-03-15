package com.intelligent.reader.widget

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.intelligent.reader.R
import kotlinx.android.synthetic.txtqbmfyd.popup_download_manger_menu.view.*
import net.lzbook.kit.utils.SettingItemsHelper

/**
 * Desc 下载管理菜单popup
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/8 0008 11:40
 */
class DownloadManagerMenuPopup(private val context: Context) {

    private val contentView = LayoutInflater.from(context).inflate(R.layout.popup_download_manger_menu, null)
    private val popupWindow = PopupWindow(contentView)

    private var onEditClickListener: (() -> Unit)? = null
    private var onTimeSortingClickListener: (() -> Unit)? = null
    private var onRecentReadSortingClickListener: (() -> Unit)? = null

    private val settingItemsHelper = SettingItemsHelper.getSettingHelper(context)

    private val selectedTextColor = context.resources.getColor(R.color.color_primary)
    private val unSelectedTextColor = context.resources.getColor(R.color.text_color_dark)

    init {
        popupWindow.width = LinearLayout.LayoutParams.WRAP_CONTENT
        popupWindow.height = LinearLayout.LayoutParams.WRAP_CONTENT
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(0x00000000))   //为PopupWindow设置透明背景.
        popupWindow.isOutsideTouchable = false

        contentView.txt_edit.setOnClickListener {
            popupWindow.dismiss()
            onEditClickListener?.invoke()
        }
        contentView.txt_time_sorting.setOnClickListener {
            popupWindow.dismiss()
            onTimeSortingClickListener?.invoke()
        }
        contentView.txt_recent_read_sorting.setOnClickListener {
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
            contentView.txt_time_sorting.setTextColor(unSelectedTextColor)
            contentView.txt_recent_read_sorting.setTextColor(selectedTextColor)
        } else {
            contentView.txt_time_sorting.setTextColor(selectedTextColor)
            contentView.txt_recent_read_sorting.setTextColor(unSelectedTextColor)
        }
        popupWindow.showAsDropDown(view, 0, 0)
    }
}