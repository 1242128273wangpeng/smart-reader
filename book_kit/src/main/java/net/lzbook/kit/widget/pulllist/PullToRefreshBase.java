package net.lzbook.kit.widget.pulllist;

import net.lzbook.kit.R;
import net.lzbook.kit.utils.logger.AppLog;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;


public abstract class PullToRefreshBase<T extends View> extends LinearLayout {
    public static final int MODE_PULL_DOWN_TO_REFRESH = 0x1;
    public static final int MODE_PULL_UP_TO_REFRESH = 0x2;

    ;

    // ===========================================================
    // Constants
    // ===========================================================
    public static final int MODE_BOTH = 0x3;
    public static final int MODE_NO = 0x4;
    static final float FRICTION = 3.0f;
    static final int PULL_TO_REFRESH = 0x0;
    static final int RELEASE_TO_REFRESH = 0x1;
    static final int REFRESHING = 0x2;
    static final int MANUAL_REFRESHING = 0x3;
    private final Handler handler = new Handler();
    public int mode = MODE_BOTH;

    // ===========================================================
    // Fields
    // ===========================================================
    public T refreshableView;
    String TAG = "PullToRefreshBase";
    long REFRSH_LOADING_MIN_TIME = 2000;// ms
    long complete_loading_time = -1;
    long start_loading_time = -1;
    private int touchSlop;
    private float initialMotionY;
    private float lastMotionX;
    private float lastMotionY;
    private boolean isBeingDragged = false;
    private int state = PULL_TO_REFRESH;
    private int currentMode = MODE_PULL_DOWN_TO_REFRESH;
    private boolean disableScrollingWhileRefreshing = true;
    // 是否开启刷新和加载更多
    private boolean isPullToRefreshEnabled = true;
    private LoadingLayoutBoth headerLayout;
    private LoadingLayoutBoth footerLayout;
    private int headerHeight;
    private OnRefreshListener onRefreshListener;
    private SmoothScrollRunnable currentSmoothScrollRunnable;
    Runnable refreshCompleteTask = new Runnable() {
        @Override
        public void run() {
            resetHeader();
            AppLog.d(TAG, "refreshCompleteTask resetHeader");
        }
    };

    // ===========================================================
    // Constructors
    // ===========================================================

    public PullToRefreshBase(Context context) {
        super(context);
        init(context, null);
    }

    public PullToRefreshBase(Context context, int mode) {
        super(context);
        this.mode = mode;
        init(context, null);
    }

    public PullToRefreshBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    /**
     * Get the Wrapped Refreshable View. Anything returned here has already been
     * added to the content view.
     *
     * @return The View which is currently wrapped
     */
    public final T getRefreshableView() {
        return refreshableView;
    }

    /**
     * Returns whether the Widget is currently in the Refreshing state
     *
     * @return true if the Widget is currently refreshing
     */
    public final boolean isRefreshing() {
        return state == REFRESHING || state == MANUAL_REFRESHING;
    }

    /**
     * Sets the Widget to be in the refresh state. The UI will be updated to
     * show the 'Refreshing' view.
     *
     * @param doScroll - true if you want to force a scroll to the Refreshing view.
     */
    public final void setRefreshing(boolean doScroll) {
        if (!isRefreshing()) {
            setRefreshingInternal(doScroll);
            state = MANUAL_REFRESHING;
        }
        start_loading_time = System.currentTimeMillis();
        AppLog.d("refreshtime", start_loading_time / 1000f + "start_loading_time");
    }

    /**
     * By default the Widget disabled scrolling on the Refreshable View while
     * refreshing. This method can change this behaviour.
     *
     * @param disableScrollingWhileRefreshing - true if you want to disable scrolling while
     *                                        refreshing
     */
    public final void setDisableScrollingWhileRefreshing(boolean disableScrollingWhileRefreshing) {
        this.disableScrollingWhileRefreshing = disableScrollingWhileRefreshing;
    }

    /**
     * Mark the current Refresh as complete. Will Reset the UI and hide the
     * Refreshing View
     */
    public final void onRefreshComplete() {
        complete_loading_time = System.currentTimeMillis();
        AppLog.d(TAG, "onRefreshComplete state" + state);
        AppLog.d(TAG, "onRefreshComplete refreshtime==" + (complete_loading_time - start_loading_time));
        if (state != PULL_TO_REFRESH) {// FIXME

            // 刷新时间2s
            if (complete_loading_time - start_loading_time <= REFRSH_LOADING_MIN_TIME) {

                handler.postDelayed(refreshCompleteTask, REFRSH_LOADING_MIN_TIME);
            } else {
                AppLog.d(TAG, "resetHeader");
                resetHeader();
            }
        }
    }

