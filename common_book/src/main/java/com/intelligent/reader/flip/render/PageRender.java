/*
 * Copyright (C) 2016 eschao <esc.chao@gmail.com>
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
package com.intelligent.reader.flip.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;

import com.intelligent.reader.flip.base.OnPageFlipListener;
import com.intelligent.reader.flip.base.PageFlip;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Page Render
 *
 * @author eschao
 */

public abstract class PageRender implements OnPageFlipListener {

//    public final static int MSG_ENDED_DRAWING_FRAME = 1;
    public final static int MSG_ENDED_FLIP_DOWN = 2;
    public final static int MSG_ENDED_FLIP_UP = 3;
    public final static int MSG_ENDED_SET_PAGE = 4;
    private final static String TAG = "PageRender";

    public final static int DRAW_MOVING_FRAME = 0;
    public final static int DRAW_ANIMATING_FRAME = 1;
    public final static int DRAW_FULL_PAGE = 2;
//    public final static int DRAW_DELETE_AFTER_FIRST_PAGE = 3;

    final static int MAX_PAGES = Integer.MAX_VALUE;

    int mDrawCommand;
    Context mContext;
    public Handler mHandler;
    public PageFlip mPageFlip;

    public List<Runnable> afterFirstDrawEvent = new ArrayList<>();

    public PageRender(Context context, PageFlip pageFlip,
                      Handler handler) {
        mContext = context;
        mPageFlip = pageFlip;
        mDrawCommand = DRAW_FULL_PAGE;
        mPageFlip.setListener(this);
        mHandler = handler;
    }

    /**
     * Release resources
     */
    public void release() {
        mPageFlip.setListener(null);
    }

    /**
     * Handle finger moving event
     *
     * @return true if event is handled
     */
    public boolean onFingerMove() {
        mDrawCommand = DRAW_MOVING_FRAME;
        return true;
    }

    /**
     * Handle finger up event
     *
     * @return true if event is handled
     */
    public boolean onFingerUp() {
        if (mPageFlip.animating()) {
            mDrawCommand = DRAW_ANIMATING_FRAME;
            return true;
        }

        return false;
    }

    /**
     * Calculate font size by given SP unit
     */
    protected int calcFontSize(int size) {
        return (int)(size * mContext.getResources().getDisplayMetrics()
                                    .scaledDensity);
    }

    /**
     * Render page frame
     */
    public abstract void onDrawFrame();

    /**
     * 重设置 画下页
     */
    public abstract void onDrawNextFrame(boolean isFlow);
    //重画背景
    public abstract void onReDrawFrame();

    /**
     * Handle surface changing event
     *
     * @param width surface width
     * @param height surface height
     */
    public abstract void onSurfaceChanged(int width, int height);

    /**
     * Handle drawing ended event
     *
     * @param what draw command
     * @return true if render is needed
     */
    public abstract boolean onEndedDrawing(int what);
}
