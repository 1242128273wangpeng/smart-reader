package com.intelligent.reader.read.page;

import com.intelligent.reader.read.help.ReadConstants;

import net.lzbook.kit.constants.Constants;
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
import android.text.TextUtils;
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

    private List<NovelLineBean> pageLines;

    private SensitiveWords readSensitiveWord;

    private List<String> readSensitiveWords;

    private boolean noReadSensitive = false;

    private int mTextColor;

    private int mTextContentHeight;

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

    public void setContent(List<NovelLineBean> pageLines) {
        this.pageLines = pageLines;
        mPaint.setTextSize(Constants.FONT_SIZE * readStatus.screenScaledDensity);
        duanPaint.setTextSize(1 * readStatus.screenScaledDensity);
        Paint.FontMetrics fm = mPaint.getFontMetrics();
        float lineSpace = Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE * readStatus.screenScaledDensity;
        float m_iFontHeight = fm.descent - fm.ascent + lineSpace;
        float m_duan = Constants.READ_PARAGRAPH_SPACE * lineSpace;

        float total_y = -fm.ascent;
        if (pageLines != null && !pageLines.isEmpty()) {
            for (int i = 0; i < pageLines.size(); i++) {
                if (!CHAPTER_HOME_PAGE.equals(pageLines.get(i).getLineContent().trim())) {
                    NovelLineBean text = pageLines.get(i);
                    if (Constants.isShielding && !noReadSensitive) {
                        for (String word : readSensitiveWords) {
                            text.setLineContent(text.getLineContent().replace(word, getChangeWord(word.length())));
                        }
                    }
                    if(!TextUtils.isEmpty(text.getLineContent())) {
                        if (text.getLineContent().equals(" ")) {
                            total_y += m_duan;
                        } else {
                            total_y += m_iFontHeight;
                            readStatus.currentPageConentLength += text.getLineContent().length();
                        }
                    }
                }
                mTextContentHeight = (int) (total_y + fm.ascent);
            }
            AppLog.d("onDraw", "mTextContentHeight : " + mTextContentHeight);
            requestLayout();
        }
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

        Paint.FontMetrics fm = mPaint.getFontMetrics();
        mWidth = readStatus.screenWidth - readStatus.screenDensity * Constants.READ_CONTENT_PAGE_LEFT_SPACE * 2;
        mLineStart = Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity;

        float lineSpace = Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE * readStatus.screenScaledDensity;
        float m_iFontHeight = fm.descent - fm.ascent + lineSpace;
        float m_duan = Constants.READ_PARAGRAPH_SPACE * lineSpace;

        float total_y = -fm.ascent;

        canvas.save();
        if (pageLines != null && !pageLines.isEmpty()) {
            for (int i = 0; i < pageLines.size(); i++) {
                if (!CHAPTER_HOME_PAGE.equals(pageLines.get(i).getLineContent().trim())) {
                    NovelLineBean text = pageLines.get(i);
                    if (Constants.isShielding && !noReadSensitive) {
                        for (String word : readSensitiveWords) {
                            text.setLineContent(text.getLineContent().replace(word, getChangeWord(word.length())));
                        }
                    }
                    if (text != null && !TextUtils.isEmpty(text.getLineContent()) && text.getLineContent().equals(" ")) {
                        total_y += m_duan;
                    } else {
                        if (text.getType() == 1) {
                            drawLineIntervalText(canvas, text, total_y);
                        } else {
                            canvas.drawText(text.getLineContent(), mLineStart, total_y, mPaint);
                        }

                        total_y += m_iFontHeight;
                        readStatus.currentPageConentLength += text.getLineContent().length();
                    }
                }
                mTextContentHeight = (int) (total_y + fm.ascent);
            }
        }
        canvas.restore();
    }

    private String getChangeWord(int len) {
        StringBuilder stringBuffer = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            stringBuffer.append('*');
        }
        return stringBuffer.toString();
    }

    private void drawLineIntervalText(Canvas canvas, NovelLineBean novelLineBean, float total_y) {
        if (novelLineBean == null || novelLineBean.getLineContent() == null || novelLineBean.getLineContent().length() < 3) {
            return;
        }
        ArrayList<Float> arrLenths = novelLineBean.getArrLenths();
        int length = novelLineBean.getLineContent().length();
        if (arrLenths == null || arrLenths.size() < length) {
            return;
        }

        int charNum;
        if (novelLineBean.isLastIsPunct()) {
            charNum = length - 2;
        } else {
            charNum = length - 1;
        }
        float marg = (mWidth - novelLineBean.getLineLength()) / charNum;
        float star;
        for (int i = 0; i < length; i++) {
            star = mLineStart + arrLenths.get(i) + marg * i;
            char c = novelLineBean.getLineContent().charAt(i);
            if (i == length - 1 && isEndPunct(c)) {
                Rect rect = new Rect();
                mPaint.getTextBounds(String.valueOf(c), 0, 1, rect);
                star -= marg;
                star -= rect.left;
                star += (ReadConstants.chineseWth / 2 - rect.width()) / 2;
                canvas.drawText(String.valueOf(c), star, total_y, mPaint);
            } else {
                canvas.drawText(String.valueOf(c), star, total_y, mPaint);
            }
        }
    }

    private boolean isEndPunct(char ch) {
        boolean isInclude = false;
        for (char c : ReadConstants.endPuncts) {
            if (ch == c) {
                isInclude = true;
                break;
            }
        }
        return isInclude;
    }
}
