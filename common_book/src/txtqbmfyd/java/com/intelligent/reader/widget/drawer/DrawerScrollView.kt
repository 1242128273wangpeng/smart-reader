package com.intelligent.reader.widget.drawer

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.ScrollView
import net.lzbook.kit.utils.logger.AppLog

/**
 * Desc 请描述这个文件
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/2/26 0026 17:55
 */
class DrawerScrollView(context: Context, attrs: AttributeSet) : ScrollView(context, attrs) {
    private val tag = "DrawerScrollView"

    private var lastPointX = 0f
    private var lastPointY = 0f

    private var isVerticalSlide = false
    private var isHorizontalSlide = false

    private val touchSlop = ViewConfiguration.get(context).scaledPagingTouchSlop

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        AppLog.i(tag, "dispatchTouchEvent")
        if (isHorizontalSlide) {
            return false
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        AppLog.i(tag, "onTouchEvent, isVerticalSlide: $isVerticalSlide , isHorizontalSlide: $isHorizontalSlide")
        val action = event?.action?.and(MotionEvent.ACTION_MASK)
        val actionIndex = event?.actionIndex
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                AppLog.i(tag, "onTouchEvent, ACTION_DOWN")
                lastPointX = event.x
                lastPointY = event.y
                isVerticalSlide = false
                isHorizontalSlide = false
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                AppLog.i(tag, "onTouchEvent, ACTION_POINTER_DOWN")
                if (isVerticalSlide) {  //有第二个手势事件加入，而且正在滑动事件中，则直接消费事件
                    return super.onTouchEvent(event)
                }
                if (isHorizontalSlide) {
                    return false
                }
            }
            MotionEvent.ACTION_MOVE -> {
                AppLog.i(tag, "onTouchEvent, ACTION_MOVE")
                if (actionIndex != 0) {
                    if (isVerticalSlide) {
                        return super.onTouchEvent(event)
                    }
                    if (isHorizontalSlide) {
                        return false
                    }
                }
                if (isHorizontalSlide) {
                    AppLog.i(tag, "return false")
                    return false
                }
                val curPointX = event.x
                val curPointY = event.y

                //横坐标位移增量
                val deltaX = Math.abs(curPointX - lastPointX)
                //纵坐标位移增量
                val deltaY = Math.abs(curPointY - lastPointY)
                AppLog.e(tag, "deltaX: $deltaX  deltaY: $deltaY")
                if (!isVerticalSlide && deltaX > touchSlop && deltaY * 1.5f < deltaX) {//如果水平方向滑动，则返回
                    isHorizontalSlide = true
                    return false
                }

                if (!isVerticalSlide) {
                    if (deltaY < touchSlop) {
                        return false
                    } else {
                        isVerticalSlide = true
                    }
                }
            }
            else -> {
                isVerticalSlide = false
                isHorizontalSlide = false
            }
        }
        return super.onTouchEvent(event)
    }
}