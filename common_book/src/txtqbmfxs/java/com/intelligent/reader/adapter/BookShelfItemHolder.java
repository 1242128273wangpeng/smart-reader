package com.intelligent.reader.adapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.intelligent.reader.R;
import com.intelligent.reader.adapter.holder.AbsRecyclerViewHolder;

import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.Tools;

import android.content.res.Resources;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Administrator on 2017/4/13 0013.
 */

public class BookShelfItemHolder extends AbsRecyclerViewHolder<Book> {

    private final ImageView mCheck;
    private final ImageView mImage;
    private final TextView mName, mUpdateTime, mUnread, mLastChapter, mUpdate;


    public BookShelfItemHolder(View itemView,
                               BookShelfReAdapter.ShelfItemClickListener shelfItemClickListener,
                               BookShelfReAdapter.ShelfItemLongClickListener shelfItemLongClickListener) {
        super(itemView, shelfItemClickListener, shelfItemLongClickListener);

        mCheck = (ImageView) itemView.findViewById(R.id.check_delete);
        mImage = (ImageView) itemView.findViewById(R.id.book_shelf_image);
        mUpdate = (TextView) itemView.findViewById(R.id.book_shelf_status_update);
        mName = (TextView) itemView.findViewById(R.id.book_shelf_name);
        mUpdateTime = (TextView) itemView.findViewById(R.id.book_shelf_update_time);
        mLastChapter = (TextView) itemView.findViewById(R.id.book_shelf_last_chapter);
        mUnread = (TextView) itemView.findViewById(R.id.book_shelf_unread);

    }

    @Override
    public void onBindData(int position, Book book, boolean update, boolean isRemoveMode, boolean
            removeMark) {
        if (!TextUtils.isEmpty(book.name))
            this.mName.setText(book.name);

        if (book.sequence + 1 > book.chapter_count) {
            book.sequence = book.chapter_count - 1;
        }

        if (book.sequence >= 0) {
            this.mUnread.setText(book.sequence + 1 + "/" + book.chapter_count + "章");
        } else {
            this.mUnread.setText("未读");
        }

//        if (book.sequence == -2) {
//            book.sequence = -1;
//        }
//        int update_count;
//        update_count = book.chapter_count - (book.sequence + 1);
//
//        //快读全本电子书 和 TXT全本小说阅读器 隐藏书架未读 for 差异化上线
//        if ("cn.zsqbydq.reader".equals(AppUtils.getPackageName()) || "cn.kkqbtxtxs.reader".equals(AppUtils.getPackageName())) {
//            this.mUnread.setVisibility(View.GONE);
//        } else {
//            if (update_count == 0) {
//                this.mUnread.setVisibility(View.GONE);
//            } else {
//                this.mUnread.setVisibility(View.VISIBLE);
//                this.mUnread.setText(String.valueOf(update_count) + "章");
//            }
//        }

        // 是否连载
        if (book.status == 2) {
            this.mLastChapter.setText("已完结");
        } else {
            if (!TextUtils.isEmpty(book.last_chapter_name))
                this.mLastChapter.setText(book.last_chapter_name);
        }
        // 是否有更新
        if (!update) {
            this.mUpdate.setVisibility(View.GONE);
        } else {
            this.mUpdate.setVisibility(View.VISIBLE);
        }
        if (this.mUpdateTime != null) {
            this.mUpdateTime.setText(Tools.compareTime(AppUtils.formatter, book
                    .last_updatetime_native));
        }

        if (!TextUtils.isEmpty(book.img_url) && !book.img_url.equals(ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL)) {
            Glide.with(itemView.getContext().getApplicationContext()).load(book.img_url).placeholder(R.drawable.icon_book_cover_default).error((R.drawable.icon_book_cover_default)).diskCacheStrategy(DiskCacheStrategy.ALL).into(this.mImage);
        } else {
            Glide.with(itemView.getContext().getApplicationContext()).load(R.drawable.icon_book_cover_default).into(this.mImage);
        }


        if (isRemoveMode) {
            this.mCheck.setVisibility(View.VISIBLE);
            int typeColor = 0;
            if (removeMark) {
                typeColor = R.mipmap.bookshelf_delete_checked;
            } else {
                typeColor = R.mipmap.bookshelf_delete_unchecked;
            }
            this.mCheck.setBackgroundResource(typeColor);
        } else {
            this.mCheck.setVisibility(View.GONE);
        }
    }
}
