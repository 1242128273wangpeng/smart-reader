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
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.intelligent.reader.activity.ReadingActivity;
import com.intelligent.reader.flip.base.PageFlip;
import com.intelligent.reader.flip.base.PageFlipException;
import com.intelligent.reader.flip.render.PageRender;
import com.intelligent.reader.flip.render.SinglePageRender;
import com.intelligent.reader.flip.texture.BaseGLTextureView;
import com.intelligent.reader.flip.texture.GLViewRenderer;
import com.intelligent.reader.util.DisplayUtils;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Page flip view
 *
 * @author eschao
 */

public class PageFlipView extends BaseGLTextureView implements GLViewRenderer {

    private final static String TAG = "PageFlipView";

    int mPageNo;
    int mDuration;
    Handler mHandler;
    PageFlip mPageFlip;
    PageRender mPageRender;
    ReentrantLock mDrawLock;

    private boolean isFangzhen;

    public boolean isFangzhen() {
        return isFangzhen;
    }

    public void setBeginLisenter(PageFlip.BeginListener beginLisenter){
        mPageFlip.setBeginListener(beginLisenter);
    }

    public void setFangzhen(boolean fangzhen) {
        if (!fangzhen) {
            mPageRender.release();
            mPageFlip.deleteUnusedTextures();
        }
        isFangzhen = fangzhen;
    }

