package com.intelligent.reader.adapter
/*

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.bumptech.glide.Glide
import com.intelligent.reader.R
import com.intelligent.reader.app.BookApplication
import com.intelligent.reader.read.help.BookHelper
import com.intelligent.reader.view.RecommendBookImageView
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.bean.Book
import java.util.ArrayList
import java.util.HashMap

*/
/**
 * Function：阅读完结页适配器
 *
 * Created by JoannChen on 2018/5/2 0002 15:49
 * E-mail:yongzuo_chen@dingyuegroup.cn
 *//*

class BookEndAdapter(private val mContext: Activity) : BaseAdapter() {

    private var books: ArrayList<Book>? = null


    override fun getCount(): Int {
        return if (books == null) 0 else books!!.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setBooks(books: ArrayList<Book>) {
        this.books = books
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bookend_recommend, null)
        val iv = view.findViewById(R.id.recommend_book) as RecommendBookImageView
        val tv = view.findViewById(R.id.recommend_book_name) as TextView
        if (books != null) {
            val book = books!![position]
            Glide.with(mContext).load(book.img_url).placeholder(R.drawable.icon_book_cover_default).error(R.drawable.icon_book_cover_default).into(iv.getBackGroundImage())
            iv.setLabelText(if (book.category == null) "" else book.category)
            tv.text = book.name
            iv.setOnClickListener {
                iv.bindBook(book)
                goCover(iv)
            }
        }
        return view
    }

    private fun goCover(iv: RecommendBookImageView) {
        if (iv.getBook() != null) {
            val goCoverInfo = HashMap<String, String>()
            goCoverInfo.put("bookid", iv.getBook()!!.book_id)
            StartLogClickUtil.upLoadEventLog(BookApplication.getGlobalContext(), StartLogClickUtil.READFINISH, StartLogClickUtil.RECOMMENDEDBOOK, goCoverInfo)
            BookHelper.goToCoverOrRead(BookApplication.getGlobalContext(), mContext, iv.getBook(), 5)
        }
    }
}
*/
