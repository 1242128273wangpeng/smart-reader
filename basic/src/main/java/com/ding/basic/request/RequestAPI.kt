package com.ding.basic.request

import com.ding.basic.Config
import com.ding.basic.bean.*
import com.google.gson.JsonObject
import com.orhanobut.logger.Logger
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
internal object RequestAPI {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().addNetworkInterceptor(RequestInterceptor()).build()

    private var requestService: RequestService by Delegates.notNull()

    init {

        initializeDataRequestService()

        Logger.v("初始化OkHttpClient!")
    }

    private fun initializeDataRequestService() {
        val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .baseUrl(Config.loadRequestAPIHost()).build()

        requestService = retrofit.create(RequestService::class.java)
    }

    fun requestDefaultBooks(): Flowable<BasicResult<CoverList>>? {
        return requestService.requestDefaultBooks()
    }

    fun requestApplicationUpdate(parameters: Map<String, String>): Flowable<JsonObject>? {
        return requestService.requestApplicationUpdate(parameters)
    }

    fun requestDynamicParameters(): Flowable<JsonObject>? {
        return requestService.requestDynamicParameters()
    }

    fun requestCDNDynamicPar(url: String): Flowable<JsonObject> {
        return requestService.requestCDNDynamicPar(url)
    }

    fun requestBookDetail(book_id: String, book_source_id: String, book_chapter_id: String): Flowable<BasicResult<Book>>? {
        if (book_chapter_id == "") {
            return requestService.requestBookDetail(book_id, book_source_id)
        }
        return requestService.requestBookDetail(book_id, book_source_id, book_chapter_id)
    }

    fun requestCatalog(book_id: String, book_source_id: String, book_chapter_id: String): Flowable<BasicResult<Catalog>> {
        if (book_chapter_id == "") {
            return requestService.requestCatalog(book_id, book_source_id)
        }
        return requestService.requestCatalog(book_id, book_source_id, book_chapter_id)
    }

    fun requestBookSources(book_id: String, book_source_id: String, book_chapter_id: String): Flowable<BasicResult<BookSource>>? {
        if (book_chapter_id == "") {
            return requestService.requestBookSources(book_id, book_source_id)
        }
        return requestService.requestBookSources(book_id, book_source_id, book_chapter_id)
    }

    fun requestAutoComplete(word: String): Flowable<SearchAutoCompleteBean>? {
        return requestService.requestAutoComplete(word)
    }

    fun requestHotWords(): Flowable<SearchHotBean>? {
        return requestService.requestHotWords()
    }

    fun requestChapterContent(chapter_id: String, book_id: String, book_source_id: String, book_chapter_id: String): Flowable<BasicResult<Chapter>> {
        if (book_chapter_id == ""){
            return requestService.requestChapterContent(chapter_id, book_id, book_source_id)
        }
        return requestService.requestChapterContent(chapter_id, book_id, book_source_id, book_chapter_id)
    }

    fun requestChapterContentSync(chapter_id: String, book_id: String, book_source_id: String, book_chapter_id: String): Call<BasicResult<Chapter>> {
        if (book_chapter_id == ""){
            return requestService.requestChapterContentSync(chapter_id, book_id, book_source_id)
        }
        return requestService.requestChapterContentSync(chapter_id, book_id, book_source_id, book_chapter_id)
    }

    fun requestBookUpdate(requestBody: RequestBody): Flowable<BasicResult<UpdateBean>>? {
        return requestService.requestBookUpdate(requestBody)
    }

    fun requestBookShelfUpdate(requestBody: RequestBody): Flowable<BasicResult<CoverList>>? {
        return requestService.requestBookShelfUpdate(requestBody)
    }

    fun requestFeedback(parameters: Map<String, String>): Flowable<NoBodyEntity>? {
        return requestService.requestFeedback(parameters)
    }


    fun requestLoginAction(parameters: Map<String, String>): Flowable<LoginResp>? {
        return requestService.requestLoginAction(parameters)
    }

    fun requestLogoutAction(parameters: Map<String, String>): Flowable<JsonObject>? {
        return requestService.requestLogoutAction(parameters)
    }

    fun requestRefreshToken(parameters: Map<String, String>): Flowable<RefreshResp>? {
        return requestService.requestRefreshToken(parameters)
    }

    fun requestUserInformation(token: String, appid: String, openid: String): Flowable<QQSimpleInfo>? {
        return requestService.requestUserInformation(token, appid, openid)
    }


    fun requestDownTaskConfig(bookID: String, bookSourceID: String
                              , type: Int, startChapterID: String): Flowable<BasicResult<CacheTaskConfig>> {
        return requestService.requestDownTaskConfig(bookID, bookSourceID, type, startChapterID)
    }
}