package com.dingyue.searchbook.adapter

import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
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

    private val random = Random() //生成不同颜色背景的随机数
    private var oldType = -1 // 避免两个连续标签显示一样的背景

    override fun getCount(): Int {
        return if (list.isNotEmpty()) {
            if (list.size >= 9) {
                9
            } else {
                list.size
            }
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
            holder.hotWordText = hotView.findViewById(R.id.tv_hotword)
            hotView.tag = holder
        } else {
            hotView = convertView
            holder = convertView.tag as ViewHolder
        }
        val dataBean = list[position]
        holder.hotWordText.text = dataBean.keyword

        var currType = random.nextInt(7)
        while (oldType == currType) {
            currType = random.nextInt(7)
        }
        oldType = currType
        when (currType) {
            0, 1, 2, 3 -> {
                holder.hotWordText.setBackgroundResource(R.drawable.draw_search_gray_bg)
                holder.hotWordText.setTextColor(ContextCompat.getColor(context, R.color.search_hot_word_black))
            }
            4 -> {
                holder.hotWordText.setBackgroundResource(R.drawable.draw_search_blue_bg)
                holder.hotWordText.setTextColor(ContextCompat.getColor(context, R.color.search_hot_word_blue))
            }
            5 -> {
                holder.hotWordText.setBackgroundResource(R.drawable.draw_search_orange_bg)
                holder.hotWordText.setTextColor(ContextCompat.getColor(context, R.color.search_hot_word_orange))
            }
            6 -> {
                holder.hotWordText.setBackgroundResource(R.drawable.draw_search_red_bg)
                holder.hotWordText.setTextColor(ContextCompat.getColor(context, R.color.search_hot_word_red))
            }
        }

        return hotView
    }

    private inner class ViewHolder {
        lateinit var hotWordText: TextView
    }

}
