package com.dingyue.contract.web

import android.content.Context
import android.graphics.RectF
import android.webkit.WebView
import android.util.AttributeSet
import android.view.MotionEvent

class CustomWebView @kotlin.jvm.JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : WebView(context, attrs) {

    private var bannerRectFList = mutableListOf<RectF>()

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                for (rectF in bannerRectFList) {
                    if (rectF.contains(x, scrollY + y)) {
                        requestDisallowInterceptTouchEvent(true)
                    }
                }
            }
        }

        return super.dispatchTouchEvent(event)
    }

    fun insertProhibitSlideArea(rectF: RectF) {
        bannerRectFList.add(rectF)
    }
}