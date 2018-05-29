package com.dy.reader.page

import android.content.Context
import android.text.format.DateFormat
import android.util.AttributeSet
import android.widget.TextView
import com.dy.reader.util.ThemeUtil
import java.util.*

/**
 *
 * Created by wt on 2018/1/15.
 */
class TimeTextView : TextView {

    private var mCalendar = Calendar.getInstance()

    private var isAttach = false

    inner class ChangeTimeRunnable: Runnable {
        override fun run() {
            mCalendar.timeInMillis = System.currentTimeMillis()
            text = DateFormat.format("k:mm", mCalendar)

//            destroyDrawingCache()
            if(isAttach) {
                postDelayed(changeTimeRunnable, 30000)
            }
        }
    }

    private val changeTimeRunnable = ChangeTimeRunnable()


    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onAttachedToWindow() {
        isAttach = true
        super.onAttachedToWindow()
        post {
            init()
        }
    }

    fun init (){
        if (mCalendar == null)  mCalendar = Calendar.getInstance()
        mCalendar.timeInMillis = System.currentTimeMillis()
        text = DateFormat.format("k:mm", mCalendar)

        setTextColor(ThemeUtil.getTitleColor(resources))

        postDelayed(changeTimeRunnable, 30000)
    }

    override fun onDetachedFromWindow() {
        isAttach = false
        super.onDetachedFromWindow()
        handler.removeCallbacks(changeTimeRunnable)
    }

}