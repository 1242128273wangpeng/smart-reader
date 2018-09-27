package com.dy.reader.view

import android.content.Context
import android.view.View
import android.view.WindowManager
import com.ding.basic.database.helper.BookDataProviderHelper

import com.dy.reader.R
import com.dy.reader.setting.ReaderStatus
import kotlinx.android.synthetic.qbmfrmxs.popup_reader_option_header_more.view.*
import net.lzbook.kit.app.base.BaseBookApplication

import net.lzbook.kit.ui.widget.base.BasePopup

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

    var addBookMarkListener: (() -> Unit)? = null

    var feedbackListener: (() -> Unit)? = null

    var bookDetailListener: (() -> Unit)? = null

    init {

        contentView.ll_header_more_content.requestFocus()

        contentView.ll_change_source.setOnClickListener {
            dismiss()
            changeSourceListener?.invoke()
        }

        contentView.ll_add_book_mark.setOnClickListener {
            dismiss()
            addBookMarkListener?.invoke()
        }

        contentView.ll_feedback.setOnClickListener {
            dismiss()
            feedbackListener?.invoke()
        }

        contentView.ll_book_detail.setOnClickListener {
            dismiss()
            bookDetailListener?.invoke()
        }
    }

    fun show(view: View) {
        val isMarkPage = BookDataProviderHelper
                .loadBookDataProviderHelper(BaseBookApplication.getGlobalContext())
                .isBookMarkExist(ReaderStatus.book.book_id, ReaderStatus.position.group,
                        ReaderStatus.position.offset)

        val text = if (isMarkPage) context.getString(R.string.remove_bookmark) else
            context.getString(R.string.insert_bookmark)
        contentView.txt_add_book_mark.text = text
        contentView.img_add_book_mark.isSelected = isMarkPage

        showAsDropDown(view)
    }
}