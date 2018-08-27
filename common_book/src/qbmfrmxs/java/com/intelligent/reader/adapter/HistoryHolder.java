package com.intelligent.reader.adapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import net.lzbook.kit.utils.AbsRecyclerViewHolder;

import com.ding.basic.bean.HistoryInfo;
import com.intelligent.reader.R;

import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.Tools;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * HistoryHolder
 * Created by yuchao on 2017/4/13 0013.
 */
public class HistoryHolder extends AbsRecyclerViewHolder<HistoryInfo> {

    private final ImageView mImage;
    private final TextView mName, mBrowTime, mAuthor, mDesc;

    public HistoryHolder(View itemView,
            ShelfItemClickListener shelfItemClickListener,
            ShelfItemLongClickListener shelfItemLongClickListener) {
        super(itemView, shelfItemClickListener, shelfItemLongClickListener);

        mImage = itemView.findViewById(R.id.book_shelf_image);

        mName = itemView.findViewById(R.id.book_shelf_name);
        mBrowTime = itemView.findViewById(R.id.book_shelf_update_time);
        mAuthor = itemView.findViewById(R.id.book_shelf_author);
        mDesc = itemView.findViewById(R.id.footprint_item_desc);
    }

    @Override
    public void onBindData(int position, HistoryInfo book, boolean update, boolean isRemoveMode,
            boolean
                    removeMark) {
        if (!TextUtils.isEmpty(book.getName())) {
            this.mName.setText(book.getName());
        }

        if (!TextUtils.isEmpty(book.getAuthor())) {
            mAuthor.setText(("作者: " + book.getAuthor()));
        }

        if (!TextUtils.isEmpty(book.getDesc())) {
            mDesc.setText(book.getDesc());
        }

        if (this.mBrowTime != null) {
            this.mBrowTime.setText(Tools.logTime(AppUtils.min_formatter, book.getBrowse_time()));
        }

        if (!TextUtils.isEmpty(book.getImg_url()) && !book.getImg_url().equals(
                ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL)) {
            Glide.with(itemView.getContext().getApplicationContext()).load(
                    book.getImg_url()).placeholder(R.drawable.common_book_cover_default_icon).error(
                    (R.drawable.common_book_cover_default_icon)).diskCacheStrategy(
                    DiskCacheStrategy.ALL).into(this.mImage);
        } else {
            Glide.with(itemView.getContext().getApplicationContext()).load(
                    R.drawable.common_book_cover_default_icon).into(this.mImage);
        }

    }
}
