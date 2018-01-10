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
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;

import com.intelligent.reader.flip.base.Page;
import com.intelligent.reader.flip.base.PageFlip;
import com.intelligent.reader.flip.base.PageFlipState;

import net.lzbook.kit.utils.AppLog;


/**
 * Single page render
 * <p>
 * Every page need 2 texture in single page mode:
 * <ul>
 * <li>First texture: current page content</li>
 * <li>Back texture: back of front content, it is same with first texture
 * </li>
 * <li>Second texture: next page content</li>
 * </ul>
 * </p>
 *
 * @author eschao
 */

public class SinglePageRender extends PageRender {
    public interface LoadBitmapListener {
        Bitmap loadBitmap(int number);
    }

    public interface PageFlipStateListener {
        void backward(int mPageNo);

        void forward(int mPageNo);

        void restore(int mPageNo);

        void gone();
    }

    private LoadBitmapListener listener;

    private PageFlipStateListener mPageFlipStateListener;

    public void setListener(LoadBitmapListener listener) {
        this.listener = listener;
    }

    public void setPageFlipStateListenerListener(PageFlipStateListener mPageFlipStateListener) {
        this.mPageFlipStateListener = mPageFlipStateListener;
    }

    /**
     * Constructor
     *
     * @see {@link #PageRender (Context, PageFlip, Handler, int)}
     */
    public SinglePageRender(Context context, PageFlip pageFlip,
                            Handler handler, int pageNo) {
        super(context, pageFlip, handler, pageNo);
    }

    /**
     * Draw frame 画布局
     */
    public void onDrawFrame() {
        Page page = mPageFlip.getFirstPage();
        page.deleteUnusedTextures();
        // 2. handle drawing command triggered from finger moving and animating 处理滑动命令
        if (mDrawCommand == DRAW_MOVING_FRAME ||
                mDrawCommand == DRAW_ANIMATING_FRAME) {
            // is forward flip 向前滑动
            if (mPageFlip.getFlipState() == PageFlipState.FORWARD_FLIP) {
                if (!page.isFirstTextureSet()) {
//                drawPage(--mPageNo, 1);
                    page.setFirstTexture(listener.loadBitmap(mPageNo));
                }
                // check if second texture of first page is valid, if not,//第一页第二页是否有效
                // create new one
                if (!page.isSecondTextureSet()) {
//                    drawPage(mPageNo + 1, 1);
                    page.setSecondTexture(listener.loadBitmap(mPageNo + 1));
                }
            }
            // in backward flip, check first texture of first page is valid 向后翻页 检查第一个页是否有效
            else {
                if (!page.isFirstTextureSet()) {
//                drawPage(--mPageNo, 1);
                    page.setFirstTexture(listener.loadBitmap(mPageNo - 1));
                }
                if (!page.isSecondTextureSet()) {
                    page.setSecondTexture(listener.loadBitmap(mPageNo));
                }
            }

            // draw frame for page flip 翻页
            mPageFlip.drawFlipFrame();
        }
        // draw stationary page without flipping 没有翻转的 画静止页
        else if (mDrawCommand == DRAW_FULL_PAGE) {

//            if (!page.isFirstTextureSet()) {
//                //第一页 不绘制
//                drawPage(mPageNo, 1);//1正常 0特殊
////                page.setFirstTexture(mPreBitmap);
//                page.setFirstTexture(mBitmap);
//                //================翻页====================
//                mPageFlip.drawPageFrame();
//                sendMessageDown();
//                return;
//            }

            if (!page.isFirstTextureSet()) {
                page.setFirstTexture(listener.loadBitmap(mPageNo));
            }

            mPageFlip.drawPageFrame();
        }

        page.deleteUnusedTextures();

        // 3. send message to main thread to notify drawing is ended so that
        // we can continue to calculate next animation frame if need.
        // Remember: the drawing operation is always in GL thread instead of
        // main thread

        sendMessage();

    }

    @Override
    public void onDrawNextFrame(boolean isFlow) {
        //重新画
        Page page = mPageFlip.getFirstPage();
//        if (isFlow) {
//            if (!page.isSecondTextureSet()) {
//                drawPage(++mPageNo, 1);
//                page.setSecondTexture(mBitmap);
//            }
//        }else {
//            if(!page.isFirstTextureSet()) {
//                drawPage(--mPageNo, 1);
//                page.setFirstTexture(mBitmap);
//            }
//        }
//        if (isFlow) {
//            page.setFirstTextureWithSecond();
//            page.setSecondTexture(listener.loadBitmap(mPageNo + 1));
//        } else {
//            page.setSecondTextureWithFirst();
//            page.setFirstTexture(listener.loadBitmap(mPageNo - 1));
//        }

        page.invalidAllTextures();
//        mPageFlip.deleteUnusedTextures();
        setPageMessage();
    }

