package com.dingyue.searchbook.adapter

import android.app.Activity
import android.graphics.Color
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.ding.basic.bean.HotWordBean
import com.example.searchbook.R


/**
 * Desc 热词适配器
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/20 0020 10:00
 */
class HotWordAdapter(private val mContext: Activity, private var list: List<HotWordBean>?) : BaseAdapter() {

    override fun getCount(): Int {
        return if (list != null && list!!.isNotEmpty()) {
            if (list!!.size >= 5) {
                6
            } else {
                list!!.size
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
        var convertView = convertView
        val holder: ViewHolder
        if (convertView == null) {
            val inflater = mContext.layoutInflater
            convertView = inflater.inflate(R.layout.item_hot_word, parent, false)
            holder = ViewHolder()
            holder.hotWordText = convertView!!.findViewById<View>(R.id.tv_hotword) as TextView
            holder.typeImg = convertView.findViewById<View>(R.id.iv_type) as ImageView
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        val dataBean = list!![position]
        holder.hotWordText.text = dataBean.keyword
        if (!TextUtils.isEmpty(dataBean.superscript)) {
            holder.typeImg.visibility = View.VISIBLE
            when (dataBean.superscript) {
                "热" -> holder.typeImg.setImageResource(R.drawable.icon_hot_re)
                "荐" -> holder.typeImg.setImageResource(R.drawable.icon_hot_jian)
                "新" -> holder.typeImg.setImageResource(R.drawable.icon_hot_xin)
            }
        } else {
            holder.typeImg.visibility = View.GONE
        }
        if (!TextUtils.isEmpty(dataBean.color)) {
            holder.hotWordText.setTextColor(Color.parseColor(dataBean.color))
        }
        return convertView


    }

    private inner class ViewHolder {
        lateinit var hotWordText: TextView
        lateinit var typeImg: ImageView
    }


    fun setData(list: List<HotWordBean>) {
        this.list = list
    }


}
