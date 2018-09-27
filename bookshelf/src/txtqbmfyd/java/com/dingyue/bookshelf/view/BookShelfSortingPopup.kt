package com.dingyue.bookshelf.view

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.WindowManager
import com.dingyue.bookshelf.BookShelfLogger
import com.dingyue.bookshelf.R
import net.lzbook.kit.ui.widget.base.BasePopup
import net.lzbook.kit.utils.book.CommonContract
import kotlinx.android.synthetic.txtqbmfyd.dialog_bookshelf_sort.view.*

/**
 * Desc 书籍排序
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/5/17 0017 11:43
 */
class BookShelfSortingPopup(private val activity: Activity, layout: Int = R.layout.dialog_bookshelf_sort,
                            width: Int = WindowManager.LayoutParams.MATCH_PARENT,
                            height: Int = WindowManager.LayoutParams.WRAP_CONTENT)
    : BasePopup(activity, layout, width, height) {

    private var recentReadListener: (() -> Unit)? = null
    private var updateTimeListener: (() -> Unit)? = null

    private val selectTextColor = Color.parseColor("#212832")
    private val selectedTextColor = Color.parseColor("#19DD8B")

    init {

        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(-0x50000000))
        popupWindow.isOutsideTouchable = false
        popupWindow.animationStyle = R.style.BottomPopupDialog

        contentView.txt_sort_read.setOnClickListener {
            recentReadListener?.invoke()
            popupWindow.dismiss()
        }
        contentView.txt_sort_update.setOnClickListener {
            updateTimeListener?.invoke()
            popupWindow.dismiss()
        }
        contentView.txt_sort_cancel.setOnClickListener {
            popupWindow.dismiss()
            BookShelfLogger.uploadBookShelfSortCancel()
        }

        popupWindow.setOnDismissListener {
            setBackgroundAlpha(1.0f)
        }
    }

    fun setOnRecentReadClickListener(listener: () -> Unit) {
        recentReadListener = listener
    }

    fun setOnUpdateTimeClickListener(listener: () -> Unit) {
        popupWindow.dismiss()
        updateTimeListener = listener
    }

    fun show(view: View) {
        if (CommonContract.queryBookSortingType() == 0) {
            contentView.txt_sort_update.setTextColor(selectTextColor)
            contentView.txt_sort_read.setTextColor(selectedTextColor)
        } else {
            contentView.txt_sort_update.setTextColor(selectedTextColor)
            contentView.txt_sort_read.setTextColor(selectTextColor)
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