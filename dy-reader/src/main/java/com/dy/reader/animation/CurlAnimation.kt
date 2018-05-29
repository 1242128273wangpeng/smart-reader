package com.intelligent.reader.reader.v2

import android.graphics.PointF
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.view.SoundEffectConstants
import com.dy.reader.Reader
import com.dy.reader.event.EventReaderConfig
import com.dy.reader.event.EventSetting
import com.dy.reader.flip.PageFlip
import com.dy.reader.flip.Status
import com.dy.reader.helper.AppHelper
import com.dy.reader.page.PageManager
import com.dy.reader.setting.ReaderSettings
import com.dy.reader.setting.ReaderStatus
import com.dy.reader.util.ThemeUtil
import org.greenrobot.eventbus.EventBus

/**
 * Created by xian on 18-3-25.
 */
class CurlAnimation : PageFlip, IGLAnimation {

    lateinit var glSurfaceView: GLSurfaceView

    constructor(glSurfaceView: GLSurfaceView) : super(glSurfaceView.context) {
        this.glSurfaceView = glSurfaceView
    }

    override fun loadProgram() {
        // create PageFlip 设置参数
        setSemiPerimeterRatio(1.0f)//圆柱半径
                .setShadowWidthOfFoldEdges(1f, 30f, 0.2f)//折叠页的边缘阴影颜色
                .setShadowColorOfFoldEdges(0.0f, 0.15f, 0.0f, 0.0f)

                .setShadowWidthOfFoldBase(80f, 220f, 1.0f)
                .setShadowColorOfFoldBase(0.0f, 0.4f, 0.0f, 0.0f)
                .setPixelsOfMesh(10)

        onSurfaceCreated()
        onSurfaceChanged(glSurfaceView.width, glSurfaceView.height)

        fillTexture(true)
    }

    override fun unloadProgram() {
        mVertexProgram.delete()
        mFoldBackVertexProgram.delete()
        mShadowVertexProgram.delete()

        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
    }

    fun isFlying(): Boolean {
        return status == Status.FLYING_TO_LEFT
                || status == Status.FLYING_TO_RIGHT
                || status == Status.BACK_TO_LEFT
                || status == Status.BACK_TO_RIGHT
    }

    override fun drawFrame() {
        if (status == Status.BEGIN) {
            page.setFirstTexture(PageManager.currentPage.textureID)
            page.setBackColor(ReaderSettings.instance.backgroundColor)
            drawPageFrame()
        } else {
            drawFlipFrame()
        }

        if (isFlying()) {
            if (!animating()) {
                status = Status.BEGIN
                glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
                EventBus.getDefault().post(EventSetting(EventSetting.Type.SHOW_AD))
            }
            glSurfaceView.requestRender()
        }
    }


    fun forceAbortAnimation() {
        if (isFlying()) {
            animationNotEnd = false
            mScroller.abortAnimation()
            status = Status.BEGIN
        }
    }

    val downPoint = PointF()

    var beginDrag = false
    var shouldGiveUpEvent = false

    var isDownActioned = false

    override fun down(x: Float, y: Float) {
        if(!isDownActioned) {
            isDownActioned = true
            downPoint.set(x, y)
            beginDrag = false
            shouldGiveUpEvent = false

            onFingerDown(x, y)
        }
    }

    override fun move(x: Float, y: Float) {
        if(!isDownActioned){
            down(x, y)
            return
        }

        val distence = x - downPoint.x
        if (!shouldGiveUpEvent && !beginDrag && Math.abs(distence) > AppHelper.touchSlop) {
//            runOnMain { ToastUtils.showToastNoRepeat("触发滑动事件") }
            beginDrag = true

            if (distence < 0) {
                //left
                shouldGiveUpEvent = !fillTexture(true, true)
            } else {
                shouldGiveUpEvent = !fillTexture(false)
            }
            if (beginDrag && !shouldGiveUpEvent) {
                forceAbortAnimation()

                /*Render的mode可以设为两种模式，一种是自动循环模式，也就是说GL线程以一 定的时间间隔自动的循环调用用户实现的onDrawFrame（）方法进行一帧一帧的绘制，还有一种的“脏”模式，也就是说当用户需要重绘的时候，主动 “拉”这个重绘过程，有点类似于Canvas中的invalidate（）
                具体的调用方法是在GLSurfaceView中
                a.自动模式
                setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
                b."脏"模式
                        .setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                当需要重绘时，调用
                GLSurfaceView.requestRender()
                一般情况下使用脏模式，这样可以有效降低cpu负载。测试结果表明，OpenGL真正绘图时一般会占到30%以上的cp*/
                glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
            }
        }

        if (beginDrag && !shouldGiveUpEvent && onFingerMove(x, y)) {
        }
    }

