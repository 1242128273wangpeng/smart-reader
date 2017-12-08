package net.lzbook.kit.net.custom.service;

import com.google.gson.JsonObject;

import net.lzbook.kit.data.NoBodyEntity;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.CoverPage;
import net.lzbook.kit.data.bean.RequestItem;
import net.lzbook.kit.data.update.UpdateBean;
import net.lzbook.kit.purchase.ChapterPriceInfo;
import net.lzbook.kit.purchase.MutiPurchaseDialogBean;
import net.lzbook.kit.purchase.PurchaseResult;
import net.lzbook.kit.purchase.SingleChapterBean;
import net.lzbook.kit.user.bean.LoginResp;
import net.lzbook.kit.user.bean.QQSimpleInfo;
import net.lzbook.kit.user.bean.RefreshResp;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.RequestTag;
import retrofit2.http.Url;


/**
 * Created by xian on 2017/6/21.
 */

public interface UserService {
    String PATH_LOGIN = "/v3/user/login";
    String PATH_REFRESH = "/v3/user/refLToken";
    String PATH_LOGOUT = "/v3/user/logout";
    String PATH_COIN = "/v3/pay/getCoin";
    String PATH_CHECKOUT = "/v3/account/charge/checkPayStatus";
    String PATN_PAYINFO = "/v4/pay/getPayInfo";
    String PATH_GETORDER = "/v3/account/charge/genOrder";
    String COVER = "v3/book";
    //错误章节反馈
    String CHAPTER_ERROR_FEEDBACK = "/v3/log/fb";
    //批量购买
    String MUTIPURCHASE_DIALOG = "/v3/pay/getPayChapterBreak";
    //根据要购买的章节数获取总价格和折扣
    String GETPRICEBYNUMBER = "/v3/pay/byChapterNumGetMoney";
    //多章购买发起
    String PURCHASE_CHAPTERS = "/v3/pay/moreChapterBuy";
    //单张购买发起
    String PURCHASE_SINGLE = "/v3/pay/oneChapterBuy";

    /**
     * 用户第一次安装，请求默认书籍的接口
     */
    String DEFAULT_BOOK = "/v3/book/default";

    // 更新书架dex值和书本的连载完结状态
    String UPDATE_SHELF_BOOKS = "/v3/book/covers";

    // APP版本检查更新
    String APP_CHECK = "/v3/app/check";
    // 书籍更新检查
    String BOOK_CHECK = "/v4/book/check";
    //动态参数接口
    String DYNAMIC_PARAMAS = "/v3/dynamic/dynamicParameter";
    //单张请求接口
    String getChapterContent = "/v3/book/chaptersContents";

    /**
     * 用户第三方登录请求接口
     */
    @GET(PATH_LOGIN)
    Observable<LoginResp> login(@QueryMap(encoded = false) Map<String, String> params);

    /**
     *
     * @param token
     * @param appid
     * @param openid
     * @return
     */
    @GET("https://graph.qq.com/user/get_simple_userinfo")
    Observable<QQSimpleInfo> getSimpleUserInfo(@Query("access_token") String token,
                                               @Query("oauth_consumer_key") String appid,
                                               @Query("openid") String openid);

    /**
     * 用户token刷新接口
     * @param params
     * @return
     */
    @GET(PATH_REFRESH)
    Observable<RefreshResp> refreshToken(@QueryMap(encoded = false) Map<String, String> params);

    /**
     * 用户退出登录接口
     * @param params
     * @return
     */
    @GET(PATH_LOGOUT)
    Observable<JsonObject> logout(@QueryMap(encoded = false) Map<String, String> params);

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
    Observable<CoverPage> getCoverDetail(@Path("book_id") String bookId,
                                         @Path("book_source_id") String book_source_id);


    @GET(DEFAULT_BOOK)
    Observable<JsonObject> getDefaultBook();

    /**
     * 目录页面章节信息拉取
     */
    @GET(COVER + "/{book_id}/{book_source_id}/chapter")
    Observable<List<Chapter>> getChapterList(@Path("book_id") String bookId, @Path("book_source_id") String book_source_id, @RequestTag RequestItem book);

    /**
     * 单章购买请求
     */
    @GET(PURCHASE_SINGLE)
    Observable<SingleChapterBean> requestSingleChapter(@Query("bookSourceId") String sourceId, @Query("chapterId") String chapterId,
                                                       @Query("chapterName") String chapterName, @Query("uid") String uid);

    /**
     * 多章购买请求
     */
    @GET(PURCHASE_CHAPTERS)
    Observable<PurchaseResult> purchaseChapters(@QueryMap() Map<String, String> purchaseData);

    /**
     * 根据要购买的章节数获取总价格和折扣
     */
    @GET(GETPRICEBYNUMBER)
    Observable<ChapterPriceInfo> parseChaptersPriceInfo(@QueryMap(encoded = false) Map<String, String> params);

    /**
     * 批量购买
     */
    @GET(MUTIPURCHASE_DIALOG)
    Observable<MutiPurchaseDialogBean> requestPurchaseDialogInfo(@QueryMap(encoded = false) Map<String, String> params);

    /**
     * 用户反馈报错
     */
    @GET(CHAPTER_ERROR_FEEDBACK)
    Observable<NoBodyEntity> sendFeedBack(@QueryMap(encoded = false) Map<String, String> params);

    @FormUrlEncoded
    @POST(BOOK_CHECK)
    Observable<UpdateBean> getUpdatedZnBooks(@FieldMap() Map<String, String> params);

    @GET(APP_CHECK)
    Observable<JsonObject> checkAppUpdate(@QueryMap() Map<String, String> params);

    @GET(DYNAMIC_PARAMAS)
    Observable<JsonObject> getDynamicParams();

    @GET
    Observable<Chapter> getChapterContent(@Url String string, @RequestTag Chapter chapter);


}
