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


/**
 * Desc 搜索模块推荐书籍适配器
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
                        R.layout.item_recommend, parent, false),recommendItemClickListener)
    }

    override fun onBindViewHolder(holder: RecommendAdapter.ViewHolder, position: Int) {
        val book = books[position]
        holder.dataBean = book
        holder.index = position
        holder.tv_book_name.text = book.bookName

        if (!TextUtils.isEmpty(book.sourceImageUrl)) {
            Glide.with(context).load(book.sourceImageUrl).placeholder(
                    net.lzbook.kit.R.drawable.icon_book_cover_default)
                    .error(R.drawable.icon_book_cover_default)
                    .into(holder.iv_url)
        } else {
            Glide.with(context).load(
                    R.drawable.icon_book_cover_default).into(holder.iv_url)
        }
    }


    override fun getItemCount(): Int {
         return if (books.isNotEmpty()) {
            if (books.size >= 5) {
                6
            } else {
                books.size
            }
        } else {
            0
        }
    }

    interface RecommendItemClickListener {
        fun onRecommendItemClick(view: View, position: Int,dataBean:SearchRecommendBook.DataBean)
    }


    class ViewHolder(
            itemView: View,
            recommendItemClickListener: RecommendItemClickListener?) : RecyclerView.ViewHolder(itemView){

        val iv_url: ImageView
        val tv_book_name: TextView

        var index = 0
        var dataBean: SearchRecommendBook.DataBean? = null

        init {

            iv_url = itemView.findViewById(R.id.iv_url)
            tv_book_name = itemView.findViewById(R.id.tv_book_name)

            itemView.setOnClickListener({
               if (dataBean!= null){
                   recommendItemClickListener?.onRecommendItemClick(it,index,dataBean!!)
               }
            })
        }
    }

}
