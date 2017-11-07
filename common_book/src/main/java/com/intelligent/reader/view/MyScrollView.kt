package com.intelligent.reader.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView
import java.util.jar.Attributes

/**
 * Created by Administrator on 2017\10\30 0030.
 */
class MyScrollView : ScrollView {
    private var scrollChangedListener: ScrollChangedListener? = null

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (this.scrollChangedListener != null) {
            scrollChangedListener!!.onScrollChanged(t, oldt)
        }

    }

    fun setScrollChangedListener(scrollChangedListener: ScrollChangedListener) {
        this.scrollChangedListener = scrollChangedListener
    }

    interface ScrollChangedListener {
        fun onScrollChanged(top: Int, oldTop: Int)
    }
}