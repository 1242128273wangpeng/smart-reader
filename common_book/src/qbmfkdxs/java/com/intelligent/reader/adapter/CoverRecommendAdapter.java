package com.intelligent.reader.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ding.basic.bean.Book;
import com.intelligent.reader.R;

import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.utils.AppUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhenXiang on 2017/10/30.
 */

public class CoverRecommendAdapter extends RecyclerView.Adapter<CoverRecommendAdapter.ViewHolder> {

    private WeakReference<Context> weakReference;
    private RecommendItemClickListener recommendItemClickListener;
    private List<Book> books = new ArrayList<>();

    public CoverRecommendAdapter(Context context, RecommendItemClickListener recommendItemClickListener, List<Book> books) {
        this.weakReference = new WeakReference<>(context);
        this.recommendItemClickListener = recommendItemClickListener;
        this.books = books;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(weakReference.get()).inflate(R.layout.item_cover_recommend_grid, parent, false), recommendItemClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Book book = books.get(position);
        holder.tv_book_name.setText(book.getName());

        if (book.fromQingoo()) {
            holder.tv_readnum.setText(AppUtils.getCommonReadNums(Long.valueOf(book.getUv())));
        } else {

            // 临时使用desc接收v5接口自有人气字段
            if (book.getDesc() != null && !TextUtils.isEmpty(book.getDesc()) && !book.getDesc().equals("null")) {
                holder.tv_readnum.setText(book.getDesc() + "人气值");
            } else {
                holder.tv_readnum.setText("");
            }
        }

        if (holder.iv_recommend_image != null && !TextUtils.isEmpty(book.getImg_url())) {
            Glide.with(weakReference.get()).load(book.getImg_url()).placeholder(net.lzbook.kit.R.drawable.icon_book_cover_default)
                    .error((net.lzbook.kit.R.drawable.icon_book_cover_default))
                    .into(holder.iv_recommend_image);
        } else {
            Glide.with(weakReference.get()).load(net.lzbook.kit.R.drawable.icon_book_cover_default).into(holder.iv_recommend_image);
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
        void onItemClick(View view, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private RecommendItemClickListener recommendItemClickListener;
        private ImageView iv_recommend_image;
        private TextView tv_book_name, tv_readnum;

        public ViewHolder(View itemView, RecommendItemClickListener recommendItemClickListener) {
            super(itemView);
            this.recommendItemClickListener = recommendItemClickListener;
            itemView.setOnClickListener(this);
            iv_recommend_image =  itemView.findViewById(R.id.iv_recommend_image);
            tv_book_name =  itemView.findViewById(R.id.tv_book_name);
            tv_readnum =  itemView.findViewById(R.id.tv_readnum);

        }

        @Override
        public void onClick(View v) {

            if (recommendItemClickListener != null) {
                recommendItemClickListener.onItemClick(v, getPosition());
            }
        }
    }
}
