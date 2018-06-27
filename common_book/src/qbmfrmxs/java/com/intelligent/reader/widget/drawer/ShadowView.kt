package com.intelligent.reader.widget.drawer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import net.lzbook.kit.utils.logd

/**
 * Desc 滑动阴影
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2017/11/20
 */

class ShadowView(context: Context) : View(context) {

    private val paint = Paint()

    private var alpha = "00"

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        logd("alpha: $alpha")
        paint.color = Color.parseColor("#${alpha}000000")
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, top.toFloat(), width.toFloat(), bottom.toFloat(), paint)
    }

    fun move(left: Int, right: Int, showing: Float) {
        val hex = 102 - Math.round(showing * 102) // 最大值为 40% 的透明度
        alpha = if (hex < 16) {
            "0" + Integer.toHexString(hex)
        } else {
            Integer.toHexString(hex)
        }
        layout(left, top, right, bottom)
    }
}
