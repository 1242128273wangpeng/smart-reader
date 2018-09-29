package com.dingyue.searchbook.adapter

import android.graphics.Color
import android.text.TextUtils
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
            holder.hotWordText = hotView.findViewById<View>(R.id.tv_hotword) as TextView
            holder.typeImg = hotView.findViewById<View>(R.id.iv_type) as ImageView
            hotView.tag = holder
        } else {
            hotView = convertView
            holder = convertView.tag as ViewHolder
        }
        val dataBean = list[position]
        holder.hotWordText.text = dataBean.keyword
        if (!TextUtils.isEmpty(dataBean.superscript)) {
            holder.typeImg.visibility = View.VISIBLE
            when (dataBean.superscript) {
                "热" -> holder.typeImg.setImageResource(R.drawable.icon_hot_word_hot)
                "荐" -> holder.typeImg.setImageResource(R.drawable.icon_hot_word_recommend)
                "新" -> holder.typeImg.setImageResource(R.drawable.icon_hot_word_new)
            }
        } else {
            holder.typeImg.visibility = View.GONE
        }
        if (!TextUtils.isEmpty(dataBean.color)) {
            holder.hotWordText.setTextColor(Color.parseColor(dataBean.color))
        }
        return hotView
    }

    private inner class ViewHolder {
        lateinit var hotWordText: TextView
        lateinit var typeImg: ImageView
    }

}
