package com.intelligent.reader.holder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import com.ding.basic.bean.Bookmark
import com.intelligent.reader.adapter.BookmarkAdapter
import kotlinx.android.synthetic.main.layout_bookmark_item.view.*
import kotlinx.android.synthetic.qbzsydq.item_book_catalog.view.*
import java.text.SimpleDateFormat

/**
 * Date: 2018/6/29 15:05
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 目录页 标签列表holder
 */
class BookMarkHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd  HH:mm")

    fun bind(position:Int,itemListener: BookmarkAdapter.BookmarkItemClickListener, data: Bookmark) {
        itemView.item_bookmark_title.text = data.chapter_name
        itemView.item_bookmark_desc.text = data.chapter_content
        itemView.item_bookmark_time.text = dateFormat.format(data.insert_time)
        itemView.setOnClickListener {
            itemListener.clickBookmarkItem(position, data)
        }
        itemView.item_bookmark_delete.setOnClickListener {
            itemListener.clickDeleteItem(position, data)
        }


    }
}