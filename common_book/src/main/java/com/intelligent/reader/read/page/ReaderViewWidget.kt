package com.intelligent.reader.read.page

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import com.intelligent.reader.activity.ReadingActivity
import com.intelligent.reader.flip.PageFlipView
import com.intelligent.reader.flip.base.PageFlip
import com.intelligent.reader.flip.render.SinglePageRender
import com.intelligent.reader.read.animation.BitmapManager
import com.intelligent.reader.read.factory.ReaderViewFactory
import com.intelligent.reader.read.help.HorizontalEvent
import com.intelligent.reader.read.help.IReadPageChange
import com.intelligent.reader.read.help.IReadView
import com.intelligent.reader.read.help.IReadWidget
import kotlinx.android.synthetic.main.layout_custom_dialog.view.*
import net.lzbook.kit.data.bean.ReadConfig
import net.lzbook.kit.data.bean.ReadViewEnums
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.runOnMain


/**
 * 阅读容器
 * Created by wt on 2017/12/13.
 */
class ReaderViewWidget : FrameLayout, IReadWidget, HorizontalEvent {

    companion object {
        val tag = "PageFlipView"
    }

    private var mReaderViewFactory: ReaderViewFactory? = null

    private var mReaderView: IReadView? = null

    private var mTextureView: PageFlipView? = null

