package com.dy.reader.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.RelativeLayout

class BlockTouchRelativeLayout : RelativeLayout {
    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    var canTouchCallbak: ((ev: MotionEvent?) -> Boolean)? = null

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return canTouchCallbak?.invoke(ev) ?: true && super.dispatchTouchEvent(ev)
    }
}
