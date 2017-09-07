package com.intelligent.reader.adapter.paging;

import com.intelligent.reader.R;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by yuchao on 2017/6/17 0017.
 */
public class LoadMoreAdapterWrapper extends BaseAdapter<String> {
    public static final int PAGE_SIZE = 20;
    private BaseAdapter mAdapter;
    private boolean hasMoreData = true;
    private OnLoad mOnLoad;

    public LoadMoreAdapterWrapper(BaseAdapter adapter, OnLoad onLoad) {
        mAdapter = adapter;
        mOnLoad = onLoad;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == R.layout.list_item_no_more) {
            View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
            return new NoMoreItemVH(view);
        } else if (viewType == R.layout.list_item_loading) {
            View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
            return new LoadingItemVH(view);
        } else {
            return mAdapter.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LoadingItemVH) {
            requestData(getDataCount(), PAGE_SIZE);
        } else if (holder instanceof NoMoreItemVH) {

        } else {
            mAdapter.onBindViewHolder(holder, position);
        }
    }

    private void requestData(int pagePosition, int pageSize) {

        //网络请求,如果是异步请求，则在成功之后的回调中添加数据，并且调用notifyDataSetChanged方法，hasMoreData为true
        //如果没有数据了，则hasMoreData为false，然后通知变化，更新recylerview

        if (mOnLoad != null) {
            mOnLoad.load(pagePosition, pageSize, new ILoadCallback() {
                @Override
                public void onSuccess() {
                    hasMoreData = true;
                    notifyDataSetChanged();
                }

                @Override
                public void onFailure() {
                    hasMoreData = false;
                    notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position > 8 && position == getItemCount() - 1) {
            if (hasMoreData && position >= PAGE_SIZE) {
                return R.layout.list_item_loading;
            } else {
                return R.layout.list_item_no_more;
            }
        } else {
            return mAdapter.getItemViewType(position);
        }
    }

    private int getDataCount() {
        if (mAdapter != null && mAdapter.mDataSet != null) {
            return mAdapter.mDataSet.size();
        }
        return 0;
    }

    @Override
    public int getItemCount() {
        if (mAdapter.mDataSet.size() < 10) {
            return mAdapter.getItemCount();
        } else {
            return mAdapter.getItemCount() + 1;
        }
    }

    public interface OnLoad {
        void load(int pagePosition, int pageSize, ILoadCallback callback);
    }

    public interface ILoadCallback {
        void onSuccess();

        void onFailure();
    }

    static class LoadingItemVH extends RecyclerView.ViewHolder {

        public LoadingItemVH(View itemView) {
            super(itemView);
        }

    }

    static class NoMoreItemVH extends RecyclerView.ViewHolder {

        public NoMoreItemVH(View itemView) {
            super(itemView);
        }
    }

}

