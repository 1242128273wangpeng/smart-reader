package com.intelligent.reader.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.intelligent.reader.R
import kotlinx.android.synthetic.mfxsqbyd.item_search_history.view.*

/**
 * Function：搜索历史子条目
 *
 * Created by JoannChen on 2018/5/30 0030 17:09
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
class SearchHistoryAdapter() : BaseAdapter() {

    private var context: Context? = null
    private var searchDate: List<String>? = null
    private var positionClick: onPositionClickListener? = null

    constructor(context: Context?, searchDate: List<String>) : this() {
        this.context = context
        this.searchDate = searchDate

    }

    override fun getItem(position: Int): Any {
        return searchDate!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return searchDate!!.size
    }

    fun setPositionClickListener(onPositionClickListener: onPositionClickListener) {
        this.positionClick = onPositionClickListener
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: ViewHolder
        val historyView: View
        if (convertView == null) {
            holder = ViewHolder()
            historyView = LayoutInflater.from(context).inflate(R.layout.item_search_history, parent, false)
            holder.history_word = historyView.history_word
            holder.history_clear = historyView.history_clear
            historyView.tag = holder
        } else {
            historyView = convertView
            holder = historyView.tag as ViewHolder
        }

        holder.history_word.text = searchDate!![position]

        holder.history_clear.setOnClickListener {
            positionClick!!.onItemClickListener(position)
        }

        return historyView
    }

    class ViewHolder {
        lateinit var history_word: TextView
        lateinit var history_clear: ImageView

    }

    /**
     * 在Adapter中回调被点击的item的索引，处理搜索历史的数据存储
     */
    interface onPositionClickListener {
        fun onItemClickListener(position: Int)
    }

}
