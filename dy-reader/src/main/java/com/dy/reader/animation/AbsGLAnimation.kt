package com.dy.reader.animation

import android.graphics.PointF
import android.opengl.GLSurfaceView
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Scroller
import com.dy.reader.data.DataProvider
import com.dy.reader.event.EventReaderConfig
import com.dy.reader.event.EventSetting
import com.dy.reader.flip.Status
import com.dy.reader.helper.AppHelper
import com.dy.reader.page.GLPage
import com.dy.reader.page.PageManager
import com.dy.reader.setting.ReaderSettings
import com.dy.reader.setting.ReaderStatus
import org.greenrobot.eventbus.EventBus
import kotlin.properties.Delegates

/**
 * Created by xian on 18-3-22.
 */
abstract class AbsGLAnimation(val glSurfaceView: GLSurfaceView) : IGLAnimation {
    val width = glSurfaceView.width
    val height = glSurfaceView.height

    var status: Status = Status.BEGIN

    val DURATION = 200

    //每次按下的那个点
    val downPoint = PointF()

    //如果在flying中, 这个点代表上次按下的那个点
    val originPoint = PointF()

    //目标点
    val targetPointF = PointF()

    //最新点
    val currentPoint = PointF()


    val scroller = Scroller(glSurfaceView.context, AccelerateDecelerateInterpolator())
//    val scroller = Scroller(App.app, CurlInterpolator())

    var firstPage by Delegates.notNull<GLPage>()
    var secondPage by Delegates.notNull<GLPage>()

    init {
        firstPage = PageManager.currentPage
        secondPage = PageManager.rightPage
    }

    abstract fun onConfirmOritation()

    abstract fun resetOffset()
    abstract fun computeOffset()
    abstract fun getMargin(): Float

    override fun drawFrame() {


//            val currentTime = System.currentTimeMillis()
//            if (scroller.isFinished && currentTime - animationStartTime < DURATION) {
//                //开始滑动时使用动画
//                var t = 1f - (currentTime - animationStartTime).toFloat() / DURATION
//                //interp
//                t = 1f - (t * t * t * (3 - 2 * t))
//
//                curDistanceX = (targetPointF.x - originPoint.x) * t
//            }

        if (!scroller.isFinished) {
            scroller.computeScrollOffset()
            currentPoint.x = scroller.currX.toFloat()
            currentPoint.y = scroller.currY.toFloat()
        }

        computeOffset()

        draw()

        onDrawFrameEnd()
    }

    abstract protected fun draw()


    fun isFlying(): Boolean {
        return status == Status.FLYING_TO_LEFT
                || status == Status.FLYING_TO_RIGHT
                || status == Status.BACK_TO_LEFT
                || status == Status.BACK_TO_RIGHT
    }

    private fun onDrawFrameEnd() {
        if (isFlying()) {
            if (scroller.isFinished) {
                println("onDrawFrameEnd")

//                if (status == Status.FLYING_TO_LEFT) {
//                    PageManager.forwardPage()
//                }
//
//                if (status == Status.FLYING_TO_RIGHT) {
//                    PageManager.backPage()
//                }

                firstPage = PageManager.currentPage
                secondPage = PageManager.rightPage

                resetOffset()

                status = Status.BEGIN

                currentPoint.set(0F, 0F)
                originPoint.set(0F, 0F)

                glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
                EventBus.getDefault().post(EventSetting(EventSetting.Type.SHOW_AD))
            }
        }

    }


    var firstMove = true

    var shouldUseEvent = false

    var isDownActioned = false

    override fun down(x: Float, y: Float) {
        if(!isDownActioned) {
            isDownActioned = true
            downPoint.set(x, y)
            firstMove = true
        }
    }

    override fun move(x: Float, y: Float) {
        if (!isDownActioned) {
            down(x, y)
            return
        }

        targetPointF.set(x, y)

        if (firstMove
                && Math.abs(x - downPoint.x) > AppHelper.touchSlop) {
//            ToastUtils.showToastNoRepeat("触发滑动事件")
            firstMove = false
            val slop = x - downPoint.x

            currentPoint.set(x, y)

            originPoint.set(downPoint)

            if (!scroller.isFinished) {

                //不同方向不切换文理, 只打断动画
                if (slop < 0 &&
                        (status == Status.FLYING_TO_LEFT
                                || status == Status.BACK_TO_LEFT)) {
                    shouldUseEvent = PageManager.isReadyForward()

                    if (shouldUseEvent) {

                        onPageForward()
                    }

                } else if (slop > 0 &&
                        (status == Status.FLYING_TO_RIGHT
                                || status == Status.BACK_TO_RIGHT)) {
                    shouldUseEvent = PageManager.isReadyBack()

                    if (shouldUseEvent) {

                        onPageBack()
                    }
                } else {
                    shouldUseEvent = true
                }

                if (shouldUseEvent) {
                    if (slop < 0) {
                        status = Status.SLIDING_TO_LEFT
                    } else {
                        status = Status.SLIDING_TO_RIGHT
                    }
                }
            } else {
                //正常滑动
                if (slop < 0) {
                    shouldUseEvent = PageManager.isReadyForward()

                    if (shouldUseEvent) {
                        status = Status.SLIDING_TO_LEFT
                    }

                } else {
                    shouldUseEvent = PageManager.isReadyBack()

                    if (shouldUseEvent) {
                        status = Status.SLIDING_TO_RIGHT
                        onPageBack()
                    }
                }
            }


            if (shouldUseEvent) {

                onConfirmOritation()

                scroller.abortAnimation()
            }
        }

        if (shouldUseEvent && !firstMove) {

            currentPoint.set(x, y)

            if (status == Status.SLIDING_TO_LEFT) {
                currentPoint.x = Math.min(originPoint.x, currentPoint.x)
            } else if (status == Status.SLIDING_TO_RIGHT) {
                currentPoint.x = Math.max(originPoint.x, currentPoint.x)
            }

        } else if (!firstMove && !DataProvider.isGroupAvalable(PageManager.rightPage.position.group)) {
            EventBus.getDefault().post(EventReaderConfig(ReaderSettings.ConfigType.GO_TO_BOOKEND))
        }
    }


