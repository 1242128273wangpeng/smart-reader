package com.intelligent.reader.widget

import android.app.Activity
import android.widget.FrameLayout
import com.intelligent.reader.R
import kotlinx.android.synthetic.txtqbmfyd.dialog_clear_cache.*
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.data.bean.Book


/**
 * Desc 清除缓存
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/1 0002 15:09
 */
class DownloadFinishErrorDialog(val activity: Activity) {

    private val dialog = MyDialog(activity, R.layout.dialog_download_finish_error)

    init {

        val window = dialog.window
        val layoutParams = window.attributes
        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
        layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT
        window.attributes = layoutParams
        window.setWindowAnimations(R.style.BottomPopupDialog)

        dialog.btn_confirm.setOnClickListener {
            dialog.dismiss()
        }
        dialog.btn_cancel.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun show(book: Book) {
        val info = String.format(activity.getString(R.string.dialog_cache_complete_with_err), book.name)
        dialog.txt_dialog_information.text = info
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }

    fun isShowing() :Boolean = dialog.isShowing

}