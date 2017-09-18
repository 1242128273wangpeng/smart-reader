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
    //	private int v;
    protected Scroller mScroller;
    boolean moveToLeft;
    boolean moveToLeftUp;
    private int speed = 10;
    private boolean isCanDoStep = false;
    private int mMinFlingVelocity = 4500;
    private int mMinFlingVelocity2 = 6000;


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
//        .4,.06,.04,.76
//        mScroller = new Scroller(BaseBookApplication.getGlobalContext(), new EaseCubicInterpolator(.58f, .2f, .04f, .76f));
        mScroller = new Scroller(BaseBookApplication.getGlobalContext(), new DecelerateInterpolator());
    }

    @Override
    public boolean moveEvent(MotionEvent event) {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
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

        synchronized (mScroller) {
            if (mScroller.computeScrollOffset()) {

                if (mScroller.timePassed() > DURATION * 3 / 4) {
                    pageView.setTouchable(true);
                }

                moveX = mScroller.getCurrX();

                System.out.println("moveX : " + moveX);

                if (moveX > mWidth) {
                    isCanDoStep = false;
                    moveX = 0;// currentbitmap归位
                    finishAnimation();
                    return;
                }

                if (moveToLeft) {
                    moveX *= -1;
                }

                pageView.postInvalidate();
            } else {

                isCanDoStep = false;
                moveX = 0;// currentbitmap归位
                finishAnimation();
            }
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
//        isCanDoStep = false;
//        if(!mScroller.isFinished()){
//            mScroller.abortAnimation();
//        }
        mTouch.x = this.startX = startX;
        mTouch.y = this.startY = startY;
        this.moveToLeft = moveToLeft;
    }

    @Override
    public void startTurnAnimation(boolean moveToLeft) {
        synchronized (mScroller) {
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }

            this.moveToLeftUp = moveToLeft;
            moveX = 0;

            pageView.setTouchable(false);


//        int d = DURATION;
//        if (moveX != 0)
//            d = (int) ((1 - Math.abs(moveX) / mWidth) * DURATION);

            mScroller.startScroll((int) Math.abs(moveX), 0, mWidth - (int) Math.abs(moveX), 0, DURATION);

            isCanDoStep = true;
            pageView.postInvalidate();
        }
    }

    @Override
    public void startFlingAnimation(boolean moveToLeft, float velocityX) {
//        if (mMinFlingVelocity == -1) {
//            mMinFlingVelocity = ViewConfiguration.get(BaseBookApplication.getGlobalContext()).getScaledMinimumFlingVelocity();
//
//        }

        float velocity = Math.abs(velocityX);

        if (velocity < mMinFlingVelocity) {
            velocity = mMinFlingVelocity;
        }

        this.moveToLeftUp = moveToLeft;
        isCanDoStep = true;

        if (readStatus.currentPage == readStatus.pageCount || readStatus.currentPage == 1) {
            pageView.setTouchable(false);
            if (velocity < mMinFlingVelocity2) {
                velocity = mMinFlingVelocity2;
            }
        }

//        if(!mScroller.isFinished()) {
//            mScroller.abortAnimation();
//            return;
//        }

        float distance = mWidth - Math.abs(moveX);

        while (FlingHelper.INSTANCE.getTargetDistance(velocity) < distance) {
            velocity += 500;
        }

        pageView.invalidate();
//        int d = DURATION;
//        if (moveX != 0)
//            d = (int) ((1 - Math.abs(moveX) / mWidth) * DURATION);
//
//        mScroller.startScroll((int) Math.abs(moveX), 0, mWidth - (int) Math.abs(moveX), 0, DURATION);

        mScroller.fling((int) Math.abs(moveX), 0, (int) velocity, 0, 0, Integer.MAX_VALUE, 0, 0);


    }

    @Override
    public void finishAnimation() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
//            moveX = mScroller.getFinalX();
//            pageView.postInvalidate();
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
