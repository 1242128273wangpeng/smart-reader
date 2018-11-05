package com.ding.basic.database.helper

import android.content.Context
import android.text.TextUtils
import android.widget.Toast
import com.ding.basic.bean.*
import com.ding.basic.dao.*
import com.ding.basic.database.BookDatabase
import com.ding.basic.database.provider.BookDataProvider
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import net.lzbook.kit.data.db.help.ChapterDaoHelper
import java.util.*

/**
 * Created on 2018/3/16.
 * Created by crazylei.
 */
class BookDataProviderHelper private constructor(private var bookdao: BookDao,
                                                 private var bookFixDao: BookFixDao,
                                                 private var bookmarkDao: BookmarkDao,
                                                 private var historyDao: HistoryDao,
                                                 private var searchDao: SearchDao,
                                                 private var userDao: UserDao) : BookDataProvider {

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

                    migrateTable(oldDB, "book", providerHelper.bookdao, Book::class.java)
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

        const val INSERT_BOOKSHELF_FULL = -50L
    }

    override fun checkBookSubscribe(book_id: String): Book? {
        return bookdao.checkBookSubscribe(book_id)
    }

    @Synchronized
    override fun insertBook(book: Book, context: Context): Long {
        val MAX_COUNT = 49
        if (book == null || TextUtils.isEmpty(book.book_id) || TextUtils.isEmpty(book.book_source_id)) {
            Toast.makeText(context, "订阅失败，资源有误", Toast.LENGTH_SHORT).show()
            return 0
        }
        if (loadBookCount()!! > MAX_COUNT) {
            Toast.makeText(context, "书架已满，请整理书架", Toast.LENGTH_SHORT).show()
            return INSERT_BOOKSHELF_FULL
        } else if (checkBookSubscribe(book.book_id) != null) {
            Toast.makeText(context, "已在书架中", Toast.LENGTH_SHORT).show()
            return 0
        } else if (TextUtils.isEmpty(book.book_id) || book.name == null || book.name == "") {
            Toast.makeText(context, "订阅失败，资源有误", Toast.LENGTH_SHORT).show()
            return 0
        } else {
            book.insert_time = System.currentTimeMillis()
            var n = bookdao.insertBook(book)
            if (n > 0) {
                if (book.sequence < -1) {
                    book.sequence = -1
                }
                return n
            } else {
                return 0
            }
        }
    }

    override fun updateBook(book: Book): Boolean {
        if (book.id <= 0) {
            val interimBook = bookdao.loadBook(book.book_id) ?: return false
            book.id = interimBook.id
        }
        return bookdao.updateBook(book) != -1
    }

    override fun updateBooks(books: List<Book>): Boolean {
        return bookdao.updateBooks(books) != -1
    }

    @Synchronized
    override fun deleteBook(book_id: String, context: Context): Boolean {
        return try {
            val result = bookdao.deleteBook(book_id) != -1
            ChapterDaoHelper.deleteDataBase(book_id, context)
            deleteBookFix(book_id)
            result
        } catch (exception: Exception) {
            exception.printStackTrace()
            false
        }
    }

    @Synchronized
    override fun deleteBooks(books: List<Book>, context: Context) {
        books.forEach {
            deleteBook(it.book_id, context)
        }
    }

    override fun deleteBooksById(books: List<Book>) {
        try {
            books.forEach {
                bookdao.deleteBookById(it.id)
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    override fun deleteShelfBooks() {
        try {
            return bookdao.deleteShelfBooks()
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    override fun loadBook(book_id: String): Book? {
        return bookdao.loadBook(book_id)
    }

    override fun loadBooks(): List<Book>? {
        return bookdao.loadBooks()
    }

    override fun loadReadBooks(): List<Book>? {
        return bookdao.loadReadBooks()
    }

    override fun loadBookCount(): Long {
        return bookdao.loadBookCount()
    }

    override fun insertBooks(books: List<Book>) {
        bookdao.insertBooks(books)
    }


    override fun loadBookShelfIDs(): String {
        val stringBuilder = StringBuilder()
        return stringBuilder.toString()
    }


    override fun insertBookFix(bookFix: BookFix) {
        return bookFixDao.insertBookFix(bookFix)
    }

    @Synchronized
    override fun deleteBookFix(id: String) {
        try {
            if (loadBookFix(id) != null) {
                bookFixDao.deleteBookFix(id)
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    override fun loadBookFixs(): List<BookFix>? {
        return bookFixDao.loadBookFixs()
    }

    override fun loadBookFix(book_id: String): BookFix? {
        return bookFixDao.loadBookFix(book_id)
    }

    override fun updateBookFix(bookFix: BookFix) {
        return bookFixDao.updateBookFix(bookFix)
    }


    @Synchronized
    override fun deleteBookMark(ids: ArrayList<Int>) {
        try {
            ids.forEach({
                bookmarkDao.deleteById(it)
            })
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    @Synchronized
    override fun deleteBookMark(book_id: String) {
        try {
            bookmarkDao.deleteByBookId(book_id)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    @Synchronized
    override fun insertBookMark(bookMark: Bookmark) {
        bookmarkDao.insertBookmark(bookMark)
    }

    @Synchronized
    override fun isBookMarkExist(book_id: String, sequence: Int, offset: Int): Boolean {
        return bookmarkDao.queryBookmarkCount(book_id, sequence, offset) > 0
    }

    @Synchronized
    override fun deleteBookMark(book_id: String, sequence: Int, offset: Int) {
        try {
            bookmarkDao.deleteByExatly(book_id, sequence, offset)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    @Synchronized
    fun deleteAllBookMark(){
        try {
            bookmarkDao.deleteAllMarks()
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    @Synchronized
    override fun getBookMarks(book_id: String): ArrayList<Bookmark> {
        return bookmarkDao.queryBookmarkByBookId(book_id) as ArrayList<Bookmark>
    }

    @Synchronized
    override fun getHistoryCount(): Long {
        return historyDao.getCount()
    }

    @Synchronized
    override fun queryHistoryPaging(startNum: Long, limtNum: Long): ArrayList<HistoryInfo> {
        return historyDao.queryByLimt(startNum, limtNum) as ArrayList<HistoryInfo>
    }

    @Synchronized
    override fun deleteAllHistory() {
        try {
            historyDao.deleteAllHistory()
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    @Synchronized
    fun insertHistoryInfo(hisInfo:HistoryInfo){
        historyDao.insertHistoryInfo(hisInfo)
    }

    @Synchronized
    override fun insertOrUpdateHistory(historyInfo: HistoryInfo): Boolean {
        return historyDao.insertHistoryInfo(historyInfo) > 0
    }

    @Synchronized
    override fun deleteSmallTimeHistory() {
        try {
            historyDao.deleteSmallTime()
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    fun insertOrUpdate(user: LoginRespV4) {
        userDao.insertOrUpdate(user)
    }

    fun queryLoginUser(): LoginRespV4 {
        return userDao.queryUserInfo()
    }

    fun deleteLoginUser() {
        try {
            userDao.deleteUsers()
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }


}