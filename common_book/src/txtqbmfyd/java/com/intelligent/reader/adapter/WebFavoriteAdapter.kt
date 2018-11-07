package com.intelligent.reader.adapter

import android.content.Context
import android.view.View
import com.ding.basic.bean.WebPageFavorite
import com.intelligent.reader.R
import kotlinx.android.synthetic.txtqbmfyd.item_web_favorite.view.*
import net.lzbook.kit.ui.adapter.base.RecyclerBaseAdapter
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.Tools

/**
 * Desc 网页收藏列表适配器
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/10/31 18:28
 */
class WebFavoriteAdapter(context: Context) : RecyclerBaseAdapter<WebPageFavorite>(context, R.layout.item_web_favorite) {

    var remove = false
    var selectAll = false
    var favoriteClick: ((position: Int) -> Unit)? = null
    var favoriteLongClick: ((position: Int) -> Unit)? = null
    private val tranX by lazy { AppUtils.dip2px(context, 50f) }

    override fun bindView(itemView: View, data: WebPageFavorite, position: Int) {
        with(itemView) {
            if (remove) {
                img_select.visibility = View.VISIBLE
                ll_content.translationX = tranX.toFloat()
                img_select.setImageResource(if (data.selected) R.drawable.download_manager_item_check_icon else R.drawable.download_manager_item_uncheck_icon)
            } else {
                img_select.visibility = View.GONE
                ll_content.translationX = 0f
            }
            txt_web_title.text = data.webTitle
            txt_source_from.text = data.webLink
            txt_time.text = Tools.formatTime(data.createTime, "yyyy.MM.dd  HH:mm")
            if (position == itemCount - 1 && !remove) {
                txt_favorite_tip.visibility = View.VISIBLE
                txt_favorite_tip.text = "共${itemCount}条收藏，最多收藏10条~"
            } else {
                txt_favorite_tip.visibility = View.GONE
            }
            fl_favorite_content.tag = position
            fl_favorite_content.setOnClickListener { favoriteClick?.invoke(it.tag as Int) }
            fl_favorite_content.setOnLongClickListener {
                favoriteLongClick?.invoke(it.tag as Int)
                true
            }
        }
    }
}