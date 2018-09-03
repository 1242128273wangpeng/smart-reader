package com.dy.reader.adapter

import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * 阅读页目录适配器
 * Created by xian on 2017/8/17.
 */
class ListRecyclerAdapter<T, C : BaseRecyclerHolder<T>>(val datas: List<T>, @LayoutRes private val itemLayout: Int, private val clazz: Class<C>, var itemClick: View.OnClickListener? = null, var itemLongClick: View.OnLongClickListener? = null) : RecyclerView.Adapter<C>() {

    private var inflater: LayoutInflater? = null

    var isEditMode = false

    override fun onBindViewHolder(holder: C, position: Int) {
        try {
            if (datas.size > position)
                holder.onBindData(position, datas[position], isEditMode)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): C {
        if (inflater == null)
            inflater = LayoutInflater.from(parent.context)
        var view: View? = null
        try {
            view = inflater!!.inflate(itemLayout, parent, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val holder = clazz.getConstructor(View::class.java).newInstance(view)
        holder.onItemClick = this.itemClick
        holder.onItemLongClick = this.itemLongClick
        return holder
    }
}