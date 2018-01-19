package com.intelligent.reader.read.page

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Message
import android.os.PowerManager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.intelligent.reader.read.mode.ReadState
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.ReadViewEnums

/**
 * @desc 自动阅读视图
 * @author lijun Lee
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/1/18 14:42
 */
class AutoReadView : View {

    private val TAG = this.javaClass.simpleName

    private var mOnAutoReadViewLoadCallback: OnAutoReadViewLoadCallback? = null

    private var mReadPosition: Int = 0

    private lateinit var mAutoReadHandler: AutoReadHandler

    private lateinit var mWakeLock: PowerManager.WakeLock

    private lateinit var mPaint: Paint

    private var mCurrentPageBitmap: Bitmap? = null

    private var mNextPageBitmap: Bitmap? = null

    private lateinit var mDividerBmp: Bitmap

    private lateinit var mSrcRect: Rect

    private lateinit var mDsfRect: Rect

    private var mWidth = 0

    private var mHeight = 0

    /**
     * message code
     */
    private val AUTO_READ_TIMER_CODE = 1

    /**
     * 阅读位置改变时间间隔
     */
    private val AUTO_READ_INTERVAL_MILLIS: Long = 50

    private var mIsAutoRead = false

    private var mInvalidatable = true

    /**
     * 阅读速度
     */
    private var mAutoReadSpeed: Double = 0.0

