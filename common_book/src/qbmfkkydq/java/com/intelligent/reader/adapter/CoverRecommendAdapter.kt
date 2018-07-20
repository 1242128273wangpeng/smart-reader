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
import com.ding.basic.bean.RecommendBean
import com.intelligent.reader.R

/**
 * Function：封面页推荐实体类
 *
 * Created by JoannChen on 2018/7/11 0011 18:29
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
class CoverRecommendAdapter(private val context: Context,
                            private val recommendItemClickListener: RecommendItemClickListener,
                            private val books: List<RecommendBean>) :
        RecyclerView.Adapter<CoverRecommendAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(context).inflate(R.layout.item_cover_recommend_grid,
                        parent, false), recommendItemClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = books[position]
        holder.bookName.text = book.bookName
        if (book.readerCountDescp != null && !TextUtils.isEmpty(book.readerCountDescp)) {
            holder.authorName.text = (book.authorName!! )
        } else {
            holder.authorName.text = ""
        }

        if (!TextUtils.isEmpty(book.sourceImageUrl)) {
            Glide.with(context).load(book.sourceImageUrl).placeholder(R.drawable.icon_book_cover_default)
                    .error(R.drawable.icon_book_cover_default)
                    .into(holder.recommendImage)
        } else {
            Glide.with(context).load(R.drawable.icon_book_cover_default).into(holder.recommendImage)
        }


    }

    override fun getItemCount(): Int {
        return if (books.isNotEmpty()) books.size else 0
    }

    interface RecommendItemClickListener {
        fun onRecommendItemClick(view: View, position: Int)
    }

    inner class ViewHolder(itemView: View, private val recommendItemClickListener: RecommendItemClickListener?) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val recommendImage: ImageView
        val bookName: TextView
        val authorName: TextView

        init {
            itemView.setOnClickListener(this)
            recommendImage = itemView.findViewById(R.id.iv_recommend_image)
            bookName = itemView.findViewById(R.id.tv_book_name)
            authorName = itemView.findViewById(R.id.txt_author)

        }

        override fun onClick(v: View) {
            recommendItemClickListener?.onRecommendItemClick(v, position)
        }
    }
}
