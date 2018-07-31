package com.ding.basic.repository

import android.annotation.SuppressLint
import android.content.Context
import com.ding.basic.bean.*
import com.ding.basic.database.helper.BookDataProviderHelper
import com.ding.basic.request.ResultCode
import com.ding.basic.util.ChapterCacheUtil
import com.google.gson.JsonObject
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import net.lzbook.kit.data.db.help.ChapterDaoHelper
import okhttp3.RequestBody
import retrofit2.Call

@SuppressLint("StaticFieldLeak")
class LocalRequestRepository private constructor(private var context: Context) : BasicRequestRepository {



    companion object {
        private var localRequestRepository: LocalRequestRepository? = null

        fun loadLocalRequestRepository(context: Context): LocalRequestRepository {
            if (localRequestRepository == null) {
                synchronized(LocalRequestRepository::class) {
                    if (localRequestRepository == null) {
                        localRequestRepository = LocalRequestRepository(context = context)
                    }
                }
            }

            return localRequestRepository!!
        }
    }

    override fun requestDefaultBooks(): Flowable<BasicResult<CoverList>>? {
        return null
    }

    override fun requestApplicationUpdate(parameters: Map<String, String>): Flowable<JsonObject>? {
        return null
    }

    override fun requestDynamicParameters(): Flowable<JsonObject>? {
        return null
    }

    override fun requestCoverBatch(requestBody: RequestBody): Flowable<BasicResult<List<Book>>>? {
        return null
    }

    override fun requestBookDetail(book_id: String, book_source_id: String, book_chapter_id: String): Flowable<BasicResult<Book>>? {
        return null
    }

    override fun requestSubBook(bookName: String, bookAuthor: String): Flowable<JsonObject>? {
        return null
    }

    override fun requestBookCatalog(book_id: String, book_source_id: String, book_chapter_id: String): Flowable<BasicResult<Catalog>> {
        return Flowable.create({ emitter ->

            val result = BasicResult<Catalog>()

            result.code = ResultCode.LOCAL_RESULT
            result.msg = "catalog from local"

            val catalog = Catalog()

            catalog.book_id = book_id
            catalog.book_source_id = book_source_id
            catalog.book_chapter_id = book_chapter_id

            if (RequestRepositoryFactory.loadRequestRepositoryFactory(context).checkBookSubscribe(book_id) != null) {
                val chapterDaoHelp = ChapterDaoHelper.loadChapterDataProviderHelper(context = context, book_id = book_id)
                catalog.chapters = chapterDaoHelp.queryAllChapters()
            }
            catalog.chapterCount = if (catalog.chapters == null) {
                0
            } else {
                catalog.chapters!!.size
            }

            result.data = catalog

            emitter.onNext(result)

            emitter.onComplete()

        }, BackpressureStrategy.BUFFER)
    }

    override fun requestBookSources(book_id: String, book_source_id: String, book_chapter_id: String): Flowable<BasicResult<BookSource>>? {
        return null
    }

    override fun requestAutoComplete(word: String): Flowable<SearchAutoCompleteBean>? {
        return null
    }

    override fun requestAutoCompleteV4(word: String): Flowable<SearchAutoCompleteBeanYouHua>? {
        return null
    }

    override fun requestAutoCompleteV5(word: String): Flowable<SearchAutoCompleteBeanYouHua>? {
        return null
    }

    override fun requestHotWords(): Flowable<SearchHotBean>? {
        return null
    }

    override fun requestSearchRecommend(bookIds: String): Flowable<SearchRecommendBook>? {
        return null
    }

    override fun requestHotWordsV4(): Flowable<Result<SearchResult>>? {
        return null
    }

    override fun requestChapterContent(chapter: Chapter): Flowable<BasicResult<Chapter>> {
        chapter.content = ChapterCacheUtil.checkChapterCacheExist(chapter)

        val result = BasicResult<Chapter>()
        result.data = chapter
        result.code = 20000
        result.msg = "success"

        return Flowable.create({
            it.onNext(result)
            it.onComplete()
        }, BackpressureStrategy.BUFFER)
    }

    override fun requestChapterContentSync(chapter_id: String, book_id: String, book_source_id: String, book_chapter_id: String): Call<BasicResult<Chapter>>? {
        return null
    }

    override fun requestBookUpdate(requestBody: RequestBody): Flowable<BasicResult<UpdateBean>>? {
        return null
    }

    override fun requestBookShelfUpdate(requestBody: RequestBody): Flowable<BasicResult<CoverList>>? {
        return null
    }

    override fun requestFeedback(parameters: Map<String, String>): Flowable<NoBodyEntity>? {
        return null
    }

    override fun requestLoginAction(parameters: Map<String, String>): Flowable<LoginResp>? {
        return null
    }


