package com.dingyue.bookshelf

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import net.lzbook.kit.utils.AppUtils

/**
 * Desc 书架添加书籍Item
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/5/10 21:07
 */
class BookShelfADDHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.item_bookshelf_add, parent, false)) {
    init {
        itemView.setPadding(itemView.paddingLeft, AppUtils.dip2px(itemView.context, 20f),
                itemView.paddingRight, itemView.paddingBottom)
        itemView.setBackgroundColor(Color.WHITE)
    }

    fun bind(size:Int,itemListener: BookShelfAdapter.BookShelfItemListener) = with(itemView) {
        itemView.setOnClickListener {
            itemListener?.clickedBookShelfItem(null,adapterPosition)
        }
    }
}