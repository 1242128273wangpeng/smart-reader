package com.ding.basic.repository

import com.ding.basic.bean.*
import com.ding.basic.request.RequestSubscriber
import com.google.gson.JsonObject
import io.reactivex.Flowable
import okhttp3.RequestBody
import java.util.HashMap

interface RequestRepository {

    /************************* 网络请求 *************************/
    fun requestDefaultBooks(requestSubscriber: RequestSubscriber<Boolean>)

    fun requestApplicationUpdate(parameters: Map<String, String>, requestSubscriber: RequestSubscriber<ApplicationUpdate>)

    fun requestDynamicParameters(requestSubscriber: RequestSubscriber<JsonObject>)

    fun requestBookDetail(book_id: String, book_source_id: String, book_chapter_id: String, requestSubscriber: RequestSubscriber<Book>)

    fun requestCatalog(book_id: String, book_source_id: String, book_chapter_id: String, requestSubscriber: RequestSubscriber<List<Chapter>>, type: Int)

    fun requestBookSources(book_id: String, book_source_id: String, book_chapter_id: String, requestSubscriber: RequestSubscriber<BookSource>)

    fun requestAutoComplete(word: String, requestSubscriber: RequestSubscriber<SearchAutoCompleteBean>)

    fun requestAutoCompleteV4(word: String, requestSubscriber: RequestSubscriber<SearchAutoCompleteBeanYouHua>)  //搜索V4接口

    fun requestSearchRecommend(bookIds: String,requestSubscriber: RequestSubscriber<SearchRecommendBook>) //搜索推荐

    fun requestHotWords(requestSubscriber: RequestSubscriber<SearchHotBean>)

    fun requestSearchOperationV4(requestSubscriber: RequestSubscriber<Result<SearchResult>>)

    fun requestChapterContent(chapter: Chapter): Flowable<Chapter>

    fun requestBookUpdate(checkBody: RequestBody, books: HashMap<String, Book>, requestSubscriber: RequestSubscriber<List<BookUpdate>>)

    fun requestBookShelfUpdate(checkBody: RequestBody, requestSubscriber: RequestSubscriber<Boolean>)

    fun requestFeedback(parameters: Map<String, String>, requestSubscriber: RequestSubscriber<Boolean>)


    fun requestLoginAction(parameters: Map<String, String>, requestSubscriber: RequestSubscriber<LoginResp>)

    fun requestLogoutAction(parameters: Map<String, String>, requestSubscriber: RequestSubscriber<JsonObject>)

    fun requestRefreshToken(parameters: Map<String, String>, requestSubscriber: RequestSubscriber<RefreshResp>)

    fun requestUserInformation(token: String, appid: String, openid: String, requestSubscriber: RequestSubscriber<QQSimpleInfo>)

    fun requestCoverRecommend(book_id: String, recommend: String, requestSubscriber: RequestSubscriber<CoverRecommendBean>)

    fun requestAuthAccess(requestSubscriber: RequestSubscriber<String>)


    /************************* 本地数据 *************************/

    fun checkBookSubscribe(book_id: String): Book?

    fun insertBook(book: Book): Long

    fun updateBook(book: Book): Boolean

    fun updateBooks(books: List<Book>): Boolean

    fun deleteBook(book_id: String): Boolean

    fun deleteBooks(books: List<Book>)

    fun deleteShelfBook()

    fun loadBook(book_id: String): Book?

    fun loadBooks(): List<Book>?

    fun loadReadBooks(): List<Book>?

    fun loadBookCount(): Long

    fun loadBookShelfIDs(): String

    fun loadBookmarkList(book_id: String, requestSubscriber: RequestSubscriber<List<Bookmark>>)


    fun insertBookFix(bookFix: BookFix)

    fun deleteBookFix(id: String)

    fun loadBookFixs(): List<BookFix>?

    fun loadBookFix(book_id: String): BookFix?

    fun updateBookFix(bookFix: BookFix)


    fun checkChapterCache(chapter: Chapter?): Boolean
}