    override fun requestLogoutAction(parameters: Map<String, String>): Flowable<JsonObject>? {
        return null
    }

    override fun requestRefreshToken(parameters: Map<String, String>): Flowable<RefreshResp>? {
        return null
    }

    override fun requestUserInformation(token: String, appid: String, openid: String): Flowable<QQSimpleInfo>? {
        return null
    }

    override fun requestCoverRecommend(book_id: String, recommend: String): Flowable<CoverRecommendBean>? {
        return null
    }

    override fun requestAuthAccess(): Flowable<BasicResult<String>>? {
        return null
    }

    override fun requestAuthAccessSync(): Call<BasicResult<String>>? {
        return null
    }

    fun checkBookSubscribe(book_id: String): Book? {
        return BookDataProviderHelper.loadBookDataProviderHelper(context = context).checkBookSubscribe(book_id)
    }

    fun insertBook(book: Book): Long {
        return BookDataProviderHelper.loadBookDataProviderHelper(context = context).insertBook(book, context)
    }

    fun updateBook(book: Book): Boolean {
        return BookDataProviderHelper.loadBookDataProviderHelper(context = context).updateBook(book)
    }

    fun updateBooks(books: List<Book>): Boolean {
        return BookDataProviderHelper.loadBookDataProviderHelper(context = context).updateBooks(books)
    }

    fun deleteBook(book_id: String): Boolean {
        return BookDataProviderHelper.loadBookDataProviderHelper(context = context).deleteBook(book_id, context)
    }

    fun deleteBooks(books: List<Book>) {
        return BookDataProviderHelper.loadBookDataProviderHelper(context = context).deleteBooks(books, context)
    }

    fun deleteShelfBooks() {
        return BookDataProviderHelper.loadBookDataProviderHelper(context = context).deleteShelfBooks()
    }


    fun loadBook(book_id: String): Book? {
        return BookDataProviderHelper.loadBookDataProviderHelper(context = context).loadBook(book_id)
    }

    fun loadBooks(): List<Book>? {
        return BookDataProviderHelper.loadBookDataProviderHelper(context = context).loadBooks()
    }

    fun loadReadBooks(): List<Book>? {
        return BookDataProviderHelper.loadBookDataProviderHelper(context = context).loadReadBooks()
    }

    fun loadBookCount(): Long {
        return BookDataProviderHelper.loadBookDataProviderHelper(context = context).loadBookCount()
    }

    fun insertBooks(books: List<Book>) {
        return BookDataProviderHelper.loadBookDataProviderHelper(context = context).insertBooks(books)
    }

    fun loadBookShelfIDs(): String {
        return BookDataProviderHelper.loadBookDataProviderHelper(context = context).loadBookShelfIDs()
    }


    fun insertBookFix(bookFix: BookFix) {
        return BookDataProviderHelper.loadBookDataProviderHelper(context = context).insertBookFix(bookFix)
    }

    fun deleteBookFix(id: String) {
        return BookDataProviderHelper.loadBookDataProviderHelper(context = context).deleteBookFix(id)
    }

    fun loadBookFixs(): List<BookFix>? {
        return BookDataProviderHelper.loadBookDataProviderHelper(context = context).loadBookFixs()
    }

    fun loadBookFix(book_id: String): BookFix? {
        return BookDataProviderHelper.loadBookDataProviderHelper(context = context).loadBookFix(book_id)
    }

    fun updateBookFix(bookFix: BookFix) {
        return BookDataProviderHelper.loadBookDataProviderHelper(context = context).updateBookFix(bookFix)
    }

    override fun requestDownTaskConfig(bookID: String, bookSourceID: String, type: Int, startChapterID: String): Flowable<BasicResult<CacheTaskConfig>>? {
        return null
    }

    override fun requestBookRecommend(book_id: String, shelfBooks: String): Flowable<CommonResult<RecommendBooks>>? {
        return null
    }

    override fun requestAuthorOtherBookRecommend(author: String, book_id: String): Flowable<CommonResult<ArrayList<RecommendBean>>>? {
        return null
    }

    override fun requestBookRecommendV4(book_id: String, recommend: String): Flowable<RecommendBooksEndResp>? {
        return null
    }
    fun insertOrUpdate(user:LoginRespV4){
        BookDataProviderHelper.loadBookDataProviderHelper(context = context).insertOrUpdate(user)
    }

    fun queryLoginUser():LoginRespV4{
        return BookDataProviderHelper.loadBookDataProviderHelper(context=context).queryLoginUser()
    }

    fun deleteLoginUser(){
        BookDataProviderHelper.loadBookDataProviderHelper(context=context).deleteLoginUser()
    }

}