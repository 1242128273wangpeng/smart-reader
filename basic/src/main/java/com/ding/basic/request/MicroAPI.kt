package com.ding.basic.request

import com.ding.basic.Config
import com.ding.basic.bean.*
import com.orhanobut.logger.Logger
import io.reactivex.Flowable
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

/**
 * Created on 2018/3/13.
 * Created by crazylei.
 */
object MicroAPI {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().addNetworkInterceptor(MicroRequestInterceptor()).connectTimeout(3, TimeUnit.SECONDS).build()

    private var microService: MicroService by Delegates.notNull()

    init {
        initMicroService()
    }

    fun initMicroService() {
        val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .baseUrl(Config.loadMicroAPIHost())
                .build()

        Logger.e("InitMicroService: " + Config.loadMicroAPIHost())

        microService = retrofit.create(MicroService::class.java)
    }

    fun requestAuthAccess(): Flowable<BasicResult<String>>? {
        return microService.requestAuthAccess()
    }

    fun requestAuthAccessSync(): Call<BasicResult<String>> {
        return microService.requestAuthAccessSync()
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

    /**
     * 选择兴趣
     * @param firstType 一级分类
     * @param secondType 二级分类
     */
    fun requestDefaultBooks(firstType: String, secondType: String): Flowable<BasicResult<CoverList>>? {
        return microService.requestDefaultBooks(firstType, secondType)
    }

    /**
     * 获取兴趣列表
     */
    fun getInterestList(): Flowable<BasicResult<List<Interest>>>? {
        return microService.getInterestList()
    }
}