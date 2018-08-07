package com.dingyue.bookshelf

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import kotlinx.android.synthetic.qbmfkkydq.item_bookshelf_add.view.*

/**
 * Desc 书架添加书籍Item
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/5/10 21:07
 */
class BookShelfADDHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_bookshelf_add, parent, false)) {

    fun bind(size: Int, itemListener: BookShelfAdapter.BookShelfItemListener) = with(itemView) {

        /**
         * 书架背景优化
         * http://note.youdao.com/noteshare?id=7b6be9f7706849746a83d21a207d3ca5&sub=BB3CD2D7346043C59050C702C72D3FEB
         */
        if (size % 3 == 0) {
            fl_add_layout.setBackgroundResource(R.color.color_white)
        } else {
            fl_add_layout.setBackgroundResource(R.drawable.bookshelf_bookitem_bg)
        }

        itemView.setOnClickListener {
            itemListener.clickedBookShelfItem(null, adapterPosition)
        }
    }
}