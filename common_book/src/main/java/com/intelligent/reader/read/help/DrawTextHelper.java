package com.intelligent.reader.read.help;

import com.intelligent.reader.R;
import com.intelligent.reader.app.BookApplication;
import com.intelligent.reader.util.DisplayUtils;

import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.NovelLineBean;
import net.lzbook.kit.data.bean.ReadStatus;
import net.lzbook.kit.data.bean.SensitiveWords;
import net.lzbook.kit.utils.AppLog;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextPaint;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class DrawTextHelper {
    private static final String TAG = "DrawTextHelper";

    int mBitmapWidth;
    int mBitmapHeight;
    int left1;
    int right1;
    int top1;
    int bottom1;
    Canvas canvas;
    private Paint mPaint;
    private Paint duanPaint;
    private Paint backgroundPaint;
    private Paint textPaint;
    private Paint chapterFirstPaint;
    private Paint headPaint;
    private Paint footPaint;
    private TextPaint mchapterPaint;
    private ReadStatus readStatus;
    private Resources resources;
    private static Bitmap mBitmap;
    private static Bitmap mBackground; // 背景Bitmap
    private static Bitmap mKraftBackground; // 牛皮纸模式 底部背景
    private static Bitmap mSoftBackground;
    private static Bitmap mIconBitmap;
    private int footHeight;
    private int slideupfootHeight;
    private int unit;
    private int textColor;
    private int translateColor;
    private float percent;
    private String timeText;


    private Paint nightPaint;
    private SensitiveWords readSensitiveWord;
    private List<String> readSensitiveWords;
    private boolean noReadSensitive = false;
    private float firstchapterHeight;
    private float mWidth;
    private float mLineStart;

    public static void clean() {
        mBitmap = null;
        mBackground = null;
        mKraftBackground = null;
        mSoftBackground = null;
        mIconBitmap = null;
    }

    public DrawTextHelper(Resources res) {
        this.resources = res;
        this.readSensitiveWord = SensitiveWords.getReadSensitiveWords();
        if (readSensitiveWord != null && readSensitiveWord.list.size() > 0) {
            readSensitiveWords = readSensitiveWord.getList();
            noReadSensitive = false;
        } else {
            noReadSensitive = true;
        }

        readStatus = BookApplication.getGlobalContext().getReadStatus();

        translateColor = res.getColor(R.color.color_black_00000000);
        footHeight = (int) (18 * readStatus.screenScaledDensity);
        slideupfootHeight = (int) (23 * readStatus.screenScaledDensity);
        unit = (int) (2 * readStatus.screenScaledDensity);

        mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);

        duanPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        duanPaint.setStyle(Paint.Style.FILL);
        duanPaint.setAntiAlias(true);
        duanPaint.setDither(true);

        backgroundPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setDither(true);

        headPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        headPaint.setStyle(Paint.Style.FILL);
        headPaint.setAntiAlias(true);
        headPaint.setDither(true);
        headPaint.setColor(res.getColor(R.color.reading_text_color_first));
        headPaint.setTextSize(12 * readStatus.screenScaledDensity);

        footPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        footPaint.setStyle(Paint.Style.FILL);
        footPaint.setAntiAlias(true);
        footPaint.setDither(true);
        footPaint.setColor(res.getColor(R.color.reading_text_color_first));
        footPaint.setTextSize(10 * readStatus.screenScaledDensity);

        textPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setAntiAlias(true);
        textPaint.setDither(true);
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(Constants.FONT_CHAPTER_SIZE * readStatus.screenScaledDensity);

        chapterFirstPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        chapterFirstPaint.setStyle(Paint.Style.FILL);
        chapterFirstPaint.setAntiAlias(true);
        chapterFirstPaint.setDither(true);
        chapterFirstPaint.setColor(Color.WHITE);
        chapterFirstPaint.setTextSize(Constants.FONT_FIRST_CHAPTER_SIZE * readStatus.screenScaledDensity);

        mchapterPaint = new TextPaint();
        mchapterPaint.setTextSize(12 * readStatus.screenScaledDensity);

        nightPaint = new Paint();
        nightPaint.setAlpha(80);
    }

    private static Bitmap resizeBitmap(Bitmap bitmap, int w, int h) {
        if (bitmap != null) {
            Bitmap copyBitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
            float scaleWidth = ((float) w) / copyBitmap.getWidth();
            float scaleHeight = ((float) h) / copyBitmap.getHeight();
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            Bitmap resizedBitmap = Bitmap.createBitmap(copyBitmap, 0, 0, copyBitmap.getWidth(), copyBitmap.getHeight(), matrix, true);
            bitmap.recycle();
            copyBitmap.recycle();
            bitmap = null;
            copyBitmap = null;
            return resizedBitmap;
        } else {
            return null;
        }
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        mPaint.setColor(textColor);
        footPaint = setPaintColor(footPaint, 1);
    }

    public void setPercent(float percent, Canvas canvas) {
        this.percent = percent;
        if (readStatus.sequence != -1) {
            drawBattery(canvas);
        }
    }

    public void setTimeText(String time) {
        this.timeText = time;
    }

    public void changeBattyBitmp(int res) {
        try {
            mBitmap = BitmapFactory.decodeResource(resources, res);
        } catch (OutOfMemoryError e) {
            System.gc();
            System.gc();
            mBitmap = BitmapFactory.decodeResource(resources, res);
        }
    }

    public void resetBackBitmap() {
//        if (mBackground != null && !mBackground.isRecycled()) {
//            mBackground.recycle();
//            mBackground = null;
//        }
//        if (mKraftBackground != null && !mKraftBackground.isRecycled()) {
//            mKraftBackground.recycle();
//            mKraftBackground = null;
//        }
//        if (mSoftBackground != null && !mSoftBackground.isRecycled()) {
//            mSoftBackground.recycle();
//            mSoftBackground = null;
//        }
    }

    /**
     * paint
     * type  0: 画背景
     */
    private Paint setPaintColor(Paint paint, int type) {

        int color_int;
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
        paint.setColor(resources.getColor(color_int));
        return paint;
    }

    private Paint setPaintColorChapter(Paint paint, int type) {

        int color_int;
        if (Constants.MODE == 51) {
            if (type == 0) {
                color_int = R.color.reading_backdrop_first;
            } else {
                color_int = R.color.reading_operation_text_color_first;
            }
        } else if (Constants.MODE == 52) {
            if (type == 0) {
                color_int = R.color.reading_backdrop_second;
            } else {
                color_int = R.color.reading_operation_text_color_second;
            }
        } else if (Constants.MODE == 53) {
            if (type == 0) {
                color_int = R.color.reading_backdrop_third;
            } else {
                color_int = R.color.reading_operation_text_color_third;
            }
        } else if (Constants.MODE == 54) {
            if (type == 0) {
                color_int = R.color.reading_backdrop_fourth;
            } else {
                color_int = R.color.reading_operation_text_color_fourth;
            }
        } else if (Constants.MODE == 55) {
            if (type == 0) {
                color_int = R.color.reading_backdrop_fifth;
            } else {
                color_int = R.color.reading_operation_text_color_fifth;
            }
        } else if (Constants.MODE == 56) {
            if (type == 0) {
                color_int = R.color.reading_backdrop_sixth;
            } else {
                color_int = R.color.reading_operation_text_color_sixth;
            }
        } else if (Constants.MODE == 61) {
            if (type == 0) {
                color_int = R.color.reading_backdrop_night;
            } else {
                color_int = R.color.reading_operation_text_color_night;
            }
        } else {
            if (type == 0) {
                color_int = R.color.reading_backdrop_first;
            } else {
                color_int = R.color.reading_operation_text_color_first;
            }
        }
        paint.setColor(resources.getColor(color_int));
        return paint;
    }

    //上下滑动
    public synchronized float drawText(Canvas canvas, List<NovelLineBean> pageLines, ArrayList<NovelLineBean> chapterNameList) {
        readStatus.currentPageConentLength = 0;
        mPaint.setTextSize(Constants.FONT_SIZE * readStatus.screenScaledDensity);
        duanPaint.setTextSize(1 * readStatus.screenScaledDensity);
        FontMetrics fm = mPaint.getFontMetrics();
        mWidth = readStatus.screenWidth - readStatus.screenDensity * Constants.READ_CONTENT_PAGE_LEFT_SPACE * 2;
        mLineStart = Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity;

        float lineSpace = Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE * readStatus.screenScaledDensity;
        float m_iFontHeight = fm.descent - fm.ascent + lineSpace;
        float m_duan = Constants.READ_PARAGRAPH_SPACE * lineSpace;

        float total_y = -fm.ascent;
        float pageHeight = 0;

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        if (pageLines != null && !pageLines.isEmpty()) {

            if (pageLines.get(0).getLineContent().startsWith("txtzsydsq_homepage")) {// 封面页
                pageHeight = drawHomePage(canvas);
            } else if (pageLines.get(0).getLineContent().startsWith("chapter_homepage")) {// 章节首页
                pageHeight = drawChapterPage(canvas, pageLines, chapterNameList);
            } else {
                for (int i = 0; i < pageLines.size(); i++) {
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
                    if (i == pageLines.size() - 1) {
                        pageHeight = total_y + fm.ascent;
                    }
                }
            }
            return pageHeight;
        }
        if (!Constants.isSlideUp) {
            drawFoot(canvas);
        }
        return pageHeight;
    }

    public synchronized Paint drawText(Canvas canvas, List<NovelLineBean> pageLines) {
        mPaint.setTextSize(Constants.FONT_SIZE * readStatus.screenScaledDensity);//设置字体大小
        duanPaint.setTextSize(1 * readStatus.screenScaledDensity);//设置画笔字体大小1x屏幕密度
        FontMetrics fm = mPaint.getFontMetrics();//字体测量类
        mLineStart = Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity;//开始位置20x屏幕密度
        mWidth = readStatus.screenWidth - readStatus.screenDensity * Constants.READ_CONTENT_PAGE_LEFT_SPACE * 2;//720-2*20

        float lineSpace = Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE * readStatus.screenScaledDensity;//10.8 行间距 = 行间距比例*字体大小*屏幕密度
        float m_iFontHeight = fm.descent - fm.ascent + lineSpace;//字体下坡度 - 字体上坡度 + 行间距 8.7 - -33.39 + 10.8 ≈ 52.9875
        float m_duan = Constants.READ_PARAGRAPH_SPACE * lineSpace;
        float height = readStatus.screenHeight - readStatus.screenDensity * Constants.READ_CONTENT_PAGE_TOP_SPACE * 2 + lineSpace;//开始高度 = 屏幕高度 - 密度*45*2+行间距
        float total_y = 0;//

        total_y += Constants.READ_CONTENT_PAGE_TOP_SPACE * readStatus.screenDensity - fm.ascent;

        float textHeight = 0;
        float duan = 0;//段落
        boolean isShow_big_ad = false;
        boolean lastIsDuan = false;
        if (pageLines != null && !pageLines.isEmpty()) {

            if (" ".equals(pageLines.get(0).getLineContent())) {
                pageLines.remove(0);
            }

            int size = pageLines.size();
            for (int i = 0; i < size; i++) {
                String text = pageLines.get(i).getLineContent();
                if (!TextUtils.isEmpty(text) && text.equals(" ")) {
                    textHeight += m_duan;
                    duan += m_duan;
                    lastIsDuan = true;//最后一段画一半，另一半画下页
                } else {
                    textHeight += m_iFontHeight;
                    lastIsDuan = false;
                }
            }

        }

        if (lastIsDuan) {//判断是否需要断字
            textHeight -= m_duan;
        }

        if (height - textHeight > 2 && height - textHeight < 4 * (fm.descent - fm.ascent)) {
            int numLine = (int) Math.round((height - duan) / m_iFontHeight);// 行数
            int numDuan = (int) Math.round(duan / m_duan);// 段间距数
            float distanceExtra = height - textHeight;//
            float distanceDuan = duan * distanceExtra / height;
            float distanceLine = distanceExtra - distanceDuan;
            m_iFontHeight = m_iFontHeight + distanceLine / numLine;
            m_duan = m_duan + distanceDuan / numDuan;
        } else if (textHeight - height > 2) {
            int n = (int) Math.round((height - duan) / m_iFontHeight);// 行数 21
            float distance = (textHeight - (height)) / n;
            m_iFontHeight = m_iFontHeight - distance;
        }
        drawBackground(canvas);//行背景


        if (pageLines != null && !pageLines.isEmpty()) {
            if (pageLines.get(0).getLineContent().startsWith("txtzsydsq_homepage")) {// 封面页
                drawHomePage(canvas);
            } else if (pageLines.get(0).getLineContent().startsWith("chapter_homepage")) {// 章节首页
                drawChapterPage(canvas, pageLines);
            } else {
                for (int i = 0; i < pageLines.size(); i++) {
                    NovelLineBean text = pageLines.get(i);
                    if (Constants.isShielding && !noReadSensitive) {
                        for (String word : readSensitiveWords) {
                            text.setLineContent(text.getLineContent().replace(word, getChangeWord(word.length())));
                        }
                    }
                    if (text != null && !TextUtils.isEmpty(text.getLineContent()) && text.getLineContent().equals(" ")) {
                        total_y += m_duan;
                    } else if (text != null && text.getLineContent().contains(NovelHelper.empty_page_ad) || text.getLineContent().startsWith(NovelHelper.empty_page_ad)) {
                    } else if (text != null && text.getLineContent().contains(NovelHelper.empty_page_ad_inChapter) || text.getLineContent().startsWith(NovelHelper.empty_page_ad_inChapter)) {
                    } else {
                        if (text.getType() == 1) {
                            drawLineIntervalText(canvas, text, total_y);//开始画行
                        } else {
                            canvas.drawText(text.getLineContent(), mLineStart, total_y, mPaint);//每段最后一行
                        }

                        total_y += m_iFontHeight;
                    }

                    if (i == pageLines.size() - 1) {
                        total_y -= m_iFontHeight;
                    }

                }
            }
        }

        Paint mOperationPaint = null;

        if (!Constants.isSlideUp) {
            mOperationPaint = drawFoot(canvas);
        }
        return mOperationPaint;
    }

    /**
     * 完整行单个字符绘制
     */
    private void drawLineIntervalText(Canvas canvas, NovelLineBean novelLineBean, float total_y) {
        if (novelLineBean == null || novelLineBean.getLineContent() == null || novelLineBean.getLineContent().length() < 3) {
            return;
        }
        ArrayList<Float> arrLenths = novelLineBean.getArrLenths();
        int length = novelLineBean.getLineContent().length();
        if (arrLenths == null || arrLenths.size() < length) {
            return;
        }

//        float[] chineseWidth = new float[1];
//        float[] charWidth = new float[1];
//        mPaint.getTextWidths("正", chineseWidth);
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
//            if (Tools.isChinese(c) || c == '，' || c == '。'){
//                charWidth[0] = chineseWidth[0];
//            }else {
//                charWidth[0] = mPaint.measureText(String.valueOf(c));
//            }
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

    //上下滑动首页
    private float drawChapterPage(Canvas canvas, List<NovelLineBean> pageLines, ArrayList<NovelLineBean> chapterNameList) {
        mPaint.setTextSize(Constants.FONT_SIZE * readStatus.screenScaledDensity);
        duanPaint.setTextSize(1 * readStatus.screenScaledDensity);
        textPaint.setTextSize(Constants.FONT_CHAPTER_SIZE * readStatus.screenScaledDensity);
        mWidth = readStatus.screenWidth - readStatus.screenDensity * Constants.READ_CONTENT_PAGE_LEFT_SPACE * 2;
        FontMetrics fm_chapter = textPaint.getFontMetrics();
        float m_iFontHeight_chapter = 0;

        FontMetrics fm = mPaint.getFontMetrics();

        float lineSpace = Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE * readStatus.screenScaledDensity;
        float m_iFontHeight = fm.descent - fm.ascent + lineSpace;
        float m_duan = Constants.READ_PARAGRAPH_SPACE * lineSpace;

        // 正文行首与顶部间距
        float total_y = -fm.ascent;
        float y_chapter;
        float pageHeight = 0;

        // 章节头顶部间距
        y_chapter = 39 * readStatus.screenScaledDensity;

        setPaintColor(textPaint, 1);

        int size_c;

        // 章节头
        if (chapterNameList != null && !chapterNameList.isEmpty()) {
            size_c = chapterNameList.size();
            for (int i = 0; i < size_c; i++) {
                if (i == 0) {
                    if (chapterNameList.get(0) != null && !TextUtils.isEmpty(chapterNameList.get(0).getLineContent())) {
                        NovelLineBean chapterNameRemain = chapterNameList.get(0);

                        textPaint.setTextSize(16 * readStatus.screenScaledDensity);
                        canvas.drawText(chapterNameRemain.getLineContent(), Constants.READ_CONTENT_PAGE_LEFT_SPACE
                                * readStatus.screenScaledDensity, y_chapter, textPaint);
                    }
                } else {
                    textPaint.setTextSize(23 * readStatus.screenScaledDensity);
                    fm_chapter = textPaint.getFontMetrics();
                    m_iFontHeight_chapter = fm_chapter.descent - fm_chapter.ascent + 0.5f
                            * Constants.FONT_CHAPTER_DEFAULT * readStatus.screenScaledDensity;
                    canvas.drawText(chapterNameList.get(i).getLineContent(), Constants.READ_CONTENT_PAGE_LEFT_SPACE
                            * readStatus.screenScaledDensity, y_chapter + m_iFontHeight_chapter * i, textPaint);
                }
            }

            total_y += 3 * 15 * readStatus.screenScaledDensity;
            total_y += Constants.READ_CONTENT_PAGE_TOP_SPACE * readStatus.screenDensity;

            // 章节头与正文间距
            if (size_c > 1) {
                total_y += 75 * readStatus.screenScaledDensity;
            }

            for (int i = 0, j = pageLines.size(); i < j; i++) {
                if (i > size_c) {
                    NovelLineBean text = pageLines.get(i);
                    if (Constants.isShielding && !noReadSensitive) {
                        for (String word : readSensitiveWords) {
                            text.setLineContent(text.getLineContent().replace(word, getChangeWord(word.length())));
                        }
                    }
                    if (text != null && !TextUtils.isEmpty(text.getLineContent())) {
                        if (text.getLineContent().equals(" ")) {
                            total_y += m_duan;
                        } else if (text.getLineContent().equals("chapter_homepage  ")) {

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
                }

                if (i == pageLines.size() - 1) {
                    pageHeight = total_y + fm.ascent;
                }
            }
        }
        return pageHeight;
    }

    /*
     * 章节首页提示效果
     */
    private void drawChapterPage(Canvas canvas, List<NovelLineBean> pageLines) {
        mPaint.setTextSize(Constants.FONT_SIZE * readStatus.screenScaledDensity);
        duanPaint.setTextSize(1 * readStatus.screenScaledDensity);
        textPaint.setTextSize(Constants.FONT_CHAPTER_SIZE * readStatus.screenScaledDensity);
        mWidth = readStatus.screenWidth - readStatus.screenDensity * Constants.READ_CONTENT_PAGE_LEFT_SPACE * 2;
        FontMetrics fm_chapter = textPaint.getFontMetrics();
        float m_iFontHeight_chapter;

        FontMetrics fm = mPaint.getFontMetrics();

        float lineSpace = Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE * readStatus.screenScaledDensity;
        float m_iFontHeight = fm.descent - fm.ascent + lineSpace;
        float m_duan = Constants.READ_PARAGRAPH_SPACE * lineSpace;

        float total_y = -fm.ascent;
        float y_chapter;
        y_chapter = 65 * readStatus.screenScaledDensity;

        setPaintColor(textPaint, 1);
        int size_c;

        // 章节头
        if (readStatus.chapterNameList != null && !readStatus.chapterNameList.isEmpty()) {
            size_c = readStatus.chapterNameList.size();
            for (int i = 0; i < size_c; i++) {
                if (i == 0) {
                    AppLog.e(TAG, "DrawChapterPage: " + readStatus.chapterName);
                    if (!TextUtils.isEmpty(readStatus.chapterName)) {
                        String chapterNameRemain = readStatus.chapterNameList.get(0).getLineContent();

                        textPaint.setTextSize(16 * readStatus.screenScaledDensity);
                        canvas.drawText(chapterNameRemain, Constants.READ_CONTENT_PAGE_LEFT_SPACE
                                * readStatus.screenScaledDensity, y_chapter, textPaint);
                    }
                } else {
                    textPaint.setTextSize(23 * readStatus.screenScaledDensity);
                    fm_chapter = textPaint.getFontMetrics();
                    m_iFontHeight_chapter = fm_chapter.descent - fm_chapter.ascent + 0.5f
                            * Constants.FONT_CHAPTER_DEFAULT * readStatus.screenScaledDensity;
                    canvas.drawText(readStatus.chapterNameList.get(i).getLineContent(), Constants.READ_CONTENT_PAGE_LEFT_SPACE
                            * readStatus.screenScaledDensity, y_chapter + m_iFontHeight_chapter * i, textPaint);
                }
            }

            float font_height = fm.descent - fm.ascent;
            total_y += 3 * 15 * readStatus.screenScaledDensity;
            float textHeight = 0;
            float duan = 0;
            boolean lastIsDuan = false;
            if (pageLines != null) {
                int size = pageLines.size();
                for (int i = 0; i < size; i++) {
                    String text = pageLines.get(i).getLineContent();
                    if (Constants.isShielding && !noReadSensitive) {
                        for (String word : readSensitiveWords) {
                            text = text.replace(word, getChangeWord(word.length()));
                        }
                    }
                    if (!TextUtils.isEmpty(text) && text.equals(" ")) {
                        textHeight += m_duan;
                        duan += m_duan;
                        lastIsDuan = true;
                    } else if (!text.equals("chapter_homepage  ")) {
                        textHeight += m_iFontHeight;
                        lastIsDuan = false;
                    }
                }

            }
            float height = 0;
            if (Constants.isSlideUp) {
                height = readStatus.screenHeight - 96 * readStatus.screenScaledDensity + lineSpace - font_height;
            } else {
                height = readStatus.screenHeight - readStatus.screenDensity * Constants.READ_CONTENT_PAGE_TOP_SPACE
                        * 2 - 3 * 15 * readStatus.screenScaledDensity + lineSpace;
                total_y += Constants.READ_CONTENT_PAGE_TOP_SPACE * readStatus.screenDensity;
            }

            if (size_c > 1) {
                total_y += 75 * readStatus.screenScaledDensity;
                height -= 75 * readStatus.screenScaledDensity;
            }

            if (lastIsDuan) {
                total_y += m_duan;
            }

            if (height - textHeight > 2 && height - textHeight < 3 * (fm.descent - fm.ascent)) {
                float distanceExtra = height - textHeight;
                total_y += distanceExtra;
            } else if (textHeight - height > 2) {
                int n = (int) Math.floor((height - duan) / m_iFontHeight);// 行数
                float distance = (textHeight - height) / n;
                m_iFontHeight = fm.descent - fm.ascent + Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE
                        * readStatus.screenScaledDensity - distance;
            }

            textPaint.setColor(textColor);
            textPaint.setTextSize(Constants.FONT_CHAPTER_DEFAULT * readStatus.screenScaledDensity);
            for (int i = 0, j = pageLines.size(); i < j; i++) {
                if (i > size_c) {
                    NovelLineBean text = pageLines.get(i);
                    if (text != null && !TextUtils.isEmpty(text.getLineContent())) {
                        if (text.getLineContent().equals(" ")) {
                            total_y += m_duan;
                        } else if (text.getLineContent().equals("chapter_homepage  ")) {
                            AppLog.e(TAG, "chapter_homepage3:" + text);
                        } else {

                            if (text.getType() == 1) {
                                drawLineIntervalText(canvas, text, total_y);
                            } else {
                                canvas.drawText(text.getLineContent(), mLineStart, total_y, mPaint);
                            }

                            total_y += m_iFontHeight;
                            firstchapterHeight = total_y;
                        }
                    }

                }

                if (i == pageLines.size() - 1) {
                    firstchapterHeight -= m_iFontHeight;
                }
            }
        }
    }

    public Paint drawFoot(Canvas canvas) {
        if (readStatus.sequence != -1 && !Constants.isSlideUp) {
//            drawHeadBackground(canvas);
//            drawFootBackground(canvas);
            //17.12.26注释
//            drawOrigin(canvas);
//            drawTransCoding(canvas);
//            drawChapterNum(canvas);
//            drawBattery(canvas);
//            drawTime(canvas);
//            drawPageNum(canvas);
            return footPaint;
        }
        return null;
    }

    private void drawOrigin(Canvas canvas) {
        float y = 20 * readStatus.screenScaledDensity;
        float x = Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity;
        canvas.drawText(resources.getString(R.string.origin_text), x, y, setPaintColorChapter(footPaint, 1));
    }

    private void drawTransCoding(Canvas canvas) {
        float y = 20 * readStatus.screenScaledDensity;
        float x = readStatus.screenWidth - Constants.READ_CONTENT_PAGE_LEFT_SPACE
                * readStatus.screenScaledDensity - footPaint.measureText(resources.getString(R.string.trans_coding_text));
        canvas.drawText(resources.getString(R.string.trans_coding_text), x, y, setPaintColorChapter(footPaint, 1));
    }

    public void drawHeadNew(Canvas canvas) {
        if (!readStatus.isCanDrawFootView && !Constants.isSlideUp) {
            return;
        }
        footPaint = setPaintColor(footPaint, 1);
        drawHeadBackgroundNew(canvas);
        drawChapterNumNew(canvas);
        drawBatteryNew(canvas);
        drawTimeNew(canvas);
        drawPageNumNew(canvas);
    }

    private void drawChapterNum(Canvas canvas) {
        float position;
        position = readStatus.screenHeight - 10 * readStatus.screenScaledDensity;

        int chapter = readStatus.sequence + 1;

        float strwid = footPaint.measureText(chapter + "/" + readStatus.chapterCount + "章");
        canvas.drawText(chapter + "/" + readStatus.chapterCount + "章", readStatus.screenWidth / 2 - strwid / 2,
                position, setPaintColorChapter(footPaint, 1));
        drawChapterName(canvas);
    }

    private void drawChapterName(Canvas canvas) {
        if (readStatus.chapterName == null)
            return;
        float position;
        position = 20 * readStatus.screenScaledDensity;
        String name = readStatus.chapterName;
        if (readStatus.chapterName.length() > 17) {
            name = name.substring(0, 16) + "...";
        }

        float strwid = footPaint.measureText(name);

        canvas.drawText(name, readStatus.screenWidth / 2 - strwid / 2,
                position, setPaintColorChapter(footPaint, 1));
    }

    private void drawChapterNumNew(Canvas canvas) {
        int chapter = readStatus.sequence + 1;
        canvas.drawText(chapter + "/" + readStatus.chapterCount + "章", Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity,
                17 * readStatus.screenScaledDensity, setPaintColorChapter(footPaint, 1));

    }

    private void drawPageNum(Canvas canvas) {
        float position;
        position = readStatus.screenHeight - 10 * readStatus.screenScaledDensity;
        String page_num = "本章第" + readStatus.currentPage + "/" + readStatus.pageCount;
        float temp_width = footPaint.measureText(page_num);
        canvas.drawText(page_num, readStatus.screenWidth - Constants.READ_CONTENT_PAGE_LEFT_SPACE
                * readStatus.screenScaledDensity - temp_width, position, setPaintColorChapter(footPaint, 1));
    }

    private void drawPageNumNew(Canvas canvas) {
        String page_num = readStatus.currentPage + "/" + readStatus.pageCount + "页";
        float temp_width = footPaint.measureText(page_num);
        canvas.drawText(page_num, readStatus.screenWidth / 2 - temp_width / 2, 17 * readStatus.screenScaledDensity, setPaintColorChapter(footPaint, 1));
    }

    private void drawTime(Canvas canvas) {
        if (timeText == null || timeText.length() <= 0) {
            return;
        }
        float position;
        position = readStatus.screenHeight - 10 * readStatus.screenScaledDensity;
        canvas.drawText(timeText, Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity + right1 + 5
                        * readStatus.screenScaledDensity, position,
                setPaintColorChapter(footPaint, 1));
    }

    private void drawTimeNew(Canvas canvas) {
        if (timeText == null || timeText.length() <= 0) {
            return;
        }
        float temp_width = footPaint.measureText(timeText);
        canvas.drawText(timeText, readStatus.screenWidth - Constants.READ_CONTENT_PAGE_LEFT_SPACE
                        * readStatus.screenScaledDensity - temp_width, 17 * readStatus.screenScaledDensity,
                setPaintColorChapter(footPaint, 1));
    }

    private void drawBattery(Canvas canvas) {
        if (mBitmap == null || mBitmap.isRecycled()) {
            return;
        }
        float position;
        position = readStatus.screenHeight - (10 * readStatus.screenScaledDensity);
        float position2;
        position2 = readStatus.screenHeight - (bottom1 + 10 * readStatus.screenScaledDensity);

        float position3;
        position3 = readStatus.screenHeight - (bottom1 - top1 + 10 * readStatus.screenScaledDensity) + 1;
        canvas.drawRect(Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity + left1 + 2,
                position3,
                (right1 - (left1 + 1)) * percent
                        + (Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity + left1 - 1),
                position, setPaintColorChapter(footPaint, 1));
        canvas.drawBitmap(mBitmap, Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity + left1,
                position2, setPaintColorChapter(footPaint, 1));
    }

    private void drawBatteryNew(Canvas canvas) {
        if (mBitmap == null || mBitmap.isRecycled()) {
            return;
        }
        if (timeText == null || timeText.length() <= 0) {
            return;
        }
        float temp_width = footPaint.measureText(timeText);
        float r_top = 7 * readStatus.screenScaledDensity + top1 + 3;
        float r_bottom = (bottom1 - top1 + 7 * readStatus.screenScaledDensity) + 4;
        float r_left = readStatus.screenWidth - Constants.READ_CONTENT_PAGE_LEFT_SPACE
                * readStatus.screenScaledDensity - temp_width - right1 - 5
                * readStatus.screenScaledDensity;
        float r_right = r_left + (right1 - (left1 + 1)) * percent - 1;
        canvas.drawRect(r_left + 2,
                r_top,
                r_right,
                r_bottom, setPaintColor(footPaint, 1));
        canvas.drawBitmap(mBitmap, r_left,
                top1 + 7 * readStatus.screenScaledDensity, setPaintColorChapter(footPaint, 1));
    }

    public void getRect() {
        mBitmap = BitmapFactory.decodeResource(resources, R.drawable.daymode_marks_power);
        mBitmapWidth = mBitmap.getWidth();
        mBitmapHeight = mBitmap.getHeight();
        for (int i = 0; i < mBitmapWidth; i++) {
            int color = mBitmap.getPixel(i, mBitmapHeight / 2);
            if (color == 0) {
                this.left1 = i;
                break;
            }
        }
        for (int i = mBitmapWidth - 1; i >= 0; i--) {
            int color = mBitmap.getPixel(i, mBitmapHeight / 2);
            if (color == 0) {
                this.right1 = i;
                break;
            }
        }
        for (int i = 0; i < mBitmapHeight; i++) {
            int color = mBitmap.getPixel(mBitmapWidth / 2, i);
            if (color == 0) {
                this.top1 = i;
                break;
            }
        }
        for (int i = mBitmapHeight - 1; i >= 0; i--) {
            int color = mBitmap.getPixel(mBitmapWidth / 2, i);
            if (color == 0) {
                this.bottom1 = i;
                break;
            }
        }
    }

    /*
     * 封面页 效果
     */
    private int drawHomePage(Canvas canvas) {

        int title_height = readStatus.screenHeight / 3;
        textPaint.setTextSize(Constants.FONT_CHAPTER_SIZE * readStatus.screenScaledDensity);
        FontMetrics fm = textPaint.getFontMetrics();
        float y = fm.descent - fm.ascent + title_height;
        float d_line = fm.descent - fm.ascent;
        ArrayList<NovelLineBean> nameList = readStatus.bookNameList;

        float paddingBottom;
        float bookNamePaddingY;
        float sloganPaddingY;

        if (Constants.isSlideUp) {
            paddingBottom = readStatus.screenHeight - 10;
            bookNamePaddingY = y + d_line;
            sloganPaddingY = 15 * readStatus.screenScaledDensity;
        } else {
            paddingBottom = readStatus.screenHeight - 25 * readStatus.screenScaledDensity;
            bookNamePaddingY = readStatus.screenHeight / 2 - d_line;
            sloganPaddingY = 40 * readStatus.screenScaledDensity;
        }

        if (nameList != null && !nameList.isEmpty()) {
        } else {
            return 0;
        }
        int name_length = nameList.size();
        name_length = (name_length > 4) ? 4 : name_length;
        int x_with = 0;

        //封面页居中
        textPaint.setTextAlign(Paint.Align.CENTER);
        if (textPaint.getTextAlign() == Paint.Align.LEFT) {
        } else if (textPaint.getTextAlign() == Paint.Align.CENTER) {
            x_with = readStatus.screenWidth / 2;
        }

        // 顶部slogan
        textPaint.setTextSize(11 * readStatus.screenScaledDensity);
        textPaint.setColor(Color.parseColor("#80000000"));
        textPaint.setTextAlign(Paint.Align.LEFT);
        drawSpacingText(canvas, resources.getString(R.string.slogan), 230, 11, sloganPaddingY);

        // 书籍名称
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(Constants.FONT_CHAPTER_SIZE * readStatus.screenScaledDensity);
        float bookNameHeight = textPaint.getFontMetrics().descent - textPaint.getFontMetrics().ascent;
        textPaint.setColor(Color.parseColor("#E6000000"));
        for (int i = 0; i < name_length; i++) {
            if (i > 0) bookNamePaddingY += bookNameHeight + 10 * readStatus.screenScaledDensity;
            canvas.drawText(nameList.get(i).getLineContent(), x_with, bookNamePaddingY, setPaintColor(textPaint, 1));
        }

        // 作者
        float authHeight = bookNamePaddingY + bookNameHeight + 10 * readStatus.screenScaledDensity;
        textPaint.setTextSize(14 * readStatus.screenScaledDensity);
        textPaint.setColor(Color.parseColor("#8C000000"));
        if (!TextUtils.isEmpty(readStatus.bookAuthor)) {
            canvas.drawText(readStatus.bookAuthor, x_with, authHeight, setPaintColor(textPaint, 1));
        }

        //底部icon及名称
        textPaint.setTextSize(12 * readStatus.screenScaledDensity);
        textPaint.setColor(Color.parseColor("#80000000"));
        textPaint.setTextAlign(Paint.Align.LEFT);
        drawSpacingText(canvas, resources.getString(R.string.app_name), 90, 11, paddingBottom);
        textPaint.setTextAlign(Paint.Align.CENTER);
        paddingBottom -= textPaint.getFontMetrics().descent - textPaint.getFontMetrics().ascent;

        if (mIconBitmap == null || mIconBitmap.isRecycled()) {
            mIconBitmap = BitmapFactory.decodeResource(resources, R.drawable.icon_home_page);
        }
        // 计算左边位置
        int left = readStatus.screenWidth / 2 - mIconBitmap.getWidth() / 2;
        // 计算上边位置
        int top = (int) (paddingBottom - mIconBitmap.getHeight() - 5 * readStatus.screenScaledDensity);
        canvas.drawBitmap(mIconBitmap, new Rect(0, 0, mIconBitmap.getWidth(), mIconBitmap.getHeight()),
                new Rect(left, top, left + mIconBitmap.getWidth(), top + mIconBitmap.getHeight()),
                new Paint());

        //默认情况
        textPaint.setTextAlign(Paint.Align.LEFT);

        return readStatus.screenHeight;
    }

    // 绘制带间距文本
    private void drawSpacingText(Canvas canvas, String text, int spacing, float textSize, float y) {
        if (TextUtils.isEmpty(text)) return;
        float textWidth = textPaint.measureText(String.valueOf(text.charAt(0)));
        float textTotalWidth = textWidth * text.length() + DisplayUtils.px2dp(resources, spacing) * (text.length() - 1);
        float drawTextStart = (readStatus.screenWidth - textTotalWidth) / 2;
        float drawTextX = 0;
        for (int i = 0; i < text.length(); i++) {
            drawTextX += (i == 0 ? drawTextStart : textWidth + DisplayUtils.px2dp(resources, spacing));
            canvas.drawText(String.valueOf(text.charAt(i)), drawTextX, y, setPaintColor(textPaint, 1));
        }
    }

    /**
     * 画背景图
     * <p/>
     * canvas
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void drawBackground(Canvas canvas) {
        if (Constants.MODE == 51) {// 柔和
            if (mBackground == null || mBackground.isRecycled()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                mBackground = BitmapFactory.decodeResource(resources, R.drawable.read_page_bg_default, options);
                int byteCount = mBackground.getByteCount();
                System.out.println("background : " + byteCount);
            }
            if (mBackground != null && !mBackground.isRecycled()) {
                canvas.drawBitmap(mBackground, new Rect(0, 0, mBackground.getWidth(), mBackground.getHeight()), new Rect(0, 0, readStatus.screenWidth, readStatus.screenHeight), null);
            } else {
                backgroundPaint.setColor(resources.getColor(R.color.transparent));
                canvas.drawRect(0, 0, readStatus.screenWidth, readStatus.screenHeight, backgroundPaint);
            }
        } else {
            // 通过新的画布，将矩形画新的bitmap上去
            canvas.drawRect(0, 0, readStatus.screenWidth, readStatus.screenHeight, setPaintColor(backgroundPaint, 0));
        }
    }

    private void drawHeadBackgroundNew(Canvas canvas) {
        int mTop = 0;
        int tempFootHeight;
        tempFootHeight = footHeight;

        if (Constants.MODE == 59) {
            if (mKraftBackground == null || mKraftBackground.isRecycled()) {
                Bitmap tempBitmap = BitmapFactory.decodeResource(resources, R.drawable.read_bg_kraft_top);
                mKraftBackground = resizeBitmap(tempBitmap, readStatus.screenWidth, tempFootHeight);
                tempBitmap.recycle();
            }
            if (mKraftBackground == null && !mKraftBackground.isRecycled()) {
                backgroundPaint.setColor(resources.getColor(R.color.reading_backdrop_first));
                canvas.drawRect(0, mTop, readStatus.screenWidth, tempFootHeight, backgroundPaint);
            } else {
                canvas.drawBitmap(mKraftBackground, 0, mTop, mPaint);
            }
        } else if (Constants.MODE == 60) {
            if (mSoftBackground == null || mSoftBackground.isRecycled()) {
                Bitmap tempBitmap = BitmapFactory.decodeResource(resources, R.drawable.read_mode_soft_bg);
                mSoftBackground = Bitmap.createBitmap(tempBitmap, 0, 0, tempBitmap.getWidth(), tempFootHeight);
                tempBitmap.recycle();
            }
            if (mSoftBackground != null && !mSoftBackground.isRecycled()) {

                int left = 0, top = 0;
                int srcBgWidth = mSoftBackground.getWidth();
                while (left < readStatus.screenWidth) {
                    if (left + srcBgWidth > readStatus.screenWidth) {
                        left = readStatus.screenWidth - srcBgWidth;
                    }
                    canvas.drawBitmap(mSoftBackground, left, mTop, backgroundPaint);
                    left += srcBgWidth;
                }
            } else {
                canvas.drawBitmap(mSoftBackground, 0, mTop, mPaint);
            }
        } else {
            // 通过新的画布，将矩形画新的bitmap上去
            canvas.drawRect(0, mTop, readStatus.screenWidth, footHeight, setPaintColor(backgroundPaint, 0));
        }


    }

    private void drawHeadBackground(Canvas canvas) {
        // 通过新的画布，将矩形画新的bitmap上去\
        backgroundPaint.setColor(Color.TRANSPARENT);
        backgroundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawRect(0, 0, readStatus.screenWidth, footHeight, backgroundPaint);
        backgroundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
    }

    private void drawFootBackground(Canvas canvas) {
        int mTop;
        mTop = readStatus.screenHeight - footHeight;
        // 通过新的画布，将矩形画新的bitmap上去
        backgroundPaint.setColor(Color.TRANSPARENT);
        backgroundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawRect(0, mTop, readStatus.screenWidth, readStatus.screenHeight, backgroundPaint);
    }

    public synchronized void clear() {
        if (mBackground != null && !mBackground.isRecycled()) {
            mBackground.recycle();
            mBackground = null;
        }
        if (mKraftBackground != null && !mKraftBackground.isRecycled()) {
            mKraftBackground.recycle();
            mKraftBackground = null;
        }
        if (mSoftBackground != null && !mSoftBackground.isRecycled()) {
            mSoftBackground.recycle();
            mSoftBackground = null;
        }
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    private String getChangeWord(int len) {
        StringBuilder stringBuffer = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            stringBuffer.append('*');
        }
        return stringBuffer.toString();
    }
}
