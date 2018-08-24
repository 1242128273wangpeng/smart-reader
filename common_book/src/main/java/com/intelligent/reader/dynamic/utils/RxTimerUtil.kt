package com.intelligent.reader.dynamic.utils

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * Created by yuchao on 2018/8/23 0023.
 */
object RxTimerUtil {

    @JvmStatic
    var mDisposable: Disposable? = null


    /**
     * 每隔milliseconds毫秒后执行next操作
     *
     * @param milliseconds
     * @param next
     *
     */
    fun interval(milliseconds: Long, next: (num: Long) -> Unit) {
        Observable.interval(milliseconds, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .subscribe(object : Observer<Long> {
                    override fun onSubscribe(d: Disposable) {
                        mDisposable = d
                    }

                    override fun onNext(t: Long) {
                        next?.invoke(t)
                    }

                    override fun onError(e: Throwable) {
                    }

                    override fun onComplete() {
                    }

                })

    }

    fun cancel() {
        if (mDisposable?.isDisposed == false)
            mDisposable?.dispose()
    }

}