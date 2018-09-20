package com.dingyue.bookshelf.view

import android.app.Activity
import android.view.View
import android.view.WindowManager
import com.dingyue.bookshelf.R
import kotlinx.android.synthetic.qbmfkdxs.popup_book_sorting.view.*
import net.lzbook.kit.base.BasePopup
import net.lzbook.kit.utils.book.CommonContract

/**
 * Desc 书籍排序弹窗
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/5/11 0011 18:38
 */
class BookShelfSortingPopup(private val activity: Activity, layout: Int = R.layout.popup_book_sorting,
                            width: Int = WindowManager.LayoutParams.MATCH_PARENT,
                            height: Int = WindowManager.LayoutParams.WRAP_CONTENT)
    : BasePopup(activity, layout, width, height) {

    var onTimeSortingClickListener: (() -> Unit)? = null
    var onRecentReadSortingClickListener: (() -> Unit)? = null

    init {
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = false
        popupWindow.animationStyle = R.style.remove_menu_anim_style

        contentView.rbtn_sort_update.setOnClickListener {
            dismiss()
            onTimeSortingClickListener?.invoke()
        }
        contentView.rbtn_sort_read.setOnClickListener {
            dismiss()
            onRecentReadSortingClickListener?.invoke()
        }

        popupWindow.setOnDismissListener{
            setBackgroundAlpha(1.0f)
        }

    }

    fun show(view: View) {
        if (CommonContract.queryBookSortingType() == 0) {
            contentView.rbtn_sort_read.isChecked = true
            contentView.rbtn_sort_update.isChecked = false
        } else {
            contentView.rbtn_sort_update.isChecked = true
            contentView.rbtn_sort_read.isChecked = false
        }
        setBackgroundAlpha(0.6f)
        showAtLocation(view)
    }

    private fun setBackgroundAlpha(bgAlpha: Float) {
        val lp = activity.window.attributes
        lp.alpha = bgAlpha
        activity.window.attributes = lp
    }

}