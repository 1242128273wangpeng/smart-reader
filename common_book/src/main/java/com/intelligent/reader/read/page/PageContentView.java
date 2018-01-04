package com.intelligent.reader.read.page;

import com.intelligent.reader.read.help.DrawTextHelper;
import com.intelligent.reader.read.mode.NovelPageBean;

import net.lzbook.kit.constants.ReadConstants;

import net.lzbook.kit.data.bean.NovelLineBean;
import net.lzbook.kit.data.bean.ReadStatus;
import net.lzbook.kit.data.bean.SensitiveWords;
import net.lzbook.kit.utils.AppLog;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lijun Lee
 * @desc 章节内容展示
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/10/27 14:56
 */

public class PageContentView extends View {

    private Paint mPaint;

    private Paint duanPaint;

    private float mWidth;

    private float mLineStart;

    private ReadStatus readStatus;

    private NovelPageBean mPageLines;

    private SensitiveWords readSensitiveWord;

    private List<String> readSensitiveWords;

    private boolean noReadSensitive = false;

    private int mTextColor;

    private int mTextContentHeight;

    private DrawTextHelper mDrawTextHelper;

    public static final String CHAPTER_HOME_PAGE = "chapter_homepage";
    public static final String BOOK_HOME_PAGE = "txtzsydsq_homepage";

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

        mDrawTextHelper = new DrawTextHelper(getResources());

        mTextColor = Color.BLACK;
        mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        mPaint.setColor(mTextColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);

        duanPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        duanPaint.setStyle(Paint.Style.FILL);
        duanPaint.setAntiAlias(true);
        duanPaint.setDither(true);

        this.readSensitiveWord = SensitiveWords.getReadSensitiveWords();
        if (readSensitiveWord != null && readSensitiveWord.list.size() > 0) {
            readSensitiveWords = readSensitiveWord.getList();
            noReadSensitive = false;
        } else {
            noReadSensitive = true;
        }

    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
        mPaint.setColor(mTextColor);
    }

    public void setReaderStatus(ReadStatus readStatus) {
        this.readStatus = readStatus;
    }

    public void setContent(NovelPageBean pageLines) {
        mPageLines = pageLines;
        invalidate();
        mTextContentHeight = (int)mPageLines.getHeight();
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

        mDrawTextHelper.drawVerticalText(canvas, mPageLines);
        AppLog.d("PageContentView", "onDraw mTextContentHeight: " + mTextContentHeight);
    }

}
