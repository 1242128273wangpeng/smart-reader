package com.ding.basic.net

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
        Logger.e("网络请求异常: " + throwable.toString() )
        throwable.printStackTrace()
        requestError(throwable.message ?:"")
    }

    abstract fun requestResult(result: T?)

    abstract fun requestError(message: String)

    open fun requestComplete() {
        Logger.v("接口请求完成！")
    }
}