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
internal object ContentAPI {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().addNetworkInterceptor(MicroRequestInterceptor()).build()

    private var contentService: ContentService by Delegates.notNull()

    init {
        initMicroService()
    }

    private fun initMicroService() {
        val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .baseUrl(Config.loadContentAPIHost())
                .build()

        contentService = retrofit.create(ContentService::class.java)
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