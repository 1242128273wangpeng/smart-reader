package com.dingyue.bookshelf

import android.support.v7.widget.RecyclerView
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.qbzsydq.layout_bookshelf_item_grid.view.*
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.Tools

/**
 * Created by Administrator on 2017/4/13 0013
 */

class BookShelfItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(book: Book, bookshelfItemListener: BookShelfAdapter.BookShelfItemListener,
             contains: Boolean, isRemove: Boolean) = with(itemView) {

        if (book.img_url.isNotEmpty() && book.img_url != ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL) {
            Glide.with(itemView.context.applicationContext)
                    .load(book.img_url)
                    .placeholder(R.drawable.icon_book_cover_default)
                    .error(R.drawable.icon_book_cover_default)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(book_shelf_image)
        } else {
            Glide.with(itemView.context.applicationContext)
                    .load(R.drawable.icon_book_cover_default)
                    .into(book_shelf_image)
        }

        if (book.name.isNotEmpty()) {
            book_shelf_name.text = book.name
        }

        if (book.last_chapter_name != null) {

            val count: Int = if (book.sequence <= 0) {
                book.chapter_count
            } else {
                book.chapter_count - (book.sequence + 1)
            }

            if (count > 0) {
                rl_unread.visibility = View.VISIBLE
                val unread = "${count}章"
                book_shelf_unread.text = unread
            } else {
                rl_unread.visibility = View.GONE
            }

            book_shelf_last_chapter.text = book.last_chapter_name

            val updateTime = "${Tools.compareTime(AppUtils.formatter, book.last_checkupdatetime)}更新"
            book_shelf_update_time.text = updateTime
        }

        when {
            book.update_status == 1 -> { //更新
                img_book_status.visibility = View.VISIBLE
                img_book_status.setImageResource(R.drawable.bookshelf_book_status_update)
            }
            book.status == 2 -> { //完结
                img_book_status.visibility = View.VISIBLE
                img_book_status.setImageResource(R.drawable.bookshelf_book_status_finish)
            }
            else -> img_book_status.visibility = View.GONE
        }

        if (isRemove) {
            check_delete.visibility = View.VISIBLE
            img_book_status.visibility = View.GONE
            if (contains) {
                check_delete.setBackgroundResource(R.drawable.bookshelf_delete_checked)
            } else {
                check_delete.setBackgroundResource(R.drawable.bookshelf_delete_unchecked)
            }
        } else {
            check_delete.visibility = View.GONE
        }

        book_shelf_item.setOnClickListener {
            bookshelfItemListener.clickedBookShelfItem(book, adapterPosition)
        }

        book_shelf_item.setOnLongClickListener {
            bookshelfItemListener.longClickedBookShelfItem()
        }

    }
}
