package com.dingyue.bookshelf

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.RelativeLayout
import net.lzbook.kit.data.bean.Book

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/5/10 21:07
 */

class BookShelfADDHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val rl_add: RelativeLayout = itemView.findViewById(R.id.rl_add) as RelativeLayout

    fun bind(book: Book?, remove: Boolean, bookshelfItemListener: BookShelfAdapter.BookShelfItemListener) = with(itemView) {

        if (remove) {
            rl_add.visibility = View.INVISIBLE
        } else {
            rl_add.visibility = View.VISIBLE
        }

        rl_add.setOnClickListener {
            bookshelfItemListener.clickedBookShelfItem(book, adapterPosition)
        }
    }
}