package com.intelligent.reader.view

import android.app.Activity
import com.intelligent.reader.R
import kotlinx.android.synthetic.qbmfkdxs.dialog_book_store.*
import net.lzbook.kit.ui.widget.MyDialog


/**
 * Desc：在精选页增加“全站书籍 永久免费”的弹窗
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/12/7 0007 14:45
 */
class BookStoreDialog(val activity: Activity) : MyDialog(activity, R.layout.dialog_book_store) {

    init {

        setCanceledOnTouchOutside(true)
        setCancelable(true)

        img_close.setOnClickListener {
            dismiss()
        }
    }

}