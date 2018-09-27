package com.ding.basic.net.api

import com.ding.basic.net.Config
import com.ding.basic.bean.*
import com.ding.basic.bean.push.BannerInfo
import com.ding.basic.net.api.service.RequestService
import com.ding.basic.net.interceptor.RequestInterceptor
import com.ding.basic.util.ReplaceConstants
import com.google.gson.JsonObject
import com.orhanobut.logger.Logger
import io.reactivex.Flowable
import com.ding.basic.bean.UserMarkBook
import net.lzbook.kit.data.user.UserBook
import net.lzbook.kit.utils.user.bean.UserNameState
import net.lzbook.kit.utils.user.bean.WXAccess
import net.lzbook.kit.utils.user.bean.WXSimpleInfo
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.properties.Delegates

/**
 * Created on 2018/3/13.
 * Created by crazylei.
 */
object RequestAPI {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().addNetworkInterceptor(RequestInterceptor()).build()

    private var requestService: RequestService by Delegates.notNull()

    init {

        initializeDataRequestService()

        Logger.v("初始化OkHttpClient!")
    }

    fun initializeDataRequestService() {
        val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .baseUrl(Config.loadRequestAPIHost()).build()

        requestService = retrofit.create(RequestService::class.java)
    }


    /**
     * sex：0全部；1男； 2女； -1不传sex字段
     */
    fun requestDefaultBooks(sex: Int): Flowable<BasicResult<CoverList>>? {
        return if (sex == -1) {
            requestService.requestDefaultBooks()
        } else {
            requestService.requestDefaultBooks(sex)
        }
    }

    fun requestApplicationUpdate(parameters: Map<String, String>): Flowable<JsonObject>? {
        return requestService.requestApplicationUpdate(parameters)
    }

    fun requestDynamicCheck(): Flowable<BasicResult<Int>> {
        return requestService.requestDynamicCheck()
    }

    fun requestDynamicParameters(): Flowable<Parameter> {
        return requestService.requestDynamicParameters()
    }

    fun requestAdControlDynamic(): Flowable<AdControlByChannelBean> {
        return requestService.requestAdControlDynamic()
    }

    fun requestCDNDynamicPar(url: String): Flowable<Parameter> {
        return requestService.requestCDNDynamicPar(url)
    }

    fun requestBookDetail(book_id: String, book_source_id: String, book_chapter_id: String): Flowable<BasicResult<Book>>? {
        if (book_chapter_id == "") {
            return requestService.requestBookDetail(book_id, book_source_id)
        }
        return requestService.requestBookDetail(book_id, book_source_id, book_chapter_id)
    }

    fun requestBookSources(book_id: String, book_source_id: String, book_chapter_id: String): Flowable<BasicResult<BookSource>>? {
        if (book_chapter_id == "") {
            return requestService.requestBookSources(book_id, book_source_id)
        }
        return requestService.requestBookSources(book_id, book_source_id, book_chapter_id)
    }

    fun requestAutoComplete(word: String): Flowable<SearchAutoCompleteBean>? {
        return requestService.requestAutoComplete(word)
    }

    fun requestAutoCompleteV4(word: String): Flowable<SearchAutoCompleteBeanYouHua>? {
        return requestService.requestAutoCompleteV4(word)
    }

    fun requestAutoCompleteV5(word: String): Flowable<SearchAutoCompleteBeanYouHua>? {
        return requestService.requestAutoCompleteV5(word)
    }

    fun requestHotWordsV4(): Flowable<Result<SearchResult>> {
        return requestService.requestHotWordV4()
    }


    fun requestSearchRecommend(bookIds: String): Flowable<SearchRecommendBook> {
        return requestService.requestSearchRecommend(bookIds)
    }

    fun requestHotWords(): Flowable<SearchHotBean>? {
        return requestService.requestHotWords()
    }

    fun requestBookShelfUpdate(requestBody: RequestBody): Flowable<BasicResult<CoverList>>? {
        return requestService.requestBookShelfUpdate(requestBody)
    }


    fun requestFeedback(parameters: Map<String, String>): Flowable<NoBodyEntity>? {
        return requestService.requestFeedback(parameters)
    }

    fun requestShareInformation(): Flowable<BasicResultV4<ShareInformation>> {
        return requestService.requestShareInformation()
    }

    // v3 的登陆接口强制走阿里云服务器，也就是默认的 host
    fun requestLoginAction(parameters: Map<String, String>): Flowable<LoginResp>? {
        val url = ReplaceConstants.getReplaceConstants().BOOK_NOVEL_DEPLOY_HOST +
                RequestService.LOGIN_ACTION
        return requestService.requestLoginAction(url, parameters)
    }

    fun requestLogoutAction(parameters: Map<String, String>): Flowable<JsonObject>? {
        val url = ReplaceConstants.getReplaceConstants().BOOK_NOVEL_DEPLOY_HOST +
                RequestService.LOGIN_ACTION
        return requestService.requestLogoutAction(url, parameters)
    }

