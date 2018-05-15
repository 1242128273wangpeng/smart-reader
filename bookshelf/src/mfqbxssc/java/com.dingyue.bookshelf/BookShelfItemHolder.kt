package com.dingyue.bookshelf

import android.support.v7.widget.RecyclerView
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.mfqbxssc.item_bookshelf_book.view.*
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.data.bean.Book

/**
 * Desc 书架页Item
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/5/12 0002 14:19
 */
class BookShelfItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(book: Book, bookshelfItemListener: BookShelfAdapter.BookShelfItemListener,
             contains: Boolean, remove: Boolean) = with(itemView) {

        if (book.name.isNotEmpty()) book_shelf_name.text = book.name

        when {
            book.update_status == 1 -> { //更新
                img_book_status.visibility = View.VISIBLE
                img_book_status.setImageResource(R.drawable.book_new)
            }
            book.status == 2 -> { //完结
                img_book_status.visibility = View.VISIBLE
                img_book_status.setImageResource(R.drawable.book_end)
            }
            else -> img_book_status.visibility = View.GONE
        }


        if (book.img_url.isNotEmpty() && book.img_url != ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL) {
            Glide.with(itemView.context)
                    .load(book.img_url)
                    .placeholder(R.drawable.icon_book_cover_default)
                    .error(R.drawable.icon_book_cover_default)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(img_book_cover)
        } else {
            Glide.with(itemView.context)
                    .load(R.drawable.icon_book_cover_default)
                    .into(img_book_cover)
        }

        if (remove) {
            check_delete.visibility = View.VISIBLE
            if (contains) {
                check_delete.setBackgroundResource(R.drawable.edit_bookshelf_selected)
            } else {
                check_delete.setBackgroundResource(R.drawable.edit_bookshelf_unselected)
            }
        } else {
            check_delete.visibility = View.GONE
        }

        rl_book_content.setOnClickListener {
            bookshelfItemListener.clickedBookShelfItem(book, adapterPosition)
        }

        rl_book_content.setOnLongClickListener {
            bookshelfItemListener.longClickedBookShelfItem()
        }

    }
}