    /**
     * Set OnRefreshListener for the Widget
     *
     * @param listener - Listener to be used when the Widget is set to Refresh
     */
    public final void setOnRefreshListener(OnRefreshListener listener) {
        onRefreshListener = listener;
    }

    /**
     * A mutator to enable/disable Pull-to-Refresh for the current View
     *
     * @param enable Whether Pull-To-Refresh should be used
     */
    public final void setPullToRefreshEnabled(boolean enable) {
        this.isPullToRefreshEnabled = enable;
    }

    public final void setRefreshing() {
        this.setRefreshing(true);
    }

    public final void showHeadView() {
        if (!isRefreshing()) {
            state = REFRESHING;

            if (null != headerLayout) {
                headerLayout.refreshing();
            }

            smoothScrollTo(-headerHeight);
            state = MANUAL_REFRESHING;
        }
    }

    public final boolean hasPullFromTop() {
        return currentMode != MODE_PULL_UP_TO_REFRESH;
    }

    @Override
    public final boolean onTouchEvent(MotionEvent event) {
        AppLog.d(TAG, "isPullToRefreshEnabled: " + isPullToRefreshEnabled);
        if (!isPullToRefreshEnabled) {
            return false;
        }

        if (isRefreshing() && disableScrollingWhileRefreshing) {

            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN && event.getEdgeFlags() != 0) {
            return false;
        }

        switch (event.getAction()) {

            case MotionEvent.ACTION_MOVE: {
                if (isBeingDragged) {
                    lastMotionY = event.getY();
                    this.pullEvent();
                    return true;
                }
                break;
            }

            case MotionEvent.ACTION_DOWN: {
                if (isReadyForPull()) {// FIXME
                    lastMotionY = initialMotionY = event.getY();
                    return true;
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (isBeingDragged) {
                    isBeingDragged = false;

                    if (state == RELEASE_TO_REFRESH && null != onRefreshListener) {
                        setRefreshingInternal(true);
                        onRefreshListener.onRefresh();
                    } else {
                        smoothScrollTo(0);
                    }
                    return true;
                }
                break;
            }
        }

        return false;
    }

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        if (!isPullToRefreshEnabled) {
            return false;
        }

        if (isRefreshing() && disableScrollingWhileRefreshing) {
            return true;
        }

        final int action = event.getAction();

        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            isBeingDragged = false;
            return false;
        }

