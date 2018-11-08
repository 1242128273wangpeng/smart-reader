package com.dy.reader.helper

import android.annotation.SuppressLint
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.view.ViewConfiguration
import com.dy.reader.Reader
import net.lzbook.kit.utils.logger.AppLog
import java.util.concurrent.Executors

@SuppressLint("StaticFieldLeak")
/**
 * Created by Xian on 2018/3/12.
 */
object AppHelper {

    val viewConfiguration by lazy { ViewConfiguration.get(Reader.context) }

    val touchSlop by lazy {
        viewConfiguration.scaledTouchSlop
    }

    val pagingTouchSlop by lazy {
        viewConfiguration.scaledPagingTouchSlop
    }


    var screenDensity = 0.toFloat()

    var screenScaledDensity = 0.toFloat()

    var screenWidth = 0

    var screenHeight = 0

    val MIN_FLYING_VELOCITY by lazy {
        400 * Reader.context.resources.displayMetrics.density
    }


    var workQueueThread = Executors.newSingleThreadExecutor()

    fun runOnBackQueue(action: () -> Unit) {
        if(workQueueThread.isShutdown){
            workQueueThread = Executors.newSingleThreadExecutor()
        }
        val future = workQueueThread.submit(action)
    }

    val workerThread by lazy {
        Executors.newCachedThreadPool()
    }

//    fun <T> runOnBackground(action: () -> T):Future<T>{
//        return workerThread.submit(action)
//    }

    fun runOnBackground(action: () -> Unit){
        workerThread.submit(action)
    }

    val mainHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    fun runInMain(action: () -> Unit) {
        mainHandler.post(action)
    }

    var glSurfaceView: GLSurfaceView? = null

    fun runInGL(action: () -> Unit) {
        glSurfaceView?.queueEvent {
            action.invoke()
        }
        if(glSurfaceView == null){
            AppLog.e("GL", "glSurfaceView = null")
        }
    }

    /**
     * 根据手机分辨率从DP转成PX
     * @param context
     * @param dpValue
     * @return
     */
    fun dp2px(dpValue: Int): Int {
        val scale = Reader.context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     * @param spValue
     * @return
     */
    fun sp2px(spValue: Int): Int {
        val fontScale = Reader.context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }

    /**
     * 根据手机的分辨率PX(像素)转成DP
     * @param context
     * @param pxValue
     * @return
     */
    fun px2dp(pxValue: Int): Int {
        val scale = Reader.context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     * @param pxValue
     * @return
     */

    fun px2sp(pxValue: Int): Int {
        val fontScale = Reader.context.resources.displayMetrics.scaledDensity
        return (pxValue / fontScale + 0.5f).toInt()
    }
}