package com.ding.basic.request

import com.ding.basic.Config
import com.ding.basic.bean.*
import io.reactivex.Flowable
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.properties.Delegates

/**
 * Created on 2018/3/13.
 * Created by crazylei.
 */
internal object MicroAPI {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().addNetworkInterceptor(MicroRequestInterceptor()).build()

    private var microService: MicroService by Delegates.notNull()

    init {
        initMicroService()
    }

    private fun initMicroService() {
        val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .baseUrl(Config.loadMicroAPIHost())
                .build()

        microService = retrofit.create(MicroService::class.java)
    }

    fun requestAuthAccess(): Flowable<BasicResult<String>>? {
        return microService.requestAuthAccess()
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
}