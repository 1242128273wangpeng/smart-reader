package com.intelligent.reader.widget.drawer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Desc 抽屉菜单主界面
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/2/24
 */

public class DrawerMain extends RelativeLayout {
    private DrawerLayout drawerLayout;

    public DrawerMain(Context context) {
        this(context, null, 0);
    }

    public DrawerMain(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawerMain(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    public void setParent(DrawerLayout DrawerLayout) {
        drawerLayout = DrawerLayout;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        //拦截事件，不往下传递
        return drawerLayout.isOpened() || super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (drawerLayout.isOpened()) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                drawerLayout.closeMenu();
            }
            return true;
        }
        return super.onTouchEvent(event);
    }
}
