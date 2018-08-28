package com.intelligent.reader.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.ding.basic.bean.Chapter
import com.ding.basic.util.DataCache
import com.intelligent.reader.R
import com.intelligent.reader.holder.CatalogHolder

/**
 * Date: 2018/6/29 14:10
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 章节页 章节列表适配器
 */
class CataloguesAdapter  : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var chapterList: List<Chapter> = ArrayList()

    private var selectedPosition: Int = 0

    private lateinit var chapterItemClickListener: ChapterItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CatalogHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_catalog_item, parent, false))
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val chapter = chapterList[position]

        val cached = DataCache.isChapterCached(chapter)

        val selected = chapter.sequence == selectedPosition

        if (viewHolder is CatalogHolder) {
            viewHolder.bind(chapter, selected, cached, chapterItemClickListener)
        }
    }

    override fun getItemCount(): Int {
        return chapterList.size
    }

    fun insertCatalog(chapterList: List<Chapter>) {
        this.chapterList = chapterList
        notifyDataSetChanged()
    }

    fun setSelectedItem(position: Int) {

        if (chapterList.isEmpty()) {
            selectedPosition = 0
            return
        }

        if (position >= chapterList.size) {
            selectedPosition = chapterList.size - 1
            return
        }

        selectedPosition = position
    }

    interface ChapterItemClickListener {
        fun clickedChapter(position: Int, chapter: Chapter)
    }

    fun insertChapterItemClickListener(chapterItemClickListener: ChapterItemClickListener) {
        this.chapterItemClickListener = chapterItemClickListener
    }
}