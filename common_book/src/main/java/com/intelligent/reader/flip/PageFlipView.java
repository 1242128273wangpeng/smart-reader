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
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.intelligent.reader.flip.base.PageFlip;
import com.intelligent.reader.flip.base.PageFlipException;
import com.intelligent.reader.flip.base.PageFlipState;
import com.intelligent.reader.flip.render.PageRender;
import com.intelligent.reader.flip.render.SinglePageRender;
import com.intelligent.reader.util.DisplayUtils;

import net.lzbook.kit.utils.AppLog;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Page flip view
 *
 * @author eschao
 */

public class PageFlipView extends GLTextureView implements GLTextureView.Renderer, Observer {

    private final static String TAG = "PageFlipView";

    private static final int MIN_FLING_VELOCITY = 400; // dips
    private static final int MIN_DISTANCE_FOR_FLING = 25; // dips
    private final int mFlingDistance;
    private final int mMinimumVelocity;

    Handler mHandler;
    PageFlip mPageFlip;
    PageRender mPageRender;

    public boolean canFlip = true;

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
        mPageFlip = new PageFlip(this);
        mPageFlip.setSemiPerimeterRatio(1.0f)//圆柱半径
                .setShadowWidthOfFoldEdges(1, 30, 0.2f)//折叠页的边缘阴影颜色
                .setShadowColorOfFoldEdges(0.0f, 0.15f, 0.0f, 0.0f)

                .setShadowWidthOfFoldBase(80, 220, 1.0f)
                .setShadowColorOfFoldBase(0.0f, 0.4f, 0.0f, 0.0f)
                .setPixelsOfMesh(pixelsOfMesh)
                .enableAutoPage(isAuto);
//        setEGLContextClientVersion(2);

        // init others
        mPageRender = new SinglePageRender(context, mPageFlip,
                mHandler);
        //setting
        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true);
        setEGLConfigChooser(5, 6, 5, 0, 16, 0);
