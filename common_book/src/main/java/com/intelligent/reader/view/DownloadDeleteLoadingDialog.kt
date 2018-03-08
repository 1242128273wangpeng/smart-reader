package com.intelligent.reader.view

import android.app.Activity
import android.widget.FrameLayout
import com.intelligent.reader.R
import net.lzbook.kit.book.view.MyDialog

/**
 * Created by qiantao on 2017/11/22 0022
 */
class DownloadDeleteLoadingDialog(activity: Activity) {

    private val dialog = MyDialog(activity, R.layout.dialog_download_clear_loading)

    init {
        val window = dialog.window
        val layoutParams = window.attributes
        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
        layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT
        window.attributes = layoutParams
        window.setWindowAnimations(R.style.BottomPopupDialog)
    }
    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }

}