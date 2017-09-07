package com.intelligent.reader.net

import io.reactivex.Observable
import net.xxx.yyy.go.spider.URLBuilderIntterface
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by xian on 2017/8/19.
 */
interface OwnBookService {
    @GET(URLBuilderIntterface.CHAPTER_LIST)
    fun requestOwnCatalogList(@Path("book_id") book_id: String, @Path("book_source_id") book_source_id: String): Observable<String>
}