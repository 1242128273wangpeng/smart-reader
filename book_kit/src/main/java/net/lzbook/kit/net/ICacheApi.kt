package net.lzbook.kit.net

import io.reactivex.Observable
import net.lzbook.kit.data.bean.CacheTaskConfig
import net.lzbook.kit.data.bean.Chapter
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.RequestTag

/**
 * Created by Danny on 2017/12/17.
 */
interface ICacheApi {
    @GET(value = "/v4/book/down")
    fun getTaskConfig(@Query(value = "bookId") str: String, @Query(value = "bookSourceId") str2: String, @Query(value = "type") i: Int, @Query(value = "chapterId") str3: String): Observable<NetResult<CacheTaskConfig>>

    @GET(value = "/v3/book/{book_id}/{book_source_id}/chapter")
    fun getChapterList(@Path(value = "book_id") str: String, @Path(value = "book_source_id") str2: String, @RequestTag book: Book): Observable<List<Chapter>>
}