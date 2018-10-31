package com.intelligent.reader.adapter

import android.content.Context
import android.view.View
import com.ding.basic.bean.WebPageFavorite
import net.lzbook.kit.ui.adapter.base.RecyclerBaseAdapter

/**
 * Desc 网页收藏列表适配器
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/10/31 18:28
 */
class WebFavoriteAdapter(context: Context) : RecyclerBaseAdapter<WebPageFavorite>(context, 0) {

    var remove = false

    override fun bindView(itemView: View, data: WebPageFavorite, position: Int) {

    }
}