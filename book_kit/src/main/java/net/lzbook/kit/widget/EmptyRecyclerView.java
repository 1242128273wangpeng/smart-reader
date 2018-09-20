package net.lzbook.kit.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by yuchao on 2017/6/23 0023.
 */

public class EmptyRecyclerView extends RecyclerView {

    private View mEmptyView;
    private OnItemChangeListener mDataChangeListener;

    final private AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            checkIfDataChange();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            checkIfDataChange();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            checkIfDataChange();
        }
    };

    public EmptyRecyclerView(Context context) {
        super(context);
    }

    public EmptyRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmptyRecyclerView(Context context, AttributeSet attrs,
                             int defStyle) {
        super(context, attrs, defStyle);
    }

    private void checkIfDataChange() {
        Adapter adapter = getAdapter();
        if (mEmptyView != null && adapter != null) {
            final boolean emptyViewVisible =
                    adapter.getItemCount() == 0;
            mEmptyView.setVisibility(emptyViewVisible ? VISIBLE : GONE);
            setVisibility(emptyViewVisible ? GONE : VISIBLE);

            if (mDataChangeListener != null) {
                mDataChangeListener.onItemChange(adapter.getItemCount());
            }
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        final Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(observer);
        }
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
        }

        checkIfDataChange();
    }

    public void setEmptyView(View emptyView) {
        this.mEmptyView = emptyView;
        checkIfDataChange();
    }

    public void setOnItemChangeListener(OnItemChangeListener l) {
        this.mDataChangeListener = l;
    }

    public interface OnItemChangeListener {
        void onItemChange(int itemCount);
    }
}
