package com.dy.reader.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import com.dy.reader.R
import com.dy.reader.model.FontData
import kotlinx.android.synthetic.txtqbdzs.item_reader_option_font.view.*

/**
 * Function：字体包适配器
 *
 * Created by JoannChen on 2018/9/10 0010 22:17
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
class FontAdapter(var list:ArrayList<FontData>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClickListener:AdapterView.OnItemClickListener? = null

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        list[position].apply {
            holder.itemView.txt_name.text = name
        }

        holder.itemView.btn_use.setOnClickListener {
            onItemClickListener?.onItemClick(null,null,position,0)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_reader_option_font,parent,false)
        return object:RecyclerView.ViewHolder(itemView){}
    }


}