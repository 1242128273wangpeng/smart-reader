package com.dingyue.searchbook.adapter

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import anet.channel.util.Utils.context
import com.bumptech.glide.Glide
import com.ding.basic.bean.SearchRecommendBook
import com.dingyue.searchbook.R
import java.text.DecimalFormat


/**
 * Desc 推荐书籍适配器
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/20 0020 15:31
 */
class RecommendAdapter(val books: List<SearchRecommendBook.DataBean>,
                       private val recommendItemClickListener: RecommendItemClickListener) :
        RecyclerView.Adapter<RecommendAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(
                LayoutInflater.from(context).inflate(
                        R.layout.item_recommend, parent, false), recommendItemClickListener)
    }

    override fun onBindViewHolder(holder: RecommendAdapter.ViewHolder, position: Int) {
        val book = books[position]
        holder.dataBean = book
        holder.index = position

        holder.txt_book_name.text = book.bookName
        holder.txt_book_author.text = book.authorName

        if (book.serialStatus == "SERIALIZE") {
            holder.img_book_status.setBackgroundResource(R.drawable.search_book_lianzai)
        } else {
            holder.img_book_status.setBackgroundResource(R.drawable.search_book_over)
        }

        if (TextUtils.isEmpty(book.description)) {
            holder.txt_book_content.text = "暂无简介"
        } else {
            holder.txt_book_content.text = book.description
        }

        if (book.score == 0.0) {
            holder.txt_book_score.visibility = View.GONE
        } else {
            holder.txt_book_score.visibility = View.VISIBLE
            holder.txt_book_score.text = ((DecimalFormat("0.0").format(book.score)) + "分")
        }

        holder.txt_read_num.text = (book.readerCountDescp.toString() + "人气")
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
            Glide.with(context).load(book.sourceImageUrl)
                    .placeholder(R.drawable.book_cover_default)
                    .error(R.drawable.book_cover_default)
                    .into(holder.img_book_cover)
        } else {
            Glide.with(context).load(
                    R.drawable.book_cover_default).into(holder.img_book_cover)
        }

    }


    override fun getItemCount(): Int {
        return if (books.isNotEmpty()) books.size else 0
    }

    interface RecommendItemClickListener {
        fun onRecommendItemClick(view: View, position: Int, dataBean: SearchRecommendBook.DataBean)
    }


    class ViewHolder(
            itemView: View,
            recommendItemClickListener: RecommendItemClickListener?) : RecyclerView.ViewHolder(itemView) {

        var img_book_cover: ImageView
        var img_book_status: ImageView
        var txt_book_name: TextView
        var txt_book_author: TextView
        var txt_book_score: TextView
        var txt_read_num: TextView
        var txt_book_type: TextView
        var txt_book_content: TextView

        var index = 0
        var dataBean: SearchRecommendBook.DataBean? = null

        init {

            img_book_cover = itemView.findViewById(R.id.img_book_cover)
            txt_book_name = itemView.findViewById(R.id.txt_book_name)
            txt_book_author = itemView.findViewById(R.id.txt_book_author)
            img_book_status = itemView.findViewById(R.id.img_book_status)
            txt_book_score = itemView.findViewById(R.id.txt_book_score)
            txt_read_num = itemView.findViewById(R.id.txt_read_num)
            txt_book_type = itemView.findViewById(R.id.txt_book_type)
            txt_book_content = itemView.findViewById(R.id.txt_book_content)

            itemView.setOnClickListener({
                if (dataBean != null) {
                    recommendItemClickListener?.onRecommendItemClick(it, index, dataBean!!)
                }
            })
        }
    }

}
