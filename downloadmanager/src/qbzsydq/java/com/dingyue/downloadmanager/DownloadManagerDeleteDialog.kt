package com.dingyue.downloadmanager

import android.app.Activity
import android.widget.FrameLayout
import kotlinx.android.synthetic.qbzsydq.dialog_download_manager_clear.*
import net.lzbook.kit.ui.widget.MyDialog


/**
 * Created by qiantao on 2017/11/22 0022
 */
class DownloadManagerDeleteDialog(activity: Activity) {

    private val dialog = MyDialog(activity, R.layout.dialog_download_manager_clear)

    init {
        dialog.txt_prompt.text = "清理中..."
        dialog.setCancelable(false)
    }

    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }
}