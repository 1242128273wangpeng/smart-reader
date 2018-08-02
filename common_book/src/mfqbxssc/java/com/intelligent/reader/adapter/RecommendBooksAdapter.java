package com.intelligent.reader.adapter;

import com.bumptech.glide.Glide;
import com.intelligent.reader.R;

import net.lzbook.kit.data.search.SearchRecommendBook;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lijun Lee
 * @desc 搜索推荐书籍
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/12/5 11:48
 */

public class RecommendBooksAdapter extends RecyclerView.Adapter<RecommendBooksAdapter.ViewHolder> {

    private WeakReference<Context> weakReference;
    private RecommendItemClickListener recommendItemClickListener;
    private List<SearchRecommendBook.DataBean> books = new ArrayList<>();

    public RecommendBooksAdapter(Context context, RecommendItemClickListener recommendItemClickListener, List<SearchRecommendBook.DataBean> books) {
        this.weakReference = new WeakReference<>(context);
        this.recommendItemClickListener = recommendItemClickListener;
        this.books = books;

    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(weakReference.get()).inflate(R.layout.item_search_recommend, parent, false), recommendItemClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SearchRecommendBook.DataBean book = books.get(position);
        holder.tv_book_name.setText(book.getBookName());
        holder.tv_book_author.setText(book.getAuthorName());
        if (holder.iv_url != null && !TextUtils.isEmpty(book.getSourceImageUrl())) {
            Glide.with(weakReference.get()).load(book.getSourceImageUrl()).placeholder(net.lzbook.kit.R.drawable.icon_book_cover_default)
                    .error((net.lzbook.kit.R.drawable.icon_book_cover_default))
                    .into(holder.iv_url);
        } else {
            Glide.with(weakReference.get()).load(net.lzbook.kit.R.drawable.icon_book_cover_default).into(holder.iv_url);
        }


    }

    @Override
    public int getItemCount() {
        if (books.size() != 0) {
            return books.size();
        }
        return 0;
    }

    public interface RecommendItemClickListener {
        void onItemClick(View view, int position, List<SearchRecommendBook.DataBean> books);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private RecommendItemClickListener recommendItemClickListener;
        private ImageView iv_url;
        private TextView tv_book_name;
        private TextView tv_book_author;

        public ViewHolder(View itemView, RecommendItemClickListener recommendItemClickListener) {
            super(itemView);
            this.recommendItemClickListener = recommendItemClickListener;
            itemView.setOnClickListener(this);
            iv_url = (ImageView) itemView.findViewById(R.id.iv_url);
            tv_book_name = (TextView) itemView.findViewById(R.id.tv_book_name);
            tv_book_author = (TextView) itemView.findViewById(R.id.tv_book_auther);

        }

        @Override
        public void onClick(View v) {

            if (recommendItemClickListener != null) {
                recommendItemClickListener.onItemClick(v, getPosition(), books);
            }
        }
    }

}
