package com.intelligent.reader.read.page;

import com.intelligent.reader.read.help.DrawTextHelper;
import com.intelligent.reader.read.mode.NovelPageBean;

import net.lzbook.kit.utils.AppLog;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;


/**
 * @author lijun Lee
 * @desc 章节内容展示
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/10/27 14:56
 */

public class PageContentView extends View {


    private NovelPageBean mPageBean;

    private int mTextContentHeight;

    private DrawTextHelper mDrawTextHelper;

    public static final String CHAPTER_HOME_PAGE = "chapter_homepage";

    public PageContentView(Context context) {
        super(context);
        init();
    }

    public PageContentView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PageContentView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        mDrawTextHelper = new DrawTextHelper(getContext());
    }

    public void setContent(NovelPageBean pageLines) {
        mPageBean = pageLines;
        invalidate();
        mTextContentHeight = (int) mPageBean.getHeight();
        AppLog.d("PageContentView", "setContent mTextContentHeight: " + mTextContentHeight);
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
        AppLog.d("PageContentView", "onMeasure measuredHeight: " + measuredHeight);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mDrawTextHelper.drawVerticalText(canvas, mPageBean);
        AppLog.d("PageContentView", "onDraw mTextContentHeight: " + mTextContentHeight);
    }

}
