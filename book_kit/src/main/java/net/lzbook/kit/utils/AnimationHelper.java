package net.lzbook.kit.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 */
public class AnimationHelper {
   static int width = 0;
    static  int height = 0;

    static private SmoothScrollRunnable currentSmoothScrollRunnable;
    static  private final Handler handler = new Handler();
    private static final long DELAY_TIME = 200;
    @SuppressLint("NewApi")
    public static void init(Context context){

        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);

        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        if (Build.VERSION.SDK_INT > 13) {
            display.getSize(point);
            width = point.x;
            height = point.y;
        } else {
            width = display.getWidth();
            height = display.getHeight();
        }

    }

    public  static final void smoothScrollTo(View view,int y) {
        if (null != currentSmoothScrollRunnable) {
            currentSmoothScrollRunnable.stop();
        }

        if (view.getScrollY() != y) {
            currentSmoothScrollRunnable = new SmoothScrollRunnable(view,handler, view.getScrollY(), y);
            handler.post(currentSmoothScrollRunnable);
        }
    }
    static  final class SmoothScrollRunnable implements Runnable {

        static final int ANIMATION_DURATION_MS = 230;// FIXME
        static final int ANIMATION_FPS = 1000 / 60;

        private final Interpolator interpolator;
        private final int scrollToY;
        private final int scrollFromY;
        private final Handler handler;

        private boolean continueRunning = true;
        private long startTime = -1;
        private int currentY = -1;
        private View view;

        public SmoothScrollRunnable(View view,Handler handler, int fromY, int toY) {
            this.view = view;
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
                setHeaderScroll(view,currentY);
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
    };
    protected  static final void setHeaderScroll(View view,int y) {
        view.scrollTo(0, y);
    }

}
