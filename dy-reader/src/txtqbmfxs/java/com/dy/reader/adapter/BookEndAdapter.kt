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

import com.dy.reader.R
import com.dy.reader.view.RecommendBookImageView

import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.utils.router.BookRouter

class BookEndAdapter(private val mContext: Activity) : BaseAdapter() {

    private var books: java.util.ArrayList<Book>? = null
    fun setBooks(books: java.util.ArrayList<Book>?) {
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
            viewHolder.imgCover = contentView?.findViewById(R.id.recommend_book)
            viewHolder.txtName = contentView?.findViewById(R.id.recommend_book_name)
            contentView.tag = viewHolder
        } else {
            viewHolder = contentView.tag as ViewHolder
        }

        val book = books!![position]

        Glide.with(parent?.context?.applicationContext)
                .load(book.img_url)
                .placeholder(R.drawable.common_book_cover_default_icon)
                .error(R.drawable.common_book_cover_default_icon)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(viewHolder.imgCover?.getBackGroundImage())

        viewHolder.txtName?.text = book.name

        viewHolder.imgCover?.setLabelText(if (book.label == null) "" else book.label!!)
        viewHolder.imgCover?.setOnClickListener {
            viewHolder.imgCover?.let {
                it.bindBook(book)
                intentCoverPage(it)
            }
        }

        return contentView
    }

    private class ViewHolder {
        internal var imgCover: RecommendBookImageView? = null
        internal var txtName: TextView? = null
    }

    private fun intentCoverPage(iv: RecommendBookImageView) {
        if (iv.getBook() != null) {
            val goCoverInfo = HashMap<String, String>()
            goCoverInfo.put("bookid", iv.getBook()!!.book_id)
            StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(), StartLogClickUtil.READFINISH, StartLogClickUtil.RECOMMENDEDBOOK, goCoverInfo)
            BookRouter.navigateCoverOrRead(mContext, iv.getBook()!!, BookRouter.NAVIGATE_TYPE_BOOKEND)
        }
    }
}