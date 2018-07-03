package com.intelligent.reader.adapter

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ding.basic.bean.RecommendBean
import com.intelligent.reader.R

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import net.lzbook.kit.utils.AppUtils
import java.text.MessageFormat

class BookRecommendAdapter : BaseAdapter() {

    private var recommends: List<RecommendBean>? = null

    override fun getCount(): Int {
        return if (recommends == null) 0 else recommends!!.size
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

        val recommend = recommends!![position]

        Glide.with(parent?.context?.applicationContext)
                .load(recommend.sourceImageUrl)
                .placeholder(R.drawable.common_book_cover_default_icon)
                .error(R.drawable.common_book_cover_default_icon)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(viewHolder.img_recommend_cover!!)

        viewHolder.txt_recommend_name?.text = recommend.bookName

        viewHolder.txt_recommend_popularity?.text = AppUtils.getCommonReadNums(recommend.uv)

        return contentView
    }

    fun setData(recommends: List<RecommendBean>) {
        this.recommends = recommends
    }


    private class ViewHolder {
        internal var img_recommend_cover: ImageView? = null
        internal var txt_recommend_name: TextView? = null
        internal var txt_recommend_popularity: TextView? = null
    }
}
