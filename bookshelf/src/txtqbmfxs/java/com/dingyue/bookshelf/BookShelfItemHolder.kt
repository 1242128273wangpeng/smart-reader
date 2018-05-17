package com.dingyue.bookshelf

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.txtqbmfxs.item_bookshelf_book.view.*
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.Tools
import java.text.MessageFormat

/**
 * Desc 书架页item
 * Author zhenxiang
 * 2018\5\15 0015
 */
class BookShelfItemHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_bookshelf_book, parent, false)) {

    fun bind(book: Book, bookshelfItemListener: BookShelfAdapter.BookShelfItemListener,
             contains: Boolean, remove: Boolean) = with(itemView) {

        if (!TextUtils.isEmpty(book.name)) {
            txt_book_name.text = book.name
        }

        if (book.sequence + 1 > book.chapter_count) {
            book.sequence = book.chapter_count - 1
        }

        if (book.sequence >= 0) {
            txt_book_read_progress.text = MessageFormat
                    .format("{0}/{1}章", book.sequence + 1, book.chapter_count)
        } else {
            txt_book_read_progress.text = "未读"
        }

        if (book.status == 2) {
            txt_book_chapter.text = "已完结"
        } else {
            if (!TextUtils.isEmpty(book.last_chapter_name)) {
                txt_book_chapter.text = book.last_chapter_name
            }
        }

        if (book.update_status != 1) {
            img_book_update.visibility = View.GONE
        } else {
            img_book_update.visibility = View.VISIBLE
        }

        if (txt_book_update_time != null) {
            txt_book_update_time.text = Tools
                    .compareTime(AppUtils.formatter, book.last_updatetime_native)
        }

        if (!TextUtils.isEmpty(book.img_url) && book.img_url != ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL) {
            Glide.with(itemView.context.applicationContext)
                    .load(book.img_url)
                    .placeholder(R.drawable.bookshelf_item_cover_icon)
                    .error(R.drawable.bookshelf_item_cover_icon)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(img_book_cover)
        } else {
            Glide.with(itemView.context.applicationContext)
                    .load(R.drawable.bookshelf_item_cover_icon)
                    .into(img_book_cover)
        }

        if (remove) {
            img_book_select_state.visibility = View.VISIBLE
            if (contains) {
                img_book_select_state.setImageResource(R.drawable.bookshelf_item_checked_icon)
            } else {
                img_book_select_state.setImageResource(R.drawable.bookshelf_item_check_icon)
            }
        } else {
            img_book_select_state.visibility = View.GONE
        }

        rl_book_content.setOnClickListener {
            bookshelfItemListener.clickedBookShelfItem(book, adapterPosition)
        }

        rl_book_content.setOnLongClickListener {
            bookshelfItemListener.longClickedBookShelfItem()
        }
    }
}