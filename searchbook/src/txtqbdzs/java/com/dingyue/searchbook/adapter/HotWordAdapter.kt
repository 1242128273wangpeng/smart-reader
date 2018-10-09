package com.dingyue.searchbook.adapter

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.ding.basic.bean.HotWordBean
import com.dingyue.searchbook.R
import java.util.*


/**
 * Desc 热词适配器
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/20 0020 10:00
 */
class HotWordAdapter(private var list: List<HotWordBean>) : BaseAdapter() {

    private val random: Random? = null

    private var oldType = -1

    override fun getCount(): Int {
        return if (list.isNotEmpty()) {
            if (list.size >= 5) 6 else list.size
        } else {
            0
        }
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val context = parent.context
        val hotView: View
        val holder: ViewHolder

        if (convertView == null) {
            hotView = LayoutInflater.from(context).inflate(R.layout.item_hot_word, parent, false)
            holder = ViewHolder()
            holder.hotWordText = hotView.findViewById<View>(R.id.tv_hotword) as TextView
            hotView.tag = holder
        } else {
            hotView = convertView
            holder = convertView.tag as ViewHolder
        }

        val dataBean = list[position]
        holder.hotWordText.text = dataBean.keyword

        var currType = random?.nextInt(7)
        while (oldType == currType) {
            currType = random?.nextInt(7)
        }
        oldType = currType ?: 0
        when (currType) {
            0, 1, 2, 3 -> {
//                holder.hotWordText.setBackgroundResource(R.drawable.search_hot_word_bg_1)
                holder.hotWordText.setTextColor(ContextCompat.getColor(context, R.color.search_hot_word_text_color_1))
            }
            4 -> {
//                holder.hotWordText.setBackgroundResource(R.drawable.search_hot_word_bg_2)
                holder.hotWordText.setTextColor(ContextCompat.getColor(context, R.color.search_hot_word_text_color_2))
            }
            5 -> {
//                holder.hotWordText.setBackgroundResource(R.drawable.search_hot_word_bg_3)
                holder.hotWordText.setTextColor(ContextCompat.getColor(context, R.color.search_hot_word_text_color_3))
            }
            6 -> {
//                holder.hotWordText.setBackgroundResource(R.drawable.search_hot_word_bg_3)
                holder.hotWordText.setTextColor(ContextCompat.getColor(context, R.color.search_hot_word_text_color_4))
            }
        }

        return hotView
    }

    private inner class ViewHolder {
        lateinit var hotWordText: TextView
    }

}
