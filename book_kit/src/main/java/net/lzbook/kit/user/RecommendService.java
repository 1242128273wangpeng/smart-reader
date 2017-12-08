package net.lzbook.kit.user;


import net.lzbook.kit.data.bean.SourceItem;
import net.lzbook.kit.user.bean.RecommendBooksEndResp;
import net.lzbook.kit.user.bean.RecommendBooksResp;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * 项目名称：11m
 * 类描述：
 * 创建人：Zach
 * 创建时间：2017/11/2 0002
 */

public interface RecommendService {
    String PATH_RECOMMEND_SHELF = "/v4/recommend/shelfPage";
    String PATH_RECOMMEND_BOOK_END = "/v4/recommend/{book_id}/readPage";
    /**
     * 换源集合
     */
    String BOOK_SOURCE_SINGLE = "/v3/book/source/{book_id}/single";
    public final String REQUESR_SUCCESS = "20000";

    @FormUrlEncoded
    @POST(PATH_RECOMMEND_SHELF)
    Observable<RecommendBooksResp> getShelfRecommendBook(@Field("recommanded") String recommanded);

    @FormUrlEncoded
    @POST(PATH_RECOMMEND_BOOK_END)
    Observable<RecommendBooksEndResp> getBookEndRecommendBook(@Field("recommanded") String recommanded, @Path("book_id") String bookId);

    @GET(BOOK_SOURCE_SINGLE)
    Observable<SourceItem> getBookSource(@Path("book_id") String bookId);
}