    constructor(context: Context?) : this(context, null) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        mAutoReadHandler = AutoReadHandler()
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG)

        mPaint = Paint()
        mPaint.isFilterBitmap = true
        mPaint.isDither = true

        mSrcRect = Rect()
        mDsfRect = Rect()

        if (Constants.MODE == 55 || Constants.MODE == 58) {
            mDividerBmp = BitmapFactory.decodeResource(resources, net.lzbook.kit.R.drawable.content_auto_read_night)
        } else {
            mDividerBmp = BitmapFactory.decodeResource(resources, net.lzbook.kit.R.drawable.content_auto_read_day)
        }
    }

    /**
     * 开始自动阅读
     */
    @SuppressLint("WakelockTimeout")
    fun startAutoRead() {
        mWakeLock.acquire()

        mOnAutoReadViewLoadCallback?.onStart()

        mNextPageBitmap = mOnAutoReadViewLoadCallback?.loadBitmap(ReadViewEnums.PageIndex.next)
        mCurrentPageBitmap = mOnAutoReadViewLoadCallback?.loadBitmap(ReadViewEnums.PageIndex.current)

        postAutoReadTimer()

        mIsAutoRead = true

        mInvalidatable = true

    }

    /**
     * 关闭自动阅读
     */
    fun closeAutoRead() {
        if (mWakeLock.isHeld) {
            mWakeLock.release()
        }

        mReadPosition = 0

        mIsAutoRead = false

        mInvalidatable = false

        mOnAutoReadViewLoadCallback?.onStop()
        mAutoReadHandler.removeMessages(AUTO_READ_TIMER_CODE)
    }

    /**
     * 恢复自动阅读
     */
    private fun resumeAutoRead() {
        mInvalidatable = true
        mOnAutoReadViewLoadCallback?.onResume()
        postAutoReadTimer()
    }

    /**
     * 暂停自动阅读
     */
    private fun pauseAutoRead() {
        mInvalidatable = false
        mOnAutoReadViewLoadCallback?.onPause()
        mAutoReadHandler.removeMessages(AUTO_READ_TIMER_CODE)
    }

    private fun postAutoReadTimer() {
        mAutoReadHandler.removeMessages(AUTO_READ_TIMER_CODE)
        mAutoReadHandler.sendEmptyMessageDelayed(AUTO_READ_TIMER_CODE, AUTO_READ_INTERVAL_MILLIS)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mReadPosition != 0) {
            mNextPageBitmap?.let {
                if (it.isRecycled) return
                canvas.drawBitmap(it, 0f, 0f, mPaint)
            }

            canvas.save()
            canvas.clipRect(0, mReadPosition, mWidth, mHeight)

            mCurrentPageBitmap?.let {
                if (it.isRecycled) return
                canvas.drawBitmap(it, 0f, 0f, mPaint)
            }

            if (!mDividerBmp.isRecycled) {
                mSrcRect.left = 0
                mSrcRect.right = mDividerBmp.width
                mSrcRect.top = 0
                mSrcRect.bottom = mDividerBmp.height
                mDsfRect.left = 0
                mDsfRect.right = mWidth
                mDsfRect.top = mReadPosition
                mDsfRect.bottom = mReadPosition + mDividerBmp.height
                canvas.drawBitmap(mDividerBmp, mSrcRect, mDsfRect, mPaint)
            }

            canvas.restore()
        } else {
            mNextPageBitmap?.let {
                if (it.isRecycled) return
                canvas.drawBitmap(it, 0f, 0f, mPaint)
            }

            mCurrentPageBitmap?.let {
                if (it.isRecycled) return
                canvas.drawBitmap(it, 0f, 0f, mPaint)
            }
        }
    }

    private var mStartTouchTime = System.currentTimeMillis()

    private var mStartEventY = 0

    private var mStartEventX = 0

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mStartEventY = event.y.toInt()
                mStartEventX = event.x.toInt()
            }
            MotionEvent.ACTION_CANCEL -> {
                mStartTouchTime = 0
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (mInvalidatable) {
                    mAutoReadHandler.changeReadPosition(event.y.toInt() - mStartEventY)
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                val touchTime = System.currentTimeMillis() - mStartTouchTime
                val distance = Math.sqrt(Math.pow((mStartEventX - event.x.toInt()).toDouble(), 2.0) + Math.pow((mStartEventY - event.y.toInt()).toDouble(), 2.0)).toInt()
                if (touchTime < 100 && distance < 30 || distance < 10) {
                    onAutoMenu()
                }
                mStartTouchTime = 0
                return true
            }
        }
        return isAutoRead()
    }

    private fun onAutoMenu() {
        if (mInvalidatable) {
            pauseAutoRead()
        } else {
            resumeAutoRead()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mDividerBmp.recycle()
    }

    fun setOnAutoReadViewLoadCallback(onAutoReadViewLoadCallback: OnAutoReadViewLoadCallback) {
        mOnAutoReadViewLoadCallback = onAutoReadViewLoadCallback
    }

    @SuppressLint("HandlerLeak")
    private inner class AutoReadHandler : Handler() {

        private var remainLen: Double = 0.toDouble()

        private val autoReadFrequency = 1000.0 / AUTO_READ_INTERVAL_MILLIS

        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            val stepLen = mHeight * mAutoReadSpeed / (10 * autoReadFrequency)
            remainLen += stepLen
            val moveIndex = remainLen.toInt()
            if (moveIndex > 0) {
                remainLen -= moveIndex.toDouble()
                if (!changeReadPosition(moveIndex)) {
                    closeAutoRead()
                    return
                }
            }
            postAutoReadTimer()
        }

        fun changeReadPosition(moveIndex: Int): Boolean {
            mReadPosition += moveIndex

            if (mReadPosition < 0) {
                mReadPosition = 0
            }
            if (mReadPosition >= mHeight) {
                if (!decideCanRun()) {
                    return false
                }
                post {
                    mNextPageBitmap = mOnAutoReadViewLoadCallback?.loadBitmap(ReadViewEnums.PageIndex.next)
                    mCurrentPageBitmap = mOnAutoReadViewLoadCallback?.loadBitmap(ReadViewEnums.PageIndex.current)
                }
            }
            postInvalidate()
            return true
        }

        private fun decideCanRun(): Boolean {
            if (ReadState.sequence + 1 == ReadState.chapterList.size - 1) {
                mOnAutoReadViewLoadCallback?.onStop()
                return false
            }
            mReadPosition = 0
            mOnAutoReadViewLoadCallback?.onNextPage()
            return true
        }
    }

    fun isAutoRead() = mIsAutoRead


    fun setAutoReadSpeed(autoReadSpeed: Double) {
        mAutoReadSpeed = autoReadSpeed
    }

    interface OnAutoReadViewLoadCallback {
        fun loadBitmap(index: ReadViewEnums.PageIndex): Bitmap?
        fun onStart()
        fun onResume()
        fun onStop()
        fun onPause()
        fun onNextPage()
    }
}