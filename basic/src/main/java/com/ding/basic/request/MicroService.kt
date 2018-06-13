package com.ding.basic.request

import com.ding.basic.bean.*
import io.reactivex.Flowable
import okhttp3.RequestBody
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
        const val CHECK_UPDATE = "/v5/book/check"
    }

    @GET(AUTH_ACCESS)
    fun requestAuthAccess(): Flowable<BasicResult<String>>


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
}