package com.dy.reader.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ding.basic.bean.Book
import com.ding.basic.bean.RecommendBean
import com.dy.reader.R
import com.dy.reader.view.RecommendBookImageView

class BookRecommendAdapter(private val mContext: Activity) : BaseAdapter() {

    private var books: java.util.ArrayList<Book>? = null
    fun setBooks(books: java.util.ArrayList<Book>) {
        this.books = books
    }

    override fun getCount(): Int {
        return if (books == null) 0 else books!!.size
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
            viewHolder.img_recommend_cover = contentView?.findViewById(R.id.recommend_book)
            viewHolder.txt_recommend_name = contentView?.findViewById(R.id.recommend_book_name)
            contentView.tag = viewHolder
        } else {
            viewHolder = contentView.tag as ViewHolder
        }

        val recommend = books!![position]

        Glide.with(parent?.context?.applicationContext)
                .load(recommend.img_url)
                .placeholder(R.drawable.common_book_cover_default_icon)
                .error(R.drawable.common_book_cover_default_icon)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(viewHolder.img_recommend_cover?.getBackGroundImage())

        viewHolder.txt_recommend_name?.text = recommend.name


        return contentView
    }

    private class ViewHolder {
        internal var img_recommend_cover: RecommendBookImageView? = null
        internal var txt_recommend_name: TextView? = null
    }
}