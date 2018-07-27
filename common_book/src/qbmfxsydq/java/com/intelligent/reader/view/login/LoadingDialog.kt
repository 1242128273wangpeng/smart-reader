package com.intelligent.reader.view.login

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.intelligent.reader.R
import kotlinx.android.synthetic.qbmfxsydq.popup_login_loading.view.*


/**
 * Desc 登录中 popup
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/30 0030 11:48
 */
class LoadingDialog(context: Context) {

    private val contentView = LayoutInflater.from(context).inflate(R.layout.popup_login_loading, null)
    private val dialog = Dialog(context, R.style.TransparentBgDialog)

    init {
        dialog.setContentView(contentView)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        val window = dialog.window
        val params = window.attributes
        params.dimAmount = 0.0f
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        window.attributes = params

    }

    fun show(title: String? = null) {
        title?.let {
            contentView.txt_title.text = it
        }
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    fun dismiss() {
        dialog.dismiss()
    }

}