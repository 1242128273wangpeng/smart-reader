package com.ding.basic.repository

import com.ding.basic.bean.*
import com.ding.basic.request.RequestAPI
import com.google.gson.JsonObject
import io.reactivex.Flowable
import okhttp3.RequestBody
import retrofit2.Call
import java.util.ArrayList

/**
 * Created on 2018/3/19.
 * Created by crazylei.
 */

interface BasicRequestRepository {

    fun requestDefaultBooks(): Flowable<BasicResult<CoverList>> ?

    fun requestApplicationUpdate(parameters: Map<String, String>): Flowable<JsonObject>?

    fun requestDynamicParameters(): Flowable<JsonObject>?

    fun requestAutoComplete(word: String): Flowable<SearchAutoCompleteBean>?

    fun requestAutoCompleteV4(word: String): Flowable<SearchAutoCompleteBeanYouHua>? //搜索V4接口

    fun requestAutoCompleteV5(word: String): Flowable<SearchAutoCompleteBeanYouHua>? //搜索V5

    fun requestHotWords(): Flowable<SearchHotBean>?

    fun requestSearchRecommend(bookIds: String): Flowable<SearchRecommendBook>? //搜索推荐

    fun requestHotWordsV4(): Flowable<Result<SearchResult>>?

    fun requestChapterContent(chapter: Chapter): Flowable<BasicResult<Chapter>>

    fun requestChapterContentSync(chapter_id: String, book_id: String, book_source_id: String, book_chapter_id: String): Call<BasicResult<Chapter>>?

    fun requestBookUpdate(requestBody: RequestBody): Flowable<BasicResult<UpdateBean>>?

    fun requestBookShelfUpdate(requestBody: RequestBody): Flowable<BasicResult<CoverList>>?

    fun requestFeedback(parameters: Map<String, String>): Flowable<NoBodyEntity>?

    fun requestCoverRecommend(book_id: String, recommend: String): Flowable<CoverRecommendBean>?


    fun requestAuthAccess(): Flowable<BasicResult<String>>?

    fun requestBookDetail(book_id: String, book_source_id: String, book_chapter_id: String): Flowable<BasicResult<Book>>?

    fun requestBookCatalog(book_id: String, book_source_id: String, book_chapter_id: String): Flowable<BasicResult<Catalog>>

    fun requestBookSources(book_id: String, book_source_id: String, book_chapter_id: String): Flowable<BasicResult<BookSource>>?

    fun requestCoverBatch(requestBody: RequestBody): Flowable<BasicResult<List<Book>>>? //书籍批量接口


    fun requestLoginAction(parameters: Map<String, String>): Flowable<LoginResp>?

    fun requestLogoutAction(parameters: Map<String, String>): Flowable<JsonObject>?

    fun requestRefreshToken(parameters: Map<String, String>): Flowable<RefreshResp>?

    fun requestUserInformation(token: String, appid: String, openid: String): Flowable<QQSimpleInfo>?

    fun requestDownTaskConfig(bookID: String, bookSourceID: String
                              , type: Int, startChapterID: String): Flowable<BasicResult<CacheTaskConfig>>?


    fun requestBookRecommend(book_id: String, shelfBooks: String): Flowable<CommonResult<RecommendBooks>>?
    fun requestBookRecommendV4(book_id: String, recommend: String): Flowable<RecommendBooksEndResp>?

    fun requestAuthorOtherBookRecommend(author: String,book_id: String): Flowable<CommonResult<ArrayList<RecommendBean>>>?



}