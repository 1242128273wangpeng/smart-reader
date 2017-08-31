package com.intelligent.reader.adapter.holder;

import android.content.res.Resources;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.intelligent.reader.R;
import com.intelligent.reader.adapter.BookShelfReAdapter;

import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.Tools;

/**
 * Created by Administrator on 2017/4/13 0013.
 */

public class ListFourHolder extends AbsRecyclerViewHolder<Book> {

    private final ImageView mCheck;
    private final ImageView mImage;
    private final TextView mName, mUpdateTime, mUnread, mLastChapter, mAuthor;
    private final View mUpdate;

    public ListFourHolder(View itemView,
                          BookShelfReAdapter.ShelfItemClickListener shelfItemClickListener,
                          BookShelfReAdapter.ShelfItemLongClickListener shelfItemLongClickListener) {
        super(itemView, shelfItemClickListener, shelfItemLongClickListener);

        mCheck = (ImageView) itemView.findViewById(R.id.check_delete);
        mImage = (ImageView) itemView.findViewById(R.id.book_shelf_image);

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

        int count = book.chapter_count - (book.sequence+1);

        if(count == 0){
            this.mUnread.setText("已读最新章节");
        }else if (count == book.chapter_count){
            this.mUnread.setText("未读");
        }else {
            this.mUnread.setText(count + "章未读");
        }

        // 是否连载
        if (((Book) book).status == 2) {
            this.mUpdateTime.setBackgroundResource(R.drawable.bookshelf_finish_bg_1);
            this.mUpdateTime.setTextColor(Color.rgb(62, 204, 150));
            this.mUpdateTime.setText("完结");
            this.mLastChapter.setText(book.last_chapter_name);
        } else {
            this.mUpdateTime.setBackgroundResource(R.drawable.bookshelf_update_bg_1);
            this.mUpdateTime.setTextColor(Color.rgb(224, 102, 78));
            this.mUpdateTime.setText(Tools.compareTime(AppUtils.formatter, book.last_updatetime_native) + "更新");
            this.mLastChapter.setText("连载至: " + book.last_chapter_name);
        }

        // 是否有更新
        if (!update) {
            this.mUpdate.setVisibility(View.GONE);
        } else {
            this.mUpdate.setVisibility(View.VISIBLE);
        }


        if (!TextUtils.isEmpty(book.img_url) && !book.img_url.equals(ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL)) {
            Glide.with(itemView.getContext().getApplicationContext()).load(book.img_url).placeholder(R.drawable.icon_book_cover_default).error((R.drawable.icon_book_cover_default)).diskCacheStrategy(DiskCacheStrategy.ALL).into(this.mImage);
        } else {
            Glide.with(itemView.getContext().getApplicationContext()).load(R.drawable.icon_book_cover_default).into(this.mImage);
        }


        if (isRemoveMode) {
            this.mCheck.setVisibility(View.VISIBLE);
            this.mUpdate.setVisibility(View.GONE);
            TypedValue typeColor = new TypedValue();
            Resources.Theme theme = itemView.getContext().getTheme();
            if (removeMark) {
                theme.resolveAttribute(R.attr.bookshelf_delete_checked, typeColor, true);
            } else {
                theme.resolveAttribute(R.attr.bookshelf_delete_unchecked, typeColor, true);
            }
            this.mCheck.setBackgroundResource(typeColor.resourceId);
        } else {
            this.mCheck.setVisibility(View.GONE);
        }
    }
}
