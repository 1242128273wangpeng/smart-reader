package com.intelligent.reader.adapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ding.basic.bean.Book;
import com.ding.basic.bean.RecommendBean;
import com.intelligent.reader.R;


import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * @author lijun Lee
 * @desc 推荐书籍
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/3/6 17:11
 */

public class BookRecommendAdapter extends BaseAdapter {

    private List<RecommendBean> mList;

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder hodler;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cover_recommend_layout, parent, false);
            hodler = new ViewHolder();
            hodler.book_img_iv = (ImageView) convertView.findViewById(R.id.book_img_iv);
            hodler.book_name_tv = (TextView) convertView.findViewById(R.id.book_name_tv);
            hodler.book_info_tv = (TextView) convertView.findViewById(R.id.book_info_tv);
            hodler.book_author_tv = (TextView) convertView.findViewById(R.id.book_author_tv);
            hodler.book_cover_status = (TextView) convertView.findViewById(R.id.book_cover_status);
            hodler.book_cover_category = (TextView) convertView.findViewById(R.id.book_cover_category);
            hodler.book_cover_nomore = (TextView) convertView.findViewById(R.id.cover_recomm_nomore);
            convertView.setTag(hodler);
        } else {
            hodler = (ViewHolder) convertView.getTag();
        }
        RecommendBean book = mList.get(position);
        Glide.with(parent.getContext().getApplicationContext()).load(book.getSourceImageUrl())
                .placeholder(net.lzbook.kit.R.drawable.icon_book_cover_default)
                .error(net.lzbook.kit.R.drawable.icon_book_cover_default)
                .diskCacheStrategy(DiskCacheStrategy.ALL).into(hodler.book_img_iv);
        hodler.book_name_tv.setText(book.getBookName());
        hodler.book_info_tv.setText(book.getDescription());
        hodler.book_author_tv.setText(book.getAuthorName());

        if (!TextUtils.isEmpty(book.getSubGenre())) {
            hodler.book_cover_category.setText(book.getSubGenre());
        } else {
            hodler.book_cover_category.setText("未分类");
        }

        if ("FINISH".equals(book.getSerialStatus())) {
            hodler.book_cover_status.setText(parent.getContext().getString(R.string.book_cover_state_writing));
        } else {
            hodler.book_cover_status.setText(parent.getContext().getString(R.string.book_cover_state_written));
        }

        if (position + 1 == getCount()) {
            hodler.book_cover_nomore.setVisibility(View.VISIBLE);
        } else {
            hodler.book_cover_nomore.setVisibility(View.GONE);
        }
        return convertView;
    }

    public void setData(List<RecommendBean> data) {
        this.mList = data;
        notifyDataSetChanged();
    }


    private static class ViewHolder {
        ImageView book_img_iv;
        TextView book_name_tv;
        TextView book_info_tv;
        TextView book_author_tv;
        TextView book_cover_status;
        TextView book_cover_category;
        TextView book_cover_nomore;
    }
}
