package com.intelligent.reader.read.util;

import com.intelligent.reader.read.selection.Selectable;
import com.intelligent.reader.read.selection.SelectableInfo;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * @author lijun Lee
 * @desc 划词管理类
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/1/11 17:49
 */

public class SelectionController {

    private static final String TAG = "SelectionController";

    private ViewGroup mSelectableViewGroup;

    private GestureDetector mGestureDetector;

    private ArrayList<SelectableInfo> selectableInfos = new ArrayList<>();

    public SelectionController(ViewGroup selectableViewGroup) {
        this.mSelectableViewGroup = selectableViewGroup;
        initGesture();
    }

    private void initGesture() {
        mGestureDetector = new GestureDetector(mSelectableViewGroup.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                Log.d("SelectionController", "onLongPress");
//                if (!selectInProcess && isEnabled()) {
                startSelection(e);
//                }
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Log.d("SelectionController", "onSingleTapUp");
                return super.onSingleTapUp(e);
            }
        });
    }

    private void startSelection(MotionEvent e) {
//        selectInProcess = setFirstCursorsPosition(e);
//        if (selectInProcess && selectionCallback != null) {
//            selectionCallback.startSelection();
//        }
//        if (selectInProcess && actionModeCallback != null)
//            actionMode = mSelectableViewGroup.startActionMode(actionModeCallback);

        int[] location = new int[2];
        String text = "";
        mSelectableViewGroup.getLocationOnScreen(location);
        Log.d(TAG, "View Touch  x :" + e.getX() + " y: " + e.getY());
        Log.d(TAG, "View Location  x :" + location[0] + " y: " + location[1]);
        for (SelectableInfo selectableInfo : selectableInfos) {
            final Selectable selectable = selectableInfo.getSelectable();
            if (selectable != null && selectable.getVisibility() == View.VISIBLE) {
                int[] locationSelectable = new int[2];
                selectable.getLocationOnScreen(locationSelectable);
                int left = locationSelectable[0];
                int top = locationSelectable[1];
                int evX = (int) (e.getX() + location[0]);
                int evY = (int) (e.getY() + location[1]);
                if (selectable.isInside(evX, evY)) {
//                    selectable.setSelectText(evX, evY);
                    break;
                }
            }
        }
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (mGestureDetector != null)
            mGestureDetector.onTouchEvent(ev);
        boolean dispatched = false;
//        if (selectInProcess) {
//            boolean right = rightHandleListener.onTouchHandle(ev);
//            boolean left = leftHandleListener.onTouchHandle(ev);
//            dispatched = right || left;
//        }
        return dispatched;
    }

    public void addViewToSelectable(View view) {
//        checkSelectableList();
        if (view instanceof Selectable) {
            addSelectableToSelectableInfos((Selectable) view);
        } else if (view instanceof ViewGroup) {
            findSelectableTextView((ViewGroup) view);
        }
    }

    private void addSelectableToSelectableInfos(Selectable selectable) {
        boolean found = false;
        for (SelectableInfo selectableInfo : selectableInfos) {
            if (selectableInfo.getKey().equals(selectable.getKey())) {
                selectableInfo.setSelectable(selectable);
                found = true;
                Log.d(">>>>>", "exist selectable = " + selectableInfo.getSelectable());
                break;
            }
        }
        if (!found) {
            final SelectableInfo selectableInfo = new SelectableInfo(selectable);
            selectableInfos.add(selectableInfo);
            Log.d(">>>>>", "new selectable = " + selectableInfo.getSelectable());
        }
    }

    public void findSelectableTextView(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof Selectable) {
                addSelectableToSelectableInfos((Selectable) view);
                continue;
            }
            if (view instanceof ViewGroup) {
                findSelectableTextView((ViewGroup) view);
            }
        }
    }
}
