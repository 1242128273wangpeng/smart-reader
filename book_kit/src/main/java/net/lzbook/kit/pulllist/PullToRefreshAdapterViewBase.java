package net.lzbook.kit.pulllist;


import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.ImageView;

public abstract class PullToRefreshAdapterViewBase<T extends AbsListView> extends PullToRefreshBase<T> implements OnScrollListener {

    private int lastSavedFirstVisibleItem = -1;
    private OnScrollListener onScrollListener;
    private OnLastItemVisibleListener onLastItemVisibleListener;
    //	private View emptyView;
    private FrameLayout refreshableViewHolder;
    private ImageView mTopImageView;
    private Context context;
    private float x1;
    private float x2;
    private OnRefreshNoDataListener onRefreshNoDataListener;

    public PullToRefreshAdapterViewBase(Context context) {
        super(context);
        this.context = context;
        refreshableView.setOnScrollListener(this);
    }

    public PullToRefreshAdapterViewBase(Context context, int mode) {
        super(context, mode);
        this.context = context;
        refreshableView.setOnScrollListener(this);
    }

    public PullToRefreshAdapterViewBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        refreshableView.setOnScrollListener(this);
    }

    abstract public ContextMenuInfo getContextMenuInfo();

    public final void onScroll(final AbsListView view,
                               final int firstVisibleItem, final int visibleItemCount,
                               final int totalItemCount) {

        if (null != onLastItemVisibleListener) {
            // detect if last item is visible
            if (visibleItemCount > 0
                    && (firstVisibleItem + visibleItemCount == totalItemCount)) {
                // only process first event
                if (firstVisibleItem != lastSavedFirstVisibleItem) {
                    lastSavedFirstVisibleItem = firstVisibleItem;
                    onLastItemVisibleListener.onLastItemVisible();
                }
            }
        }

        if (null != onScrollListener) {
            onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount,
                    totalItemCount);
        }
    }

    public final void onScrollStateChanged(final AbsListView view,
                                           final int scrollState) {
        if (null != onScrollListener) {
            onScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    /**
     * Sets the Empty View to be used by the Adapter View.
     *
     * We need it handle it ourselves so that we can Pull-to-Refresh when the
     * Empty View is shown.
     *
     * Please note, you do <strong>not</strong> usually need to call this method
     * yourself. Calling setEmptyView on the AdapterView will automatically call
     * this method and set everything up. This includes when the Android
     * Framework automatically sets the Empty View based on it's ID.
     *
     * @param newEmptyView - Empty View to be used
     */
    public final void setEmptyView(View newEmptyView) {
        // If we already have an Empty View, remove it
//		if (emptyView != null) {
//			refreshableViewHolder.removeView(emptyView);
//		}

        if (null != newEmptyView) {
            ViewParent newEmptyViewParent = newEmptyView.getParent();
            if (null != newEmptyViewParent && newEmptyViewParent instanceof ViewGroup) {
                ((ViewGroup) newEmptyViewParent).removeView(newEmptyView);
            }

            this.refreshableViewHolder.addView(newEmptyView, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
        }

        if (refreshableView instanceof EmptyViewMethodAccessor) {
            ((EmptyViewMethodAccessor) refreshableView).setEmptyViewInternal(newEmptyView);
        } else {
            this.refreshableView.setEmptyView(newEmptyView);
        }
    }

    protected void addRefreshableView(Context context, T refreshableView) {
        refreshableViewHolder = new FrameLayout(context);
        refreshableViewHolder.addView(refreshableView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        addView(refreshableViewHolder, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f));
    }

    ;

    protected boolean isReadyForPullDown() {// FIXME

        return isFirstItemVisible();
    }

    protected boolean isReadyForPullUp() {// FIXME


        return isLastItemVisible();
    }

    private boolean isFirstItemVisible() {
        if (this.refreshableView.getCount() == 0) {
            return true;
        } else if (refreshableView.getFirstVisiblePosition() == 0) {

            final View firstVisibleChild = refreshableView.getChildAt(0);

            if (firstVisibleChild != null) {
                return firstVisibleChild.getTop() >= refreshableView.getTop();
            }
        }

        return false;
    }

    private boolean isLastItemVisible() {
        final int count = this.refreshableView.getCount();
        final int lastVisiblePosition = refreshableView
                .getLastVisiblePosition();

        if (count == 0) {
            return false;
        } else if (lastVisiblePosition == count - 1) {

            // final int childIndex = lastVisiblePosition
            // - refreshableView.getFirstVisiblePosition();
            // final View lastVisibleChild = refreshableView
            // .getChildAt(childIndex);
            // // 利用这种方式可以判断listview的条数是否超过该屏幕
            // if (lastVisibleChild != null) {
            // int child = lastVisibleChild.getBottom();
            // int listBottom = refreshableView.getBottom();
            // return child >= listBottom;
            // }
            final int childIndex = lastVisiblePosition
                    - refreshableView.getFirstVisiblePosition();
            final View lastVisibleChild = refreshableView
                    .getChildAt(childIndex);
            if (lastVisibleChild != null) {
                return lastVisibleChild.getBottom() == refreshableView
                        .getBottom();
            }
        }
        return false;
    }

    public ImageView getmTopImageView() {
        return mTopImageView;
    }

    public void setmTopImageView(ImageView mTopImageView) {
        this.mTopImageView = mTopImageView;
    }

    //点击刷新
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            x1 = event.getX();
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            x2 = event.getX();
            if (Math.abs(x1 - x2) < 6) {
                final int count = this.refreshableView.getChildCount();
                if (count == 0) {
                    if (onRefreshNoDataListener != null) {
                        showHeadView();
                        onRefreshNoDataListener.onRefresh();
                    }
                }
            }
        }


        return super.onInterceptTouchEvent(event);
    }


    public final void setOnRefreshNoDataListener(
            OnRefreshNoDataListener listener) {
        onRefreshNoDataListener = listener;
    }


    public static interface OnRefreshNoDataListener {

        public void onRefresh();

    }
}
