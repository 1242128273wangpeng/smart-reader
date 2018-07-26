package com.dingyue.bookshelf

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.qbmfxsydq.item_bookshelf_ad.view.*

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/5/10 21:07
 */
class BookShelfADHolder(parent: ViewGroup, isHeaderAd: Boolean) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_bookshelf_ad, parent, false)) {

    fun bind(view: View) = with(itemView) {

        rl_content.removeAllViews()

        rl_content.addView(view)
    }
}