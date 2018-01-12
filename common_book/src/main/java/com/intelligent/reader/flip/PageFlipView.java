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
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

import com.intelligent.reader.flip.base.PageFlip;
import com.intelligent.reader.flip.base.PageFlipException;
import com.intelligent.reader.flip.base.DefaultWindowSurfaceFactory;
import com.intelligent.reader.flip.render.PageRender;
import com.intelligent.reader.flip.render.SinglePageRender;
import com.intelligent.reader.util.DisplayUtils;

import net.lzbook.kit.data.bean.ReadConfig;
import net.lzbook.kit.utils.AppUtils;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Page flip view
 *
 * @author eschao
 */

public class PageFlipView extends GLSurfaceView implements GLSurfaceView.Renderer {

    private final static String TAG = "PageFlipView";


    int mDuration;
    Handler mHandler;
    PageFlip mPageFlip;
    PageRender mPageRender;

    private boolean isDownActioned = false;

    private boolean isFangzhen;
    public volatile boolean surfaceAviable = false;

    public boolean isFangzhen() {
        return isFangzhen;
    }

    public void setBeginLisenter(PageFlip.BeginListener beginLisenter) {
        mPageFlip.setBeginListener(beginLisenter);
    }

    public void setFangzhen(boolean fangzhen) {
        if (!fangzhen) {
//            if (mPageRender != null) mPageRender.release();
            if (mPageFlip != null) mPageFlip.deleteUnusedTextures();
        }
        isFangzhen = fangzhen;
    }

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
        mDuration = pref.getInt(Constants.PREF_DURATION, 800);
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
        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true);
        setEGLConfigChooser(8, 8, 8, 0, 16, 0);
        setEGLWindowSurfaceFactory(new DefaultWindowSurfaceFactory());
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);//设置透明
        // configure render
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
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
     * Get duration of animating
     *
     * @return duration of animating
     */
    public int getAnimateDuration() {
        return mDuration;
    }

    /**
     * Set animate duration
     *
     * @param duration duration of animating
     */
    public void setAnimateDuration(int duration) {
        mDuration = duration;
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

    /**
     * Handle finger down event
     *
     * @param x finger x coordinate
     * @param y finger y coordinate
     */
    public synchronized void onFingerDown(final float x, final float y) {
        Runnable event = new Runnable() {
            @Override
            public void run() {
                // if the animation is going, we should ignore this event to avoid
                // mess drawing on screen
                if (!isDownActioned && !mPageFlip.isAnimating() &&
                        mPageFlip.getFirstPage() != null) {
                    mPageFlip.onFingerDown(x, y);
                    isDownActioned = true;
                    log("down");
                } else {
                    log("down miss");
                }
            }
        };
        if(getVisibility() == VISIBLE && surfaceAviable) {
            queueEvent(event);
        }else{
            mbeforeEventQueue.add(event);
        }
    }

    private void log(String msg){
        android.util.Log.w("PageFlipView", msg);
    }

    /**
     * Handle finger moving event
     *
     * @param x finger x coordinate
     * @param y finger y coordinate
     */
    public synchronized void onFingerMove(final float x, final float y) {

        Runnable event = new Runnable() {
            @Override
            public void run() {
                if(!isDownActioned)
                    return;
                if (mPageFlip.isAnimating()) {
                    // nothing to do during animating
                    log("onFingerMove isAnimating");
                } else if (mPageFlip.canAnimate(x, y)) {
                    // if the point is out of current page, try to start animating
                    onFingerUp(x, y);
                    log("onFingerMove canAnimate");
                }
                // move page by finger
                else if (mPageFlip.onFingerMove(x, y)) {

                    if (mPageRender != null &&
                            mPageRender.onFingerMove(x, y)) {
                    }

                    log("onFingerMove");
                }else {
                    log("onFingerMove miss");
                }

                requestRender();
            }
        };

        if(getVisibility() == VISIBLE && surfaceAviable) {
            queueEvent(event);
        }else{
            mbeforeEventQueue.add(event);
        }

    }

    /**
     * Handle finger up event and start animating if need
     *
     * @param x finger x coordinate
     * @param y finger y coordinate
     */
    public synchronized void onFingerUp(final float x, final float y) {

        Runnable event = new Runnable() {
            @Override
            public void run() {
                if(isDownActioned) {

                    if (!mPageFlip.isAnimating()) {
                        mPageFlip.onFingerUp(x, y, mDuration);

                        if (mPageRender != null &&
                                mPageRender.onFingerUp(x, y)) {
                        }
                        log("onFingerUp");
                    } else {
                        log("onFingerUp miss");
                    }

                    requestRender();
                }

                isDownActioned = false;
            }
        };

        if(getVisibility() == VISIBLE && surfaceAviable) {
            queueEvent(event);
        }else{
            mbeforeEventQueue.add(event);
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
     * Draw frame
     *
     * @param gl OpenGL handle
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        surfaceAviable = true;
        if (mPageRender != null) {
            mPageRender.onDrawFrame();
        }
    }

    /**
     * Handle surface is changed
     *
     * @param gl     OpenGL handle
     * @param width  new width of surface
     * @param height new height of surface
     */
    @Override
    public synchronized void onSurfaceChanged(GL10 gl, int width, int height) {
        if(!mbeforeEventQueue.isEmpty()){
            for (Runnable event : mbeforeEventQueue){
                queueEvent(event);
            }
            mbeforeEventQueue.clear();
        }
//        try {
//            mPageFlip.onSurfaceChanged(width, height);
            // let page render handle surface change
//            mPageRender.onSurfaceChanged(width, height);
//        } catch (PageFlipException e) {
//            Log.e(TAG, "Failed to run PageFlipFlipRender:onSurfaceChanged");
//        }
    }

    /**
     * Handle surface is created
     *
     * @param gl     OpenGL handle
     * @param config EGLConfig object
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        try {
            mPageFlip.onSurfaceCreated();
            mPageFlip.onSurfaceChanged(ReadConfig.INSTANCE.getScreenWidth(), ReadConfig.INSTANCE.getScreenHeight());
        } catch (PageFlipException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to run PageFlipFlipRender:onSurfaceCreated");
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        surfaceAviable = false;
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
                        onFingerUp(DisplayUtils.getScreenWight(getContext()) / 2 + 500, DisplayUtils.getScreenHeight(getContext()) / 2);
                    case PageRender.MSG_ENDED_FLIP_UP:
                        //翻下页
                        onFingerDown(DisplayUtils.getScreenWight(getContext()) / 2 - 500, DisplayUtils.getScreenHeight(getContext()) / 2);
                        onFingerUp(DisplayUtils.getScreenWight(getContext()) / 2 - 500, DisplayUtils.getScreenHeight(getContext()) / 2);
                    case PageRender.MSG_ENDED_SET_PAGE:

                        requestRender();

                        break;
                    default:
                        break;
                }
            }
        };
    }

}
