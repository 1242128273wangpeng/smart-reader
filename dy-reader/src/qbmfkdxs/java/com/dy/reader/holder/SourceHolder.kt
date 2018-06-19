package com.dy.reader.holder

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.ding.basic.bean.Source
import com.dy.reader.R
import com.dy.reader.listener.SourceClickListener
import kotlinx.android.synthetic.qbmfkdxs.item_book_source.view.*
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.Tools
import java.text.MessageFormat

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/6/2 15:05
 */

class SourceHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_book_source, parent, false)) {

    fun bind(source: Source, sourceClickListener: SourceClickListener) = with(itemView) {

        txt_source_chapter.text = source.last_chapter_name

        txt_source_host.text = MessageFormat.format("来源{0}: {1}", adapterPosition + 1, source.host)

        txt_source_update.text = Tools.compareTime(AppUtils.formatter, source.update_time)

        rl_source_content.setOnClickListener {
            sourceClickListener.clickedSource(source)
        }
    }
}