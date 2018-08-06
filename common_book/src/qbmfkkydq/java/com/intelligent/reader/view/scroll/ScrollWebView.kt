package com.intelligent.reader.view.scroll

import android.content.Context
import android.graphics.RectF
import android.webkit.WebView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import com.intelligent.reader.fragment.RecommendFragment

/**
 * Date: 2018/7/23 14:38
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 滑动WebView ，判断是否禁止父布局拦截滑动事件
 */
class ScrollWebView @kotlin.jvm.JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : WebView(context, attrs) {

    val Tag = "ScrollWebView"
    private var mLastX: Int = 0
    private var mLastY: Int = 0
    private var mScrollView: ViewGroup? = null
    private var mViewPager: ViewGroup? = null
    private var bannerRect: RectF? = null

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        var x = ev.x.toInt()
        var y = ev.y.toInt()

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                mScrollView?.requestDisallowInterceptTouchEvent(true)
                mViewPager?.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = x - mLastX
                val deltaY = y - mLastY

                if (Math.abs(deltaY) < Math.abs(deltaX)) {// 左右滑动
                    if (bannerRect != null && bannerRect!!.contains(ev.getX(), ev.getY() - scrollY)) {
                        mViewPager?.requestDisallowInterceptTouchEvent(true)
                    } else {
                        mViewPager?.requestDisallowInterceptTouchEvent(false)
                    }

                } else { // 上下滑动
                    if (RecommendFragment.canScroll || scrollY == 0) {// ScrollView 可以滑动，或者WebView已经滑动顶部，不拦截事件
                        mScrollView?.requestDisallowInterceptTouchEvent(false)
                    } else {
                        mScrollView?.requestDisallowInterceptTouchEvent(true)
                    }
                }


            }

        }

        mLastX = x
        mLastY = y

        return super.dispatchTouchEvent(ev)
    }

    fun setScrollViewGroup(viewGroup: ViewGroup) {
        mScrollView = viewGroup;
    }

    fun setViewPagerViewGroup(viewGroup: ViewGroup) {
        mViewPager = viewGroup
    }


    fun setBannerRect(rect: RectF) {
        bannerRect = rect;
    }


}

