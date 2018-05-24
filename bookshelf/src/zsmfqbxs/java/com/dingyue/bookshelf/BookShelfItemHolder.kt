package com.dingyue.bookshelf

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import android.view.ViewGroup
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.Tools
import kotlinx.android.synthetic.zsmfqbxs.item_bookshelf_book.view.*

/**
 * Desc 书架Item
 * Created by zhenxiang
 * on 2018/5/12 0013.
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

        if (book.sequence + 1 == book.chapter_count) {
            txt_book_unread_chapters.visibility = View.GONE
        } else {
            txt_book_unread_chapters.visibility = View.VISIBLE
            txt_book_unread_chapters.text = (book.sequence + 1).toString() + "/" + book.chapter_count + "章"
        }

        when {
            book.update_status == 1 -> { //更新
                img_book_status_update.visibility = View.VISIBLE
            }
            book.status == 2 -> { //完结
                img_book_status.visibility = View.VISIBLE
                img_book_status.setImageResource(R.drawable.bookshelf_item_book_status_finish_icon)
            }
            else -> {
                img_book_status.visibility = View.GONE
                img_book_status_update.visibility = View.GONE
            }
        }

        if (txt_book_last_update_time != null) {
            txt_book_last_update_time.text = Tools.compareTime(AppUtils.formatter, book
                    .last_updatetime_native) + "更新"
        }

        if ((!TextUtils.isEmpty(book.img_url) && book.img_url != ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL)) {
            Glide.with(itemView.context.applicationContext)
                    .load(book.img_url)
                    .placeholder(R.drawable.common_book_cover_default_icon)
                    .error(R.drawable.common_book_cover_default_icon)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(img_book_cover)
        } else {
            Glide.with(itemView.context.applicationContext)
                    .load(R.drawable.common_book_cover_default_icon)
                    .into(img_book_cover)
        }


        if (remove) {
            img_item_select_state.visibility = View.VISIBLE
            img_book_status.visibility = View.GONE
            img_book_status_update.visibility = View.GONE

            if (contains) {
                img_item_select_state.setImageResource(R.drawable.bookshelf_item_delete_checked_icon)
            } else {
                img_item_select_state.setImageResource(R.drawable.bookshelf_item_delete_unchecked_icon)
            }
        } else {
            img_item_select_state.visibility = View.GONE
        }

        rl_main.setOnClickListener {
            bookshelfItemListener.clickedBookShelfItem(book, adapterPosition)
        }

        rl_main.setOnLongClickListener {
            bookshelfItemListener.longClickedBookShelfItem()
        }
    }
}
