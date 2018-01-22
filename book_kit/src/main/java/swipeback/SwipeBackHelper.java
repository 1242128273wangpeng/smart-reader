package swipeback;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import net.lzbook.kit.R;
import net.lzbook.kit.utils.AppLog;

import org.jetbrains.annotations.NotNull;

import iyouqu.theme.FrameActivity;

/**
 * Desc 滑动返回辅助类
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2017/11/20
 */

public class SwipeBackHelper {

    private static final String TAG = "SwipeBackHelper";

    private static final int SHADOW_WIDTH = 80; //px 阴影宽度
    private static final float SLIDE_SPEED = 1.2f; //默认开始滑动的速度
    private boolean isHorizontalSliding; //是否正在水平滑动
    private boolean isVerticalSliding; //是否正在竖直滑动
    private boolean isSlideAnimPlaying; //滑动动画展示过程中
    private float distanceX;  //px 当前滑动距离 （正数或0）
    private float lastPointX;  //记录手势在屏幕上的X轴坐标
    private float lastPointY;
    private long actionDownMills = 0;

    private int touchSlop;

    private Activity curActivity;
    private ViewManager viewManager;
    private final FrameLayout curContentContainer;
    private AnimatorSet animatorSet;

    private Activity preActivity;


    public boolean isSliding() {
        return isSlideAnimPlaying;
    }

    private SlideBackManager slideBackManager;
    private SlideAnimListener listener;

    private View nightShadowView;

    private View getNightShadowView(Activity activity) {
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        return decorView.findViewById(R.id.night_shadow_view);
    }

    public SwipeBackHelper(@NotNull SlideBackManager slideBackManager, @NotNull SlideAnimListener listener) {

        this.slideBackManager = slideBackManager;
        this.listener = listener;

        curActivity = slideBackManager.getSlideActivity();
        preActivity = ActivityLifecycleHelper.getPreviousActivity();
        curContentContainer = getContentView(curActivity);
        viewManager = new ViewManager();

        touchSlop = ViewConfiguration.get(curActivity).getScaledTouchSlop();

    }

    public boolean processTouchEvent(MotionEvent ev) {
        if (!slideBackManager.supportSlideBack()) { //不支持滑动返回，则手势事件交给View处理
            return false;
        }

        if (isSlideAnimPlaying) {  //正在滑动动画播放中，直接消费手势事件
            return true;
        }


        final int action = ev.getAction() & MotionEvent.ACTION_MASK;

        final int actionIndex = ev.getActionIndex();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                onTouchActionDown(ev);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (isHorizontalSliding) {  //有第二个手势事件加入，而且正在滑动事件中，则直接消费事件
                    return true;
                }
                if (isVerticalSliding) {
                    return false;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                //一旦触发滑动机制，拦截所有其他手指的滑动事件
                if (actionIndex != 0) {
                    return isHorizontalSliding || isVerticalSliding;
                }

                if (isVerticalSliding) return false;

                final float curPointX = ev.getX();
                final float curPointY = ev.getY();

                //横坐标位移增量
                float deltaX = Math.abs(curPointX - lastPointX);
                //纵坐标位移增量
                float deltaY = Math.abs(curPointY - lastPointY);

                if (!isHorizontalSliding && deltaY > touchSlop && deltaY * 1.5f > deltaX) {//如果竖直方向滑动，则返回
                    isVerticalSliding = true;
                    return false;
                }

                boolean isSliding = isHorizontalSliding;
                if (!isSliding) {
                    if (deltaX < touchSlop) {
                        return false;
                    } else {
                        isHorizontalSliding = true;
                    }
                }

                onSliding(curPointX, curPointY);

                if (isSliding == isHorizontalSliding) {
                    return true;
                } else {
                    MotionEvent cancelEvent = MotionEvent.obtain(ev); //首次判定为滑动需要修正事件：手动修改事件为 ACTION_CANCEL，并通知底层View
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
                    curActivity.getWindow().superDispatchTouchEvent(cancelEvent);
                    return true;
                }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_OUTSIDE:
                isVerticalSliding = false;
                if (distanceX == 0) { //没有进行滑动
                    isHorizontalSliding = false;
                    onTouchActionUp();
                    return false;
                }

                if (isHorizontalSliding && actionIndex == 0) { // 取消滑动 或 手势抬起 ，而且手势事件是第一手势，开始滑动动画
                    isHorizontalSliding = false;
                    onTouchActionUp();
                    return true;
                } else if (isHorizontalSliding && actionIndex != 0) {
                    return true;
                }
                break;
            default:
                isHorizontalSliding = false;
                isVerticalSliding = false;
                break;
        }
        return false;
    }

