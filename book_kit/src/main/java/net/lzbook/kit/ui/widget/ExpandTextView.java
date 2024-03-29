/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright 2014 Manabu Shimobe
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

package net.lzbook.kit.ui.widget;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dingyue.statistics.DyStatService;

import net.lzbook.kit.R;
import net.lzbook.kit.app.base.BaseBookApplication;
import net.lzbook.kit.pointpage.EventPoint;
import net.lzbook.kit.utils.AppUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 新壳TXT全本免费阅读 展开收起
 */
public class ExpandTextView extends RelativeLayout implements View.OnClickListener {

    private static final int MAX_COLLAPSED_LINES = 8;

    private static final int DEFAULT_ANIM_DURATION = 300;

    private static final float DEFAULT_ANIM_ALPHA_START = 0.7f;

    protected TextView mTv;

    protected ImageView mButton;

    private boolean mRelayout;

    private boolean mCollapsed = true;

    private int mCollapsedHeight;

    private int mTextHeightWithMaxLines;

    private int mMaxCollapsedLines;

    private int mMarginBetweenTxtAndBottom;

    private Drawable mExpandDrawable;

    private Drawable mCollapseDrawable;

    private int mAnimationDuration;

    private float mAnimAlphaStart;

    private boolean mAnimating;

    private SparseBooleanArray mCollapsedStatus;
    private int mPosition;

    String[] aa = new String[3];
    public ExpandTextView(Context context) {
        super(context);
    }

    public ExpandTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ExpandTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private static boolean isPostHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void applyAlphaAnimation(View view, float alpha) {
        if (isPostHoneycomb()) {
            view.setAlpha(alpha);
        } else {
            AlphaAnimation alphaAnimation = new AlphaAnimation(alpha, alpha);
            // make it instant
            alphaAnimation.setDuration(0);
            alphaAnimation.setFillAfter(true);
            view.startAnimation(alphaAnimation);
        }
    }

    private static int getRealTextViewHeight(TextView textView) {
        int textHeight = textView.getLayout().getLineTop(textView.getLineCount());
        int padding = textView.getCompoundPaddingTop() + textView.getCompoundPaddingBottom();
        return textHeight + padding;
    }

    @Override
    public void onClick(View view) {
        if (mButton.getVisibility() != View.VISIBLE || mTv.getLineCount() < 2) {
            return;
        }

        mCollapsed = !mCollapsed;
//        mButton.setText(mCollapsed ? "展开" : "收起");
//        mButton.setTextColor(Color.GRAY);
//        mButton.setCompoundDrawablesWithIntrinsicBounds(null, null, mCollapsed ? mExpandDrawable : mCollapseDrawable, null);
        try{
            mButton.setImageDrawable(mCollapsed ? mExpandDrawable : mCollapseDrawable);
        }catch (Throwable e){
            e.printStackTrace();
        }
        setCollapsedMarins();
        Map<String, String> data = new HashMap<>();

        if (mCollapsedStatus != null) {
            mCollapsedStatus.put(mPosition, mCollapsed);
        }
        if (mCollapsed) {
            data.put("type", "2");
        } else {
            data.put("type", "1");
        }
        DyStatService.onEvent(EventPoint.BOOOKDETAIL_INTRODUCTION, data);

        mAnimating = true;

        Animation animation;
        if (mCollapsed) {
            animation = new ExpandCollapseAnimation(this, getHeight(), mCollapsedHeight);
        } else {
            animation = new ExpandCollapseAnimation(this, getHeight(), getHeight() +
                    mTextHeightWithMaxLines - mTv.getHeight());
        }

        animation.setFillAfter(true);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                applyAlphaAnimation(mTv, mAnimAlphaStart);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                clearAnimation();
                mAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        clearAnimation();
        startAnimation(animation);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mAnimating;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        findViews();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!mRelayout || getVisibility() == View.GONE) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        mRelayout = false;

