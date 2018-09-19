package com.ding.basic.net.api.service

import com.ding.basic.bean.*
import io.reactivex.Flowable
import retrofit2.Call
import retrofit2.http.*

interface ContentService {

    companion object {
        //内容
        const val CHAPTER_CONTENT = "/content/book/content"
    }

    @GET(CHAPTER_CONTENT)
    fun requestChapterContent(@Query("chapter_id") chapter_id: String, @Query("book_id") book_id: String, @Query("book_source_id") book_source_id: String, @Query("book_chapter_id") book_chapter_id: String): Flowable<BasicResult<Chapter>>

    @GET(CHAPTER_CONTENT)
    fun requestChapterContent(@Query("chapter_id") chapter_id: String, @Query("book_id") book_id: String, @Query("book_source_id") book_source_id: String): Flowable<BasicResult<Chapter>>

    @GET(CHAPTER_CONTENT)
    fun requestChapterContentSync(@Query("chapter_id") chapter_id: String, @Query("book_id") book_id: String, @Query("book_source_id") book_source_id: String, @Query("book_chapter_id") book_chapter_id: String): Call<BasicResult<Chapter>>

    @GET(CHAPTER_CONTENT)
    fun requestChapterContentSync(@Query("chapter_id") chapter_id: String, @Query("book_id") book_id: String, @Query("book_source_id") book_source_id: String): Call<BasicResult<Chapter>>
}