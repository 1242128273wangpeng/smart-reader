package com.dingyue.searchbook.adapter

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
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

    private lateinit var context: Context

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
        context = parent.context
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            holder.hotWordText.background = getHotWordBgColor(hotWordBgColor[position % 6])
        }

        return hotView
    }

    private inner class ViewHolder {
        lateinit var hotWordText: TextView
    }


    /**
     * 边框填充和文字颜色
     */
    private val hotWordBgColor = intArrayOf(R.color.search_hotWord_bg1, R.color.search_hotWord_bg2, R.color.search_hotWord_bg3, R.color.search_hotWord_bg4, R.color.search_hotWord_bg5, R.color.search_hotWord_bg6)

    /**
     * 设置边框，背景，圆角
     */
    private fun getHotWordBgColor(solidColor: Int): GradientDrawable {
        val gd = GradientDrawable()
        gd.shape = GradientDrawable.RECTANGLE
        gd.cornerRadius = 2f //圆角
        gd.setColor(ContextCompat.getColor(context, solidColor))//填充色
        return gd
    }

}
