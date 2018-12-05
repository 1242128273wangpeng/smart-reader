package com.intelligent.reader.view.scroll

import android.content.Context
import android.graphics.RectF
import android.os.Build
import android.support.v4.view.MotionEventCompat
import android.support.v4.view.NestedScrollingChild
import android.support.v4.view.NestedScrollingChildHelper
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.webkit.WebView

/**
 * Date: 2018/7/23 14:38
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 滑动WebView ，判断是否禁止父布局拦截滑动事件
 */
class ScrollWebView @kotlin.jvm.JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : WebView(context, attrs), NestedScrollingChild {

    init {
        init()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setWebContentsDebuggingEnabled(true)
        }
    }


    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 如果按下位置在滑动banner区域内，则拦截本次事件
                prohibitSlideAreaList
                        .filter { it.contains(x, scrollY + y) }
                        .forEach { requestDisallowInterceptTouchEvent(true) }
            }
        }

        return super.dispatchTouchEvent(event)
    }


    /**
     * 滑动操作处理开始
     */
    private var mLastMotionY: Int = 0

    private val mScrollOffset = IntArray(2)
    private val mScrollConsumed = IntArray(2)

    private var mNestedYOffset: Int = 0

    private var mChildHelper: NestedScrollingChildHelper? = null


    private fun init() {
        mChildHelper = NestedScrollingChildHelper(this)
        isNestedScrollingEnabled = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var result = false

        val trackedEvent = MotionEvent.obtain(event)

        val action = MotionEventCompat.getActionMasked(event)

        if (action == MotionEvent.ACTION_DOWN) {
            mNestedYOffset = 0
        }

        val y = event.y.toInt()

        event.offsetLocation(0f, mNestedYOffset.toFloat())

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mLastMotionY = y
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
                result = super.onTouchEvent(event)
            }
            MotionEvent.ACTION_MOVE -> {
                var deltaY = mLastMotionY - y

                if (dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset)) {
                    deltaY -= mScrollConsumed[1]
                    trackedEvent.offsetLocation(0f, mScrollOffset[1].toFloat())
                    mNestedYOffset += mScrollOffset[1]
                }

                mLastMotionY = y - mScrollOffset[1]

                val oldY = scrollY
                val newScrollY = Math.max(0, oldY + deltaY)
                val dyConsumed = newScrollY - oldY
                val dyUnconsumed = deltaY - dyConsumed

                if (dispatchNestedScroll(0, dyConsumed, 0, dyUnconsumed, mScrollOffset)) {
                    mLastMotionY -= mScrollOffset[1]
                    trackedEvent.offsetLocation(0f, mScrollOffset[1].toFloat())
                    mNestedYOffset += mScrollOffset[1]
                }

                result = super.onTouchEvent(trackedEvent)
                trackedEvent.recycle()
            }
            MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                stopNestedScroll()
                result = super.onTouchEvent(event)
            }
        }
        return result
    }

    // NestedScrollingChild

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        mChildHelper!!.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return mChildHelper!!.isNestedScrollingEnabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return mChildHelper!!.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        mChildHelper!!.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return mChildHelper!!.hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?): Boolean {
        return mChildHelper!!.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow)
    }

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?): Boolean {
        return mChildHelper!!.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
    }

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return mChildHelper!!.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return mChildHelper!!.dispatchNestedPreFling(velocityX, velocityY)
    }


    /**
     * 滑动操作处理结束
     */


    /**
     * 滑动处理
     */

    private var prohibitSlideAreaList = mutableListOf<RectF>()
    private var scrollChangeListener: ScrollWebView.ScrollChangeListener? = null

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        scrollChangeListener?.onScrollChanged(l, t, oldl, oldt)
    }


    interface ScrollChangeListener {
        fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int)
    }

    fun insertProhibitSlideArea(rectF: RectF) {
        prohibitSlideAreaList.add(rectF)
    }

    fun insertScrollChangeListener(scrollChangeListener: ScrollWebView.ScrollChangeListener?) {
        this.scrollChangeListener = scrollChangeListener
    }

}


