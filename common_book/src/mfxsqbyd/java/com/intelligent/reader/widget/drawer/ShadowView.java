package com.intelligent.reader.widget.drawer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

/**
 * Desc 滑动返回侧边阴影
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2017/11/20
 */

public class ShadowView extends View {

    private Drawable mDrawable;

    public ShadowView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawable == null) {
            int colors[] = {0x00B8C5D9, 0x12B8C5D9, 0x30B8C5D9};//分别为开始颜色，中间颜色，结束颜色
            mDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        }
        mDrawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
        mDrawable.draw(canvas);
    }
}
