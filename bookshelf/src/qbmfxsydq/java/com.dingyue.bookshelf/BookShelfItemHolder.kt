package com.dingyue.bookshelf

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ding.basic.bean.Book
import kotlinx.android.synthetic.qbmfxsydq.item_bookshelf_book.view.*
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.Tools
import net.lzbook.kit.utils.book.RepairHelp
import java.text.MessageFormat

/**
 * Created by Administrator on 2017/4/13 0013
 */
class BookShelfItemHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_bookshelf_book, parent, false)) {

    fun bind(book: Book, bookshelfItemListener: BookShelfAdapter.BookShelfItemListener,
             contains: Boolean, isRemove: Boolean) = with(itemView) {

        if (!TextUtils.isEmpty(book.name)) {
            txt_book_name.text = book.name
        }

        if (!TextUtils.isEmpty(book.author)) {
            txt_book_author.text = book.author
        }

        if (book.sequence + 1 > book.chapter_count) {
            book.sequence = book.chapter_count - 1
        }

        if (book.sequence >= 0) {
            txt_read_progress.text = MessageFormat.format("{0}/{1}章", book.sequence + 1, book.chapter_count)
        } else {
            txt_read_progress.text = "未读"
        }

        val sp = BaseBookApplication.getGlobalContext().getSharedPreferences(Constants.SHAREDPREFERENCES_KEY, 0)
        if (RepairHelp.isShowFixBtn(context, book.book_id) && sp.getBoolean(book.book_id, true)) {
            img_book_update.visibility = View.VISIBLE
            txt_read_progress.text = "章节已修复至最新"
        } else {
            img_book_update.visibility = View.GONE

            // 是否连载
            if (book.status.equals("FINISH")) {
                img_book_status.visibility = View.VISIBLE
            } else {
                img_book_status.visibility = View.GONE
            }
            // 是否有更新
            if (book.update_status != 1) {
                img_book_update.visibility = View.GONE
            } else {
                img_book_update.visibility = View.VISIBLE
                img_book_status.visibility = View.GONE
            }
        }

        if (txt_book_update_time != null && book.last_chapter != null) {
            txt_book_update_time.text = MessageFormat.format("{0}更新:", Tools.compareTime(AppUtils.formatter, book.last_chapter!!.update_time))
        }

        if (!TextUtils.isEmpty(book.img_url) && !book.img_url.equals(
                        ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL)) {
            Glide.with(itemView.context.applicationContext)
                    .load(book.img_url).placeholder(R.drawable.icon_book_cover_default)
                    .error(R.drawable.icon_book_cover_default)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(img_book_cover)
        } else {
            Glide.with(itemView.context.applicationContext)
                    .load(R.drawable.icon_book_cover_default)
                    .into(img_book_cover)
        }


        //显示最后一个章节
        txt_book_last_chapter.text = book.last_chapter?.name ?: ""


        if (isRemove) {
            img_book_select_state.visibility = View.VISIBLE
            img_book_status.visibility = View.GONE
            img_book_update.visibility = View.GONE
            if (contains) {
                img_book_select_state.setBackgroundResource(R.drawable.bookshelf_delete_check_selected_icon)
            } else {
                img_book_select_state.setBackgroundResource(R.drawable.bookshelf_delete_check_select_icon)
            }
        } else {
            img_book_select_state.visibility = View.GONE
        }

        itemView.setOnClickListener {
            bookshelfItemListener.clickedBookShelfItem(book, adapterPosition)
        }

        itemView.setOnLongClickListener {
            bookshelfItemListener.longClickedBookShelfItem()
        }
    }
}
