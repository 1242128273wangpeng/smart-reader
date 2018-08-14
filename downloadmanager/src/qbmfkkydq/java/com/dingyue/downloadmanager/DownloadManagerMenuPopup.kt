package com.dingyue.downloadmanager

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import kotlinx.android.synthetic.qbmfkkydq.popup_download_manager_head_menu.view.*
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.SettingItemsHelper

/**
 * Desc 下载管理菜单popup
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/8 0008 11:40
 */
class DownloadManagerMenuPopup(context: Context) {

    private val contentView = LayoutInflater.from(context).inflate(R.layout.popup_download_manager_head_menu, null)
    private val popupWindow = PopupWindow(contentView)

    private var onEditClickListener: (() -> Unit)? = null
    private var onAddSortingClickListener: (() -> Unit)? = null
    private var onTimeSortingClickListener: (() -> Unit)? = null
    private var onRecentReadSortingClickListener: (() -> Unit)? = null

    private val settingItemsHelper = SettingItemsHelper.getSettingHelper(context)

    private val unSelectColor = ContextCompat.getColor(context, R.color.text_color_dark)
    private val selectedColor = ContextCompat.getColor(context, R.color.primary)

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
        contentView.txt_add_sorting.setOnClickListener {
            popupWindow.dismiss()
            onAddSortingClickListener?.invoke()
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

    fun setOnAddSortingClickListener(listener: (() -> Unit)) {
        onAddSortingClickListener = listener
    }

    fun setOnTimeSortingClickListener(listener: (() -> Unit)) {
        onTimeSortingClickListener = listener
    }

    fun setOnRecentReadSortingClickListener(listener: (() -> Unit)) {
        onRecentReadSortingClickListener = listener
    }

    /**
     * 书架书籍排序
     * 0 阅读时间
     * 1 更新时间
     * 2 添加时间
     */
    fun show(view: View) {
        when (Constants.book_list_sort_type) {
            0 -> setSortChecked(isReadSort = true)
            1 -> setSortChecked(isTimeSort = true)
            2 -> setSortChecked(isAddSort = true)
        }
        popupWindow.showAsDropDown(view, 0, 0)
    }

    private fun setSortChecked(isAddSort: Boolean = false, isTimeSort: Boolean = false, isReadSort: Boolean = false) {
        contentView.txt_add_sorting.setTextColor(if (isAddSort) selectedColor else unSelectColor)
        contentView.txt_time_sorting.setTextColor(if (isTimeSort) selectedColor else unSelectColor)
        contentView.txt_recent_read_sorting.setTextColor(if (isReadSort) selectedColor else unSelectColor)
    }
}