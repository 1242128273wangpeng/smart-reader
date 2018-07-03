package com.intelligent.reader.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.intelligent.reader.R;

public class ExpandTextView extends RelativeLayout implements View.OnClickListener {

    private static final int MAX_COLLAPSED_LINES = 4;

    private static final int DEFAULT_ANIM_DURATION = 300;

    private static final float DEFAULT_ANIM_ALPHA_START = 0.7f;

    protected TextView contentView;

    protected TextView promptView;

    protected ImageView expandView;

    private boolean relayout;

    private boolean collapsed = true;

    private int contentCollapsedHeight;

    private int contentMaxLinesHeight;

    private int maxCollapsedLine;

    private int marginBetweenTxtAndBottom;

    private int mAnimationDuration;

    private float mAnimAlphaStart;

    private boolean animating;


    private int promptHeight;
    private int expandHeight;

    private int contentHeight;


    public ExpandTextView(Context context) {
        this(context, null);
    }

    public ExpandTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("CustomViewStyleable")
    public ExpandTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs,
                    R.styleable.ExpandTextVIew);

            maxCollapsedLine = typedArray.getInt(R.styleable.ExpandTextVIew_expandMaxLine,
                    MAX_COLLAPSED_LINES);
            mAnimationDuration = typedArray.getInt(R.styleable.ExpandTextVIew_expandAnimDuration,
                    DEFAULT_ANIM_DURATION);
            mAnimAlphaStart = typedArray.getFloat(R.styleable.ExpandTextVIew_expandAnimAlphaStart,
                    DEFAULT_ANIM_ALPHA_START);

            typedArray.recycle();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void applyAlphaAnimation(View view, float alpha) {
        view.setAlpha(alpha);
    }

    private static int getRealTextViewHeight(TextView textView) {
        int textHeight = textView.getLayout().getLineTop(textView.getLineCount());
        int padding = textView.getCompoundPaddingTop() + textView.getCompoundPaddingBottom();
        return textHeight + padding;
    }

    @Override
    public void onClick(View view) {
        if (contentView.getVisibility() != View.VISIBLE) {
            return;
        }

        if (view.getId() == R.id.expand_prompt) {
            if (collapsed) {
                handleClickListener();
            }
        } else if (view.getId() == R.id.expand_view){
            if (!collapsed) {
                handleClickListener();
            }
        }
    }

    private void handleClickListener() {
        animating = true;

        if (collapsed) {
            expandView.setVisibility(GONE);
            promptView.setVisibility(VISIBLE);
        } else {
            promptView.setVisibility(GONE);
            expandView.setVisibility(VISIBLE);
        }

        Animation animation;

        if (collapsed) {
            animation = new ExpandCollapseAnimation(this, getHeight(), getHeight() + contentMaxLinesHeight - contentView.getHeight());
        } else {
            animation = new ExpandCollapseAnimation(this, getHeight(), contentCollapsedHeight);
        }

        animation.setFillAfter(true);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                applyAlphaAnimation(contentView, mAnimAlphaStart);
                if (collapsed) {
                    promptView.setVisibility(GONE);
                } else {
                    expandView.setVisibility(GONE);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                clearAnimation();

                if (collapsed) {
                    expandView.setVisibility(VISIBLE);

                    ViewGroup.LayoutParams layoutParams = ExpandTextView.this.getLayoutParams();
                    layoutParams.height = contentHeight + expandHeight;

                    requestLayout();
                } else {
                    promptView.setVisibility(VISIBLE);
                }

                animating = false;

                collapsed = !collapsed;
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
        return animating;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initViews();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!relayout || getVisibility() == View.GONE) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        relayout = false;

        measureChild(promptView, widthMeasureSpec, heightMeasureSpec);
        promptHeight = promptView.getMeasuredHeight();

        measureChild(expandView, widthMeasureSpec, heightMeasureSpec);
        expandHeight = expandView.getMeasuredHeight();

        contentView.setMaxLines(Integer.MAX_VALUE);

        expandView.setVisibility(View.GONE);
        promptView.setVisibility(View.GONE);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (contentView.getLineCount() <= maxCollapsedLine) {
            return;
        }

        measureChild(contentView, widthMeasureSpec, heightMeasureSpec);
        contentHeight = contentView.getMeasuredHeight();

        contentMaxLinesHeight = getRealTextViewHeight(contentView);

        collapsed = contentView.getLineCount() > maxCollapsedLine;

        if (collapsed) {
            contentView.setMaxLines(maxCollapsedLine);
            expandView.setVisibility(GONE);
            promptView.setVisibility(VISIBLE);
        } else {
            expandView.setVisibility(GONE);
            promptView.setVisibility(VISIBLE);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (collapsed) {
            contentView.post(new Runnable() {
                @Override
                public void run() {
                    marginBetweenTxtAndBottom = getHeight() - contentView.getHeight();
                }
            });

            contentCollapsedHeight = getMeasuredHeight();
        }
    }

    private void initViews() {
        contentView = findViewById(R.id.expand_content);

        expandView =  findViewById(R.id.expand_view);
        expandView.setOnClickListener(this);

        promptView = findViewById(R.id.expand_prompt);
        promptView.setOnClickListener(this);
    }

    public void setText(CharSequence text) {
        relayout = true;
        contentView.setText(text);
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
            contentView.setMaxHeight(newHeight - marginBetweenTxtAndBottom);
            if (Float.compare(mAnimAlphaStart, 1.0f) != 0) {
                applyAlphaAnimation(contentView, mAnimAlphaStart + interpolatedTime * (1.0f - mAnimAlphaStart));
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
}