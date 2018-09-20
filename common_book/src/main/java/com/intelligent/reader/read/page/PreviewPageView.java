package com.intelligent.reader.read.page;

import net.lzbook.kit.R;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.utils.logger.AppLog;
import net.lzbook.kit.utils.Tools;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

public class PreviewPageView extends SurfaceView implements SurfaceHolder.Callback {

    private static String TAG = PreviewPageView.class.getSimpleName();
    private SurfaceHolder surfaceHolder;

    private int screenWidth;
    private int screenHeight;
    private float screenDensity;
    private float screenScaledDensity;

    private Paint mPaint;
    private Paint backgroundPaint;

    private Resources resources;

    //背景Bitmap
    private Bitmap mBackground;

    public PreviewPageView(Context context, Resources resources, int screenWidth, int screenHeight) {
        super(context);
        this.resources = resources;

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.screenDensity = displayMetrics.density;
        this.screenScaledDensity = displayMetrics.scaledDensity;


        mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);

        backgroundPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setDither(true);

        initData();
    }

    public PreviewPageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public PreviewPageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private static Bitmap resizeBitmap(Bitmap bitmap, int w, int h) {
        if (bitmap != null) {
            float scaleWidth = ((float) w) / bitmap.getWidth();
            float scaleHeight = ((float) h) / bitmap.getHeight();
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (resizedBitmap != bitmap) {
                bitmap.recycle();
            }
            return resizedBitmap;
        } else {
            return null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        drawPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private void initData() {
        AppLog.e(TAG, "initData");
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
    }

    public void drawPreview() {

        String message = "读史使人明智，读诗使人灵秀，数学使人周密，科学使人深刻，伦理学使人庄重，逻辑修辞使人善辩，凡有所学，皆成性格。";
        List<String> data = initTextContent(message);

        if (mBackground != null) {
            mBackground.recycle();
        }

        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas != null) {
            drawText(canvas, data);
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private ArrayList<String> initTextContent(String content) {

        TextPaint mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(Constants.FONT_SIZE * screenScaledDensity);

        float width = screenWidth - screenDensity * Constants.READ_CONTENT_PAGE_LEFT_SPACE * 2;
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float tHeight = fontMetrics.descent - fontMetrics.ascent;
        float height = screenHeight - tHeight - screenDensity * 10 * 2;

        float lineHeight = fontMetrics.descent - fontMetrics.ascent + Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE * screenScaledDensity;

        int textSpace = 0;

        ArrayList<String> contentList = getNovelText(mTextPaint, content, width);
        ArrayList<String> pageLines = new ArrayList<>();

        for (int i = 0; i < contentList.size(); i++) {
            String lineText = contentList.get(i);
            textSpace += lineHeight;

            if (textSpace < height) {
                pageLines.add(lineText);
            }
        }

        return pageLines;
    }

    private ArrayList<String> getNovelText(TextPaint textPaint, String text, float width) {
        ArrayList<String> list = new ArrayList<>();
        float w = 0;
        int start = 0;
        char mChar;
        float[] widths = new float[1];
        float[] chineseWidth = new float[1];
        textPaint.getTextWidths("正", chineseWidth);
        if (text == null) {
            return list;
        }
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            mChar = text.charAt(i);
            if (mChar == '\n') {
                widths[0] = 0;
            } else if (Tools.isChinese(mChar)) {
                widths[0] = chineseWidth[0];
            } else {
                String srt = String.valueOf(mChar);
                textPaint.getTextWidths(srt, widths);
            }
            if (mChar == '\n') {
                count++;
                list.add(text.substring(start, i) + " ");
                if (count > 3) {
                    list.add(" ");
                }
                start = i + 1;
                w = 0;
            } else {
                w += widths[0];
                if (w > width) {
                    list.add(text.substring(start, i));
                    start = i;
                    i--;
                    w = 0;
                } else {
                    if (i == (text.length() - 1)) {
                        list.add(text.substring(start, text.length()));
                    }
                }
            }
        }
        return list;
    }

    //绘制文字
    public synchronized void drawText(Canvas canvas, List<String> pageLines) {
        mPaint.setTextSize(Constants.FONT_SIZE * screenScaledDensity);
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();

        float m_iFontHeight = fontMetrics.descent - fontMetrics.ascent + Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE * screenScaledDensity;
        float sHeight = Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE * screenScaledDensity;

        float height = screenHeight - screenDensity * 10 * 2 + sHeight;
        float y = fontMetrics.descent - fontMetrics.ascent + 10 * screenScaledDensity;

        float textHeight = 0;

        if (pageLines != null) {
            int size = pageLines.size();
            for (int i = 0; i < size; i++) {
                textHeight += m_iFontHeight;
            }
        }

        if (height - textHeight > 2 && height - textHeight < 4 * (fontMetrics.descent - fontMetrics.ascent)) {
            int n = Math.round((height - 0) / m_iFontHeight);
            float distance = (height - textHeight) / n;
            m_iFontHeight = fontMetrics.descent - fontMetrics.ascent + Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE * screenScaledDensity + distance;
        } else if (textHeight - height > 2) {
            int n = Math.round((height - 0) / m_iFontHeight);
            float distance = (textHeight - (height)) / n;
            m_iFontHeight = fontMetrics.descent - fontMetrics.ascent + Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE * screenScaledDensity - distance;
        }

        drawBackground(canvas);

        if (pageLines != null && !pageLines.isEmpty()) {
            for (int i = 0; i < pageLines.size(); i++) {
                String text = pageLines.get(i);
                canvas.drawText(text, Constants.READ_CONTENT_PAGE_LEFT_SPACE * screenScaledDensity, y + m_iFontHeight * i, mPaint);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void drawBackground(Canvas canvas) {

        canvas.drawRect(0, 0, screenWidth, screenHeight, setPaintColor(backgroundPaint, 0));

    }

    public Paint setPaintColor(Paint paint, int type) {

        int color_int = R.color.reading_bg_day_other;
        if (Constants.MODE == 51) {
            if (type == 0) {
                color_int = R.color.reading_backdrop_first;
            } else {
                color_int = R.color.reading_text_color_first;
            }
        } else if (Constants.MODE == 52) {
            if (type == 0) {
                color_int = R.color.reading_backdrop_second;
            } else {
                color_int = R.color.reading_text_color_second;
            }
        } else if (Constants.MODE == 53) {
            if (type == 0) {
                color_int = R.color.reading_backdrop_third;
            } else {
                color_int = R.color.reading_text_color_third;
            }
        } else if (Constants.MODE == 54) {
            if (type == 0) {
                color_int = R.color.reading_backdrop_fourth;
            } else {
                color_int = R.color.reading_text_color_fourth;
            }
        } else if (Constants.MODE == 55) {
            if (type == 0) {
                color_int = R.color.reading_backdrop_fifth;
            } else {
                color_int = R.color.reading_text_color_fifth;
            }
        } else if (Constants.MODE == 56) {
            if (type == 0) {
                color_int = R.color.reading_backdrop_sixth;
            } else {
                color_int = R.color.reading_text_color_sixth;
            }
        } else if (Constants.MODE == 61) {
            if (type == 0) {
                color_int = R.color.reading_backdrop_night;
            } else {
                color_int = R.color.reading_text_color_night;
            }
        } else {
            if (type == 0) {
                color_int = R.color.reading_backdrop_first;
            } else {
                color_int = R.color.reading_text_color_first;
            }
        }
        if (type == -1) {
            color_int = R.color.color_black_00ffffff;
        }
        paint.setColor(resources.getColor(color_int));
        return paint;
    }

    public void setTextColor(int textColor) {
        mPaint.setColor(textColor);
    }
}
