/*
 * Copyright 2015 lujun
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intelligent.reader.view;

import static net.lzbook.kit.utils.AppUtils.dp2px;
import static net.lzbook.kit.utils.AppUtils.sp2px;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.DrawableRes;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.ding.basic.bean.HotWordBean;
import com.intelligent.reader.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Desc: 自动换行推荐热词容器
 * Author: lujun(http://blog.lujun.co)
 * Date: 2015-12-30 17:14
 */
public class TagContainerLayout extends ViewGroup {

    /**
     * Vertical interval, default 5(dp)
     */
    private int mVerticalInterval;

    /**
     * Horizontal interval, default 5(dp)
     */
    private int mHorizontalInterval;

    /**
     * TagContainerLayout border width(default 0.5dp)
     */
    private float mBorderWidth = 0.1f;

    /**
     * TagContainerLayout border radius(default 10.0dp)
     */
    private float mBorderRadius = 10.0f;

    /**
     * The sensitive of the ViewDragHelper(default 1.0f, normal)
     */
    private float mSensitivity = 1.0f;

    /**
     * TagView average height
     */
    private int mChildHeight;

    /**
     * TagContainerLayout border color(default #22FF0000)
     */
    private int mBorderColor = Color.parseColor("#22FF0000");

    /**
     * TagContainerLayout background color(default #11FF0000)
     */
    private int mBackgroundColor = Color.parseColor("#11FF0000");

    /**
     * The container layout gravity(default left)
     */
    private int mGravity = Gravity.LEFT;

    /**
     * The max line count of TagContainerLayout
     */
    private int mMaxLines = 0;

    /**
     * The max length for TagView(default max length 23)
     */
    private int mTagMaxLength = 10;

    /**
     * TagView Border width(default 0.5dp)
     */
    private float mTagBorderWidth = 0.1f;

    /**
     * TagView Border radius(default 15.0dp)
     */
    private float mTagBorderRadius = 5.0f;

    /**
     * TagView Text size(default 14sp)
     */
    private float mTagTextSize = 14;

    /**
     * Text direction(support:TEXT_DIRECTION_RTL & TEXT_DIRECTION_LTR, default TEXT_DIRECTION_LTR)
     */
    private int mTagTextDirection = View.TEXT_DIRECTION_LTR;

    /**
     * Horizontal padding for TagView, include left & right padding(left & right padding are equal,
     * default 10dp)
     */
    private int mTagHorizontalPadding = 15;

    /**
     * Vertical padding for TagView, include top & bottom padding(top & bottom padding are equal,
     * default 8dp)
     */
    private int mTagVerticalPadding = 6;

    /**
     * TagView border color(default #BCBCBC)
     */
    private int mTagBorderColor = Color.parseColor("#BCBCBC");

    /**
     * TagView background color(default #33F44336)
     */
    private int mTagBackgroundColor = Color.parseColor("#33F44336");

    /**
     * TagView text color(default #303135)
     */
    private int mTagTextColor = Color.parseColor("#303135");

    /**
     * TagView typeface
     */
    private Typeface mTagTypeface = Typeface.DEFAULT;

    /**
     * Whether TagView can clickable(default unclickable)
     */
    private boolean isTagViewClickable;

    /**
     * Tags
     */
    private List<HotWordBean> mTags;

    /**
     * Can drag TagView(default false)
     */
    private boolean mDragEnable;

    /**
     * TagView drag state(default STATE_IDLE)
     */
    private int mTagViewState = ViewDragHelper.STATE_IDLE;

    /**
     * The distance between baseline and descent(default 2.75dp)
     */
    private float mTagBdDistance = 2.75f;

    /**
     * OnTagClickListener for TagView
     */
    private TagView.OnTagClickListener mOnTagClickListener;

    /**
     * Whether to support 'letters show with RTL(eg: Android to diordnA)' style(default false)
     */
    private boolean mTagSupportLettersRTL = false;

    private Paint mPaint;

    private RectF mRectF;

    private ViewDragHelper mViewDragHelper;

    private List<View> mChildViews;

    private int[] mViewPos;

    /**
     * Default interval(dp)
     */
    private static final float DEFAULT_INTERVAL = 20;

    /**
     * Default tag min length
     */
    private static final int TAG_MIN_LENGTH = 3;

