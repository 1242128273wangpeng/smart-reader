package com.dingyue.bookshelf

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.Tools
import kotlinx.android.synthetic.zsmfqbxs.layout_bookshelf_item_grid.view.*

/**
 * Desc 书架Item
 * Created by zhenxiang
 * on 2018/5/12 0013.
 */

class BookShelfItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(book: Book, bookshelfItemListener: BookShelfAdapter.BookShelfItemListener,
             contains: Boolean, remove: Boolean) = with(itemView) {
        if (!TextUtils.isEmpty(book.name)) {
            txt_book_name.setText(book.name)
        }

        if (book.sequence + 1 > book.chapter_count) {
            book.sequence = book.chapter_count - 1
        }

        if (book.sequence + 1 == book.chapter_count) {
            txt_book_unread_chapters.setVisibility(View.GONE)
        } else {
            txt_book_unread_chapters.setVisibility(View.VISIBLE)
            txt_book_unread_chapters.setText((book.sequence + 1).toString() + "/" + book.chapter_count + "章")
        }

        when {
            book.update_status == 1 -> { //更新
                img_book_status_update.visibility = View.VISIBLE
            }
            book.status == 2 -> { //完结
                img_book_status.visibility = View.VISIBLE
                img_book_status.setImageResource(R.drawable.bookshelf_book_status_finish)
            }
            else -> {
                img_book_status.visibility = View.GONE
                img_book_status_update.visibility = View.GONE
            }
        }




        if (txt_book_last_update_time != null) {
            txt_book_last_update_time.setText(Tools.compareTime(AppUtils.formatter, book
                    .last_updatetime_native) + "更新")
        }

        if ((!TextUtils.isEmpty(book.img_url) && book.img_url != ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL)) {
            Glide.with(itemView.getContext().getApplicationContext()).load(
                    book.img_url).placeholder(R.drawable.icon_book_cover_default).error(
                    R.drawable.icon_book_cover_default).diskCacheStrategy(
                    DiskCacheStrategy.ALL).into(img_book_cover)
        } else {
            Glide.with(itemView.getContext().getApplicationContext()).load(
                    R.drawable.icon_book_cover_default).into(img_book_cover)
        }


        if (remove) {
            this.img_item_select_state.setVisibility(View.VISIBLE)
            this.img_book_status.setVisibility(View.GONE)
            this.img_book_status_update.setVisibility(View.GONE)
            var typeColor = 0
            if (contains) {
                typeColor = R.drawable.bookshelf_delete_checked
            } else {
                typeColor = R.drawable.bookshelf_delete_unchecked
            }
            this.img_item_select_state.setBackgroundResource(typeColor)
        } else {
            this.img_item_select_state.setVisibility(View.GONE)
        }

        rl_content.setOnClickListener {
            bookshelfItemListener.clickedBookShelfItem(book, adapterPosition)
        }

        rl_content.setOnLongClickListener {
            bookshelfItemListener.longClickedBookShelfItem()
        }
    }
}
