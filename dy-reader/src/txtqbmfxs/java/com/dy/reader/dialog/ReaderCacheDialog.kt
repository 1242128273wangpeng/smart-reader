package com.dy.reader.dialog

import android.app.Activity
import android.view.Gravity
import com.dy.reader.R
import kotlinx.android.synthetic.qbzsydq.dialog_reader_cache.*
import net.lzbook.kit.book.view.MyDialog

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/6/9 10:46
 */
class ReaderCacheDialog(activity: Activity) {

    private val dialog = MyDialog(activity, R.layout.dialog_reader_cache, Gravity.BOTTOM)

    var cacheAllListener: (() -> Unit)? = null
    var cacheCancelListener: (() -> Unit)? = null
    var cacheCurrentStartListener: (() -> Unit)? = null

    init {

        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        dialog.reader_cache_all.setOnClickListener {
            cacheAllListener?.invoke()
        }

        dialog.reader_cache_current_start.setOnClickListener {
            cacheCurrentStartListener?.invoke()
        }

        dialog.reader_cache_cancel.setOnClickListener {
            cacheCancelListener?.invoke()
        }
    }

    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }
}