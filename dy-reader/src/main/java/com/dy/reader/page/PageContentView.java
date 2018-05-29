package com.dy.reader.page;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.dy.reader.helper.DrawTextHelper;
import com.intelligent.reader.read.mode.NovelPageBean;

import net.lzbook.kit.utils.AppLog;


/**
 * @author lijun Lee
 * @desc 章节内容展示
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/10/27 14:56
 */

public class PageContentView extends View {


    private NovelPageBean mPageBean;

    private int mTextContentHeight;

    public PageContentView(Context context) {
        super(context);
    }

    public PageContentView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PageContentView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setContent(NovelPageBean pageLines) {
        mPageBean = pageLines;
        invalidate();
        mTextContentHeight = (int) mPageBean.getHeight();
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        if (mTextContentHeight > 0 && mTextContentHeight != measuredHeight) {
            measuredHeight = mTextContentHeight;
        }
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        DrawTextHelper.INSTANCE.drawVerticalText(canvas, mPageBean);
    }

}