    public void finishSwipeImmediately() {

        if (animatorSet != null) {
            animatorSet.cancel();
        }

        curActivity = null;
    }

//    private int getWindowBackgroundColor() {
//        TypedArray array = null;
//        try {
//            array = curActivity.getTheme().obtainStyledAttributes(new int[]{android.R.attr.windowBackground});
//            return array.getColor(0, ContextCompat.getColor(curActivity, android.R.color.transparent));
//        } finally {
//            if (array != null) {
//                array.recycle();
//            }
//        }
//    }

    private void onTouchActionDown(MotionEvent ev) {
        nightShadowView = getNightShadowView(curActivity);
        if (preActivity instanceof FrameActivity) {
            FrameActivity activity = (FrameActivity) preActivity;
            if (activity.shouldShowNightShadow()) {
                activity.nightShift(activity.mThemeHelper.isNight(), false);
            }
        }
        lastPointX = ev.getX();
        lastPointY = ev.getY();
        actionDownMills = System.currentTimeMillis();
        InputMethodManager inputMethod = (InputMethodManager) curActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = curActivity.getCurrentFocus();
        if (view != null && inputMethod != null) {
            inputMethod.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

    }

    private void onTouchActionUp() {
        long actionUpMills = System.currentTimeMillis();
        final int width = curActivity.getResources().getDisplayMetrics().widthPixels;
        long interval = actionUpMills - actionDownMills;
        float speed = distanceX / interval;
        if (distanceX == 0) {
            if (curContentContainer.getChildCount() >= 2) {
                viewManager.removeShadowView();
//                viewManager.resetPreviousView();
            }
        } else if (distanceX > width / 2 || speed > SLIDE_SPEED) {
            startSlideAnim(false);
        } else {
            startSlideAnim(true);
        }
    }

    /**
     * 手动处理滑动事件
     */
    private synchronized void onSliding(float curPointX, float curPointY) {
        if (!viewManager.hasAddViews()) viewManager.addShadowView();
        View shadowView = viewManager.shadowView;
        View curContentView = viewManager.getCurContentView();

        if (curContentView == null || shadowView == null) {
            cancelSlide();
            return;
        }

        final float distanceX = curPointX - lastPointX;
        lastPointX = curPointX;
        lastPointY = curPointY;
        this.distanceX = this.distanceX + distanceX;
        if (this.distanceX < 0) {
            this.distanceX = 0;
        }

        shadowView.setX(this.distanceX - SHADOW_WIDTH);
        curContentView.setX(this.distanceX);
        if (nightShadowView != null) {
            nightShadowView.setX(this.distanceX);
        }
    }

    /**
     * 开始自动滑动动画
     *
     * @param isSlideCancel 是不是要返回（true则不关闭当前页面）
     */
    private void startSlideAnim(final boolean isSlideCancel) {
        final View shadowView = viewManager.shadowView;
        final View curContentView = viewManager.getCurContentView();

        if (curContentView == null) {
            return;
        }

        int width = curActivity.getResources().getDisplayMetrics().widthPixels;
        Interpolator interpolator = new DecelerateInterpolator();

        // shadow view's animation
        ObjectAnimator shadowViewAnim = new ObjectAnimator();
        shadowViewAnim.setInterpolator(interpolator);
        shadowViewAnim.setProperty(View.TRANSLATION_X);
        float shadowViewStart = distanceX - SHADOW_WIDTH;
        float shadowViewEnd = isSlideCancel ? -SHADOW_WIDTH : width + SHADOW_WIDTH;
        shadowViewAnim.setFloatValues(shadowViewStart, shadowViewEnd);
        shadowViewAnim.setTarget(shadowView);

        // current view's animation
        ObjectAnimator currentViewAnim = new ObjectAnimator();
        currentViewAnim.setInterpolator(interpolator);
        currentViewAnim.setProperty(View.TRANSLATION_X);
        float curViewStart = distanceX;
        float curViewStop = isSlideCancel ? 0 : width;
        currentViewAnim.setFloatValues(curViewStart, curViewStop);
        currentViewAnim.setTarget(curContentView);

        // nightShadow view's animation
        ObjectAnimator nightShadowAnim = new ObjectAnimator();
        nightShadowAnim.setInterpolator(interpolator);
        nightShadowAnim.setProperty(View.TRANSLATION_X);
        float nightShadowStart = distanceX;
        float nightShadowStop = isSlideCancel ? 0 : width;
        nightShadowAnim.setFloatValues(nightShadowStart, nightShadowStop);
        nightShadowAnim.setTarget(nightShadowView);

        // play animation together
        animatorSet = new AnimatorSet();
        animatorSet.setDuration(isSlideCancel ? 150 : 300);
        if (nightShadowView != null) {
            animatorSet.playTogether(shadowViewAnim, currentViewAnim, nightShadowAnim);
        } else {
            animatorSet.playTogether(shadowViewAnim, currentViewAnim);
        }
        animatorSet.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                AppLog.e(TAG, "onAnimationStart");
                isSlideAnimPlaying = true;
                if (listener != null) listener.onSlideAnimStart();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                AppLog.e(TAG, "onAnimationEnd");
                if (isSlideCancel) {
                    isSlideAnimPlaying = false;
                    shadowView.setX(-SHADOW_WIDTH);
                    curContentView.setX(0);
                    cancelSlide();
                } else {
                    finishSlide();
                }
            }
        });
        animatorSet.start();
    }

    private void cancelSlide() {
        distanceX = 0;
        isHorizontalSliding = false;
        viewManager.removeShadowView();
        if (listener != null) listener.onSlideCancelAnimEnd();
    }

    private void finishSlide() {
        AppLog.e(TAG, "MSG_SLIDE_FINISHED");
        viewManager.removeShadowView();

        ActivityLifecycleHelper.build().finishActivity(curActivity);
        curActivity.finish();
        curActivity.overridePendingTransition(0, 0);
        isSlideAnimPlaying = false;
        if (listener != null) listener.onSlideFinishAnimEnd();
    }

    private FrameLayout getContentView(Activity activity) {
        return (FrameLayout) activity.findViewById(Window.ID_ANDROID_CONTENT);
    }

    private class ViewManager {

        private View shadowView;

        private synchronized void addShadowView() {
            if (shadowView == null) {
                shadowView = new ShadowView(curActivity);
                shadowView.setX(-SHADOW_WIDTH);
            }
            final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    SHADOW_WIDTH, FrameLayout.LayoutParams.MATCH_PARENT);

            if (this.shadowView.getParent() == null) {
                curContentContainer.addView(this.shadowView, 0, layoutParams);
            } else {
                this.removeShadowView();
                this.addShadowView();
            }
        }

        private synchronized void removeShadowView() {
            if (shadowView == null) return;
            final FrameLayout contentView = getContentView(curActivity);
            contentView.removeView(shadowView);
            shadowView = null;
        }

        private View getCurContentView() {
            int index = 0;
            if (viewManager.shadowView != null) {
                index = index + 1;
            }
            return curContentContainer.getChildAt(index);
        }

        private boolean hasAddViews() {
            return shadowView != null;
        }
    }

    public interface SlideBackManager {

        @NonNull
        @NotNull
        Activity getSlideActivity();

        /**
         * 是否支持滑动返回
         */
        boolean supportSlideBack();

        /**
         * 能否滑动返回至当前Activity
         */
        boolean canBeSlideBack();

    }

    public interface SlideAnimListener {

        /**
         * 滑动开始
         */
        void onSlideAnimStart();

        /**
         * 滑动取消
         */
        void onSlideCancelAnimEnd();

        /**
         * 滑动结束
         */
        void onSlideFinishAnimEnd();
    }

}
