package com.dingyue.searchbook.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.dingyue.searchbook.R
import kotlinx.android.synthetic.qbzsydq.item_history.view.*

/**
 * Desc：搜索历史子条目
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/9/20 0020 16:30
 */
class HistoryAdapter(var context: Context?,
                     private var historyList: List<String>?,
                     private var onHistoryItemClickListener: OnHistoryItemClickListener?) : BaseAdapter() {

    override fun getItem(position: Int): Any {
        return historyList!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return historyList!!.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: ViewHolder
        val historyView: View
        if (convertView == null) {
            holder = ViewHolder()
            historyView = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false)
            holder.history_word = historyView.history_word
            historyView.tag = holder
        } else {
            historyView = convertView
            holder = historyView.tag as ViewHolder
        }

        holder.history_word.text = historyList!![position]

        historyView.setOnClickListener {
            onHistoryItemClickListener?.onHistoryItemClickListener(position, historyList)
        }

        return historyView
    }

    class ViewHolder {
        lateinit var history_word: TextView

    }

    /**
     * 在Adapter中回调被点击的item的索引，处理搜索历史的数据存储
     */
    interface OnHistoryItemClickListener {
        fun onHistoryItemClickListener(position: Int, historyList: List<String>?)
    }

}
