package com.dy.reader.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.bumptech.glide.Glide
import com.ding.basic.bean.Book

import com.dy.reader.R
import com.dy.reader.view.RecommendBookImageView
import kotlinx.android.synthetic.qbmfkkydq.item_bookend_recommend.view.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.utils.router.BookRouter
import java.util.*
import kotlin.collections.ArrayList

/**
 * Function：阅读完结页适配器
 *
 * Created by JoannChen on 2018/5/2 0002 15:49
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
class BookEndAdapter(private val activity: Activity) : BaseAdapter() {

    private var books: ArrayList<Book> = ArrayList()

    override fun getCount(): Int {
        return books.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setBooks(books: ArrayList<Book>) {
        this.books.clear()
        this.books.addAll(books)
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bookend_recommend, null)
        val book = books[position]
        Glide.with(activity)
                .load(book.img_url)
                .placeholder(R.drawable.icon_book_cover_default)
                .error(R.drawable.icon_book_cover_default)
                .into(view.img_book_cover.getBackGroundImage())
        view.img_book_cover.setLabelText(book.genre ?: "")
        view.txt_book_name.text = book.name

        view.img_book_cover.setOnClickListener {
            view.img_book_cover.bindBook(book)
            navigateCoverOrRead(view.img_book_cover)
        }

        return view
    }

    private fun navigateCoverOrRead(img: RecommendBookImageView) {
        img.getBook()?.let {
            val data = HashMap<String, String>()
            data.put("bookid", it.book_id)
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.BOOKENDPAGE_PAGE,
                    StartLogClickUtil.RECOMMENDEDBOOK, data)
            BookRouter.navigateCoverOrRead(activity, it,
                    BookRouter.NAVIGATE_TYPE_BOOKEND)
        }
    }
}