    fun requestRefreshToken(parameters: Map<String, String>): Flowable<RefreshResp>? {
        val url = ReplaceConstants.getReplaceConstants().BOOK_NOVEL_DEPLOY_HOST +
                RequestService.LOGIN_ACTION
        return requestService.requestRefreshToken(url, parameters)
    }

    fun requestSmsCode(mobile: String): Flowable<BasicResultV4<String>>? {
        return requestService.requestSmsCode(mobile)
    }

    fun requestSmsLogin(smsBody: RequestBody): Flowable<BasicResultV4<LoginRespV4>>? {

        return requestService.requestSmsLogin(smsBody)
    }

    fun requestLogout(): Flowable<BasicResultV4<String>> {
        return requestService.logout()
    }

    fun uploadUserAvatar(avatarBody: RequestBody): Flowable<BasicResultV4<LoginRespV4>> {
        return requestService.uploadUserAvatar(avatarBody)
    }

    fun requestUserNameState(): Flowable<BasicResultV4<UserNameState>> {
        return requestService.requestUserNameState()

    }

    fun uploadUserGender(genderBody: RequestBody): Flowable<BasicResultV4<LoginRespV4>> {

        return requestService.uploadUserGender(genderBody)
    }

    fun uploadUserName(nameBody: RequestBody): Flowable<BasicResultV4<LoginRespV4>> {
        return requestService.uploadUserName(nameBody)
    }

    fun requestBookshelf(accountId: String): Flowable<BasicResultV4<List<UserBook>>> {
        return requestService.requestBookshelf(accountId)
    }

    fun uploadBookshelf(bookShelfBody: RequestBody): Flowable<BasicResultV4<String>> {
        return requestService.uploadBookshelf(bookShelfBody)
    }

    fun refreshToken(): Flowable<BasicResultV4<LoginRespV4>> {
        return requestService.refreshToken()
    }

    fun requestBookMarks(accountId: String): Flowable<BasicResultV4<List<UserMarkBook>>> {
        return requestService.requestBookMarks(accountId)
    }

    fun uploadBookMarks(markBody: RequestBody): Flowable<BasicResultV4<String>> {
        return requestService.uploadBookMark(markBody)
    }

    fun requestFootPrint(accountId: String): Flowable<BasicResultV4<List<UserBook>>> {
        return requestService.requestFootPrintList(accountId)
    }

    fun uploadFootPrint(footBody: RequestBody): Flowable<BasicResultV4<String>> {
        return requestService.uploadFootPrint(footBody)
    }


    fun bindPhoneNumber(phoneBody: RequestBody): Flowable<BasicResultV4<LoginRespV4>> {
        return requestService.bindPhoneNumber(phoneBody)
    }

    fun requestUserInformation(token: String, appid: String, openid: String): Flowable<QQSimpleInfo>? {
        return requestService.requestUserInformation(token, appid, openid)
    }

    fun requestWXAccessToken(appid: String, secret: String, code: String, authorizationCode: String): Flowable<WXAccess> {
        return requestService.requestWXAccessToken(appid, secret, code, authorizationCode)
    }

    fun requestWXUserInfo(token: String, openid: String): Flowable<WXSimpleInfo> {
        return requestService.requestWXUserInfo(token, openid)
    }


    fun thirdLogin(thirdBody: RequestBody): Flowable<BasicResultV4<LoginRespV4>> {
        return requestService.thirdLogin(thirdBody)
    }

    fun bindThirdAccount(accountBody: RequestBody): Flowable<BasicResultV4<LoginRespV4>> {
        return requestService.bindThirdAccount(accountBody)
    }


    fun requestDownTaskConfig(bookID: String, bookSourceID: String
                              , type: Int, startChapterID: String): Flowable<BasicResult<CacheTaskConfig>> {
        return requestService.requestDownTaskConfig(bookID, bookSourceID, type, startChapterID)
    }

    fun requestCoverRecommend(book_id: String, recommend: String): Flowable<CoverRecommendBean>? {
        return requestService.requestCoverRecommend(book_id, recommend)
    }

    fun requestBookRecommend(book_id: String, shelfBooks: String): Flowable<CommonResult<RecommendBooks>>? {
        return requestService.requestBookRecommend(book_id, shelfBooks)
    }

    fun requestAuthorOtherBookRecommend(author: String, book_id: String): Flowable<CommonResult<ArrayList<RecommendBean>>>? {
        return requestService.requestAuthorOtherBookRecommend(author, book_id)
    }

    fun requestBookRecommendV4(book_id: String, recommend: String): Flowable<RecommendBooksEndResp>? {
        return requestService.requestBookRecommendV4(book_id, recommend)
    }

    fun requestPushTags(url:String, udid: String): Flowable<CommonResult<ArrayList<String>>> {
        return requestService.requestPushTags(url, udid)
    }

    fun requestBannerTags(): Flowable<CommonResult<BannerInfo>> {
        return requestService.requestBannerTags()
    }

    fun requestSubBook(bookName: String, bookAuthor: String): Flowable<JsonObject>? {
        return requestService.requestSubBook(bookName, bookAuthor)
    }

    fun downloadFont(fontName: String): Flowable<ResponseBody> {
        val url = RequestService.FONT_URL + fontName
        return requestService.downloadFont(url)
    }

}