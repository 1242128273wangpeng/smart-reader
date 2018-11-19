package com.ding.basic.net.api

import com.ding.basic.bean.*
import com.ding.basic.net.api.service.MicroService
import com.ding.basic.net.interceptor.MicroRequestInterceptor
import com.ding.basic.net.rx.SchedulerHelper
import com.ding.basic.util.AESUtil
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.google.gson.Gson
import com.orhanobut.logger.Logger
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subscribers.ResourceSubscriber
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.antlr.v4.runtime.BailErrorStrategy
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.properties.Delegates

/**
 * Created on 2018/3/13.
 * Created by crazylei.
 */
object MicroAPI {

    /***
     * 微服务API接口
     * **/
    var microHost: String = ""
        get() {
            return if (field.isNotEmpty()) {
                field
            } else {
                val value = SPUtils.loadPrivateSharedString(SPKey.MICRO_AUTH_HOST)
                field = if (value.isNotEmpty()) {
                    value
                } else {
                    ""
                }
                field
            }
        }
        set(value) {
            if (value.isNotEmpty()) {
                field = value

                SPUtils.insertPrivateSharedString(SPKey.MICRO_AUTH_HOST, value)
            }
        }

    /***
     * 请求公钥
     * **/
    var publicKey: String? = null
        get() {
            return if (field?.isNotEmpty() == false) {
                field
            } else {
                val value = SPUtils.loadPrivateSharedString(SPKey.MICRO_AUTH_PUBLIC_KEY + microHost, "")

                field = if (value.isNotEmpty() == true) {
                    value
                } else {
                    ""
                }
                field
            }
        }
        set(value) {
            if (value?.isNotEmpty() == true) {
                field = value

                SPUtils.insertPrivateSharedString(SPKey.MICRO_AUTH_PUBLIC_KEY + microHost, value)
            }
        }

    /***
     * 请求私钥
     * **/
    var privateKey: String? = null
        get() {
            return if (field?.isNotEmpty() == true) {
                field
            } else {
                val value = SPUtils.loadPrivateSharedString(SPKey.MICRO_AUTH_PRIVATE_KEY + microHost)

                field = if (value.isNotEmpty() == true) {
                    value
                } else {
                    ""
                }
                field
            }
        }
        set(value) {
            if (value?.isNotEmpty() == true) {
                field = value

                SPUtils.insertPrivateSharedString(SPKey.MICRO_AUTH_PRIVATE_KEY + microHost, value)
            }
        }

    /***
     * 鉴权过期时间
     * **/
    var expire = 0L
        set(value) {
            if (value > 0) {
                field = System.currentTimeMillis() + (value * 1000)
            }
        }

    /***
     * 鉴权临时秘钥
     * **/
    var accessKey: String = "wangpeng12345678"

    private var microService: MicroService by Delegates.notNull()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().addInterceptor(MicroRequestInterceptor()).build()

    fun initMicroService() {

        publicKey = SPUtils.loadPrivateSharedString(SPKey.MICRO_AUTH_PUBLIC_KEY + microHost)

        privateKey = SPUtils.loadPrivateSharedString(SPKey.MICRO_AUTH_PRIVATE_KEY + microHost)

        val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .baseUrl(microHost)
                .build()

        microService = retrofit.create(MicroService::class.java)

        if (publicKey?.isEmpty() == true || privateKey?.isEmpty() == true) {
            requestAuthAccess()
        }
    }

    fun requestAuthAccess() {
        microService.requestAuthAccess().compose(SchedulerHelper.schedulerHelper<BasicResult<String>>())?.subscribeWith(object : ResourceSubscriber<BasicResult<String>>() {
            override fun onNext(result: BasicResult<String>?) {
                if (result != null && result.checkResultAvailable()) {
                    Logger.e("接口鉴权请求结果正常！")
                    val message = AESUtil.decrypt(result.data, accessKey)

                    if (message != null && message.isNotEmpty()) {
                        val access = Gson().fromJson(message, Access::class.java)
                        if (access != null) {
                            if (access.publicKey != null) {
                                publicKey = access.publicKey
                            }

                            if (access.privateKey != null) {
                                privateKey = access.privateKey
                            }

                            if (access.expire > 0) {
                                expire = access.expire.toLong()
                            }
                        }
                    }
                } else {
                    Logger.e("接口鉴权请求结果异常！")
                }
            }

            override fun onError(throwable: Throwable) {
                Logger.e("接口鉴权请求异常！")
            }

            override fun onComplete() {
                Logger.e("接口鉴权请求完成！")
            }
        })
    }

    fun requestAuthAccessSync(): Boolean {
        val result = microService.requestAuthAccessSync().execute().body()

        if (result != null && result.checkResultAvailable()) {
            Logger.e("接口鉴权请求结果正常！")
            val message = AESUtil.decrypt(result.data, accessKey)

            if (message != null && message.isNotEmpty()) {
                val access = Gson().fromJson(message, Access::class.java)
                if (access != null) {
                    if (access.publicKey != null) {
                        publicKey = access.publicKey
                    }

                    if (access.privateKey != null) {
                        privateKey = access.privateKey
                    }

                    if (access.expire > 0) {
                        expire = access.expire.toLong()
                    }
                }
            }
            return true
        } else {
            Logger.e("鉴权请求结果异常！")
            return false
        }
    }

    fun requestBookDetail(book_id: String, book_source_id: String, book_chapter_id: String): Flowable<BasicResult<Book>>? {
        if (book_chapter_id == "") {
            return microService.requestBookDetail(book_id, book_source_id)
        }
        return microService.requestBookDetail(book_id, book_source_id, book_chapter_id)
    }

    fun requestBookCatalog(book_id: String, book_source_id: String, book_chapter_id: String): Flowable<BasicResult<Catalog>> {
        if (book_chapter_id == "") {
            return microService.requestBookCatalog(book_id, book_source_id)
        }
        return microService.requestBookCatalog(book_id, book_source_id, book_chapter_id)
    }

    fun requestCoverBatch(requestBody: RequestBody):Flowable<BasicResult<List<Book>>>?{
        return microService.requestCoverBatch(requestBody)
    }

    fun requestBookUpdate(requestBody: RequestBody): Flowable<BasicResult<UpdateBean>>? {
        return microService.requestBookUpdate(requestBody)
    }

    fun requestDownTaskConfig(bookID: String, bookSourceID: String
                              , type: Int, startChapterID: String): Flowable<BasicResult<CacheTaskConfig>>? {
        return microService.requestDownTaskConfig(bookID, bookSourceID, type, startChapterID)
    }

    fun requestWebViewResult(url: String): Observable<String> {
        return microService.requestWebViewResult(url)
    }

    fun requestWebViewResult(url: String, requestBody: RequestBody): Observable<String> {
        return microService.requestWebViewResult(url, requestBody)
    }


    fun requestWebViewConfig(): Observable<BasicResult<String>> {
        return microService.requestWebViewConfig()
    }
}