        if (action != MotionEvent.ACTION_DOWN && isBeingDragged) {
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                if (isReadyForPull()) {// FIXME

                    final float y = event.getY();
                    final float dy = y - lastMotionY;
                    final float yDiff = Math.abs(dy);
                    final float xDiff = Math.abs(event.getX() - lastMotionX);

                    if (yDiff > touchSlop && yDiff > xDiff) {
                        if ((mode == MODE_PULL_DOWN_TO_REFRESH || mode == MODE_BOTH) && dy >= 0.0001f
                                && isReadyForPullDown()) {
                            if (!isRefreshing()) {
                                lastMotionY = y;
                                isBeingDragged = true;
                                // if (mode == MODE_BOTH) {
                                currentMode = MODE_PULL_DOWN_TO_REFRESH;
                                // }
                            }
                        } else if ((mode == MODE_PULL_UP_TO_REFRESH || mode == MODE_BOTH) && dy <= 0.0001f
                                && isReadyForPullUp()) {
                            if (!isRefreshing()) {
                                lastMotionY = y;
                                isBeingDragged = true;
                                // if (mode == MODE_BOTH) {
                                currentMode = MODE_PULL_UP_TO_REFRESH;
                                // }
                            }
                        }
                    }
                }
                break;
            }
            case MotionEvent.ACTION_DOWN: {
                if (isReadyForPull()) {// FIXME
                    lastMotionY = initialMotionY = event.getY();
                    lastMotionX = event.getX();
                    isBeingDragged = false;
                }
                break;
            }
        }

        return isBeingDragged;
    }

    protected void addRefreshableView(Context context, T refreshableView) {
        addView(refreshableView, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f));
    }

    /**
     * This is implemented by derived classes to return the created View. If you
     * need to use a custom View (such as a custom ListView), override this
     * method and return an instance of your custom class.
     *
     * Be sure to set the ID of the view in this method, especially if you're
     * using a ListActivity or ListFragment.
     *
     * @param attrs AttributeSet from wrapped class. Means that anything you
     *              include in the XML layout declaration will be routed to the
     *              created View
     * @return New instance of the Refreshable View
     */
    protected abstract T createRefreshableView(Context context, AttributeSet attrs);

    public final int getMode() {
        return mode;
    }

    public void setMode(int model) {
        this.mode = model;
    }

    /**
     * Implemented by derived class to return whether the View is in a state
     * where the user can Pull to Refresh by scrolling down.
     *
     * @return true if the View is currently the correct state (for example, top
     * of a ListView)
     */
    protected abstract boolean isReadyForPullDown();

    /**
     * Implemented by derived class to return whether the View is in a state
     * where the user can Pull to Refresh by scrolling up.
     *
     * @return true if the View is currently in the correct state (for example,
     * bottom of a ListView)
     */
    protected abstract boolean isReadyForPullUp();

    protected void resetHeader() {
        state = PULL_TO_REFRESH;
        isBeingDragged = false;

        if (null != headerLayout) {
            headerLayout.reset(false);
        }
        if (null != footerLayout) {
            footerLayout.reset(false);
        }

        smoothScrollTo(0);
    }

    // ===========================================================
    // Methods
    // ===========================================================

    protected void setRefreshingInternal(boolean doScroll) {

        state = REFRESHING;

        if (null != headerLayout) {
            headerLayout.refreshing();
        }
        if (null != footerLayout) {
            footerLayout.refreshing();
        }

        if (doScroll) {
            smoothScrollTo(currentMode == MODE_PULL_DOWN_TO_REFRESH ? -headerHeight : headerHeight);
        }
    }

    protected final void setHeaderScroll(int y) {
        scrollTo(0, y);
    }

    protected final void smoothScrollTo(int y) {
        if (null != currentSmoothScrollRunnable) {
            currentSmoothScrollRunnable.stop();
        }

        if (this.getScrollY() != y) {
            this.currentSmoothScrollRunnable = new SmoothScrollRunnable(handler, getScrollY(), y);
            handler.post(currentSmoothScrollRunnable);
        }
    }

    private void init(Context context, AttributeSet attrs) {

        setOrientation(LinearLayout.VERTICAL);
        ViewConfiguration vc = ViewConfiguration.get(context);
        touchSlop = vc.getScaledTouchSlop();

        // Styleables from XML
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PullToRefresh);
        if (a.hasValue(R.styleable.PullToRefresh_mode)) {
            mode = a.getInteger(R.styleable.PullToRefresh_mode, MODE_PULL_DOWN_TO_REFRESH);
        }

        // Refreshable View
        // By passing the attrs, we can add ListView/GridView params via XML
        refreshableView = this.createRefreshableView(context, attrs);
        this.addRefreshableView(context, refreshableView);

        // headlayout View Strings
        String pullLabel = context.getString(R.string.pull_to_refresh_pull_label_list);
        String refreshingLabel = context.getString(R.string.pull_to_refresh_refreshing_label_list);
        String releaseLabel = context.getString(R.string.pull_to_refresh_release_label_list);

        // footlayout View Strings
        String pullMoreLabel = context.getString(R.string.pull_to_more_pull_label);
        String moreRefreshingLabel = context.getString(R.string.pull_to_more_refreshing_label);
        String moreReleaseLabel = context.getString(R.string.pull_to_more_release_label);

        // Add Loading Views
        if (mode == MODE_PULL_DOWN_TO_REFRESH || mode == MODE_BOTH) {
            headerLayout = new LoadingLayoutBoth(context, MODE_PULL_DOWN_TO_REFRESH, releaseLabel, pullLabel,
                    refreshingLabel);
            addView(headerLayout, 0, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            measureView(headerLayout);
            headerHeight = headerLayout.getMeasuredHeight();
        }
        if (mode == MODE_PULL_UP_TO_REFRESH || mode == MODE_BOTH) {
            footerLayout = new LoadingLayoutBoth(context, MODE_PULL_UP_TO_REFRESH, moreReleaseLabel, pullMoreLabel,
                    moreRefreshingLabel);
            addView(footerLayout, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            measureView(footerLayout);
            headerHeight = footerLayout.getMeasuredHeight();
        }

        // Styleables from XML
        if (a.hasValue(R.styleable.PullToRefresh_headerTextColor)) {
            final int color = a.getColor(R.styleable.PullToRefresh_headerTextColor, Color.BLACK);
            if (null != headerLayout) {
                headerLayout.setTextColor(color);
            }
            if (null != footerLayout) {
                footerLayout.setTextColor(color);
            }
        }
        if (a.hasValue(R.styleable.PullToRefresh_headerBackground)) {
            this.setBackgroundResource(a.getResourceId(R.styleable.PullToRefresh_headerBackground, Color.WHITE));
        }
        if (a.hasValue(R.styleable.PullToRefresh_adapterViewBackground)) {
            refreshableView.setBackgroundResource(a.getResourceId(R.styleable.PullToRefresh_adapterViewBackground,
                    Color.WHITE));
        }
        a.recycle();

        // Hide Loading Views
        switch (mode) {
            case MODE_BOTH:
                setPadding(0, -headerHeight, 0, -headerHeight);
                break;
            case MODE_PULL_UP_TO_REFRESH:
                setPadding(0, 0, 0, -headerHeight);
                break;
            case MODE_PULL_DOWN_TO_REFRESH:
            default:
                setPadding(0, -headerHeight, 0, 0);
                break;
        }

        // If we're not using MODE_BOTH, then just set currentMode to current
        // mode
        if (mode != MODE_BOTH) {
            currentMode = mode;
        }
    }

    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    /**
     * Actions a Pull Event
     *
     * @return true if the Event has been handled, false if there has been no
     * change
     */
    private boolean pullEvent() {

        final int newHeight;
        final int oldHeight = this.getScrollY();

        switch (currentMode) {
            case MODE_PULL_UP_TO_REFRESH:
                newHeight = Math.round(Math.max(initialMotionY - lastMotionY, 0) / FRICTION);
                // newHeight = Math.round((initialMotionY - lastMotionY) /
                // FRICTION);
                break;
            case MODE_PULL_DOWN_TO_REFRESH:
            default:
                newHeight = Math.round(Math.min(initialMotionY - lastMotionY, 0) / FRICTION);// FIXME
                // newHeight = Math.round((initialMotionY - lastMotionY) /
                // FRICTION);
                break;
        }

        setHeaderScroll(newHeight);

        if (newHeight != 0) {
            if (state == PULL_TO_REFRESH && headerHeight < Math.abs(newHeight)) {
                state = RELEASE_TO_REFRESH;

                switch (currentMode) {
                    case MODE_PULL_UP_TO_REFRESH:
                        footerLayout.releaseToRefresh();
                        break;
                    case MODE_PULL_DOWN_TO_REFRESH:
                        headerLayout.releaseToRefresh();
                        break;
                }

                return true;

            } else if (state == RELEASE_TO_REFRESH && headerHeight >= Math.abs(newHeight)) {
                state = PULL_TO_REFRESH;

                switch (currentMode) {
                    case MODE_PULL_UP_TO_REFRESH:
                        footerLayout.pullToRefresh();
                        break;
                    case MODE_PULL_DOWN_TO_REFRESH:
                        headerLayout.pullToRefresh();
                        break;
                }

                return true;
            }
        }

        return oldHeight != newHeight;
    }

    private boolean isReadyForPull() {
        switch (mode) {
            case MODE_PULL_DOWN_TO_REFRESH:
                return isReadyForPullDown();
            case MODE_PULL_UP_TO_REFRESH:
                return isReadyForPullUp();
            case MODE_BOTH:
                return isReadyForPullUp() || isReadyForPullDown();
        }
        return false;
    }

    @Override
    public void setLongClickable(boolean longClickable) {
        getRefreshableView().setLongClickable(longClickable);
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    public static interface OnRefreshListener {

        public void onRefresh();

    }

    public static interface OnLastItemVisibleListener {

        public void onLastItemVisible();

    }

    final class SmoothScrollRunnable implements Runnable {

        // static final int ANIMATION_DURATION_MS = 190;
        static final int ANIMATION_DURATION_MS = 230;// FIXME
        static final int ANIMATION_FPS = 1000 / 60;

        private final Interpolator interpolator;
        private final int scrollToY;
        private final int scrollFromY;
        private final Handler handler;

        private boolean continueRunning = true;
        private long startTime = -1;
        private int currentY = -1;

        public SmoothScrollRunnable(Handler handler, int fromY, int toY) {
            this.handler = handler;
            this.scrollFromY = fromY;
            this.scrollToY = toY;
            this.interpolator = new AccelerateDecelerateInterpolator();
        }

        @Override
        public void run() {

            /**
             * Only set startTime if this is the first time we're starting, else
             * actually calculate the Y delta
             */
            if (startTime == -1) {
                startTime = System.currentTimeMillis();
            } else {

                /**
                 * We do do all calculations in long to reduce software float
                 * calculations. We use 1000 as it gives us good accuracy and
                 * small rounding errors
                 */
                long normalizedTime = (1000 * (System.currentTimeMillis() - startTime)) / ANIMATION_DURATION_MS;
                normalizedTime = Math.max(Math.min(normalizedTime, 1000), 0);

                final int deltaY = Math.round((scrollFromY - scrollToY)
                        * interpolator.getInterpolation(normalizedTime / 1000f));
                this.currentY = scrollFromY - deltaY;
                setHeaderScroll(currentY);
            }

            // If we're not at the target Y, keep going...
            if (continueRunning && scrollToY != currentY) {
                handler.postDelayed(this, ANIMATION_FPS);
            }
        }

        public void stop() {
            this.continueRunning = false;
            this.handler.removeCallbacks(this);
        }
    }

    // public void setModeEdit(boolean edit) {
    // this.isPullToRefreshEnabled = !edit;
    // AppLog.d(TAG, isPullToRefreshEnabled + "");
    // }
}