    /**
     * The ripple effect duration(In milliseconds, default 1000ms)
     */
    private int mRippleDuration = 1000;

    /**
     * The ripple effect color(default #EEEEEE)
     */
    private int mRippleColor;

    /**
     * The ripple effect color alpha(the value may between 0 - 255, default 128)
     */
    private int mRippleAlpha = 128;

    /**
     * Enable draw cross icon(default false)
     */
    private boolean mEnableCross = false;

    /**
     * The cross area width(your cross click area, default equal to the TagView's height)
     */
    private float mCrossAreaWidth = 0.0f;

    /**
     * The padding of the cross area(default 10dp)
     */
    private float mCrossAreaPadding = 10.0f;

    /**
     * The cross icon color(default Color.BLACK)
     */
    private int mCrossColor = Color.BLACK;

    /**
     * The cross line width(default 1dp)
     */
    private float mCrossLineWidth = 1.0f;

    /**
     * TagView background resource
     */
    private int mTagBackgroundResource;

    /**
     * TagView max line list
     */
    private int mTagLineMax;

    public TagContainerLayout(Context context) {
        this(context, null);
    }

    public TagContainerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TagContainerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.AndroidTagView,
                defStyleAttr, 0);
        mVerticalInterval = (int) attributes.getDimension(R.styleable.AndroidTagView_vertical_interval,
                dp2px(context.getResources(), DEFAULT_INTERVAL));
        mHorizontalInterval = (int) attributes.getDimension(R.styleable.AndroidTagView_horizontal_interval,
                dp2px(context.getResources(), DEFAULT_INTERVAL));
        mBorderWidth = attributes.getDimension(R.styleable.AndroidTagView_container_border_width,
                dp2px(context.getResources(), mBorderWidth));
        mBorderRadius = attributes.getDimension(R.styleable.AndroidTagView_container_border_radius,
                dp2px(context.getResources(), mBorderRadius));
        mTagBdDistance = attributes.getDimension(R.styleable.AndroidTagView_tag_bd_distance,
                dp2px(context.getResources(), mTagBdDistance));
        mBorderColor = attributes.getColor(R.styleable.AndroidTagView_container_border_color,
                mBorderColor);
        mBackgroundColor = attributes.getColor(R.styleable.AndroidTagView_container_background_color,
                mBackgroundColor);
        mDragEnable = attributes.getBoolean(R.styleable.AndroidTagView_container_enable_drag, false);
        mSensitivity = attributes.getFloat(R.styleable.AndroidTagView_container_drag_sensitivity,
                mSensitivity);
        mGravity = attributes.getInt(R.styleable.AndroidTagView_container_gravity, mGravity);
        mMaxLines = attributes.getInt(R.styleable.AndroidTagView_container_max_lines, mMaxLines);
        mTagMaxLength = attributes.getInt(R.styleable.AndroidTagView_tag_max_length, mTagMaxLength);
        mTagBorderWidth = attributes.getDimension(R.styleable.AndroidTagView_tag_border_width,
                dp2px(context.getResources(), mTagBorderWidth));
        mTagBorderRadius = attributes.getDimension(
                R.styleable.AndroidTagView_tag_corner_radius, dp2px(context.getResources(), mTagBorderRadius));
        mTagHorizontalPadding = (int) attributes.getDimension(
                R.styleable.AndroidTagView_tag_horizontal_padding,
                dp2px(context.getResources(), mTagHorizontalPadding));
        mTagVerticalPadding = (int) attributes.getDimension(
                R.styleable.AndroidTagView_tag_vertical_padding, dp2px(context.getResources(), mTagVerticalPadding));
        mTagTextSize = attributes.getDimension(R.styleable.AndroidTagView_tag_text_size,
                sp2px(context.getResources(), mTagTextSize));
        mTagBorderColor = attributes.getColor(R.styleable.AndroidTagView_tag_border_color,
                mTagBorderColor);
        mTagBackgroundColor = attributes.getColor(R.styleable.AndroidTagView_tag_background_color,
                mTagBackgroundColor);
        mTagTextColor = attributes.getColor(R.styleable.AndroidTagView_tag_text_color, mTagTextColor);
        mTagTextDirection = attributes.getInt(R.styleable.AndroidTagView_tag_text_direction, mTagTextDirection);
        isTagViewClickable = attributes.getBoolean(R.styleable.AndroidTagView_tag_clickable, false);
        mRippleColor = attributes.getColor(R.styleable.AndroidTagView_tag_ripple_color, Color.parseColor("#EEEEEE"));
        mRippleAlpha = attributes.getInteger(R.styleable.AndroidTagView_tag_ripple_alpha, mRippleAlpha);
        mRippleDuration = attributes.getInteger(R.styleable.AndroidTagView_tag_ripple_duration, mRippleDuration);
        mEnableCross = attributes.getBoolean(R.styleable.AndroidTagView_tag_enable_cross, mEnableCross);
        mCrossAreaWidth = attributes.getDimension(R.styleable.AndroidTagView_tag_cross_width,
                dp2px(context.getResources(), mCrossAreaWidth));
        mCrossAreaPadding = attributes.getDimension(R.styleable.AndroidTagView_tag_cross_area_padding,
                dp2px(context.getResources(), mCrossAreaPadding));
        mCrossColor = attributes.getColor(R.styleable.AndroidTagView_tag_cross_color, mCrossColor);
        mCrossLineWidth = attributes.getDimension(R.styleable.AndroidTagView_tag_cross_line_width,
                dp2px(context.getResources(), mCrossLineWidth));
        mTagSupportLettersRTL = attributes.getBoolean(R.styleable.AndroidTagView_tag_support_letters_rlt,
                mTagSupportLettersRTL);
        mTagBackgroundResource = attributes.getResourceId(R.styleable.AndroidTagView_tag_background,
                mTagBackgroundResource);
        attributes.recycle();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRectF = new RectF();
        mChildViews = new ArrayList<View>();
        mViewDragHelper = ViewDragHelper.create(this, mSensitivity, new DragHelperCallBack());
        setWillNotDraw(false);
        setTagMaxLength(mTagMaxLength);
        setTagHorizontalPadding(mTagHorizontalPadding);
        setTagVerticalPadding(mTagVerticalPadding);

        if (isInEditMode()) {
//            addTag("sample tag");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        measureChildren(widthMeasureSpec, heightMeasureSpec);
        final int childCount = getChildCount();
        int lines = childCount == 0 ? 0 : getChildLines(childCount);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
//        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

        if (childCount == 0) {
            setMeasuredDimension(0, 0);
        } else if (heightSpecMode == MeasureSpec.AT_MOST
                || heightSpecMode == MeasureSpec.UNSPECIFIED) {
//            setMeasuredDimension(widthSpecSize, (mVerticalInterval + mChildHeight) * lines
//                    - mVerticalInterval + getPaddingTop() + getPaddingBottom());
            setMeasuredDimension(widthSpecSize, (mVerticalInterval + mChildHeight) * mTagLineMax
                    - mVerticalInterval + getPaddingTop() + getPaddingBottom());
        } else {
            setMeasuredDimension(widthSpecSize, heightSpecSize);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRectF.set(0, 0, w, h);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount;
        if ((childCount = getChildCount()) <= 0) {
            return;
        }
        int availableW = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int curRight = getMeasuredWidth() - getPaddingRight();
        int curTop = getPaddingTop();
        int curLeft = getPaddingLeft();
        int sPos = 0;
        mViewPos = new int[childCount * 2];
        int childViewHeight = 0;

        for (int i = 0; i < childCount; i++) {
            final View childView = getChildAt(i);
            if (childView.getVisibility() != GONE) {
                int width = childView.getMeasuredWidth();
                childViewHeight = childView.getMeasuredHeight();
                if (mGravity == Gravity.RIGHT) {
                    if (curRight - width < getPaddingLeft()) {
                        curRight = getMeasuredWidth() - getPaddingRight();
                        curTop += mChildHeight + mVerticalInterval;
                    }
                    mViewPos[i * 2] = curRight - width;
                    mViewPos[i * 2 + 1] = curTop;
                    curRight -= width + mHorizontalInterval;
                } else if (mGravity == Gravity.CENTER) {
                    if (curLeft + width - getPaddingLeft() > availableW) {
                        int leftW = getMeasuredWidth() - mViewPos[(i - 1) * 2]
                                - getChildAt(i - 1).getMeasuredWidth() - getPaddingRight();
                        for (int j = sPos; j < i; j++) {
                            mViewPos[j * 2] = mViewPos[j * 2] + leftW / 2;
                        }
                        sPos = i;
                        curLeft = getPaddingLeft();
                        curTop += mChildHeight + mVerticalInterval;
                    }
                    mViewPos[i * 2] = curLeft;
                    mViewPos[i * 2 + 1] = curTop;
                    curLeft += width + mHorizontalInterval;

                    if (i == childCount - 1) {
                        int leftW = getMeasuredWidth() - mViewPos[i * 2]
                                - childView.getMeasuredWidth() - getPaddingRight();
                        for (int j = sPos; j < childCount; j++) {
                            mViewPos[j * 2] = mViewPos[j * 2] + leftW / 2;
                        }
                    }
                } else {
                    if (curLeft + width - getPaddingLeft() > availableW) {
                        curLeft = getPaddingLeft();
                        curTop += mChildHeight + mVerticalInterval;
                    }
                    mViewPos[i * 2] = curLeft;
                    mViewPos[i * 2 + 1] = curTop;
                    curLeft += width + mHorizontalInterval;
                }
            }
        }

        // layout all child views
        for (int i = 0; i < mViewPos.length / 2; i++) {
            View childView = getChildAt(i);
            if (mViewPos[i * 2 + 1] > (childViewHeight * mTagLineMax)) {
                break;
            }
            childView.layout(mViewPos[i * 2], mViewPos[i * 2 + 1],
                    mViewPos[i * 2] + childView.getMeasuredWidth(),
                    mViewPos[i * 2 + 1] + mChildHeight);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        mPaint.setStyle(Paint.Style.FILL);
//        mPaint.setColor(mBackgroundColor);
//        canvas.drawRoundRect(mRectF, mBorderRadius, mBorderRadius, mPaint);
//
//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setStrokeWidth(mBorderWidth);
//        mPaint.setColor(mBorderColor);
//        canvas.drawRoundRect(mRectF, mBorderRadius, mBorderRadius, mPaint);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mViewDragHelper.continueSettling(true)) {
            requestLayout();
        }
    }

    private int getChildLines(int childCount) {
        int availableW = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int lines = 1;
        for (int i = 0, curLineW = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            int dis = childView.getMeasuredWidth() + mHorizontalInterval;
            int height = childView.getMeasuredHeight();
            mChildHeight = i == 0 ? height : Math.min(mChildHeight, height);
            curLineW += dis;
            if (curLineW - mHorizontalInterval > availableW) {
                lines++;
                curLineW = dis;
            }
        }

        return mMaxLines <= 0 ? lines : mMaxLines;
    }

    private void onSetTag() {
        if (mTags == null) {
            throw new RuntimeException("NullPointer exception!");
        }
        removeAllTags();
        if (mTags.size() == 0) {
            return;
        }
        for (int i = 0; i < mTags.size(); i++) {
            onAddTag(mTags.get(i), mChildViews.size());
        }
        postInvalidate();
    }

    private void onAddTag(HotWordBean text, int position) {
        if (position < 0 || position > mChildViews.size()) {
            throw new RuntimeException("Illegal position!");
        }
        TagView tagView = new TagView(getContext(), text);
        initTagView(tagView);
        mChildViews.add(position, tagView);
        if (position < mChildViews.size()) {
            for (int i = position; i < mChildViews.size(); i++) {
                mChildViews.get(i).setTag(i);
            }
        } else {
            tagView.setTag(position);
        }
        addView(tagView, position);
    }

    private void initTagView(TagView tagView) {

        tagView.setTagMaxLength(mTagMaxLength);
        tagView.setTextDirection(mTagTextDirection);
        tagView.setTypeface(mTagTypeface);
        tagView.setBorderWidth(mTagBorderWidth);
        tagView.setBorderRadius(mTagBorderRadius);
        tagView.setTagBorderColor(mTagBorderColor);
        tagView.setTextSize(mTagTextSize);
        tagView.setHorizontalPadding(mTagHorizontalPadding);
        tagView.setVerticalPadding(mTagVerticalPadding);
        tagView.setIsViewClickable(isTagViewClickable);
        tagView.setBdDistance(mTagBdDistance);
        tagView.setOnTagClickListener(mOnTagClickListener);
        tagView.setRippleAlpha(mRippleAlpha);
        tagView.setRippleColor(mRippleColor);
        tagView.setRippleDuration(mRippleDuration);
        tagView.setEnableCross(mEnableCross);
        tagView.setCrossAreaWidth(mCrossAreaWidth);
        tagView.setCrossAreaPadding(mCrossAreaPadding);
        tagView.setCrossColor(mCrossColor);
        tagView.setCrossLineWidth(mCrossLineWidth);
        tagView.setTagSupportLettersRTL(mTagSupportLettersRTL);
        tagView.setBackgroundResource(mTagBackgroundResource);
        tagView.setTagTextColor(mTagTextColor);
    }

    private void invalidateTags() {
        for (View view : mChildViews) {
            final TagView tagView = (TagView) view;
            tagView.setOnTagClickListener(mOnTagClickListener);
        }
    }

    private void onRemoveTag(int position) {
        if (position < 0 || position >= mChildViews.size()) {
            throw new RuntimeException("Illegal position!");
        }
        mChildViews.remove(position);
        removeViewAt(position);
        for (int i = position; i < mChildViews.size(); i++) {
            mChildViews.get(i).setTag(i);
        }
        // TODO, make removed view null?
    }

    private int[] onGetNewPosition(View view) {
        int left = view.getLeft();
        int top = view.getTop();
        int bestMatchLeft = mViewPos[(int) view.getTag() * 2];
        int bestMatchTop = mViewPos[(int) view.getTag() * 2 + 1];
        int tmpTopDis = Math.abs(top - bestMatchTop);
        for (int i = 0; i < mViewPos.length / 2; i++) {
            if (Math.abs(top - mViewPos[i * 2 + 1]) < tmpTopDis) {
                bestMatchTop = mViewPos[i * 2 + 1];
                tmpTopDis = Math.abs(top - mViewPos[i * 2 + 1]);
            }
        }
        int rowChildCount = 0;
        int tmpLeftDis = 0;
        for (int i = 0; i < mViewPos.length / 2; i++) {
            if (mViewPos[i * 2 + 1] == bestMatchTop) {
                if (rowChildCount == 0) {
                    bestMatchLeft = mViewPos[i * 2];
                    tmpLeftDis = Math.abs(left - bestMatchLeft);
                } else {
                    if (Math.abs(left - mViewPos[i * 2]) < tmpLeftDis) {
                        bestMatchLeft = mViewPos[i * 2];
                        tmpLeftDis = Math.abs(left - bestMatchLeft);
                    }
                }
                rowChildCount++;
            }
        }
        return new int[]{bestMatchLeft, bestMatchTop};
    }

    private int onGetCoordinateReferPos(int left, int top) {
        int pos = 0;
        for (int i = 0; i < mViewPos.length / 2; i++) {
            if (left == mViewPos[i * 2] && top == mViewPos[i * 2 + 1]) {
                pos = i;
            }
        }
        return pos;
    }

    private void onChangeView(View view, int newPos, int originPos) {
        mChildViews.remove(originPos);
        mChildViews.add(newPos, view);
        for (View child : mChildViews) {
            child.setTag(mChildViews.indexOf(child));
        }

        removeViewAt(originPos);
        addView(view, newPos);
    }

    private int ceilTagBorderWidth() {
        return (int) Math.ceil(mTagBorderWidth);
    }

    private class DragHelperCallBack extends ViewDragHelper.Callback {

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
            mTagViewState = state;
        }

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            requestDisallowInterceptTouchEvent(true);
            return mDragEnable;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            final int leftX = getPaddingLeft();
            final int rightX = getWidth() - child.getWidth() - getPaddingRight();
            return Math.min(Math.max(left, leftX), rightX);
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            final int topY = getPaddingTop();
            final int bottomY = getHeight() - child.getHeight() - getPaddingBottom();
            return Math.min(Math.max(top, topY), bottomY);
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return getMeasuredWidth() - child.getMeasuredWidth();
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return getMeasuredHeight() - child.getMeasuredHeight();
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            requestDisallowInterceptTouchEvent(false);
            int[] pos = onGetNewPosition(releasedChild);
            int posRefer = onGetCoordinateReferPos(pos[0], pos[1]);
            onChangeView(releasedChild, posRefer, (int) releasedChild.getTag());
            mViewDragHelper.settleCapturedViewAt(pos[0], pos[1]);
            invalidate();
        }
    }

    /**
     * Get current drag view state.
     */
    public int getTagViewState() {
        return mTagViewState;
    }

    /**
     * Get TagView text baseline and descent distance.
     */
    public float getTagBdDistance() {
        return mTagBdDistance;
    }

    /**
     * Set TagView text baseline and descent distance.
     */
    public void setTagBdDistance(float tagBdDistance) {
        this.mTagBdDistance = dp2px(getContext().getResources(), tagBdDistance);
    }

    /**
     * Set tags
     */
    public void setTags(List<HotWordBean> tags) {
        mTags = tags;
        onSetTag();
    }

    /**
     * Set tags with own color
     */
    public void setTags(List<HotWordBean> tags, List<int[]> colorArrayList) {
        mTags = tags;
        onSetTag();
    }

    /**
     * Inserts the specified TagView into this ContainerLayout at the end.
     */
    public void addTag(HotWordBean text) {
        addTag(text, mChildViews.size());
    }

    /**
     * Inserts the specified TagView into this ContainerLayout at the specified location.
     * The TagView is inserted before the current element at the specified location.
     */
    public void addTag(HotWordBean text, int position) {
        onAddTag(text, position);
        postInvalidate();
    }

    /**
     * Remove a TagView in specified position.
     */
    public void removeTag(int position) {
        onRemoveTag(position);
        postInvalidate();
    }

    /**
     * Remove all TagViews.
     */
    public void removeAllTags() {
        mChildViews.clear();
        removeAllViews();
        postInvalidate();
    }

    /**
     * Set OnTagClickListener for TagView.
     */
    public void setOnTagClickListener(TagView.OnTagClickListener listener) {
        mOnTagClickListener = listener;
        invalidateTags();
    }

    /**
     * Get TagView text.
     */
    public String getTagText(int position) {
        return ((TagView) mChildViews.get(position)).getText();
    }

    /**
     * Get a string list for all tags in TagContainerLayout.
     */
    public List<String> getTags() {
        List<String> tmpList = new ArrayList<String>();
        for (View view : mChildViews) {
            if (view instanceof TagView) {
                tmpList.add(((TagView) view).getText());
            }
        }
        return tmpList;
    }

    /**
     * Set whether the child view can be dragged.
     */
    public void setDragEnable(boolean enable) {
        this.mDragEnable = enable;
    }

    /**
     * Get current view is drag enable attribute.
     */
    public boolean getDragEnable() {
        return mDragEnable;
    }

    /**
     * Set vertical interval
     */
    public void setVerticalInterval(float interval) {
        mVerticalInterval = (int) dp2px(getContext().getResources(), interval);
        postInvalidate();
    }

    /**
     * Get vertical interval in this view.
     */
    public int getVerticalInterval() {
        return mVerticalInterval;
    }

    /**
     * Set horizontal interval.
     */
    public void setHorizontalInterval(float interval) {
        mHorizontalInterval = (int) dp2px(getContext().getResources(), interval);
        postInvalidate();
    }

    /**
     * Get horizontal interval in this view.
     */
    public int getHorizontalInterval() {
        return mHorizontalInterval;
    }

    /**
     * Get TagContainerLayout border width.
     */
    public float getBorderWidth() {
        return mBorderWidth;
    }

    /**
     * Set TagContainerLayout border width.
     */
    public void setBorderWidth(float width) {
        this.mBorderWidth = width;
    }

    /**
     * Get TagContainerLayout border radius.
     */
    public float getBorderRadius() {
        return mBorderRadius;
    }

    /**
     * Set TagContainerLayout border radius.
     */
    public void setBorderRadius(float radius) {
        this.mBorderRadius = radius;
    }

    /**
     * Get TagContainerLayout border color.
     */
    public int getBorderColor() {
        return mBorderColor;
    }

    /**
     * Set TagContainerLayout border color.
     */
    public void setBorderColor(int color) {
        this.mBorderColor = color;
    }

    /**
     * Get TagContainerLayout background color.
     */
    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    /**
     * Set TagContainerLayout background color.
     */
    @Override
    public void setBackgroundColor(int color) {
        this.mBackgroundColor = color;
    }

    /**
     * Get container layout gravity.
     */
    public int getGravity() {
        return mGravity;
    }

    /**
     * Set container layout gravity.
     */
    public void setGravity(int gravity) {
        this.mGravity = gravity;
    }

    /**
     * Get TagContainerLayout ViewDragHelper sensitivity.
     */
    public float getSensitivity() {
        return mSensitivity;
    }

    /**
     * Set TagContainerLayout ViewDragHelper sensitivity.
     */
    public void setSensitivity(float sensitivity) {
        this.mSensitivity = sensitivity;
    }

    /**
     * Set max line count for TagContainerLayout
     *
     * @param maxLines max line count
     */
    public void setMaxLines(int maxLines) {
        mMaxLines = maxLines;
        postInvalidate();
    }

    /**
     * Get TagContainerLayout's max lines
     *
     * @return maxLines
     */
    public int getMaxLines() {
        return mMaxLines;
    }

    /**
     * Set the TagView text max length(must greater or equal to 3).
     */
    public void setTagMaxLength(int maxLength) {
        mTagMaxLength = maxLength < TAG_MIN_LENGTH ? TAG_MIN_LENGTH : maxLength;
    }

    /**
     * Get TagView max length.
     */
    public int getTagMaxLength() {
        return mTagMaxLength;
    }

    /**
     * Get TagView is clickable.
     */
    public boolean getIsTagViewClickable() {
        return isTagViewClickable;
    }

    /**
     * Set TagView is clickable
     */
    public void setIsTagViewClickable(boolean clickable) {
        this.isTagViewClickable = clickable;
    }

    /**
     * Get TagView border width.
     */
    public float getTagBorderWidth() {
        return mTagBorderWidth;
    }

    /**
     * Set TagView border width.
     */
    public void setTagBorderWidth(float width) {
        this.mTagBorderWidth = width;
    }

    /**
     * Get TagView border radius.
     */
    public float getTagBorderRadius() {
        return mTagBorderRadius;
    }

    /**
     * Set TagView border radius.
     */
    public void setTagBorderRadius(float radius) {
        this.mTagBorderRadius = radius;
    }

    /**
     * Get TagView text size.
     */
    public float getTagTextSize() {
        return mTagTextSize;
    }

    /**
     * Set TagView text size.
     */
    public void setTagTextSize(float size) {
        this.mTagTextSize = size;
    }

    /**
     * Get TagView horizontal padding.
     */
    public int getTagHorizontalPadding() {
        return mTagHorizontalPadding;
    }

    /**
     * Set TagView horizontal padding.
     */
    public void setTagHorizontalPadding(int padding) {
        int ceilWidth = ceilTagBorderWidth();
        this.mTagHorizontalPadding = padding < ceilWidth ? ceilWidth : padding;
    }

    /**
     * Get TagView vertical padding.
     */
    public int getTagVerticalPadding() {
        return mTagVerticalPadding;
    }

    /**
     * Set TagView vertical padding.
     */
    public void setTagVerticalPadding(int padding) {
        int ceilWidth = ceilTagBorderWidth();
        this.mTagVerticalPadding = padding < ceilWidth ? ceilWidth : padding;
    }

    /**
     * Get TagView border color.
     */
    public int getTagBorderColor() {
        return mTagBorderColor;
    }

    /**
     * Set TagView border color.
     */
    public void setTagBorderColor(int color) {
        this.mTagBorderColor = color;
    }

    /**
     * Get TagView background color.
     */
    public int getTagBackgroundColor() {
        return mTagBackgroundColor;
    }

    /**
     * Set TagView background color.
     */
    public void setTagBackgroundColor(int color) {
        this.mTagBackgroundColor = color;
    }

    /**
     * Get TagView text color.
     */
    public int getTagTextColor() {
        return mTagTextColor;
    }

    /**
     * Set tag text direction, support:View.TEXT_DIRECTION_RTL and View.TEXT_DIRECTION_LTR,
     * default View.TEXT_DIRECTION_LTR
     */
    public void setTagTextDirection(int textDirection) {
        this.mTagTextDirection = textDirection;
    }

    /**
     * Get TagView typeface.
     */
    public Typeface getTagTypeface() {
        return mTagTypeface;
    }

    /**
     * Set TagView typeface.
     */
    public void setTagTypeface(Typeface typeface) {
        this.mTagTypeface = typeface;
    }

    /**
     * Get tag text direction
     */
    public int getTagTextDirection() {
        return mTagTextDirection;
    }

    /**
     * Set TagView text color.
     */
    public void setTagTextColor(int color) {
        this.mTagTextColor = color;
    }

    /**
     * Get the ripple effect color's alpha.
     */
    public int getRippleAlpha() {
        return mRippleAlpha;
    }

    /**
     * Set TagView ripple effect alpha, the value may between 0 to 255, default is 128.
     */
    public void setRippleAlpha(int mRippleAlpha) {
        this.mRippleAlpha = mRippleAlpha;
    }

    /**
     * Get the ripple effect color.
     */
    public int getRippleColor() {
        return mRippleColor;
    }

    /**
     * Set TagView ripple effect color.
     */
    public void setRippleColor(int mRippleColor) {
        this.mRippleColor = mRippleColor;
    }

    /**
     * Get the ripple effect duration.
     */
    public int getRippleDuration() {
        return mRippleDuration;
    }

    /**
     * Set TagView ripple effect duration, default is 1000ms.
     */
    public void setRippleDuration(int mRippleDuration) {
        this.mRippleDuration = mRippleDuration;
    }

    /**
     * Set TagView cross color.
     */
    public int getCrossColor() {
        return mCrossColor;
    }

    /**
     * Set TagView cross color, default Color.BLACK.
     */
    public void setCrossColor(int mCrossColor) {
        this.mCrossColor = mCrossColor;
    }

    /**
     * Get agView cross area's padding.
     */
    public float getCrossAreaPadding() {
        return mCrossAreaPadding;
    }

    /**
     * Set TagView cross area padding, default 10dp.
     */
    public void setCrossAreaPadding(float mCrossAreaPadding) {
        this.mCrossAreaPadding = mCrossAreaPadding;
    }

    public void setTagLineMax(int num) {
        this.mTagLineMax = num;
    }

    /**
     * Get is the TagView's cross enable, default false.
     */
    public boolean isEnableCross() {
        return mEnableCross;
    }

    /**
     * Enable or disable the TagView's cross.
     */
    public void setEnableCross(boolean mEnableCross) {
        this.mEnableCross = mEnableCross;
    }

    /**
     * Get TagView cross area width.
     */
    public float getCrossAreaWidth() {
        return mCrossAreaWidth;
    }

    /**
     * Set TagView area width.
     */
    public void setCrossAreaWidth(float mCrossAreaWidth) {
        this.mCrossAreaWidth = mCrossAreaWidth;
    }

    /**
     * Get TagView cross line width.
     */
    public float getCrossLineWidth() {
        return mCrossLineWidth;
    }

    /**
     * Set TagView cross line width, default 1dp.
     */
    public void setCrossLineWidth(float mCrossLineWidth) {
        this.mCrossLineWidth = mCrossLineWidth;
    }

    /**
     * Get the 'letters show with RTL(like: Android to diordnA)' style if it's enabled
     */
    public boolean isTagSupportLettersRTL() {
        return mTagSupportLettersRTL;
    }

    /**
     * Set whether the 'support letters show with RTL(like: Android to diordnA)' style is enabled.
     */
    public void setTagSupportLettersRTL(boolean mTagSupportLettersRTL) {
        this.mTagSupportLettersRTL = mTagSupportLettersRTL;
    }

    /**
     * Get TagView in specified position.
     *
     * @param position the position of the TagView
     */
    public TagView getTagView(int position) {
        if (position < 0 || position >= mChildViews.size()) {
            throw new RuntimeException("Illegal position!");
        }
        return (TagView) mChildViews.get(position);
    }

    /**
     * Get TagView background resource
     */
    public int getTagBackgroundResource() {
        return mTagBackgroundResource;
    }

    /**
     * Set TagView background resource
     */
    public void setTagBackgroundResource(@DrawableRes int tagBackgroundResource) {
        this.mTagBackgroundResource = tagBackgroundResource;
    }
}
