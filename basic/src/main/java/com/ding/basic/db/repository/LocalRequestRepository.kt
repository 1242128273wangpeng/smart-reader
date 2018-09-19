package com.ding.basic.db.repository

import android.annotation.SuppressLint
import android.content.Context
import com.ding.basic.bean.*
import com.ding.basic.bean.push.BannerInfo
import com.ding.basic.bean.push.PushInfo
import com.ding.basic.db.provider.BookDataProviderHelper
import com.ding.basic.db.provider.ChapterDataProviderHelper
import com.ding.basic.net.ResultCode
import com.ding.basic.util.ChapterCacheUtil
import com.ding.basic.util.getSharedObject
import com.ding.basic.util.isSameDay
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

/**
 * 本地数据库，数据对外提供类
 */
class LocalRequestRepository private constructor(private var context: Context) {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var localRequestRepository: LocalRequestRepository? = null

        fun loadLocalRequestRepository(context: Context): LocalRequestRepository {
            if (localRequestRepository == null) {
                synchronized(LocalRequestRepository::class) {
                    if (localRequestRepository == null) {
                        localRequestRepository = LocalRequestRepository(context)
                    }
                }
            }
            return localRequestRepository!!
        }
    }

    /**
     * 获取书籍目录
     */
    fun requestBookCatalog(book_id: String, book_source_id: String, book_chapter_id: String): Flowable<BasicResult<Catalog>> {
        return Flowable.create({ emitter ->

            val result = BasicResult<Catalog>()

            result.code = ResultCode.LOCAL_RESULT
            result.msg = "catalog from local"

            val catalog = Catalog()

            catalog.book_id = book_id
            catalog.book_source_id = book_source_id
            catalog.book_chapter_id = book_chapter_id

            if (checkBookSubscribe(book_id) != null) {
                val chapterDaoHelp = ChapterDataProviderHelper.loadChapterDataProviderHelper(context, book_id)
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

    fun requestChapterContent(chapter: Chapter): Flowable<BasicResult<Chapter>> {
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

    fun deleteBooksById(books: List<Book>) {
        return BookDataProviderHelper.loadBookDataProviderHelper(context = context).deleteBooksById(books)
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

    fun insertOrUpdate(user: LoginRespV4) {
        BookDataProviderHelper.loadBookDataProviderHelper(context = context).insertOrUpdate(user)
    }

    fun queryLoginUser(): LoginRespV4 {
        return BookDataProviderHelper.loadBookDataProviderHelper(context = context).queryLoginUser()
    }

    fun deleteLoginUser() {
        BookDataProviderHelper.loadBookDataProviderHelper(context = context).deleteLoginUser()
    }

    fun requestPushInfo(): Flowable<PushInfo>? {
        val pushInfo = context.getSharedObject(PushInfo.KEY, PushInfo::class.java)
        if (pushInfo != null) {
            val isSameDay = isSameDay(pushInfo.updateMillSecs, System.currentTimeMillis())
            if (isSameDay) {
                pushInfo.isFromCache = true
                return Flowable.create<PushInfo>({ emitter ->
                    emitter.onNext(pushInfo)
                    emitter.onComplete()
                }, BackpressureStrategy.BUFFER)
            }
        }
        return null
    }

    fun requestBannerTags(): Flowable<BannerInfo>? {
        val bannerInfo = context.getSharedObject(BannerInfo.KEY, BannerInfo::class.java)
        if (bannerInfo != null) {
            val isSameDay = isSameDay(bannerInfo.updateMillSecs, System.currentTimeMillis())
            if (isSameDay) {
                return Flowable.create<BannerInfo>({ emitter ->
                    emitter.onNext(bannerInfo)
                    emitter.onComplete()
                }, BackpressureStrategy.BUFFER)
            }
        }
        return null
    }

}