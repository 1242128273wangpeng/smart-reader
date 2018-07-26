package com.intelligent.reader.holder

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.View
import com.ding.basic.bean.Chapter
import com.intelligent.reader.adapter.CataloguesAdapter
import kotlinx.android.synthetic.qbmfxsydq.content_catalog_item.view.*
import kotlinx.android.synthetic.qbmfxsydq.item_book_catalog.view.*

/**
 * Date: 2018/6/29 14:07
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 目录页 章节目录holder
 */
class CatalogHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(chapter: Chapter, selected: Boolean, cached: Boolean, chapterItemClickListener: CataloguesAdapter.ChapterItemClickListener?) {
        if (selected) {
            itemView.catalog_chapter_name.setTextColor(Color.parseColor("#42BE54"))
        } else {
            if (cached) {
                itemView.catalog_chapter_name.setTextColor(Color.parseColor("#616161"))
            } else {
                itemView.catalog_chapter_name.setTextColor(Color.parseColor("#B9B9B9"))
            }
        }
        itemView.catalog_chapter_name.text = chapter.name

        itemView.setOnClickListener {
            chapterItemClickListener?.clickedChapter(adapterPosition, chapter)
        }
    }
}