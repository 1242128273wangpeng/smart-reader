package net.lzbook.kit.ui.adapter.base

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.lzbook.kit.utils.antiShakeClick

/**
 * Desc recyclerView适配器基类
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/10/12 14:36
 */
abstract class RecyclerBaseAdapter<T>(ctx: Context, private val layoutId: Int) : RecyclerView.Adapter<RecyclerBaseAdapter.ViewHolder>() {

    private var listener: OnItemClickListener? = null
    private var longClickListener: OnItemLongClickListener? = null

    protected var list: List<T> = emptyList()

    fun setData(data: List<T>) {
        this.list = data
    }

    private val layoutInflater by lazy { LayoutInflater.from(ctx) }

    override fun getItemCount() = list.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(layoutInflater.inflate(layoutId, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            tag = position
            antiShakeClick { listener?.onItemClick(it, it.tag as Int) }
            setOnLongClickListener {
                longClickListener?.onItemLongClick(it, it.tag as Int) ?: false
            }
            bindView(this, list[position], position)
        }
    }

    abstract fun bindView(itemView: View, data: T, position: Int)

    fun setOnItemClickListener(l: OnItemClickListener) {
        this.listener = l
    }

    fun setOnItemLongClickListener(l: OnItemLongClickListener) {
        this.longClickListener = l
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(view: View, position: Int): Boolean
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}