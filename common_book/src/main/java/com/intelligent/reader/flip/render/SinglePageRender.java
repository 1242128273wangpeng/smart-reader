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
import android.os.Handler;
import android.os.Message;

import com.intelligent.reader.flip.base.Page;
import com.intelligent.reader.flip.base.PageFlip;
import com.intelligent.reader.flip.base.PageFlipState;

import net.lzbook.kit.utils.AppLog;

import static com.intelligent.reader.flip.base.PageFlipState.END_WITH_FORWARD;
import static com.intelligent.reader.flip.base.PageFlipState.END_WITH_RESTORE_BACKWARD;


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

    public interface PageFlipStateListener {
        boolean backward();

        boolean forward();

        boolean restore();
    }

    /**
     * Constructor
     *
     * @see {@link #PageRender (Context, PageFlip, Handler, int)}
     */
    public SinglePageRender(Context context, PageFlip pageFlip,
                            Handler handler) {
        super(context, pageFlip, handler);
    }

    private boolean isShouldShow = false;

    /**
     * Draw frame 画布局
     */
    public void onDrawFrame() {
        Page page = mPageFlip.getFirstPage();
        page.deleteUnusedTextures();
        // 2. handle drawing command triggered from finger moving and animating 处理滑动命令
        if (mDrawCommand == DRAW_MOVING_FRAME ||
                mDrawCommand == DRAW_ANIMATING_FRAME) {

            // draw frame for page flip 翻页
            mPageFlip.drawFlipFrame();

//            //防止闪屏
//            if(isShouldShow && mPageFlip.getFirstPage().isFirstTextureSet() && mPageFlip.getFirstPage().isSecondTextureSet()) {
            Message obtain = Message.obtain();
            obtain.what = -1;
            mHandler.sendMessage(obtain);
//            }
//
//            if(!isShouldShow) {
//                isShouldShow = true;
//            }
        }
        // draw stationary page without flipping 没有翻转的 画静止页
        else if (mDrawCommand == DRAW_FULL_PAGE /*|| mDrawCommand == DRAW_DELETE_AFTER_FIRST_PAGE*/) {

            mPageFlip.drawPageFrame();


//            if(mDrawCommand == DRAW_DELETE_AFTER_FIRST_PAGE){
//                mPageFlip.getFirstPage().deleteAllTextures();
//            }

//            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

            Message obtain = Message.obtain();
            obtain.what = -1;
            mHandler.sendMessage(obtain);

            while (!afterFirstDrawEvent.isEmpty()){
                afterFirstDrawEvent.remove(0).run();
            }
        }

        AppLog.v("flip","single");
        // 3. send message to main thread to notify drawing is ended so that
        // we can continue to calculate next animation frame if need.
        // Remember: the drawing operation is always in GL thread instead of
        // main thread

        page.deleteUnusedTextures();

        sendMessage();

    }

    @Override
    public void onDrawNextFrame(boolean isFlow) {

    }

    @Override
    public void onReDrawFrame() {
        //重新画
    }

    public void sendMessage() {

        if (onEndedDrawing(mDrawCommand)) {
            mPageFlip.mView.requestRender();
        }
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
//        Page page = mPageFlip.getFirstPage();
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
                mDrawCommand = mPageFlip.mEndAnimationCommand;
                final PageFlipState state = mPageFlip.getFlipState();
                // update page number for backward flip
                switch (state){
                    case END_WITH_FORWARD :
                    case END_WITH_RESTORE_BACKWARD :
                        mPageFlip.getFirstPage().setFirstTextureWithSecond();
                        break;
                    default:
                        break;
                }

                mPageFlip.setFlipState(PageFlipState.BEGIN_FLIP);
                mPageFlip.mView.post(new Runnable() {
                    @Override
                    public void run() {
                        AppLog.e("flip", "onEndedDrawing");
                        mPageFlip.mView.setAlpha(0F);
                    }
                });
                mPageFlip.mView.canFlip = true;
                //绘制最后一帧后，删除第一页文理
                return true;
            }
        }
//        else if(DRAW_DELETE_AFTER_FIRST_PAGE == mDrawCommand){
////            final PageFlipState state = mPageFlip.getFlipState();
////            mPageFlip.getFirstPage().deleteAllTextures();
//            mDrawCommand = DRAW_FULL_PAGE;
//        }

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
        return true;
    }

    /**
     * If page can flip backward
     *
     * @return true if it can flip backward
     */
    public boolean canFlipBackward() {
        return true;
    }
}
