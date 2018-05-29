package com.ding.basic.repository

import com.ding.basic.bean.*
import com.google.gson.JsonObject
import io.reactivex.Flowable
import okhttp3.RequestBody
import retrofit2.Call

/**
 * Created on 2018/3/19.
 * Created by crazylei.
 */

interface BasicRequestRepository {

    fun requestDefaultBooks(): Flowable<BasicResult<CoverList>> ?

    fun requestApplicationUpdate(parameters: Map<String, String>): Flowable<JsonObject>?

    fun requestDynamicParameters(): Flowable<JsonObject>?

    fun requestBookDetail(book_id: String, book_source_id: String, book_chapter_id: String): Flowable<BasicResult<Book>>?

    fun requestCatalog(book_id: String, book_source_id: String, book_chapter_id: String): Flowable<BasicResult<Catalog>>

    fun requestBookSources(book_id: String, book_source_id: String, book_chapter_id: String): Flowable<BasicResult<BookSource>>?

    fun requestAutoComplete(word: String): Flowable<SearchAutoCompleteBean>?

    fun requestHotWords(): Flowable<SearchHotBean>?

    fun requestChapterContent(chapter: Chapter): Flowable<BasicResult<Chapter>>

    fun requestChapterContentSync(chapter_id: String, book_id: String, book_source_id: String, book_chapter_id: String): Call<BasicResult<Chapter>>?

    fun requestBookUpdate(requestBody: RequestBody): Flowable<BasicResult<UpdateBean>>?

    fun requestBookShelfUpdate(requestBody: RequestBody): Flowable<BasicResult<CoverList>>?

    fun requestFeedback(parameters: Map<String, String>): Flowable<NoBodyEntity>?





    fun requestLoginAction(parameters: Map<String, String>): Flowable<LoginResp>?

    fun requestLogoutAction(parameters: Map<String, String>): Flowable<JsonObject>?

    fun requestRefreshToken(parameters: Map<String, String>): Flowable<RefreshResp>?

    fun requestUserInformation(token: String, appid: String, openid: String): Flowable<QQSimpleInfo>?

    fun requestDownTaskConfig(bookID: String, bookSourceID: String
                              , type: Int, startChapterID: String): Flowable<BasicResult<CacheTaskConfig>>?








}