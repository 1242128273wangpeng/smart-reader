package net.lzbook.kit.net.custom.service

import com.google.gson.JsonObject

import net.lzbook.kit.data.NoBodyEntity
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.CoverPage
import net.lzbook.kit.data.bean.RequestItem
import net.lzbook.kit.data.recommend.CoverRecommendBean
import net.lzbook.kit.data.search.SearchAutoCompleteBean
import net.lzbook.kit.data.search.SearchHotBean
import net.lzbook.kit.data.update.UpdateBean
import net.lzbook.kit.encrypt.URLBuilderIntterface
import net.lzbook.kit.purchase.ChapterPriceInfo
import net.lzbook.kit.purchase.MutiPurchaseDialogBean
import net.lzbook.kit.purchase.PurchaseResult
import net.lzbook.kit.purchase.SingleChapterBean

import io.reactivex.Observable
import net.lzbook.kit.data.bean.SourceItem
import net.lzbook.kit.user.bean.*
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import retrofit2.http.RequestTag
import retrofit2.http.Url


/**
 * Created by xian on 2017/6/21.
 */

interface UserService {

    @FormUrlEncoded
    @POST(PATH_RECOMMEND_SHELF)
    abstract fun getShelfRecommendBook(@Field("recommanded") recommanded: String): Observable<RecommendBooksResp>

    @FormUrlEncoded
    @POST(PATH_RECOMMEND_BOOK_END)
    abstract fun getBookEndRecommendBook(@Field("recommanded") recommanded: String, @Path("book_id") bookId: String): Observable<RecommendBooksEndResp>

    @GET(BOOK_SOURCE_SINGLE)
    abstract fun getBookSource(@Path("book_id") bookId: String): Observable<SourceItem>

    @GET(URLBuilderIntterface.CHAPTER_LIST)
    fun requestOwnCatalogList(@Path("book_id") book_id: String, @Path("book_source_id") book_source_id: String): Observable<String>

    @GET(URLBuilderIntterface.COVER)
    fun requestBookCover(@Path("book_id") book_id: String, @Path("book_source_id") book_source_id: String): Observable<String>


    @FormUrlEncoded
    @POST(URLBuilderIntterface.GET_COVER_RECOMMEND)
    fun requestCoverRecommend(@Path("book_id") book_id: String, @Field("recommanded") bookIds: String): Observable<CoverRecommendBean>

    @get:GET(SEARCH_HOT)
    val hotWord: Observable<SearchHotBean>


    @GET(SEARCH_AUTO_COMPLETE)
    fun searchAutoComplete(@Query("word") word: String): Observable<SearchAutoCompleteBean>

    /**
     * 用户第三方登录请求接口
     */
    @GET(PATH_LOGIN)
    fun login(@QueryMap(encoded = false) params: Map<String, String>): Observable<LoginResp>

    /**

     * @param token
     * *
     * @param appid
     * *
     * @param openid
     * *
     * @return
     */
    @GET("https://graph.qq.com/user/get_simple_userinfo")
    fun getSimpleUserInfo(@Query("access_token") token: String,
                          @Query("oauth_consumer_key") appid: String,
                          @Query("openid") openid: String): Observable<QQSimpleInfo>

    /**
     * 用户token刷新接口
     * @param params
     * *
     * @return
     */
    @GET(PATH_REFRESH)
    fun refreshToken(@QueryMap(encoded = false) params: Map<String, String>): Observable<RefreshResp>

    /**
     * 用户退出登录接口
     * @param params
     * *
     * @return
     */
    @GET(PATH_LOGOUT)
    fun logout(@QueryMap(encoded = false) params: Map<String, String>): Observable<JsonObject>

    //    /**
    //     * SettingActivity页面获取用户剩余金币数量接口
    //     * @param uid
    //     * @return
    //     */
    //    @GET(PATH_COIN)
    //    Observable<GetCoinBean> getCoin(@Query("uid") String uid);
    //
    //    /**
    //     * 用户充值成功后根据订单信息向后台校验是否支付成功接口
    //     * @param orderNum
    //     * @return
    //     */
    //    @GET(PATH_CHECKOUT)
    //    Observable<CheckStateBean> checkState(@Query("orderNum") String orderNum);
    //
    //    /**
    //     * 阅读页充值dialog页面拉取充值信息接口
    //     * @return
    //     */
    //    @GET(PATN_PAYINFO)
    //    Observable<PayInfoBean> getPayInfo();
    //
    //    /**
    //     * 获取支付订单信息
    //     * @param price
    //     * @param coins
    //     * @param uid
    //     * @param payWay
    //     * @param packageName
    //     * @param facilityId
    //     * @return
    //     */
    //    @GET(PATH_GETORDER)
    //    Observable<WxOrderBean> getWxOrder(@Query("price") int price,
    //                                       @Query("coins") int coins,
    //                                       @Query("uid") int uid,
    //                                       @Query("payWay") int payWay,
    //                                       @Query("packageName") String packageName,
    //                                       @Query("facilityId") String facilityId);
    //
    //    /**
    //     * 获取支付订单信息
    //     * @param price
    //     * @param coins
    //     * @param uid
    //     * @param payWay
    //     * @param packageName
    //     * @param facilityId
    //     * @return
    //     */
    //    @GET(PATH_GETORDER)
    //    Observable<ALiOrderBean> getALiOrder(@Query("price") int price,
    //                                         @Query("coins") int coins,
    //                                         @Query("uid") int uid,
    //                                         @Query("payWay") int payWay,
    //                                         @Query("packageName") String packageName,
    //                                         @Query("facilityId") String facilityId);

