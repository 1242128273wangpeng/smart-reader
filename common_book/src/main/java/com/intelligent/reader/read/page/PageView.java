package com.intelligent.reader.read.page;

import com.dingyueads.sdk.Bean.AdSceneData;
import com.dingyueads.sdk.Bean.Novel;
import com.dingyueads.sdk.NativeInit;
import com.intelligent.reader.activity.ReadingActivity;
import com.intelligent.reader.read.animation.AnimationProvider;
import com.intelligent.reader.read.animation.BitmapManager;
import com.intelligent.reader.read.animation.CurlAnimationProvider;
import com.intelligent.reader.read.animation.ShiftAnimationProvider;
import com.intelligent.reader.read.animation.SlideAnimationProvider;
import com.intelligent.reader.read.help.CallBack;
import com.intelligent.reader.read.help.DrawTextHelper;
import com.intelligent.reader.read.help.IReadDataFactory;
import com.intelligent.reader.read.help.NovelHelper;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.ReadStatus;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.StatisticManager;
import net.lzbook.kit.utils.ToastUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;
import android.widget.Toast;

import java.util.List;

/**
 * 阅读页View
 */
public class PageView extends View implements PageInterface {
    private static final String TAG = "PageView";
    private Context mContext;
    private Activity mActivity;
    private CallBack callBack;
    private Scroller mScroller;
    private BitmapManager myBitmapManager;
    private Canvas mCurPageCanvas;
    private Canvas mNextPageCanvas;

    private static final int kMoveThresholdDP = 5;
    private static final int kTurnThresholdDP = 10;

    private int pageWidth, pageHeight;

    private int moveThreshold;
    private int turnThreshold;

    private AnimationProvider provider;
    private List<String> pageLines;

    private int backColor;// 仿真翻页背面颜色

    private ReadStatus readStatus;
    private NovelHelper novelHelper;
    private DrawTextHelper drawTextHelper;
    private IReadDataFactory dataFactory;
    private boolean isCurlType = false;
    private boolean isAutoMenuShowing = false;
    private float percent;
    private boolean isFirstCome = true;//打点统计用 判断每次退出阅读后是否重新进来

    private StatisticManager statisticManager;
    private int count;//用户首次进入后进行标识（打点用）  会执行两次的drawCurrentPage 和 drawNextPage（原因待查）

    public PageView(Context context) {
        super(context);
        this.mContext = context;
    }

