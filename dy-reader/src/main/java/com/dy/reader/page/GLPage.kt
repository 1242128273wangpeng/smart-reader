package com.dy.reader.page

import android.content.res.Configuration
import android.graphics.*
import android.opengl.GLES20
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.dy.reader.ReadMediaManager
import com.dy.reader.Reader
import com.dy.reader.data.DataProvider
import com.dy.reader.helper.AppHelper
import com.dy.reader.helper.DrawTextHelper
import com.dy.reader.helper.INDEX_TEXTURE_ID
import com.dy.reader.helper.glCheckErr
import com.dy.reader.setting.ReaderSettings
import net.lzbook.kit.utils.logger.AppLog
import net.lzbook.kit.utils.runOnMain
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Created by xian on 18-3-22
 */
class GLPage(var position: Position, var refreshListener: RefreshListener?) {

    interface RefreshListener {
        fun onRefresh(position: Position)
    }

    var filledAD = false

    companion object {

        val lock = GLPage::class.java
        var semaphore = Semaphore(1, true)

        var mOrientation = Configuration.ORIENTATION_UNDEFINED

        private var bitmap: Bitmap? = null
        private var canvas: Canvas? = null

        fun createBitmap(orientation: Int) {
            if (mOrientation != orientation) {
                mOrientation = orientation
                bitmap = if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    Bitmap.createBitmap(AppHelper.screenWidth, AppHelper.screenHeight, Bitmap.Config.RGB_565)
                } else {
                    Bitmap.createBitmap(AppHelper.screenHeight, AppHelper.screenWidth, Bitmap.Config.RGB_565)
                }

                canvas = Canvas(bitmap)
                canvas?.density = Reader.context.resources.displayMetrics.densityDpi
            }
        }