    override fun up(x: Float, y: Float, xVelocity: Float) {
        if(!isDownActioned || (!firstMove && !shouldUseEvent)){
            firstMove = true
            shouldUseEvent = false
            isDownActioned = false
            return
        }


//        val dx = x - downPoint.x

//        ToastUtils.showToastNoRepeat("触发结束")
        val useVelocity = Math.abs(xVelocity) > AppHelper.MIN_FLYING_VELOCITY

        when (status) {
            Status.SLIDING_TO_LEFT -> {
                if (!useVelocity) {
//                    if (-dx >= width / 3) {
                    if (xVelocity < 0) {

                        targetPointF.x = width - width.toFloat() * (1 + getMargin()) - (width - originPoint.x)
                        targetPointF.y = 0F

                        status = Status.FLYING_TO_LEFT
                    } else {
                        targetPointF.x = originPoint.x
                        targetPointF.y = 0F

                        status = Status.BACK_TO_RIGHT
                    }
                } else {
                    if (xVelocity < 0 || Math.abs(xVelocity) < 2 * AppHelper.MIN_FLYING_VELOCITY) {
                        targetPointF.x = width - width.toFloat() * (1 + getMargin()) - (width - originPoint.x)
                        targetPointF.y = 0F

                        status = Status.FLYING_TO_LEFT
                    } else {

                        targetPointF.x = originPoint.x
                        targetPointF.y = 0F

                        status = Status.BACK_TO_RIGHT
                    }
                }
            }
            Status.SLIDING_TO_RIGHT -> {
                if (!useVelocity) {
//                    if (dx >= width / 3) {
                    if (xVelocity > 0) {
                        targetPointF.x = width.toFloat() + originPoint.x
                        targetPointF.y = 0F

                        status = Status.FLYING_TO_RIGHT
                    } else {
                        targetPointF.x = originPoint.x - width * getMargin()
                        targetPointF.y = 0F

                        status = Status.BACK_TO_LEFT
                    }
                } else {
                    if (xVelocity > 0 || Math.abs(xVelocity) < 2 * AppHelper.MIN_FLYING_VELOCITY) {
                        targetPointF.x = width.toFloat() + originPoint.x
                        targetPointF.y = 0F

                        status = Status.FLYING_TO_RIGHT
                    } else {
                        targetPointF.x = originPoint.x - width * getMargin()
                        targetPointF.y = 0F

                        status = Status.BACK_TO_LEFT
                    }
                }
            }
            else -> {


                if (!useVelocity) {

                    originPoint.set(width.toFloat(), 0f)
                    currentPoint.set(width.toFloat(), 0f)


                    if (x >= width / 2 || ReaderSettings.instance.isFullScreenRead) {

                        shouldUseEvent = PageManager.isReadyForward()

                        if (shouldUseEvent) {
                            targetPointF.x = 0 - width * getMargin()
                            targetPointF.y = 0F

                            status = Status.FLYING_TO_LEFT

                            onPageForward()
                        } else if (PageManager.currentPage.position.group == ReaderStatus.chapterCount - 1
                                && PageManager.currentPage.position.index == PageManager.currentPage.position.groupChildCount - 1) {

                            EventBus.getDefault().post(EventReaderConfig(ReaderSettings.ConfigType.GO_TO_BOOKEND))
                        }
                    } else {

                        shouldUseEvent = PageManager.isReadyBack()


                        if (shouldUseEvent) {

                            targetPointF.x = width + width.toFloat()
                            targetPointF.y = 0F

                            status = Status.FLYING_TO_RIGHT

                            onPageBack()
                        }
                    }

                    if (shouldUseEvent) {
                        onConfirmOritation()
//                    AppHelper.runInMain {
//                        glSurfaceView.playSoundEffect(SoundEffectConstants.CLICK)
//                    }
                    }
                }

            }
        }
        if (shouldUseEvent) {
            scroller.abortAnimation()

            if (status == Status.FLYING_TO_LEFT) {
                PageManager.forwardPage()
            }

            if (status == Status.FLYING_TO_RIGHT) {
                PageManager.backPage()
            }


            scroller.startScroll(
                    currentPoint.x.toInt(),
                    currentPoint.y.toInt(),
                    (targetPointF.x - currentPoint.x).toInt(),
                    (targetPointF.y - currentPoint.y).toInt(),
                    400)
        }

        isDownActioned = false
        firstMove = true
        shouldUseEvent = false
    }

    override fun cancel(){
        if(status == Status.BEGIN) {
            isDownActioned = false
        }
    }

    private fun onPageBack() {
        firstPage = PageManager.leftPage
        secondPage = PageManager.currentPage
    }

    private fun onPageForward() {
        firstPage = PageManager.currentPage
        secondPage = PageManager.rightPage
    }

    override fun onFlipUp() {
        down(10f, AppHelper.screenHeight.toFloat() - 10f)
        up(10f, AppHelper.screenHeight.toFloat() - 10f, 0F)
    }

    override fun onFlipDown() {
        down(AppHelper.screenWidth.toFloat() - 10f, AppHelper.screenHeight.toFloat() - 10f)
        up(AppHelper.screenWidth.toFloat() - 10f, AppHelper.screenHeight.toFloat() - 10f, 0f)
    }

}
