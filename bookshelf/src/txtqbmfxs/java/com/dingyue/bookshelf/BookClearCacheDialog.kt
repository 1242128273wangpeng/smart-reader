package com.dingyue.bookshelf

import android.app.Activity
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.data.bean.Book

/**
 * Desc 点击删除确定时dialog
 * Author zhenxiang
 * 2018\5\15 0015
 */
class BookClearCacheDialog(activity: Activity) {

    private val dialog = MyDialog(activity, R.layout.dialog_download_clean)
    private val dialog_title = dialog.findViewById(R.id.dialog_msg) as TextView
    init {
        dialog.setCancelable(false)
        dialog_title.setText("清理中...")
    }

    fun show() {
        dialog.show()
    }
    fun dimiss() {
        dialog.dismiss()
    }

    fun isShow(): Boolean{
        return dialog.isShowing
    }


}