        fun destroy() {
            synchronized(lock) {

                AppHelper.workQueueThread.shutdownNow()

                semaphore.release(10)

                canvas = null
                bitmap = null
                mOrientation = Configuration.ORIENTATION_UNDEFINED
            }
        }
    }

    var textureID = -1

    var isLoaded = AtomicBoolean(false)
    var notifyRefresh = AtomicBoolean(false)


    fun ready(orientation: Int = Configuration.ORIENTATION_PORTRAIT) {

        semaphore = Semaphore(1, true)

        createBitmap(orientation)

        //修正位置信息
        DataProvider.revisePosition(position)


//        prepareBitmap(position)
//
//        textureID = loadTexture(bitmap!!)[INDEX_TEXTURE_ID]
//
//        glCheckErr()
//
//        if (textureID != -1) {
//            isLoaded.set(true)
//            println("loadTexture $position")
//        } else {
//            System.err.println("loadTexture err $position")
//        }
    }


    fun loadTexture(callback: ((Boolean) -> Unit)? = null) {
        isLoaded.set(false)
        filledAD = false

        if (DataProvider.isGroupExist(position.group)) {
            DataProvider.revisePosition(position)

            val lastPosition = position.copy()
            runOnMain {
                //通过position 获取广告bitmap  main
                val adType = "${lastPosition.group}/${lastPosition.index}"
                val adBean = ReadMediaManager.adCache.get(adType)

                fun load(adBitmap: Bitmap?) {

                    AppHelper.runOnBackQueue {

                        semaphore.acquire()
                        try {
                            prepareBitmap(lastPosition)
                            println("prepareBitmap over $position")
                            //广告bitmap与文字bitmap合并
                            adBitmap?.apply {

                                if (adBean?.mark == "8-1") {
                                    canvas?.density?.let {
                                        adBitmap.density = it
                                    }
                                    canvas?.drawBitmap(adBitmap, 0F, AppHelper.screenHeight - adBean.height.toFloat(), null)
                                    filledAD = true
                                } else {

                                    if (this.height == (bitmap!!.height - (AppHelper.dp2px(26) * 2)) && bitmap!!.width == this.width) {
                                        val frontRect = Rect(0, 0, this.width, this.height)
                                        val baseRect = Rect(0, AppHelper.dp2px(26), bitmap!!.width, bitmap!!.height - AppHelper.dp2px(26))
                                        canvas?.drawBitmap(adBitmap, frontRect, baseRect, null)
                                        filledAD = true
                                    }

                                }

                            }

                        } catch (e: Exception) {
                            e.printStackTrace()

                            semaphore.release()
                            callback?.invoke(isLoaded.get())
                            if (notifyRefresh.get()) {
                                refreshListener?.onRefresh(position)
                            }
                            return@runOnBackQueue
                        }


                        AppHelper.runInGL {
                            //gl
                            try {
                                if (lastPosition.equals(position)) {

                                    position.offset = lastPosition.offset

                                    if (textureID <= 0) {
                                        textureID = com.dy.reader.helper.loadTexture(bitmap!!)[INDEX_TEXTURE_ID]
                                    } else {
                                        com.dy.reader.helper.loadTexture(bitmap!!, textureID)
                                    }


                                    glCheckErr()

                                    if (textureID > 0) {
                                        isLoaded.set(true)
                                        println("loadTexture $position")
                                    } else {
                                        System.err.println("loadTexture err $position")
                                    }


                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                semaphore.release()

                                callback?.invoke(isLoaded.get())

                                if (notifyRefresh.get()) {
                                    refreshListener?.onRefresh(position)
                                }
                            }
                        }
                    }
                }

                adBean?.view?.apply {
                    try {
                        if (this.visibility == View.VISIBLE) {
                            if (this.parent != null) {
                                var copy: Bitmap? = null
                                try {
                                    this.buildDrawingCache()
                                    copy = drawingCache?.copy(Bitmap.Config.ARGB_4444, false)
                                } catch (e: OutOfMemoryError) {
                                    e.printStackTrace()
                                    Glide.get(Reader.context).clearMemory()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Glide.get(Reader.context).clearMemory()
                                }
                                this.destroyDrawingCache()
                                load(copy)
                            } else {
                                ReadMediaManager.frameLayout?.removeAllViews()
                                if (this.parent != null) {
                                    (this.parent as ViewGroup).removeView(this)
                                }
                                ReadMediaManager.frameLayout?.addView(this)
                                ReadMediaManager.frameLayout?.post {
                                    var copy: Bitmap? = null
                                    try {
                                        this.buildDrawingCache()
                                        copy = drawingCache?.copy(Bitmap.Config.ARGB_4444, false)
                                    } catch (e: OutOfMemoryError) {
                                        e.printStackTrace()
                                        Glide.get(Reader.context).clearMemory()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Glide.get(Reader.context).clearMemory()
                                    }
                                    this.destroyDrawingCache()
                                    load(copy)
                                    ReadMediaManager.frameLayout?.removeAllViews()

                                }
                            }
                            return@runOnMain
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
                load(null)
            }
        } else {
            System.err.println("loadTexture not exist $position")
            callback?.invoke(false)
        }
    }

    fun unloadTexture() {
        isLoaded.set(false)
        if (textureID != -1) {
            GLES20.glDeleteTextures(1, arrayOf(textureID).toIntArray(), 0)
        }
        textureID = -1
    }

    fun refresh(callback: ((Boolean) -> Unit)? = null) {

        notifyRefresh.set(true)

        loadTexture(callback)
    }

    private fun prepareBitmap(posi: Position) {
        synchronized(lock) {
            val startTime = System.currentTimeMillis()

//        canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            canvas?.drawColor(Color.WHITE)

            when (ReaderSettings.instance.readThemeMode) {
                51 -> {
                    val bitmap = if (ReaderSettings.instance.isLandscape) {
                        ReaderSettings.instance.kraftBitmapLandscape
                    } else {
                        ReaderSettings.instance.kraftBitmapPortrait
                    }
                    setBitmap(bitmap)
                }
                511 -> {
                    val bitmap = if (ReaderSettings.instance.isLandscape) {
                        ReaderSettings.instance.blueBitmapLandscape
                    } else {
                        ReaderSettings.instance.blueBitmapPortrait
                    }
                    setBitmap(bitmap)
                }
                512 -> {
                    val bitmap = if (ReaderSettings.instance.isLandscape) {
                        ReaderSettings.instance.pinkBitmapLandscape
                    } else {
                        ReaderSettings.instance.pinkBitmapPortrait
                    }
                    setBitmap(bitmap)
                }
                513 -> {
                    val bitmap = if (ReaderSettings.instance.isLandscape) {
                        ReaderSettings.instance.greenBitmapLandscape
                    } else {
                        ReaderSettings.instance.greenBitmapPortrait
                    }
                    setBitmap(bitmap)
                }
                514 -> {
                    val bitmap = if (ReaderSettings.instance.isLandscape) {
                        ReaderSettings.instance.darkBitmapLandscape
                    } else {
                        ReaderSettings.instance.darkBitmapPortrait
                    }
                    setBitmap(bitmap)
                }
                515 -> {
                    val bitmap = if (ReaderSettings.instance.isLandscape) {
                        ReaderSettings.instance.dimBitmapLandscape
                    } else {
                        ReaderSettings.instance.dimBitmapPortrait
                    }
                    setBitmap(bitmap)
                }
                else -> {
                    canvas?.drawColor(ReaderSettings.instance.backgroundColor)
                }
            }
            if (DataProvider.isGroupExist(posi.group)) {
                DrawTextHelper.drawText(canvas, DataProvider.getPage(posi))
            } else {
                println("cant draw page content == null")
            }

            println("loadBitmap ${posi.group}:${posi.index} use time ${System.currentTimeMillis() - startTime}")
        }
    }

    /**
     * 设置横竖屏Bitmap
     */
    private fun setBitmap(bitmap: Bitmap?) {
        bitmap?.let {
            canvas?.drawBitmap(it, Rect(0, 0, it.width, it.height),
                    Rect(0, 0, canvas?.width ?: 0, canvas?.height ?: 0), null)
        }
    }


    /**
     * 从view中获取bitmap
     */
    fun loadBitmapFromView(v: View?): Bitmap? {
        if (v == null) {
            return null
        }
        val bitmap = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_4444)
        val canvas = Canvas(bitmap)
        canvas.translate((-v.scrollX).toFloat(), (-v.scrollY).toFloat())//我们在用滑动View获得它的Bitmap时候，获得的是整个View的区域（包括隐藏的），如果想得到当前区域，需要重新定位到当前可显示的区域
        v.draw(canvas)// 将 view 画到画布上
        return bitmap
    }

    /**
     * 从view中获取bitmap
     */
    fun getBitmapFromView(view: View): Bitmap {
        view.destroyDrawingCache()
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        view.isDrawingCacheEnabled = true
        return view.getDrawingCache(true)
    }


    /**
     * 把两个位图覆盖合成为一个位图，以底层位图的长宽为基准
     * @param backBitmap 在底部的位图
     * @param frontBitmap 盖在上面的位图
     * @return
     */
    fun mergeBitmap(backBitmap: Bitmap?, frontBitmap: Bitmap?, top: Int = 0): Bitmap? {

        if (backBitmap == null || backBitmap.isRecycled
                || frontBitmap == null || frontBitmap.isRecycled) {
            AppLog.e("mergeBitmap:", "backBitmap=$backBitmap;frontBitmap=$frontBitmap")
            return null
        }
        val bitmap = backBitmap.copy(Bitmap.Config.RGB_565, true)
        val canvas = Canvas(bitmap)
        val baseRect = Rect(0, 0, backBitmap.width, backBitmap.height)
        val frontRect = Rect(0, top, frontBitmap.width, frontBitmap.height)
        canvas.drawBitmap(frontBitmap, frontRect, baseRect, null)
        return bitmap
    }
}