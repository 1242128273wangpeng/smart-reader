package com.intelligent.reader.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.ding.basic.bean.Bookmark
import com.ding.basic.bean.Chapter
import com.intelligent.reader.R
import com.intelligent.reader.holder.BookMarkHolder
import com.intelligent.reader.holder.CatalogHolder
import java.util.ArrayList

/**
 * Date: 2018/6/29 15:02
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 目录页 书签列表适配器
 */
class BookmarkAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var bookmarkList: List<Bookmark> = ArrayList()
    private lateinit var bookMarkListener: BookmarkItemClickListener
    override fun getItemCount(): Int {
        return if (bookmarkList == null) {
            0
        } else {
            bookmarkList.size
        }
    }

    fun insertBookMark(bookmarkList: List<Bookmark>) {
        this.bookmarkList = bookmarkList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BookMarkHolder) {
            holder.bind(position,bookMarkListener,bookmarkList[position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return BookMarkHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_bookmark_item, parent, false))
    }

    fun setListener(itemListener: BookmarkItemClickListener) {
        this.bookMarkListener = itemListener
    }

    interface BookmarkItemClickListener {
        fun clickBookmarkItem(position: Int, bookmark: Bookmark)
        fun clickDeleteItem(position: Int, bookmark: Bookmark)
    }
}