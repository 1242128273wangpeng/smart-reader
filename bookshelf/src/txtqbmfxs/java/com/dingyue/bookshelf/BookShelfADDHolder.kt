package com.dingyue.bookshelf

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.ding.basic.bean.Book

/**
 * Desc 书架添加书籍Item
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/5/10 21:07
 */
class BookShelfADDHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.item_bookshelf_add, parent, false)) {

    fun bind(book: Book?, remove: Boolean, bookshelfItemListener: BookShelfAdapter.BookShelfItemListener) = with(itemView) {

    }
}