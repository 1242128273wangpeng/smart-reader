package com.intelligent.reader.widget

import android.app.Activity
import android.widget.FrameLayout
import com.intelligent.reader.R
import kotlinx.android.synthetic.txtqbmfyd.dialog_book_sorting.*
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.utils.SettingItemsHelper


/**
 * Desc 清除缓存
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/1 0002 15:09
 */
class BookSortingDialog(val activity: Activity) {

    private val dialog = MyDialog(activity, R.layout.dialog_book_sorting)

    private var recentReadListener: (() -> Unit)? = null
    private var updateTimeListener: (() -> Unit)? = null

    private val settingItemsHelper = SettingItemsHelper.getSettingHelper(activity)

    private val selectedTextColor = activity.resources.getColor(R.color.color_primary)
    private val unSelectedTextColor = activity.resources.getColor(R.color.text_color_dark)

    init {

        val window = dialog.window
        val layoutParams = window.attributes
        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
        layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT
        window.attributes = layoutParams
        window.setWindowAnimations(R.style.BottomPopupDialog)

        dialog.txt_recent_read_sorting.setOnClickListener {
            recentReadListener?.invoke()
            dialog.dismiss()
        }
        dialog.txt_update_time_sorting.setOnClickListener {
            updateTimeListener?.invoke()
            dialog.dismiss()
        }
        dialog.txt_cancel.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun setOnRecentReadClickListener(listener: () -> Unit) {
        recentReadListener = listener
    }

    fun setOnUpdateTimeClickListener(listener: () -> Unit) {
        dialog.dismiss()
        updateTimeListener = listener
    }

    fun show() {
        if (settingItemsHelper.values.booklist_sort_type == 0) {
            dialog.txt_update_time_sorting.setTextColor(unSelectedTextColor)
            dialog.txt_recent_read_sorting.setTextColor(selectedTextColor)
        } else {
            dialog.txt_update_time_sorting.setTextColor(selectedTextColor)
            dialog.txt_recent_read_sorting.setTextColor(unSelectedTextColor)
        }
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }

}