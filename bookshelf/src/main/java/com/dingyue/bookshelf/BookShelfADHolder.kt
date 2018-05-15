package com.dingyue.bookshelf

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.txtqbmfyd.item_bookshelf_ad.view.*

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/5/10 21:07
 */

class BookShelfADHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(view: View) = with(itemView) {

        this.rl_content.removeAllViews()

        this.rl_content.addView(view)
    }
}