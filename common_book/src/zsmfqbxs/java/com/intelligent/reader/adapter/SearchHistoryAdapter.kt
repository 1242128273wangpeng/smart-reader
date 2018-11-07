package com.intelligent.reader.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.intelligent.reader.R

/**
 * Created by YHang on 2017\12\2 0002.
 */
class SearchHistoryAdapter() : BaseAdapter() {

    private var context : Context? = null
    private var searchDate : List<String>? = null
    private var positionClick : onPositionClickListener? = null

    constructor(context: Context?,searchDate: List<String>) : this() {
        this.context = context
        this.searchDate = searchDate

    }

    override fun getItem(position: Int): Any {
        return searchDate!![position]
    }

    override fun getItemId(position : Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return searchDate!!.size
    }

    fun setPositionClickListener(onPositionClickListener: onPositionClickListener){
        this.positionClick = onPositionClickListener
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var holder : ViewHolder
        var historyView : View
        if(convertView == null){
            holder = ViewHolder()
            historyView = LayoutInflater.from(context).inflate(R.layout.item_search_history,parent,false)
            holder.iv_history = historyView.findViewById(R.id.historyImg) as ImageView
            holder.tv_history = historyView.findViewById(R.id.history_word) as TextView
            holder.clearIv = historyView.findViewById(R.id.history_clear) as ImageView
            historyView.tag = holder
        }else{
            historyView = convertView
            holder = historyView.tag as ViewHolder
        }

        holder.tv_history.text = searchDate!![position]

        holder.clearIv.setOnClickListener {
            positionClick?.onItemClickListener(position)
        }

        return historyView
    }

    class ViewHolder{
        lateinit var iv_history : ImageView
        lateinit var tv_history: TextView
        lateinit var clearIv : ImageView
    }

    /**
     * 在Adapter中回调被点击的item的索引，处理搜索历史的数据存储
     */
     interface onPositionClickListener {
        fun onItemClickListener(position: Int)
    }

}