package com.dingyue.bookshelf

import android.support.v7.widget.RecyclerView
import android.view.View
import com.ding.basic.bean.Book
import kotlinx.android.synthetic.main.item_bookshelf_add.view.*

/**
 * Desc 书架添加书籍Item
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/5/10 21:07
 */
class BookShelfADDHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(book: Book?, remove: Boolean, bookshelfItemListener: BookShelfAdapter.BookShelfItemListener) = with(itemView) {

        if (remove) {
            itemView.rl_content.visibility = View.INVISIBLE
        } else {
            itemView.rl_content.visibility = View.VISIBLE
        }

        itemView.rl_content.setOnClickListener {
            bookshelfItemListener.clickedBookShelfItem(book, adapterPosition)
        }
    }
}