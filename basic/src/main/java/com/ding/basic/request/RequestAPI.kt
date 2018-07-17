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
import java.util.ArrayList
import kotlin.properties.Delegates

/**
 * Created on 2018/3/13.
 * Created by crazylei.
 */
object RequestAPI {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().addNetworkInterceptor(RequestInterceptor()).build()

    private var requestService: RequestService by Delegates.notNull()

    init {

        initializeDataRequestService()

        Logger.v("初始化OkHttpClient!")
    }

    fun initializeDataRequestService() {
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

    fun requestBookSources(book_id: String, book_source_id: String, book_chapter_id: String): Flowable<BasicResult<BookSource>>? {
        if (book_chapter_id == "") {
            return requestService.requestBookSources(book_id, book_source_id)
        }
        return requestService.requestBookSources(book_id, book_source_id, book_chapter_id)
    }

    fun requestAutoComplete(word: String): Flowable<SearchAutoCompleteBean>? {
        return requestService.requestAutoComplete(word)
    }
    fun requestAutoCompleteV4(word: String): Flowable<SearchAutoCompleteBeanYouHua>? {
        return requestService.requestAutoCompleteV4(word)
    }

    fun requestAutoCompleteV5(word: String): Flowable<SearchAutoCompleteBeanYouHua>? {
        return requestService.requestAutoCompleteV5(word)
    }

    fun requestHotWordsV4(): Flowable<Result<SearchResult>>{
        return requestService.requestHotWordV4()
    }


    fun requestSearchRecommend(bookIds: String): Flowable<SearchRecommendBook>{
        return requestService.requestSearchRecommend(bookIds)
    }

    fun requestHotWords(): Flowable<SearchHotBean>? {
        return requestService.requestHotWords()
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

    fun requestCoverRecommend(book_id: String, recommend: String): Flowable<CoverRecommendBean>? {
        return requestService.requestCoverRecommend(book_id, recommend)
    }

    fun requestBookRecommend(book_id: String, shelfBooks: String): Flowable<CommonResult<RecommendBooks>>? {
        return requestService.requestBookRecommend(book_id, shelfBooks)
    }

    fun requestAuthorOtherBookRecommend(author: String,book_id: String): Flowable<CommonResult<ArrayList<RecommendBean>>>? {
        return requestService.requestAuthorOtherBookRecommend(author,book_id)
    }

    fun requestBookRecommendV4(book_id: String, recommend: String): Flowable<RecommendBooksEndResp>? {
        return requestService.requestBookRecommendV4(book_id, recommend)
    }
}