package com.ding.basic.request

import com.ding.basic.bean.*
import com.google.gson.JsonObject
import io.reactivex.Flowable
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface RequestService {

    companion object {

        //应用更新
        const val CHECK_APPLICATION = "/v3/app/check"

        //动态参数
        const val DYNAMIC_PARAMETERS = "/v3/dynamic/dynamicParameter"

        //默认书架
        const val DEFAULT_BOOK = "/v5/book/default"

        //书籍详情
        const val BOOK_DETAIL = "/v5/book/cover"

        //章节列表
        const val CHAPTER_LIST = "/v5/book/chapter"

        //来源列表
        const val SOURCE_LIST = "/v5/book/source"

        //自动补全
        const val AUTO_COMPLETE = "/v3/search/autoComplete"

        //自动补全V4接口
        const val AUTO_COMPLETE_V4 = "/v4/search/autoComplete"

        //自动补全V5接口
        const val AUTO_COMPLETE_V5 = "/v5/search/autoComplete"


        //搜索热词
        const val HOT_WORDS = "/v3/search/hotWords"

        // V5搜索
        const val SEARCH_V5 = "/v5/search/page"

        //新版搜索热词
        const val HOT_WORDS_V4 = "/v4/search/hotWords"

        //新版搜索推荐
        const val SEARCH_RECOMMEND_V5 = "/v5/search/autoOperations"

        //完结页推荐
        const val RECOMMEND_FINISH = "/v4/recommend/{book_id}/readPage"

        //书架推荐
        const val RECOMMEND_SHELF = "/v4/recommend/shelfPage"

        //封面页推荐
        const val RECOMMEND_COVER = "/v4/recommend/{book_id}/coverPage"

        //检查更新
        const val CHECK_UPDATE = "/v5/book/check"

        //书架更新
        const val BOOKSHELF_UPDATE = "/v5/book/update"

        const val FEEDBACK_ERROR = "/v3/log/fb"

        //登陆操作
        const val LOGIN_ACTION = "/v3/user/login"

        //登出操作
        const val LOGOUT_ACTION = "/v3/user/logout"

        //刷新Token
        const val REFRESH_TOKEN = "/v3/user/refLToken"

        //获得缓存方式和package 列表
        const val DOWN_TASK_CONFIG = "/v5/book/down"

        // cdn智能
        const val DYNAMIC_ZN = "https://public.lsread.cn/dpzn/{packageName}.json"

        // cdn传媒
        const val DYNAMIC_CM = "https://public.dingyueads.com/dpzn/{packageName}.json"

        // cdn原创
        const val DYNAMIC_YC = "https://public.qingoo.cn/dpzn/{packageName}.json"



        //书籍封面页推荐
        const val COVER_RECOMMEND = "/v4/recommend/{book_id}/coverPage"

        const val BOOK_RECOMMEND = "/v5/search/recommend"

    }

    @GET(DEFAULT_BOOK)
    fun requestDefaultBooks(): Flowable<BasicResult<CoverList>>

    @GET(CHECK_APPLICATION)
    fun requestApplicationUpdate(@QueryMap parameters: Map<String, String>): Flowable<JsonObject>

    @GET(DYNAMIC_PARAMETERS)
    fun requestDynamicParameters(): Flowable<JsonObject>

    @GET
    fun requestCDNDynamicPar(@Url url: String): Flowable<JsonObject>

    @GET(BOOK_DETAIL)
    fun requestBookDetail(@Query("book_id") book_id: String, @Query("book_source_id") book_source_id: String): Flowable<BasicResult<Book>>

    @GET(BOOK_DETAIL)
    fun requestBookDetail(@Query("book_id") book_id: String, @Query("book_source_id") book_source_id: String, @Query("book_chapter_id") book_chapter_id: String): Flowable<BasicResult<Book>>

    @GET(SOURCE_LIST)
    fun requestBookSources(@Query("book_id") book_id: String, @Query("book_source_id") book_source_id: String, @Query("book_chapter_id") book_chapter_id: String): Flowable<BasicResult<BookSource>>

    @GET(SOURCE_LIST)
    fun requestBookSources(@Query("book_id") book_id: String, @Query("book_source_id") book_source_id: String): Flowable<BasicResult<BookSource>>

    @GET(AUTO_COMPLETE)
    fun requestAutoComplete(@Query("word") word: String): Flowable<SearchAutoCompleteBean>

    @GET(AUTO_COMPLETE_V4)
    fun requestAutoCompleteV4(@Query("keyword") word: String): Flowable<SearchAutoCompleteBeanYouHua>


    @GET(AUTO_COMPLETE_V5)
    fun requestAutoCompleteV5(@Query("keyword") word: String): Flowable<SearchAutoCompleteBeanYouHua>


    @GET(HOT_WORDS_V4)
    fun requestHotWordV4(): Flowable<Result<SearchResult>>

    @GET(SEARCH_RECOMMEND_V5)
    fun requestSearchRecommend(@Query("shelfBooks") shelfBooks: String): Flowable<SearchRecommendBook>

    @GET(HOT_WORDS)
    fun requestHotWords(): Flowable<SearchHotBean>

    @POST(CHECK_UPDATE)
    @Headers("Content-Type: application/json;charset=UTF-8")
    fun requestBookUpdate(@Body json: RequestBody): Flowable<BasicResult<UpdateBean>>


    @POST(BOOKSHELF_UPDATE)
    @Headers("Content-Type: application/json;charset=UTF-8")
    fun requestBookShelfUpdate(@Body json: RequestBody): Flowable<BasicResult<CoverList>>

    @GET(FEEDBACK_ERROR)
    fun requestFeedback(@QueryMap(encoded = false) params: Map<String, String>): Flowable<NoBodyEntity>


    /************************************* 用户相关 *************************************/

    @GET(LOGIN_ACTION)
    fun requestLoginAction(@QueryMap(encoded = false) parameters: Map<String, String>): Flowable<LoginResp>

    @GET(LOGOUT_ACTION)
    fun requestLogoutAction(@QueryMap(encoded = false) parameters: Map<String, String>): Flowable<JsonObject>

    @GET(REFRESH_TOKEN)
    fun requestRefreshToken(@QueryMap(encoded = false) parameters: Map<String, String>): Flowable<RefreshResp>

    @GET("https://graph.qq.com/user/get_simple_userinfo")
    fun requestUserInformation(@Query("access_token") token: String, @Query("oauth_consumer_key") appid: String, @Query("openid") openid: String): Flowable<QQSimpleInfo>


    /************************************* 缓存相关 *************************************/
    @GET(DOWN_TASK_CONFIG)
    fun requestDownTaskConfig(@Query(value = "bookId") str: String, @Query(value = "bookSourceId") str2: String, @Query(value = "type") i: Int, @Query(value = "chapterId") str3: String): Flowable<BasicResult<CacheTaskConfig>>



    @FormUrlEncoded
    @POST(COVER_RECOMMEND)
    fun requestCoverRecommend(@Path("book_id") book_id: String, @Field("recommanded") bookIds: String): Flowable<CoverRecommendBean>


    @GET(BOOK_RECOMMEND)
    fun requestBookRecommend(@Query("bookId") book_id: String, @Query("shelfBooks") shelfBooks: String): Flowable<CommonResult<RecommendBooks>>
}