        mButton.setVisibility(View.GONE);
        mTv.setMaxLines(Integer.MAX_VALUE);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mTv.getLineCount() <= mMaxCollapsedLines) {
            return;
        }

        mTextHeightWithMaxLines = getRealTextViewHeight(mTv);

        if (mCollapsed) {
            mTv.setMaxLines(mMaxCollapsedLines);
        }
        mButton.setVisibility(View.VISIBLE);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mCollapsed) {
            mTv.post(new Runnable() {
                @Override
                public void run() {
                    mMarginBetweenTxtAndBottom = getHeight() - mTv.getHeight();
                }
            });
            mCollapsedHeight = getMeasuredHeight();
        }
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ExpandableTextView);
        mMaxCollapsedLines = typedArray.getInt(R.styleable.ExpandableTextView_maxCollapsedLines, MAX_COLLAPSED_LINES);
        mAnimationDuration = typedArray.getInt(R.styleable.ExpandableTextView_animDuration, DEFAULT_ANIM_DURATION);
        mAnimAlphaStart = typedArray.getFloat(R.styleable.ExpandableTextView_animAlphaStart, DEFAULT_ANIM_ALPHA_START);
        mExpandDrawable = typedArray.getDrawable(R.styleable.ExpandableTextView_expandDrawable);
        mCollapseDrawable = typedArray.getDrawable(R.styleable.ExpandableTextView_collapseDrawable);

        Resources resources = BaseBookApplication.getGlobalContext().getResources();
        if (mExpandDrawable == null) {
            try {
                int img_Id = resources.getIdentifier("icon_close_text", "drawable", AppUtils.getPackageName());
                mExpandDrawable = getResources().getDrawable(img_Id);
            }catch (Throwable e){
                e.printStackTrace();
            }
        }
        if (mCollapseDrawable == null) {
            try {
                int img_Id = resources.getIdentifier("icon_open_text", "drawable", AppUtils.getPackageName());
                mCollapseDrawable = getResources().getDrawable(img_Id);
            }catch (Throwable e){
                e.printStackTrace();
            }
        }

        typedArray.recycle();
    }

    private void findViews() {
        int textId=AppUtils.getResourceId(this.getContext(),"expandable_text");
        mTv = (TextView) findViewById(textId);
        mTv.setOnClickListener(this);
        int collapseId=AppUtils.getResourceId(this.getContext(),"expand_collapse");
        mButton = (ImageView) findViewById(collapseId);
//        mButton.setCompoundDrawablePadding(12);
//        mButton.setText(mCollapsed ? "展开" : "收起");
//        mButton.setTextColor(Color.GRAY);
//        mButton.setCompoundDrawablesWithIntrinsicBounds(null, null, mCollapsed ? mExpandDrawable : mCollapseDrawable, null);
       try {
           mButton.setImageDrawable(mCollapsed ? mExpandDrawable : mCollapseDrawable);
       }catch (Throwable e){
        e.printStackTrace();
       }
        setCollapsedMarins();
        mButton.setOnClickListener(this);
    }

    public void setText(CharSequence text, SparseBooleanArray collapsedStatus, int position) {
        mCollapsedStatus = collapsedStatus;
        mPosition = position;
        boolean isCollapsed = collapsedStatus.get(position, true);
        clearAnimation();
        mCollapsed = isCollapsed;
//        mButton.setText(mCollapsed ? "展开" : "收起");
//        mButton.setTextColor(Color.GRAY);
//        mButton.setCompoundDrawablesWithIntrinsicBounds(null, null, mCollapsed ? mExpandDrawable : mCollapseDrawable, null);
        try{
            mButton.setImageDrawable(mCollapsed ? mExpandDrawable : mCollapseDrawable);
        }catch (Throwable e){
            e.printStackTrace();
        }
        setCollapsedMarins();
        setText(text);
        getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        requestLayout();
    }

    private void setCollapsedMarins() {
//        if (!mCollapsed) {
////            ((RelativeLayout.LayoutParams) mButton.getLayoutParams()).addRule(TRUE);
//            ((RelativeLayout.LayoutParams) mButton.getLayoutParams()).addRule(RelativeLayout.BELOW, R.id.expandable_text);
//            mTv.setMaxLines(Integer.MAX_VALUE);
//        } else {
////            ((RelativeLayout.LayoutParams) mButton.getLayoutParams()).addRule(RelativeLayout.BELOW, TRUE);
//            ((RelativeLayout.LayoutParams) mButton.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//            ((RelativeLayout.LayoutParams) mButton.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//            mTv.setMaxLines(3);
//            mTv.setEllipsize(TextUtils.TruncateAt.END);
//        }
    }

    public CharSequence getText() {
        if (mTv == null) {
            return "";
        }
        return mTv.getText();
    }

    public void setText(CharSequence text) {
        mRelayout = true;
        SpannableStringBuilder spannableString = new SpannableStringBuilder();
        if(!"cc.quanben.novel".equals(AppUtils.getPackageName())){
            spannableString.append("简介：");
        }
        spannableString.append(text);
        if(!"cc.quanben.novel".equals(AppUtils.getPackageName())){
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#212832"));
            spannableString.setSpan(colorSpan, 0, 3, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        }
        mTv.setText(spannableString);
        setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
    }

    protected class ExpandCollapseAnimation extends Animation {
        private final View mTargetView;
        private final int mStartHeight;
        private final int mEndHeight;

        public ExpandCollapseAnimation(View view, int startHeight, int endHeight) {
            mTargetView = view;
            mStartHeight = startHeight;
            mEndHeight = endHeight;
            setDuration(mAnimationDuration);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            final int newHeight = (int) ((mEndHeight - mStartHeight) * interpolatedTime + mStartHeight);
            mTv.setMaxHeight(newHeight - mMarginBetweenTxtAndBottom);
            if (Float.compare(mAnimAlphaStart, 1.0f) != 0) {
                applyAlphaAnimation(mTv, mAnimAlphaStart + interpolatedTime * (1.0f - mAnimAlphaStart));
            }
            mTargetView.getLayoutParams().height = newHeight;
            mTargetView.requestLayout();
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }

    ;
}