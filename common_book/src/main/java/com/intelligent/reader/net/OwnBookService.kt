package com.intelligent.reader.net

import io.reactivex.Observable
import net.lzbook.kit.data.recommend.CoverRecommendBean
import net.lzbook.kit.encrypt.URLBuilderIntterface
import retrofit2.http.*

/**
 * Created by xian on 2017/8/19.
 */
interface OwnBookService {
    @GET(URLBuilderIntterface.CHAPTER_LIST)
    fun requestOwnCatalogList(@Path("book_id") book_id: String, @Path("book_source_id") book_source_id: String): Observable<String>

    @GET(URLBuilderIntterface.COVER)
    fun requestBookCover(@Path("book_id") book_id: String, @Path("book_source_id") book_source_id: String): Observable<String>


    @FormUrlEncoded
    @POST(URLBuilderIntterface.GET_COVER_RECOMMEND)
    fun requestCoverRecommend(@Path("book_id") book_id: String, @Field("recommanded") bookIds: String): Observable<CoverRecommendBean>



}