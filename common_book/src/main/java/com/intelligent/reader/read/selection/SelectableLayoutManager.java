package com.intelligent.reader.read.selection;

import com.intelligent.reader.read.util.SelectionController;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author lijun Lee
 * @desc SelectableLayoutManager
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/1/5 14:21
 */

public class SelectableLayoutManager extends LinearLayoutManager {

    private SelectionController mController;

    public SelectableLayoutManager(Context context) {
        super(context);
    }

    public SelectableLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public SelectableLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setSelectionController(SelectionController selectionController) {
        this.mController = selectionController;
    }

    @Override
    public void offsetChildrenVertical(int dy) {
        super.offsetChildrenVertical(dy);
    }

    @Override
    public void addDisappearingView(View child) {
        super.addDisappearingView(child);
        if (mController != null) mController.addViewToSelectable(child);
    }

    @Override
    public void addView(View child, int index) {
        super.addView(child, index);
        if (mController != null) mController.addViewToSelectable(child);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
    }
}