    override fun up(x: Float, y: Float, xVelocity: Float) {
        if(!isDownActioned){
            return
        }
        isDownActioned = false

//        runOnMain {
//            ToastUtils.showToastNoRepeat("触发结束")
//        }
        if (!shouldGiveUpEvent) {
            var filledTexture = true
            if (!beginDrag) {
                filledTexture = fillTexture(x > glSurfaceView.width / 2 || ReaderSettings.instance.isFullScreenRead, true)
            }
            if (filledTexture) {

//                AppHelper.runInMain {
//                    if (!beginDrag) {
//                        glSurfaceView.playSoundEffect(SoundEffectConstants.CLICK)
//                    }
//                }

                forceAbortAnimation()

                var forceFlip = Math.abs(x - downPoint.x) > glSurfaceView.width / 4

                if (beginDrag) {
                    //滑动的时候
                    forceFlip = if (Math.abs(xVelocity) >= AppHelper.MIN_FLYING_VELOCITY) {
                        if (status == Status.SLIDING_TO_LEFT) {
                            xVelocity < 0
                        } else {
                            xVelocity > 0
                        }
                    } else {
                        if (status == Status.SLIDING_TO_LEFT) {
                            forceFlip or (x.toInt() < glSurfaceView.width / 2)
                        } else {
                            forceFlip or (x.toInt() > glSurfaceView.width / 2)
                        }
                    }
                }

                onFingerUp(x, y, forceFlip)

                if (status == Status.FLYING_TO_LEFT) {
                    PageManager.forwardPage()
                } else if (status == Status.FLYING_TO_RIGHT) {
                    PageManager.backPage()
                }

            }

            glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }
    }

    override fun cancel() {
        if(status == Status.BEGIN) {
            isDownActioned = false
        }
    }

    private fun fillTexture(isForward: Boolean, gotoBookEnd:Boolean = false): Boolean {
        var isFilled = false
        if (isForward) {
            if (PageManager.isReadyForward()) {
                page.setFirstTexture(PageManager.currentPage.textureID)
                page.setSecondTexture(PageManager.rightPage.textureID)
                isFilled = true
                println("fillForwardTexture")
            } else if (gotoBookEnd && PageManager.currentPage.position.group == ReaderStatus.chapterCount - 1
                    && PageManager.currentPage.position.index == PageManager.currentPage.position.groupChildCount - 1) {

                EventBus.getDefault().post(EventReaderConfig(ReaderSettings.ConfigType.GO_TO_BOOKEND))
            }
        } else {
            if (PageManager.isReadyBack()) {
                page.setFirstTexture(PageManager.leftPage.textureID)
                page.setSecondTexture(PageManager.currentPage.textureID)
                isFilled = true
                println("fillBackTexture")
            }
        }

        page.setBackColor(ReaderSettings.instance.backgroundColor)

        return isFilled
    }

    override fun onFlipUp() {
        down(10f, AppHelper.screenHeight.toFloat() - 10f)
        up(10f, AppHelper.screenHeight.toFloat() - 10f, 0F)
    }

    override fun onFlipDown() {
        down(AppHelper.screenWidth.toFloat() - 10f, AppHelper.screenHeight.toFloat() - 10f)
        up(AppHelper.screenWidth.toFloat() - 10f, AppHelper.screenHeight.toFloat() - 10f, 0F)
    }
}