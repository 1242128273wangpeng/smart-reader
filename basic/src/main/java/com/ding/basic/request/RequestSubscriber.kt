package com.ding.basic.request

import android.content.Context
import com.ding.basic.bean.Book
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
        Logger.e("网络请求异常: " + throwable.toString() )
        throwable.printStackTrace()
        requestError(throwable.message ?:"")
    }

    abstract fun requestResult(result: T?)

    abstract fun requestError(message: String)

    open fun requestRetry()  {

    }

    open fun requestComplete() {

    }

    @Synchronized fun requestAuthAccess(context: Context, requestSubscriber: RequestSubscriber<T>) {
        RequestRepositoryFactory.loadRequestRepositoryFactory(context = context).requestAuthAccess(object : RequestSubscriber<String>() {
            override fun requestResult(result: String?) {
                if (result != null && result.isNotEmpty()) {
                    requestSubscriber.requestRetry()
                } else {
                    requestSubscriber.requestError("获取鉴权异常！")
                }
            }

            override fun requestError(message: String) {
                requestSubscriber.requestError("获取鉴权失败！")
            }

            override fun requestComplete() {
                super.requestComplete()
                Logger.e("获取鉴权完成！")
            }
        })
    }
}