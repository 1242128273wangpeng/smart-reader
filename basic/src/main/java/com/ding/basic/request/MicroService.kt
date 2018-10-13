package com.ding.basic.request

import com.ding.basic.bean.*
import io.reactivex.Flowable
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface MicroService {

    companion object {
        //鉴权
        const val AUTH_ACCESS = "/auth/access/getKeys"

        //封面
        const val COVER_DETAIL = "/union/book/cover"

        //目录
        const val BOOK_CATALOG = "/union/book/chapter"

        //更新
        const val CHECK_UPDATE = "/union/book/check"

        //书架每天一次批量更新接口
        const val COVER_BATCH="/union/book/coverBatch"

        //获得缓存方式和package 列表
        const val DOWN_TASK_CONFIG = "/union/book/down"

        // 兴趣列表
        const val INTEREST_LIST = "/union/bookrack/label"

        //默认书架
        const val DEFAULT_BOOK = "/union/bookrack/labelCover"

    }

    @GET(AUTH_ACCESS)
    fun requestAuthAccess(): Flowable<BasicResult<String>>?

    @GET(AUTH_ACCESS)
    fun requestAuthAccessSync(): Call<BasicResult<String>>

    @GET(COVER_DETAIL)
    fun requestBookDetail(@Query("book_id") book_id: String, @Query("book_source_id") book_source_id: String): Flowable<BasicResult<Book>>

    @GET(COVER_DETAIL)
    fun requestBookDetail(@Query("book_id") book_id: String, @Query("book_source_id") book_source_id: String, @Query("book_chapter_id") book_chapter_id: String): Flowable<BasicResult<Book>>


    @GET(BOOK_CATALOG)
    fun requestBookCatalog(@Query("book_id") book_id: String, @Query("book_source_id") book_source_id: String): Flowable<BasicResult<Catalog>>

    @GET(BOOK_CATALOG)
    fun requestBookCatalog(@Query("book_id") book_id: String, @Query("book_source_id") book_source_id: String, @Query("book_chapter_id") book_chapter_id: String): Flowable<BasicResult<Catalog>>


    @POST(CHECK_UPDATE)
    @Headers("Content-Type: application/json;charset=UTF-8")
    fun requestBookUpdate(@Body json: RequestBody): Flowable<BasicResult<UpdateBean>>


    @POST(COVER_BATCH)
    @Headers("Content-Type: application/json;charset=UTF-8")
    fun requestCoverBatch(@Body json: RequestBody): Flowable<BasicResult<List<Book>>>

    /************************************* 缓存相关 *************************************/
    @GET(DOWN_TASK_CONFIG)
    fun requestDownTaskConfig(@Query(value = "bookId") str: String, @Query(value = "bookSourceId") str2: String, @Query(value = "type") i: Int, @Query(value = "chapterId") str3: String): Flowable<BasicResult<CacheTaskConfig>>

    @GET(INTEREST_LIST)
    fun getInterestList(): Flowable<BasicResult<InterestDto>>

    @GET(DEFAULT_BOOK)
    fun requestDefaultBooks(@Query("labelOne") first: String, @Query("labelTwo") second: String): Flowable<BasicResult<CoverList>>
}