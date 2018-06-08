package com.dy.reader.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.ding.basic.bean.Source
import com.dy.reader.listener.SourceClickListener
import java.util.ArrayList

import com.dy.reader.holder.SourceHolder

class SourceAdapter(private var sources: ArrayList<Source>, private var sourceClickListener: SourceClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SourceHolder(parent)
    }

    override fun getItemCount(): Int {
        return sources.size
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val source: Source? = sources[position]

        if (source == null) {
            viewHolder.itemView.visibility = View.GONE
            return
        }

        viewHolder.itemView.visibility = View.VISIBLE

        if (viewHolder is SourceHolder) {
            viewHolder.bind(source, sourceClickListener)
        }
    }
}