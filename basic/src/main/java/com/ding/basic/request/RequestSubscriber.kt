package com.ding.basic.request

import android.content.Context
import com.ding.basic.repository.RequestRepositoryFactory
import com.orhanobut.logger.Logger
import io.reactivex.subscribers.ResourceSubscriber

/**
 * Created on 2018/3/15.
 * Created by crazylei.
 */
abstract class RequestSubscriber<T>: ResourceSubscriber<T>() {

    override fun onNext(t: T?) {
        requestResult(t)
    }

    override fun onComplete() {
        requestComplete()
    }

    override fun onError(throwable: Throwable) {
        throwable.printStackTrace()
        Logger.v("网络请求异常: " + throwable.toString() )
        requestError(throwable.message ?:"")
    }

    abstract fun requestResult(result: T?)

    abstract fun requestError(message: String)

    open fun requestRetry()  {

    }

    open fun requestComplete() {

    }

    fun requestAuthAccess(context: Context) {
        RequestRepositoryFactory.loadRequestRepositoryFactory(context = context).requestAuthAccess(object : RequestSubscriber<String>() {

            override fun requestResult(result: String?) {

            }

            override fun requestError(message: String) {

            }
        })
    }
}