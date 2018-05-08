package com.intelligent.reader.adapter.paging;

import com.intelligent.reader.R;
import com.dingyue.bookshelf.BookShelfReAdapter;
import com.dingyue.bookshelf.AbsRecyclerViewHolder;
import com.intelligent.reader.adapter.holder.HistoryHolder;

import net.lzbook.kit.data.ormlite.bean.HistoryInfo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by yuchao on 2017/6/17 0017.
 */
public class HisAdapter extends BaseAdapter<HistoryInfo> {
    private Context mContext;
    private BookShelfReAdapter.ShelfItemClickListener shelfItemClickListener;
    private BookShelfReAdapter.ShelfItemLongClickListener shelfItemLongClickListener;

    public HisAdapter(Context context, BookShelfReAdapter.ShelfItemClickListener itemClick, BookShelfReAdapter.ShelfItemLongClickListener itemLongClick) {
        mContext = context;
        shelfItemClickListener = itemClick;
        shelfItemLongClickListener = itemLongClick;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout
                        .layout_history_item_list,
                parent, false);
        return new HistoryHolder(view, shelfItemClickListener,
                shelfItemLongClickListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        HistoryInfo info = mDataSet.get(position);
        ((AbsRecyclerViewHolder<HistoryInfo>) holder).onBindData(position, info,
                false, false, false);
    }


}
