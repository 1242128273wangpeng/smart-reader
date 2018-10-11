package com.dingyue.searchbook.adapter

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ding.basic.bean.HotWordBean
import com.dingyue.searchbook.R
import kotlinx.android.synthetic.qbmfxsydq.item_hot_word.view.*
import java.util.*


/**
 * Desc 热词适配器
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/20 0020 10:00
 */
class HotWordAdapter(private var list: List<HotWordBean>,
                     private val hotWordClickListener: HotWordClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val random: Random? = null

    private var oldType = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            object : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_hot_word, parent, false)) {}

    override fun getItemCount(): Int {
        return if (list.isNotEmpty()) {
            if (list.size >= 5) 6 else list.size
        } else {
            0
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val dataBean = list[position]
        holder.itemView.tv_hotWord.text = dataBean.keyword

        val context = holder.itemView.context

        when (dataBean.superscript) {

            "热" -> {
                holder.itemView.iv_type.setImageResource(R.drawable.icon_search_hot)
                holder.itemView.tv_hotWord.setBackgroundResource(R.drawable.draw_search_red_bg)
                holder.itemView.tv_hotWord.setTextColor(ContextCompat.getColor(context, R.color.search_hot_word_red))
            }
            "荐" -> {
                holder.itemView.iv_type.setImageResource(R.drawable.icon_search_recommend)
                holder.itemView.tv_hotWord.setBackgroundResource(R.drawable.draw_search_orange_bg)
                holder.itemView.tv_hotWord.setTextColor(ContextCompat.getColor(context, R.color.search_hot_word_orange))
            }
            "新" -> {
                holder.itemView.iv_type.setImageResource(R.drawable.icon_search_new)
                holder.itemView.tv_hotWord.setBackgroundResource(R.drawable.draw_search_green_bg)
                holder.itemView.tv_hotWord.setTextColor(ContextCompat.getColor(context, R.color.search_hot_word_green))
            }

            else -> {
                holder.itemView.iv_type.setImageResource(R.drawable.icon_search_transparent)
                holder.itemView.tv_hotWord.setBackgroundResource(R.drawable.draw_search_gray_bg)
                holder.itemView.tv_hotWord.setTextColor(ContextCompat.getColor(context, R.color.color_black))
            }
        }


        holder.itemView.setOnClickListener({
            hotWordClickListener.onHotWordItemClick(it, position, dataBean)
        })
    }


    interface HotWordClickListener {
        fun onHotWordItemClick(view: View, position: Int, dataBean: HotWordBean)
    }

}
