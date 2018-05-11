package com.dingyue.bookshelf

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.txtqbmfyd.item_bookshelf_add.view.*
import net.lzbook.kit.data.bean.Book

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/5/10 21:07
 */

class BookShelfADDHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(book: Book?, remove: Boolean, bookshelfItemListener: BookShelfAdapter.BookShelfItemListener) = with(itemView) {

        if (remove) {
            this.rl_add.visibility = View.INVISIBLE
        } else {
            this.rl_add.visibility = View.VISIBLE
        }

        this.rl_add.setOnClickListener {
            bookshelfItemListener.clickedBookShelfItem(book, adapterPosition)
        }
    }
}