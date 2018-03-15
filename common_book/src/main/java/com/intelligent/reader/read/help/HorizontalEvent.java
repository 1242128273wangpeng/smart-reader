package com.intelligent.reader.read.help;

import android.view.MotionEvent;

/**
 * Created by wt on 2018/1/9.
 */

public interface HorizontalEvent {
    boolean myDispatchTouchEvent(MotionEvent event);
    boolean forceUseTouchEvent();
}
