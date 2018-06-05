package com.dy.reader.adapter

import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class ListRecyclerAdapter<T, C : BaseRecyclerHolder<T>>(private val dataList: List<T>, @LayoutRes private val itemLayout: Int, private val clazz: Class<C>, var itemClick: View.OnClickListener? = null, var itemLongClick: View.OnLongClickListener? = null) : RecyclerView.Adapter<C>() {

    private var inflater: LayoutInflater? = null

    var isEditMode = false

    override fun onBindViewHolder(holder: C?, position: Int) {
        try {
            if (dataList.size > position)
                holder?.onBindData(position, dataList[position], isEditMode)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): C {
        if (inflater == null)
            inflater = LayoutInflater.from(parent!!.context)
        var view: View? = null
        try {
            view = inflater!!.inflate(itemLayout, parent, false)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        val holder = clazz.getConstructor(View::class.java).newInstance(view)
        holder.onItemClick = this.itemClick
        holder.onItemLongClick = this.itemLongClick
        return holder
    }
}