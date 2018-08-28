package com.intelligent.reader.view

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * Function：自定义RecycleView，解决滑动冲突
 *
 * Created by JoannChen on 2018/7/16 0016 22:43
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
class MyRecyclerView : RecyclerView {
    var isSupport: Boolean = true

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {

        isSupport = false

        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {

            }
            MotionEvent.ACTION_MOVE -> {
                isSupport = false
            }

            else -> isSupport = true
        }
        return super.dispatchTouchEvent(ev)
    }

}