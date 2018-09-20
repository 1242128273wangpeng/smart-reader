package com.dy.reader.view

import android.content.Context
import android.view.WindowManager
import net.lzbook.kit.base.BasePopup
import com.dy.reader.R
import kotlinx.android.synthetic.txtqbmfyd.popup_reader_option_header_more.view.*

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/6/9 10:22
 */
class ReaderHeaderMorePopup(context: Context, layout: Int = R.layout.popup_reader_option_header_more,
                            width: Int = WindowManager.LayoutParams.WRAP_CONTENT,
                            height: Int = WindowManager.LayoutParams.WRAP_CONTENT)
    : BasePopup(context, layout, width, height) {

    var changeSourceListener: (() -> Unit)? = null

    var startFeedbackListener: (() -> Unit)? = null

    var startBookDetailListener: (() -> Unit)? = null

    init {

        contentView.ll_header_more_content.requestFocus()

        contentView.txt_reader_change_source.setOnClickListener {
            changeSourceListener?.invoke()
        }

        contentView.txt_reader_feedback.setOnClickListener {
            startFeedbackListener?.invoke()
        }

        contentView.txt_reader_book_detail.setOnClickListener {
            startBookDetailListener?.invoke()
        }
    }

    fun insertBookmarkContent(string: String) {
        contentView.txt_reader_feedback.text = string
    }
}