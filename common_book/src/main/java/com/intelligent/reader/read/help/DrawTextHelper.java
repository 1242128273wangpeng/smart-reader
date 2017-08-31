package com.intelligent.reader.read.help;

import com.dingyueads.sdk.Native.YQNativeAdInfo;
import com.dingyueads.sdk.NativeInit;
import com.intelligent.reader.app.BookApplication;
import com.intelligent.reader.read.page.PageInterface;

import net.lzbook.kit.R;
import net.lzbook.kit.ad.OwnNativeAdManager;
import net.lzbook.kit.constants.Constants;
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

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        mPaint.setColor(textColor);
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
        headPaint.setColor(res.getColor(R.color.reading_title_day));
        headPaint.setTextSize(12 * readStatus.screenScaledDensity);

        footPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        footPaint.setStyle(Paint.Style.FILL);
        footPaint.setAntiAlias(true);
        footPaint.setDither(true);
        footPaint.setColor(res.getColor(R.color.reading_title_day));
        footPaint.setTextSize(12 * readStatus.screenScaledDensity);

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

    /**
     * paint
     * type  0: 画背景
     */
    public Paint setPaintColor(Paint paint, int type) {

        int color_int = R.color.reading_backdrop_first;
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
        }else if (Constants.MODE == 61) {
            if (type == 0) {
                color_int = R.color.reading_backdrop_night;
            } else {
                color_int = R.color.reading_text_color_night;
            }
        }else{
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
    public synchronized void drawText(Canvas canvas, List<String> pageLines, ArrayList<String> chapterNameList) {
        mPaint.setTextSize(Constants.FONT_SIZE * readStatus.screenScaledDensity);
        duanPaint.setTextSize(1 * readStatus.screenScaledDensity);
        FontMetrics fm = mPaint.getFontMetrics();

        float m_iFontHeight = fm.descent - fm.ascent + Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE
                * readStatus.screenScaledDensity;
        float m_duan = (Constants.READ_PARAGRAPH_SPACE - Constants.READ_INTERLINEAR_SPACE) * Constants.FONT_SIZE
                * readStatus.screenScaledDensity;
        float sHeight = Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE * readStatus.screenScaledDensity;
        float height = 0;
        float y = 0;
        if (Constants.isSlideUp) {
            y = m_iFontHeight;
            height = readStatus.screenHeight - 52 * readStatus.screenScaledDensity;
        } else {
            y = fm.descent - fm.ascent + Constants.READ_CONTENT_PAGE_TOP_SPACE * readStatus.screenScaledDensity;

            height = readStatus.screenHeight - readStatus.screenDensity
                    * Constants.READ_CONTENT_PAGE_TOP_SPACE * 2 + sHeight;
        }


        float textHeight = 0;
        float duan = 0;
        if (pageLines != null) {
            int size = pageLines.size();
            for (int i = 0; i < size; i++) {
                String text = pageLines.get(i);
                if (!TextUtils.isEmpty(text) && text.equals(" ")) {
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
        drawBackground(canvas);

        if (pageLines != null && !pageLines.isEmpty()) {

            if (pageLines.get(0).startsWith("txtzsydsq_homepage")) {// 封面页
                drawHomePage(canvas);
            } else if (pageLines.get(0).startsWith("chapter_homepage")) {// 章节首页
                drawChapterPage(canvas, pageLines, chapterNameList);
            } else {

                for (int i = 0; i < pageLines.size(); i++) {
                    String text = pageLines.get(i);
                    if (Constants.isShielding && !noReadSensitive) {
                        for (String word : readSensitiveWords) {
                            text = text.replace(word, getChangeWord(word.length()));
                        }
                    }
                    if (!TextUtils.isEmpty(text) && text.equals(" ")) {
                        canvas.drawText(text, Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity,
                                y + m_duan * i, duanPaint);
                        y -= m_iFontHeight - m_duan;
                    } else {
                        canvas.drawText(text, Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity,
                                y + m_iFontHeight * i, mPaint);
                    }

                }
            }
        }
        if (!Constants.isSlideUp) {
            drawFoot(canvas);
        }
    }

    public synchronized void drawText(Canvas canvas, List<String> pageLines, Activity activity) {
        boolean isChapterFirstPage = false;
        readStatus.y_nativead = 0;
        readStatus.native_type = 0;
        mPaint.setTextSize(Constants.FONT_SIZE * readStatus.screenScaledDensity);
        duanPaint.setTextSize(1 * readStatus.screenScaledDensity);
        FontMetrics fm = mPaint.getFontMetrics();

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
                String text = pageLines.get(i);
                if (!TextUtils.isEmpty(text) && text.equals(" ")) {
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
        drawBackground(canvas);


        if (pageLines != null && !pageLines.isEmpty()) {
            if (pageLines.get(0).startsWith("txtzsydsq_homepage")) {// 封面页
                drawHomePage(canvas);
            } else if (pageLines.get(0).startsWith("chapter_homepage")) {// 章节首页
                drawChapterPage(canvas, pageLines);
                isChapterFirstPage = true;
            } else {
                isChapterFirstPage = false;
                for (int i = 0; i < pageLines.size(); i++) {
                    String text = pageLines.get(i);
                    if (Constants.isShielding && !noReadSensitive) {
                        for (String word : readSensitiveWords) {
                            text = text.replace(word, getChangeWord(word.length()));
                        }
                    }
                    if (!TextUtils.isEmpty(text) && text.equals(" ")) {
                        canvas.drawText(text, Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity,
                                y + m_duan * i, duanPaint);
                        total_y = y + m_iFontHeight * i;
                        y -= m_iFontHeight - m_duan;
                    } else if (text.contains(NovelHelper.empty_page_ad) || text.startsWith(NovelHelper.empty_page_ad)) {
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
                    } else if (text.contains(NovelHelper.empty_page_ad_inChapter) || text.startsWith(NovelHelper.empty_page_ad_inChapter)) {
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
                        canvas.drawText(text, Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity,
                                y + m_iFontHeight * i, mPaint);
                    }

                }
            }
        }
        if (!Constants.isSlideUp) {
            drawFoot(canvas);
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
    private void drawChapterPage(Canvas canvas, List<String> pageLines, ArrayList<String> chapterNameList) {
        mPaint.setTextSize(Constants.FONT_SIZE * readStatus.screenScaledDensity);
        duanPaint.setTextSize(1 * readStatus.screenScaledDensity);
        textPaint.setTextSize(Constants.FONT_CHAPTER_SIZE * readStatus.screenScaledDensity);
        FontMetrics fm_chapter = textPaint.getFontMetrics();
        float m_iFontHeight_chapter = fm_chapter.descent - fm_chapter.ascent + 0.5f * Constants.FONT_CHAPTER_SIZE
                * readStatus.screenScaledDensity;

        FontMetrics fm = mPaint.getFontMetrics();

        float m_iFontHeight = fm.descent - fm.ascent + Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE
                * readStatus.screenScaledDensity;

        float m_duan = (Constants.READ_PARAGRAPH_SPACE - Constants.READ_INTERLINEAR_SPACE) * Constants.FONT_SIZE
                * readStatus.screenScaledDensity;
        float y = 0;
        float y_chapter = 0;
        if (Constants.isSlideUp) {
            y_chapter = fm_chapter.descent - fm_chapter.ascent;
            y = fm.descent - fm.ascent - 5 * readStatus.screenScaledDensity;
        } else {
            y_chapter = fm_chapter.descent - fm_chapter.ascent + Constants.READ_CONTENT_PAGE_TOP_SPACE * readStatus.screenScaledDensity;
            y = fm.descent - fm.ascent + Constants.READ_CONTENT_PAGE_TOP_SPACE * readStatus.screenScaledDensity;
        }

        int size_c = -1;
        if (chapterNameList != null && !chapterNameList.isEmpty()) {
            size_c = chapterNameList.size();
            for (int i = 0; i < size_c; i++) {
                if (i == 0) {
                    if (!TextUtils.isEmpty(chapterNameList.get(0))) {
                        String chapterNameRemain = chapterNameList.get(0);

                        textPaint.setColor(textColor);
                        textPaint.setTextSize(Constants.FONT_CHAPTER_DEFAULT * readStatus.screenScaledDensity);
                        canvas.drawText(chapterNameRemain, Constants.READ_CONTENT_PAGE_LEFT_SPACE
                                        * readStatus.screenScaledDensity,
                                y_chapter + unit + m_iFontHeight * i, textPaint);
                    }
                } else {
                    fm_chapter = textPaint.getFontMetrics();
                    m_iFontHeight_chapter = fm_chapter.descent - fm_chapter.ascent + 0.5f
                            * Constants.FONT_CHAPTER_DEFAULT * readStatus.screenScaledDensity;
                    canvas.drawText(chapterNameList.get(i), Constants.READ_CONTENT_PAGE_LEFT_SPACE
                            * readStatus.screenScaledDensity, y_chapter + m_iFontHeight_chapter * i, textPaint);
                }
            }
            float line_2 = y_chapter + 15 * readStatus.screenScaledDensity + m_iFontHeight_chapter * (size_c - 1);
            canvas.drawLine(Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity, line_2,
                    readStatus.screenWidth - Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity,
                    line_2, mPaint);
            float font_height = fm.descent - fm.ascent;
            y = y + 50 * readStatus.screenScaledDensity + font_height;

            float sHeight = Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE * readStatus.screenScaledDensity;
            float textHeight = 0;
            float duan = 0;
            if (pageLines != null) {
                int size = pageLines.size();
                for (int i = 0; i < size; i++) {
                    String text = pageLines.get(i);
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

            for (int i = 0, j = pageLines.size(); i < j; i++) {
                if (i > size_c) {
                    String text = pageLines.get(i);
                    if (Constants.isShielding && !noReadSensitive) {
                        for (String word : readSensitiveWords) {
                            text = text.replace(word, getChangeWord(word.length()));
                        }
                    }
                    if (!TextUtils.isEmpty(text)) {
                        if (text.equals(" ")) {
                            canvas.drawText(text, Constants.READ_CONTENT_PAGE_LEFT_SPACE
                                    * readStatus.screenScaledDensity, y + m_duan * i, duanPaint);
                            y -= m_iFontHeight - m_duan;
                        } else if (text.equals("chapter_homepage  ")) {

                        } else {
                            canvas.drawText(text, Constants.READ_CONTENT_PAGE_LEFT_SPACE
                                    * readStatus.screenScaledDensity, y + m_iFontHeight * (i - 3), mPaint);

                        }
                    }

                }
            }
        }
    }

    private float firstchapterHeight;

    /*
     * 章节首页提示效果
     */
    private void drawChapterPage(Canvas canvas, List<String> pageLines) {
        readStatus.native_type = 0;
        mPaint.setTextSize(Constants.FONT_SIZE * readStatus.screenScaledDensity);
        duanPaint.setTextSize(1 * readStatus.screenScaledDensity);
        textPaint.setTextSize(Constants.FONT_CHAPTER_SIZE * readStatus.screenScaledDensity);
        FontMetrics fm_chapter = textPaint.getFontMetrics();
        float m_iFontHeight_chapter = fm_chapter.descent - fm_chapter.ascent + 0.5f * Constants.FONT_CHAPTER_SIZE
                * readStatus.screenScaledDensity;

        FontMetrics fm = mPaint.getFontMetrics();

        float m_iFontHeight = fm.descent - fm.ascent + Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE
                * readStatus.screenScaledDensity;

        float m_duan = (Constants.READ_PARAGRAPH_SPACE - Constants.READ_INTERLINEAR_SPACE) * Constants.FONT_SIZE
                * readStatus.screenScaledDensity;
        float y = 0;
        float y_chapter = 0;
        if (Constants.isSlideUp) {
            y_chapter = fm_chapter.descent - fm_chapter.ascent;
            y = fm.descent - fm.ascent - 5 * readStatus.screenScaledDensity;
        } else {
            y_chapter = fm_chapter.descent - fm_chapter.ascent + Constants.READ_CONTENT_PAGE_TOP_SPACE * readStatus.screenScaledDensity;
            y = fm.descent - fm.ascent + Constants.READ_CONTENT_PAGE_TOP_SPACE * readStatus.screenScaledDensity;
        }

        int size_c = -1;
        if (readStatus.chapterNameList != null && !readStatus.chapterNameList.isEmpty()) {
            size_c = readStatus.chapterNameList.size();
            for (int i = 0; i < size_c; i++) {
                if (i == 0) {
                    AppLog.e(TAG, "DrawChapterPage: " + readStatus.chapterName);
                    if (!TextUtils.isEmpty(readStatus.chapterName)) {
                        String chapterNameRemain = readStatus.chapterNameList.get(0);

                        textPaint.setColor(textColor);
                        textPaint.setTextSize(Constants.FONT_CHAPTER_DEFAULT * readStatus.screenScaledDensity);
                        canvas.drawText(chapterNameRemain, Constants.READ_CONTENT_PAGE_LEFT_SPACE
                                        * readStatus.screenScaledDensity,
                                y_chapter + unit + m_iFontHeight * i, textPaint);
                    }
                } else {
                    fm_chapter = textPaint.getFontMetrics();
                    m_iFontHeight_chapter = fm_chapter.descent - fm_chapter.ascent + 0.5f
                            * Constants.FONT_CHAPTER_DEFAULT * readStatus.screenScaledDensity;
                    canvas.drawText(readStatus.chapterNameList.get(i), Constants.READ_CONTENT_PAGE_LEFT_SPACE
                            * readStatus.screenScaledDensity, y_chapter + m_iFontHeight_chapter * i, textPaint);
                }
            }
            float line_2 = y_chapter + 15 * readStatus.screenScaledDensity + m_iFontHeight_chapter * (size_c - 1);
            mPaint.setStrokeWidth(1 * readStatus.screenScaledDensity);
            canvas.drawLine(Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity, line_2,
                    readStatus.screenWidth - Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity,
                    line_2, mPaint);
            mPaint.setStrokeWidth(0.0f);
            float font_height = fm.descent - fm.ascent;
            y = y + 50 * readStatus.screenScaledDensity + font_height;
            float sHeight = Constants.READ_INTERLINEAR_SPACE * Constants.FONT_SIZE * readStatus.screenScaledDensity;
            float textHeight = 0;
            float duan = 0;
            if (pageLines != null) {
                int size = pageLines.size();
                for (int i = 0; i < size; i++) {
                    String text = pageLines.get(i);
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
            for (int i = 0, j = pageLines.size(); i < j; i++) {
                if (i > size_c) {
                    String text = pageLines.get(i);
                    if (!TextUtils.isEmpty(text)) {
                        if (text.equals(" ")) {
                            canvas.drawText(text, Constants.READ_CONTENT_PAGE_LEFT_SPACE
                                    * readStatus.screenScaledDensity, y + m_duan * i, duanPaint);
                            y -= m_iFontHeight - m_duan;
                        } else if (text.equals("chapter_homepage  ")) {
                            AppLog.e(TAG, "chapter_homepage3:" + text);
                        } else {
                            firstchapterHeight = y + m_iFontHeight * (i - 3);
                            canvas.drawText(text, Constants.READ_CONTENT_PAGE_LEFT_SPACE
                                    * readStatus.screenScaledDensity, firstchapterHeight, mPaint);
                        }
                    }

                }
            }

        }
    }

    public void drawFoot(Canvas canvas) {
        if (readStatus.sequence != -1 && !Constants.isSlideUp) {
            drawHeadBackground(canvas);
            drawFootBackground(canvas);
            drawChapterNum(canvas);
            drawBattery(canvas);
            drawTime(canvas);
            drawPageNum(canvas);
        }
    }

    public void drawHeadNew(Canvas canvas) {
        if (!readStatus.isCanDrawFootView && !Constants.isSlideUp) {
            return;
        }
        drawHeadBackgroundNew(canvas);
        drawChapterNumNew(canvas);
        drawBatteryNew(canvas);
        drawTimeNew(canvas);
        drawPageNumNew(canvas);
    }

    private void drawChapterNum(Canvas canvas) {
        float position;
        position = readStatus.screenHeight - 5 * readStatus.screenScaledDensity;

        int chapter = readStatus.sequence + 1;

        float strwid = footPaint.measureText(chapter + "/" + readStatus.chapterCount + "章");

        canvas.drawText(chapter + "/" + readStatus.chapterCount + "章", readStatus.screenWidth / 2 - strwid / 2,
                position, setPaintColor(footPaint, 1));
        drawChapterName(canvas);
    }

    private void drawChapterName(Canvas canvas){
        if(readStatus.chapterName == null)
            return;
        float position;
        position = 15 * readStatus.screenScaledDensity;
        String name = readStatus.chapterName;
        if(readStatus.chapterName.length() > 17){
            name = name.substring(0, 16) + "...";
        }

        float strwid = footPaint.measureText(name);

        canvas.drawText(name, readStatus.screenWidth / 2 - strwid / 2,
                position, footPaint);
    }


    private void drawChapterNumNew(Canvas canvas) {
        int chapter = readStatus.sequence + 1;
        canvas.drawText(chapter + "/" + readStatus.chapterCount + "章", Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity,
                17 * readStatus.screenScaledDensity, setPaintColor(footPaint, 1));

    }

    private void drawPageNum(Canvas canvas) {
        float position;
        position = readStatus.screenHeight - 5 * readStatus.screenScaledDensity;
        String page_num = readStatus.currentPage + "/" + readStatus.pageCount + "页";
        float temp_width = footPaint.measureText(page_num);
        canvas.drawText(page_num, readStatus.screenWidth - Constants.READ_CONTENT_PAGE_LEFT_SPACE
                * readStatus.screenScaledDensity - temp_width, position, footPaint);
    }

    private void drawPageNumNew(Canvas canvas) {
        String page_num = readStatus.currentPage + "/" + readStatus.pageCount + "页";
        float temp_width = footPaint.measureText(page_num);
        canvas.drawText(page_num, readStatus.screenWidth / 2 - temp_width / 2, 17 * readStatus.screenScaledDensity, footPaint);
    }

    private void drawTime(Canvas canvas) {
        if (timeText == null || timeText.length() <= 0) {
            return;
        }
        float position;
        position = readStatus.screenHeight - 5 * readStatus.screenScaledDensity;
        canvas.drawText(timeText, Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity + right1 + 5
                        * readStatus.screenScaledDensity, position,
                footPaint);
    }

    private void drawTimeNew(Canvas canvas) {
        if (timeText == null || timeText.length() <= 0) {
            return;
        }
        float temp_width = footPaint.measureText(timeText);
        canvas.drawText(timeText, readStatus.screenWidth - Constants.READ_CONTENT_PAGE_LEFT_SPACE
                        * readStatus.screenScaledDensity - temp_width, 17 * readStatus.screenScaledDensity,
                footPaint);
    }

    private void drawBattery(Canvas canvas) {
        if (mBitmap == null || mBitmap.isRecycled()) {
            return;
        }
        float position;
        position = readStatus.screenHeight - (5 * readStatus.screenScaledDensity);
        float position2;
        position2 = readStatus.screenHeight - (bottom1 + 5 * readStatus.screenScaledDensity);

        float position3;
        position3 = readStatus.screenHeight - (bottom1 - top1 + 5 * readStatus.screenScaledDensity) + 1;
        canvas.drawRect(Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity + left1 + 2,
                position3,
                (right1 - (left1 + 1)) * percent
                        + (Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity + left1 - 1),
                position, footPaint);
        canvas.drawBitmap(mBitmap, Constants.READ_CONTENT_PAGE_LEFT_SPACE * readStatus.screenScaledDensity + left1,
                position2, footPaint);
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
                r_bottom, footPaint);
        canvas.drawBitmap(mBitmap, r_left,
                top1 + 7 * readStatus.screenScaledDensity, footPaint);
    }

    Bitmap mBitmap;
    int mBitmapWidth;
    int mBitmapHeight;
    int left1;
    int right1;
    int top1;
    int bottom1;

    Canvas canvas;

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

        int title_height = readStatus.screenHeight * 1 / 3;
        textPaint.setTextSize(Constants.FONT_CHAPTER_SIZE * readStatus.screenScaledDensity);
        FontMetrics fm = textPaint.getFontMetrics();
        float y = fm.descent - fm.ascent + title_height;
        float d_line = fm.descent - fm.ascent;
        ArrayList<String> nameList = readStatus.bookNameList;

        if (nameList != null && !nameList.isEmpty()) {
        } else {
            return;
        }
        textPaint.setColor(textColor);

        int name_length = nameList.size();
        name_length = (name_length > 4) ? 4 : name_length;
        int x_with = 0;
        //封面页居中
        textPaint.setTextAlign(Paint.Align.CENTER);
        if (textPaint.getTextAlign() == Paint.Align.LEFT) {
        } else if (textPaint.getTextAlign() == Paint.Align.CENTER) {
            x_with = readStatus.screenWidth / 2;
        } else if (textPaint.getTextAlign() == Paint.Align.RIGHT) {
        }
        for (int i = 0; i < name_length; i++) {
            if (i == 0) {
                canvas.drawText(nameList.get(i), x_with, y, textPaint);
            } else {
                canvas.drawText(nameList.get(i), x_with, y + d_line * i, textPaint);
            }
        }
        float lineHeight = y + d_line * (nameList.size() - 1) + 16 * readStatus.screenScaledDensity;
        float line_1 = 10 + 8 * readStatus.screenScaledDensity;
        textPaint.setTextSize(16 * readStatus.screenScaledDensity);

        if (!TextUtils.isEmpty(readStatus.bookAuthor)) {
            canvas.drawText(readStatus.bookAuthor, x_with, lineHeight + 2 * line_1, textPaint);
        }
        /*if (!TextUtils.isEmpty(readStatus.bookSource)) {
            canvas.drawText(readStatus.bookSource, x_with, readStatus.screenHeight - line_1, textPaint);
        }*/
        //默认情况
        textPaint.setTextAlign(Paint.Align.LEFT);
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
                backgroundPaint.setColor(resources.getColor(R.color.reading_backdrop_first));
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
        // 通过新的画布，将矩形画新的bitmap上去
        canvas.drawRect(0, 0, readStatus.screenWidth, footHeight, setPaintColor(backgroundPaint, 0));
    }

    private void drawFootBackground(Canvas canvas) {
        int mTop;
        mTop = readStatus.screenHeight - footHeight;
        // 通过新的画布，将矩形画新的bitmap上去
        canvas.drawRect(0, mTop, readStatus.screenWidth, readStatus.screenHeight, setPaintColor(backgroundPaint, 0));
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
