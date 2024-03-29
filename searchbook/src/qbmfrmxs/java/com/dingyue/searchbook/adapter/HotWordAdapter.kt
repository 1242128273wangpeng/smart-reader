package com.dingyue.searchbook.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.ding.basic.bean.HotWordBean
import com.dingyue.searchbook.R


/**
 * Desc 热词适配器
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/20 0020 10:00
 */
class HotWordAdapter(private var list: List<HotWordBean>) : BaseAdapter() {

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
            holder.txt_hot_word = hotView.findViewById<View>(R.id.txt_hotword) as TextView
            holder.img_hot_rank = hotView.findViewById<View>(R.id.img_hot_rank) as ImageView
            hotView.tag = holder
        } else {
            hotView = convertView
            holder = convertView.tag as ViewHolder
        }

        val dataBean = list[position]
        holder.txt_hot_word.text = dataBean.keyword
        setHotShowType(holder.img_hot_rank, position)
        return hotView

    }

    private inner class ViewHolder {
        lateinit var txt_hot_word: TextView
        lateinit var img_hot_rank: ImageView
    }

    private fun setHotShowType(bubbleIv: ImageView, position: Int) {
        when (position) {
            0 -> bubbleIv.setImageResource(R.drawable.search_img_hot_1)
            1 -> bubbleIv.setImageResource(R.drawable.search_img_hot_2)
            2 -> bubbleIv.setImageResource(R.drawable.search_img_hot_3)
            3 -> bubbleIv.setImageResource(R.drawable.search_img_hot_4)
            4 -> bubbleIv.setImageResource(R.drawable.search_img_hot_5)
            5 -> bubbleIv.setImageResource(R.drawable.search_img_hot_6)
        }
    }

}
