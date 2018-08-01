package com.intelligent.reader.fragment.scroll

import android.content.Context
import android.support.v4.widget.NestedScrollView
import android.util.AttributeSet
import android.view.MotionEvent
import android.webkit.WebView
import com.intelligent.reader.fragment.RecommendFragment

/**
 * Date: 2018/7/24 12:47
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 重写拦截事件，解决和WebView的事件冲突
 */
class ScrollNestedScrollView @kotlin.jvm.JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : NestedScrollView(context, attrs) {
    private var mLastXIntercept: Int = 0
    private var mLastYIntercept: Int = 0
    var currentFrag: ScrollWebFragment? = null

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        super.onInterceptTouchEvent(ev)
        var intercepted = false
        val x = ev.x.toInt()
        val y = ev.y.toInt()

        when (ev.action) {


            MotionEvent.ACTION_DOWN -> intercepted = false
            MotionEvent.ACTION_MOVE -> {
                val deltaX = x - mLastXIntercept
                val deltaY = y - mLastYIntercept
                if (Math.abs(deltaY) > Math.abs(deltaX) && RecommendFragment.canScroll) {
                    intercepted = true
                } else if (currentFrag != null && deltaY > 0 && Math.abs(deltaY) > Math.abs(deltaX) && (currentFrag!!.webScorllDistance == 0)) {
                    intercepted = true
                } else {
                    intercepted = false
                }
            }
            MotionEvent.ACTION_UP -> intercepted = false
        }
        mLastXIntercept = x
        mLastYIntercept = y
        return intercepted

    }

}