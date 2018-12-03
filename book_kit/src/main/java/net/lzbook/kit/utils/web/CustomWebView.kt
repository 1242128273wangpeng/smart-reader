package net.lzbook.kit.utils.web

import android.content.Context
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.webkit.WebView


/**
 * Desc：
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/10/26 0026 11:14
 */
class CustomWebView @kotlin.jvm.JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : WebView(context, attrs) {

    private var prohibitSlideAreaList = mutableListOf<RectF>()

    private var scrollChangeListener: ScrollChangeListener? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setWebContentsDebuggingEnabled(true)
        }
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        scrollChangeListener?.onScrollChanged(l, t, oldl, oldt)
    }

    fun insertScrollChangeListener(scrollChangeListener: ScrollChangeListener?) {
        this.scrollChangeListener = scrollChangeListener
    }

    interface ScrollChangeListener {
        fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                for (rectF in prohibitSlideAreaList) {
                    if (rectF.contains(x, scrollY + y)) {
                        requestDisallowInterceptTouchEvent(true)
                    }
                }
            }
        }

        return super.dispatchTouchEvent(event)
    }

    fun insertProhibitSlideArea(rectF: RectF) {
        prohibitSlideAreaList.add(rectF)
    }
}
