package com.dingyue.bookshelf


import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.txtqbdzs.item_bookshelf_book.view.*
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.Tools
import android.view.ViewGroup
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
        
        if (!TextUtils.isEmpty(book.author)) {
            txt_book_author.text = book.author
        }

        if (book.sequence + 1 > book.chapter_count) {
            book.sequence = book.chapter_count - 1
        }

        if (book.sequence >= 0) {
            txt_book_unread_chapters.text = MessageFormat.format("{0}/{1}章", book.sequence + 1, book.chapter_count)
        } else {
            txt_book_unread_chapters.text = "未读"
        }

        // 是否连载
        if (book.status == 2) {
            txt_book_states_finish.visibility = View.VISIBLE
        } else {
            txt_book_states_finish.visibility = View.GONE
        }
        // 是否有更新
        if (book.update_status != 1) {
            txt_book_states_update.visibility = View.GONE
        } else {
            txt_book_states_update.visibility = View.VISIBLE
            txt_book_states_finish.visibility = View.GONE
        }
        if (txt_book_last_update_time != null) {
            txt_book_last_update_time.text = Tools.compareTime(AppUtils.formatter, book
                    .last_updatetime_native) + "更新: "
        }

        if (!TextUtils.isEmpty(book.img_url) && book.img_url != ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL) {
            Glide.with(itemView.context.applicationContext)
                    .load(book.img_url)
                    .placeholder(R.drawable.bookshelf_item_book_cover_icon)
                    .error(R.drawable.bookshelf_item_book_cover_icon)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(img_book_cover)
        } else {
            Glide.with(itemView.context.applicationContext)
                    .load(R.drawable.bookshelf_item_book_cover_icon)
                    .into(img_book_cover)
        }

        txt_book_last_chapter.text = book.last_chapter_name

        if (remove) {
            img_item_select_state.visibility = View.VISIBLE
            txt_book_states_finish.visibility = View.GONE
            txt_book_states_update.visibility = View.GONE

            if (contains) {
                img_item_select_state.setImageResource(R.drawable.bookshelf_item_book_checked_icon)
            } else {
                img_item_select_state.setImageResource(R.drawable.bookshelf_item_book_check_icon)
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