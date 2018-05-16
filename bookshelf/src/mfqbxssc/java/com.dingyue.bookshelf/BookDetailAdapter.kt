package com.dingyue.bookshelf

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.mfqbxssc.item_book_detail.view.*
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.Tools
import java.util.*

/**
 * Created by Administrator on 2017\8\2 0002
 */

class BookDetailAdapter(private val context: Context) : PagerAdapter() {

    private var books: ArrayList<Book> = ArrayList()

    fun update(books: List<Book>) {
        this.books.clear()
        this.books.addAll(books)
        notifyDataSetChanged()
    }

    override fun getCount(): Int = books.size

    override fun isViewFromObject(arg0: View, arg1: Any): Boolean {
        return arg0 === arg1
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context)
                .inflate(R.layout.item_book_detail, container, false)
        container.addView(view)

        val book = books[position]
        val bookAuthor = "作者：${book.author}"
        view.txt_book_author.text = bookAuthor
        view.txt_book_name.text = book.name
        view.txt_book_update_time.text = Tools.compareTime(AppUtils.formatter, book
                .last_updatetime_native)

        val latestChapter = "最新章节：${book.last_chapter_name}"
        view.txt_book_chapter.text = latestChapter

        if (book.img_url.isNotEmpty()) {
            Glide.with(context)
                    .load(book.img_url)
                    .placeholder(R.drawable.icon_book_cover_default)
                    .error(R.drawable.icon_book_cover_default)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(view.img_book_cover)
        } else {
            Glide.with(context)
                    .load(R.drawable.icon_book_cover_default)
                    .into(view.img_book_cover)
        }

        return view
    }


    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getItemPosition(`object`: Any?): Int {
        return PagerAdapter.POSITION_NONE
    }
}