package com.ding.basic.db.repository

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.widget.Toast
import com.ding.basic.bean.*
import com.ding.basic.bean.push.BannerInfo
import com.ding.basic.bean.push.PushInfo
import com.ding.basic.db.dao.*
import com.ding.basic.db.database.BookDatabase
import com.ding.basic.db.migration.helper.MigrationDBOpenHeler
import com.ding.basic.db.migration.helper.migrateTable
import com.ding.basic.db.provider.ChapterDataProviderHelper
import com.ding.basic.net.ResultCode
import com.ding.basic.util.ChapterCacheUtil
import com.ding.basic.util.getSharedObject
import com.ding.basic.util.isSameDay
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import java.util.ArrayList

/**
 * 本地数据
 */
class LocalRequestRepository private constructor(private var context: Context,
                                                 private var bookDao: BookDao,
                                                 private var bookFixDao: BookFixDao,
                                                 private var bookmarkDao: BookmarkDao,
                                                 private var historyDao: HistoryDao,
                                                 private var searchDao: SearchDao,
                                                 private var userDao: UserDao) {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var localRequestRepository: LocalRequestRepository? = null
        private var database: BookDatabase? = null

        fun loadLocalRequestRepository(context: Context): LocalRequestRepository {
            if (localRequestRepository == null) {
                synchronized(LocalRequestRepository::class) {
                    if (localRequestRepository == null || database?.isOpen != true) {
                        database = BookDatabase.loadBookDatabase(context)
                        localRequestRepository = LocalRequestRepository(context,
                                database!!.bookDao(),
                                database!!.fixBookDao(),
                                database!!.bookmarkDao(),
                                database!!.historyDao(),
                                database!!.searchDao(),
                                database!!.userDao())
                    }
                }
            }
            return localRequestRepository!!
        }
    }

    /**
     * 升级数据库
     */
    fun upgradeFromOld(dbName: String): Flowable<Int> {
        return Flowable.create<Int>({
            try {
                val oldDB = MigrationDBOpenHeler(context, dbName).writableDatabase

                migrateTable(oldDB, "book", bookDao, Book::class.java)
                it.onNext(60)

                //其余几个表可能不存在
                try {
                    migrateTable(oldDB, "tb_history_info", historyDao, HistoryInfo::class.java)
                    it.onNext(70)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                try {
                    migrateTable(oldDB, "book_fix", bookFixDao, BookFix::class.java)
                    it.onNext(80)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                try {
                    migrateTable(oldDB, "search_recommend", searchDao, SearchRecommendBook.DataBean::class.java)
                    it.onNext(90)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                try {
                    migrateTable(oldDB, "user", userDao, LoginRespV4::class.java)
                    it.onNext(95)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                try {
                    migrateTable(oldDB, "book_mark", bookmarkDao, Bookmark::class.java)
                    it.onNext(100)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                it.onComplete()
            } catch (t: Throwable) {
                t.printStackTrace()
                it.onError(t)
            }
        }, BackpressureStrategy.BUFFER)
    }

    @Synchronized
    fun release() {
        localRequestRepository = null
        database?.close()
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

    /**
     * 检查书籍是否存在
     */
    fun checkBookSubscribe(book_id: String): Book? {
        return bookDao.checkBookSubscribe(book_id)
    }

    @Synchronized
    fun insertBook(book: Book?): Long {
        val MAX_COUNT = 49
        if (book == null || TextUtils.isEmpty(book.book_id) || TextUtils.isEmpty(book.book_source_id)) {
            Toast.makeText(context, "订阅失败，资源有误", Toast.LENGTH_SHORT).show()
            return 0
        }
        if (loadBookCount() > MAX_COUNT) {
            Toast.makeText(context, "书架已满，请整理书架", Toast.LENGTH_SHORT).show()
            return 0
        } else if (checkBookSubscribe(book.book_id) != null) {
            Toast.makeText(context, "已在书架中", Toast.LENGTH_SHORT).show()
            return 0
        } else if (TextUtils.isEmpty(book.book_id) || book.name == null || book.name == "") {
            Toast.makeText(context, "订阅失败，资源有误", Toast.LENGTH_SHORT).show()
            return 0
        } else {
            book.insert_time = System.currentTimeMillis()
            val n = bookDao.insertBook(book)
            return if (n > 0) {
                if (book.sequence < -1) {
                    book.sequence = -1
                }
                n
            } else {
                0
            }
        }
    }

    fun updateBook(book: Book): Boolean {
        if (book.id <= 0) {
            val interimBook = bookDao.loadBook(book.book_id) ?: return false
            book.id = interimBook.id
        }
        return bookDao.updateBook(book) != -1
    }

    fun updateBooks(books: List<Book>): Boolean {
        return bookDao.updateBooks(books) != -1
    }

    @Synchronized
    fun deleteBook(book_id: String): Boolean {
        val result = bookDao.deleteBook(book_id) != -1
        ChapterDataProviderHelper.deleteDataBase(book_id, context)
        deleteBookFix(book_id)
        return result
    }

    @Synchronized
    fun deleteBooks(books: List<Book>) {
        books.forEach {
            deleteBook(it.book_id)
        }
    }

    fun deleteBooksById(books: List<Book>) {
        books.forEach {
            bookDao.deleteBookById(it.id)
        }
    }

    fun deleteShelfBooks() {
        return bookDao.deleteShelfBooks()
    }

    fun loadBook(book_id: String): Book? {
        return bookDao.loadBook(book_id)
    }

    fun loadBooks(): List<Book>? {
        return bookDao.loadBooks()
    }

    fun loadReadBooks(): List<Book>? {
        return bookDao.loadReadBooks()
    }

    fun loadBookCount(): Long {
        return bookDao.loadBookCount()
    }

    fun insertBooks(books: List<Book>) {
        bookDao.insertBooks(books)
    }

    fun loadBookShelfIDs(): String {
        val stringBuilder = StringBuilder()
        return stringBuilder.toString()
    }

    fun insertBookFix(bookFix: BookFix) {
        return bookFixDao.insertBookFix(bookFix)
    }

    @Synchronized
    fun deleteBookFix(id: String) {
        if (loadBookFix(id) != null) {
            bookFixDao.deleteBookFix(id)
        }
    }

    fun loadBookFixs(): List<BookFix>? {
        return bookFixDao.loadBookFixs()
    }

    fun loadBookFix(book_id: String): BookFix? {
        return bookFixDao.loadBookFix(book_id)
    }

    fun updateBookFix(bookFix: BookFix) {
        return bookFixDao.updateBookFix(bookFix)
    }

    fun insertOrUpdate(user: LoginRespV4) {
        userDao.insertOrUpdate(user)
    }

    fun queryLoginUser(): LoginRespV4 {
        return userDao.queryUserInfo()
    }

    fun deleteLoginUser() {
        userDao.deleteUsers()
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

    @Synchronized
    fun queryHistoryPaging(startNum: Long, limtNum: Long): ArrayList<HistoryInfo> {
        return historyDao.queryByLimt(startNum, limtNum) as ArrayList<HistoryInfo>
    }

    @Synchronized
    fun deleteAllHistory() {
        historyDao.deleteAllHistory()
    }

    @Synchronized
    fun insertHistoryInfo(hisInfo: HistoryInfo) {
        historyDao.insertHistoryInfo(hisInfo)
    }

    @Synchronized
    fun deleteAllBookMark() {
        bookmarkDao.deleteAllMarks()
    }

    @Synchronized
    fun insertBookMark(bookMark: Bookmark) {
        bookmarkDao.insertOrUpdate(bookMark)
    }

    @Synchronized
    fun getBookMarks(book_id: String): ArrayList<Bookmark> {
        return bookmarkDao.queryBookmarkByBookId(book_id) as ArrayList<Bookmark>
    }

    @Synchronized
    fun deleteBookMark(book_id: String) {
        bookmarkDao.deleteByBookId(book_id)
    }

    @Synchronized
    fun deleteBookMark(ids: ArrayList<Int>) {
        ids.forEach {
            bookmarkDao.deleteById(it)
        }
    }

    @Synchronized
    fun deleteBookMark(book_id: String, sequence: Int, offset: Int) {
        bookmarkDao.deleteByExatly(book_id, sequence, offset)
    }

    @Synchronized
    fun isBookMarkExist(book_id: String, sequence: Int, offset: Int): Boolean {
        return bookmarkDao.queryBookmarkCount(book_id, sequence, offset) > 0
    }

    @Synchronized
    fun getHistoryCount(): Long {
        return historyDao.getCount()
    }

    @Synchronized
    fun insertOrUpdateHistory(historyInfo: HistoryInfo): Boolean {
        return historyDao.insertHistoryInfo(historyInfo) > 0
    }

    @Synchronized
    fun deleteSmallTimeHistory() {
        historyDao.deleteSmallTime()
    }


}