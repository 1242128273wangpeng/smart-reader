package com.intelligent.reader.widget

import android.app.Activity
import android.widget.FrameLayout
import com.intelligent.reader.R
import kotlinx.android.synthetic.txtqbmfyd.dialog_clear_cache.*
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.data.bean.BookTask


/**
 * Desc 清除缓存
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/1 0002 15:09
 */
class DownloadWaitingWifiDialog(val activity: Activity) {

    private val dialog = MyDialog(activity, R.layout.dialog_download_finish_error)

    private var list: List<BookTask>? = null

    private var confirmListener: ((list: List<BookTask>?) -> Unit)? = null

    init {

        val window = dialog.window
        val layoutParams = window.attributes
        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
        layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT
        window.attributes = layoutParams
        window.setWindowAnimations(R.style.BottomPopupDialog)

        dialog.btn_confirm.setOnClickListener {
            dialog.dismiss()
            confirmListener?.invoke(list)
        }

        dialog.btn_cancel.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun show(list: List<BookTask>) {
        this.list = list
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }

    fun setOnConfirmListener(listener: ((list: List<BookTask>?) -> Unit)) {
        confirmListener = listener
    }

    fun isShowing(): Boolean = dialog.isShowing

}