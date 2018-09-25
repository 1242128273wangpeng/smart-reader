package com.dy.reader.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dy.reader.R
import com.dy.reader.model.FontData
import com.dy.reader.service.FontDownLoadService
import kotlinx.android.synthetic.txtqbdzs.item_reader_option_font.view.*
import net.lzbook.kit.utils.sp.SPKey
import net.lzbook.kit.utils.sp.SPUtils

/**
 * Function：字体包适配器
 *
 * Created by JoannChen on 2018/9/10 0010 22:17
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
class FontAdapter(var list: ArrayList<FontData>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClickListener: ((data: FontData, position: Int) -> Unit)? = null


    private var curFontName: String = FontDownLoadService.FONT_DEFAULT
        get() {
            field = SPUtils.getDefaultSharedString(SPKey.READER_TYPE_FACE, FontDownLoadService.FONT_DEFAULT)
            return field
        }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = list[position]
        val img = data.iconRes
        if (img != null) {
            holder.itemView.img_name.setImageResource(img)
        } else {
            holder.itemView.txt_name.visibility = View.VISIBLE
            holder.itemView.img_name.visibility = View.GONE
        }

        if (data.name == curFontName) {
            holder.itemView.btn_use.visibility = View.GONE
            holder.itemView.img_current_use.visibility = View.VISIBLE
        } else {
            holder.itemView.btn_use.visibility = View.VISIBLE
            holder.itemView.img_current_use.visibility = View.GONE
        }

        val progress = data.progress
        holder.itemView.btn_use.text = when (progress) {
            100 -> holder.itemView.resources.getString(R.string.use)
            -1 -> holder.itemView.context.resources.getString(R.string.download)
            else -> {
                "$progress%"
            }
        }

        holder.itemView.btn_use.setOnClickListener {
            if (data.name == curFontName) return@setOnClickListener
            onItemClickListener?.invoke(data, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_reader_option_font, parent, false)
        return object : RecyclerView.ViewHolder(itemView) {}
    }


}