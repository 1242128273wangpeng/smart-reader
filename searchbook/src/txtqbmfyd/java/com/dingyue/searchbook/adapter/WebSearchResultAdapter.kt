package com.dingyue.searchbook.adapter

import android.content.Context
import android.text.Html
import android.view.View
import android.widget.TextView
import com.dingyue.searchbook.R
import kotlinx.android.synthetic.txtqbmfyd.item_web_search_result.view.*
import net.lzbook.kit.bean.CrawlerResult
import net.lzbook.kit.ui.adapter.base.RecyclerBaseAdapter

/**
 * Desc 百度检索结果适配器
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/10/31 14:57
 */
class WebSearchResultAdapter(context: Context) : RecyclerBaseAdapter<CrawlerResult>(context, R.layout.item_web_search_result) {

    var onLatestChapterClick: ((url: String?) -> Unit)? = null
    var onItemClick: ((url: String?) -> Unit)? = null
    var keyword: String? = null

    override fun bindView(itemView: View, data: CrawlerResult, position: Int) {
        with(itemView) {
            if (position == 0) {
                view_top_tip.visibility = View.VISIBLE
                view_top_tip.findViewById<TextView>(R.id.txt_tip).text = "以下搜索结果来自互联网"
            } else {
                view_top_tip.visibility = View.GONE
            }
            ll_content.tag = data.url
            ll_content.setOnClickListener { onItemClick?.invoke(it.tag as String?) }
            // 赋值
            var title = data.title?.replace("<em>","<font color='#DC0000'>")?.replace("</em>","</font>")
            txt_title.text = Html.fromHtml(title)
            if (data.author.isNullOrBlank()) {
                txt_author.visibility = View.GONE
            } else {
                txt_author.visibility = View.VISIBLE
                txt_author.text = data.author
            }
            var abstract = data.abstract?.replace("<em>","<font color='#DC0000'>")?.replace("</em>","</font>")
            txt_summary.text =  Html.fromHtml(abstract)
            if (!data.newChapter.isNullOrBlank() && !data.newChapterUrl.isNullOrBlank()) {
                txt_latest_chapter.tag = data.newChapterUrl
                txt_latest_chapter.visibility = View.VISIBLE
                txt_latest_chapter.text = data.newChapter
                txt_latest_chapter.setOnClickListener { onLatestChapterClick?.invoke(it.tag as String?) }
            } else txt_latest_chapter.visibility = View.GONE
            txt_source_from.text = data.source
            txt_update_time.text = data.updateTime
        }
    }
}