package com.ding.basic.repository

import android.content.Context
import com.ding.basic.bean.*
import com.ding.basic.request.ContentAPI
import com.ding.basic.request.MicroAPI
import com.ding.basic.request.RequestAPI
import com.google.gson.JsonObject
import io.reactivex.Flowable
import net.lzbook.kit.user.bean.UserNameState
import net.lzbook.kit.user.bean.WXAccess
import net.lzbook.kit.user.bean.WXSimpleInfo
import okhttp3.RequestBody
import retrofit2.Call

/**
 * Created on 2018/3/6.
 * Created by crazylei.
 */
class InternetRequestRepository private constructor(context: Context?) : BasicRequestRepository {


    companion object {
        private var internetRequestRepository: InternetRequestRepository? = null

        fun loadInternetRequestRepository(context: Context?): InternetRequestRepository {
            if (internetRequestRepository == null) {
                synchronized(LocalRequestRepository::class) {
                    if (internetRequestRepository == null) {
                        internetRequestRepository = InternetRequestRepository(context = context)
                    }
                }
            }

            return internetRequestRepository!!
        }
    }

    override fun requestDefaultBooks(): Flowable<BasicResult<CoverList>>? {
        return RequestAPI.requestDefaultBooks()
    }

    override fun requestApplicationUpdate(parameters: Map<String, String>): Flowable<JsonObject>? {
        return RequestAPI.requestApplicationUpdate(parameters = parameters)
    }

    override fun requestDynamicParameters(): Flowable<JsonObject>? {
        return RequestAPI.requestDynamicParameters()
    }

    override fun requestBookSources(book_id: String, book_source_id: String, book_chapter_id: String): Flowable<BasicResult<BookSource>>? {
        return RequestAPI.requestBookSources(book_id, book_source_id, book_chapter_id)
    }

    override fun requestAutoComplete(word: String): Flowable<SearchAutoCompleteBean>? {
        return RequestAPI.requestAutoComplete(word)
    }

    override fun requestAutoCompleteV4(word: String): Flowable<SearchAutoCompleteBeanYouHua>? {
        return RequestAPI.requestAutoCompleteV4(word)
    }

    override fun requestAutoCompleteV5(word: String): Flowable<SearchAutoCompleteBeanYouHua>? {
        return RequestAPI.requestAutoCompleteV5(word)
    }

    override fun requestSearchRecommend(bookIds: String): Flowable<SearchRecommendBook>? {
        return RequestAPI.requestSearchRecommend(bookIds)
    }

    override fun requestHotWords(): Flowable<SearchHotBean>? {
        return RequestAPI.requestHotWords()
    }

    override fun requestHotWordsV4(): Flowable<Result<SearchResult>> {
        return RequestAPI.requestHotWordsV4()
    }

    override fun requestBookShelfUpdate(requestBody: RequestBody): Flowable<BasicResult<CoverList>>? {
        return RequestAPI.requestBookShelfUpdate(requestBody)
    }


    override fun requestFeedback(parameters: Map<String, String>): Flowable<NoBodyEntity>? {
        return RequestAPI.requestFeedback(parameters)
    }

    override fun requestLoginAction(parameters: Map<String, String>): Flowable<LoginResp>? {
        return RequestAPI.requestLoginAction(parameters)
    }

    override fun requestLogoutAction(parameters: Map<String, String>): Flowable<JsonObject>? {
        return RequestAPI.requestLogoutAction(parameters)
    }

    fun requestSmsCode(mobile: String): Flowable<BasicResultV4<String>>? {
        return RequestAPI.requestSmsCode(mobile)
    }

    fun requestSmsLogin(smsRequestBody: RequestBody): Flowable<BasicResultV4<LoginRespV4>>? {

        return RequestAPI.requestSmsLogin(smsRequestBody)
    }
    fun requestLogout():Flowable<BasicResultV4<String>>{
        return RequestAPI.requestLogout()
    }

    fun uploadUserAvatar(avatarBody: RequestBody): Flowable<BasicResultV4<LoginRespV4>> {
        return RequestAPI.uploadUserAvatar(avatarBody)
    }

    fun requestUserNameState(): Flowable<BasicResultV4<UserNameState>> {
        return RequestAPI.requestUserNameState()
    }

    fun uploadUserGender(genderBody: RequestBody): Flowable<BasicResultV4<LoginRespV4>> {
        return RequestAPI.uploadUserGender(genderBody)
    }

    fun uploadUserName(nameBody:RequestBody):Flowable<BasicResultV4<LoginRespV4>>{

        return RequestAPI.uploadUserName(nameBody)
    }

