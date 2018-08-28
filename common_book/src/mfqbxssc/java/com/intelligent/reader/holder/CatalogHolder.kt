package com.intelligent.reader.holder

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.View
import com.ding.basic.bean.Chapter
import com.intelligent.reader.adapter.CataloguesAdapter
import kotlinx.android.synthetic.mfqbxssc.content_catalog_item.view.*

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/6/25 17:16
 */
class CatalogHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(chapter: Chapter, selected: Boolean, cached: Boolean, chapterItemClickListener: CataloguesAdapter.ChapterItemClickListener?) {
        if (selected) {
            itemView.catalog_chapter_name.setTextColor(Color.parseColor("#882f46"))
        } else {
            if (cached) {
                itemView.catalog_chapter_name.setTextColor(Color.parseColor("#323232"))
            } else {
                itemView.catalog_chapter_name.setTextColor(Color.parseColor("#838181"))
            }
        }
        itemView.catalog_chapter_name.text = chapter.name

        itemView.setOnClickListener {
            chapterItemClickListener?.clickedChapter(adapterPosition, chapter)
        }
    }
}