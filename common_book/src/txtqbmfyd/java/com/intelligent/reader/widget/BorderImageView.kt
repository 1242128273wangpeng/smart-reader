package com.intelligent.reader.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.ImageView


/**
 * Desc 请描述这个文件
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/12 0012 09:33
 */
class BorderImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : ImageView(context, attrs, defStyleAttr) {

    private val DEFAULT_BORDER_WIDTH = 1

    private val DEFAULT_BORDER_COLOR: Int = 0xFFF4F5F7.toInt()

    private val MASK_COLOR_PRESSED: Int = 0x1A000000

    private var borderWidth = 0

    private val borderPaint: Paint = Paint()

    private val rect: Rect = Rect()

    private var isShowMask = false

    private val maskPaint: Paint = Paint()

    init {
        val density = resources.displayMetrics.density//屏幕密度
        borderWidth = (DEFAULT_BORDER_WIDTH * density + 0.5f).toInt()
        borderPaint.strokeWidth = borderWidth.toFloat()
        borderPaint.style = Paint.Style.STROKE
        borderPaint.color = DEFAULT_BORDER_COLOR

        maskPaint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            it.getClipBounds(rect)
            it.drawRect(rect, borderPaint)
//            if (isShowMask) {
//                maskPaint.color = MASK_COLOR_PRESSED
//            } else {
//                maskPaint.color = Color.TRANSPARENT
//            }
//            it.drawRect(rect, maskPaint)
        }
    }

//    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        val action = event?.action
//        when (action) {
//            MotionEvent.ACTION_DOWN -> {
//                showMask()
//            }
//            MotionEvent.ACTION_UP ->{
//                hideMask()
//            }
//        }
//        return false
//    }
//
//    private inner class ForegroundDrawable(d: Drawable?) : LayerDrawable(arrayOf(d)) {
//
//        override fun onStateChange(states: IntArray): Boolean {
//            AppLog.e("BorderImageView", "states")
//            var pressed = false
//
//            for (state in states) {
//                if (state == android.R.attr.state_pressed)
//                    pressed = true
//            }
//
//            mutate()
//            if (pressed) {
//                setBackgroundColor(MASK_COLOR_PRESSED)
//            } else {
//                setBackgroundColor(Color.TRANSPARENT)
//            }
//
//            invalidateSelf()
//
//            return super.onStateChange(states)
//        }
//
//        override fun isStateful(): Boolean {
//            return true
//        }
//    }
//
//    private fun hideMask() {
//        AppLog.e("BorderImageView", "hideMask")
//        isShowMask = false
//        invalidate()
//    }
//
//    private fun showMask() {
//        AppLog.e("BorderImageView", "showMask")
//        isShowMask = true
//        invalidate()
//    }
}