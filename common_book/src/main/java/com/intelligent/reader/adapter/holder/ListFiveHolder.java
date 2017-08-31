package com.intelligent.reader.adapter.holder;

import android.content.res.Resources;
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

public class ListFiveHolder extends AbsRecyclerViewHolder<Book> {

    private final ImageView mCheck;
    private final ImageView mImage;
    private final TextView mName, mUpdateTime, mReadCount, mChapterCount, mAuthor, mLastChapter;
    private final View mFinish, mUpdate;

    public ListFiveHolder(View itemView,
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
        mReadCount = (TextView) itemView.findViewById(R.id.book_shelf_read_count);
        mChapterCount = (TextView) itemView.findViewById(R.id.book_shelf_chapter_count);
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

        if(book.sequence < 0){
            this.mReadCount.setText("未读");
            this.mChapterCount.setText("");
        }else if (book.sequence + 1 == book.chapter_count){
            this.mReadCount.setText("已读最新章节");
            this.mChapterCount.setText("");
        }else {
            this.mReadCount.setText(String.valueOf(book.sequence + 1));
            this.mChapterCount.setText("/" + book.chapter_count);
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
                    .last_updatetime_native) + "更新");
        }

        if (!TextUtils.isEmpty(book.img_url) && !book.img_url.equals(ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL)) {
            Glide.with(itemView.getContext().getApplicationContext()).load(book.img_url).placeholder(R.drawable.icon_book_cover_default).error((R.drawable.icon_book_cover_default)).diskCacheStrategy(DiskCacheStrategy.ALL).into(this.mImage);
        } else {
            Glide.with(itemView.getContext().getApplicationContext()).load(R.drawable.icon_book_cover_default).into(this.mImage);
        }

        this.mLastChapter.setText("最新章节: " + book.last_chapter_name);


        if (isRemoveMode) {
            this.mCheck.setVisibility(View.VISIBLE);
            this.mFinish.setVisibility(View.GONE);
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
