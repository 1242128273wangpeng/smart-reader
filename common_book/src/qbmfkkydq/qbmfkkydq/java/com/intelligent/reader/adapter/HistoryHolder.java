package com.intelligent.reader.adapter;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ding.basic.bean.HistoryInfo;
import com.intelligent.reader.R;

import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.utils.AbsRecyclerViewHolder;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.Tools;

/**
 * Created by yuchao on 2017/4/13 0013.
 */

public class HistoryHolder extends AbsRecyclerViewHolder<HistoryInfo> {

    private final ImageView mImage;
    private final TextView mName, mBrowTime, mAuthor;

    public HistoryHolder(View itemView,
                         AbsRecyclerViewHolder.ShelfItemClickListener shelfItemClickListener,
                         AbsRecyclerViewHolder.ShelfItemLongClickListener shelfItemLongClickListener) {
        super(itemView, shelfItemClickListener, shelfItemLongClickListener);

        mImage = (ImageView) itemView.findViewById(R.id.book_shelf_image);

        mName = (TextView) itemView.findViewById(R.id.book_shelf_name);
        mBrowTime = (TextView) itemView.findViewById(R.id.book_shelf_update_time);
        mAuthor = (TextView) itemView.findViewById(R.id.book_shelf_author);

    }

    @Override
    public void onBindData(int position, HistoryInfo book, boolean update, boolean isRemoveMode, boolean
            removeMark) {
        if (!TextUtils.isEmpty(book.getName()))
            this.mName.setText(book.getName());

        if (!TextUtils.isEmpty(book.getAuthor())) {
            mAuthor.setText("作者: " + book.getAuthor());
        }

        if (this.mBrowTime != null) {
            this.mBrowTime.setText(Tools.logTime(AppUtils.min_formatter, book.getBrowse_time()));
        }

        if (!TextUtils.isEmpty(book.getImg_url()) && !book.getImg_url().equals(ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL)) {
            Glide.with(itemView.getContext().getApplicationContext()).load(book.getImg_url()).placeholder(R.drawable.icon_book_cover_default).error((R.drawable.icon_book_cover_default)).diskCacheStrategy(DiskCacheStrategy.ALL).into(this.mImage);
        } else {
            Glide.with(itemView.getContext().getApplicationContext()).load(R.drawable.icon_book_cover_default).into(this.mImage);
        }

    }
}
