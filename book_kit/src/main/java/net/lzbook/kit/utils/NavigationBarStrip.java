package net.lzbook.kit.utils;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import net.lzbook.kit.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NavigationBarStrip extends View implements ViewPager.OnPageChangeListener {

    //默认导航条宽度
    int DEFAULT_INDICATOR_WIDTH = 4;
    //默认导航条圆角
    int DEFAULT_INDICATOR_RADIUS = 4;
    //默认导航条内边距
    int DEFAULT_INDICATOR_PADDING = 10;
    //默认导航条底部外边距
    int DEFAULT_INDICATOR_MARGIN_BOTTOM = 2;
    //默认导航栏文字大小
    int DEFAULT_INDICATOR_TEXT_SIZE = 15;

    private final static int HIGH_QUALITY_FLAGS = Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG;

    private final RectF bounds = new RectF();
    private final RectF stripBounds = new RectF();
    private final Rect titleBounds = new Rect();

    private final ValueAnimator valueAnimator = new ValueAnimator();
    private final ArgbEvaluator argbEvaluator = new ArgbEvaluator();

    private final ResizeInterpolator resizeInterpolator = new ResizeInterpolator();

    private List<String> titles = new ArrayList<>();

    private ViewPager navigationViewPager;
    private ViewPager.OnPageChangeListener onPageChangeListener;


    private int scrollState;

    private float mTabSize;

    private float mFraction;

    private float mStartStripX;
    private float mEndStripX;
    private float mStripLeft;
    private float mStripRight;

    private boolean mIsViewPagerMode;
    private boolean mIsResizeIn;
    private boolean mIsActionDown;
    private boolean mIsTabActionDown;
    private boolean mIsSetIndexFromTabBar;

    private float indicatorHeight;

    private float indicatorRadius;

    private float indicatorPadding;

    private int indicatorAnimationDuration;

    private int indicatorActiveColor;
    private int indicatorInactiveColor;

    //滑动控件距离底部高度
    private int indicatorMarginBottom;

    private boolean clickAble = true;

    private StripType stripType;
    private StripGravity stripGravity;

    private final static int INVALID_INDEX = -1;

    private final static float MIN_FRACTION = 0.0F;
    private final static float MAX_FRACTION = 1.0F;

    private int mLastIndex = INVALID_INDEX;
    private int mIndex = INVALID_INDEX;

    private final Paint stripPaint = new Paint(HIGH_QUALITY_FLAGS) {
        {
            setStyle(Style.FILL);
        }
    };

    private final Paint titlePaint = new TextPaint(HIGH_QUALITY_FLAGS) {
        {
            setTextAlign(Align.CENTER);
        }
    };


    public NavigationBarStrip(final Context context) {
        this(context, null);
    }

    public NavigationBarStrip(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("CustomViewStyleable")
    public NavigationBarStrip(final Context context, final AttributeSet attrs,
            final int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setWillNotDraw(false);

        ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_SOFTWARE, null);

        setLayerType(LAYER_TYPE_SOFTWARE, null);

        Resources resources = context.getResources();

        int indicatorType;
        int indicatorGravity;

        float indicatorFactor;

        float density = resources.getDisplayMetrics().density;

        float scaledDensity = resources.getDisplayMetrics().scaledDensity;

        int indicatorColor;

        float indicatorTextSize;

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs,
                    R.styleable.NavigationBarStrip);

            indicatorMarginBottom = typedArray.getDimensionPixelSize(
                    R.styleable.NavigationBarStrip_indicatorMarginBottom,
                    Math.round(DEFAULT_INDICATOR_MARGIN_BOTTOM * density));

            indicatorColor = typedArray.getColor(R.styleable.NavigationBarStrip_indicatorColor,
                    Color.parseColor("#4D91D0"));

            indicatorTextSize = typedArray.getDimensionPixelSize(
                    R.styleable.NavigationBarStrip_indicatorTextSize,
                    Math.round(DEFAULT_INDICATOR_TEXT_SIZE * scaledDensity));

            indicatorHeight = typedArray.getDimension(
                    R.styleable.NavigationBarStrip_indicatorHeight,
                    Math.round(DEFAULT_INDICATOR_WIDTH * density));

            indicatorPadding = typedArray.getDimension(
                    R.styleable.NavigationBarStrip_indicatorPadding,
                    Math.round(DEFAULT_INDICATOR_PADDING * density));

            indicatorFactor = typedArray.getFloat(R.styleable.NavigationBarStrip_indicatorFactor,
                    2.5f);

            indicatorType = typedArray.getInt(R.styleable.NavigationBarStrip_indicatorType,
                    StripType.LINE_INDEX);

            indicatorGravity = typedArray.getInt(R.styleable.NavigationBarStrip_indicatorGravity,
                    StripGravity.BOTTOM_INDEX);

            indicatorRadius = typedArray.getDimension(
                    R.styleable.NavigationBarStrip_indicatorRadius,
                    Math.round(DEFAULT_INDICATOR_RADIUS * density));

            indicatorAnimationDuration = typedArray.getInteger(
                    R.styleable.NavigationBarStrip_indicatorAnimationDuration, 200);

            indicatorActiveColor = typedArray.getColor(
                    R.styleable.NavigationBarStrip_indicatorActiveColor,
                    Color.parseColor("#191919"));

            indicatorInactiveColor = typedArray.getColor(
                    R.styleable.NavigationBarStrip_indicatorInactiveColor,
                    Color.parseColor("#9B9B9B"));

            typedArray.recycle();
        } else {

            indicatorMarginBottom = Math.round(DEFAULT_INDICATOR_MARGIN_BOTTOM * density);

            indicatorColor = Color.parseColor("#4D91D0");
            indicatorTextSize = Math.round(DEFAULT_INDICATOR_TEXT_SIZE * scaledDensity);
            indicatorHeight = Math.round(DEFAULT_INDICATOR_WIDTH * density);
            indicatorPadding = Math.round(DEFAULT_INDICATOR_PADDING * density);

            indicatorFactor = 2.5f;

            indicatorType = StripType.LINE_INDEX;
            indicatorGravity = StripGravity.BOTTOM_INDEX;

            indicatorRadius = Math.round(DEFAULT_INDICATOR_RADIUS * density);

            indicatorAnimationDuration = 200;

            indicatorActiveColor = Color.parseColor("#191919");
            indicatorInactiveColor = Color.parseColor("#9B9B9B");
        }

        insertStripColor(indicatorColor);
        insertTitleSize(indicatorTextSize);
        insertStripWeight(indicatorHeight);
        insertStripFactor(indicatorFactor);
        insertStripType(indicatorType);
        insertStripGravity(indicatorGravity);

        insertCornersRadius(indicatorRadius);
        insertAnimationDuration(indicatorAnimationDuration);

        insertActiveColor(indicatorActiveColor);
        insertInactiveColor(indicatorInactiveColor);

        valueAnimator.setFloatValues(MIN_FRACTION, MAX_FRACTION);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                updateIndicatorPosition((Float) animation.getAnimatedValue());
            }
        });
    }


    public void insertTitle(String title) {
        if (!TextUtils.isEmpty(title)) {
            titles.add(title);
            requestLayout();
        }
    }

    public void insertStripColor(final int color) {
        stripPaint.setColor(color);
        postInvalidate();
    }

    public void insertStripWeight(final float stripHeight) {
        indicatorHeight = stripHeight;
        requestLayout();
    }

    private void insertStripGravity(final int index) {
        switch (index) {
            case StripGravity.TOP_INDEX:
                insertStripGravity(StripGravity.TOP);
                break;
            case StripGravity.BOTTOM_INDEX:
            default:
                insertStripGravity(StripGravity.BOTTOM);
                break;
        }
    }

    public void insertStripGravity(final StripGravity stripGravity) {
        this.stripGravity = stripGravity;
        requestLayout();
    }

    private void insertStripType(final int index) {
        switch (index) {
            case StripType.POINT_INDEX:
                insertStripType(StripType.POINT);
                break;
            case StripType.LINE_INDEX:
            default:
                insertStripType(StripType.LINE);
                break;
        }
    }

    public void insertStripType(final StripType stripType) {
        this.stripType = stripType;
        requestLayout();
    }

    public void insertStripFactor(final float factor) {
        resizeInterpolator.setFactor(factor);
    }

    public void insertActiveColor(final int activeColor) {
        indicatorActiveColor = activeColor;
        postInvalidate();
    }

    public void insertInactiveColor(final int inactiveColor) {
        indicatorInactiveColor = inactiveColor;
        postInvalidate();
    }

    public void insertCornersRadius(final float cornersRadius) {
        indicatorRadius = cornersRadius;
        postInvalidate();
    }

    public void insertAnimationDuration(final int animationDuration) {
        this.indicatorAnimationDuration = animationDuration;
        valueAnimator.setDuration(animationDuration);
        valueAnimator.setInterpolator(new AccelerateInterpolator());
        resetScroller();
    }

    public void insertTitleSize(final float titleSize) {
        titlePaint.setTextSize(titleSize);
        postInvalidate();
    }

    public void insertViewPager(final ViewPager viewPager) {
        if (viewPager == null) {
            mIsViewPagerMode = false;
            return;
        }

        if (viewPager.equals(navigationViewPager)) {
            return;
        }

        if (viewPager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not provide adapter instance.");
        }

        mIsViewPagerMode = true;
        navigationViewPager = viewPager;
        navigationViewPager.addOnPageChangeListener(this);

        resetScroller();
        postInvalidate();
    }

    private void resetScroller() {
        if (navigationViewPager == null) {
            return;
        }
        try {
            final Field scrollerField = ViewPager.class.getDeclaredField("mScroller");
            scrollerField.setAccessible(true);
            final ResizeViewPagerScroller scroller = new ResizeViewPagerScroller(getContext());
            scrollerField.set(navigationViewPager, scroller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertOnPageChangeListener(
            final ViewPager.OnPageChangeListener onPageChangeListener) {
        this.onPageChangeListener = onPageChangeListener;
    }

    public void setTabIndex(int index) {
        setTabIndex(index, false);
    }

    public void setTabIndex(int tabIndex, boolean isForce) {
        if (valueAnimator.isRunning()) {
            return;
        }

        if (titles.size() == 0) {
            return;
        }

        int index = tabIndex;
        boolean force = isForce;

        if (mIndex == INVALID_INDEX) {
            force = true;
        }

        if (index == mIndex) {
            return;
        }

        index = Math.max(0, Math.min(index, titles.size() - 1));

        mIsResizeIn = index < mIndex;
        mLastIndex = mIndex;
        mIndex = index;

        mIsSetIndexFromTabBar = true;
        if (mIsViewPagerMode) {
            if (navigationViewPager == null) {
                throw new IllegalStateException("ViewPager is null.");
            }
            navigationViewPager.setCurrentItem(index, !force);
        }

        mStartStripX = mStripLeft;

        mEndStripX = (mIndex * mTabSize) + (stripType == StripType.POINT ? mTabSize * 0.5F : 0.0F);

        if (force) {
            updateIndicatorPosition(MAX_FRACTION);
            if (mIsViewPagerMode) {
                if (!navigationViewPager.isFakeDragging()) {
                    navigationViewPager.beginFakeDrag();
                }
                if (navigationViewPager.isFakeDragging()) {
                    navigationViewPager.fakeDragBy(0.0F);
                    navigationViewPager.endFakeDrag();
                }
            }
        } else {
            valueAnimator.start();
        }
    }

    public void deselect() {
        mLastIndex = INVALID_INDEX;
        mIndex = INVALID_INDEX;
        mStartStripX = INVALID_INDEX * mTabSize;
        mEndStripX = mStartStripX;
        updateIndicatorPosition(MIN_FRACTION);
    }

    public void updateIndicatorPosition(final float mFraction) {
        this.mFraction = mFraction;

        mStripLeft = mStartStripX + (resizeInterpolator.loadResizeInterpolation(mFraction,
                mIsResizeIn) * (mEndStripX - mStartStripX));

        mStripRight =
                (mStartStripX + (stripType == StripType.LINE ? mTabSize : indicatorHeight)) + (
                        resizeInterpolator.loadResizeInterpolation(mFraction, !mIsResizeIn) * (
                                mEndStripX
                                        - mStartStripX));

        postInvalidate();
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(final MotionEvent event) {

        if (!clickAble) {
            return false;
        }

        if (valueAnimator.isRunning()) {
            return true;
        }
        if (scrollState != ViewPager.SCROLL_STATE_IDLE) {
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsActionDown = true;
                if (!mIsViewPagerMode) {
                    break;
                }
                mIsTabActionDown = (int) (event.getX() / mTabSize) == mIndex;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsTabActionDown) {
                    navigationViewPager.setCurrentItem((int) (event.getX() / mTabSize), true);
                    break;
                }
                if (mIsActionDown) {
                    break;
                }
            case MotionEvent.ACTION_UP:
                if (mIsActionDown) {
                    setTabIndex((int) (event.getX() / mTabSize));
                }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
            default:
                mIsTabActionDown = false;
                mIsActionDown = false;
                break;
        }

        return true;
    }

    @Override
    @SuppressLint("DrawAllocation")
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec);

        bounds.set(0.0f, 0.0f, width, height);

        if (titles.size() == 0 || width == 0f || height == 0f) {
            return;
        }

        mTabSize = width / titles.size();

        if (isInEditMode() || !mIsViewPagerMode) {
            mIsSetIndexFromTabBar = true;

            if (isInEditMode()) mIndex = new Random().nextInt(titles.size());

            mStartStripX =
                    (mIndex * mTabSize) + (stripType == StripType.POINT ? mTabSize * 0.5F : 0.0F);

            mEndStripX = mStartStripX;

            updateIndicatorPosition(MAX_FRACTION);
        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        stripBounds.set(mStripLeft - (stripType == StripType.POINT ? indicatorHeight * 0.5F : 0.0F)
                        + indicatorPadding,
                stripGravity == StripGravity.BOTTOM ? bounds.height() - indicatorHeight : 0.0F,
                mStripRight - (stripType == StripType.POINT ? indicatorHeight * 0.5F : 0.0F)
                        - indicatorPadding,
                stripGravity == StripGravity.BOTTOM ? bounds.height() - indicatorMarginBottom
                        : indicatorHeight);

        if (indicatorRadius == 0) {
            canvas.drawRect(stripBounds, stripPaint);
        } else {
            canvas.drawRoundRect(stripBounds, indicatorRadius, indicatorRadius, stripPaint);
        }

        for (int i = 0; i < titles.size(); i++) {
            final String title = titles.get(i);

            final float leftTitleOffset = (mTabSize * i) + (mTabSize * 0.5F);

            titlePaint.getTextBounds(title, 0, title.length(), titleBounds);
            final float topTitleOffset =
                    (bounds.height() - indicatorHeight) * 0.5F + titleBounds.height() * 0.5F
                            - titleBounds.bottom;

            final float interpolation = resizeInterpolator.loadResizeInterpolation(mFraction, true);
            final float lastInterpolation = resizeInterpolator.loadResizeInterpolation(mFraction,
                    false);

            if (mIsSetIndexFromTabBar) {
                if (mIndex == i) {
                    updateCurrentTitle(interpolation);
                } else if (mLastIndex == i) {
                    updateLastTitle(lastInterpolation);
                } else {
                    updateInactiveTitle();
                }
            } else {
                if (i != mIndex && i != mIndex + 1) {
                    updateInactiveTitle();
                } else if (i == mIndex + 1) {
                    updateCurrentTitle(interpolation);
                } else if (i == mIndex) {
                    updateLastTitle(lastInterpolation);
                }
            }

            canvas.drawText(title, leftTitleOffset,
                    topTitleOffset + (stripGravity == StripGravity.TOP ? indicatorHeight : 0.0F),
                    titlePaint);
        }
    }

    private void updateCurrentTitle(final float interpolation) {
        titlePaint.setColor((int) argbEvaluator.evaluate(interpolation, indicatorInactiveColor,
                indicatorActiveColor));
    }

    private void updateLastTitle(final float lastInterpolation) {
        titlePaint.setColor((int) argbEvaluator.evaluate(lastInterpolation, indicatorActiveColor,
                indicatorInactiveColor));
    }

    private void updateInactiveTitle() {
        titlePaint.setColor(indicatorInactiveColor);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, final int positionOffsetPixels) {
        if (onPageChangeListener != null) {
            onPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }

        if (!mIsSetIndexFromTabBar) {
            mIsResizeIn = position < mIndex;
            mLastIndex = mIndex;
            mIndex = position;

            mStartStripX =
                    (position * mTabSize) + (stripType == StripType.POINT ? mTabSize * 0.5F : 0.0F);
            mEndStripX = mStartStripX + mTabSize;
            updateIndicatorPosition(positionOffset);
        }

        if (!valueAnimator.isRunning() && mIsSetIndexFromTabBar) {
            mFraction = MIN_FRACTION;
            mIsSetIndexFromTabBar = false;
        }
    }

    @Override
    public void onPageSelected(final int position) {

    }

    @Override
    public void onPageScrollStateChanged(final int state) {
        scrollState = state;
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            if (onPageChangeListener != null) {
                onPageChangeListener.onPageSelected(mIndex);
            }
        }

        if (onPageChangeListener != null) {
            onPageChangeListener.onPageScrollStateChanged(state);
        }
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        final SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mIndex = savedState.index;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        final SavedState savedState = new SavedState(superState);
        savedState.index = mIndex;
        return savedState;
    }

    private static class SavedState extends BaseSavedState {

        private int index;

        SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        private SavedState(Parcel parcel) {
            super(parcel);
            index = parcel.readInt();
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            super.writeToParcel(parcel, flags);
            parcel.writeInt(index);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    protected void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        requestLayout();
        final int tempIndex = mIndex;
        deselect();
        post(new Runnable() {
            @Override
            public void run() {
                setTabIndex(tempIndex, true);
            }
        });
    }

    private class ResizeViewPagerScroller extends Scroller {

        ResizeViewPagerScroller(Context context) {
            super(context, new AccelerateDecelerateInterpolator());
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, indicatorAnimationDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            super.startScroll(startX, startY, dx, dy, indicatorAnimationDuration);
        }
    }

    private static class ResizeInterpolator implements Interpolator {
        private float factor;
        private boolean resize;

        void setFactor(final float factor) {
            this.factor = factor;
        }

        @Override
        public float getInterpolation(final float input) {
            if (resize) {
                return (float) (1.0F - Math.pow((1.0F - input), 2.0F * factor));
            } else {
                return (float) (Math.pow(input, 2.0F * factor));
            }
        }

        float loadResizeInterpolation(final float input, final boolean resizeIn) {
            resize = resizeIn;
            return getInterpolation(input);
        }
    }

    private enum StripType {
        LINE, POINT;
        private final static int LINE_INDEX = 0;
        private final static int POINT_INDEX = 1;
    }

    private enum StripGravity {
        TOP, BOTTOM;
        private final static int TOP_INDEX = 0;
        private final static int BOTTOM_INDEX = 1;
    }
}