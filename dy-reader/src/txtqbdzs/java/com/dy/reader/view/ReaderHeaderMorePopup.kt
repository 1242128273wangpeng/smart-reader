package com.dy.reader.view

import android.content.Context
import android.view.View
import android.view.WindowManager
import com.ding.basic.database.helper.BookDataProviderHelper
import com.dingyue.contract.BasePopup
import com.dy.reader.R
import com.dy.reader.setting.ReaderStatus
import kotlinx.android.synthetic.txtqbdzs.popup_reader_option_header_more.view.*
import net.lzbook.kit.app.BaseBookApplication

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

    var handleBookmarkListener: (() -> Unit)? = null

    var startBookDetailListener: (() -> Unit)? = null

    var feedbackListener: (() -> Unit)? = null

    init {

        contentView.ll_header_more_content.requestFocus()

        contentView.ll_change_source.setOnClickListener {
            changeSourceListener?.invoke()
        }

        contentView.ll_book_mark.setOnClickListener {
            handleBookmarkListener?.invoke()
        }

        contentView.ll_book_detail.setOnClickListener {
            startBookDetailListener?.invoke()
        }

        contentView.ll_feedback.setOnClickListener {
            feedbackListener?.invoke()
        }
    }

    fun insertBookmarkContent(isToAdd: Boolean) {
        if (isToAdd) {
            contentView.txt_book_mark.text = "添加书签"
            contentView.img_book_mark.setImageResource(R.drawable.reader_option_bookmark_add_icon)
        } else {
            contentView.txt_book_mark.text = "删除书签"
            contentView.img_book_mark.setImageResource(R.drawable.reader_option_bookmark_delete_icon)
        }
    }

    fun show(view: View) {
        val isMarkPage = BookDataProviderHelper.loadBookDataProviderHelper(BaseBookApplication.getGlobalContext()).isBookMarkExist(ReaderStatus.book.book_id, ReaderStatus.position.group, ReaderStatus.position.offset)
        if (isMarkPage) {
            insertBookmarkContent(false)
        } else {
            insertBookmarkContent(true)
        }
        showAsDropDown(view)
    }
}