    public PageRender getmPageRender() {
        return mPageRender;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public PageFlipView(final Context context) {
        super(context);

        // create handler to tackle message
        newHandler();

        // load preferences
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(context);
        mDuration = pref.getInt(Constants.PREF_DURATION, 3000);
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
        mPageFlip.setShadowColorOfFoldBase(0.1f, 0.7f, 0.5f, 0.3f);//
        // init others
//        mPageNo  = Integer.MAX_VALUE/4;
        mPageNo = Integer.MAX_VALUE/2;
        mDrawLock = new ReentrantLock();
        mPageRender = new SinglePageRender(context, mPageFlip,
                mHandler, mPageNo);
        // configure render
        setRenderer(this);
//        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
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

    /**
     * Handle finger down event
     *
     * @param x finger x coordinate
     * @param y finger y coordinate
     */
    public void onFingerDown(float x, float y) {
        // if the animation is going, we should ignore this event to avoid
        // mess drawing on screen
        if (!mPageFlip.isAnimating() &&
                mPageFlip.getFirstPage() != null) {
            mPageFlip.onFingerDown(x, y);
        }
    }

    /**
     * Handle finger moving event
     *
     * @param x finger x coordinate
     * @param y finger y coordinate
     */
    public void onFingerMove(float x, float y) {
        if (mPageFlip.isAnimating()) {
            // nothing to do during animating
        } else if (mPageFlip.canAnimate(x, y)) {
            // if the point is out of current page, try to start animating
            onFingerUp(x, y);
        }
        // move page by finger
        else if (mPageFlip.onFingerMove(x, y)) {
            try {
                mDrawLock.lock();
                if (mPageRender != null &&
                        mPageRender.onFingerMove(x, y)) {
                    requestRender();
                }
            } finally {
                mDrawLock.unlock();
            }
        }
    }

    /**
     * Handle finger up event and start animating if need
     *
     * @param x finger x coordinate
     * @param y finger y coordinate
     */
    public void onFingerUp(float x, float y) {
        if (!mPageFlip.isAnimating()) {
            mPageFlip.onFingerUp(x, y, mDuration);
            try {
                mDrawLock.lock();
                if (mPageRender != null &&
                        mPageRender.onFingerUp(x, y)) {
                    requestRender();
                }
            } finally {
                mDrawLock.unlock();
            }
        }
    }

    @Override
    public void onSurfaceCreated() {
        try {
            mPageFlip.onSurfaceCreated();
        } catch (PageFlipException e) {
            Log.e(TAG, "Failed to run PageFlipFlipRender:onSurfaceCreated");
        }
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        try {
            mPageFlip.onSurfaceChanged(width, height);

            // if there is the second page, create double page render when need
            int pageNo = mPageRender.getPageNo();
            if (mPageFlip.getSecondPage() != null && width > height) {
//                if (!(mPageRender instanceof DoublePagesRender)) {
//                    mPageRender.release();
//                    mPageRender = new DoublePagesRender(getContext(),
//                            mPageFlip,
//                            mHandler,
//                            pageNo);
//                }
            }
            // if there is only one page, create single page render when need
            else if (!(mPageRender instanceof SinglePageRender)) {
                mPageRender.release();
                mPageRender = new SinglePageRender(getContext(),
                        mPageFlip,
                        mHandler,
                        pageNo);
            }

            // let page render handle surface change
            mPageRender.onSurfaceChanged(width, height);
        } catch (PageFlipException e) {
            Log.e(TAG, "Failed to run PageFlipFlipRender:onSurfaceChanged");
        }
    }

    @Override
    public void onDrawFrame() {
        try {
            mDrawLock.lock();
            if (mPageRender != null) {
                mPageRender.onDrawFrame();
            }
        } finally {
            mDrawLock.unlock();
        }
    }

    public void onDrawNextFrame(boolean isFlow) {
        try {
            mDrawLock.lock();
            if (mPageRender != null) {
                mPageRender.onDrawNextFrame(isFlow);
            }
        } finally {
            mDrawLock.unlock();
        }
    }
    public void onReDrawFrame() {
        try {
            mDrawLock.lock();
            if (mPageRender != null) {
                mPageRender.onReDrawFrame();
            }
        } finally {
            mDrawLock.unlock();
        }
    }
//    /**
//     * Draw frame
//     *
//     * @param gl OpenGL handle
//     */
//    @Override
//    public void onDrawFrame(GL10 gl) {
//        try {
//            mDrawLock.lock();
//            if (mPageRender != null) {
//                mPageRender.onDrawFrame();
//            }
//        }
//        finally {
//            mDrawLock.unlock();
//        }
//    }
//
//    /**
//     * Handle surface is changed
//     *
//     * @param gl OpenGL handle
//     * @param width new width of surface
//     * @param height new height of surface
//     */
//    @Override
//    public void onSurfaceChanged(GL10 gl, int width, int height) {
//        try {
//            mPageFlip.onSurfaceChanged(width, height);
//
//            // if there is the second page, create double page render when need
//            int pageNo = mPageRender.getPageNo();
//            if (mPageFlip.getSecondPage() != null && width > height) {
//                if (!(mPageRender instanceof DoublePagesRender)) {
//                    mPageRender.release();
//                    mPageRender = new DoublePagesRender(getContext(),
//                                                        mPageFlip,
//                                                        mHandler,
//                                                        pageNo);
//                }
//            }
//            // if there is only one page, create single page render when need
//            else if(!(mPageRender instanceof SinglePageRender)) {
//                mPageRender.release();
//                mPageRender = new SinglePageRender(getContext(),
//                                                   mPageFlip,
//                                                   mHandler,
//                                                   pageNo);
//            }
//
//            // let page render handle surface change
//            mPageRender.onSurfaceChanged(width, height);
//        }
//        catch (PageFlipException e) {
//            Log.e(TAG, "Failed to run PageFlipFlipRender:onSurfaceChanged");
//        }
//    }
//
//    /**
//     * Handle surface is created
//     *
//     * @param gl OpenGL handle
//     * @param config EGLConfig object
//     */
//    @Override
//    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//        try {
//            mPageFlip.onSurfaceCreated();
//        }
//        catch (PageFlipException e) {
//            Log.e(TAG, "Failed to run PageFlipFlipRender:onSurfaceCreated");
//        }
//    }

    /**
     * Create message handler to cope with messages from page render,
     * Page render will send message in GL thread, but we want to handle those
     * messages in main thread that why we need handler here
     */
    private void newHandler() {
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case PageRender.MSG_ENDED_DRAWING_FRAME:
                        try {
                            mDrawLock.lock();
                            // notify page render to handle ended drawing
                            // message
                            if (mPageRender != null &&
                                    mPageRender.onEndedDrawing(msg.arg1)) {
                                requestRender();
                            }
                        } finally {
                            mDrawLock.unlock();
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
                        try {
                            mDrawLock.lock();
                            // notify page render to handle ended drawing
                            // message
                            if (mPageRender != null &&
                                    mPageRender.onEndedDrawing(msg.arg1)) {
//                                requestRender();
                            }
                        } finally {
                            mDrawLock.unlock();
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }

}
