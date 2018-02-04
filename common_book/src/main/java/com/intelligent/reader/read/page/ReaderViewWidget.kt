package com.intelligent.reader.read.page

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import com.intelligent.reader.activity.ReadingActivity
import com.intelligent.reader.flip.PageFlipView
import com.intelligent.reader.flip.base.PageFlipState
import com.intelligent.reader.flip.render.SinglePageRender
import com.intelligent.reader.read.factory.ReaderViewFactory
import com.intelligent.reader.read.help.HorizontalEvent
import com.intelligent.reader.read.help.IReadPageChange
import com.intelligent.reader.read.help.IReadView
import com.intelligent.reader.read.help.IReadWidget
import com.intelligent.reader.read.mode.ReadState
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

    private val mReaderViewFactory by lazy {
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


    private var mPageFlipStateListener = object : SinglePageRender.PageFlipStateListener {

        private fun invisibelSurface(): Boolean {
            post {
                mTextureView?.canFlip = true
            }
            //等待ViewPager切换完页面再隐藏
            if (mReaderView != null && mReaderView is HorizontalReaderView) {
                val curView = (mReaderView as HorizontalReaderView).findViewWithTag(ReadViewEnums.PageIndex.current) as HorizontalPage?
                mTextureView?.alpha = 0F

                return curView?.hasAd == true
            } else {
                return false
            }
        }

        override fun backward(): Boolean {
            if (mReaderView != null && mReaderView is HorizontalReaderView) {
                (mReaderView as HorizontalReaderView).onClickLeft(false)
            }

            return invisibelSurface()
        }

        override fun forward(): Boolean {
            if (mReaderView != null && mReaderView is HorizontalReaderView) {
                (mReaderView as HorizontalReaderView).onClickRight(false)
            }
            return invisibelSurface()
        }

        override fun restore(): Boolean = invisibelSurface()
    }

    private val mMaximumVelocity by lazy {
        ViewConfiguration.get(context).scaledMaximumFlingVelocity.toFloat()
    }

    private val mTouchSlop by lazy {
        ViewConfiguration.get(context).scaledPagingTouchSlop
    }

    /**
     * 初始化GLSufaceView
     */
    private fun initGLSufaceView() {

        if (lastPageAnimation == ReadViewEnums.Animation.curl) {
            if (mTextureView == null) {
                mTextureView = PageFlipView(context)
            }

            if (mTextureView!!.parent == null) {
                mTextureView?.alpha = 0f
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
                    if (mReaderView != null && mReaderView is HorizontalReaderView) {
                        (mReaderView as HorizontalReaderView).onClickRight(false)
                    }
                }

                override fun loadBitmap(index: ReadViewEnums.PageIndex): Bitmap? {
                    if (mReaderView != null && mReaderView is HorizontalReaderView) {
                        val view = (mReaderView as HorizontalReaderView).findViewWithTag(index) as HorizontalPage?
                        if (view?.hasAd == true) {
                            view.destroyDrawingCache()
                        }
                        return view?.drawingCache
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


        if (mReaderView != null) {
            removeView(mReaderView as View)
        }
        if (mTextureView != null) {
            removeView(mTextureView)
        }
        if (mAutoReadView != null) {
            removeView(mAutoReadView)
        }


        lastPageAnimation = ReadConfig.animation//记录动画模式

        mReaderView = mReaderViewFactory.getView(ReadConfig.animation)//创建

        (mReaderView as View).isClickable = false

        addView(mReaderView as View)//添加

        mReaderView?.setHorizontalEventListener(this)
        mReaderView?.setIReadPageChange(mReadPageChange)
        mReaderView?.entrance()

        initGLSufaceView()
        initAutoReadView()
    }


    override fun changeAnimMode(mode: Int) {
        if (lastPageAnimation != ReadConfig.animation) {
            if (lastPageAnimation == ReadViewEnums.Animation.list
                    || ReadConfig.animation == ReadViewEnums.Animation.list) {
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
            mTextureView = null
        }

        mReaderView?.onAnimationChange(ReadConfig.animation)
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {

        if (mAutoReadView?.isAutoRead() == true) {
            return false
        }

        if (mTextureView != null && mTextureView?.visibility == View.VISIBLE) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    flipUp(event)
                    return true
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    flipDown(event)
                    return true
                }
            }
            return false
        }

        return mReaderView?.onKeyEvent(event) ?: false
    }

    private fun flipUp(event: KeyEvent) {
        if (event.action == KeyEvent.ACTION_UP) {
            if (onCurlDown(10f, ReadConfig.screenHeight.toFloat() - 10)) {
                onCurlUp(10f, ReadConfig.screenHeight.toFloat() - 10)
            }
        }
    }

    private fun flipDown(event: KeyEvent) {
        if (event.action == KeyEvent.ACTION_UP) {
            if (onCurlDown(ReadConfig.screenWidth.toFloat() - 10, ReadConfig.screenHeight.toFloat() - 10)) {
                onCurlUp(ReadConfig.screenWidth.toFloat() - 10, ReadConfig.screenHeight.toFloat() - 10)
            }
        }
    }

    override fun onResume() {
        mTextureView?.onResume()
    }

    override fun onPause() {
        mTextureView?.onPause()
        mTextureView?.alpha = 0f
        mAutoReadView?.closeAutoRead()
    }

    override fun onDestroy() {
        mReaderView = null
    }

    private var mReadPageChange: IReadPageChange? = null

    /**
     * 设置 IReadView 实现 View 的变化监听
     * @param mReadPageChange 监听对象
     */
    override fun setIReadPageChange(readPageChange: IReadPageChange?) {
        mReadPageChange = readPageChange
        mReaderView?.setIReadPageChange(mReadPageChange)
    }


    private var isDownActioned = false
    private var shouldGiveUpAction = false


    private var velocityTracker: VelocityTracker? = null

    override fun myDispatchTouchEvent(event: MotionEvent): Boolean {

        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }

        velocityTracker!!.addMovement(event)

        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> onCurlDown(event.x, event.y)
            MotionEvent.ACTION_MOVE -> onCurlMove(event.x, event.y)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> onCurlUp(event.x, event.y)
            else -> {
                false
            }
        }
    }

    private val downPointF = PointF()

    private var orientationLimit: ReadViewEnums.ScrollLimitOrientation? = null

    private fun onCurlDown(x: Float, y: Float): Boolean {
        if (mTextureView?.canFlip == true) {
            isDownActioned = true

            orientationLimit = (findViewWithTag(ReadViewEnums.PageIndex.current) as HorizontalPage?)?.orientationLimit

            downPointF.x = x
            downPointF.y = y

//        setFlipCurrentAsFirstTexture()

            mTextureView?.onFingerDown(x, y)

            return true
        } else {
            return false
        }
    }

    private fun onCurlUp(x: Float, y: Float): Boolean {

        if (!shouldGiveUpAction && isDownActioned) {
            isDownActioned = false

            do {
                if (mTextureView!!.getmPageRender().mPageFlip.flipState == PageFlipState.BEGIN_FLIP) {
                    if (x < width / 2 && !ReadConfig.FULL_SCREEN_READ) {
                        if (ReadViewEnums.ScrollLimitOrientation.LEFT == orientationLimit) {
                            break
                        }
                        //left
                        if (!flipPreviousPage()) {
                            Log.e("ReaderWidget", "cant fillPreTexture")
                            break
                        }
                    } else {
                        if (ReadViewEnums.ScrollLimitOrientation.RIGHT == orientationLimit) {
                            mReadPageChange?.goToBookOver()//跳bookend
                            break
                        }
                        //right
                        if (!flipNextPage()) {
                            Log.e("ReaderWidget", "cant fillNextTexture")
                            break
                        }
                    }
                }

                velocityTracker?.computeCurrentVelocity(1000, mMaximumVelocity)
                mTextureView?.onFingerUp(x, y, velocityTracker?.xVelocity ?: 0f)
            } while (false)
        }

        velocityTracker?.recycle()
        velocityTracker = null
        shouldGiveUpAction = false
        return true
    }

    private fun onCurlMove(x: Float, y: Float): Boolean {

        if (isDownActioned) {

            if (Math.abs(downPointF.x - x) >= mTouchSlop) {
                var filledTexture = true
                //perpare texture
                if (downPointF.x - x < 0) {
                    if (ReadViewEnums.ScrollLimitOrientation.LEFT == orientationLimit) {
                        shouldGiveUpAction = true
                        return false
                    } else {
                        //left
                        filledTexture = flipPreviousPage()
                    }
                } else {
                    if (ReadViewEnums.ScrollLimitOrientation.RIGHT == orientationLimit) {
                        shouldGiveUpAction = true
                        mReadPageChange?.goToBookOver()//跳bookend
                        return false
                    } else {
                        //right
                        filledTexture = flipNextPage()
                    }
                }

                if (filledTexture) {
                    mTextureView?.onFingerMove(x, y)
                } else {
                    shouldGiveUpAction = true
                    Log.e("ReaderWidget", "cant filledTexture")
                }
            }
        } else {
            onCurlDown(x, y)
        }

        return true
    }

    private fun flipPreviousPage(): Boolean {
        var flag = true
        synchronized(mTextureView as Any) {
            if (!mTextureView!!.hasFirstTexture()) {
                mTextureView!!.firstTexture = (mReaderView as HorizontalReaderView).findViewWithTag(ReadViewEnums.PageIndex.previous).drawingCache
                flag = mTextureView!!.firstTexture != null
            }
            if (flag && !mTextureView!!.hasSecondTexture()) {
                mTextureView!!.secondTexture = (mReaderView as HorizontalReaderView).findViewWithTag(ReadViewEnums.PageIndex.current).drawingCache
                flag = mTextureView!!.secondTexture != null
            }
        }

        return flag
    }

    private fun flipNextPage(): Boolean {
        var flag = true
        if (mReaderView == null) {
            return flag
        }
        synchronized(mTextureView as Any) {
            if (!mTextureView!!.hasFirstTexture()) {
                mTextureView!!.firstTexture = (mReaderView as HorizontalReaderView).findViewWithTag(ReadViewEnums.PageIndex.current).drawingCache
                flag = mTextureView!!.firstTexture != null
            }
            if (flag && !mTextureView!!.hasSecondTexture()) {
                mTextureView!!.secondTexture = (mReaderView as HorizontalReaderView).findViewWithTag(ReadViewEnums.PageIndex.next).drawingCache
                flag = mTextureView!!.secondTexture != null
            }
        }

        return flag
    }

//    private fun setFlipCurrentAsFirstTexture() {
//        if (!mTextureView!!.getmPageRender().mPageFlip.firstPage.isFirstTextureSet) {
//            val horizontalPage = (mReaderView as HorizontalReaderView).findViewWithTag(ReadViewEnums.PageIndex.current) as HorizontalPage
//            if (horizontalPage.hasAd || horizontalPage.hasBigAd) {
//                horizontalPage.destroyDrawingCache()
//            }
//            val current = horizontalPage.drawingCache
//            mTextureView!!.queueEvent {
//                mTextureView!!.getmPageRender().mPageFlip.firstPage.setFirstTexture(current)
//            }
//        }
//    }

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