    @Override
    public void onReDrawFrame() {
        //重新画
        mPageFlip.deleteUnusedTextures();
        Page page = mPageFlip.getFirstPage();
//        drawPage(mPageNo, 1);
        Bitmap loadBitmap = listener.loadBitmap(mPageNo);
        page.setFirstTexture(loadBitmap);
        page.setSecondTexture(listener.loadBitmap(mPageNo + 1));
        sendMessage();
    }

    public void sendMessage() {
        Message msg = Message.obtain();
        msg.what = MSG_ENDED_DRAWING_FRAME;
        msg.arg1 = mDrawCommand;
        mHandler.sendMessage(msg);
    }

    public void sendMessageDown() {
        Message msg = Message.obtain();
        msg.what = MSG_ENDED_FLIP_DOWN;
        msg.arg1 = mDrawCommand;
        mHandler.sendMessage(msg);
    }

    public void sendMessageUp() {
        Message msg = Message.obtain();
        msg.what = MSG_ENDED_FLIP_UP;
        msg.arg1 = mDrawCommand;
        mHandler.sendMessage(msg);
    }

    public void setPageMessage() {
        Message msg = Message.obtain();
        msg.what = MSG_ENDED_SET_PAGE;
        msg.arg1 = mDrawCommand;
        mHandler.sendMessage(msg);
    }

    /**
     * Handle GL surface is changed
     *
     * @param width  surface width
     * @param height surface height
     */
    public void onSurfaceChanged(int width, int height) {
        // recycle bitmap resources if need

        // create bitmap and canvas for page
        //mBackgroundBitmap = background;
        Page page = mPageFlip.getFirstPage();
//        LoadBitmapTask.get(mContext).set(width, height, 1);
    }

    /**
     * Handle ended drawing event
     * In here, we only tackle the animation drawing event, If we need to
     * continue requesting render, please return true. Remember this function
     * will be called in main thread
     *
     * @param what event type
     * @return ture if need render again
     */
    public boolean onEndedDrawing(int what) {
        if (what == DRAW_ANIMATING_FRAME) {
            boolean isAnimating = mPageFlip.animating();
            // continue animating
            if (isAnimating) {
                mDrawCommand = DRAW_ANIMATING_FRAME;
                return true;
            }
            // animation is finished
            else {
                final PageFlipState state = mPageFlip.getFlipState();
                // update page number for backward flip
                if (state == PageFlipState.END_WITH_BACKWARD) {
                    // don't do anything on page number since mPageNo is always
                    // represents the FIRST_TEXTURE no;
                    mPageNo--;
                    if (mPageFlipStateListener != null) {
                        mPageFlipStateListener.backward(mPageNo);
                    }
                }
                // update page number and switch textures for forward flip
                else if (state == PageFlipState.END_WITH_FORWARD) {
                    mPageFlip.getFirstPage().setFirstTextureWithSecond();
                    mPageNo++;
                    if (mPageFlipStateListener != null) {
                        mPageFlipStateListener.forward(mPageNo);
                    }
                } else if (state == PageFlipState.END_WITH_RESTORE) {
                    if (mPageFlipStateListener != null) {
                        mPageFlipStateListener.restore(mPageNo);
                    }
                }

                mDrawCommand = DRAW_FULL_PAGE;
                return true;
            }
        } else if (what == DRAW_FULL_PAGE) {
            mPageFlipStateListener.gone();
        }
        return false;
    }

    /**
     * Draw page content
     *
     * @param number page number
     */
//    private void drawPage(final int number, int type) {
//        AppLog.e("number",number+"");
//        final int width = mCanvas.getWidth();
//        final int height = mCanvas.getHeight();
//        final Paint p = new Paint();
//        p.setFilterBitmap(true);
//
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                Bitmap background = listener.loadBitmap(number);
//                Rect rect = new Rect(0, 0, width, height);
//                mCanvas.drawBitmap(background, null, rect, p);
//            }
//        });
//    }

    /**
     * If page can flip forward
     *
     * @return true if it can flip forward
     */
    public boolean canFlipForward() {
        return (mPageNo < MAX_PAGES);
    }

    /**
     * If page can flip backward
     *
     * @return true if it can flip backward
     */
    public boolean canFlipBackward() {
        if (mPageNo > 1) {
            mPageFlip.getFirstPage().setSecondTextureWithFirst();
            return true;
        } else {
            return false;
        }
    }
}
