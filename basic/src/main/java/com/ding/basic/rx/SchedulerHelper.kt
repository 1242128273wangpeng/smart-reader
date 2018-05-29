package com.ding.basic.rx

import io.reactivex.FlowableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created on 2018/3/12.
 * Created by crazylei.
 */
object SchedulerHelper {

    fun <T> schedulerHelper(): FlowableTransformer<T, T> {
        return FlowableTransformer { observable ->
            observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
        }
    }

    fun <T> schedulerIOHelper(): FlowableTransformer<T, T> {
        return FlowableTransformer { observable ->
            observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
        }
    }
}