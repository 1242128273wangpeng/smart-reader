package com.dingyue.bookshelf

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.qbmfrmxs.item_bookshelf_add.view.*

/**
 * Desc 书架添加书籍Item
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/5/10 21:07
 */
class BookShelfADDHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.item_bookshelf_add, parent, false)) {

    fun bind(size: Int, itemListener: BookShelfAdapter.BookShelfItemListener) = with(itemView) {

        if (size > 4) {
            itemView.rl_content.visibility = View.VISIBLE
        } else {
            itemView.rl_content.visibility = View.GONE
        }
    }
}