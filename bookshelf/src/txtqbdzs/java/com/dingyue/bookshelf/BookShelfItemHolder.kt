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

/**
 * Desc 书架页item
 * Author zhenxiang
 * 2018\5\15 0015
 */


class BookShelfItemHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_bookshelf_book, parent, false)) {
    fun bind(book: Book, bookshelfItemListener: BookShelfAdapter.BookShelfItemListener,
             contains: Boolean, remove: Boolean) = with(itemView) {

        if (!TextUtils.isEmpty(book.name))
            this.txt_book_name.text = book.name

        if (!TextUtils.isEmpty(book.author)) {
            txt_book_author.text = book.author
        }

        if (book.sequence + 1 > book.chapter_count) {
            book.sequence = book.chapter_count - 1
        }

        if (book.sequence >= 0) {
            this.txt_book_unread_chapters.text = (book.sequence + 1).toString() + "/" + book.chapter_count + "章"
        } else {
            this.txt_book_unread_chapters.text = "未读"
        }

        // 是否连载
        if (book.status == 2) {
            this.txt_book_states_finish.visibility = View.VISIBLE
        } else {
            this.txt_book_states_finish.visibility = View.GONE
        }
        // 是否有更新
        if (book.update_status != 1) {
            this.txt_book_states_update.visibility = View.GONE
        } else {
            this.txt_book_states_update.visibility = View.VISIBLE
            this.txt_book_states_finish.visibility = View.GONE
        }
        if (this.txt_book_last_update_time != null) {
            this.txt_book_last_update_time.text = Tools.compareTime(AppUtils.formatter, book
                    .last_updatetime_native) + "更新: "
        }

        if (!TextUtils.isEmpty(book.img_url) && book.img_url != ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL) {
            Glide.with(itemView.getContext().getApplicationContext()).load(book.img_url).placeholder(R.drawable.icon_book_cover_default).error(R.drawable.icon_book_cover_default).diskCacheStrategy(DiskCacheStrategy.ALL).into(this.img_book_cover)
        } else {
            Glide.with(itemView.getContext().getApplicationContext()).load(R.drawable.icon_book_cover_default).into(this.img_book_cover)
        }

        this.txt_book_last_chapter.text = book.last_chapter_name


        if (remove) {
            this.img_item_select_state.visibility = View.VISIBLE
            this.txt_book_states_finish.visibility = View.GONE
            this.txt_book_states_update.visibility = View.GONE
            var typeColor = 0
            if (contains) {
                typeColor = R.drawable.bookshelf_delete_checked
            } else {
                typeColor = R.drawable.bookshelf_delete_unchecked
            }
            this.img_item_select_state.setBackgroundResource(typeColor)
        } else {
            this.img_item_select_state.visibility = View.GONE
        }

        rl_main.setOnClickListener {
            bookshelfItemListener.clickedBookShelfItem(book, adapterPosition)
        }

        rl_main.setOnLongClickListener {
            bookshelfItemListener.longClickedBookShelfItem()
        }
    }
}