//        setEGLWindowSurfaceFactory(new GLSurfaceView.DefaultWindowSurfaceFactory());
//        setZOrderOnTop(true);
//        getHolder().setFormat(PixelFormat.TRANSLUCENT);//设置透明
        // configure render
        setRenderer(this);
        setRenderMode(GLTextureView.RENDERMODE_WHEN_DIRTY);

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

    private float downX = 0F;

    private boolean isFilledFirstTexture = false;
    private boolean isFilledSecondTexture = false;

    public volatile Bitmap firstTexture = null;
    public volatile Bitmap secondTexture = null;

    public synchronized boolean hasFirstTexture() {
        return isFilledFirstTexture || (firstTexture != null && !firstTexture.isRecycled());
    }

    public synchronized boolean hasSecondTexture() {
        return isFilledSecondTexture || (secondTexture != null && !secondTexture.isRecycled());
    }

    private float limitX(float x) {
        return Math.max(1, Math.min(x, getWidth() - 1));
    }

    private float limitY(float Y) {
        return Math.max(1, Math.min(Y, getHeight() - 1));
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
        canFlip = false;
        queueEvent(new Runnable() {
            @Override
            public void run() {
                isFilledFirstTexture = false;
                isFilledSecondTexture = false;
                mPageFlip.onFingerDown(limitX(x), limitY(y));
            }
        });
    }

    private void log(String msg) {
//        android.util.Log.w("curl", msg);
    }

    /**
     * Handle finger moving event
     *
     * @param x finger x coordinate
     * @param y finger y coordinate
     */
    public synchronized void onFingerMove(final float x, final float y) {

        queueEvent(new Runnable() {
            @Override
            public void run() {

                fillTextures();
                if (isFilledFirstTexture
                        && isFilledSecondTexture) {
                    if (mPageFlip.isAnimating()) {
                        mPageFlip.forceFinishAnimating();
                    }

                    if (mPageFlip.onFingerMove(limitX(x), limitY(y))) {

                        if (mPageRender != null &&
                                mPageRender.onFingerMove()) {
                        }
                    }

                    AppLog.e("flip","fingerMove2");
                    requestRender();
                }else{
//                    if (mPageFlip.isAnimating()) {
//                        mPageFlip.forceFinishAnimating();
//                    }
                    isFilledFirstTexture = false;
                    isFilledSecondTexture = false;
                }
            }
        });
    }

    /**
     * Handle finger up event and start animating if need
     *
     * @param x finger x coordinate
     * @param y finger y coordinate
     */
    public synchronized void onFingerUp(final float x, final float y, final float xVelocity, final float yVelocity) {

        queueEvent(new Runnable() {
                       @Override
                       public void run() {

                           fillTextures();
                           if (isFilledFirstTexture
                                   && isFilledSecondTexture) {
                               if (mPageFlip.isAnimating()) {
                                   mPageFlip.forceFinishAnimating();
                               }

                               boolean forceFlip = false;
                               boolean shouldBack = false;

                               boolean canUseVelocity = mFlingDistance < Math.abs(downX - x) && (Math.abs(xVelocity) > mMinimumVelocity
                                       || Math.abs(yVelocity) > mMinimumVelocity);

                               if (mPageFlip.getFlipState() == PageFlipState.FORWARD_FLIP) {
                                   if (canUseVelocity) {
                                       forceFlip = xVelocity < 0;
                                       shouldBack = !forceFlip;
                                   }
                               } else if (mPageFlip.getFlipState() == PageFlipState.BACKWARD_FLIP) {
                                   if (canUseVelocity) {
                                       forceFlip = xVelocity > 0;
                                       shouldBack = !forceFlip;
                                   }
                               }

                               if(mPageFlip.getFlipState() == PageFlipState.BACKWARD_FLIP
                                       || mPageFlip.getFlipState() == PageFlipState.FORWARD_FLIP){
                                   mPageFlip.onFingerUp(limitX(x), limitY(y), forceFlip, shouldBack);

                                   if (mPageRender != null &&
                                           mPageRender.onFingerUp()) {
                                   }

//                                   requestRender();
                               } else {

                                   //先绘制一帧

                                   final boolean finalForceFlip = forceFlip;
                                   final boolean finalShouldBack = shouldBack;
                                   mPageRender.afterFirstDrawEvent.add(new Runnable() {
                                       @Override
                                       public void run() {
                                           mPageFlip.onFingerUp(limitX(x), limitY(y), finalForceFlip, finalShouldBack);

                                           if (mPageRender != null &&
                                                   mPageRender.onFingerUp()) {
                                           }

                                           requestRender();
                                       }
                                   });
                               }
                               requestRender();

                           }
                           isFilledFirstTexture = false;
                           isFilledSecondTexture = false;

                       }
                   }
        );

    }

    private synchronized void fillTextures() {

        if (!isFilledFirstTexture && firstTexture != null && !firstTexture.isRecycled()) {
            isFilledFirstTexture = mPageFlip.getFirstPage().setFirstTexture(firstTexture);
            log("fillTextures");
        }

        if (!isFilledSecondTexture && secondTexture != null && !secondTexture.isRecycled()) {
            isFilledSecondTexture = mPageFlip.getFirstPage().setSecondTexture(secondTexture);
        }

        if (isFilledFirstTexture && isFilledSecondTexture && getAlpha() != 1.0F) {
            Message obtain = Message.obtain();
            obtain.what = -1;
            mHandler.sendMessage(obtain);
        }

        firstTexture = null;
        secondTexture = null;
    }

    public void setPageFlipStateListenerListener(SinglePageRender.PageFlipStateListener mPageFlipStateListener) {
        this.mPageFlip.setPageFlipStateListenerListener(mPageFlipStateListener);
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
//                    case PageRender.MSG_ENDED_DRAWING_FRAME:
//
//                        if (mPageRender != null &&
//                                mPageRender.onEndedDrawing(msg.arg1)) {
//                            requestRender();
//                        }
//                        break;
                    case PageRender.MSG_ENDED_FLIP_DOWN:
                        //翻下页
                        onFingerDown(DisplayUtils.getScreenWight(getContext()) / 2 + 500, DisplayUtils.getScreenHeight(getContext()) / 2);
                        onFingerUp(DisplayUtils.getScreenWight(getContext()) / 2 + 500, DisplayUtils.getScreenHeight(getContext()) / 2, 10000, 0);
                    case PageRender.MSG_ENDED_FLIP_UP:
                        //翻下页
                        onFingerDown(DisplayUtils.getScreenWight(getContext()) / 2 - 500, DisplayUtils.getScreenHeight(getContext()) / 2);
                        onFingerUp(DisplayUtils.getScreenWight(getContext()) / 2 - 500, DisplayUtils.getScreenHeight(getContext()) / 2, 10000, 0);
                    case PageRender.MSG_ENDED_SET_PAGE:

                        requestRender();

                        break;
                    case -1:
                        if (getAlpha() != 1.0F) {
                            setAlpha(1.0F);
                        }
                        break;
                    default:
                        if (getAlpha() != 1.0F) {
                            setAlpha(1.0F);
                        }
                        break;
                }
            }
        };
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        try {
            mPageFlip.onSurfaceCreated();
        } catch (PageFlipException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to run PageFlipFlipRender:onSurfaceCreated");
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                setAlpha(0F);
            }
        });
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
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

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                setAlpha(0F);
            }
        });
    }

//    @Override
//    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//        return super.onSurfaceTextureDestroyed(surface);
//    }

    @Override
    public boolean onDrawFrame(GL10 gl) {
        if (mPageRender != null && mPageFlip.getFirstPage().isFirstTextureSet()
                && mPageFlip.getFirstPage().isSecondTextureSet()) {
            mPageRender.onDrawFrame();
            return true;
        }else{
            AppLog.e("flip","miss onDrawFrame");
        }
        return false;
    }


    public void onSurfaceDestroyed() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                setAlpha(0F);
            }
        });
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
