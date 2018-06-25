package com.dingyue.bookshelf

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.item_bookshelf_bottom_line.view.*

/**
 * Function：书架列表的底线提示Item,我是有底线的~
 *
 * Created by JoannChen on 2018/6/21 0021 17:50
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */

class BookShelfFooterHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(isShow: Boolean) = with(itemView) {

        if (isShow) {
            itemView.rl_content.visibility = View.VISIBLE
        } else {
            itemView.rl_content.visibility = View.GONE
        }

    }
}