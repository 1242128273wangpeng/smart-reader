package com.intelligent.reader.adapter.paging;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuchao on 2017/6/17 0017.
 */
public abstract class BaseAdapter<T> extends RecyclerView.Adapter {
    protected List<T> mDataSet = new ArrayList<>();

    public void updateData(List dataSet) {
        this.mDataSet.clear();
        appendData(dataSet);
    }

    public void appendData(List dataSet) {
        if (dataSet != null && !dataSet.isEmpty()) {
            this.mDataSet.addAll(dataSet);
            notifyDataSetChanged();
        }
    }

    public List<T> getDataSet() {
        return mDataSet;
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}
