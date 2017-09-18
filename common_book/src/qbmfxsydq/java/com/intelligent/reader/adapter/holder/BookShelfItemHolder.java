package com.intelligent.reader.adapter.holder;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.intelligent.reader.R;
import com.intelligent.reader.adapter.BookShelfReAdapter;

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
    private final TextView mName, mUpdateTime, mUnread, mAuthor, mLastChapter;
    private final View mFinish, mUpdate;

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
        mAuthor = (TextView) itemView.findViewById(R.id.book_shelf_author);

    }

    @Override
    public void onBindData(int position, Book book, boolean update, boolean isRemoveMode, boolean
     removeMark) {
        if (!TextUtils.isEmpty(book.name))
            this.mName.setText(book.name);

        if(!TextUtils.isEmpty(book.author)){
            mAuthor.setText(book.author);
        }

        if (book.sequence + 1 > book.chapter_count) {
            book.sequence = book.chapter_count - 1;
        }

        if(book.sequence >= 0){
            this.mUnread.setText(book.sequence + 1 + "/" + book.chapter_count + "章");
        }else{
            this.mUnread.setText("未读");
        }


        // 是否连载
        if (((Book) book).status == 2) {
            this.mFinish.setVisibility(View.VISIBLE);
        } else {
            this.mFinish.setVisibility(View.GONE);
        }
        // 是否有更新
        if (!update) {
            this.mUpdate.setVisibility(View.GONE);
        } else {
            this.mUpdate.setVisibility(View.VISIBLE);
            this.mFinish.setVisibility(View.GONE);
        }
        if (this.mUpdateTime != null) {
            this.mUpdateTime.setText(Tools.compareTime(AppUtils.formatter, book
                    .last_updatetime_native) + "更新: ");
        }

        if (!TextUtils.isEmpty(book.img_url) && !book.img_url.equals(ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL)) {
            Glide.with(itemView.getContext().getApplicationContext()).load(book.img_url).placeholder(R.drawable.icon_book_cover_default).error((R.drawable.icon_book_cover_default)).diskCacheStrategy(DiskCacheStrategy.ALL).into(this.mImage);
        } else {
            Glide.with(itemView.getContext().getApplicationContext()).load(R.drawable.icon_book_cover_default).into(this.mImage);
        }

        this.mLastChapter.setText(book.last_chapter_name);


        if (isRemoveMode) {
            this.mCheck.setVisibility(View.VISIBLE);
            this.mFinish.setVisibility(View.GONE);
            this.mUpdate.setVisibility(View.GONE);
            if (removeMark) {
                this.mCheck.setBackgroundResource(R.mipmap.readsetting_check);
            } else {
                this.mCheck.setBackgroundResource(R.mipmap.readsetting_uncheck);
            }
        } else {
            this.mCheck.setVisibility(View.GONE);
        }
    }
}