package com.intelligent.reader.holder

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.View
import com.ding.basic.bean.Chapter
import com.intelligent.reader.adapter.CataloguesAdapter
import kotlinx.android.synthetic.qbmfrmxs.item_book_catalog.view.*

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/6/25 17:16
 */
class CatalogHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(chapter: Chapter, selected: Boolean, cached: Boolean, chapterItemClickListener: CataloguesAdapter.ChapterItemClickListener?) {
        if (selected) {
            itemView.txt_chapter_name.setTextColor(Color.parseColor("#42BE54"))
        } else {
            if (cached) {
                itemView.txt_chapter_name.setTextColor(Color.parseColor("#616161"))
            } else {
                itemView.txt_chapter_name.setTextColor(Color.parseColor("#B9B9B9"))
            }
        }
        itemView.txt_chapter_name.text = chapter.name

        itemView.setOnClickListener {
            chapterItemClickListener?.clickedChapter(adapterPosition, chapter)
        }
    }
}