    public PageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
    }

    public PageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public void setReadFactory(IReadDataFactory factory) {
        this.dataFactory = factory;
    }

    @SuppressLint("NewApi")
    public void init(Activity activity, ReadStatus readStatus, NovelHelper novelHelper) {
        this.mActivity = activity;
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "auto read");
        readStatus.startReadTime = System.currentTimeMillis();
        AppLog.e("jjj",readStatus.toString());
        this.readStatus = readStatus;
        this.novelHelper = novelHelper;

        mScroller = new Scroller(mContext);

        myBitmapManager = new BitmapManager(readStatus.screenWidth, readStatus.screenHeight);

        Bitmap mCurPageBitmap = myBitmapManager.getBitmap(0);
        Bitmap mNextPageBitmap = myBitmapManager.getBitmap(1);
        // 创建绘制当前页以及下一页的画布canvas
        mCurPageCanvas = new Canvas(mCurPageBitmap);
        mNextPageCanvas = new Canvas(mNextPageBitmap);
        drawTextHelper = new DrawTextHelper(getResources(), this, mActivity);

        moveThreshold = AppUtils.dip2px(mContext, kMoveThresholdDP);
        turnThreshold = AppUtils.dip2px(mContext, kTurnThresholdDP);

        pageWidth = readStatus.screenWidth;
        pageHeight = readStatus.screenHeight;

        drawTextHelper.getRect();
        drawTextHelper.drawText(mCurPageCanvas, pageLines, mActivity);


        postInvalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        pageWidth = readStatus.screenWidth = w;
        pageHeight = readStatus.screenHeight = h;
        if (provider != null) {
            provider.setWH(w, h);
        }
        if (callBack != null && (Math.abs(oldh - h) > AppUtils.dip2px(mContext, 26))) {
            getAnimationProvider();
            callBack.onResize();
            if (!isMoveing) {
                drawCurrentPage();
            } else {

            }
            drawNextPage();
        }
        setBackground();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (isCurlType) {
            ((CurlAnimationProvider) provider).dealComputerScorll();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        provider = getAnimationProvider();
        provider.drawInternal(canvas);

    }

    private String timeText;

    public void freshTime(CharSequence time) {
        if (time != null && time.length() > 0) {
            drawTextHelper.setTimeText(time.toString());
            timeText = time.toString();
        }
        if (!isAutoReadMode()) {
            if (mCurPageCanvas != null) {
                drawTextHelper.drawFoot(mCurPageCanvas);
            }
            if (mNextPageCanvas != null) {
                drawTextHelper.drawFoot(mNextPageCanvas);
            }
        }

        postInvalidate();
    }

    public void freshBattery(float percent) {
        this.percent = percent;
        if (drawTextHelper != null && mCurPageCanvas != null) {
            drawTextHelper.setPercent(percent, mCurPageCanvas);
        }
    }

    /**
     * 画下一页内容
     */
    public void drawNextPage() {
        nextPageContent = pageLines = novelHelper.getPageContent();
        drawTextHelper.drawText(mNextPageCanvas, pageLines, mActivity);
        if(count==1){
            readStatus.lastSequenceRemark = readStatus.sequence+1;
            readStatus.lastCurrentPageRemark = readStatus.currentPage;
            readStatus.lastPageCount = readStatus.pageCount;
            isFirstCome = false;
        }
        count++;
        postInvalidate();
    }

    private List<String> currentPageContent;
    private List<String> nextPageContent;
    private long endTime;

    /**
     * 画当前页内容
     */
    public void drawCurrentPage() {
        currentPageContent = pageLines = novelHelper.getPageContent();
        refreshCurrentPage();
    }

    public void setTextColor(int color) {
//        AppLog.e(TAG, "SetTextColor: " + color);
        drawTextHelper.setTextColor(color);
    }

    public void changeBatteryBg(int res) {
        drawTextHelper.changeBattyBitmp(res);
        System.gc();
        if (!isAutoReadMode()) {
            if (mCurPageCanvas != null) {
                drawTextHelper.drawFoot(mCurPageCanvas);
            }
            if (mNextPageCanvas != null) {
                drawTextHelper.drawFoot(mNextPageCanvas);
            }

        }

    }

    public void setBackground() {
        if (!isAutoReadMode()) {
            drawTextHelper.resetBackBitmap();
            drawTextHelper.drawText(mCurPageCanvas, pageLines, mActivity);
            drawTextHelper.drawText(mNextPageCanvas, pageLines, mActivity);

            invalidate();
        }

    }

    public void setPageBackColor(int color) {
        this.backColor = color;
        if (provider != null) {
            provider.backColor = color;
        }
    }

    public void showToast(String str) {
        Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show();
    }

    /*
     * 8 刷新当前页
     */
    public void refreshCurrentPage() {

        drawTextHelper.drawText(mCurPageCanvas, pageLines, mActivity);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (callBack != null && readStatus != null && readStatus.isMenuShow) {
            callBack.onShowMenu(false);
            return false;
        }
        if (null != autoReadImpl && autoReadImpl.onTouchEvent(e)) {
            return true;
        }
        return onTouchEventSlide(e);

    }

    private enum MotionState {
        kWaiting, kMoveToLeft, kMoveToRight, kNone,
    }

    private int touchStartX;
    private int touchStartY;
    private MotionState motionState = MotionState.kNone;
    private MotionState initMoveState = MotionState.kNone;
    private MotionState validMoveState = MotionState.kNone;
    private int currentMoveStateStartX;
    private int currentMoveStateLastX;
    private boolean isMoveing = false;

    private boolean onTouchEventSlide(MotionEvent event) {
        int tmpX = getStartPoint(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartX = tmpX;
                touchStartY = (int) event.getY();
                motionState = MotionState.kWaiting;
                initMoveState = MotionState.kNone;
                validMoveState = MotionState.kNone;
                provider = getAnimationProvider();
                return true;
            case MotionEvent.ACTION_CANCEL:
                isMoveing = false;
                motionState = MotionState.kNone;
                return true;
            case MotionEvent.ACTION_MOVE:
                isMoveing = true;
                return handleTouchEventMove(event);
            case MotionEvent.ACTION_UP:
                isMoveing = false;
                return handleTouchEventUp(event);
        }
        return super.onTouchEvent(event);
    }

    private int getStartPoint(MotionEvent event) {
        return (int) event.getX();
    }

    private boolean handleTouchEventMove(MotionEvent event) {
        int tmpX = getStartPoint(event);
        if (MotionState.kNone == motionState) {
            return false;
        }
        if (event.getPointerCount() > 1) {
            return true;
        }
        if (MotionState.kWaiting == motionState) {
            if (Math.abs(tmpX - touchStartX) < moveThreshold) {
                return true;
            }
            provider.finishAnimation();
            if (tmpX > touchStartX) {
                if (isCurlType) {
                    drawNextPage();
                }
                if (!prepareTurnPrePage() && readStatus.book.book_type == 0) {
                    motionState = MotionState.kNone;
                    return false;
                }
                if (isCurlType) {
                    drawCurrentPage();
                } else {
                    drawNextPage();
                }
                motionState = MotionState.kMoveToRight;
            } else {
                if (!prepareTurnNextPage()) {
                    motionState = MotionState.kNone;
                    return false;
                }
                drawNextPage();
                motionState = MotionState.kMoveToLeft;
            }


            initMoveState = motionState;
            currentMoveStateStartX = touchStartX;
            currentMoveStateLastX = touchStartX;
            provider.setTouchStartPosition(touchStartX, touchStartY, MotionState.kMoveToLeft == motionState);
        }

        updateMoveState(tmpX);

        provider.moveEvent(event);
        postInvalidate();
        return true;
    }

    private boolean prepareTurnPrePage() {
        if (!isAutoReadMode()) {
            Constants.manualReadedCount++;
            dataFactory.dealManualDialogShow();
        }
        return dataFactory.previous();
    }

    private boolean prepareTurnNextPage() {
        if (!isAutoReadMode()) {
            Constants.manualReadedCount++;
            dataFactory.dealManualDialogShow();
        }
        return dataFactory.next();
    }

    private boolean handleTouchEventUp(MotionEvent event) {
        if (provider != null) {
            provider.upEvent();
        }
        if (motionState == MotionState.kNone)
            return false;
        if (motionState == MotionState.kWaiting) {
            onClick(event);
        } else {
            if (initMoveState == validMoveState && provider != null) {
                if (MotionState.kMoveToRight == validMoveState) {
                    provider.startTurnAnimation(false);
                } else {
                    provider.startTurnAnimation(true);
                }
            } else if (provider != null) {
                provider.startTurnAnimation(MotionState.kMoveToRight == initMoveState);
            }
        }
        motionState = MotionState.kNone;
        return true;
    }

    private Rect rect = new Rect();
    private Rect rectDown = new Rect();

    private void onClick(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        int h4 = pageHeight / 4;
        int w3 = pageWidth / 3;
        if (AdOnclick(x, y)) {
            if (Constants.DEVELOPER_MODE) {
                ToastUtils.showToastNoRepeat("AdOnclick");
            }
        } else {
            if(!Constants.FULL_SCREEN_READ) {
                if (x <= w3) {
                    tryTurnPrePage();
                } else if (x >= pageWidth - w3 || (y >= pageHeight - h4 && x >= w3)) {
                    tryTurnNextPage(event);
                } else {

                    if (callBack != null) {
                        callBack.onShowMenu(true);
                    }
                }
            }else{
                if(x<=w3 || x >= pageWidth - w3 || (y >= pageHeight - h4 && x >= w3)){
                    tryTurnNextPage(event);
                }else{
                    if (callBack != null) {
                        callBack.onShowMenu(true);
                    }
                }
            }
        }
    }

    private boolean AdOnclick(int x, int y) {
        if (readStatus.native_type == 20) {
            if (readStatus.width_nativead_middle == 0 || readStatus.height_middle_nativead == 0) {
                return false;
            }
            rect.left = 0;
            rect.top = (int) readStatus.y_nativead;
            rect.right = readStatus.screenWidth;
            rect.bottom = (int) readStatus.y_nativead + readStatus.height_middle_nativead;

            rectDown.left = 0;
            rectDown.top = 0;
            rectDown.right = 0;
            rectDown.bottom = 0;
        } else if (readStatus.native_type == 2 || readStatus.native_type == 5) {
            if (readStatus.width_nativead_big == 0 || readStatus.height_nativead_big == 0) {
                return false;
            }
            rect.left = (readStatus.screenWidth - readStatus.width_nativead_big) / 2;
            rect.top = (readStatus.screenHeight - readStatus.height_nativead_big) / 2;
            rect.right = (readStatus.screenWidth + readStatus.width_nativead_big) / 2;
            rect.bottom = (readStatus.screenHeight + readStatus.height_nativead_big) / 2;

            rectDown.left = 0;
            rectDown.top = 0;
            rectDown.right = 0;
            rectDown.bottom = 0;
        } else if (readStatus.native_type == 21) {
            if (readStatus.width_nativead_middle == 0 || readStatus.height_middle_nativead == 0) {
                return false;
            }
            rect.left = 0;
            rect.top = (int) (readStatus.y_nativead - readStatus.height_middle_nativead);
            rect.right = readStatus.screenWidth;
            rect.bottom = (int) readStatus.y_nativead;

            rectDown.left = 0;
            rectDown.top = (int) readStatus.y_nativead;
            rectDown.right = readStatus.screenWidth;
            rectDown.bottom = (int) readStatus.y_nativead + readStatus.height_middle_nativead;
        } else if (readStatus.native_type == 22) {
            if (readStatus.width_nativead_middle == 0 || readStatus.height_middle_nativead == 0) {
                return false;
            }
            rect.left = 0;
            rect.top = (int) (readStatus.y_nativead);
            rect.right = readStatus.screenWidth;
            rect.bottom = (int) readStatus.y_nativead + readStatus.height_middle_nativead;

            rectDown.left = 0;
            rectDown.top = 0;
            rectDown.right = 0;
            rectDown.bottom = 0;
        } else if (readStatus.native_type == 23) {
            if (readStatus.width_nativead_middle == 0 || readStatus.height_middle_nativead == 0) {
                return false;
            }
            rect.left = 0;
            rect.top = (int) (readStatus.y_nativead);
            rect.right = readStatus.screenWidth;
            rect.bottom = (int) readStatus.y_nativead + readStatus.height_middle_nativead;

            rectDown.left = 0;
            rectDown.top = 0;
            rectDown.right = 0;
            rectDown.bottom = 0;
        } else if (readStatus.native_type == 24) {
            if (readStatus.width_nativead_middle == 0 || readStatus.height_middle_nativead == 0) {
                return false;
            }
            rect.left = 0;
            rect.top = (int) (readStatus.y_nativead);
            rect.right = readStatus.screenWidth;
            rect.bottom = (int) readStatus.y_nativead + readStatus.height_middle_nativead;

            rectDown.left = 0;
            rectDown.top = 0;
            rectDown.right = 0;
            rectDown.bottom = 0;
        } else if (readStatus.native_type == 25) {
            if (readStatus.width_nativead == 0 || readStatus.height_nativead == 0) {
                return false;
            }
            rect.left = 0;
            rect.top = (int) (readStatus.y_nativead);
            rect.right = readStatus.screenWidth;
            rect.bottom = (int) readStatus.y_nativead + readStatus.height_nativead;

            rectDown.left = 0;
            rectDown.top = 0;
            rectDown.right = 0;
            rectDown.bottom = 0;
        } else {
            return false;
        }

        if (rect.contains(x, y)) {
            AppLog.e(TAG, "PageView onADClicked");
            if (statisticManager == null) {
                statisticManager = StatisticManager.getStatisticManager();
            }
            if (readStatus.native_type == 20 || readStatus.native_type == 21 || readStatus.native_type == 23) {
                try {
                    if (readStatus.currentAdInfo != null) {
                        AdSceneData adSceneData = readStatus.currentAdInfo.getAdSceneData();
                        if (adSceneData != null) {
                            adSceneData.ad_markId = NativeInit.ad_mark_id[1];
                            adSceneData.ad_clickLocation = x + "*" + y;
                        }
                        statisticManager.schedulingRequest(mActivity, readStatus.novel_basePageView, readStatus.currentAdInfo, getCurrentNovel(), StatisticManager.TYPE_CLICK, NativeInit.ad_position[1]);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            } else if (readStatus.native_type == 22 || readStatus.native_type == 24 || readStatus.native_type == 25) {
                try {
                    if (readStatus.currentAdInfoDown != null) {
                        AdSceneData adSceneData = readStatus.currentAdInfoDown.getAdSceneData();
                        if (adSceneData != null) {
                            adSceneData.ad_markId = NativeInit.ad_mark_id[1];
                            adSceneData.ad_clickLocation = x + "*" + y;
                        }
                        statisticManager.schedulingRequest(mActivity, readStatus.novel_basePageView, readStatus.currentAdInfoDown, getCurrentNovel(), StatisticManager.TYPE_CLICK, NativeInit.ad_position[1]);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            } else if (readStatus.native_type == 2 || readStatus.native_type == 5) {
                try {
                    AdSceneData adSceneData;
                    if (readStatus.native_type == 2 && readStatus.currentAdInfo_image != null) {
                        adSceneData = readStatus.currentAdInfo_image.getAdSceneData();
                        if (adSceneData != null) {
                            adSceneData.ad_markId = NativeInit.ad_mark_id[2];
                            adSceneData.ad_clickLocation = x + "*" + y;
                        }
                        statisticManager.schedulingRequest(mActivity, readStatus.novel_basePageView, readStatus.currentAdInfo_image, getCurrentNovel(), StatisticManager.TYPE_CLICK, NativeInit.ad_position[2]);
                    } else if (readStatus.native_type == 5 && readStatus.currentAdInfo_in_chapter != null) {
                        adSceneData = readStatus.currentAdInfo_in_chapter.getAdSceneData();
                        if (adSceneData != null) {
                            adSceneData.ad_markId = NativeInit.ad_mark_id[7];
                            adSceneData.ad_clickLocation = x + "*" + y;
                        }
                        statisticManager.schedulingRequest(mActivity, readStatus.novel_basePageView, readStatus.currentAdInfo_in_chapter, getCurrentNovel(), StatisticManager.TYPE_CLICK, NativeInit.ad_position[7]);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
        if (rectDown.contains(x, y)) {
            AppLog.e(TAG, "PageView onADClicked");
            if (statisticManager == null) {
                statisticManager = StatisticManager.getStatisticManager();
            }
            if (readStatus.native_type == 21) {
                try {
                    if (readStatus.currentAdInfoDown != null) {
                        AdSceneData adSceneData = readStatus.currentAdInfoDown.getAdSceneData();
                        if (adSceneData != null) {
                            adSceneData.ad_markId = NativeInit.ad_mark_id[1];
                            adSceneData.ad_clickLocation = x + "*" + y;
                        }
                        statisticManager.schedulingRequest(mActivity, readStatus.novel_basePageView, readStatus.currentAdInfoDown, getCurrentNovel(), StatisticManager.TYPE_CLICK, NativeInit.ad_position[1]);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 翻到上一页
     */
    public void tryTurnPrePage() {
        readStatus.startReadTime = System.currentTimeMillis();
        if (isCurlType) {
            drawNextPage();
        }
        if (prepareTurnPrePage()) {
            provider.setTouchStartPosition(0, pageHeight, false);
            provider.startTurnAnimation(false);
            if (isCurlType) {
                drawCurrentPage();
            } else {
                drawNextPage();
            }
        }
    }

    private void tryTurnNextPage(MotionEvent event) {
        drawCurrentPage();
        if (prepareTurnNextPage()) {
            int pix = AppUtils.dip2px(getContext(), 50);
            if (provider != null) {
                if (null == event) {
                    provider.setTouchStartPosition(pageWidth - pix, pageHeight - pix, true);
                } else {
                    provider.setTouchStartPosition((int) event.getX(), (int) event.getY(), true);
                }
            }
            if (provider != null) {
                provider.startTurnAnimation(true);
            }

            endTime = System.currentTimeMillis();
            addLog(endTime);
            drawNextPage();
        }
    }


    public void addLog(long endTime){
        //判断章节的最后一页
        if(readStatus.sequence+1>readStatus.lastSequenceRemark&&!isFirstCome){
            //按照此顺序传值 当前的book_id，阅读章节，书籍源，章节总页数，当前阅读页，当前页总字数，当前页面来自，开始阅读时间,结束时间,阅读时间,是否有阅读中间退出行为,书籍来源1为青果，2为智能
            StartLogClickUtil.upLoadReadContent(readStatus.book_id,readStatus.lastSequenceRemark+"",readStatus.source_ids,readStatus.lastPageCount+"",
                    readStatus.lastCurrentPageRemark+"",readStatus.currentPageConentLength+"",readStatus.requestItem.fromType+"",
                    readStatus.startReadTime+"",endTime+"",endTime-readStatus.startReadTime+"","false",readStatus.requestItem.channel_code+"");
        }else{
            //按照此顺序传值 当前的book_id，阅读章节，书籍源，章节总页数，当前阅读页，当前页总字数，当前页面来自，开始阅读时间,结束时间,阅读时间,是否有阅读中间退出行为,书籍来源1为青果，2为智能
            StartLogClickUtil.upLoadReadContent(readStatus.book_id,readStatus.sequence+1+"",readStatus.source_ids,readStatus.pageCount+"",
                    readStatus.currentPage-1+"",readStatus.currentPageConentLength+"",readStatus.requestItem.fromType+"",
                    readStatus.startReadTime+"",endTime+"",endTime-readStatus.startReadTime+"","false",readStatus.requestItem.channel_code+"");
        }

        readStatus.startReadTime = endTime;
        readStatus.requestItem.fromType =2;
        readStatus.lastSequenceRemark = readStatus.sequence+1;
        readStatus.lastCurrentPageRemark = readStatus.currentPage;
        readStatus.lastPageCount = readStatus.pageCount;
        isFirstCome = false;
    }
    private boolean cancelState = false;
    private boolean canRestore = false;

    private void updateMoveState(int currentX) {
        if (MotionState.kMoveToRight == motionState) {
            if (canRestore && !isCurlType && (currentX > touchStartX) && callBack != null) {
                canRestore = false;
                AppLog.e("updateMoveState", "kMoveToRight-onCancelPage");
            }

            if (currentX < currentMoveStateLastX - moveThreshold) {
                motionState = MotionState.kMoveToLeft;
                currentMoveStateStartX = currentMoveStateLastX;
                cancelState = !cancelState;
                canRestore = true;
            }

            currentMoveStateLastX = currentX;
        } else if (MotionState.kMoveToLeft == motionState) {
            if (canRestore && !isCurlType && (currentX < touchStartX) && callBack != null) {
                canRestore = false;
                AppLog.e("updateMoveState", "kMoveToLeft-onCancelPage");
            }
            if (currentX > currentMoveStateLastX + moveThreshold) {
                motionState = MotionState.kMoveToRight;
                currentMoveStateStartX = currentMoveStateLastX;
                cancelState = !cancelState;
                canRestore = true;
            }

            currentMoveStateLastX = currentX;
        } else {
            return;
        }

        if (Math.abs(currentX - currentMoveStateStartX) > turnThreshold) {
            if (currentX > currentMoveStateStartX) {
                validMoveState = MotionState.kMoveToRight;
            } else {
                validMoveState = MotionState.kMoveToLeft;
            }
        }
    }

    private AnimationProvider getAnimationProvider() {

        if (provider == null || provider.pageMode != Constants.PAGE_MODE) {
            AppLog.e("PageView", "getAnimationProvider new");
            switch (Constants.PAGE_MODE) {
                case AnimationProvider.SHIFT_MODE:
                    isCurlType = false;
                    Constants.isSlideUp = false;
                    provider = new ShiftAnimationProvider(myBitmapManager, readStatus);
                    break;
                case AnimationProvider.CURL_MODE:
                    isCurlType = true;
                    Constants.isSlideUp = false;
                    provider = new CurlAnimationProvider(myBitmapManager, readStatus);
                    break;

                case AnimationProvider.SLIDE_MODE:
                    isCurlType = false;
                    Constants.isSlideUp = false;
                    provider = new SlideAnimationProvider(myBitmapManager, readStatus);
                    break;
                case AnimationProvider.SLIDE_UP_MODE:
                    isCurlType = false;
                    Constants.isSlideUp = true;
                    provider = new SlideAnimationProvider(myBitmapManager, readStatus);
                    break;
                default:
                    isCurlType = false;
                    provider = new ShiftAnimationProvider(myBitmapManager, readStatus);
                    break;

            }
            callBack.onResize();
            provider.clear();
            provider.backColor = this.backColor;
            provider.pageMode = Constants.PAGE_MODE;
            provider.setScroller(mScroller);
            provider.setCallBack(callBack);
            provider.setPageView(this);
            provider.getDivider(getResources());
        }

        return provider;
    }

    public void onAnimationFinish() {
        if (cancelState && callBack != null) {
            callBack.onCancelPage();
        }
        cancelState = false;
        drawCurrentPage();
    }

    @Override
    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public void clear() {
        if (myBitmapManager != null) {
            myBitmapManager.clearBitmap();
        }
        if (drawTextHelper != null) {
            drawTextHelper.clear();
        }
        if (null != autoReadImpl) {
            autoReadImpl.close();
            autoReadImpl = null;
        }
        if (provider != null) {
            provider.clear();
        }
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }

        if (this.mActivity != null) {
            this.mActivity = null;
        }

        if (this.mContext != null) {
            this.mContext = null;
        }

//        if (this.readStatus != null) {
//            this.readStatus = null;
//        }

        if (this.dataFactory != null) {
            this.dataFactory = null;
        }

        if (this.pageLines != null) {
            this.pageLines.clear();
            this.pageLines = null;
        }

        if (this.nextPageContent != null) {
            this.nextPageContent.clear();
            this.nextPageContent = null;
        }

        if (this.currentPageContent != null) {
            this.currentPageContent.clear();
            this.currentPageContent = null;
        }
        System.gc();
    }

    private AutoReadImpl autoReadImpl;
    private int tempPageMode = AnimationProvider.SHIFT_MODE;
    private PowerManager.WakeLock mWakeLock;

    public boolean isAutoReadMode() {
        return autoReadImpl != null;
    }

    public void startAutoRead() {
        if (readStatus.currentPage == readStatus.pageCount && readStatus.sequence + 1 == readStatus.chapterCount) {
            return;
        }
        isMoveing = true;
        tempPageMode = Constants.PAGE_MODE;
        Constants.PAGE_MODE = AnimationProvider.SHIFT_MODE;
        getAnimationProvider();
        if (null == autoReadImpl) {
            autoReadImpl = new AutoReadImpl();
        }
        refreshCurrentPage();
        tryResumeAutoRead();
    }

    public void exitAutoRead() {
        Constants.PAGE_MODE = tempPageMode;
        if (tempPageMode == AnimationProvider.SLIDE_UP_MODE) {
            Constants.isSlideUp = true;
        } else {
            Constants.isSlideUp = false;
        }
        if (null != autoReadImpl) {
            autoReadImpl.close();
            autoReadImpl = null;
            isAutoMenuShowing = false;

            if (callBack != null) {
                callBack.onCancelPage();
            }
            drawCurrentPage();
            drawNextPage();
        }
    }

    public void exitAutoReadNoCancel() {
        Constants.PAGE_MODE = tempPageMode;
        if (null != autoReadImpl) {
            autoReadImpl.close();
            autoReadImpl = null;

            drawCurrentPage();
            drawNextPage();

            if (mContext instanceof ReadingActivity) {
                ReadingActivity new_name = (ReadingActivity) mContext;
                new_name.autoStop();

            }
        }
    }

    /**
     * 开始自动阅读，这里面有读下一页的逻辑
     */
    public void tryResumeAutoRead() {

        if (null != autoReadImpl) {
            if (!prepareTurnNextPage()) {
                exitAutoReadNoCancel();
                return;
            }
            drawNextPage();
            autoReadImpl.updateDrawPosition();
            autoReadImpl.start();
        }
    }

    /**
     * 恢复自动阅读，没有翻页逻辑
     */
    public void resumeAutoRead() {
        if (null != autoReadImpl && !isAutoMenuShowing) {
            autoReadImpl.updateDrawPosition();
            autoReadImpl.start();
        }
    }

    public void pauseAutoRead() {
        if (null != autoReadImpl) {
            autoReadImpl.pause();
        }
    }

    private class AutoReadImpl {

        private static final long kAutoReadIntervalMillis = 50;
        private static final double kAutoReadFrequency = 1000.0 / kAutoReadIntervalMillis;
        private static final int kAutoReadTimer = 1;

        private int readPosition;
        private int lastTouchY;
        private long startTouchTime;
        private int startTouchX;
        private int startTouchY;

        private double remainLen;

        private Handler handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {

                if (null == handler) {
                    return;
                }

                if (kAutoReadTimer == msg.what) {
                    if (0 != startTouchTime) {
                        postAutoReadTimer();
                        return;
                    }

                    double stepLen = pageHeight * readStatus.autoReadFactor() / (10 * kAutoReadFrequency);
                    remainLen += stepLen;
                    int inc = (int) remainLen;
                    if (inc > 0) {
                        remainLen -= inc;
                        if (!changeReadPosition(inc)) {
                            exitAutoRead();
                            return;
                        }
                    }
                    postAutoReadTimer();
                    return;
                }
            }
        };

        public void start() {
            mWakeLock.acquire();
            postAutoReadTimer();
        }

        public void close() {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
            if (handler != null) {
                handler.removeMessages(kAutoReadTimer);
            }
            handler = null;
            if (provider != null) {
                provider.setReadPosition(0);
            }
        }

        public void pause() {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
            handler.removeMessages(kAutoReadTimer);
        }

        private void postAutoReadTimer() {
            handler.removeMessages(kAutoReadTimer);
            handler.sendEmptyMessageDelayed(kAutoReadTimer, kAutoReadIntervalMillis);
        }

        public boolean onTouchEvent(MotionEvent event) {

            int tmpX = (int) event.getX();
            int tmpY = (int) event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastTouchY = tmpY;
                    startTouchTime = System.currentTimeMillis();
                    startTouchX = tmpX;
                    startTouchY = tmpY;
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    startTouchTime = 0;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (tmpY != lastTouchY && !isAutoMenuShowing) {
                        changeReadPosition(tmpY - lastTouchY);
                        lastTouchY = tmpY;
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    long touchTime = System.currentTimeMillis() - startTouchTime;
                    int distance = (int) Math.sqrt(Math.pow(startTouchX - tmpX, 2) + Math.pow(startTouchY - tmpY, 2));
                    if (touchTime < 100 && distance < 30 || distance < 10) {
                        onClick();
                    }
                    startTouchTime = 0;
                    return true;
            }

            return false;
        }

        public void onClick() {
            if (callBack != null) {
                isAutoMenuShowing = !isAutoMenuShowing;
                callBack.onShowAutoMenu(isAutoMenuShowing);

            }
        }

        private boolean changeReadPosition(int inc) {
            readPosition += inc;

            if (readPosition < 0) {
                readPosition = 0;
            }
            if (readPosition >= pageHeight) {
                if (!decideCanRun()) {
                    return false;
                }
                drawNextPage();
            }

            updateDrawPosition();
            return true;
        }

        public boolean decideCanRun() {
            if ((readStatus.sequence + 1 == readStatus.chapterCount && readStatus.currentPage == readStatus.pageCount)) {
                exitAutoReadNoCancel();
                return false;
            }
            readPosition = 0;
            try {
                onAnimationFinish();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!tryTurnPage()) {
                exitAutoReadNoCancel();
                if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
                    dataFactory.getChapterByLoading(ReadingActivity.MSG_LOAD_NEXT_CHAPTER, readStatus.sequence + 1);
                } else {
                    Toast.makeText(mContext, "网络不给力，请稍后重试", Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            return true;
        }

        private void updateDrawPosition() {
            provider.setReadPosition(readPosition);
            invalidate();
        }

        private boolean tryTurnPage() {
            if (readStatus.book.book_type == 0) {
                if (!dataFactory.nextByAutoRead()) {
                    return false;
                }
            } else {
                if (!dataFactory.next()) {
                    return false;
                }
            }

            return true;
        }
    }


    @Override
    public void getChapter(boolean needSavePage) {
        // TODO Auto-generated method stub

    }

    @Override
    public void getPreChapter() {
        // TODO Auto-generated method stub

    }

    @Override
    public void getNextChapter() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setFirstPage(boolean firstPage) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setisAutoMenuShowing(boolean isShowing) {
        isAutoMenuShowing = isShowing;

    }

    @Override
    public boolean setKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                tryTurnPrePage();
            }
            return true;
        }
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                tryTurnNextPage(null);
            }
            return true;
        }

        return false;
    }

    @Override
    public void loadNatvieAd() {
        drawTextHelper.loadNatvieAd();
    }

    @Override
    public Novel getCurrentNovel() {
        if (dataFactory != null) {
            return dataFactory.transformation();
        }
        return null;
    }
}
