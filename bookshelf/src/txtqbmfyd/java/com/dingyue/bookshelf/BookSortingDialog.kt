package com.dingyue.bookshelf

import android.app.Activity
import android.graphics.Color
import android.view.Gravity
import com.dingyue.contract.CommonContract
import kotlinx.android.synthetic.txtqbmfyd.dialog_bookshelf_sort.*
import net.lzbook.kit.book.view.MyDialog


/**
 * Desc 清除缓存
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/1 0002 15:09
 */
class BookSortingDialog(val activity: Activity) {

    private val dialog = MyDialog(activity, R.layout.dialog_bookshelf_sort, Gravity.BOTTOM)

    private var recentReadListener: (() -> Unit)? = null
    private var updateTimeListener: (() -> Unit)? = null

    private val selectTextColor = Color.parseColor("#212832")
    private val selectedTextColor = Color.parseColor("#19DD8B")

    init {

        val window = dialog.window
        window.setWindowAnimations(R.style.BottomPopupDialog)

        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        dialog.txt_sort_read.setOnClickListener {
            recentReadListener?.invoke()
            dialog.dismiss()
        }
        dialog.txt_sort_update.setOnClickListener {
            updateTimeListener?.invoke()
            dialog.dismiss()
        }
        dialog.txt_sort_cancel.setOnClickListener {
            dialog.dismiss()
            BookShelfLogger.uploadBookShelfSortCancel()
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
        if (CommonContract.queryBookSortingType() == 0) {
            dialog.txt_sort_update.setTextColor(selectTextColor)
            dialog.txt_sort_read.setTextColor(selectedTextColor)
        } else {
            dialog.txt_sort_update.setTextColor(selectedTextColor)
            dialog.txt_sort_read.setTextColor(selectTextColor)
        }
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }

}