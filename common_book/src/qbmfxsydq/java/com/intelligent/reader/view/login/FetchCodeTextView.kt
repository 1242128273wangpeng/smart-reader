package com.intelligent.reader.view.login

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.TextView
import com.intelligent.reader.R
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


/**
 * Desc 验证码 EditText
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/29 0029 10:41
 */
class FetchCodeTextView : TextView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val enabledColor = Color.parseColor("#1DBFBB")
    private val disabledColor = Color.parseColor("#C4C4C4")

    private var countManager: Disposable? = null

    private var isCountDown = false

    fun startCountdown() {
        val count: Long = 60
        countManager?.dispose()
        countManager = Flowable.interval(0, 1, TimeUnit.SECONDS)
                .take(count + 1)
                .map { long -> count - long }
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    isEnabled = false
                    isCountDown = true
                    val time = context.getString(R.string.login_re_fetch) + it + "s"
                    text = time
                }, onComplete = {
                    isCountDown = false
                    isEnabled = true
                    text = context.getString(R.string.login_fetch_verify_code)
                })
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (isCountDown) return
        if (enabled) {
            setTextColor(enabledColor)
        } else {
            setTextColor(disabledColor)
        }
    }

    fun stopCountDown() {
        countManager?.dispose()
    }

}