    fun bindPhoneNumber(phoneBody:RequestBody):Flowable<BasicResultV4<LoginRespV4>>{
        return  RequestAPI.bindPhoneNumber(phoneBody)
    }

    override fun requestRefreshToken(parameters: Map<String, String>): Flowable<RefreshResp>? {
        return RequestAPI.requestRefreshToken(parameters)
    }

    override fun requestUserInformation(token: String, appid: String, openid: String): Flowable<QQSimpleInfo>? {
        return RequestAPI.requestUserInformation(token, appid, openid)
    }

    fun requestWXAccessToken(appid: String, secret: String, code: String, authorizationCode: String): Flowable<WXAccess> {
        return RequestAPI.requestWXAccessToken(appid, secret, code, authorizationCode)
    }

    fun requestWXUserInfo(token: String, openid: String): Flowable<WXSimpleInfo> {
        return RequestAPI.requestWXUserInfo(token, openid)
    }

    fun thirdLogin(thirdBody: RequestBody): Flowable<BasicResultV4<LoginRespV4>> {
        return RequestAPI.thirdLogin(thirdBody)
    }

    fun bindThirdAccount(accountBody: RequestBody): Flowable<BasicResultV4<LoginRespV4>> {
        return RequestAPI.bindThirdAccount(accountBody)
    }


    override fun requestCoverRecommend(book_id: String, recommend: String): Flowable<CoverRecommendBean>? {
        return RequestAPI.requestCoverRecommend(book_id, recommend)
    }


    override fun requestBookRecommend(book_id: String, shelfBooks: String): Flowable<CommonResult<RecommendBooks>>? {
        return RequestAPI.requestBookRecommend(book_id, shelfBooks)
    }

    override fun requestAuthorOtherBookRecommend(author: String, book_id: String): Flowable<CommonResult<ArrayList<RecommendBean>>>? {
        return RequestAPI.requestAuthorOtherBookRecommend(author, book_id)
    }

    override fun requestBookRecommendV4(book_id: String, recommend: String): Flowable<RecommendBooksEndResp>? {
        return RequestAPI.requestBookRecommendV4(book_id, recommend)
    }

    fun requestPushTags(udid: String): Flowable<CommonResult<ArrayList<String>>> {
        return RequestAPI.requestPushTags(udid)
    }

    override fun requestSubBook(bookName: String, bookAuthor: String): Flowable<JsonObject>? {
        return RequestAPI.requestSubBook(bookName, bookAuthor)
    }

    /***************** 微服务 *****************/

    override fun requestAuthAccess(): Flowable<BasicResult<String>>? {
        return MicroAPI.requestAuthAccess()
    }

    /***************** 微服务同步鉴权 *****************/

    override fun requestAuthAccessSync(): Call<BasicResult<String>> {
        return MicroAPI.requestAuthAccessSync()
    }

    override fun requestBookDetail(book_id: String, book_source_id: String, book_chapter_id: String): Flowable<BasicResult<Book>>? {
        return MicroAPI.requestBookDetail(book_id, book_source_id, book_chapter_id)
    }

    override fun requestBookCatalog(book_id: String, book_source_id: String, book_chapter_id: String): Flowable<BasicResult<Catalog>> {
        return MicroAPI.requestBookCatalog(book_id, book_source_id, book_chapter_id)
    }

    override fun requestBookUpdate(requestBody: RequestBody): Flowable<BasicResult<UpdateBean>>? {
        return MicroAPI.requestBookUpdate(requestBody)
    }

    override fun requestCoverBatch(requestBody: RequestBody): Flowable<BasicResult<List<Book>>>? {
        return MicroAPI.requestCoverBatch(requestBody)
    }

    override fun requestDownTaskConfig(bookID: String, bookSourceID: String
                                       , type: Int, startChapterID: String): Flowable<BasicResult<CacheTaskConfig>>? {
        return MicroAPI.requestDownTaskConfig(bookID, bookSourceID, type, startChapterID)
    }

    override fun requestChapterContent(chapter: Chapter): Flowable<BasicResult<Chapter>> {
        return ContentAPI.requestChapterContent(chapter.chapter_id, chapter.book_id, chapter.book_source_id, chapter.book_chapter_id)
    }

    override fun requestChapterContentSync(chapter_id: String, book_id: String, book_source_id: String, book_chapter_id: String): Call<BasicResult<Chapter>>? {
        return ContentAPI.requestChapterContentSync(chapter_id, book_id, book_source_id, book_chapter_id)
    }
}