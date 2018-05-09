package com.dingyue.bookshelf;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.data.bean.Book;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Desc 书架页Item
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/5 0002 14:19
 */
public class BookShelfItemHolder extends AbsRecyclerViewHolder<Book> {

    private final ImageView imgCheckDelete;
    private final ImageView imgBookCover;
    private final TextView txtBookName;
    private final ImageView imgStateFinish;
    private final ImageView imgStateUpdate;

    public BookShelfItemHolder(View itemView, BookShelfReAdapter.ShelfItemClickListener shelfItemClickListener,
                               BookShelfReAdapter.ShelfItemLongClickListener shelfItemLongClickListener) {
        super(itemView, shelfItemClickListener, shelfItemLongClickListener);

        txtBookName = (TextView) itemView.findViewById(R.id.txt_book_name);
        imgStateFinish = (ImageView) itemView.findViewById(R.id.img_book_state_finish);
        imgStateUpdate = (ImageView) itemView.findViewById(R.id.img_book_state_update);
        imgCheckDelete = (ImageView) itemView.findViewById(R.id.img_check_delete);
        imgBookCover = (ImageView) itemView.findViewById(R.id.img_book_cover);

    }

    @Override
    public void onBindData(int position, Book book, boolean update, boolean isRemoveMode, boolean
            removeMark) {
        if (!TextUtils.isEmpty(book.name))
            this.txtBookName.setText(book.name);


        if (book.sequence + 1 > book.chapter_count) {
            book.sequence = book.chapter_count - 1;
        }

        // 是否连载
        if (book.status == 2) {
            this.imgStateFinish.setVisibility(View.VISIBLE);
        } else {
            this.imgStateFinish.setVisibility(View.GONE);
        }
        // 是否有更新
        if (!update) {
            this.imgStateUpdate.setVisibility(View.GONE);
        } else {
            this.imgStateUpdate.setVisibility(View.VISIBLE);
            this.imgStateFinish.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(book.img_url) && !book.img_url.equals(ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL)) {
            Glide.with(itemView.getContext().getApplicationContext())
                    .load(book.img_url)
                    .placeholder(R.drawable.bookshelf_book_cover_default)
                    .error((R.drawable.bookshelf_book_cover_default))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(this.imgBookCover);
        } else {
            Glide.with(itemView.getContext().getApplicationContext())
                    .load(R.drawable.bookshelf_book_cover_default)
                    .into(this.imgBookCover);
        }

//        this.mLastChapter.setText(book.last_chapter_name);


        if (isRemoveMode) {
            this.imgCheckDelete.setVisibility(View.VISIBLE);
//            this.imgStateFinish.setVisibility(View.GONE);
//            this.imgStateUpdate.setVisibility(View.GONE);
            if (removeMark) {
                this.imgCheckDelete.setBackgroundResource(R.drawable.bookshelf_edit_selected_icon);
            } else {
                this.imgCheckDelete.setBackgroundResource(R.drawable.bookshelf_edit_unselected_icon);
            }
        } else {
            this.imgCheckDelete.setVisibility(View.GONE);
        }
    }
}
