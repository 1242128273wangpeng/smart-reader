package com.dingyue.bookshelf

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.Tools
import java.util.*

/**
 * Created by Administrator on 2017\8\2 0002.
 */

class BookDetailAdapter(private val context: Context) : PagerAdapter() {


    internal var lists: MutableList<Book>? = ArrayList()

    fun setBooks(lists: List<Book>) {
        if (this.lists != null && this.lists!!.size == 0) {
            this.lists!!.clear()
        }
        this.lists!!.addAll(lists)
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return if (lists != null) {
            lists!!.size
        } else 0
    }

    override fun isViewFromObject(arg0: View, arg1: Any): Boolean {
        // TODO Auto-generated method stub
        return arg0 === arg1
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(container.context)
                .inflate(R.layout.item_book_detail, container, false)
        container.addView(view)
        val mAuthor = view.findViewById(R.id.book_shelf_author) as TextView
        val mBookName = view.findViewById(R.id.book_shelf_name) as TextView
        val mIv = view.findViewById(R.id.book_shelf_image) as ImageView

        val mNewChapter = view.findViewById(R.id.book_shelf_new) as TextView
        val mUpdateTime = view.findViewById(R.id.book_shelf_update_time) as TextView

        val book = lists!![position]
        mAuthor.text = "作者：" + book.author
        mBookName.text = book.name
        mUpdateTime.text = Tools.compareTime(AppUtils.formatter, book
                .last_updatetime_native)

        AppLog.e("uuu", book.last_chapter_name + "===")
        mNewChapter.text = "最新章节：" + book.last_chapter_name
        if (!TextUtils.isEmpty(book.img_url)) {
            Glide.with(context).load(book.img_url).placeholder(R.drawable.icon_book_cover_default)
                    .error(R.drawable.icon_book_cover_default).diskCacheStrategy(DiskCacheStrategy.ALL).into(mIv)
        } else {
            Glide.with(context).load(R.drawable.icon_book_cover_default).into(mIv)
        }


        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}
