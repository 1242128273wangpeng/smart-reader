package com.dingyue.bookshelf

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.txtqbmfyd.item_bookshelf_book.view.*

import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.data.bean.Book

/**
 * Desc 书架页Item
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/5 0002 14:19
 */
class BookShelfItemHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_bookshelf_book, parent, false)) {

    fun bind(book: Book, bookshelfItemListener: BookShelfAdapter.BookShelfItemListener,
             contains: Boolean, remove: Boolean) = with(itemView) {

        if (!TextUtils.isEmpty(book.name)) {
            this.txt_book_name.text = book.name
        }

        if (book.sequence + 1 > book.chapter_count) {
            book.sequence = book.chapter_count - 1
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

        if (!TextUtils.isEmpty(book.img_url) && book.img_url != ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL) {
            Glide.with(itemView.context.applicationContext)
                    .load(book.img_url)
                    .placeholder(R.drawable.bookshelf_item_cover_icon)
                    .error(R.drawable.bookshelf_item_cover_icon)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(this.img_book_cover)
        } else {
            Glide.with(itemView.context.applicationContext)
                    .load(R.drawable.bookshelf_item_cover_icon)
                    .into(this.img_book_cover)
        }

        if (remove) {
            this.img_book_select_state.visibility = View.VISIBLE
            if (contains) {
                this.img_book_select_state.setBackgroundResource(R.drawable.bookshelf_item_checked_icon)
            } else {
                this.img_book_select_state.setBackgroundResource(R.drawable.bookshelf_item_check_icon)
            }
        } else {
            this.img_book_select_state.visibility = View.GONE
        }

        rl_book_content.setOnClickListener {
            bookshelfItemListener.clickedBookShelfItem(book, adapterPosition)
        }

        rl_book_content.setOnLongClickListener {
            bookshelfItemListener.longClickedBookShelfItem()
        }
    }
}