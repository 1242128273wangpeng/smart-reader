package com.intelligent.reader.read.page

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import com.intelligent.reader.activity.ReadingActivity
import com.intelligent.reader.flip.PageFlipView
import com.intelligent.reader.flip.render.SinglePageRender
import com.intelligent.reader.read.factory.ReaderViewFactory
import com.intelligent.reader.read.help.HorizontalEvent
import com.intelligent.reader.read.help.IReadPageChange
import com.intelligent.reader.read.help.IReadView
import com.intelligent.reader.read.help.IReadWidget
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

    private val mReaderViewFactory by lazy{
        ReaderViewFactory(context)
    }

    private var mReaderView: IReadView? = null

    private var mTextureView: PageFlipView? = null

    private var mAutoReadView: AutoReadView? = null

    private var lastPageAnimation: ReadViewEnums.Animation? = null

    private var mAutoReadSpeed = 0.0

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var mLoadBitmaplistener = SinglePageRender.LoadBitmapListener { index ->

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


    private var mPageFlipStateListener = object : SinglePageRender.PageFlipStateListener {

        private fun invisibelSurface(): Boolean {
            //等待ViewPager切换完页面再隐藏
            var curView = (mReaderView as HorizontalReaderView).findViewWithTag(ReadViewEnums.PageIndex.current) as HorizontalPage
//            runOnMain {
                if (curView.hasAd) {
                    mTextureView?.alpha = 0F
//                    mTextureView?.onChangTexture()
                }
//            }
            return curView.hasAd
        }

        override fun backward(): Boolean {
            AppLog.e(ReaderViewWidget.tag, "backward")
            (mReaderView as HorizontalReaderView).onClickLeft(false)

            return invisibelSurface()
        }

        override fun forward(): Boolean {
            AppLog.e(ReaderViewWidget.tag, "forward")
            (mReaderView as HorizontalReaderView).onClickRight(false)
            return invisibelSurface()
        }

        override fun restore(): Boolean {
            AppLog.e(ReaderViewWidget.tag, "restore")
            return invisibelSurface()
        }
    }

    private val mMaximumVelocity by lazy{
        ViewConfiguration.get(context).scaledMaximumFlingVelocity.toFloat()
    }

    /**
     * 初始化GLSufaceView
     */
    private fun initGLSufaceView() {

        if (lastPageAnimation == ReadViewEnums.Animation.curl) {
            if (mTextureView == null) {
                mTextureView = PageFlipView(context)
            }
            if(mTextureView!!.parent == null) {
                mTextureView?.alpha = 0f
                //加载Bitmap数据监听
                (mTextureView?.getmPageRender() as SinglePageRender).setListener(mLoadBitmaplistener)
                //翻页动画结束监听
                (mTextureView?.getmPageRender() as SinglePageRender).setPageFlipStateListenerListener(mPageFlipStateListener)
                addView(mTextureView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                mTextureView?.let {
                    ReadConfig.registObserver(it)
                }
            }
        }
    }

    private fun initAutoReadView() {

        if (lastPageAnimation != ReadViewEnums.Animation.list) {
            if (mAutoReadView == null) {
                mAutoReadView = AutoReadView(context)
                mAutoReadView?.setAutoReadSpeed(mAutoReadSpeed)
            }
            mAutoReadView?.setOnAutoReadViewLoadCallback(object : AutoReadView.OnAutoReadViewLoadCallback {

                override fun onStart() {
                    mTextureView?.visibility = View.INVISIBLE
                    mAutoReadView?.visibility = View.VISIBLE
                }

                override fun onResume() {
                    mOnAutoReadCallback?.onAutoReadResume()
                }

                override fun onStop() {
                    mTextureView?.visibility = View.VISIBLE
                    mAutoReadView?.visibility = View.INVISIBLE
                }

                override fun onPause() {
                    mOnAutoReadCallback?.onAutoReadStop()
                }

                override fun onNextPage() {
                    if (mReaderView is HorizontalReaderView) {
                        (mReaderView as HorizontalReaderView).onClickRight(false)
                    }
                }

                override fun loadBitmap(index: ReadViewEnums.PageIndex): Bitmap? {
                    if (mReaderView is HorizontalReaderView) {
                        val view = (mReaderView as HorizontalReaderView).findViewWithTag(index) as HorizontalPage
                        if (view.hasAd) {
                            view.destroyDrawingCache()
                        }
                        return view.drawingCache
                    }
                    return null
                }

            })
            addView(mAutoReadView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            mAutoReadView?.visibility = View.INVISIBLE
        }
    }

    override fun startAutoRead() {
        mAutoReadView?.startAutoRead()
    }

    override fun stopAutoRead() {
        mAutoReadView?.closeAutoRead()
    }

    override fun isAutoRead(): Boolean {
        return mAutoReadView?.isAutoRead() ?: false
    }

    /**
     * 入口
     */
    override fun entrance() {

        if(mReaderView != null){
            removeView(mReaderView as View)
        }
        if(mTextureView != null){
            removeView(mTextureView)
        }
        if(mAutoReadView != null){
            removeView(mAutoReadView)
        }


        lastPageAnimation = ReadConfig.animation//记录动画模式

        mReaderView = mReaderViewFactory?.getView(ReadConfig.animation)//创建
        (mReaderView as View).isClickable = false

        addView(mReaderView as View)//添加

        mReaderView!!.setHorizontalEventListener(this)
        mReaderView!!.setIReadPageChange(mReadPageChange)
        mReaderView!!.entrance()

        initGLSufaceView()
        initAutoReadView()
    }


    override fun changeAnimMode(mode: Int) {
        if(lastPageAnimation != ReadConfig.animation){
            if(lastPageAnimation == ReadViewEnums.Animation.list
                    || ReadConfig.animation == ReadViewEnums.Animation.list){
                entrance()
            }
        }

        lastPageAnimation = ReadConfig.animation

        if (ReadConfig.animation == ReadViewEnums.Animation.curl) {
            initGLSufaceView()
        } else {
            removeView(mTextureView)
            mTextureView?.alpha = 0f
            mTextureView?.let {
                ReadConfig.unregistObserver(it)
            }
        }

        mReaderView?.onAnimationChange(ReadConfig.animation)
    }

    override fun onPause() {
        mTextureView?.alpha = 0f
        mAutoReadView?.closeAutoRead()
    }

    private var mReadPageChange: IReadPageChange? = null

    /**
     * 设置 IReadView 实现 View 的变化监听
     * @param mReadPageChange 监听对象
     */
    override fun setIReadPageChange(readPageChange: IReadPageChange?){
        mReadPageChange = readPageChange
        mReaderView?.setIReadPageChange(mReadPageChange)
    }



    private var isDownActioned = false
    var eventList: ArrayList<MotionEvent> = arrayListOf()

    private var velocityTracker: VelocityTracker? = null

    override fun myDispatchTouchEvent(event: MotionEvent): Boolean {

        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }

        velocityTracker!!.addMovement(event)

//        if (ReadConfig.FULL_SCREEN_READ) {
//            if (event.action == MotionEvent.ACTION_DOWN) {
//                eventList.add(event)
//                AppLog.e("event", event.action.toString())
//                return true
//            }
//            if (eventList.isNotEmpty()) {
//                if (event.action == MotionEvent.ACTION_UP) {
//                    //执行操作
//                    val x = ReadConfig.screenWidth.minus(100).toFloat()
//                    val y = ReadConfig.screenHeight.div(2).toFloat()
//                    onCurlDown(x, y)
//                    onCurlUp(x, y)
//                    return true
//                } else if (event.action == MotionEvent.ACTION_CANCEL) {
//                    onCurlDown(eventList.last().x, eventList.last().y)
//                }
//            }
//            eventList.clear()
//        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> onCurlDown(event.x, event.y)
            MotionEvent.ACTION_MOVE -> onCurlMove(event.x, event.y)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> onCurlUp(event.x, event.y)
        }
        return true
    }

    private fun onCurlDown(x: Float, y: Float) {
        isDownActioned = true
        if (context is ReadingActivity) (context as ReadingActivity).showMenu(false)

        mTextureView?.onFingerDown(x, y)
    }

    private fun onCurlUp(x: Float, y: Float) {
        if (isDownActioned) {
            isDownActioned = false

//            if (mTextureView!!.alpha != 1.0f) {
//                //翻页显示
//                mTextureView!!.alpha = 1.0f
//            }

            velocityTracker?.computeCurrentVelocity(1000, mMaximumVelocity)
            mTextureView?.onFingerUp(x, y, velocityTracker!!.xVelocity)
            velocityTracker?.recycle()
            velocityTracker = null
        }
    }

    private fun onCurlMove(x: Float, y: Float) {

        if (isDownActioned) {

//            if (mTextureView!!.alpha != 1.0f) {
//                //翻页显示
//                mTextureView!!.alpha = 1.0f
//            }

            mTextureView?.onFingerMove(x, y)
        } else {
            isDownActioned = true
            if (context is ReadingActivity) (context as ReadingActivity).showMenu(false)

//            if (mTextureView!!.alpha != 1.0f) {
//                //翻页显示
//                mTextureView!!.alpha = 1.0f
//            }
            mTextureView?.onFingerDown(x, y)
        }
    }

    private var mOnAutoReadCallback: OnAutoReadCallback? = null

    fun setOnAutoReadCallback(onAutoReadCallback: OnAutoReadCallback) {
        this.mOnAutoReadCallback = onAutoReadCallback
    }

    fun setAutoReadSpeed(autoReadSpeed: Double) {
        mAutoReadSpeed = autoReadSpeed
        mAutoReadView?.setAutoReadSpeed(mAutoReadSpeed)
    }

    interface OnAutoReadCallback {
        fun onAutoReadResume()
        fun onAutoReadStop()
    }
}