    /**
     * 书籍封面页信息拉取接口
     */
    @GET(COVER + "/{book_id}/{book_source_id}/cover")
    fun getCoverDetail(@Path("book_id") bookId: String,
                       @Path("book_source_id") book_source_id: String): Observable<CoverPage>


    @get:GET(DEFAULT_BOOK)
    val defaultBook: Observable<JsonObject>

    /**
     * 目录页面章节信息拉取
     */
    @GET(COVER + "/{book_id}/{book_source_id}/chapter")
    fun getChapterList(@Path("book_id") bookId: String, @Path("book_source_id") book_source_id: String, @RequestTag book: RequestItem): Observable<List<Chapter>>

    /**
     * 单章购买请求
     */
    @GET(PURCHASE_SINGLE)
    fun requestSingleChapter(@Query("bookSourceId") sourceId: String, @Query("chapterId") chapterId: String,
                             @Query("chapterName") chapterName: String, @Query("uid") uid: String): Observable<SingleChapterBean>

    /**
     * 多章购买请求
     */
    @GET(PURCHASE_CHAPTERS)
    fun purchaseChapters(@QueryMap purchaseData: Map<String, String>): Observable<PurchaseResult>

    /**
     * 根据要购买的章节数获取总价格和折扣
     */
    @GET(GETPRICEBYNUMBER)
    fun parseChaptersPriceInfo(@QueryMap(encoded = false) params: Map<String, String>): Observable<ChapterPriceInfo>

    /**
     * 批量购买
     */
    @GET(MUTIPURCHASE_DIALOG)
    fun requestPurchaseDialogInfo(@QueryMap(encoded = false) params: Map<String, String>): Observable<MutiPurchaseDialogBean>

    /**
     * 用户反馈报错
     */
    @GET(CHAPTER_ERROR_FEEDBACK)
    fun sendFeedBack(@QueryMap(encoded = false) params: Map<String, String>): Observable<NoBodyEntity>

    @FormUrlEncoded
    @POST(BOOK_CHECK)
    fun getUpdatedZnBooks(@FieldMap params: Map<String, String>): Observable<UpdateBean>

    @GET(APP_CHECK)
    fun checkAppUpdate(@QueryMap params: Map<String, String>): Observable<JsonObject>

    @get:GET(DYNAMIC_PARAMAS)
    val dynamicParams: Observable<JsonObject>

    @GET
    fun getChapterContent(@Url string: String, @RequestTag chapter: Chapter): Observable<Chapter>

    companion object {

        const val SEARCH_HOT = "/v3/search/hotWords"
        const val SEARCH_AUTO_COMPLETE = "/v3/search/autoComplete"

        const val PATH_LOGIN = "/v3/user/login"
        const val PATH_REFRESH = "/v3/user/refLToken"
        const val PATH_LOGOUT = "/v3/user/logout"
        val PATH_COIN = "/v3/pay/getCoin"
        val PATH_CHECKOUT = "/v3/account/charge/checkPayStatus"
        val PATN_PAYINFO = "/v4/pay/getPayInfo"
        val PATH_GETORDER = "/v3/account/charge/genOrder"
        const val COVER = "v3/book"
        //错误章节反馈
        const val CHAPTER_ERROR_FEEDBACK = "/v3/log/fb"
        //批量购买
        const val MUTIPURCHASE_DIALOG = "/v3/pay/getPayChapterBreak"
        //根据要购买的章节数获取总价格和折扣
        const val GETPRICEBYNUMBER = "/v3/pay/byChapterNumGetMoney"
        //多章购买发起
        const val PURCHASE_CHAPTERS = "/v3/pay/moreChapterBuy"
        //单张购买发起
        const val PURCHASE_SINGLE = "/v3/pay/oneChapterBuy"

        /**
         * 用户第一次安装，请求默认书籍的接口
         */
        const val DEFAULT_BOOK = "/v3/book/default"

        // 更新书架dex值和书本的连载完结状态
        val UPDATE_SHELF_BOOKS = "/v3/book/covers"

        // APP版本检查更新
        const val APP_CHECK = "/v3/app/check"
        // 书籍更新检查
        const val BOOK_CHECK = "/v4/book/check"
        //动态参数接口
        const val DYNAMIC_PARAMAS = "/v3/dynamic/dynamicParameter"
        //单张请求接口
        val getChapterContent = "/v3/book/chaptersContents"

        const val PATH_RECOMMEND_SHELF = "/v4/recommend/shelfPage"
        const val PATH_RECOMMEND_BOOK_END = "/v4/recommend/{book_id}/readPage"
        /**
         * 换源集合
         */
        const val BOOK_SOURCE_SINGLE = "/v3/book/source/{book_id}/single"
        val REQUESR_SUCCESS = "20000"
    }


}
