package com.dingyue.bookshelf.view

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.dingyue.bookshelf.R
import com.dingyue.contract.CommonContract
import kotlinx.android.synthetic.mfqbxssc.popup_book_sorting.view.*

/**
 * Desc 书籍排序弹窗
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/5/11 0011 18:38
 */
class BookSortingPopup(private val activity: Activity) {

    private val contentView = LayoutInflater.from(activity).inflate(R.layout.popup_book_sorting, null)!!
    private val popupWindow = PopupWindow(contentView)

    var onTimeSortingClickListener: (() -> Unit)? = null
    var onRecentReadSortingClickListener: (() -> Unit)? = null

    init {
        popupWindow.width = LinearLayout.LayoutParams.MATCH_PARENT
        popupWindow.height = LinearLayout.LayoutParams.WRAP_CONTENT
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(-0x50000000))
        popupWindow.isOutsideTouchable = false
        popupWindow.animationStyle = R.style.remove_menu_anim_style

        contentView.rbtn_update_time_sorting.setOnClickListener {
            dismiss()
            onTimeSortingClickListener?.invoke()
        }
        contentView.rbtn_recent_read_sorting.setOnClickListener {
            dismiss()
            onRecentReadSortingClickListener?.invoke()
        }

        popupWindow.setOnDismissListener{
            setBackgroundAlpha(1.0f)
        }

    }

    fun show(view: View) {
        if (CommonContract.queryBookSortingType() == 0) {
            contentView.rbtn_recent_read_sorting.isChecked = true
            contentView.rbtn_update_time_sorting.isChecked = false
        } else {
            contentView.rbtn_update_time_sorting.isChecked = true
            contentView.rbtn_recent_read_sorting.isChecked = false
        }
        setBackgroundAlpha(0.6f)
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0)
    }

    fun dismiss() {
        popupWindow.dismiss()
    }

    private fun setBackgroundAlpha(bgAlpha: Float) {
        val lp = activity.window.attributes
        lp.alpha = bgAlpha
        activity.window.attributes = lp
    }

}