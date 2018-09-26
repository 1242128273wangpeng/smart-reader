package net.lzbook.kit.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ding.basic.bean.HistoryInfo;

import net.lzbook.kit.ui.adapter.base.BaseAdapter;
import net.lzbook.kit.utils.AbsRecyclerViewHolder;
import net.lzbook.kit.utils.AppUtils;

import java.lang.reflect.Constructor;

/**
 * Created by yuchao on 2017/6/17 0017.
 */
public class HisAdapter extends BaseAdapter<HistoryInfo> {
    private Context mContext;
    private AbsRecyclerViewHolder.ShelfItemClickListener shelfItemClickListener;
    private AbsRecyclerViewHolder.ShelfItemLongClickListener shelfItemLongClickListener;

    public HisAdapter(Context context, AbsRecyclerViewHolder.ShelfItemClickListener itemClick, AbsRecyclerViewHolder.ShelfItemLongClickListener itemLongClick) {
        mContext = context;
        shelfItemClickListener = itemClick;
        shelfItemLongClickListener = itemLongClick;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId= AppUtils.getLayoutId(mContext,"layout_history_item_list");
        View view = LayoutInflater.from(mContext).inflate(layoutId,
                parent, false);
        try {
            Class historyHolder = Class.forName("com.intelligent.reader.adapter.HistoryHolder");
            Constructor<RecyclerView.ViewHolder> con = historyHolder.getConstructor(View.class, AbsRecyclerViewHolder.ShelfItemClickListener.class,AbsRecyclerViewHolder.ShelfItemLongClickListener.class);
            return con.newInstance(view, shelfItemClickListener, shelfItemLongClickListener);
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        HistoryInfo info = mDataSet.get(position);
        ((AbsRecyclerViewHolder<HistoryInfo>) holder).onBindData(position, info,
                false, false, false);
    }
}