package com.intelligent.reader.widget

import android.app.Activity
import android.widget.FrameLayout
import com.intelligent.reader.R
import kotlinx.android.synthetic.txtqbmfyd.dialog_book_sorting.*
import net.lzbook.kit.book.view.MyDialog


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
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }

}