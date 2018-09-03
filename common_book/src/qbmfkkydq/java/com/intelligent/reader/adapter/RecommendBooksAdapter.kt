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

import java.lang.ref.WeakReference

/**
 * Function：搜索推荐书籍
 *
 * Created by JoannChen on 2018/7/19 0019 16:28
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
class RecommendBooksAdapter(context: Context,
                            private val recommendItemClickListener: RecommendItemClickListener,
                            val books: List<SearchRecommendBook.DataBean>) : RecyclerView.Adapter<RecommendBooksAdapter.ViewHolder>() {

    private val weakReference: WeakReference<Context> = WeakReference(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(weakReference.get()).inflate(R.layout.item_search_recommend,
                        parent, false), recommendItemClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = books[position]
        holder.bookNameText.text = book.bookName
        holder.authorText.text = book.authorName
        if (!TextUtils.isEmpty(book.sourceImageUrl)) {
            Glide.with(weakReference.get()).load(book.sourceImageUrl).placeholder(
                    net.lzbook.kit.R.drawable.icon_book_cover_default)
                    .error(net.lzbook.kit.R.drawable.icon_book_cover_default)
                    .into(holder.urlImageView)
        } else {
            Glide.with(weakReference.get()).load(
                    net.lzbook.kit.R.drawable.icon_book_cover_default).into(holder.urlImageView)
        }

    }

    override fun getItemCount(): Int {
        return books.size
    }

    interface RecommendItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    inner class ViewHolder(itemView: View, private val recommendItemClickListener: RecommendItemClickListener?) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val urlImageView: ImageView
        val bookNameText: TextView
        val authorText: TextView

        init {
            itemView.setOnClickListener(this)
            urlImageView = itemView.findViewById(R.id.iv_url)
            bookNameText = itemView.findViewById(R.id.tv_book_name)
            authorText = itemView.findViewById(R.id.tv_author)

        }

        override fun onClick(v: View) {

            recommendItemClickListener?.onItemClick(v, position)
        }
    }

}
