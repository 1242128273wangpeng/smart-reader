package com.dy.reader.adapter

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.Glide
import com.ding.basic.bean.Book
import com.ding.basic.bean.RecommendBean
import com.dy.reader.R
import com.dy.reader.view.RecommendBookImageView
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.utils.router.BookRouter
import java.util.HashMap

/**
 * Date: 2018/7/13 10:51
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 完结页推荐适配器
 */
class BookEndRecommendAdapter(private val context: Activity) : RecyclerView.Adapter<BookEndRecommendAdapter.ViewHolder>() {
    private var recommendList: ArrayList<RecommendBean> = ArrayList()


    fun setBooks(books: ArrayList<RecommendBean>) {
        if (books.isNotEmpty()) {
            this.recommendList.clear()
            this.recommendList.addAll(books)
            notifyDataSetChanged()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(context).inflate(R.layout.item_bookend_recommend,
                        parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rBook = recommendList[position]
        holder.bookName.text = rBook.bookName

        holder.recommendImage.setLabelText(rBook.genre ?: "")

        if (!TextUtils.isEmpty(rBook.sourceImageUrl)) {

            Glide.with(context)
                    .load(rBook.sourceImageUrl).placeholder(R.drawable.book_cover_default)
                    .error(R.drawable.book_cover_default)
                    .into(holder.recommendImage.getBackGroundImage())

        } else {
            Glide.with(context).load(R.drawable.icon_book_cover_default).into(holder.recommendImage.getBackGroundImage())
        }
        val b = Book()
        b.book_id = rBook.bookId
        b.book_chapter_id = rBook.bookChapterId
        b.book_source_id = rBook.id

        holder.recommendImage.bindBook(b)


    }

    override fun getItemCount(): Int {
        return if (recommendList.isNotEmpty()) recommendList.size else 0
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recommendImage: RecommendBookImageView
        val bookName: TextView

        init {
            recommendImage = itemView.findViewById(R.id.img_book_cover)
            recommendImage.setOnClickListener {
                navigateCoverOrRead(recommendImage)
            }
            bookName = itemView.findViewById(R.id.txt_book_name)

        }

        private fun navigateCoverOrRead(img: RecommendBookImageView) {
            img.getBook()?.let {
                val data = HashMap<String, String>()
                data.put("bookid", it.book_id)
                StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.BOOKENDPAGE_PAGE,
                        StartLogClickUtil.RECOMMENDEDBOOK, data)
                BookRouter.navigateCoverOrRead(context, it,
                        BookRouter.NAVIGATE_TYPE_BOOKEND)
            }
        }

    }
}