package com.intelligent.reader.read.page

import android.content.Context
import android.text.format.DateFormat
import android.util.AttributeSet
import android.widget.TextView
import com.intelligent.reader.util.ThemeUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

/**
 *
 * Created by wt on 2018/1/15.
 */
class TimeTextView : TextView {

    private var period: Long = 30

    private var mCalendar = Calendar.getInstance()

    private var timeDispost: Disposable? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        init()
    }

    fun init (){
        if (mCalendar == null)  mCalendar = Calendar.getInstance()
        text = DateFormat.format("k:mm", mCalendar)
        timeDispost = Observable.interval(period, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mCalendar.timeInMillis = System.currentTimeMillis()
                    text = DateFormat.format("k:mm", mCalendar)
                }, {e-> e.printStackTrace()})
        setTextColor(resources.getColor(ThemeUtil.modePrimaryColor))
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        timeDispost?.dispose()
    }
}