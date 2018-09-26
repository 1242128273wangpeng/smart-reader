package com.intelligent.reader.view

import android.app.Activity
import android.view.Gravity
import android.view.View
import com.intelligent.reader.R
import kotlinx.android.synthetic.qbmfkdxs.dialog_clear_cache.*
import net.lzbook.kit.ui.widget.MyDialog


/**
 * Function：清除缓存
 *
 * Created by JoannChen on 2018/9/12 0012 14:19
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
class ClearCacheDialog(val activity: Activity) {

    private val dialog = MyDialog(activity, R.layout.dialog_clear_cache, Gravity.CENTER)

    private var confirmListener: (() -> Unit)? = null
    private var cancelListener: (() -> Unit)? = null

    init {

        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        dialog.btn_confirm.setOnClickListener {
            confirmListener?.invoke()
        }
        dialog.btn_cancel.setOnClickListener {
            dialog.dismiss()
            cancelListener?.invoke()
        }
    }

    fun showLoading() {
        dialog.txt_dialog_title.text = activity.getString(R.string.tip_cleaning_cache)
        dialog.txt_dialog_information.visibility = View.INVISIBLE
        dialog.view_divider.visibility = View.INVISIBLE
        dialog.ll_btn.visibility = View.INVISIBLE
        dialog.pgbar_loading.visibility = View.VISIBLE
    }

    fun setOnConfirmListener(listener: () -> Unit) {
        confirmListener = listener
    }

    fun setOnCancelListener(listener: () -> Unit) {
        dialog.dismiss()
        cancelListener = listener
    }

    fun show() {
        dialog.txt_dialog_title.text = activity.getString(R.string.prompt)
        dialog.txt_dialog_information.visibility = View.VISIBLE
        dialog.view_divider.visibility = View.VISIBLE
        dialog.ll_btn.visibility = View.VISIBLE
        dialog.pgbar_loading.visibility = View.GONE
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }

}