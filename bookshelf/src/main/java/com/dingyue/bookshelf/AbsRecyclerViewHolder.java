package com.dingyue.bookshelf;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Danny.Z on 2017/4/13 0013.
 */

public abstract class AbsRecyclerViewHolder<T> extends RecyclerView.ViewHolder implements
        View.OnClickListener, View.OnLongClickListener {

    protected BookShelfReAdapter.ShelfItemClickListener mShelfItemClickListener;
    protected BookShelfReAdapter.ShelfItemLongClickListener mShelfItemLongClickListener;

    public AbsRecyclerViewHolder(View itemView,
                                 BookShelfReAdapter.ShelfItemClickListener shelfItemClickListener,
                                 BookShelfReAdapter.ShelfItemLongClickListener shelfItemLongClickListener) {
        super(itemView);
        mShelfItemClickListener = shelfItemClickListener;
        mShelfItemLongClickListener = shelfItemLongClickListener;
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (mShelfItemClickListener != null) {
            mShelfItemClickListener.onItemClick(v, getPosition());
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mShelfItemLongClickListener != null) {
            mShelfItemLongClickListener.onItemLongClick(v, getPosition());
        }
        return true;
    }

    public abstract void onBindData(int position, T data, boolean update, boolean isRemoveMode, boolean
            removeMark);
}
