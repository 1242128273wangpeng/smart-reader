package com.ding.basic.db.provider

import android.content.Context
import android.text.TextUtils
import android.widget.Toast
import com.ding.basic.bean.*
import com.ding.basic.db.migration.helper.MigrationDBOpenHeler
import com.ding.basic.db.migration.helper.migrateTable
import com.ding.basic.db.dao.*
import com.ding.basic.db.database.BookDatabase
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import java.util.*

/**
 * Created on 2018/3/16.
 * Created by crazylei.
 */
class BookDataProviderHelper private constructor(private var bookDao: BookDao,
                                                 private var bookFixDao: BookFixDao,
                                                 private var bookmarkDao: BookmarkDao,
                                                 private var historyDao: HistoryDao,
                                                 private var searchDao: SearchDao,
                                                 private var userDao: UserDao) {

    companion object {
        private var database: BookDatabase? = null
        private var bookInterfaceHelper: BookDataProviderHelper? = null

        @Synchronized
        fun loadBookDataProviderHelper(context: Context): BookDataProviderHelper {
            if (database?.isOpen != true) {
                database = BookDatabase.loadBookDatabase(context = context)
                bookInterfaceHelper = BookDataProviderHelper(
                        database!!.bookDao(),
                        database!!.fixBookDao(),
                        database!!.bookmarkDao(),
                        database!!.historyDao(),
                        database!!.searchDao(),
                        database!!.userDao())

            }

            return bookInterfaceHelper!!
        }

        fun upgradeFromOld(context: Context, dbName: String): Flowable<Int> {
            return Flowable.create<Int>({

                val providerHelper = loadBookDataProviderHelper(context)

                try {
                    val oldDB = MigrationDBOpenHeler(context, dbName).writableDatabase

                    migrateTable(oldDB, "book", providerHelper.bookDao, Book::class.java)
                    it.onNext(60)

                    //其余几个表可能不存在
                    try {
                        migrateTable(oldDB, "tb_history_info", providerHelper.historyDao, HistoryInfo::class.java)
                        it.onNext(70)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    try {
                        migrateTable(oldDB, "book_fix", providerHelper.bookFixDao, BookFix::class.java)
                        it.onNext(80)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    try {
                        migrateTable(oldDB, "search_recommend", providerHelper.searchDao, SearchRecommendBook.DataBean::class.java)
                        it.onNext(90)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    try {
                        migrateTable(oldDB, "user", providerHelper.userDao, LoginRespV4::class.java)
                        it.onNext(95)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    try {
                        migrateTable(oldDB, "book_mark", providerHelper.bookmarkDao, Bookmark::class.java)
                        it.onNext(100)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                    //context.deleteDatabase(dbName)

                    it.onComplete()
                } catch (t: Throwable) {
                    t.printStackTrace()
                    it.onError(t)
                }
            }, BackpressureStrategy.BUFFER)
        }

        @Synchronized
        fun release() {
            bookInterfaceHelper = null
            database?.close()
        }
    }

    fun checkBookSubscribe(book_id: String): Book? {
        return bookDao.checkBookSubscribe(book_id)
    }

    @Synchronized
    fun insertBook(book: Book, context: Context): Long {
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
    fun deleteBook(book_id: String, context: Context): Boolean {
        val result = bookDao.deleteBook(book_id) != -1
        ChapterDataProviderHelper.deleteDataBase(book_id, context)
        deleteBookFix(book_id)
        return result
    }

    @Synchronized
    fun deleteBooks(books: List<Book>, context: Context) {
        books.forEach {
            deleteBook(it.book_id, context)
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


    @Synchronized
    fun deleteBookMark(ids: ArrayList<Int>) {
        ids.forEach {
            bookmarkDao.deleteById(it)
        }
    }

    @Synchronized
    fun deleteBookMark(book_id: String) {
        bookmarkDao.deleteByBookId(book_id)
    }

    @Synchronized
    fun insertBookMark(bookMark: Bookmark) {
        bookmarkDao.insertBookmark(bookMark)
    }

    @Synchronized
    fun isBookMarkExist(book_id: String, sequence: Int, offset: Int): Boolean {
        return bookmarkDao.queryBookmarkCount(book_id, sequence, offset) > 0
    }

    @Synchronized
    fun deleteBookMark(book_id: String, sequence: Int, offset: Int) {
        bookmarkDao.deleteByExatly(book_id, sequence, offset)
    }
    @Synchronized
    fun deleteAllBookMark(){
        bookmarkDao.deleteAllMarks()
    }

    @Synchronized
    fun getBookMarks(book_id: String): ArrayList<Bookmark> {
        return bookmarkDao.queryBookmarkByBookId(book_id) as ArrayList<Bookmark>
    }

    @Synchronized
    fun getHistoryCount(): Long {
        return historyDao.getCount()
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
    fun insertHistoryInfo(hisInfo:HistoryInfo){
        historyDao.insertHistoryInfo(hisInfo)
    }

    @Synchronized
    fun insertOrUpdateHistory(historyInfo: HistoryInfo): Boolean {
        return historyDao.insertHistoryInfo(historyInfo) > 0
    }

    @Synchronized
    fun deleteSmallTimeHistory() {
        historyDao.deleteSmallTime()
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


}