package com.ding.basic.request

import com.ding.basic.bean.*
import com.ding.basic.bean.push.BannerInfo
import com.google.gson.JsonObject
import io.reactivex.Flowable
import net.lzbook.kit.data.book.UserMarkBook
import net.lzbook.kit.data.user.UserBook
import net.lzbook.kit.user.bean.UserNameState
import net.lzbook.kit.user.bean.WXAccess
import net.lzbook.kit.user.bean.WXSimpleInfo
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface RequestService {

    companion object {
        //五步替默认接口请求地址：登陆二期必须用阿里的地址
        const val QBMFXSYDQ_DEFAULT_HOST = "https://zhuishu.lsread.cn"

        // cdn智能
        const val DYNAMIC_ZN = "https://public.lsread.cn/dpzn/{packageName}.json"

        // cdn传媒
        const val DYNAMIC_CM = "https://public.dingyueads.com/dpzn/{packageName}.json"

        // cdn原创
        const val DYNAMIC_YC = "https://public.qingoo.cn/dpzn/{packageName}.json"

        // 新的用户广告数据搜集接口
        const val AD_DATA_COLLECT = "http://ad.dingyueads.com:8010/insertData"


        /************************************ Host拼接 ***************************************/

        //应用更新
        const val CHECK_APPLICATION = "/v3/app/check"

        //动态参数校验
        const val DYNAMIC_CHECK = "/v3/dynamic/dynamicCheck"

        //动态参数
        const val DYNAMIC_PARAMETERS = "/v3/dynamic/dynamicParameter"


        //广告分渠道，分版本，分广告位 动态参数
        const val AD_CONTROL_DYNAMIC_PARAMETERS = "/v5/cn.dingyueApi.reader/advertising/get"

        //默认书架
        const val DEFAULT_BOOK = "/v5/book/default"

        //书籍详情
        const val BOOK_DETAIL = "/v5/book/cover"

        //章节列表
        const val CHAPTER_LIST = "/v5/book/chapter"

        //来源列表
        const val SOURCE_LIST = "/v5/book/source"

        /**
         * 搜索按钮（h5为前后端分离后的接口）
         */
        const val SEARCH_V3 = "/v3/search"
        const val SEARCH_V4 = "/v4/search/page"
        const val SEARCH_V5 = "/v5/search/page"
        const val SEARCH_VUE = "/h5/{packageName}/search"
        // 前后端不分离，数据融合（搜索一期）
        const val SEARCH_S1_V5 = "/v5/search/page"
        // 前后端不分离，数据融合（搜索二期）
        const val SEARCH_S2_V5 = "/v5/search/searchPage"

        /**
         * 搜索热词
         */
        const val HOT_WORDS_V3 = "/v3/search/hotWords"
        const val HOT_WORDS_V4 = "/v4/search/hotWords"

        /**
         * 搜索推荐
         */
        const val SEARCH_RECOMMEND_V5 = "/v5/search/autoOperations"

        /**
         * 搜索自动补全
         */
        const val AUTO_COMPLETE_V3 = "/v3/search/autoComplete"
        const val AUTO_COMPLETE_V4 = "/v4/search/autoComplete"
        const val AUTO_COMPLETE_V5 = "/v5/search/autoComplete"

        /**
         * 搜索页作者
         */
        const val AUTHOR_V4 = "/v4/author/homepage/page"
        const val AUTHOR_h5 = "/h5/{packageName}/author"


        //检查更新
        const val CHECK_UPDATE = "/v5/book/check"

        //书架更新
        const val BOOKSHELF_UPDATE = "/v5/book/update"

        const val FEEDBACK_ERROR = "/v3/log/fb"

        const val SHARE_INFORMATION = "/v5/share/getShareInfo"

        // 用户相关----------------------
        //登陆操作
        const val LOGIN_ACTION = "/v3/user/login"

        //登出操作
        const val LOGOUT_ACTION = "/v3/user/logout"

        //刷新Token
        const val REFRESH_TOKEN = "/v3/user/refLToken"
        /**
         * 刷新用户token
         */
        const val PATH_REFRESH_TOKEN = "/v4/user/refresh_token"

        // 获取短信
        const val PATH_FETCH_SMS_CODE_V4 = "/v4/message/sms"
        // 短信登录
        const val PATH_SMS_LOGIN_V4 = "/v4/user/sms_create_token"
        /**
         * 注销用户信息
         */
        const val PATH_LOGOUT = "/v4/user/invalid_token"

        // 上传头像
        const val PATH_UPLOAD_USER_AVATAR = "/v4/user/update_user_Avatar"

        // 获取昵称修改剩余天数
        const val PATH_FETCH_USER_NAME_STATE = "/v4/user/get_update_name_count"
        // 修改用户性别
        const val PATH_UPDATE_USER_GENDER = "/v4/user/update_user_gender"
        // 绑定手机号
        const val PATH_BIND_PHONE_NUMBER = "/v4/user/link_local"

        // 修改用户昵称
        const val PATH_UPDATE_USER_NAME = "/v4/user/update_user_name"
        // 第三方登录
        const val PATH_THIRD_LOGIN = "/v4/user/third_create_token"
        // 绑定第三方账户
        const val PATH_BIND_THIRD_ACCOUNT = "/v4/user/link_third"
        // 上传书架
        const val PATH_BOOKSHELF_UPLOAD = "/v4/bookshelf/add"
        // 获取书架
        const val PATH_BOOKSHELF_FETCH = "/v4/bookshelf/get"

        //获取用户书签
        const val PATH_BOOKMAEK_GET = "/v4/bookmark/get"
        //上传书签
        const val PATH_BOOKMAEK_ADD = "/v4/bookmark/add"
        // 根据用户Id获取浏览足迹
        const val PATH_GET_FOOTPRINT = "/v4/footprint/get"
        // 上传足迹
        const val PATH_UPLOAD_FOOTPRINT = "/v4/footprint/add"

        // 用户相关----------------------


        //获得缓存方式和package 列表
        @Deprecated("已切换到union,MicroService.DOWN_TASK_CONFIG")
        const val DOWN_TASK_CONFIG = "/v5/book/down"


        /**
         * 书籍推荐（包括完结页推荐、书籍详情页推荐）
         */
        const val BOOK_RECOMMEND = "/v5/search/recommend"
        //完结页推荐
        const val BOOK_END_RECOMMEND_V4 = "/v4/recommend/{book_id}/readPage"
        //书籍封面页推荐
        const val COVER_RECOMMEND = "/v4/recommend/{book_id}/coverPage"
        //书籍封面页该作者其他作品推荐
        const val AUTHOR_OTHER_BOOK_RECOMMEND = "/v5/search/authorRecommend "
        //书架推荐
        const val RECOMMEND_SHELF = "/v4/recommend/shelfPage"

        /**
         * 标签聚合页（从书籍详情页跳入，仅智能书籍有标签推荐）
         */
        const val LABEL_SEARCH_V4 = "/v4/search/labelSearch/page"

        /**
         * WebView分类页面（h5为前后端分离后的接口）
         */
        const val WEB_CATEGORY_V4 = "/v4/cn.dingyueWeb.reader/category/free/category"
        const val WEB_CATEGORY_V3 = "/{packageName}/v3/category/index.do"
        const val WEB_CATEGORY_MAN_H5 = "/h5/{packageName}/categoryBoy"
        const val WEB_CATEGORY_WOMAN_H5 = "/h5/{packageName}/categoryGirl"
        const val WEB_CATEGORY_H5 = "/h5/{packageName}/category"

        /**
         * WebView推荐 / 精选页面（h5为前后端分离后的接口）
         */
        const val WEB_RECOMMEND_V4 = "/v4/cn.dingyueWeb.reader/recommend/free/recommend"
        const val WEB_RECOMMEND_V3 = "/{packageName}/v3/recommend/index.do"
        const val WEB_RECOMMEND_H5 = "/h5/{packageName}/recommend"// 精选
        const val WEB_RECOMMEND_H5_BOY = "/h5/{packageName}/recommendBoy" //精选男频
        const val WEB_RECOMMEND_H5_Girl = "/h5/{packageName}/recommendGirl" // 精选女频
        const val WEB_RECOMMEND_H5_Finish = "/h5/{packageName}/recommendFinish" //精选完本

        /**
         * WebView排行 / 榜单排行页面（h5为前后端分离后的接口）
         */
        const val WEB_RANK_V3 = "/{packageName}/v3/rank/index.do"
        const val WEB_RANK_H5 = "/h5/{packageName}/rank"
        const val WEB_RANK_H5_BOY = "/h5/{packageName}/rankBoy"
        const val WEB_RANK_H5_Girl = "/h5/{packageName}/rankGirl"

        /**
         * PUSH标签
         */
        const val PUSH_TAG = "/v1/zn.bigdata.api/activity/getUserTag"

        /**
         * 活动标签
         */
        const val BANNER_TAG = "/v4/cn.dingyueWeb.reader/getActivityData"

        /**
         * 搜索无结果页  点击订阅  searchEmpty/userSubscription
         */
        const val SEARCH_SUB_BOOK = "/v5/cn.dingyueWeb.reader/searchEmpty/userSubscription"

        /**
         * 字体下载链接
         */
        const val FONT_URL = "https://sta.zhuishuwang.com/cc-remennovel/apk/"

        /**
         * 语音插件包下载地址
         */
        const val VOICE_PLUGIN_URL = "http://zn-app-plugin.oss-cn-hangzhou.aliyuncs.com/Android/voice_plugin.zip"

        /**
         * 二维码 拉新
         */
        const val QR_CODE = "/h5/{packageName}/share"



        /***
         * 精选页
         * **/
        const val WEB_RECOMMEND = "/h5/{packageName}/recommend"
        /***
         * 精选男频
         * **/
        const val WEB_RECOMMEND_MAN = "/h5/{packageName}/recommendBoy"
        /***
         * 精选女频
         * **/
        const val WEB_RECOMMEND_WOMAN = "/h5/{packageName}/recommendGirl"
        /***
         * 精选完结
         * **/
        const val WEB_RECOMMEND_FINISH = "/h5/{packageName}/recommendFinish"
        /***
         * 榜单
         * **/
        const val WEB_RANKING = "/h5/{packageName}/rank"
        /***
         * 分类
         * **/
        const val WEB_CATEGORY = "/h5/{packageName}/category"
        /***
         * 搜索
         * **/
        const val WEB_SEARCH = "/h5/{packageName}/search"
        /***
         * 作者主页
         * **/
        const val WEB_AUTHOR = "/h5/{packageName}/author"
    }

    @GET(DEFAULT_BOOK)
    fun requestDefaultBooks(): Flowable<BasicResult<CoverList>>

    // sex：0全部 1男 2女
    @GET(DEFAULT_BOOK)
    fun requestDefaultBooks(@Query("sex") sex: Int): Flowable<BasicResult<CoverList>>

    @GET(CHECK_APPLICATION)
    fun requestApplicationUpdate(@QueryMap parameters: Map<String, String>): Flowable<JsonObject>

    @GET(DYNAMIC_CHECK)
    fun requestDynamicCheck(): Flowable<BasicResult<Int>>

    @GET(DYNAMIC_PARAMETERS)
    fun requestDynamicParameters(): Flowable<Parameter>

    @GET(AD_CONTROL_DYNAMIC_PARAMETERS)
    fun requestAdControlDynamic(): Flowable<AdControlByChannelBean>

    @GET
    fun requestCDNDynamicPar(@Url url: String): Flowable<Parameter>

    @GET(BOOK_DETAIL)
    fun requestBookDetail(@Query("book_id") book_id: String, @Query("book_source_id") book_source_id: String): Flowable<BasicResult<Book>>

    @GET(BOOK_DETAIL)
    fun requestBookDetail(@Query("book_id") book_id: String, @Query("book_source_id") book_source_id: String, @Query("book_chapter_id") book_chapter_id: String): Flowable<BasicResult<Book>>

    @GET(SOURCE_LIST)
    fun requestBookSources(@Query("book_id") book_id: String, @Query("book_source_id") book_source_id: String, @Query("book_chapter_id") book_chapter_id: String): Flowable<BasicResult<BookSource>>

    @GET(SOURCE_LIST)
    fun requestBookSources(@Query("book_id") book_id: String, @Query("book_source_id") book_source_id: String): Flowable<BasicResult<BookSource>>

    @GET(AUTO_COMPLETE_V3)
    fun requestAutoComplete(@Query("word") word: String): Flowable<SearchAutoCompleteBean>

    @GET(AUTO_COMPLETE_V4)
    fun requestAutoCompleteV4(@Query("keyword") word: String): Flowable<SearchAutoCompleteBeanYouHua>


    @GET(AUTO_COMPLETE_V5)
    fun requestAutoCompleteV5(@Query("keyword") word: String): Flowable<SearchAutoCompleteBeanYouHua>

    @GET(HOT_WORDS_V4)
    fun requestHotWordV4(): Flowable<Result<SearchResult>>

    @GET(SEARCH_RECOMMEND_V5)
    fun requestSearchRecommend(@Query("shelfBooks") shelfBooks: String): Flowable<SearchRecommendBook>

    @GET(HOT_WORDS_V3)
    fun requestHotWords(): Flowable<SearchHotBean>

    @POST(CHECK_UPDATE)
    @Headers("Content-Type: application/json;charset=UTF-8")
    fun requestBookUpdate(@Body json: RequestBody): Flowable<BasicResult<UpdateBean>>

    @POST(BOOKSHELF_UPDATE)
    @Headers("Content-Type: application/json;charset=UTF-8")
    fun requestBookShelfUpdate(@Body json: RequestBody): Flowable<BasicResult<CoverList>>

    @GET(FEEDBACK_ERROR)
    fun requestFeedback(@QueryMap(encoded = false) params: Map<String, String>): Flowable<NoBodyEntity>

    @GET(SHARE_INFORMATION)
    fun requestShareInformation(): Flowable<BasicResultV4<ShareInformation>>

    /************************************* 用户相关 *************************************/

    @GET
    fun requestLoginAction(@Url url: String, @QueryMap(encoded = false) parameters: Map<String, String>): Flowable<LoginResp>

    @GET
    fun requestLogoutAction(@Url url: String, @QueryMap(encoded = false) parameters: Map<String, String>): Flowable<JsonObject>

    @GET
    fun requestRefreshToken(@Url url: String, @QueryMap(encoded = false) parameters: Map<String, String>): Flowable<RefreshResp>

    @GET(PATH_FETCH_SMS_CODE_V4)// 获取短信验证码
    fun requestSmsCode(@Query("phoneNumber") mobileNumber: String): Flowable<BasicResultV4<String>>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST(PATH_SMS_LOGIN_V4) // 短信验证码登录
    fun requestSmsLogin(@Body smsLoginBody: RequestBody): Flowable<BasicResultV4<LoginRespV4>>

    @GET(PATH_LOGOUT)
    fun logout(): Flowable<BasicResultV4<String>>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST(PATH_UPLOAD_USER_AVATAR)// 上传用户头像
    fun uploadUserAvatar(@Body avatarBody: RequestBody): Flowable<BasicResultV4<LoginRespV4>>

    @GET(PATH_FETCH_USER_NAME_STATE) // 获取修改昵称剩余天数
    fun requestUserNameState(): Flowable<BasicResultV4<UserNameState>>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST(PATH_UPDATE_USER_GENDER)
    fun uploadUserGender(@Body genderBody: RequestBody): Flowable<BasicResultV4<LoginRespV4>>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST(PATH_BIND_PHONE_NUMBER)
    fun bindPhoneNumber(@Body phoneBody: RequestBody): Flowable<BasicResultV4<LoginRespV4>>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST(PATH_UPDATE_USER_NAME)
    fun uploadUserName(@Body nameBody: RequestBody): Flowable<BasicResultV4<LoginRespV4>>

    @GET(PATH_BOOKSHELF_FETCH) // 获取书架
    fun requestBookshelf(@Query("accountId") accountId: String): Flowable<BasicResultV4<List<UserBook>>>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST(PATH_BOOKSHELF_UPLOAD) //上传书架
    fun uploadBookshelf(@Body bookshelf: RequestBody): Flowable<BasicResultV4<String>>
    /**
     * 刷新Token
     * @return Observable<Result></Result><User>>
    </User> */
    @GET(PATH_REFRESH_TOKEN)
    fun refreshToken(): Flowable<BasicResultV4<LoginRespV4>>

    @GET(PATH_BOOKMAEK_GET) // 获取用户书签
    fun requestBookMarks(@Query("accountId") userId: String): Flowable<BasicResultV4<List<UserMarkBook>>>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST(PATH_BOOKMAEK_ADD)// 上传书签
    fun uploadBookMark(@Body bookMarkBody: RequestBody): Flowable<BasicResultV4<String>>

    @GET(PATH_GET_FOOTPRINT)// 获取用户浏览记录
    fun requestFootPrintList(@Query("accountId") userId: String): Flowable<BasicResultV4<List<UserBook>>>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST(PATH_UPLOAD_FOOTPRINT)
    fun uploadFootPrint(@Body bookBrowseReqBody: RequestBody): Flowable<BasicResultV4<String>>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST(PATH_THIRD_LOGIN) //第三方登录
    fun thirdLogin(@Body thirdLoginBody: RequestBody): Flowable<BasicResultV4<LoginRespV4>>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST(PATH_BIND_THIRD_ACCOUNT) // 绑定第三方
    fun bindThirdAccount(@Body thirdLoginBody: RequestBody): Flowable<BasicResultV4<LoginRespV4>>

    @GET("https://graph.qq.com/user/get_simple_userinfo")// 获取QQ用户信息
    fun requestUserInformation(@Query("access_token") token: String,
                               @Query("oauth_consumer_key") appid: String,
                               @Query("openid") openid: String): Flowable<QQSimpleInfo>

    @GET("https://api.weixin.qq.com/sns/oauth2/access_token")// 获取微信token
    fun requestWXAccessToken(@Query("appid") appId: String,
                             @Query("secret") secret: String,
                             @Query("code") code: String,
                             @Query("grant_type") authorizationCode: String): Flowable<WXAccess>

    @GET("https://api.weixin.qq.com/sns/userinfo")// 获取微信用户信息
    fun requestWXUserInfo(@Query("access_token") accessToken: String,
                          @Query("openid") openid: String): Flowable<WXSimpleInfo>

    /************************************* 缓存相关 *************************************/
    @GET(DOWN_TASK_CONFIG)
    fun requestDownTaskConfig(@Query(value = "bookId") str: String, @Query(value = "bookSourceId") str2: String, @Query(value = "type") i: Int, @Query(value = "chapterId") str3: String): Flowable<BasicResult<CacheTaskConfig>>


    @FormUrlEncoded
    @POST(COVER_RECOMMEND)
    fun requestCoverRecommend(@Path("book_id") book_id: String, @Field("recommanded") bookIds: String): Flowable<CoverRecommendBean>


    @GET(BOOK_RECOMMEND)
    fun requestBookRecommend(@Query("bookId") book_id: String, @Query("shelfBooks") shelfBooks: String): Flowable<CommonResult<RecommendBooks>>

    @GET(AUTHOR_OTHER_BOOK_RECOMMEND)
    fun requestAuthorOtherBookRecommend(@Query("author") author: String, @Query("blockBoooks") book_id: String): Flowable<CommonResult<ArrayList<RecommendBean>>>

    @FormUrlEncoded
    @POST(BOOK_END_RECOMMEND_V4)
    fun requestBookRecommendV4(@Path("book_id") book_id: String, @Field("recommanded") bookIds: String): Flowable<RecommendBooksEndResp>

    @GET
    fun requestPushTags(@Url url: String, @Query("udid") udid: String): Flowable<CommonResult<ArrayList<String>>>

    @GET(BANNER_TAG)
    fun requestBannerTags(): Flowable<CommonResult<BannerInfo>>

    //搜索无结果页  订阅
    @GET(SEARCH_SUB_BOOK)
    fun requestSubBook(@Query("bookName") bookName: String, @Query("authorName") bookAuthor: String): Flowable<JsonObject>

    @Streaming
    @GET
    fun downloadFont(@Url url: String): Flowable<ResponseBody>

    @Streaming
    @GET(VOICE_PLUGIN_URL)
    fun downloadVoicePlugin(): Flowable<ResponseBody>



}