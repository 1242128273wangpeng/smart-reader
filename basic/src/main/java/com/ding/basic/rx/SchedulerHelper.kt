package com.ding.basic.rx

import io.reactivex.FlowableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created on 2018/3/12.
 * Created by crazylei.
 */
object SchedulerHelper {

    const val Type_IO = 0x80
    const val Type_Main = 0x81
    const val Type_Default = 0x82

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

    fun <T> schedulerHelper(type: Int): FlowableTransformer<T, T> {
        return when (type) {
            Type_IO -> FlowableTransformer {
                it
                        .subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
            }
            Type_Main -> FlowableTransformer {
                it
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .unsubscribeOn(Schedulers.io())
            }
            else -> FlowableTransformer {
                it
            }
        }
    }
}