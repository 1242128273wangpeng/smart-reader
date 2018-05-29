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
package com.dy.reader.flip

import android.content.Context
import android.graphics.Point
import android.graphics.PointF
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import android.widget.Scroller
import com.dy.reader.Reader
import com.dy.reader.setting.ReaderSettings
import com.intelligent.reader.reader.v2.flip.CurlInterpolator


/**
 * 3D Style Page Flip
 *
 * @author escchao
 */
open class PageFlip
/**
 * Constructor
 */
(private val mContext: Context) {

    //    val mScroller: Scroller = Scroller(App.app, AccelerateDecelerateInterpolator())
    val mScroller: Scroller = Scroller(Reader.context, CurlInterpolator())

    // view size
    var mViewRect: GLViewRect

    var isMiddleCurl = false
    // the pixel size for each mesh
    private var mPixelsOfMesh: Int = 0
    // gradient shadow texture id
    private var mGradientShadowTextureID: Int = 0
    // touch point and last touch point
    private val mTouchP: PointF
    // the last touch point (could be deleted?)
    private val mLastTouchP: PointF
    // the first touch point when finger down on the screen
    private val mStartTouchP: PointF
    private val mDownTouchP: PointF
    // the middle point between touch point and origin point
    private val mMiddleP: PointF
    // from 2D perspective, the line will intersect Y axis and X axis that being
    // through middle point and perpendicular to the line which is from touch
    // point to origin point, The point on Y axis is mYFoldP, the mXFoldP is on
    // X axis. The mY{X}FoldP1 is up mY{X}FoldP, The mY{X}FoldP0 is under
    // mY{X}FoldP
    //
    //        <----- Flip
    //                          ^ Y
    //                          |
    //                          + mYFoldP1
    //                        / |
    //                       /  |
    //                      /   |
    //                     /    |
    //                    /     |
    //                   /      |
    //                  /       + mYFoldP
    //    mTouchP      /      / |
    //       .        /      /  |
    //               /      /   |
    //              /      /    |
    //             /      /     |
    //            /   .  /      + mYFoldP0
    //           /      /      /|
    //          /      /      / |
    //         /      /      /  |
    //X <-----+------+------+---+ originP
    //   mXFoldP1 mXFoldP mXFoldP0
    //
    private val mYFoldP: PointF
    private val mYFoldP0: PointF
    private val mYFoldP1: PointF
    private val mXFoldP: PointF
    private val mXFoldP0: PointF
    private val mXFoldP1: PointF
    //            ^ Y
    //   mTouchP  |
    //        +   |
    //         \  |
    //          \ |
    //       A ( \|
    // X <--------+ originP
    //
    // A is angle between X axis and line from mTouchP to originP
    // the max curling angle between line from touchP to originP and X axis
    private var mMaxT2OAngleTan: Float = 0.toFloat()
    // another max curling angle when finger moving causes the originP change
    // from (x, y) to (x, -y) which means mirror based on Y axis.
    private var mMaxT2DAngleTan: Float = 0.toFloat()
    // the tan value of current curling angle
    // mKValue = (touchP.y - originP.y) / (touchP.x - originP.x)
    private var mKValue: Float = 0.toFloat()
    // the length of line from mTouchP to originP
    private var mLenOfTouchOrigin: Float = 0.toFloat()
    // the cylinder radius
    private var mR: Float = 0.toFloat()
    // the perimeter ratio of semi-cylinder based on mLenOfTouchOrigin;
    private var mSemiPerimeterRatio: Float = 1.toFloat()
    // Mesh count
    private var mMeshCount: Int = 0
    // edges shadow width of back of fold page
    private val mFoldEdgesShadowWidth: ShadowWidth
    // base shadow width of front of fold page
    private val mFoldBaseShadowWidth: ShadowWidth
    // fold page and shadow vertexes
    private val mFoldFrontVertexes: Vertexes
    private val mFoldBackVertexes: FoldBackVertexes
    private val mFoldEdgesShadow: ShadowVertexes
    private val mFoldBaseShadow: ShadowVertexes
    // Shader program for openGL drawing
    protected val mVertexProgram: VertexProgram
    protected val mFoldBackVertexProgram: FoldBackVertexProgram
    protected val mShadowVertexProgram: ShadowVertexProgram
    // is vertical page flip
    var status: Status = Status.BEGIN
    // pages and page mode
    // in single page mode, there is only one page in the index 0
    // in double pages mode, there are two pages, the first one is always active
    // page which is receiving finger events, for example: finger down/move/up
    /**
     * Get the first page
     * First page is currently operating page which means it is the page user
     * finger is clicking or moving
     *
     * @return flip state, See [Status]
     */
    lateinit var page: Page
        private set
    private var mWidthRationOfClickToFlip: Float = 0.toFloat()
    // listener for page flipping
    var animationNotEnd = false

    /**
     * Get surface width
     *
     * @return surface width
     */
    val surfaceWidth: Int
        get() = mViewRect.surfaceW.toInt()

    /**
     * Is animating ?
     *
     * @return true if page is flipping
     */
    val isAnimating: Boolean
        get() {
            mScroller.computeScrollOffset()
            return !mScroller.isFinished
        }

    init {
        mViewRect = GLViewRect()
        mPixelsOfMesh = DEFAULT_MESH_VERTEX_PIXELS
        mSemiPerimeterRatio = 0.8f
        mWidthRationOfClickToFlip = WIDTH_RATIO_OF_CLICK_TO_FLIP

        // key points
        mMiddleP = PointF()
        mYFoldP = PointF()
        mYFoldP0 = PointF()
        mYFoldP1 = PointF()
        mXFoldP = PointF()
        mXFoldP0 = PointF()
        mXFoldP1 = PointF()
        mTouchP = PointF()
        mLastTouchP = PointF()
        mStartTouchP = PointF()
        mDownTouchP = PointF()

        // init shadow width
        mFoldEdgesShadowWidth = ShadowWidth(5f, 60f, 0.25f)
        mFoldBaseShadowWidth = ShadowWidth(10f, 80f, 0.4f)

        // init shader program
        mVertexProgram = VertexProgram()
        mFoldBackVertexProgram = FoldBackVertexProgram()
        mShadowVertexProgram = ShadowVertexProgram()

        // init vertexes
        mFoldFrontVertexes = Vertexes()
        mFoldBackVertexes = FoldBackVertexes()
        mFoldEdgesShadow = ShadowVertexes(FOLD_TOP_EDGE_SHADOW_VEX_COUNT,
                FOLD_EDGE_SHADOW_START_COLOR,
                FOLD_EDGE_SHADOW_START_ALPHA,
                FOLD_EDGE_SHADOW_END_COLOR,
                FOLD_EDGE_SHADOW_END_ALPHA)
        mFoldBaseShadow = ShadowVertexes(0,
                FOLD_BASE_SHADOW_START_COLOR,
                FOLD_BASE_SHADOW_START_ALPHA,
                FOLD_BASE_SHADOW_END_COLOR,
                FOLD_BASE_SHADOW_END_ALPHA)
    }//        mScroller = new Scroller(context, new AccelerateDecelerateInterpolator());


    /**
     * Set width ratio of clicking to flip, the default is 0.5f
     *
     * Which area the finger is clicking on will trigger a flip forward or
     * backward
     *
     * @param ratio width ratio of clicking to flip, is (0 ... 0.5]
     * @return self
     */
    fun setWidthRatioOfClickToFlip(ratio: Float): PageFlip {
        if (ratio <= 0 || ratio > 0.5f) {
            throw IllegalArgumentException("Invalid ratio value: $ratio")
        }

        mWidthRationOfClickToFlip = ratio
        return this
    }


    /**
     * Sets pixels of each mesh
     *
     * The default value is 10 pixels for each mesh
     *
     * @param pixelsOfMesh pixel amount of each mesh
     * @return self
     */
    fun setPixelsOfMesh(pixelsOfMesh: Int): PageFlip {
        mPixelsOfMesh = if (pixelsOfMesh > 0)
            pixelsOfMesh
        else
            DEFAULT_MESH_VERTEX_PIXELS
        return this
    }

    /**
     * Set ratio of semi-perimeter of fold cylinder
     *
     *
     * When finger is clicking and moving on page, the page from touch point to
     * original point will be curled like as a cylinder, the radius of cylinder
     * is determined by line length from touch point to original point. You can
     * give a ratio of this line length to set cylinder radius, the default
     * value is 0.8
     *
     *
     * @param ratio ratio of line length from touch point to original point. Its
     * value is (0..1]
     * @return self
     */
    fun setSemiPerimeterRatio(ratio: Float): PageFlip {
        if (ratio <= 0 || ratio > 1) {
            throw IllegalArgumentException("Invalid ratio value: $ratio")
        }

        mSemiPerimeterRatio = ratio
        return this
    }

    /**
     * Sets edge shadow color of fold page
     *
     * @param startColor shadow start color: [0..1]
     * @param startAlpha shadow start alpha: [0..1]
     * @param endColor   shadow end color: [0..1]
     * @param endAlpha   shadow end alpha: [0..1]
     * @return self
     */
    fun setShadowColorOfFoldEdges(startColor: Float,
                                  startAlpha: Float,
                                  endColor: Float,
                                  endAlpha: Float): PageFlip {
        mFoldEdgesShadow.mColor.set(startColor, startAlpha,
                endColor, endAlpha)
        return this
    }

    /**
     * Sets base shadow color of fold page
     *
     * @param startColor shadow start color: [0..1]
     * @param startAlpha shadow start alpha: [0..1]
     * @param endColor   shadow end color: [0..1]
     * @param endAlpha   shadow end alpha: [0..1]
     * @return self
     */
    fun setShadowColorOfFoldBase(startColor: Float,
                                 startAlpha: Float,
                                 endColor: Float,
                                 endAlpha: Float): PageFlip {
        mFoldBaseShadow.mColor.set(startColor, startAlpha,
                endColor, endAlpha)
        return this
    }

    /**
     * Set shadow width of fold edges
     *
     * @param min   minimal width
     * @param max   maximum width
     * @param ratio width ratio based on fold cylinder radius. It is in (0..1)
     * @return self
     */
    fun setShadowWidthOfFoldEdges(min: Float,
                                  max: Float,
                                  ratio: Float): PageFlip {
        mFoldEdgesShadowWidth.set(min, max, ratio)
        return this
    }

    /**
     * Set shadow width of fold base
     *
     * @param min   minimal width
     * @param max   maximum width
     * @param ratio width ratio based on fold cylinder radius. It is in (0..1)
     * @return self
     */
    fun setShadowWidthOfFoldBase(min: Float,
                                 max: Float,
                                 ratio: Float): PageFlip {
        mFoldBaseShadowWidth.set(min, max, ratio)
        return this
    }


    /**
     * Handle surface creation event
     *
     * @throws PageFlipException if failed to compile and link OpenGL shader
     */
    @Throws(PageFlipException::class)
    fun onSurfaceCreated() {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClearDepthf(1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        //        GLES20.glEnable(GLES20.GL_CULL_FACE_MODE);
        try {
            // init shader programs
            mVertexProgram.init(mContext)
            mFoldBackVertexProgram.init(mContext)
            mShadowVertexProgram.init(mContext)

            // create gradient shadow texture
            createGradientShadowTexture()
        } catch (e: PageFlipException) {
            mVertexProgram.delete()
            mFoldBackVertexProgram.delete()
            mShadowVertexProgram.delete()
            throw e
        }

    }

    /**
     * Handle surface changing event
     *
     * @param width  surface width
     * @param height surface height
     * @throws PageFlipException if failed to compile and link OpenGL shader
     */
    @Throws(PageFlipException::class)
    fun onSurfaceChanged(width: Int, height: Int) {
        mViewRect.set(width.toFloat(), height.toFloat())
        GLES20.glViewport(0, 0, width, height)
        mVertexProgram.initMatrix(-mViewRect.halfW, mViewRect.halfW,
                -mViewRect.halfH, mViewRect.halfH)
        computeMaxMeshCount()

        page = Page(mViewRect.left, mViewRect.right,
                mViewRect.top, mViewRect.bottom)

        computeVertexesAndBuildPage()
    }


    /**
     * Handle finger down event
     *
     * @param touchX x of finger down point
     * @param touchY y of finger down point
     */
    fun onFingerDown(touchX: Float, touchY: Float) {
        var touchX = touchX
        var touchY = touchY

        mDownTouchP.set(touchX, touchY)

        //根据区域判断是否是横向动画
        isMiddleCurl = touchY >= mViewRect.surfaceH / 3 && touchY <= mViewRect.surfaceH / 3 * 2
//        isMiddleCurl = false

        // covert to OpenGL coordinate
        touchX = mViewRect.toOpenGLX(touchX)
        touchY = mViewRect.toOpenGLY(touchY)

        mMaxT2OAngleTan = 0f
        mMaxT2DAngleTan = 0f

        mLastTouchP.set(touchX, touchY)
        mStartTouchP.set(touchX, touchY)
    }

    private fun getGrap(): Float {
        if (page.originP.y >= 0) {
            return 0.01F
        } else {
            return -0.01F
        }
    }

    /**
     * Handle finger up event
     *
     * @param touchX x of finger moving point
     * @param touchY y of finger moving point
     * @return true if animation is started or animation is not triggered
     */
    fun onFingerUp(touchX: Float, touchY: Float, forceFlip: Boolean) {
        var touchX = touchX
        var touchY = touchY

        val absDx = Math.abs(touchX - mDownTouchP.x)

        touchX = mViewRect.toOpenGLX(touchX)
        touchY = mViewRect.toOpenGLX(touchY)

        val originP = page!!.originP
        val diagonalP = page!!.diagonalP

        val start = Point(mTouchP.x.toInt(), mTouchP.y.toInt())
        val end = Point(0, 0)

        // forward flipping
        if (status == Status.SLIDING_TO_LEFT) {
            // can't going forward, restore current page
            if (!forceFlip) {
                end.x = originP.x.toInt()
                status = Status.BACK_TO_RIGHT
            } else {
                end.x = (diagonalP.x * 2.0 - mFoldBaseShadowWidth.mMax).toInt()
                status = Status.FLYING_TO_LEFT
            }
            end.y = originP.y.toInt()
        } else if (status == Status.SLIDING_TO_RIGHT) {
            // if not over middle x, change from backward to forward to restore
            if (!forceFlip) {
                status = Status.BACK_TO_LEFT
                end.set((diagonalP.x - page!!.width).toInt(), originP.y.toInt())
            } else {
                mMaxT2OAngleTan = (mTouchP.y - originP.y) / (mTouchP.x - originP.x)
                end.set(originP.x.toInt(), originP.y.toInt() + if (originP.y > 0) -1 else 1)
                status = Status.FLYING_TO_RIGHT
            }
        } else if (status == Status.BEGIN) {

            start.set(touchX.toInt(), touchY.toInt())

            mTouchP.set(touchX.toInt().toFloat(), touchY.toInt().toFloat())

            page!!.setOriginAndDiagonalPoints(true)

            // if enable clicking to flip, compute scroller points for animation
            computeScrollPointsForClickingFlip(touchX, start, end)


            if (status == Status.FLYING_TO_RIGHT) {
                mTouchP.x = start.x.toFloat()
                mTouchP.y = originP.y - getGrap()
            } else {
                mTouchP.x = start.x.toFloat()
                mTouchP.y = start.y.toFloat()
            }

            mMiddleP.x = (mTouchP.x + originP.x) * 0.5f
            mMiddleP.y = (mTouchP.y + originP.y) * 0.5f

            // continue to compute points to drawing flip
            computeVertexesAndBuildPage()
        }

        // start scroller for animating
        if (status == Status.FLYING_TO_LEFT ||
                status == Status.FLYING_TO_RIGHT ||
                status == Status.BACK_TO_RIGHT ||
                status == Status.BACK_TO_LEFT) {
            var dur = BASE_DURATION * Math.abs(end.x - start.x) / surfaceWidth

            if (status == Status.FLYING_TO_LEFT) {
                dur = 400
            }

            dur = Math.min(400, Math.max(200, dur))

//            dur = 10000

            Log.e("PageFlip", "startScroll $dur")
            mScroller.startScroll(start.x * 100, start.y * 100,
                    (end.x - start.x) * 100 - (getGrap() * 100).toInt(), (end.y - start.y) * 100 - (getGrap() * 100).toInt(),
                    dur)
        }

    }

    /**
     * Handle finger moving event
     *
     * @param touchX x of finger moving point
     * @param touchY y of finger moving point
     * @return true if moving will trigger to draw a new frame for page flip,
     * False means the movement should be ignored.
     */
    fun onFingerMove(touchX: Float, touchY: Float): Boolean {
        var x = touchX
        var y = touchY



        x = mViewRect.toOpenGLX(x)
        y = mViewRect.toOpenGLY(y)

        val originP = page!!.originP
        val diagonalP = page!!.diagonalP

        // compute moving distance (dx, dy)
        var dy = y - mStartTouchP.y
        var dx = x - mStartTouchP.x

        if (status == Status.BEGIN) {

            if (Math.abs(touchY - mDownTouchP.y) > Math.abs(touchX - mDownTouchP.x)) {
                page!!.setOriginAndDiagonalPoints(dy > 0)
            } else {
                page!!.setOriginAndDiagonalPoints(y < 0)
            }

            mStartTouchP.x = page!!.originP.x
            mStartTouchP.y = page!!.originP.y - getGrap()
            if (isMiddleCurl && Math.abs(dy) < Math.abs(dx)) {
                isMiddleCurl = true
            } else if (Math.abs(dy) < Math.abs(dx) / 10) {
                isMiddleCurl = true
            } else if (touchX - mDownTouchP.x < 0) {
                isMiddleCurl = false
            }
        }


        if (isMiddleCurl || status == Status.SLIDING_TO_RIGHT) {
            y = originP.y - getGrap()
            dy = 0.01f

            mStartTouchP.y = originP.y - getGrap()
            mLastTouchP.y = originP.y - getGrap()
            mTouchP.y = originP.y - getGrap()
        }

        // begin to move
        if (status == Status.BEGIN /*&&
                (Math.abs(dx) > mViewRect.width * 0.05f)*/) {
            // set OriginP and DiagonalP points

            // compute max degree between X axis and line from TouchP to OriginP
            // and max degree between X axis and line from TouchP to
            // (OriginP.x, DiagonalP.Y)

            val y2o = Math.abs(mStartTouchP.y - originP.y)
            val y2d = Math.abs(mStartTouchP.y - diagonalP.y)
            //            float y2o = Math.abs(originP.y - originP.y);
            //            float y2d = Math.abs(originP.y - diagonalP.y);

            mMaxT2OAngleTan = computeTanOfCurlAngle(y2o)
            mMaxT2DAngleTan = computeTanOfCurlAngle(y2d)

            // moving at the top and bottom screen have different tan value of
            // angle
            if (originP.y < 0 && page!!.right > 0 || originP.y > 0 && page!!.right <= 0) {
                mMaxT2OAngleTan = -mMaxT2OAngleTan
            } else {
                mMaxT2DAngleTan = -mMaxT2DAngleTan
            }

            // determine if it is moving backward or forward
            if (dx > 0) {

                dx = x - mStartTouchP.x
                status = Status.SLIDING_TO_RIGHT

                y = originP.y - getGrap()
                dy = 0.01f

                mLastTouchP.y = originP.y - getGrap()
                mTouchP.y = originP.y - getGrap()

            } else if (dx < 0 && originP.x > 0 || dx > 0 && originP.x < 0) {

                status = Status.SLIDING_TO_LEFT
            }
        }

        // in moving, compute the TouchXY
        if (status == Status.SLIDING_TO_LEFT ||
                status == Status.SLIDING_TO_RIGHT ||
                status == Status.BACK_TO_LEFT ||
                status == Status.BACK_TO_RIGHT) {


            // moving direction is changed:
            // 1. invert max curling angle
            // 2. invert Y of original point and diagonal point
            if (dy < 0 && originP.y < 0 || dy > 0 && originP.y > 0) {
                val t = mMaxT2DAngleTan
                mMaxT2DAngleTan = mMaxT2OAngleTan
                mMaxT2OAngleTan = t
                page.invertYOfOriginPoint()
            }

            // compute new TouchP.y
            val maxY = dx * mMaxT2OAngleTan
            if (Math.abs(dy) > Math.abs(maxY)) {
                dy = maxY
            }

            // check if XFoldX1 is outside page width, if yes, recompute new
            // TouchP.y to assure the XFoldX1 is in page width
            val t2oK = dy / dx
            val xTouchX = dx + dy * t2oK
            val xRatio = (1 + mSemiPerimeterRatio) * 0.5f
            val xFoldX1 = xRatio * xTouchX
            if (Math.abs(xFoldX1) + 2 >= page!!.width) {
                val dy2 = ((diagonalP.x - originP.x) / xRatio - dx) * dx
                // ignore current moving if we can't get a valid dy, for example
                // , in double pages mode, when finger is moving from the one
                // page to another page, the dy2 is negative and should be
                // ignored
                if (dy2 < 0) {
                    return false
                }

                var t = Math.sqrt(dy2.toDouble())
                if (originP.y > 0) {
                    t = -t
                    dy = Math.ceil(t).toInt().toFloat()
                } else {
                    dy = Math.floor(t).toInt().toFloat()
                }
            }

            // set touchP(x, y) and middleP(x, y)
            mLastTouchP.set(x, y)
            mTouchP.set(dx + originP.x, dy + originP.y)
            //            Log.e(TAG, "onFingerMove: dx, dy, originP \t" + dx + ", \t" + dy + ", \t" + originP.x + ", \t" + originP.y);
            //            mTouchP.set(x, y);

            mMiddleP.x = (mTouchP.x + originP.x) * 0.5f
            mMiddleP.y = (mTouchP.y + originP.y) * 0.5f

            // continue to compute points to drawing flip
            computeVertexesAndBuildPage()
            return true
        }

        return false
    }


    /**
     * Compute scroller points for animating
     *
     * @param x     x of clicking point
     * @param start start point of scroller will be set
     * @param end   end point of scroller will be set
     */
    private fun computeScrollPointsForClickingFlip(x: Float,
                                                   start: Point,
                                                   end: Point) {
        val originP = page!!.originP
        val diagonalP = page!!.diagonalP

        // forward and backward flip have different degree
        var tanOfForwardAngle = MAX_TAN_OF_FORWARD_FLIP
        var tanOfBackwardAngle = MAX_TAN_OF_BACKWARD_FLIP
        if (originP.y < 0 && originP.x > 0 || originP.y > 0 && originP.x < 0) {
            tanOfForwardAngle = -tanOfForwardAngle
            tanOfBackwardAngle = -tanOfBackwardAngle
        }

        // backward flip
        if (!ReaderSettings.instance.isFullScreenRead && x < diagonalP.x + page!!.width * mWidthRationOfClickToFlip) {
            status = Status.FLYING_TO_RIGHT
            mKValue = tanOfBackwardAngle
            start.y = originP.y.toInt() + if (originP.y > 0) -1 else 1
            end.set(originP.x.toInt(), originP.y.toInt() + if (originP.y > 0) -1 else 1)
        } else if (ReaderSettings.instance.isFullScreenRead || page!!.isXInRange(x, mWidthRationOfClickToFlip)) {
            status = Status.FLYING_TO_LEFT
            mKValue = tanOfForwardAngle

            start.x = (originP.x - page!!.width * 0.25f).toInt()
            // compute start.y
            start.y = (originP.y + (start.x - originP.x) * mKValue).toInt()

            end.x = (diagonalP.x * 2.0 - mFoldBaseShadowWidth.mMax).toInt()
            end.y = originP.y.toInt()

            if (isMiddleCurl) {
                start.x = mTouchP.x.toInt()
                start.y = (originP.y + if (originP.y > 0) -1 else 1).toInt()
            }
        }// forward flip
    }

    /**
     * Compute animating and check if it can continue
     *
     * @return true animating is continue or it is stopped
     */
    fun animating(): Boolean {
        val originP = page!!.originP
        val diagonalP = page!!.diagonalP

        // is to end animating?
        val isAnimating = !mScroller.isFinished
        if (isAnimating || animationNotEnd) {
            animationNotEnd = isAnimating
            // get new (x, y)
            mScroller.computeScrollOffset()

            if (isAnimating) {
                mTouchP.set(mScroller.currX / 100.0f, mScroller.currY / 100.0f)
            } else {
                mTouchP.set(mScroller.finalX / 100.0f, mScroller.finalY / 100.0f)
            }

            // for backward and restore flip, compute x to check if it can
            // continue to flip
            if (/*status == Status.SLIDING_TO_RIGHT ||*/
                    status == Status.BACK_TO_RIGHT || status == Status.BACK_TO_LEFT) {
                mTouchP.y = (mTouchP.x - originP.x) * mKValue + originP.y
                if (isMiddleCurl && Math.abs(mTouchP.y - originP.y) >= 0) {
                    mTouchP.y = originP.y - getGrap()
                }
                if (Math.abs(mTouchP.x - originP.x) < 0.1) {

                    mScroller.abortAnimation()
                    return false
                }
            }


            // compute middle point
            mMiddleP.set((mTouchP.x + originP.x) * 0.5f,
                    (mTouchP.y + originP.y) * 0.5f)


            computeKeyVertexesWhenSlope()
            computeVertexesWhenSlope()


//            if (status == Status.FLYING_TO_LEFT) {
//                val r = (mLenOfTouchOrigin * mSemiPerimeterRatio / Math.PI).toFloat()
//                val x = (mYFoldP1.y - diagonalP.y) * mKValue + r
//
//                if (x <= diagonalP.x - originP.x) {
//
//                    mScroller.abortAnimation()
//                    return false
//                }
//            }


            return true
        }

        mScroller.abortAnimation()
        return false
    }


    /**
     * Draw flipping frame 画翻转
     */
    fun drawFlipFrame() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // 1. draw back of fold page 画褶皱
        GLES20.glUseProgram(mFoldBackVertexProgram.mProgramRef)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        mFoldBackVertexes.draw(mFoldBackVertexProgram,
                page!!,
                false,
                mGradientShadowTextureID)

        // 2. draw unfold page and front of fold page 画褶皱和展开页
        GLES20.glUseProgram(mVertexProgram.mProgramRef)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        page!!.drawFrontPage(mVertexProgram,
                mFoldFrontVertexes)

        // 3. draw edge and base shadow of fold parts 基础阴影
        GLES20.glUseProgram(mShadowVertexProgram.mProgramRef)
        mFoldBaseShadow.draw(mShadowVertexProgram)
        mFoldEdgesShadow.draw(mShadowVertexProgram)
    }

    /**
     * Draw frame with full page
     */
    fun drawPageFrame() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(mVertexProgram.mProgramRef)
        GLES20.glUniformMatrix4fv(mVertexProgram.mMVPMatrixLoc, 1, false,
                VertexProgram.MVPMatrix, 0)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

        // 1. draw first page
        page!!.drawFullPage(mVertexProgram, true)

    }

    /**
     * Compute max mesh count and allocate vertexes buffer
     */
    private fun computeMaxMeshCount() {
        // compute max mesh count
        var maxMeshCount = mViewRect.minOfWH().toInt() / mPixelsOfMesh

        // make sure the vertex count is even number
        if (maxMeshCount % 2 != 0) {
            maxMeshCount++
        }

        // init vertexes buffers
        mFoldBackVertexes.set(maxMeshCount + 2)
        mFoldFrontVertexes.set((maxMeshCount shl 1) + 8, 3, true)
        mFoldEdgesShadow.set(maxMeshCount + 2)
        mFoldBaseShadow.set(maxMeshCount + 2)
    }

    /**
     * Create gradient shadow texture for lighting effect
     */
    private fun createGradientShadowTexture() {
        val textureIDs = IntArray(1)
        GLES20.glGenTextures(1, textureIDs, 0)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        mGradientShadowTextureID = textureIDs[0]

        // gradient shadow texture
        val shadow = PageFlipUtils.createGradientBitmap()
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mGradientShadowTextureID)
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, shadow, 0)
        shadow.recycle()
    }

    /**
     * Compute vertexes of page
     */
    private fun computeVertexesAndBuildPage() {
        computeKeyVertexesWhenSlope()
        computeVertexesWhenSlope()
    }

    /**
     * Compute key vertexes when page flip is slope
     */
    private fun computeKeyVertexesWhenSlope() {
        val oX = page!!.originP.x
        val oY = page!!.originP.y

        val dX = mMiddleP.x - oX
        val dY = mMiddleP.y - oY

        // compute key points on X axis
        val r0 = 1 - mSemiPerimeterRatio
        val r1 = 1 + mSemiPerimeterRatio
        mXFoldP.set(mMiddleP.x + dY * dY / dX, oY)
        mXFoldP0.set(oX + (mXFoldP.x - oX) * r0, mXFoldP.y)
        mXFoldP1.set(oX + r1 * (mXFoldP.x - oX), mXFoldP.y)

        // compute key points on Y axis
        mYFoldP.set(oX, mMiddleP.y + dX * dX / dY)
        mYFoldP0.set(mYFoldP.x, oY + (mYFoldP.y - oY) * r0)
        mYFoldP1.set(mYFoldP.x, oY + r1 * (mYFoldP.y - oY))

        // line length from TouchXY to OriginalXY
        mLenOfTouchOrigin = Math.hypot((mTouchP.x - oX).toDouble(),
                (mTouchP.y - oY).toDouble()).toFloat()

        // cylinder radius
        mR = (mLenOfTouchOrigin * mSemiPerimeterRatio / Math.PI).toFloat()

        // compute line slope
        if (mTouchP.x - oX != 0f) {
            mKValue = (mTouchP.y - oY) / (mTouchP.x - oX)
        } else {
            mKValue = 1.0f
        }

        // compute mesh count
        computeMeshCount()
    }

    /**
     * Compute back vertex and edge shadow vertex of fold page
     *
     *
     * In 2D coordinate system, for every vertex on fold page, we will follow
     * the below steps to compute its 3D point (x,y,z) on curled page(cylinder):
     *
     *
     *  * deem originP as (0, 0) to simplify the next computing steps
     *  * translate point(x, y) to new coordinate system
     * (originP is (0, 0))
     *  * rotate point(x, y) with curling angle A in clockwise
     *  * compute 3d point (x, y, z) for 2d point(x, y), at this time, the
     * cylinder is vertical in new coordinate system which will help us
     * compute point
     *  * rotate 3d point (x, y, z) with -A to restore
     *  * translate 3d point (x, y, z) to original coordinate system
     *
     *
     *
     *
     * For point of edge shadow, the most computing steps are same but:
     *
     *  * shadow point is following the page point except different x
     * coordinate
     *  * shadow point has same z coordinate with the page point
     *
     *
     * @param isX    is vertex for x point on x axis or y point on y axis?
     * @param x0     x of point on axis
     * @param y0     y of point on axis
     * @param sx0    x of edge shadow point
     * @param sy0    y of edge shadow point
     * @param tX     x of xFoldP1 point in rotated coordinate system
     * @param sinA   sin value of page curling angle
     * @param cosA   cos value of page curling angel
     * @param coordX x of texture coordinate
     * @param coordY y of texture coordinate
     * @param oX     x of originate point
     * @param oY     y of originate point
     */
    private fun computeBackVertex(isX: Boolean, x0: Float, y0: Float, sx0: Float,
                                  sy0: Float, tX: Float, sinA: Float, cosA: Float,
                                  coordX: Float, coordY: Float, oX: Float,
                                  oY: Float) {
        // rotate degree A
        var x = x0 * cosA - y0 * sinA
        val y = x0 * sinA + y0 * cosA

        // rotate degree A for vertexes of fold edge shadow
        var sx = sx0 * cosA - sy0 * sinA
        val sy = sx0 * sinA + sy0 * cosA

        // compute mapping point on cylinder
        val rad = (x - tX) / mR
        val sinR = Math.sin(rad.toDouble())
        x = (tX + mR * sinR).toFloat()
        val cz = (mR * (1 - Math.cos(rad.toDouble()))).toFloat()

        // rotate degree -A, sin(-A) = -sin(A), cos(-A) = cos(A)
        val cx = x * cosA + y * sinA + oX
        val cy = y * cosA - x * sinA + oY
        mFoldBackVertexes.addVertex(cx, cy, cz, sinR.toFloat(), coordX, coordY)

        // compute coordinates of fold shadow edge
        val sRadian = (sx - tX) / mR
        sx = (tX + mR * Math.sin(sRadian.toDouble())).toFloat()
        mFoldEdgesShadow.addVertexes(isX, cx, cy,
                sx * cosA + sy * sinA + oX,
                sy * cosA - sx * sinA + oY)
    }

    /**
     * Compute back vertex of fold page
     *
     *
     * Almost same with another computeBackVertex function except expunging the
     * shadow point part
     *
     *
     * @param x0     x of point on axis
     * @param y0     y of point on axis
     * @param tX     x of xFoldP1 point in rotated coordinate system
     * @param sinA   sin value of page curling angle
     * @param cosA   cos value of page curling angel
     * @param coordX x of texture coordinate
     * @param coordY y of texture coordinate
     * @param oX     x of originate point
     * @param oY     y of originate point
     */
    private fun computeBackVertex(x0: Float, y0: Float, tX: Float,
                                  sinA: Float, cosA: Float, coordX: Float,
                                  coordY: Float, oX: Float, oY: Float) {
        // rotate degree A
        var x = x0 * cosA - y0 * sinA
        val y = x0 * sinA + y0 * cosA

        // compute mapping point on cylinder
        val rad = (x - tX) / mR
        val sinR = Math.sin(rad.toDouble())
        x = (tX + mR * sinR).toFloat()
        val cz = (mR * (1 - Math.cos(rad.toDouble()))).toFloat()

        // rotate degree -A, sin(-A) = -sin(A), cos(-A) = cos(A)
        val cx = x * cosA + y * sinA + oX
        val cy = y * cosA - x * sinA + oY
        mFoldBackVertexes.addVertex(cx, cy, cz, sinR.toFloat(), coordX, coordY)
    }

    /**
     * Compute front vertex and base shadow vertex of fold page
     *
     * The computing principle is almost same with
     * [.computeBackVertex]
     *
     * @param isX       is vertex for x point on x axis or y point on y axis?
     * @param x0        x of point on axis
     * @param y0        y of point on axis
     * @param tX        x of xFoldP1 point in rotated coordinate system
     * @param sinA      sin value of page curling angle
     * @param cosA      cos value of page curling angel
     * @param baseWcosA base shadow width * cosA
     * @param baseWsinA base shadow width * sinA
     * @param coordX    x of texture coordinate
     * @param coordY    y of texture coordinate
     * @param oX        x of originate point
     * @param oY        y of originate point
     */
    private fun computeFrontVertex(isX: Boolean, x0: Float, y0: Float, tX: Float,
                                   sinA: Float, cosA: Float,
                                   baseWcosA: Float, baseWsinA: Float,
                                   coordX: Float, coordY: Float,
                                   oX: Float, oY: Float, dY: Float) {
        // rotate degree A
        var x = x0 * cosA - y0 * sinA
        val y = x0 * sinA + y0 * cosA

        // compute mapping point on cylinder
        val rad = (x - tX) / mR
        x = (tX + mR * Math.sin(rad.toDouble())).toFloat()
        val cz = (mR * (1 - Math.cos(rad.toDouble()))).toFloat()

        // rotate degree -A, sin(-A) = -sin(A), cos(-A) = cos(A)
        val cx = x * cosA + y * sinA + oX
        val cy = y * cosA - x * sinA + oY
        mFoldFrontVertexes.addVertex(cx, cy, cz, coordX, coordY)
        mFoldBaseShadow.addVertexes(isX, cx, cy,
                cx + baseWcosA, cy - baseWsinA)
    }

    /**
     * Compute front vertex
     *
     * The difference with another
     * [.computeFrontVertex] is that it won't
     * compute base shadow vertex
     *
     * @param x0     x of point on axis
     * @param y0     y of point on axis
     * @param tX     x of xFoldP1 point in rotated coordinate system
     * @param sinA   sin value of page curling angle
     * @param cosA   cos value of page curling angel
     * @param coordX x of texture coordinate
     * @param coordY y of texture coordinate
     * @param oX     x of originate point
     * @param oY     y of originate point
     */
    private fun computeFrontVertex(x0: Float, y0: Float, tX: Float,
                                   sinA: Float, cosA: Float,
                                   coordX: Float, coordY: Float,
                                   oX: Float, oY: Float) {
        // rotate degree A
        var x = x0 * cosA - y0 * sinA
        val y = x0 * sinA + y0 * cosA

        // compute mapping point on cylinder
        val rad = (x - tX) / mR
        x = (tX + mR * Math.sin(rad.toDouble())).toFloat()
        val cz = (mR * (1 - Math.cos(rad.toDouble()))).toFloat()

        // rotate degree -A, sin(-A) = -sin(A), cos(-A) = cos(A)
        val cx = x * cosA + y * sinA + oX
        val cy = y * cosA - x * sinA + oY
        mFoldFrontVertexes.addVertex(cx, cy, cz, coordX, coordY)
    }

    /**
     * Compute last vertex of base shadow(backward direction)
     *
     *
     * The vertexes of base shadow are composed by two part: forward and
     * backward part. Forward vertexes are computed from XFold points and
     * backward vertexes are computed from YFold points. The reason why we use
     * forward and backward is because how to change float buffer index when we
     * add a new vertex to buffer. Backward means the index is declined from
     * buffer middle position to the head, in contrast, the forward is
     * increasing index from middle to the tail. This design will help keep
     * float buffer consecutive and to be draw at a time.
     *
     *
     * Sometimes, the whole or part of YFold points will be outside page, that
     * means their Y coordinate are greater than page height(diagonal.y). In
     * this case, we have to crop them like cropping line on 2D coordinate
     * system. If delve further, we can conclude that we only need to compute
     * the first start/end vertexes which is falling on the border line of
     * diagonal.y since other backward vertexes must be outside page and could
     * not be seen, and then combine these vertexes with forward vertexes to
     * render base shadow.
     *
     *
     * This function is just used to compute the couple vertexes.
     *
     *
     * @param x0        x of point on axis
     * @param y0        y of point on axis
     * @param tX        x of xFoldP1 point in rotated coordinate system
     * @param sinA      sin value of page curling angle
     * @param cosA      cos value of page curling angel
     * @param baseWcosA base shadow width * cosA
     * @param baseWsinA base shadow width * sinA
     * @param oX        x of originate point
     * @param oY        y of originate point
     * @param dY        y of diagonal point
     */
    private fun computeBaseShadowLastVertex(x0: Float, y0: Float, tX: Float,
                                            sinA: Float, cosA: Float,
                                            baseWcosA: Float, baseWsinA: Float,
                                            oX: Float, oY: Float, dY: Float) {
        // like computing front vertex, we firstly compute the mapping vertex
        // on fold cylinder for point (x0, y0) which also is last vertex of
        // base shadow(backward direction)
        var x = x0 * cosA - y0 * sinA
        val y = x0 * sinA + y0 * cosA

        // compute mapping point on cylinder
        val rad = (x - tX) / mR
        x = (tX + mR * Math.sin(rad.toDouble())).toFloat()

        val cx1 = x * cosA + y * sinA + oX
        val cy1 = y * cosA - x * sinA + oY

        // now, we have start vertex(cx1, cy1), compute end vertex(cx2, cy2)
        // which is translated based on start vertex(cx1, cy1)
        val cx2 = cx1 + baseWcosA
        val cy2 = cy1 - baseWsinA

        // as we know, this function is only used to compute last vertex of
        // base shadow(backward) when the YFold points are outside page height,
        // that means the (cx1, cy1) and (cx2, cy2) we computed above normally
        // is outside page, so we need to compute their projection points on page
        // border as rendering vertex of base shadow
        val bx1 = cx1 + mKValue * (cy1 - dY)
        val bx2 = cx2 + mKValue * (cy2 - dY)

        // add start/end vertex into base shadow buffer, it will be linked with
        // forward vertexes to draw base shadow
        mFoldBaseShadow.addVertexes(false, bx1, dY, bx2, dY)
    }

    /**
     * Compute vertexes when page flip is slope
     */
    private fun computeVertexesWhenSlope() {
        val oX = page!!.originP.x
        val oY = page!!.originP.y
        val dY = page!!.diagonalP.y
        val cOX = page!!.originP.texX
        val cOY = page!!.originP.texY
        val cDY = page!!.diagonalP.texY
        val height = page!!.height
        val d2oY = dY - oY

        // compute radius and sin/cos of angle
        val sinA = (mTouchP.y - oY) / mLenOfTouchOrigin
        val cosA = (oX - mTouchP.x) / mLenOfTouchOrigin

        // need to translate before rotate, and then translate back
        val count = mMeshCount
        val xFoldP1 = (mXFoldP1.x - oX) * cosA
        val edgeW = mFoldEdgesShadowWidth.width(mR)
        val baseW = mFoldBaseShadowWidth.width(mR)
        val baseWcosA = baseW * cosA
        val baseWsinA = baseW * sinA
        val edgeY = if (oY > 0) edgeW else -edgeW
        val edgeX = if (oX > 0) edgeW else -edgeW
        val stepSY = edgeY / count
        val stepSX = edgeX / count

        // reset vertexes buffer counter
        mFoldEdgesShadow.reset()
        mFoldBaseShadow.reset()
        mFoldFrontVertexes.reset()
        mFoldBackVertexes.reset()

        // add the first 3 float numbers is fold triangle
        mFoldBackVertexes.addVertex(mTouchP.x, mTouchP.y, 1f, 0f, cOX, cOY)

        // compute vertexes for fold back part
        var stepX = (mXFoldP0.x - mXFoldP.x) / count
        var stepY = (mYFoldP0.y - mYFoldP.y) / count
        var x = mXFoldP0.x - oX
        var y = mYFoldP0.y - oY
        var sx = edgeX
        var sy = edgeY

        // compute point of back of fold page
        // Case 1: y coordinate of point YFP0 -> YFP is < diagonalP.y
        //
        //   <---- Flip
        // +-------------+ diagonalP
        // |             |
        // |             + YFP
        // |            /|
        // |           / |
        // |          /  |
        // |         /   |
        // |        /    + YFP0
        // |       / p  /|
        // +------+--.-+-+ originP
        //      XFP   XFP0
        //
        // 1. XFP -> XFP0 -> originP -> YFP0 ->YFP is back of fold page
        // 2. XFP -> XFP0 -> YFP0 -> YFP is a half of cylinder when page is
        //    curled
        // 3. P point will be computed
        //
        // compute points within the page
        var i = 0
        while (i <= count && Math.abs(y) < height) {
            computeBackVertex(true, x, 0f, x, sy, xFoldP1, sinA, cosA,
                    page!!.textureX(x + oX), cOY, oX, oY)
            computeBackVertex(false, 0f, y, sx, y, xFoldP1, sinA, cosA, cOX,
                    page!!.textureY(y + oY), oX, oY)
            ++i
            x -= stepX
            y -= stepY
            sy -= stepSY
            sx -= stepSX
        }

        // If y coordinate of point on YFP0 -> YFP is > diagonalP
        // There are two cases:
        //                      <---- Flip
        //     Case 2                               Case 3
        //          YFP                               YFP   YFP0
        // +---------+---+ diagonalP          +--------+-----+--+ diagonalP
        // |        /    |                    |       /     /   |
        // |       /     + YFP0               |      /     /    |
        // |      /     /|                    |     /     /     |
        // |     /     / |                    |    /     /      |
        // |    /     /  |                    |   /     /       |
        // |   / p   /   |                    |  / p   /        |
        // +--+--.--+----+ originalP          +-+--.--+---------+ originalP
        //   XFP   XFP0                        XFP   XFP0
        //
        // compute points outside the page
        if (i <= count) {
            if (Math.abs(y) != height) {
                // case 3: compute mapping point of diagonalP
                if (Math.abs(mYFoldP0.y - oY) > height) {
                    val tx = oX + 2f * mKValue * (mYFoldP.y - dY)
                    val ty = dY + mKValue * (tx - oX)
                    mFoldBackVertexes.addVertex(tx, ty, 1f, 0f, cOX, cDY)

                    val tsx = tx - sx
                    val tsy = dY + mKValue * (tsx - oX)
                    mFoldEdgesShadow.addVertexes(false, tx, ty, tsx, tsy)
                } else {
                    val x1 = mKValue * d2oY
                    computeBackVertex(true, x1, 0f, x1, sy, xFoldP1, sinA, cosA,
                            page!!.textureX(x1 + oX), cOY, oX, oY)
                    computeBackVertex(false, 0f, d2oY, sx, d2oY, xFoldP1, sinA,
                            cosA, cOX, cDY, oX, oY)
                }// case 2: compute mapping point of diagonalP
            }

            // compute the remaining points
            while (i <= count) {
                computeBackVertex(true, x, 0f, x, sy, xFoldP1, sinA, cosA,
                        page!!.textureX(x + oX), cOY, oX, oY)

                // since the origin Y is beyond page, we need to compute its
                // projection point on page border and then compute mapping
                // point on curled cylinder
                val x1 = mKValue * (y + oY - dY)
                computeBackVertex(x1, d2oY, xFoldP1, sinA, cosA,
                        page!!.textureX(x1 + oX), cDY, oX, oY)
                ++i
                x -= stepX
                y -= stepY
                sy -= stepSY
                sx -= stepSX
            }
        }

        mFoldBackVertexes.toFloatBuffer()

        // Like above computation, the below steps are computing vertexes of
        // front of fold page
        // Case 1: y coordinate of point YFP -> YFP1 is < diagonalP.y
        //
        //     <---- Flip
        // +----------------+ diagonalP
        // |                |
        // |                + YFP1
        // |               /|
        // |              / |
        // |             /  |
        // |            /   |
        // |           /    + YFP
        // |          /    /|
        // |         /    / |
        // |        /    /  + YFP0
        // |       /    /  /|
        // |      / p  /  / |
        // +-----+--.-+--+--+ originP
        //    XFP1  XFP  XFP0
        //
        // 1. XFP -> YFP -> YFP1 ->XFP1 is front of fold page and a half of
        //    cylinder when page is curled.
        // 2. YFP->XFP is joint line of front and back of fold page
        // 3. P point will be computed
        //
        // compute points within the page
        stepX = (mXFoldP.x - mXFoldP1.x) / count
        stepY = (mYFoldP.y - mYFoldP1.y) / count
        x = mXFoldP.x - oX - stepX
        y = mYFoldP.y - oY - stepY
        var j = 0
        while (j < count && Math.abs(y) < height) {
            computeFrontVertex(true, x, 0f, xFoldP1, sinA, cosA,
                    baseWcosA, baseWsinA,
                    page!!.textureX(x + oX), cOY, oX, oY, dY)
            computeFrontVertex(false, 0f, y, xFoldP1, sinA, cosA,
                    baseWcosA, baseWsinA,
                    cOX, page!!.textureY(y + oY), oX, oY, dY)
            ++j
            x -= stepX
            y -= stepY
        }

        // compute points outside the page
        if (j < count) {
            // compute mapping point of diagonalP
            if (Math.abs(y) != height && j > 0) {
                val y1 = dY - oY
                val x1 = mKValue * y1
                computeFrontVertex(true, x1, 0f, xFoldP1, sinA, cosA,
                        baseWcosA, baseWsinA,
                        page!!.textureX(x1 + oX), cOY, oX, oY, dY)

                computeFrontVertex(0f, y1, xFoldP1, sinA, cosA, cOX,
                        page!!.textureY(y1 + oY), oX, oY)
            }

            // compute last pair of vertexes of base shadow
            computeBaseShadowLastVertex(0f, y, xFoldP1, sinA, cosA,
                    baseWcosA, baseWsinA,
                    oX, oY, dY)

            // compute the remaining points
            while (j < count) {
                computeFrontVertex(true, x, 0f, xFoldP1, sinA, cosA,
                        baseWcosA, baseWsinA,
                        page!!.textureX(x + oX), cOY, oX, oY, dY)

                val x1 = mKValue * (y + oY - dY)
                computeFrontVertex(x1, d2oY, xFoldP1, sinA, cosA,
                        page!!.textureX(x1 + oX), cDY, oX, oY)
                ++j
                x -= stepX
                y -= stepY
            }

        }

        // set uniform Z value for shadow vertexes
        mFoldEdgesShadow.vertexZ = mFoldFrontVertexes.getFloatAt(2)
        mFoldBaseShadow.vertexZ = -0.5f

        // add two vertexes to connect with the unfold front page
        page!!.buildVertexesOfPageWhenSlope(mFoldFrontVertexes, mXFoldP1, mYFoldP1,
                mKValue)
        mFoldFrontVertexes.toFloatBuffer()

        // compute vertexes of fold edge shadow
        mFoldBaseShadow.toFloatBuffer()
        computeVertexesOfFoldTopEdgeShadow(mTouchP.x, mTouchP.y, sinA, cosA,
                -edgeX, edgeY)
        mFoldEdgesShadow.toFloatBuffer()
    }

    /**
     * Compute vertexes of fold top edge shadow
     *
     * Top edge shadow of fold page is a quarter circle
     *
     * @param x0   X of touch point
     * @param y0   Y of touch point
     * @param sinA Sin value of page curling angle
     * @param cosA Cos value of page curling angle
     * @param sx   Shadow width on X axis
     * @param sy   Shadow width on Y axis
     */
    private fun computeVertexesOfFoldTopEdgeShadow(x0: Float, y0: Float,
                                                   sinA: Float, cosA: Float,
                                                   sx: Float, sy: Float) {
        val sin2A = 2f * sinA * cosA
        val cos2A = (1 - 2 * Math.pow(sinA.toDouble(), 2.0)).toFloat()
        var r = 0f
        val dr = (Math.PI / (FOLD_TOP_EDGE_SHADOW_VEX_COUNT - 2)).toFloat()
        val size = FOLD_TOP_EDGE_SHADOW_VEX_COUNT / 2
        var j = mFoldEdgesShadow.mMaxBackward

        //                 ^ Y                             __ |
        //      TouchP+    |                             /    |
        //             \   |                            |     |
        //              \  |                             \    |
        //               \ |              X <--------------+--+- OriginP
        //                \|                                 /|
        // X <----------+--+- OriginP                       / |
        //             /   |                               /  |
        //             |   |                              /   |
        //              \__+ Top edge              TouchP+    |
        //                 |                                  v Y
        // 1. compute quarter circle at origin point
        // 2. rotate quarter circle to touch point direction
        // 3. move quarter circle to touch point as top edge shadow
        var i = 0
        while (i < size) {
            val x = (sx * Math.cos(r.toDouble())).toFloat()
            val y = (sy * Math.sin(r.toDouble())).toFloat()

            // rotate -2A and then translate to touchP
            mFoldEdgesShadow.setVertexes(j, x0, y0,
                    x * cos2A + y * sin2A + x0,
                    y * cos2A - x * sin2A + y0)
            ++i
            r += dr
            j += 8
        }
    }

    /**
     * Compute mesh count for page flip
     */
    private fun computeMeshCount() {
        val dx = Math.abs(mXFoldP0.x - mXFoldP1.x)
        val dy = Math.abs(mYFoldP0.y - mYFoldP1.y)
        val len = Math.min(dx, dy).toInt()
        mMeshCount = 0

        // make sure mesh count is greater than threshold, if less than it,
        // the page maybe is drawn unsmoothly
        var i = mPixelsOfMesh
        while (i >= 1 && mMeshCount < MESH_COUNT_THRESHOLD) {
            mMeshCount = len / i
            i = i shr 1
        }

        // keep count is even
        if (mMeshCount % 2 != 0) {
            mMeshCount++
        }

        // half count for fold page
        mMeshCount = mMeshCount shr 1
    }

    /**
     * Compute tan value of curling angle
     *
     * @param dy the diff value between touchP.y and originP.y
     * @return tan value of max curl angle
     */
    private fun computeTanOfCurlAngle(dy: Float): Float {
        val ratio = dy / mViewRect.halfH
        if (ratio <= 1 - MAX_PAGE_CURL_ANGLE_RATIO) {
            return MAX_PAGE_CURL_TAN_OF_ANGEL
        }

        val degree = MAX_PAGE_CURL_ANGLE - PAGE_CURL_ANGEL_DIFF * ratio
        return if (degree < MIN_PAGE_CURL_ANGLE) {
            MIN_PAGE_CURL_TAN_OF_ANGLE
        } else {
            Math.tan(Math.PI * degree / 180).toFloat()
        }
    }

    /**
     * Debug information
     */
    private fun debugInfo() {
        val originP = page!!.originP
        val diagonalP = page!!.diagonalP

        Log.d(TAG, "************************************")
        Log.d(TAG, " Mesh Count:    $mMeshCount")
        Log.d(TAG, " Mesh Pixels:   $mPixelsOfMesh")
        Log.d(TAG, " Origin:        " + originP.x + ", " + originP.y)
        Log.d(TAG, " Diagonal:      " + diagonalP.x + ", " + diagonalP.y)
        Log.d(TAG, " OriginTouchP:  " + mStartTouchP.x + ", "
                + mStartTouchP.y)
        Log.d(TAG, " TouchP:        " + mTouchP.x + ", " + mTouchP.y)
        Log.d(TAG, " MiddleP:       " + mMiddleP.x + ", " + mMiddleP.y)
        Log.d(TAG, " XFoldP:        " + mXFoldP.x + ", " + mXFoldP.y)
        Log.d(TAG, " XFoldP0:       " + mXFoldP0.x + ", " + mXFoldP0.y)
        Log.d(TAG, " XFoldP1:       " + mXFoldP1.x + ", " + mXFoldP1.y)
        Log.d(TAG, " YFoldP:        " + mYFoldP.x + ", " + mYFoldP.y)
        Log.d(TAG, " YFoldP0:       " + mYFoldP0.x + ", " + mYFoldP0.y)
        Log.d(TAG, " YFoldP1:       " + mYFoldP1.x + ", " + mYFoldP1.y)
        Log.d(TAG, " LengthT->O:    $mLenOfTouchOrigin")
    }

    companion object {
        internal val TAG = "PageFlip"

        // default pixels of mesh vertex
        private val DEFAULT_MESH_VERTEX_PIXELS = 10
        private val MESH_COUNT_THRESHOLD = 20

        // The min page curl angle (5 degree)
        private val MIN_PAGE_CURL_ANGLE = 5
        // The max page curl angle (65 degree)
        private val MAX_PAGE_CURL_ANGLE = 65
        private val PAGE_CURL_ANGEL_DIFF = MAX_PAGE_CURL_ANGLE - MIN_PAGE_CURL_ANGLE
        private val MIN_PAGE_CURL_RADIAN = (Math.PI * MIN_PAGE_CURL_ANGLE / 180).toFloat()
        private val MAX_PAGE_CURL_RADIAN = (Math.PI * MAX_PAGE_CURL_ANGLE / 180).toFloat()
        private val MIN_PAGE_CURL_TAN_OF_ANGLE = Math.tan(MIN_PAGE_CURL_RADIAN.toDouble()).toFloat()
        private val MAX_PAGE_CURL_TAN_OF_ANGEL = Math.tan(MAX_PAGE_CURL_RADIAN.toDouble()).toFloat()
        private val MAX_PAGE_CURL_ANGLE_RATIO = MAX_PAGE_CURL_ANGLE / 90f
        private val MAX_TAN_OF_FORWARD_FLIP = Math.tan(Math.PI / 6).toFloat()
        private val MAX_TAN_OF_BACKWARD_FLIP = Math.tan(Math.PI / 20).toFloat()

        // width ratio of clicking to flip
        private val WIDTH_RATIO_OF_CLICK_TO_FLIP = 0.5f

        // width ratio of triggering restore flip
        private val WIDTH_RATIO_OF_RESTORE_FLIP = 0.25f

        // folder page shadow color buffer size
        private val FOLD_TOP_EDGE_SHADOW_VEX_COUNT = 22

        // fold edge shadow color
        private val FOLD_EDGE_SHADOW_START_COLOR = 0.1f
        private val FOLD_EDGE_SHADOW_START_ALPHA = 0.25f
        private val FOLD_EDGE_SHADOW_END_COLOR = 0.3f
        private val FOLD_EDGE_SHADOW_END_ALPHA = 0f

        // fold base shadow color
        private val FOLD_BASE_SHADOW_START_COLOR = 0.05f
        private val FOLD_BASE_SHADOW_START_ALPHA = 0.4f
        private val FOLD_BASE_SHADOW_END_COLOR = 0.3f
        private val FOLD_BASE_SHADOW_END_ALPHA = 0f


        private val BASE_DURATION = 500
    }
}
