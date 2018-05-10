package com.dingyue.bookshelf;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.Tools;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017\8\2 0002.
 */

public class BookDetailAdapter extends PagerAdapter {


    List<Book> lists = new ArrayList<>();
    private Context context;

    public BookDetailAdapter( Context context) {
        this.context = context;
    }

    public void setBooks(List<Book> lists){
        if(this.lists != null && this.lists.size() == 0){
            this.lists.clear();
        }
        this.lists.addAll(lists);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (lists != null) {
            return lists.size();
        }
        return 0;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        // TODO Auto-generated method stub
        return arg0 == arg1;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext())
                .inflate(R.layout.item_book_detail, container, false);
        container.addView(view);
        TextView mAuthor = (TextView) view.findViewById(R.id.book_shelf_author);
        TextView mBookName = (TextView) view.findViewById(R.id.book_shelf_name);
        ImageView mIv = (ImageView) view.findViewById(R.id.book_shelf_image);

        TextView mNewChapter = (TextView) view.findViewById(R.id.book_shelf_new);
        TextView mUpdateTime = (TextView) view.findViewById(R.id.book_shelf_update_time);

        Book book = lists.get(position);
        mAuthor.setText("作者：" + book.author);
        mBookName.setText(book.name);
        mUpdateTime.setText(Tools.compareTime(AppUtils.formatter, book
                .last_updatetime_native));

        AppLog.e("uuu", book.last_chapter_name + "===");
        mNewChapter.setText("最新章节：" + book.last_chapter_name);
        if (!TextUtils.isEmpty(book.img_url)) {
            Glide.with(context).load(book.img_url).placeholder(R.drawable.icon_book_cover_default)
                    .error((R.drawable.icon_book_cover_default)).diskCacheStrategy(DiskCacheStrategy.ALL).into(mIv);
        } else {
            Glide.with(context).load(R.drawable.icon_book_cover_default).into(mIv);
        }


        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
