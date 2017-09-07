package com.intelligent.reader.read.animation;

import com.intelligent.reader.read.help.CallBack;
import com.intelligent.reader.read.page.PageView;

import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.ReadStatus;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.widget.Scroller;


public abstract class AnimationProvider {

    public static final int SHIFT_MODE = 0;
    public static final int CURL_MODE = 1;
    public static final int SLIDE_MODE = 2;
    public static final int SLIDE_UP_MODE = 3;
    public int backColor = 0xFFAAAAAA;
    public int pageMode;
    protected BitmapManager bitmapManager;
    protected ReadStatus readStatus;
    protected PointF mTouch = new PointF(); // 拖拽点
    protected Bitmap mCurPageBitmap = null; // 当前页
    protected Bitmap mNextPageBitmap = null;
    protected int mWidth = 480;
    protected int mHeight = 800;
    protected float startX, startY;
    protected Scroller mScroller;
    protected CallBack callBack;
    protected PageView pageView;
    protected int readPosition;
    protected int footTop;
    protected int footHeight;
    Bitmap sDividerBmpDay, sDividerBmpNight, dividerBmp;

    public AnimationProvider(BitmapManager manager, ReadStatus readStatus) {
        this.bitmapManager = manager;
        this.readStatus = readStatus;

        mCurPageBitmap = bitmapManager.getBitmap(BitmapManager.CURRENT);
        mNextPageBitmap = bitmapManager.getBitmap(BitmapManager.NEXT);

        mWidth = readStatus.screenWidth;
        mHeight = readStatus.screenHeight;
    }

    public void setWH(int w, int h) {
        this.mWidth = w;
        this.mHeight = h;
        footTop = mHeight - footHeight;
    }

    public void setTouch(float x, float y) {
        mTouch.x = x;
        mTouch.y = y;
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public void setPageView(PageView pageView) {
        this.pageView = pageView;
    }

    public void setReadPosition(int position) {
        this.readPosition = position;
    }

    public void resetReadPosition() {
        this.readPosition = 0;
    }

    public void setScroller(Scroller scroller) {
        this.mScroller = scroller;
    }

    public void getDivider(Resources res) {
        if (Constants.MODE == 55 || Constants.MODE == 58) {
            if (sDividerBmpNight == null || sDividerBmpNight.isRecycled()) {
                sDividerBmpNight = BitmapFactory.decodeResource(res, net.lzbook.kit.R.drawable.content_auto_read_night);
            }
            dividerBmp = sDividerBmpNight;
        } else {
            if (sDividerBmpDay == null || sDividerBmpDay.isRecycled()) {
                sDividerBmpDay = BitmapFactory.decodeResource(res, net.lzbook.kit.R.drawable.content_auto_read_day);
            }
            dividerBmp = sDividerBmpDay;
        }
    }

    public void clear() {
        if (sDividerBmpNight != null && !sDividerBmpNight.isRecycled()) {
            sDividerBmpNight.isRecycled();
            sDividerBmpNight = null;
        }
        if (sDividerBmpDay != null && !sDividerBmpDay.isRecycled()) {
            sDividerBmpDay.isRecycled();
            sDividerBmpDay = null;
        }
    }

    public abstract void drawInternal(Canvas canvas);

    public abstract boolean moveEvent(MotionEvent event);

    public abstract void setTouchStartPosition(int x, int y, boolean moveToLeft);

    public abstract void startTurnAnimation(boolean moveToLeft);

    public abstract void finishAnimation();

    public abstract void loadPage(boolean next);

    public abstract void upEvent();
}
