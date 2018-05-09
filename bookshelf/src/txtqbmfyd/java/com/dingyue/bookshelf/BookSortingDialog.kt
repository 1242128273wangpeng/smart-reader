package com.dingyue.bookshelf

import android.app.Activity
import android.graphics.Color
import android.view.Gravity
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

    private val dialog = MyDialog(activity, R.layout.dialog_book_sorting, Gravity.BOTTOM)

    private var recentReadListener: (() -> Unit)? = null
    private var updateTimeListener: (() -> Unit)? = null

    private val settingItemsHelper = SettingItemsHelper.getSettingHelper(activity)

    private val selectedTextColor = Color.parseColor("#19DD8B")
    private val unSelectedTextColor = Color.parseColor("#212832")

    init {

        val window = dialog.window
        window.setWindowAnimations(R.style.BottomPopupDialog)

        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

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