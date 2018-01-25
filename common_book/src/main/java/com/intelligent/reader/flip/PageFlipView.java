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
package com.intelligent.reader.flip;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.intelligent.reader.flip.base.PageFlip;
import com.intelligent.reader.flip.base.PageFlipException;
import com.intelligent.reader.flip.render.PageRender;
import com.intelligent.reader.flip.render.SinglePageRender;
import com.intelligent.reader.flip.texture.BaseGLTextureView;
import com.intelligent.reader.flip.texture.GLViewRenderer;
import com.intelligent.reader.util.DisplayUtils;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import kotlin.jvm.Volatile;

/**
 * Page flip view
 *
 * @author eschao
 */

public class PageFlipView extends BaseGLTextureView implements GLViewRenderer, Observer {

    private final static String TAG = "PageFlipView";

    private static final int MIN_FLING_VELOCITY = 400; // dips
    private static final int MIN_DISTANCE_FOR_FLING = 25; // dips
    private final int mFlingDistance;
    private final int mMinimumVelocity;

    Handler mHandler;
    PageFlip mPageFlip;
    PageRender mPageRender;

    public PageRender getmPageRender() {
        return mPageRender;
    }

    public PageFlipView(final Context context) {
        super(context);

        // create handler to tackle message
        newHandler();

        // load preferences
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(context);
        int pixelsOfMesh = pref.getInt(Constants.PREF_MESH_PIXELS, 10);
        boolean isAuto = pref.getBoolean(Constants.PREF_PAGE_MODE, false);

        // create PageFlip 设置参数
        mPageFlip = new PageFlip(context);
        mPageFlip.setSemiPerimeterRatio(1.0f)//圆柱半径
                .setShadowWidthOfFoldEdges(5, 80, 0.7f)//折叠页的边缘阴影颜色
                .setShadowWidthOfFoldBase(5, 110, 0.7f)
                .setPixelsOfMesh(pixelsOfMesh)
                .enableAutoPage(isAuto);
//        setEGLContextClientVersion(2);
        mPageFlip.setShadowColorOfFoldBase(0.1f, 0.5f, 0.3f, 0.01f);//
        // init others
        mPageRender = new SinglePageRender(context, mPageFlip,
                mHandler);
        //setting
//        setEGLContextClientVersion(2);
//        setPreserveEGLContextOnPause(true);
//        setEGLConfigChooser(8, 8, 8, 0, 16, 0);
//        setEGLWindowSurfaceFactory(new DefaultWindowSurfaceFactory());
//        setZOrderOnTop(true);
//        setZOrderMediaOverlay(true);
//        getHolder().setFormat(PixelFormat.TRANSLUCENT);//设置透明
        // configure render
        setRenderer(this);
//        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        final float density = context.getResources().getDisplayMetrics().density;

        mMinimumVelocity = (int) (MIN_FLING_VELOCITY * density);
        mFlingDistance = (int) (MIN_DISTANCE_FOR_FLING * density);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        setAlpha(0F);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * Is auto page mode enabled?
     *
     * @return true if auto page mode enabled
     */
    public boolean isAutoPageEnabled() {
        return mPageFlip.isAutoPageEnabled();
    }

    /**
     * Enable/Disable auto page mode
     *
     * @param enable true is enable
     */
    public void enableAutoPage(boolean enable) {
//        if (mPageFlip.enableAutoPage(enable)) {
//            try {
//                mDrawLock.lock();
//                if (mPageFlip.getSecondPage() != null &&
//                        mPageRender instanceof SinglePageRender) {
//                    mPageRender = new DoublePagesRender(getContext(),
//                            mPageFlip,
//                            mHandler,
//                            mPageNo);
//                    mPageRender.onSurfaceChanged(mPageFlip.getSurfaceWidth(),
//                            mPageFlip.getSurfaceHeight());
//                }
//                else if (mPageFlip.getSecondPage() == null &&
//                        mPageRender instanceof DoublePagesRender) {
//                    mPageRender = new SinglePageRender(getContext(),
//                            mPageFlip,
//                            mHandler,
//                            mPageNo);
//                    mPageRender.onSurfaceChanged(mPageFlip.getSurfaceWidth(),
//                            mPageFlip.getSurfaceHeight());
//                }
//                requestRender();
//            }
//            finally {
//                mDrawLock.unlock();
//            }
//        }
    }


    /**
     * Get pixels of mesh
     *
     * @return pixels of mesh
     */
    public int getPixelsOfMesh() {
        return mPageFlip.getPixelsOfMesh();
    }

    private ArrayList<Runnable> mbeforeEventQueue = new ArrayList<Runnable>();

    private boolean downActioned = false;
    private float downX = 0F;

    public volatile Bitmap firstTexture = null;
    public volatile Bitmap secondTexture = null;

    public synchronized boolean hasFirstTexture(){
        return firstTexture != null || getmPageRender().mPageFlip.getFirstPage().isFirstTextureSet();
    }

    public synchronized boolean hasSecondTexture(){
        return secondTexture != null || getmPageRender().mPageFlip.getFirstPage().isSecondTextureSet();
    }

    /**
     * Handle finger down event
     *
     * @param x finger x coordinate
     * @param y finger y coordinate
     */
    public synchronized void onFingerDown(final float x, final float y) {
        // if the animation is going, we should ignore this event to avoid
        // mess drawing on screen

        downX = x;

        if (!mPageFlip.isAnimating() &&
                mPageFlip.getFirstPage() != null) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    mPageFlip.onFingerDown(x, y);
//                    requestRender();
                }
            });
            downActioned = true;
            log("down");
        } else {
            log("down miss");
        }
    }

    private void log(String msg) {
        android.util.Log.w("PageFlipView", msg);
    }

    /**
     * Handle finger moving event
     *
     * @param x finger x coordinate
     * @param y finger y coordinate
     */
    public synchronized void onFingerMove(final float x, final float y) {
        if (downActioned && !mPageFlip.isAnimating()) {
            queueEvent(new Runnable() {
                @Override
                public void run() {

                    fillTextures();

                    if (mPageFlip.onFingerMove(x, y)) {

                        if (mPageRender != null &&
                                mPageRender.onFingerMove(x, y)) {
                        }

                        log("onFingerMove");
                    } else {
                        log("onFingerMove miss");
                    }

                    requestRender();
                }
            });
        }
    }

    /**
     * Handle finger up event and start animating if need
     *
     * @param x finger x coordinate
     * @param y finger y coordinate
     */
    public synchronized void onFingerUp(final float x, final float y, final float velocity) {

        if (downActioned && !mPageFlip.isAnimating()) {
            queueEvent(new Runnable() {
                           @Override
                           public void run() {
                               fillTextures();

                               boolean forceFlip = Math.abs(velocity) > mMinimumVelocity;
                               forceFlip = forceFlip && mFlingDistance < Math.abs(downX - x);
                               mPageFlip.onFingerUp(x, y, forceFlip);

                               if (mPageRender != null &&
                                       mPageRender.onFingerUp(x, y)) {
                               }
                               log("onFingerUp");

                               requestRender();
                           }
                       }
            );
        }

        downActioned = false;
    }

    private synchronized void fillTextures() {
        if(firstTexture != null && !firstTexture.isRecycled()){
            mPageFlip.getFirstPage().setFirstTexture(firstTexture);
            firstTexture = null;
        }

        if(secondTexture != null && !secondTexture.isRecycled()){
            mPageFlip.getFirstPage().setSecondTexture(secondTexture);
            secondTexture = null;
        }
    }

    public void onDrawNextFrame(boolean isFlow) {
        if (mPageRender != null) {
            mPageRender.onDrawNextFrame(isFlow);
        }
    }

    public void onReDrawFrame() {
        if (mPageRender != null) {
            mPageRender.onReDrawFrame();
        }
    }

    /**
     * Create message handler to cope with messages from page render,
     * Page render will send message in GL thread, but we want to handle those
     * messages in main thread that why we need handler here
     */
    private void newHandler() {
        mHandler = new Handler() {
            public void handleMessage(final Message msg) {
                switch (msg.what) {
                    case PageRender.MSG_ENDED_DRAWING_FRAME:

                        if (mPageRender != null &&
                                mPageRender.onEndedDrawing(msg.arg1)) {
                            requestRender();
                        }
                        break;
                    case PageRender.MSG_ENDED_FLIP_DOWN:
                        //翻下页
                        onFingerDown(DisplayUtils.getScreenWight(getContext()) / 2 + 500, DisplayUtils.getScreenHeight(getContext()) / 2);
                        onFingerUp(DisplayUtils.getScreenWight(getContext()) / 2 + 500, DisplayUtils.getScreenHeight(getContext()) / 2, 10000);
                    case PageRender.MSG_ENDED_FLIP_UP:
                        //翻下页
                        onFingerDown(DisplayUtils.getScreenWight(getContext()) / 2 - 500, DisplayUtils.getScreenHeight(getContext()) / 2);
                        onFingerUp(DisplayUtils.getScreenWight(getContext()) / 2 - 500, DisplayUtils.getScreenHeight(getContext()) / 2, 10000);
                    case PageRender.MSG_ENDED_SET_PAGE:

                        requestRender();

                        break;
                    default:
                        if(getAlpha() != 1.0F) {
                            setAlpha(1.0F);
                        }
                        break;
                }
            }
        };
    }

    @Override
    public void onSurfaceCreated() {
        try {
            mPageFlip.onSurfaceCreated();
        } catch (PageFlipException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to run PageFlipFlipRender:onSurfaceCreated");
        }
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        try {
            mPageFlip.onSurfaceChanged(width, height);
        } catch (PageFlipException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to run PageFlipFlipRender:onSurfaceChanged");
        }
        if (!mbeforeEventQueue.isEmpty()) {
            for (Runnable event : mbeforeEventQueue) {
                queueEvent(event);
            }
            mbeforeEventQueue.clear();
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return super.onSurfaceTextureDestroyed(surface);
    }

    @Override
    public void onDrawFrame() {
        if (mPageRender != null) {
            mPageRender.onDrawFrame();
        }
    }

    @Override
    public void update(Observable o, final Object arg) {
        if ("READ_INTERLINEAR_SPACE".equals(arg)) {
            onChangTexture();
        } else if ("FONT_SIZE".equals(arg)) {
            onChangTexture();
        } else if ("MODE".equals(arg)) {
            onChangTexture();
        } else if ("JUMP".equals(arg)) {
            onChangTexture();
        }
    }

    public void onChangTexture() {
        post(new Runnable() {
            @Override
            public void run() {
                setAlpha(0f);
            }
        });
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mPageRender.mPageFlip.getFirstPage().deleteAllTextures();
            }
        });
    }

    public void nextPage() {
        ((SinglePageRender) mPageRender).sendMessageDown();
    }
}
