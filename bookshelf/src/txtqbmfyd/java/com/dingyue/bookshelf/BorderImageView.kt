package com.dingyue.bookshelf

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

    private var borderWidth = 0

    private val borderPaint: Paint = Paint()

    private val rect: Rect = Rect()

    private val maskPaint: Paint = Paint()

    init {
        val density = resources.displayMetrics.density
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
        }
    }
}