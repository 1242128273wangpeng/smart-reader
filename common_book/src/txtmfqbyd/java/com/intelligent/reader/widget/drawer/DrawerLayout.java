package com.intelligent.reader.widget.drawer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import swipeback.ShadowView;

/**
 * Desc 抽屉菜单布局
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/2/24
 */

public class DrawerLayout extends FrameLayout {

    private static final String TAG = "SRDrawerLayout";
    private final int screenWidth;

    private View menuView;
    private DrawerMain mainView;
    private final ShadowView shadowView;

    private final ViewDragHelper viewDragHelper;

    private static final int MENU_CLOSED = 1;
    private static final int MENU_OPENED = 2;
    private int menuState = MENU_CLOSED;

    private int dragOrientation;
    private static final int LEFT_TO_RIGHT = 3;
    private static final int RIGHT_TO_LEFT = 4;

    private static final float SPRING_BACK_VELOCITY = 1500;
    private static final int SPRING_BACK_DISTANCE = 80;
    private final int springBackDistance;

    private static final int MENU_MARGIN_RIGHT = 64;
    private final int menuWidth;

    private static final int SHADOW_WIDTH = 15;
    private final int shadowWidth;

    private static final float TOUCH_SLOP_SENSITIVITY = 1.f;

    private int mainLeft;

    public DrawerLayout(Context context) {
        this(context, null);
    }

    public DrawerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final float density = getResources().getDisplayMetrics().density;//屏幕密度

        screenWidth = getResources().getDisplayMetrics().widthPixels;

        springBackDistance = (int) (SPRING_BACK_DISTANCE * density + 0.5f);

        mainLeft = 0;

        menuWidth = screenWidth - (int) (MENU_MARGIN_RIGHT * density + 0.5f);

        shadowView = new ShadowView(context);
        shadowWidth = (int) (SHADOW_WIDTH * density + 0.5f);

        viewDragHelper = ViewDragHelper.create(this, TOUCH_SLOP_SENSITIVITY, new CoordinatorCallback());
        viewDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);
    }

    private class CoordinatorCallback extends ViewDragHelper.Callback {

        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            viewDragHelper.captureChildView(mainView, pointerId);
        }

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            if (menuState == MENU_OPENED) {
                return menuView == child || mainView == child;
            } else {
                return menuView == child;
            }
        }

        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            if (capturedChild == menuView) {
                viewDragHelper.captureChildView(mainView, activePointerId);
            }
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return 1;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (left < 0) {
                left = 0;
            } else if (left > menuWidth) {
                left = menuWidth;
            }
            return left;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
//            Log.e(TAG, "onViewReleased: xvel: " + xvel);
            if (dragOrientation == LEFT_TO_RIGHT) {
                if (xvel > SPRING_BACK_VELOCITY || mainView.getLeft() > springBackDistance) {
                    openMenu();
                } else {
                    closeMenu();
                }
            } else if (dragOrientation == RIGHT_TO_LEFT) {
                if (xvel < -SPRING_BACK_VELOCITY || mainView.getLeft() < menuWidth - springBackDistance) {
                    closeMenu();
                } else {
                    openMenu();
                }
            }

        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
//            Log.d(TAG, "onViewPositionChanged: left:" + left);
            mainLeft = left;
            if (dx > 0) {
                dragOrientation = LEFT_TO_RIGHT;
            } else if (dx < 0) {
                dragOrientation = RIGHT_TO_LEFT;
            }
        }
    }

    //加载完布局文件后调用
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        menuView = getChildAt(0);

        mainView = (DrawerMain) getChildAt(1);
        mainView.setParent(this);

        addView(shadowView);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return viewDragHelper.shouldInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //将触摸事件传递给ViewDragHelper，此操作必不可少
        viewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        MarginLayoutParams menuParams = (MarginLayoutParams) menuView.getLayoutParams();
        menuParams.width = menuWidth;
        menuView.setLayoutParams(menuParams);

        mainView.layout(mainLeft, 0, mainLeft + screenWidth, bottom);

        MarginLayoutParams shadowParams = (MarginLayoutParams) shadowView.getLayoutParams();
        shadowParams.width = shadowWidth;
        shadowView.setLayoutParams(shadowParams);
        int shadowLeft = mainLeft - shadowWidth;
        shadowView.layout(shadowLeft, 0, mainLeft, bottom);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        final int restoreCount = canvas.save();//保存画布当前的剪裁信息

        int clipLeft = 0;
        int clipRight = mainView.getLeft();
        if (child == menuView && clipRight == 0) {//在 menu 关闭后剪裁
            canvas.clipRect(clipLeft, 0, clipRight, getHeight());//剪裁显示的区域
        }

        boolean result = super.drawChild(canvas, child, drawingTime);//绘制当前view

        //恢复画布之前保存的剪裁信息
        //以正常绘制之后的view
        canvas.restoreToCount(restoreCount);

        shadowView.setX(mainLeft - shadowWidth);

        return result;
    }

    @Override
    public void computeScroll() {
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
        if (mainView.getLeft() == 0) {
            menuState = MENU_CLOSED;
        } else if (mainView.getLeft() == menuWidth) {
            menuState = MENU_OPENED;
        }
    }

    public void openMenu() {
        viewDragHelper.smoothSlideViewTo(mainView, menuWidth, 0);
        ViewCompat.postInvalidateOnAnimation(DrawerLayout.this);
    }

    public void closeMenu() {
        viewDragHelper.smoothSlideViewTo(mainView, 0, 0);
        ViewCompat.postInvalidateOnAnimation(DrawerLayout.this);
    }

    public boolean isOpened() {
        return menuState == MENU_OPENED;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        final SavedState ss = new SavedState(superState);
        ss.menuState = menuState;
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        final SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        if (ss.menuState == MENU_OPENED) {
            openMenu();
        }
    }

    protected static class SavedState extends AbsSavedState {
        int menuState;

        SavedState(Parcel in, ClassLoader loader) {
            super(in, loader);
            menuState = in.readInt();
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(menuState);
        }

        public static final Creator<SavedState> CREATOR = ParcelableCompat.newCreator(
                new ParcelableCompatCreatorCallbacks<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                        return new SavedState(in, loader);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                });
    }

    public int transToHsvColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] = hsv[1] + 0.1f;
        hsv[2] = hsv[2] - 0.1f;
        return Color.HSVToColor(hsv);
    }
}