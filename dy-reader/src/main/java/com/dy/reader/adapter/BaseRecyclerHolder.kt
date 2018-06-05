package com.dy.reader.adapter

import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Created by xian on 2017/8/17.
 */
abstract open class BaseRecyclerHolder<T>(itemView: View?) : RecyclerView.ViewHolder(itemView) {

    var onItemClick: View.OnClickListener? = null
    var onItemLongClick: View.OnLongClickListener? = null

    abstract open fun onBindData(position: Int, data: T, editMode: Boolean)
}