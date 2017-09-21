package com.intelligent.reader.read.help;

import com.dingyueads.sdk.Native.YQNativeAdInfo;
import com.dingyueads.sdk.NativeInit;
import com.intelligent.reader.R;
import com.intelligent.reader.app.BookApplication;
import com.intelligent.reader.read.page.PageInterface;
import com.intelligent.reader.util.DisplayUtils;

import net.lzbook.kit.ad.OwnNativeAdManager;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.NovelLineBean;
import net.lzbook.kit.data.bean.ReadStatus;
import net.lzbook.kit.data.bean.SensitiveWords;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.ResourceUtil;
import net.lzbook.kit.utils.StatisticManager;

import android.annotation.TargetApi;
import android.app.Activity;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DrawTextHelper {
    private static final String TAG = "DrawTextHelper";
    Bitmap mBitmap;
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
    private Bitmap mBackground; // 背景Bitmap
    private Bitmap mKraftBackground; // 牛皮纸模式 底部背景
    private Bitmap mSoftBackground;
    private Bitmap mIconBitmap;
    private int footHeight;
    private int slideupfootHeight;
    private int unit;
    private int textColor;
    private int translateColor;
    private float percent;
    private String timeText;
    private PageInterface pageView;
    private OwnNativeAdManager nativeAdManager;
    private Paint nightPaint;
    private StatisticManager statisticManager;
    private SensitiveWords readSensitiveWord;
    private List<String> readSensitiveWords;
    private boolean noReadSensitive = false;
    private float firstchapterHeight;
    private float mWidth;
    private float mLineStart;

    public DrawTextHelper(Resources res, PageInterface pageView, Activity mActivity) {
        this.resources = res;
        this.pageView = pageView;

        this.readSensitiveWord = SensitiveWords.getReadSensitiveWords();
        if (readSensitiveWord != null && readSensitiveWord.list.size() > 0) {
            readSensitiveWords = readSensitiveWord.getList();
            noReadSensitive = false;
        } else {
            noReadSensitive = true;
        }

        readStatus = BookApplication.getGlobalContext().getReadStatus();
        nativeAdManager = OwnNativeAdManager.getInstance(mActivity);
        nativeAdManager.setReadStatus(readStatus);

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

    //上下滑动
    public synchronized float drawText(Canvas canvas, List<NovelLineBean> pageLines, ArrayList<NovelLineBean> chapterNameList) {
        mPaint.setTextSize(Constants.FONT_SIZE * readStatus.screenScaledDensity);
        duanPaint.setTextSize(1 * readStatus.screenScaledDensity);
        FontMetrics fm = mPaint.getFontMetrics();
        mWidth = readStatus.screenWidth - readStatus.screenDensity * Constants.READ_CONTENT_PAGE_LEFT_SPACE * 2;
        mLineStart = Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity;

        float m_iFontHeight = fm.descent - fm.ascent + Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE * readStatus.screenScaledDensity;
        float m_duan = (Constants.READ_PARAGRAPH_SPACE - Constants.READ_INTERLINEAR_SPACE) * Constants.FONT_SIZE
                * readStatus.screenScaledDensity;
        float sHeight = Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE * readStatus.screenScaledDensity;
        float height;
        float y;
        float pageHeight = 0;


        y = -fm.top;
        height = readStatus.screenHeight;

        float textHeight = 0;
        float duan = 0;
        if (pageLines != null) {
            int size = pageLines.size();
            for (int i = 0; i < size; i++) {
                NovelLineBean text = pageLines.get(i);
                if (text != null && !TextUtils.isEmpty(text.getLineContent()) && text.getLineContent().equals(" ")) {
                    textHeight += m_duan;
                    duan += m_duan;
                } else {
                    textHeight += m_iFontHeight;
                }
            }
        }
        if (height - textHeight > 2 && height - textHeight < 4 * (fm.descent - fm.ascent)) {
            int n = (int) Math.round((height - duan) / m_iFontHeight);// 行数
            float distance = (height - textHeight) / n;
            m_iFontHeight = fm.descent - fm.ascent + Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE
                    * readStatus.screenScaledDensity + distance;
        } else if (textHeight - height > 2) {
            int n = (int) Math.round((height - duan) / m_iFontHeight);// 行数
            float distance = (textHeight - (height)) / n;
            m_iFontHeight = fm.descent - fm.ascent + Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE
                    * readStatus.screenScaledDensity - distance;
        }

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        if (pageLines != null && !pageLines.isEmpty()) {

            if (pageLines.get(0).getLineContent().startsWith("txtzsydsq_homepage")) {// 封面页
                drawHomePage(canvas);
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
                        canvas.drawText(text.getLineContent(), Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity,
                                y + m_duan * i, duanPaint);
                        y -= m_iFontHeight - m_duan;
                    } else {

                        if (text.getType() == 1) {
                            mPaint.setTextScaleX(1.0f);
                            mPaint.setTextScaleX(mWidth / mPaint.measureText(text.getLineContent()));
                        } else {
                            mPaint.setTextScaleX(1.0f);
                        }
                        canvas.drawText(text.getLineContent(), mLineStart, y + m_iFontHeight * i, mPaint);
                    }
                    if (i == pageLines.size() - 1) {
                        pageHeight = (y + m_iFontHeight * i) - Constants.READ_CONTENT_PAGE_TOP_SPACE;
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

    public synchronized Paint drawText(Canvas canvas, List<NovelLineBean> pageLines, Activity activity) {
        boolean isChapterFirstPage = false;
        readStatus.y_nativead = 0;
        readStatus.native_type = 0;
        mPaint.setTextSize(Constants.FONT_SIZE * readStatus.screenScaledDensity);
        duanPaint.setTextSize(1 * readStatus.screenScaledDensity);
        FontMetrics fm = mPaint.getFontMetrics();
        mLineStart = Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity;
        mWidth = readStatus.screenWidth - readStatus.screenDensity * Constants.READ_CONTENT_PAGE_LEFT_SPACE * 2;

        float m_iFontHeight = fm.descent - fm.ascent + Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE
                * readStatus.screenScaledDensity;
        float m_duan = (Constants.READ_PARAGRAPH_SPACE - Constants.READ_INTERLINEAR_SPACE) * Constants.FONT_SIZE
                * readStatus.screenScaledDensity;
        float sHeight = Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE * readStatus.screenScaledDensity;
        float height = 0;
        float y = 0;
        float total_y = 0;
        if (Constants.isSlideUp) {
            y = m_iFontHeight;
            height = readStatus.screenHeight - 52 * readStatus.screenScaledDensity;
        } else {
            y = fm.descent - fm.ascent + Constants.READ_CONTENT_PAGE_TOP_SPACE * readStatus.screenScaledDensity;

            height = readStatus.screenHeight - readStatus.screenDensity
                    * Constants.READ_CONTENT_PAGE_TOP_SPACE * 2 + sHeight;
        }


        float textHeight = 0;
        int distance_ad = 0;
        int distance_ad_middle = 0;
        int distance_ad_middle_down = 0;
        float duan = 0;
        boolean isShow_big_ad = false;
        if (pageLines != null) {
            int size = pageLines.size();
            for (int i = 0; i < size; i++) {
                String text = pageLines.get(i).getLineContent();
                if (!TextUtils.isEmpty(text) && text.equals(" ")) {
                    textHeight += m_duan;
                    duan += m_duan;
                } else {
                    textHeight += m_iFontHeight;
                }
            }

        }
        if (height - textHeight > 2 && height - textHeight < 4 * (fm.descent - fm.ascent)) {
            int n = Math.round((height - duan) / m_iFontHeight);// 行数
            float distance = (height - textHeight) / n;
            m_iFontHeight = fm.descent - fm.ascent + Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE
                    * readStatus.screenScaledDensity + distance;
        } else if (textHeight - height > 2) {
            int n = Math.round((height - duan) / m_iFontHeight);// 行数
            float distance = (textHeight - (height)) / n;
            m_iFontHeight = fm.descent - fm.ascent + Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE
                    * readStatus.screenScaledDensity - distance;
        }
        drawBackground(canvas);


        if (pageLines != null && !pageLines.isEmpty()) {
            if (pageLines.get(0).getLineContent().startsWith("txtzsydsq_homepage")) {// 封面页
                drawHomePage(canvas);
            } else if (pageLines.get(0).getLineContent().startsWith("chapter_homepage")) {// 章节首页
                drawChapterPage(canvas, pageLines);
                isChapterFirstPage = true;
            } else {
                isChapterFirstPage = false;
                for (int i = 0; i < pageLines.size(); i++) {
                    NovelLineBean text = pageLines.get(i);
                    if (Constants.isShielding && !noReadSensitive) {
                        for (String word : readSensitiveWords) {
                            text.setLineContent(text.getLineContent().replace(word, getChangeWord(word.length())));
                        }
                    }
                    if (text != null && !TextUtils.isEmpty(text.getLineContent()) && text.getLineContent().equals(" ")) {
                        canvas.drawText(text.getLineContent(), Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity,
                                y + m_duan * i, duanPaint);
                        total_y = y + m_iFontHeight * i;
                        y -= m_iFontHeight - m_duan;
                    } else if (text != null && text.getLineContent().contains(NovelHelper.empty_page_ad) || text.getLineContent().startsWith(NovelHelper.empty_page_ad)) {
                        isShow_big_ad = true;
                        //章节间大图绘制代码
                        if (readStatus.getAd_bitmap_big() != null && !readStatus.getAd_bitmap_big().isRecycled()) {
                            readStatus.native_type = 2;
                            readStatus.y_nativead_big = (readStatus.screenHeight - readStatus.height_nativead_big) / 2;
                            if ("night".equals(ResourceUtil.mode)) {
                                if (Constants.IS_LANDSCAPE) {
                                    canvas.drawBitmap(readStatus.getAd_bitmap_big(), (readStatus.screenWidth - readStatus.width_nativead_big) / 2, getHeight(readStatus.getAd_bitmap_big()), nightPaint);
                                } else {
                                    canvas.drawBitmap(readStatus.getAd_bitmap_big(), 0, getHeight(readStatus.getAd_bitmap_big()), nightPaint);
                                }
                                AppLog.e(TAG, "drawBitmap ad_bitmap_big nightPaint");
                            } else {
                                if (Constants.IS_LANDSCAPE) {
                                    canvas.drawBitmap(readStatus.getAd_bitmap_big(), (readStatus.screenWidth - readStatus.width_nativead_big) / 2, getHeight(readStatus.getAd_bitmap_big()), null);
                                } else {
                                    canvas.drawBitmap(readStatus.getAd_bitmap_big(), 0, getHeight(readStatus.getAd_bitmap_big()), null);
                                }
                                AppLog.e(TAG, "drawBitmap ad_bitmap_big");
                            }
                            attach(activity);
                        }
                        AppLog.e(TAG, "2_startsWith ad_page_tag" + " sequence:" + readStatus.sequence + " isShow_big_ad:" + isShow_big_ad);
                    } else if (text != null && text.getLineContent().contains(NovelHelper.empty_page_ad_inChapter) || text.getLineContent().startsWith(NovelHelper.empty_page_ad_inChapter)) {
                        isShow_big_ad = true;
                        //章节内大图绘制代码
                        if (readStatus.containerInChapter != null && readStatus.containerInChapter.size() > 0) {
                            for (int j = 1; j < readStatus.containerInChapter.size() + 1; j++) {
                                if (text.equals(NovelHelper.empty_page_ad_inChapter + j)) {
                                    HashMap<YQNativeAdInfo, Bitmap> hashMap = readStatus.containerInChapter.get(j - 1);
                                    Iterator<Map.Entry<YQNativeAdInfo, Bitmap>> iterator = hashMap.entrySet().iterator();
                                    if (iterator.hasNext()) {
                                        Map.Entry<YQNativeAdInfo, Bitmap> map = iterator.next();
                                        readStatus.currentAdInfo_in_chapter = map.getKey();
                                        readStatus.ad_bimap_big_inChapter = map.getValue();
                                    }
                                    break;
                                }
                            }
                        }

                        if (readStatus.ad_bimap_big_inChapter != null && !readStatus.ad_bimap_big_inChapter.isRecycled()) {
                            readStatus.native_type = 5;
                            readStatus.y_nativead_big = (readStatus.screenHeight - readStatus.height_nativead_big) / 2;
                            if ("night".equals(ResourceUtil.mode)) {
                                if (Constants.IS_LANDSCAPE) {
                                    canvas.drawBitmap(readStatus.ad_bimap_big_inChapter, (readStatus.screenWidth - readStatus.width_nativead_big) / 2, getHeight(readStatus.ad_bimap_big_inChapter), nightPaint);
                                } else {
                                    canvas.drawBitmap(readStatus.ad_bimap_big_inChapter, 0, getHeight(readStatus.ad_bimap_big_inChapter), nightPaint);
                                }
                                AppLog.e(TAG, "drawBitmap ad_bitmap_big nightPaint");
                            } else {
                                if (Constants.IS_LANDSCAPE) {
                                    canvas.drawBitmap(readStatus.ad_bimap_big_inChapter, (readStatus.screenWidth - readStatus.width_nativead_big) / 2, getHeight(readStatus.ad_bimap_big_inChapter), null);
                                } else {
                                    canvas.drawBitmap(readStatus.ad_bimap_big_inChapter, 0, getHeight(readStatus.ad_bimap_big_inChapter), null);
                                }
                                AppLog.e(TAG, "drawBitmap ad_bitmap_big");
                            }
                            attach(activity);

                        }
                        AppLog.e(TAG, "2_startsWith ad_page_tag" + " sequence:" + readStatus.sequence + " isShow_big_ad:" + isShow_big_ad);
                    } else {
                        total_y = y + m_iFontHeight * i;
                        if (text.getType() == 1) {
                            mPaint.setTextScaleX(1.0f);
                            mPaint.setTextScaleX(mWidth / mPaint.measureText(text.getLineContent()));
                        } else {
                            mPaint.setTextScaleX(1.0f);
                        }
                        canvas.drawText(text.getLineContent(), mLineStart, total_y, mPaint);
                    }

                }
            }
        }

        Paint mOperationPaint = null;

        if (!Constants.isSlideUp) {
            mOperationPaint = drawFoot(canvas);
        }

        textHeight += sHeight;
        total_y = isChapterFirstPage ? firstchapterHeight : total_y;
//        distance_ad = (int) ((int) total_y + readStatus.height_nativead + 40*readStatus.screenScaledDensity);
//        distance_ad_middle = distance_ad + readStatus.height_middle_nativead;

        distance_ad = (int) ((int) total_y + readStatus.height_nativead + 40 * readStatus.screenScaledDensity);
        distance_ad_middle = (int) ((int) total_y + readStatus.height_middle_nativead + 40 * readStatus.screenScaledDensity);
        distance_ad_middle_down = (int) ((int) total_y + 2 * readStatus.height_middle_nativead + 40 * readStatus.screenScaledDensity);
        if (readStatus.sequence != -1 && !isShow_big_ad) {

            //中图方案
            if (Constants.dy_page_end_ad_switch && Constants.readedCount % Constants.dy_page_end_ad_freq == 0) {
                if (readStatus.screenHeight > distance_ad_middle_down) {
                    //显示两张中图
                    total_y = total_y + 20 * readStatus.screenScaledDensity;

                    if (readStatus.getAd_bitmap_middle() != null && !readStatus.getAd_bitmap_middle().isRecycled()) {
                        readStatus.native_type = 20;//显示最上面那张中图
                        readStatus.y_nativead = total_y;
                        if ("night".equals(ResourceUtil.mode)) {
                            canvas.drawBitmap(readStatus.getAd_bitmap_middle(), (readStatus.screenWidth - readStatus.width_nativead_middle) / 2, total_y, nightPaint);
                            if (Constants.DEVELOPER_MODE)
                                AppLog.e(TAG, "drawBitmap ad_bitmap_middle nightPaint");
                        } else {
                            canvas.drawBitmap(readStatus.getAd_bitmap_middle(), (readStatus.screenWidth - readStatus.width_nativead_middle) / 2, total_y, null);

                            if (Constants.DEVELOPER_MODE)
                                AppLog.e(TAG, "drawBitmap ad_bitmap_middle");
                        }
                        total_y = total_y + readStatus.height_middle_nativead;
                    } else {
                        readStatus.native_type = 30;
                    }

                    if (readStatus.getAd_bitmap_middle_down() != null && !readStatus.getAd_bitmap_middle_down().isRecycled()) {

                        if (readStatus.native_type == 20) {
                            readStatus.native_type = 21;//显示两张中图
                        } else if (readStatus.native_type == 30) {
                            readStatus.native_type = 22;//只显示最下面那一张中图
//                            total_y = total_y + 20 * readStatus.screenScaledDensity;
                        }
                        readStatus.y_nativead = total_y;

                        if ("night".equals(ResourceUtil.mode)) {
                            canvas.drawBitmap(readStatus.getAd_bitmap_middle_down(), (readStatus.screenWidth - readStatus.width_nativead_middle) / 2, total_y, nightPaint);
                            if (Constants.DEVELOPER_MODE)
                                AppLog.e(TAG, "drawBitmap ad_bitmap_middle_down nightPaint");
                        } else {
                            canvas.drawBitmap(readStatus.getAd_bitmap_middle_down(), (readStatus.screenWidth - readStatus.width_nativead_middle) / 2, total_y, null);
                            if (Constants.DEVELOPER_MODE)
                                AppLog.e(TAG, "drawBitmap ad_bitmap_middle_down");
                        }

                    }
                } else if (readStatus.screenHeight > distance_ad_middle) {
                    total_y = total_y + 20 * readStatus.screenScaledDensity;


                    if (readStatus.getAd_bitmap_middle() != null && !readStatus.getAd_bitmap_middle().isRecycled()) {
                        readStatus.native_type = 23;//只显示一张中图的情况下，显示中图上
                        readStatus.y_nativead = total_y;
                        if ("night".equals(ResourceUtil.mode)) {
                            canvas.drawBitmap(readStatus.getAd_bitmap_middle(), (readStatus.screenWidth - readStatus.width_nativead_middle) / 2, total_y, nightPaint);


                        } else {
                            canvas.drawBitmap(readStatus.getAd_bitmap_middle(), (readStatus.screenWidth - readStatus.width_nativead_middle) / 2, total_y, null);


                        }

                    } else {
                        if (readStatus.getAd_bitmap_middle_down() != null && !readStatus.getAd_bitmap_middle_down().isRecycled()) {
                            readStatus.native_type = 24;//只显示一张中图的情况下，显示中图下
                            readStatus.y_nativead = total_y;
                            if ("night".equals(ResourceUtil.mode)) {
                                canvas.drawBitmap(readStatus.getAd_bitmap_middle_down(), (readStatus.screenWidth - readStatus.width_nativead_middle) / 2, total_y, nightPaint);
                            } else {
                                canvas.drawBitmap(readStatus.getAd_bitmap_middle_down(), (readStatus.screenWidth - readStatus.width_nativead_middle) / 2, total_y, null);
                            }
                        }
                    }
                } else if (readStatus.screenHeight > distance_ad) {
                    //只显示小图,用的是中图下对应的小图
                    if (readStatus.getAd_bitmap() != null && !readStatus.getAd_bitmap().isRecycled()) {
                        readStatus.native_type = 25;//只显示一张小图
                        readStatus.y_nativead = total_y;

                        if ("night".equals(ResourceUtil.mode)) {
                            canvas.drawBitmap(readStatus.getAd_bitmap(), (readStatus.screenWidth - readStatus.width_nativead_middle) / 2, total_y, nightPaint);
                        } else {
                            canvas.drawBitmap(readStatus.getAd_bitmap(), (readStatus.screenWidth - readStatus.width_nativead_middle) / 2, total_y, null);
                        }
                    }
                } else {
                    //剩余空间不够，什么都放不下
                }
            }

            attach(activity);
            if (Constants.DEVELOPER_MODE)
                AppLog.e(TAG, "ad total_y:" + total_y + " textHeight:" + textHeight + " readStatus.y_nativead:" + readStatus.y_nativead + " show ad" + "" +
                        " distance_ad2:" + distance_ad_middle + " height:" + readStatus.screenHeight + " native_type:" + readStatus.native_type + " height_nativead:" + readStatus.height_nativead);
        }

        return mOperationPaint;
    }

    private final int getHeight(Bitmap bitmap) {
        return (readStatus.screenHeight - bitmap.getHeight()) / 2;
    }

    private void attach(Activity activity) {
        if (readStatus == null) {
            return;
        }

        if (readStatus.novel_basePageView != null) {
            try {
                if (statisticManager == null) {
                    statisticManager = StatisticManager.getStatisticManager();
                }
                /*if (readStatus.currentAdInfo != null && (readStatus.native_type == 1 || readStatus.native_type == 3 || readStatus.native_type == 4)) {



                    statisticManager.schedulingRequest(activity, readStatus.novel_basePageView, readStatus.currentAdInfo, pageView.getCurrentNovel(), StatisticManager.TYPE_SHOW, NativeInit.ad_position[1]);
                } else */
                if (readStatus.currentAdInfo_image != null && readStatus.native_type == 2) {


                    statisticManager.schedulingRequest(activity, readStatus.novel_basePageView, readStatus.currentAdInfo_image, pageView.getCurrentNovel(), StatisticManager.TYPE_SHOW, NativeInit.ad_position[2]);
                } else if (readStatus.currentAdInfo_in_chapter != null && readStatus.native_type == 5) {

                    statisticManager.schedulingRequest(activity, readStatus.novel_basePageView, readStatus.currentAdInfo_in_chapter, pageView.getCurrentNovel(), StatisticManager.TYPE_SHOW, NativeInit.ad_position[7]);
                } else if (readStatus.currentAdInfo != null && (readStatus.native_type == 20 || readStatus.native_type == 23)) {

                    statisticManager.schedulingRequest(activity, readStatus.novel_basePageView, readStatus.currentAdInfo, pageView.getCurrentNovel(), StatisticManager.TYPE_SHOW, NativeInit.ad_position[1]);
                } else if (readStatus.currentAdInfoDown != null && (readStatus.native_type == 22 || readStatus.native_type == 24 || readStatus.native_type == 25)) {

                    statisticManager.schedulingRequest(activity, readStatus.novel_basePageView, readStatus.currentAdInfoDown, pageView.getCurrentNovel(), StatisticManager.TYPE_SHOW, NativeInit.ad_position[1]);
                } else if (readStatus.native_type == 21) {
                    if (readStatus.currentAdInfo != null) {
                        statisticManager.schedulingRequest(activity, readStatus.novel_basePageView, readStatus.currentAdInfo, pageView.getCurrentNovel(), StatisticManager.TYPE_SHOW, NativeInit.ad_position[1]);
                    }
                    if (readStatus.currentAdInfoDown != null) {
                        statisticManager.schedulingRequest(activity, readStatus.novel_basePageView, readStatus.currentAdInfoDown, pageView.getCurrentNovel(), StatisticManager.TYPE_SHOW, NativeInit.ad_position[1]);
                    }
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadNatvieAd() {
        if (nativeAdManager != null) {
//            if (nativeAdManager.getAdType()) {
//                nativeAdManager.loadAd(NativeInit.CustomPositionName.READING_MIDDLE_POSITION);
//            } else {
//                nativeAdManager.loadAd(NativeInit.CustomPositionName.READING_POSITION);
//            }
            nativeAdManager.loadAdForMiddle(NativeInit.CustomPositionName.READING_MIDDLE_POSITION);
            nativeAdManager.loadAd(NativeInit.CustomPositionName.READING_POSITION);
        }
    }

    //上下滑动模式
    private float drawChapterPage(Canvas canvas, List<NovelLineBean> pageLines, ArrayList<NovelLineBean> chapterNameList) {
        mPaint.setTextSize(Constants.FONT_SIZE * readStatus.screenScaledDensity);
        duanPaint.setTextSize(1 * readStatus.screenScaledDensity);
        textPaint.setTextSize(Constants.FONT_CHAPTER_SIZE * readStatus.screenScaledDensity);
        mWidth = readStatus.screenWidth - readStatus.screenDensity * Constants.READ_CONTENT_PAGE_LEFT_SPACE * 2;
        FontMetrics fm_chapter = textPaint.getFontMetrics();
        float m_iFontHeight_chapter = 0;

        FontMetrics fm = mPaint.getFontMetrics();

        float m_iFontHeight = fm.descent - fm.ascent + Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE
                * readStatus.screenScaledDensity;

        float m_duan = (Constants.READ_PARAGRAPH_SPACE - Constants.READ_INTERLINEAR_SPACE) * Constants.FONT_SIZE
                * readStatus.screenScaledDensity;

        // 正文行首与顶部间距
        float y = 0;
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
                    y = y_chapter + m_iFontHeight_chapter * i;
                }
            }

            float font_height = fm.descent - fm.ascent;
            float sHeight = Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE * readStatus.screenScaledDensity;
            float textHeight = 0;
            float duan = 0;
            if (pageLines != null) {
                int size = pageLines.size();
                for (int i = 0; i < size; i++) {
                    String text = pageLines.get(i).getLineContent();
                    if (!TextUtils.isEmpty(text) && text.equals(" ")) {
                        textHeight += m_duan;
                        duan += m_duan;
                    } else if (!text.equals("chapter_homepage  ")) {
                        textHeight += m_iFontHeight;
                    }
                }

            }
            float height = 0;
            height = readStatus.screenHeight;

            // 章节头与正文间距
            if (size_c > 1) {
                y += 75 * readStatus.screenScaledDensity;
                height -= y;
            }
            if (height - textHeight > 2 && height - textHeight < 120 * readStatus.screenScaledDensity) {
                int n = (int) Math.floor((height - duan) / m_iFontHeight);// 行数
                float distance = (height - textHeight) / n;
                m_iFontHeight = fm.descent - fm.ascent + Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE
                        * readStatus.screenScaledDensity + distance;
            } else if (textHeight - height > 2) {
                int n = (int) Math.floor((height - duan) / m_iFontHeight);// 行数
                float distance = (textHeight - height) / n;
                m_iFontHeight = fm.descent - fm.ascent + Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE
                        * readStatus.screenScaledDensity;
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
                            canvas.drawText(text.getLineContent(), Constants.READ_CONTENT_PAGE_LEFT_SPACE
                                    * readStatus.screenScaledDensity, y + m_duan * i, duanPaint);
                            y -= m_iFontHeight - m_duan;
                        } else if (text.getLineContent().equals("chapter_homepage  ")) {

                        } else {
                            if (text.getType() == 1) {
                                mPaint.setTextScaleX(1.0f);
                                mPaint.setTextScaleX(mWidth / mPaint.measureText(text.getLineContent()));
                            } else {
                                mPaint.setTextScaleX(1.0f);
                            }
                            canvas.drawText(text.getLineContent(), Constants.READ_CONTENT_PAGE_LEFT_SPACE
                                    * readStatus.screenScaledDensity, y + m_iFontHeight * (i - 3), mPaint);
                        }
                    }
                }

                if (i == pageLines.size() - 1) {
                    pageHeight = y + m_iFontHeight * (pageLines.size() - 3);
                }
            }
        }
        return pageHeight;
    }

    /*
     * 章节首页提示效果
     */
    private void drawChapterPage(Canvas canvas, List<NovelLineBean> pageLines) {
        readStatus.native_type = 0;
        mPaint.setTextSize(Constants.FONT_SIZE * readStatus.screenScaledDensity);
        duanPaint.setTextSize(1 * readStatus.screenScaledDensity);
        textPaint.setTextSize(Constants.FONT_CHAPTER_SIZE * readStatus.screenScaledDensity);
        mWidth = readStatus.screenWidth - readStatus.screenDensity * Constants.READ_CONTENT_PAGE_LEFT_SPACE * 2;
        FontMetrics fm_chapter = textPaint.getFontMetrics();
        float m_iFontHeight_chapter;

        FontMetrics fm = mPaint.getFontMetrics();

        float m_iFontHeight = fm.descent - fm.ascent + Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE
                * readStatus.screenScaledDensity;

        float m_duan = (Constants.READ_PARAGRAPH_SPACE - Constants.READ_INTERLINEAR_SPACE) * Constants.FONT_SIZE
                * readStatus.screenScaledDensity;
        float y;
        float y_chapter;
        y_chapter = 65 * readStatus.screenScaledDensity;
        y = fm.descent - fm.ascent + Constants.READ_CONTENT_PAGE_TOP_SPACE * readStatus.screenScaledDensity;

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

            mPaint.setStrokeWidth(0.0f);
            float font_height = fm.descent - fm.ascent;
            y = y + 90 * readStatus.screenScaledDensity;
            float sHeight = Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE * readStatus.screenScaledDensity;
            float textHeight = 0;
            float duan = 0;
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
                    } else if (!text.equals("chapter_homepage  ")) {
                        textHeight += m_iFontHeight;
                    }
                }

            }
            float height = 0;
            if (Constants.isSlideUp) {
                height = readStatus.screenHeight - 96 * readStatus.screenScaledDensity + sHeight - font_height;
            } else {
                height = readStatus.screenHeight - readStatus.screenDensity * Constants.READ_CONTENT_PAGE_TOP_SPACE
                        * 2 - 50 * readStatus.screenScaledDensity + sHeight - font_height;
            }

            if (size_c > 1) {
                y += 30 * readStatus.screenScaledDensity;
                height -= 30 * readStatus.screenScaledDensity;
            }
            if (height - textHeight > 2 && height - textHeight < 120 * readStatus.screenScaledDensity) {
                int n = (int) Math.floor((height - duan) / m_iFontHeight);// 行数
                float distance = (height - textHeight) / n;
                m_iFontHeight = fm.descent - fm.ascent + Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE
                        * readStatus.screenScaledDensity + distance;
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
                            canvas.drawText(text.getLineContent(), Constants.READ_CONTENT_PAGE_LEFT_SPACE
                                    * readStatus.screenScaledDensity, y + m_duan * i, duanPaint);
                            y -= m_iFontHeight - m_duan;
                        } else if (text.getLineContent().equals("chapter_homepage  ")) {
                            AppLog.e(TAG, "chapter_homepage3:" + text);
                        } else {
                            firstchapterHeight = y + m_iFontHeight * (i - 3);

                            if (text.getType() == 1) {
                                mPaint.setTextScaleX(1.0f);
                                mPaint.setTextScaleX(mWidth / mPaint.measureText(text.getLineContent()));
                            } else {
                                mPaint.setTextScaleX(1.0f);
                            }
                            canvas.drawText(text.getLineContent(), Constants.READ_CONTENT_PAGE_LEFT_SPACE
                                    * readStatus.screenScaledDensity, firstchapterHeight, mPaint);
                        }
                    }

                }
            }
        }
    }

    public Paint drawFoot(Canvas canvas) {
        if (readStatus.sequence != -1 && !Constants.isSlideUp) {
//            drawHeadBackground(canvas);
//            drawFootBackground(canvas);
            drawOrigin(canvas);
            drawTransCoding(canvas);
            drawChapterNum(canvas);
            drawBattery(canvas);
            drawTime(canvas);
            drawPageNum(canvas);
            return footPaint;
        }
        return null;
    }

    private void drawOrigin(Canvas canvas) {
        float y = 20 * readStatus.screenScaledDensity;
        float x = Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity;
        canvas.drawText(resources.getString(R.string.origin_text), x, y, setPaintColor(footPaint, 1));
    }

    private void drawTransCoding(Canvas canvas) {
        float y = 20 * readStatus.screenScaledDensity;
        float x = readStatus.screenWidth - Constants.READ_CONTENT_PAGE_LEFT_SPACE
                * readStatus.screenScaledDensity - footPaint.measureText(resources.getString(R.string.trans_coding_text));
        canvas.drawText(resources.getString(R.string.trans_coding_text), x, y, setPaintColor(footPaint, 1));
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
                position, setPaintColor(footPaint, 1));
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
                position, setPaintColor(footPaint, 1));
    }

    private void drawChapterNumNew(Canvas canvas) {
        int chapter = readStatus.sequence + 1;
        canvas.drawText(chapter + "/" + readStatus.chapterCount + "章", Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity,
                17 * readStatus.screenScaledDensity, setPaintColor(footPaint, 1));

    }

    private void drawPageNum(Canvas canvas) {
        float position;
        position = readStatus.screenHeight - 10 * readStatus.screenScaledDensity;
        String page_num = "本章第" + readStatus.currentPage + "/" + readStatus.pageCount;
        float temp_width = footPaint.measureText(page_num);
        canvas.drawText(page_num, readStatus.screenWidth - Constants.READ_CONTENT_PAGE_LEFT_SPACE
                * readStatus.screenScaledDensity - temp_width, position, setPaintColor(footPaint, 1));
    }

    private void drawPageNumNew(Canvas canvas) {
        String page_num = readStatus.currentPage + "/" + readStatus.pageCount + "页";
        float temp_width = footPaint.measureText(page_num);
        canvas.drawText(page_num, readStatus.screenWidth / 2 - temp_width / 2, 17 * readStatus.screenScaledDensity, setPaintColor(footPaint, 1));
    }

    private void drawTime(Canvas canvas) {
        if (timeText == null || timeText.length() <= 0) {
            return;
        }
        float position;
        position = readStatus.screenHeight - 10 * readStatus.screenScaledDensity;
        canvas.drawText(timeText, Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity + right1 + 5
                        * readStatus.screenScaledDensity, position,
                setPaintColor(footPaint, 1));
    }

    private void drawTimeNew(Canvas canvas) {
        if (timeText == null || timeText.length() <= 0) {
            return;
        }
        float temp_width = footPaint.measureText(timeText);
        canvas.drawText(timeText, readStatus.screenWidth - Constants.READ_CONTENT_PAGE_LEFT_SPACE
                        * readStatus.screenScaledDensity - temp_width, 17 * readStatus.screenScaledDensity,
                setPaintColor(footPaint, 1));
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
                position, setPaintColor(footPaint, 1));
        canvas.drawBitmap(mBitmap, Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity + left1,
                position2, setPaintColor(footPaint, 1));
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
                top1 + 7 * readStatus.screenScaledDensity, setPaintColor(footPaint, 1));
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
    private void drawHomePage(Canvas canvas) {

        int title_height = readStatus.screenHeight / 3;
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
            paddingBottom = readStatus.screenHeight - d_line;
            bookNamePaddingY = y + d_line * readStatus.screenScaledDensity;
            sloganPaddingY = 40 * readStatus.screenScaledDensity;
        }

        if (nameList != null && !nameList.isEmpty()) {
        } else {
            return;
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
        textPaint.setColor(Color.parseColor("#50000000"));
        drawSpacingText(canvas, resources.getString(R.string.slogan), 240, 13, sloganPaddingY);

        // 书籍名称
        textPaint.setTextSize(Constants.FONT_CHAPTER_SIZE * readStatus.screenScaledDensity);
        float bookNameHeight = textPaint.getFontMetrics().descent - textPaint.getFontMetrics().ascent;
        textPaint.setColor(Color.parseColor("#90000000"));
        for (int i = 0; i < name_length; i++) {
            if (i > 0) bookNamePaddingY += bookNameHeight + 10 * readStatus.screenScaledDensity;
            canvas.drawText(nameList.get(i).getLineContent(), x_with, bookNamePaddingY, setPaintColor(textPaint, 1));
        }

        // 作者
        float authHeight = bookNamePaddingY + bookNameHeight + 10 * readStatus.screenScaledDensity;
        textPaint.setTextSize(14 * readStatus.screenScaledDensity);
        textPaint.setColor(Color.parseColor("#56000000"));
        if (!TextUtils.isEmpty(readStatus.bookAuthor)) {
            canvas.drawText(readStatus.bookAuthor, x_with, authHeight, setPaintColor(textPaint, 1));
        }

        //底部icon及名称
        textPaint.setTextSize(12 * readStatus.screenScaledDensity);
        drawSpacingText(canvas, resources.getString(R.string.app_name), 100, 13, paddingBottom);
        paddingBottom -= d_line;

        if (mIconBitmap == null || mIconBitmap.isRecycled()) {
            mIconBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher);
        }
        // 计算左边位置
        int left = readStatus.screenWidth / 2 - mIconBitmap.getWidth() / 2;
        // 计算上边位置
        int top;
        if (Constants.isSlideUp) {
            top = (int) (paddingBottom - (mIconBitmap.getHeight()) / 1.5);
        } else {
            top = (int) (paddingBottom - mIconBitmap.getHeight());
        }
        canvas.drawBitmap(mIconBitmap, new Rect(0, 0, mIconBitmap.getWidth(), mIconBitmap.getHeight()),
                new Rect(left, top, left + mIconBitmap.getWidth(), top + mIconBitmap.getHeight()),
                new Paint());

        //默认情况
        textPaint.setTextAlign(Paint.Align.LEFT);
    }

    // 绘制带间距文本
    private void drawSpacingText(Canvas canvas, String text, int spacing, float textSize, float y) {
        if (TextUtils.isEmpty(text)) return;

        float textWidth = textSize * text.length() + text.length() * DisplayUtils.px2dp(resources, spacing);
        float drawTextStart = (readStatus.screenWidth - textWidth) / 2;

        for (int i = 0; i < text.length(); i++) {
            canvas.drawText(String.valueOf(text.charAt(i)), drawTextStart + DisplayUtils.px2dp(resources, spacing) * i * 1.5f, y, setPaintColor(textPaint, 1));
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
                mBackground = BitmapFactory.decodeResource(resources, com.intelligent.reader.R.drawable.read_page_bg_default);
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
