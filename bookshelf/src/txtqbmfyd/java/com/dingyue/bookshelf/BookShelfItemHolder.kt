package com.dingyue.bookshelf

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.txtqbmfyd.layout_bookshelf_item_grid.view.*

import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.data.bean.Book

/**
 * Desc 书架页Item
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/5 0002 14:19
 */
class BookShelfItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(book: Book, bookshelfItemListener: BookShelfAdapter.BookShelfItemListener, contains: Boolean, remove: Boolean, update: Boolean) = with(itemView) {

        if (!TextUtils.isEmpty(book.name)) {
            this.txt_book_name.text = book.name
        }

        if (book.sequence + 1 > book.chapter_count) {
            book.sequence = book.chapter_count - 1
        }

        // 是否连载
        if (book.status == 2) {
            this.img_book_state_finish.visibility = View.VISIBLE
        } else {
            this.img_book_state_finish.visibility = View.GONE
        }
        // 是否有更新
        if (!update) {
            this.img_book_state_update.visibility = View.GONE
        } else {
            this.img_book_state_update.visibility = View.VISIBLE
            this.img_book_state_finish.visibility = View.GONE
        }

        if (!TextUtils.isEmpty(book.img_url) && book.img_url != ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL) {
            Glide.with(itemView.context.applicationContext)
                    .load(book.img_url)
                    .placeholder(R.drawable.bookshelf_book_cover_default)
                    .error(R.drawable.bookshelf_book_cover_default)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(this.img_book_cover)
        } else {
            Glide.with(itemView.context.applicationContext)
                    .load(R.drawable.bookshelf_book_cover_default)
                    .into(this.img_book_cover)
        }

        if (remove) {
            this.img_check_delete.visibility = View.VISIBLE
            if (contains) {
                this.img_check_delete.setBackgroundResource(R.drawable.bookshelf_edit_selected_icon)
            } else {
                this.img_check_delete.setBackgroundResource(R.drawable.bookshelf_edit_unselected_icon)
            }
        } else {
            this.img_check_delete.visibility = View.GONE
        }

        book_shelf_item.setOnClickListener {
            bookshelfItemListener.clickedBookShelfItem(book, adapterPosition)
        }

        book_shelf_item.setOnLongClickListener {
            bookshelfItemListener.longClickedBookShelfItem()
        }
    }
}
