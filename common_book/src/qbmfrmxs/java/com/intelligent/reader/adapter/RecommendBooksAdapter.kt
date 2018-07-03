package com.intelligent.reader.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.bumptech.glide.Glide
import com.ding.basic.bean.SearchRecommendBook
import com.intelligent.reader.R
import net.lzbook.kit.utils.AppUtils

import java.lang.ref.WeakReference
import java.text.DecimalFormat
import java.util.ArrayList

/**
 * @author lijun Lee
 * @desc 搜索推荐书籍
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/12/5 11:48
 */

class RecommendBooksAdapter(context: Context,
                            private val recommendItemClickListener: RecommendItemClickListener,
                            books: List<SearchRecommendBook.DataBean>) : RecyclerView.Adapter<RecommendBooksAdapter.ViewHolder>() {

    private val weakReference: WeakReference<Context>
    private var books = ArrayList<SearchRecommendBook.DataBean>()

    init {
        this.weakReference = WeakReference(context)
        this.books = books as ArrayList<SearchRecommendBook.DataBean>

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(weakReference.get()).inflate(R.layout.item_search_recommend,
                        parent, false), recommendItemClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = books[position]
        holder.txt_book_name.text = book.bookName
        holder.txt_book_author.text = book.authorName
        if (book.serialStatus == "SERIALIZE") {
            holder.img_book_status.setBackgroundResource(R.drawable.search_book_lianzai)
        } else {
            holder.img_book_status.setBackgroundResource(R.drawable.search_book_over)
        }

        if(TextUtils.isEmpty(book.description)){
            holder.txt_book_content.text = "暂无简介"
        }else{
            holder.txt_book_content.text = book.description
        }

        if(book.score == 0.0){
            holder.txt_book_score.visibility = View.GONE
        }else{
            holder.txt_book_score.visibility = View.VISIBLE
            holder.txt_book_score.text = (DecimalFormat("0.0").format(book.score)) + "分"
        }

        holder.txt_read_num.text = AppUtils.getCommonReadNums(book.readerCount.toLong())
        if (!TextUtils.isEmpty(book.genre)) {
            holder.txt_book_type.visibility = View.VISIBLE
            holder.txt_book_type.text = book.genre
        } else {
            if (!TextUtils.isEmpty(book.subGenre)) {
                holder.txt_book_type.visibility = View.VISIBLE
                holder.txt_book_type.text = book.subGenre
            } else {
                holder.txt_book_type.visibility = View.GONE
            }

        }

        if (!TextUtils.isEmpty(book.sourceImageUrl)) {
            Glide.with(weakReference.get()).load(book.sourceImageUrl).placeholder(
                    net.lzbook.kit.R.drawable.icon_book_cover_default)
                    .error(net.lzbook.kit.R.drawable.icon_book_cover_default)
                    .into(holder.img_book_cover)
        } else {
            Glide.with(weakReference.get()).load(
                    net.lzbook.kit.R.drawable.icon_book_cover_default).into(holder.img_book_cover)
        }


    }

    override fun getItemCount(): Int {
        return if (books.size != 0) {
            books.size
        } else 0
    }

    interface RecommendItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    inner class ViewHolder(itemView: View, private val recommendItemClickListener: RecommendItemClickListener?) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val img_book_cover: ImageView
        val img_book_status: ImageView
        val txt_book_name: TextView
        val txt_book_author: TextView
        val txt_book_score: TextView
        val txt_read_num: TextView
        val txt_book_type: TextView
        val txt_book_content:TextView

        init {
            itemView.setOnClickListener(this)
            img_book_cover = itemView.findViewById<View>(R.id.img_book_cover) as ImageView
            txt_book_name = itemView.findViewById<View>(R.id.txt_book_name) as TextView
            txt_book_author = itemView.findViewById<View>(R.id.txt_book_author) as TextView
            img_book_status = itemView.findViewById<View>(R.id.img_book_status) as ImageView
            txt_book_score = itemView.findViewById<View>(R.id.txt_book_score) as TextView
            txt_read_num = itemView.findViewById<View>(R.id.txt_read_num) as TextView
            txt_book_type = itemView.findViewById<View>(R.id.txt_book_type) as TextView
            txt_book_content = itemView.findViewById<View>(R.id.txt_book_content) as TextView
        }

        override fun onClick(v: View) {

            recommendItemClickListener?.onItemClick(v, position)
        }
    }
}