package com.dy.media

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_reader_rest.*

/**
 * Desc 阅读页休息广告
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/6/8 10:56
 */
class ReaderRestDialog(private val activity: Activity) {

    val dialog = Dialog(activity, R.style.custom_dialog)

    init {
        dialog.setContentView(R.layout.dialog_reader_rest)
        val window = dialog.window
        val params = window.attributes
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        params.gravity = Gravity.CENTER
        window.attributes = params
        dialog.setCanceledOnTouchOutside(false)

        dialog.img_close.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun show(view: View?) {
        //广告 3-1
        dialog.rl_ad.removeAllViews()
        dialog.rl_ad.addView(view)
        dialog.rl_ad.postInvalidate()
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }

    fun isShowing() = dialog.isShowing

}