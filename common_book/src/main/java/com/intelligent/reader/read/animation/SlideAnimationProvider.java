package com.intelligent.reader.read.animation;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.data.bean.ReadStatus;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

public class SlideAnimationProvider extends AnimationProvider {

    protected float moveX, moveY;
    protected Paint paint;
    boolean moveToLeft;
    boolean moveToLeftUp;
    private int speed = 10;
    private boolean isCanDoStep = false;
    //	private int v;
    private Scroller mScroller;

    public SlideAnimationProvider(BitmapManager manager, ReadStatus readStatus) {
        super(manager, readStatus);
        init();
    }

    @Override
    public void drawInternal(Canvas canvas) {
        doStep();
        if (moveX > 0) {
            canvas.drawBitmap(mNextPageBitmap, moveX - mWidth, 0, paint);

        } else {
            canvas.drawBitmap(mNextPageBitmap, moveX + mWidth, 0, paint);
        }

        canvas.drawBitmap(mCurPageBitmap, moveX, 0, paint);

    }

    protected void init() {
        speed = mWidth / 12;
//		v = mWidth / 8;

        paint = new Paint();
        paint.setFilterBitmap(true);
        paint.setDither(true);
//		paint.setAntiAlias(true);
        mScroller = new Scroller(BaseBookApplication.getGlobalContext(), new DecelerateInterpolator());
    }

    @Override
    public boolean moveEvent(MotionEvent event) {
        moveX = event.getX() - mTouch.x;
        moveY = event.getY() - mTouch.y;
        // mTouch.x = event.getX();
        // mTouch.y = event.getY();

        return true;
    }

    protected void doStep() {
        if (!isCanDoStep) {
            return;
        }

        if (mScroller.computeScrollOffset()) {

            moveX = mScroller.getCurrX();

            pageView.postInvalidate();
        } else {

            isCanDoStep = false;
            moveX = 0;// currentbitmap归位
            finishAnimation();
        }

//		if (moveToLeftUp) {
//			moveX -= speed;
//		} else {
//			moveX += speed;
//		}
//
//		pageView.postInvalidate();
//		if (moveToLeftUp != moveToLeft) {
//            if(moveToLeftUp && moveX <= 0){
//                moveX = 0;// currentbitmap归位
//                isCanDoStep = false;
//                finishAnimation();
//            }else if(!moveToLeftUp && moveX >= 0){
//                moveX = 0;// currentbitmap归位
//                isCanDoStep = false;
//                finishAnimation();
//            }
////			if (moveX <= 0 || moveX >= 0) {
////				moveX = 0;// currentbitmap归位
////				isCanDoStep = false;
////				finishAnimation();
////			}
//		} else {
//			if (moveX <= -mWidth || moveX >= mWidth) {
//				moveX = 0;// currentbitmap归位
//				isCanDoStep = false;
//				finishAnimation();
//			}
//		}

    }

    @Override
    public void setTouchStartPosition(int startX, int startY, boolean moveToLeft) {
        mTouch.x = this.startX = startX;
        mTouch.y = this.startY = startY;
        this.moveToLeft = moveToLeft;
    }

    @Override
    public void startTurnAnimation(boolean moveToLeft) {
        this.moveToLeftUp = moveToLeft;
        isCanDoStep = true;
        pageView.setTouchable(false);
        if (moveToLeft)
            mScroller.startScroll((int) moveX, 0, -mWidth - (int) moveX, 0, 600);
        else
            mScroller.startScroll((int) moveX, 0, mWidth - (int) moveX, 0, 600);
        pageView.invalidate();
    }

    @Override
    public void finishAnimation() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
            moveX = mScroller.getFinalX();
            pageView.postInvalidate();
        }
        if (pageView != null) {
            pageView.onAnimationFinish();
        }
    }

    @Override
    public void loadPage(boolean next) {
        // TODO Auto-generated method stub

    }

    @Override
    public void upEvent() {
        // TODO Auto-generated method stub

    }

}
