package com.intelligent.reader.read.page;

import com.intelligent.reader.util.DisplayUtils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * Desc 绘制间距
 * Author lijun Lee
 * Mail jun_li@dingyuegroup.cn
 * Data 2017/9/21 11:49
 */

public class SpacingTextView extends View {

    private float spacing;

    private String text;

    private int screenWidth;

    private float textSize;

    private int textColor;

    private TextPaint mTextPaint;

    public SpacingTextView(Context context) {
        super(context);
        init();
    }

    public SpacingTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        DisplayMetrics dis = getResources().getDisplayMetrics();
        screenWidth = dis.widthPixels;

        mTextPaint = new TextPaint();
        mTextPaint.setTextSize(12 * dis.scaledDensity);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setDither(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        invalidate();
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
        mTextPaint.setTextSize(DisplayUtils.px2dp(getResources(), this.textSize));
        invalidate();
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        mTextPaint.setColor(this.textColor);
        invalidate();
    }

    public void setTextView(float spacing, String text) {
        this.spacing = spacing;
        this.text = text;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawSpacingText(canvas, text, spacing);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void drawSpacingText(Canvas canvas, String text, float spacing) {
        if (TextUtils.isEmpty(text)) return;
        float textWidth = mTextPaint.measureText(String.valueOf(text.charAt(0)));
        float textTotalWidth = textWidth * text.length() + (textWidth * spacing) * (text.length() - 1);
        float drawTextStart = (screenWidth - textTotalWidth) / 2;
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        float drawTextX = 0;
        for (int i = 0; i < text.length(); i++) {
            drawTextX += (i == 0 ? drawTextStart : textWidth + (textWidth * spacing));
            canvas.drawText(String.valueOf(text.charAt(i)), drawTextX, -fm.ascent, mTextPaint);
        }
    }
}
