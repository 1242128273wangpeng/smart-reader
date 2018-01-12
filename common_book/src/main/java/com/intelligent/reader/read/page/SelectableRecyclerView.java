package com.intelligent.reader.read.page;

import com.intelligent.reader.read.util.SelectionController;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * @author lijun Lee
 * @desc 划词RecyclerView
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/1/11 17:48
 */

public class SelectableRecyclerView extends RecyclerView {

    private SelectionController mController;

    public SelectableRecyclerView(Context context) {
        super(context);
    }

    public SelectableRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SelectableRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mController = new SelectionController(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mController != null) {
            if (mController.onTouchEvent(ev)) {
                return true;
            }
        }

        return super.dispatchTouchEvent(ev);
    }
}
