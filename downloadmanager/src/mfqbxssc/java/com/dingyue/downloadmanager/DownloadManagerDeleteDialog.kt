package com.dingyue.downloadmanager

import android.app.Activity
import kotlinx.android.synthetic.mfqbxssc.dialog_download_manager_clear.*
import net.lzbook.kit.ui.widget.MyDialog

/**
 * Created by qiantao on 2017/11/22 0022
 */
class DownloadManagerDeleteDialog(activity: Activity) {

    private val dialog = MyDialog(activity, R.layout.dialog_download_manager_clear)

    init {
        dialog.txt_prompt.text = "正在清理缓存中..."
        dialog.setCancelable(false)
    }

    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }
}