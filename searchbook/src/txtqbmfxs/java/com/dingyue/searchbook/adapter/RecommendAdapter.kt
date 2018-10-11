package com.dingyue.searchbook.adapter

import android.support.v4.content.ContextCompat
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

        holder.tv_book_name.text = book.bookName
        holder.tv_author.text = ("作者：" + book.authorName)
        if (book.serialStatus == "SERIALIZE") {
            holder.tv_status.text = "连载中"
            holder.tv_status.setBackgroundResource(R.drawable.draw_recommend_serialize_bg)
            holder.tv_status.setTextColor(ContextCompat.getColor(context, R.color.search_recommend_lianzai_color))
        } else {
            holder.tv_status.text = "已完结"
            holder.tv_status.setBackgroundResource(R.drawable.draw_recommend_finish_bg)
            holder.tv_status.setTextColor(ContextCompat.getColor(context, R.color.search_recommend_finish_color))
        }

        holder.tv_score.text = (DecimalFormat("0.0").format(book.score) + "分")
        holder.tv_read_num.text = (book.readerCountDescp + "人在读")
        if (!TextUtils.isEmpty(book.genre)) {
            holder.tv_type.visibility = View.VISIBLE
            holder.tv_type.text = book.genre
        } else {
            if (!TextUtils.isEmpty(book.subGenre)) {
                holder.tv_type.visibility = View.VISIBLE
                holder.tv_type.text = book.subGenre
            } else {
                holder.tv_type.visibility = View.GONE
            }

        }

        if (!TextUtils.isEmpty(book.sourceImageUrl)) {
            Glide.with(context).load(book.sourceImageUrl).placeholder(
                    net.lzbook.kit.R.drawable.icon_book_cover_default)
                    .error(net.lzbook.kit.R.drawable.icon_book_cover_default)
                    .into(holder.iv_url)
        } else {
            Glide.with(context).load(
                    net.lzbook.kit.R.drawable.icon_book_cover_default).into(holder.iv_url)
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

        var iv_url: ImageView
        var tv_book_name: TextView
        var tv_author: TextView
        val tv_status: TextView
        val tv_score: TextView
        val tv_read_num: TextView
        val tv_type: TextView

        var index = 0
        var dataBean: SearchRecommendBook.DataBean? = null

        init {
            iv_url = itemView.findViewById(R.id.iv_url)
            tv_book_name = itemView.findViewById(R.id.tv_book_name)
            tv_author = itemView.findViewById(R.id.tv_author)
            tv_status = itemView.findViewById(R.id.tv_status)
            tv_score = itemView.findViewById(R.id.tv_score)
            tv_read_num = itemView.findViewById(R.id.tv_read_num)
            tv_type = itemView.findViewById(R.id.tv_type)

            itemView.setOnClickListener({
                if (dataBean != null) {
                    recommendItemClickListener?.onRecommendItemClick(it, index, dataBean!!)
                }
            })
        }
    }

}
