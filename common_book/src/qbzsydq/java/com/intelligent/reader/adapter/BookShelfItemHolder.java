package com.intelligent.reader.adapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ding.basic.bean.Book;
import com.intelligent.reader.R;
import com.intelligent.reader.adapter.holder.AbsRecyclerViewHolder;

import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.Tools;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.MessageFormat;

/**
 * Created by Administrator on 2017/4/13 0013.
 */

public class BookShelfItemHolder extends AbsRecyclerViewHolder<Book> {

    private final ImageView mCheck;
    private final ImageView mImage;
    private final TextView mName, mUpdateTime, mUnread, mLastChapter;
    private final View mFinish, mUpdate, mUnreadLayout;

    public BookShelfItemHolder(View itemView,
                               BookShelfReAdapter.ShelfItemClickListener shelfItemClickListener,
                               BookShelfReAdapter.ShelfItemLongClickListener shelfItemLongClickListener) {
        super(itemView, shelfItemClickListener, shelfItemLongClickListener);

        mCheck = (ImageView) itemView.findViewById(R.id.check_delete);
        mImage = (ImageView) itemView.findViewById(R.id.book_shelf_image);

        mFinish = itemView.findViewById(R.id.book_shelf_status_finish);
        mUpdate = itemView.findViewById(R.id.book_shelf_status_update);
        mName = (TextView) itemView.findViewById(R.id.book_shelf_name);
        mUpdateTime = (TextView) itemView.findViewById(R.id.book_shelf_update_time);
        mLastChapter = (TextView) itemView.findViewById(R.id.book_shelf_last_chapter);
        mUnread = (TextView) itemView.findViewById(R.id.book_shelf_unread);
        mUnreadLayout = itemView.findViewById(R.id.rl_unread);

    }

    @Override
    public void onBindData(int position, Book book, boolean update, boolean isRemoveMode, boolean removeMark) {
        if (!TextUtils.isEmpty(book.getName())) {
            this.mName.setText(book.getName());
        }
        this.mUnreadLayout.setVisibility(View.GONE);

        if (book.getLast_chapter() != null) {

            int count;

            if (book.getSequence() <= 0) {
                count = book.getChapter_count();
            } else {
                count = book.getChapter_count() - (book.getSequence() + 1);
            }

            if (count > 0) {
                this.mUnreadLayout.setVisibility(View.VISIBLE);
                this.mUnread.setText(MessageFormat.format("{0}章", count));
            }

            this.mLastChapter.setText(book.getLast_chapter().getName());

            if (this.mUpdateTime != null) {
                this.mUpdateTime.setText(MessageFormat.format("{0}更新", Tools.compareTime(AppUtils.formatter, book.getLast_chapter().getUpdate_time())));
            }
        }

        if ("FINISH".equals(book.getStatus())) {
            this.mFinish.setVisibility(View.VISIBLE);
        } else {
            this.mFinish.setVisibility(View.GONE);
        }

        if (!update) {
            this.mUpdate.setVisibility(View.GONE);
        } else {
            this.mFinish.setVisibility(View.GONE);
            this.mUpdate.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(book.getImg_url()) && !book.getImg_url().equals(ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL)) {
            Glide.with(itemView.getContext().getApplicationContext()).load(book.getImg_url()).placeholder(R.drawable.icon_book_cover_default).error((R.drawable.icon_book_cover_default)).diskCacheStrategy(DiskCacheStrategy.ALL).into(this.mImage);
        } else {
            Glide.with(itemView.getContext().getApplicationContext()).load(R.drawable.icon_book_cover_default).into(this.mImage);
        }

        if (isRemoveMode) {
            this.mCheck.setVisibility(View.VISIBLE);
            this.mFinish.setVisibility(View.GONE);
            this.mUpdate.setVisibility(View.GONE);
            if (removeMark) {
                this.mCheck.setBackgroundResource(R.mipmap.bookshelf_delete_checked);
            } else {
                this.mCheck.setBackgroundResource(R.mipmap.bookshelf_delete_unchecked);
            }
        } else {
            this.mCheck.setVisibility(View.GONE);
        }
    }
}