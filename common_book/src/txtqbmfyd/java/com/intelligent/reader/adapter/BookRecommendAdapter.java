package com.intelligent.reader.adapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.intelligent.reader.R;

import net.lzbook.kit.data.bean.Book;

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

    private List<Book> mList;

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
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_recommend_item_layout, parent, false);
            hodler = new ViewHolder();
            hodler.book_img_iv = (ImageView) convertView.findViewById(R.id.book_img_iv);
            hodler.book_name_tv = (TextView) convertView.findViewById(R.id.book_name_tv);
            hodler.book_info_tv = (TextView) convertView.findViewById(R.id.book_info_tv);
            hodler.book_author_tv = (TextView) convertView.findViewById(R.id.book_author_tv);
            hodler.book_cover_status = (TextView) convertView.findViewById(R.id.book_cover_status);
            hodler.book_cover_category = (TextView) convertView.findViewById(R.id.book_cover_category);
            convertView.setTag(hodler);
        } else {
            hodler = (ViewHolder) convertView.getTag();
        }
        Book book = mList.get(position);
        Glide.with(parent.getContext().getApplicationContext()).load(book.img_url)
                .placeholder(R.drawable.common_book_cover_default_icon)
                .error(R.drawable.common_book_cover_default_icon)
                .diskCacheStrategy(DiskCacheStrategy.ALL).into(hodler.book_img_iv);
        hodler.book_name_tv.setText(book.name);
        hodler.book_info_tv.setText(book.desc);
        hodler.book_author_tv.setText(book.author);

        if (!TextUtils.isEmpty(book.category)) {
            hodler.book_cover_category.setText(book.category);
        } else {
            hodler.book_cover_category.setText("未分类");
        }

        if (1 == book.status) {
            hodler.book_cover_status.setText(parent.getContext().getString(R.string.book_cover_state_writing));
        } else {
            hodler.book_cover_status.setText(parent.getContext().getString(R.string.book_cover_state_written));
        }
        return convertView;
    }

    public void setData(List<Book> data) {
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
    }
}
