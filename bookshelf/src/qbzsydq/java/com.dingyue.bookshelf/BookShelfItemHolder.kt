package com.dingyue.bookshelf

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.qbzsydq.item_bookshelf_book.view.*
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.Tools

/**
 * Created by Administrator on 2017/4/13 0013
 */

class BookShelfItemHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_bookshelf_book, parent, false)) {

    fun bind(book: Book, bookshelfItemListener: BookShelfAdapter.BookShelfItemListener,
             contains: Boolean, isRemove: Boolean) = with(itemView) {

        if (book.img_url.isNotEmpty() && book.img_url != ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL) {
            Glide.with(itemView.context.applicationContext)
                    .load(book.img_url)
                    .placeholder(R.drawable.icon_book_cover_default)
                    .error(R.drawable.icon_book_cover_default)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(img_book_cover)
        } else {
            Glide.with(itemView.context.applicationContext)
                    .load(R.drawable.icon_book_cover_default)
                    .into(img_book_cover)
        }

        if (book.name.isNotEmpty()) {
            txt_book_name.text = book.name
        }

        if (book.last_chapter_name != null) {

            val count: Int = if (book.sequence <= 0) {
                book.chapter_count
            } else {
                book.chapter_count - (book.sequence + 1)
            }

            if (count > 0) {
                txt_book_unread_chapters.visibility = View.VISIBLE
                img_book_unread_chapters.visibility = View.VISIBLE
                val unread = "${count}章"
                txt_book_unread_chapters.text = unread
            } else {
                txt_book_unread_chapters.visibility = View.GONE
                img_book_unread_chapters.visibility = View.GONE
            }

            txt_book_latest_chapter.text = book.last_chapter_name

            val updateTime = "${Tools.compareTime(AppUtils.formatter, book.last_updatetime_native)}更新"
            txt_book_last_update_time.text = updateTime
        }

        when {
            book.update_status == 1 -> { //更新
                img_book_status.visibility = View.VISIBLE
                img_book_status.setImageResource(R.drawable.bookshelf_item_book_update_icon)
            }
            book.status == 2 -> { //完结
                img_book_status.visibility = View.VISIBLE
                img_book_status.setImageResource(R.drawable.bookshelf_item_book_finish_icon)
            }
            else -> img_book_status.visibility = View.GONE
        }

        if (isRemove) {
            img_book_select_state.visibility = View.VISIBLE
            img_book_status.visibility = View.GONE
            if (contains) {
                img_book_select_state.setBackgroundResource(R.drawable.bookshelf_item_selected_icon)
            } else {
                img_book_select_state.setBackgroundResource(R.drawable.bookshelf_item_unselected_icon)
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
