package com.intelligent.reader.widget

import android.app.Activity
import android.view.Gravity
import com.intelligent.reader.R
import kotlinx.android.synthetic.txtqbmfyd.dialog_clear_cache.*
import net.lzbook.kit.bean.BookTask
import net.lzbook.kit.widget.MyDialog


/**
 * Desc 非 wifi 环境下载提醒
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/1 0002 15:09
 */
class DownloadWaitingWifiDialog(val activity: Activity) {

    private val dialog = MyDialog(activity, R.layout.dialog_wifi_warning, Gravity.BOTTOM)

    private var list: List<BookTask>? = null

    private var confirmListener: ((list: List<BookTask>?) -> Unit)? = null

    init {

        val window = dialog.window
        window.setWindowAnimations(R.style.BottomPopupDialog)

        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

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