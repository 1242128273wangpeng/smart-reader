package com.intelligent.reader.util;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * 捕捉IndexOutOfBoundsException,该异常为后台崩溃日志发现,偶现,需要google从底层代码修复,这里仅捕捉该异常.
 */
public class ShelfGridLayoutManager extends GridLayoutManager {
    public ShelfGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public ShelfGridLayoutManager(Context context, int spanCount, int orientation,
                                  boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    public ShelfGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
