package net.lzbook.kit.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NonSwipeViewPager extends ViewPager {

    private boolean mScrollable = true;

    public NonSwipeViewPager(Context context) {
        super(context);
    }

    public NonSwipeViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        //return false;// 可行,不消费,传给父控件
        //return true;// 可行,消费,拦截事件
        if (mScrollable) {
            return super.onTouchEvent(ev);
        } else {
            return true;// 可行,消费,拦截事件
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // return false;//可行,不拦截事件,
        // return true;//不行,孩子无法处理事件
        //return super.onInterceptTouchEvent(ev);//不行,会有细微移动
        try {
            if (mScrollable) {
                return super.onInterceptTouchEvent(ev);
            } else {
                return false;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //不拦截,否则子孩子都无法收到事件,一般这个自定义的时候都不作处理
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }

    public boolean isScrollable() {
        return mScrollable;
    }

    public void setScrollable(boolean scrollble) {
        this.mScrollable = scrollble;
    }

}
