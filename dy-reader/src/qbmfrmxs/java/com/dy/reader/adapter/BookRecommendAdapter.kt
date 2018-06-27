package com.dy.reader.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ding.basic.bean.RecommendBean
import com.dy.reader.R
import java.text.MessageFormat

class BookRecommendAdapter(private var recommends: ArrayList<RecommendBean>) : BaseAdapter() {

    override fun getCount(): Int {
        return recommends.size
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var contentView = convertView

        val viewHolder: ViewHolder

        if (contentView == null) {
            contentView = LayoutInflater.from(parent?.context).inflate(R.layout.item_book_end_recommend, parent, false)
            viewHolder = ViewHolder()
            viewHolder.img_recommend_cover = contentView?.findViewById(R.id.img_recommend_cover)
            viewHolder.txt_recommend_name = contentView?.findViewById(R.id.txt_recommend_name)
            viewHolder.txt_recommend_popularity = contentView?.findViewById(R.id.txt_recommend_popularity)
            contentView.tag = viewHolder
        } else {
            viewHolder = contentView.tag as ViewHolder
        }

        val recommend = recommends[position]

        Glide.with(parent?.context?.applicationContext)
                .load(recommend.sourceImageUrl)
                .placeholder(R.drawable.common_book_cover_default_icon)
                .error(R.drawable.common_book_cover_default_icon)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(viewHolder.img_recommend_cover!!)

        viewHolder.txt_recommend_name?.text = recommend.bookName

        viewHolder.txt_recommend_popularity?.text = MessageFormat.format("{0}人气", recommend.uv)

        return contentView
    }

    private class ViewHolder {
        internal var img_recommend_cover: ImageView? = null
        internal var txt_recommend_name: TextView? = null
        internal var txt_recommend_popularity: TextView? = null
    }
}