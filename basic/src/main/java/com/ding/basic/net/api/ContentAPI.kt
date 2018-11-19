package com.ding.basic.net.api

import com.ding.basic.bean.*
import com.ding.basic.net.api.service.ContentService
import com.ding.basic.net.interceptor.ContentInterceptor
import com.ding.basic.net.rx.SchedulerHelper
import com.ding.basic.util.AESUtil
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.google.gson.Gson
import com.orhanobut.logger.Logger
import io.reactivex.Flowable
import io.reactivex.subscribers.ResourceSubscriber
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.properties.Delegates

/**
 * Created on 2018/3/13.
 * Created by crazylei.
 */
object ContentAPI {

    /***
     * 微服务API接口
     * **/
    var contentHost: String = ""
        get() {
            return if (field.isNotEmpty()) {
                field
            } else {
                val value = SPUtils.loadPrivateSharedString(SPKey.CONTENT_AUTH_HOST)
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

                SPUtils.insertPrivateSharedString(SPKey.CONTENT_AUTH_HOST, value)
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
                val value = SPUtils.loadPrivateSharedString(SPKey.CONTENT_AUTH_PUBLIC_KEY + contentHost, "")

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

                SPUtils.insertPrivateSharedString(SPKey.CONTENT_AUTH_PUBLIC_KEY + contentHost, value)
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
                val value = SPUtils.loadPrivateSharedString(SPKey.CONTENT_AUTH_PRIVATE_KEY + contentHost)

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
                SPUtils.insertPrivateSharedString(SPKey.CONTENT_AUTH_PRIVATE_KEY + contentHost, value)
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

    private var contentService: ContentService by Delegates.notNull()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().addInterceptor(ContentInterceptor()).build()

    fun initContentService() {

        publicKey = SPUtils.loadPrivateSharedString(SPKey.CONTENT_AUTH_PUBLIC_KEY + contentHost)

        privateKey = SPUtils.loadPrivateSharedString(SPKey.CONTENT_AUTH_PRIVATE_KEY + contentHost)

        val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .baseUrl(contentHost)
                .build()

        contentService = retrofit.create(ContentService::class.java)

        if (publicKey?.isEmpty() == true || privateKey?.isEmpty() == true) {
            requestAuthAccess()
        }
    }

    fun requestAuthAccess() {
        contentService.requestAuthAccess().compose(SchedulerHelper.schedulerHelper<BasicResult<String>>())?.subscribeWith(object : ResourceSubscriber<BasicResult<String>>() {
            override fun onNext(result: BasicResult<String>?) {
                if (result != null && result.checkResultAvailable()) {
                    Logger.e("内容鉴权请求结果正常！")
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
                    Logger.e("内容鉴权请求结果异常！")
                }
            }

            override fun onError(throwable: Throwable) {
                Logger.e("内容鉴权请求异常！")
            }

            override fun onComplete() {
                Logger.e("内容鉴权请求完成！")
            }
        })
    }

    fun requestChapterContent(chapter_id: String, book_id: String, book_source_id: String, book_chapter_id: String): Flowable<BasicResult<Chapter>> {
        if (book_chapter_id == ""){
            return contentService.requestChapterContent(chapter_id, book_id, book_source_id)
        }
        return contentService.requestChapterContent(chapter_id, book_id, book_source_id, book_chapter_id)
    }

    fun requestChapterContentSync(chapter_id: String, book_id: String, book_source_id: String, book_chapter_id: String): Call<BasicResult<Chapter>> {
        if (book_chapter_id == ""){
            return contentService.requestChapterContentSync(chapter_id, book_id, book_source_id)
        }
        return contentService.requestChapterContentSync(chapter_id, book_id, book_source_id, book_chapter_id)
    }
}