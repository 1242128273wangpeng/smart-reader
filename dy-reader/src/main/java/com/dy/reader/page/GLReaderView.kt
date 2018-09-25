package com.dy.reader.page

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import com.dy.reader.animation.*
import com.dy.reader.event.EventLoading
import com.dy.reader.event.EventReaderConfig
import com.dy.reader.event.EventSetting
import com.dy.reader.setting.ReaderSettings
import com.dy.reader.setting.ReaderStatus
import net.lzbook.kit.utils.oneclick.OneClickUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by xian on 18-3-24.
 */

class GLReaderView : GLSurfaceView, GLSurfaceView.Renderer {

    enum class AnimationType {
        //用枚举的ordinal 对应旧版设置的魔数
        NONE,
        CURL, TRANSLATION, LIST, AUTO, OVERLAP
    }

    var glAnimation: IGLAnimation? = null

    constructor(context: Context) : super(context) {
        initParams()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initParams()
    }

    private fun initParams() {
        isSoundEffectsEnabled = true
        setEGLContextClientVersion(2)
        preserveEGLContextOnPause = true
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        setRenderer(this)
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    @Subscribe(threadMode = org.greenrobot.eventbus.ThreadMode.ASYNC)
    fun onNeedRefresh(event: EventReaderConfig) {
        if (event.type == ReaderSettings.ConfigType.ANIMATION) {
            queueEvent {
                createGLAnimation()
            }
        } else if (event.type == ReaderSettings.ConfigType.AUTO_PAUSE) {
            if (glAnimation is AutoAnimation) {
                (glAnimation as AutoAnimation).pause()
            }
        } else if (event.type == ReaderSettings.ConfigType.AUTO_RESUME) {
            if (glAnimation is AutoAnimation) {
                (glAnimation as AutoAnimation).resume()
            }
        }
    }

    private fun createGLAnimation() {
        glAnimation?.unloadProgram()

        when (ReaderSettings.instance.animation) {
            AnimationType.OVERLAP -> {
                glAnimation = OverlapAnimation(this)
            }
            AnimationType.TRANSLATION -> {
                glAnimation = TranslationAnimation(this)
            }
            AnimationType.AUTO -> {
                glAnimation = AutoAnimation(this)
            }
            else -> {
                glAnimation = CurlAnimation(this)
            }
        }

        glAnimation!!.loadProgram()
    }


//    var lastColor = Color.parseColor("#ff607d8b")
//    var newColor = Color.parseColor("#ff607d8b")
//    var red = Color.red(newColor) / 255F
//    var green = Color.green(newColor) / 255F
//    var blue = Color.blue(newColor) / 255F

    override fun onDrawFrame(gl: GL10?) {
//        println("renderer onDrawFrame")
//        GLES20.glClearColor(red, green, blue, 1.0F)

        GLES20.glClearColor(ReaderSettings.instance.backgroundColoRed,
                ReaderSettings.instance.backgroundColorGreen,
                ReaderSettings.instance.backgroundColorBlue,
                1F)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)


        glAnimation?.drawFrame()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        println("renderer onSurfaceChanged $width * $height")

//        PageManager.clear()

        if (ReaderSettings.instance.isLandscape && (width < height)) {
            println("isLandscape && (width < height)")
            return
        } else if (!ReaderSettings.instance.isLandscape && (width > height)) {
            println("!isLandscape && (width > height)")
            return
        }

        if (!PageManager.isReady) {

            PageManager.prepare(ReaderStatus.position)

            createGLAnimation()

            PageManager.currentPage.ready()

            queueEvent {
                PageManager.currentPage.loadTexture {
                    PageManager.rightPage.loadTexture {
                        PageManager.leftPage.loadTexture()

                        postDelayed({
                            EventBus.getDefault().post(EventLoading(EventLoading.Type.SUCCESS))
                        }, 200)
                    }
                    requestRender()
                }
            }
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        println("renderer surfaceCreated")

    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {

        parent.requestDisallowInterceptTouchEvent(true)

        if (event == null) {
            println("onTouchEvent event == null")
            return false
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> onActionDown(event)
            MotionEvent.ACTION_MOVE -> onActionMove(event)
            MotionEvent.ACTION_UP -> onActionUp(event)
            MotionEvent.ACTION_CANCEL -> onActionCancel()
        }
        return true
    }

    private var velocityTracker: VelocityTracker? = null

    fun getTracker(): VelocityTracker {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        return velocityTracker!!
    }

    fun onActionDown(event: MotionEvent) {

        getTracker().addMovement(event)

        queueEvent {
            println("ACTION_DOWN")

            try {
                glAnimation?.down(event.x, event.y)
            } catch (e: Exception) {
            }
        }
    }

    fun onActionMove(event: MotionEvent) {
        hideAd()
        getTracker().addMovement(event)

        queueEvent {
            try {
                glAnimation?.move(event.x, event.y)
            } catch (e: Exception) {
            }
        }
    }

    fun onActionUp(event: MotionEvent) {
        hideAd()
        getTracker().addMovement(event)
        getTracker().computeCurrentVelocity(1000, 10000F)

        val xVelocity = getTracker().xVelocity
        queueEvent {
            println("ACTION_UP")

            try {
                glAnimation?.up(event.x, event.y, xVelocity)
            } catch (e: Exception) {
            }
        }

        velocityTracker?.recycle()
        velocityTracker = null
    }

    fun onActionCancel() {
        queueEvent {
            println("ACTION_CANCEL")

            glAnimation?.cancel()
        }
        velocityTracker?.recycle()
        velocityTracker = null
    }

    fun onKeyEvent(event: KeyEvent): Boolean {
        return when {
            event.keyCode == KeyEvent.KEYCODE_VOLUME_UP && this.visibility == View.VISIBLE -> {
                if(!OneClickUtil.isVolumDoubleClick(System.currentTimeMillis())){
                    onClickLife()
                }

                true
            }
            event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && this.visibility == View.VISIBLE -> {
                if(!OneClickUtil.isVolumDoubleClick(System.currentTimeMillis())){
                    onClickRight()
                }

                true
            }
            else -> false
        }
    }

    fun onClickLife() {
        hideAd()
        queueEvent {
            glAnimation?.onFlipUp()
        }
    }

    fun onClickRight() {
        hideAd()
        queueEvent {
            glAnimation?.onFlipDown()
        }
    }

    private fun hideAd() = EventBus.getDefault().post(EventSetting(EventSetting.Type.HIDE_AD))

}