    private var animaEnums: ReadViewEnums.Animation? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var mLoadBitmaplistener = SinglePageRender.LoadBitmapListener { index ->
        if ((mTextureView != null) and (mTextureView!!.isFangzhen) and (mReaderView is HorizontalReaderView)) {

            var bitmap: Bitmap? = null
            var isFinishCache = false

            runOnMain {
                println("${ReaderViewWidget.tag} load ${index.name}")
                val view = (mReaderView as HorizontalReaderView).findViewWithTag(index) as HorizontalPage
                if (view.hasAd) {
                    view.destroyDrawingCache()
                }
                bitmap = view.drawingCache

                synchronized((this@ReaderViewWidget as Object)) {
                    println("this@ReaderViewWidget as Object).notify")
                    (this@ReaderViewWidget as Object).notify()
                }
                isFinishCache = true
            }


            synchronized((this@ReaderViewWidget as Object)) {
                if (!isFinishCache) {
                    try {
                        println("this@ReaderViewWidget as Object).wait ${Thread.currentThread().name}")
                        (this@ReaderViewWidget as Object).wait()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            return@LoadBitmapListener bitmap
        }
        BitmapManager.getInstance().createBitmap()
    }
    var isFlow: Boolean? = null
    private var mBeginLisenter = object : PageFlip.BeginListener {
        override fun beginNext() {
            if (mReaderView is HorizontalReaderView) {
                if (isFlow == false) {
                    mTextureView?.onDrawNextFrame(true)
                }
                isFlow = true
            }
        }

        override fun beginPre() {
            if (mReaderView is HorizontalReaderView) {
                if (isFlow == true) {
                    mTextureView?.onDrawNextFrame(false)
                }
                isFlow = false
            }
        }
    }

    private var mPageFlipStateListener = object : SinglePageRender.PageFlipStateListener {

        private fun invisibelSurface() {
            //等待ViewPager切换完页面再隐藏
            runOnMain {
                var curView = (mReaderView as HorizontalReaderView).findViewWithTag(ReadViewEnums.PageIndex.current) as HorizontalPage
                if (curView.hasAd) {
                    mTextureView!!.alpha = 0f
                    mTextureView!!.queueEvent {
                        mTextureView!!.getmPageRender().mPageFlip.firstPage.deleteAllTextures()
                    }
                }
            }
        }

        override fun backward() {
            AppLog.e(ReaderViewWidget.tag, "backward")
            (mReaderView as HorizontalReaderView).onClickLeft(false)

            invisibelSurface()
        }

        override fun forward() {
            AppLog.e(ReaderViewWidget.tag, "forward")
            (mReaderView as HorizontalReaderView).onClickRight(false)
            invisibelSurface()
        }

        override fun restore() {
            AppLog.e(ReaderViewWidget.tag, "restore")
            invisibelSurface()
        }
    }

    /**
     * 初始化GLSufaceView
     */
    private fun initGLSufaceView() {
        removeView(mTextureView)
        if (mTextureView == null) {
            mTextureView = PageFlipView(context)
        }
        mTextureView!!.alpha = 0f
        //加载Bitmap数据监听
        (mTextureView?.getmPageRender() as SinglePageRender).setListener(mLoadBitmaplistener)
        //翻页动画结束监听
        (mTextureView?.getmPageRender() as SinglePageRender).setPageFlipStateListenerListener(mPageFlipStateListener)
        mTextureView?.setBeginLisenter(mBeginLisenter)
        addView(mTextureView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        if (Build.VERSION.SDK_INT < 16) {
            (content as Activity).window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            mTextureView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
        mTextureView?.isFangzhen = when (animaEnums) {
            ReadViewEnums.Animation.curl -> true
            else -> false
        }
//        mTextureView?.createGLThread()
    }

    /**
     * 初始化ReaderViewFactory
     */
    fun initReaderViewFactory() = if (mReaderViewFactory == null) mReaderViewFactory = ReaderViewFactory(context) else Unit

    /**
     * 入口
     */
    override fun entrance() {
        if (animaEnums != ReadConfig.animation) { //如果阅读模式发生变化
            if (mReaderView != null) removeView(mReaderView as View)//移除
            mReaderView = mReaderViewFactory?.getView(ReadConfig.animation)//创建
            (mReaderView as View).isClickable = false
            if (mReaderView != null) addView(mReaderView as View)//添加
            animaEnums = ReadConfig.animation//记录动画模式
            mReaderView?.setHorizontalEventListener(this)
            initGLSufaceView()
        }
        mReaderView?.entrance()
    }

    override fun onPause() {
        mTextureView?.alpha = 0f
    }


    /**
     * 设置 IReadView 实现 View 的变化监听
     * @param mReadPageChange 监听对象
     */
    override fun setIReadPageChange(mReadPageChange: IReadPageChange?) = mReaderView?.setIReadPageChange(mReadPageChange) ?: Unit

    override fun changeAnimMode(mode: Int) {
        ReadConfig.animation = when (mode) {
            0 -> ReadViewEnums.Animation.slide
            1 -> {
                mTextureView?.isFangzhen = true
                ReadViewEnums.Animation.curl
            }
            2 -> ReadViewEnums.Animation.shift
            else -> ReadViewEnums.Animation.list
        }
        if (mode != 1) mTextureView!!.alpha = 0f
        mReaderView?.onAnimationChange(ReadConfig.animation)
    }

    private var isDownActioned = false

    override fun myDispatchTouchEvent(event: MotionEvent): Boolean {
        AppLog.e("touch", event.action.toString())

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isDownActioned = true
                if (context is ReadingActivity) (context as ReadingActivity).showMenu(false)

                if (mTextureView!!.alpha != 1.0f) {
                    //翻页显示
                    mTextureView!!.alpha = 1.0f
                }

                mTextureView?.onFingerDown(event.x, event.y)
            }

            MotionEvent.ACTION_MOVE -> {
                if(isDownActioned) {
                    mTextureView?.onFingerMove(event.x, event.y)
                }else{
                    isDownActioned = true
                    if (context is ReadingActivity) (context as ReadingActivity).showMenu(false)

                    if (mTextureView!!.alpha != 1.0f) {
                        //翻页显示
                        mTextureView!!.alpha = 1.0f
                    }
                    mTextureView?.onFingerDown(event.x, event.y)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if(isDownActioned) {
                    isDownActioned = false
                    mTextureView?.onFingerUp(event.x, event.y)
                }
            }

        }

        return true
    }

}