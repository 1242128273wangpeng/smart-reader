package com.dingyue.downloadmanager

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import com.orhanobut.logger.Logger

/**
 * Desc 可以禁止ViewPager滑动的类
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/6/21 14:33
 */
class CustomViewPager : ViewPager {
    private var removeAble: Boolean = true

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context) : super(context)

    override fun onInterceptTouchEvent(motionEvent: MotionEvent): Boolean {
        return removeAble && super.onInterceptTouchEvent(motionEvent)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        return !removeAble || super.onTouchEvent(motionEvent)
    }

    fun insertRemoveAble(removeAble: Boolean) {
        this.removeAble = removeAble
        Logger.e("changeRemoveState: $removeAble")
    }
}