package com.ding.basic.database.helper

import android.content.Context
import android.text.TextUtils
import android.widget.Toast
import com.ding.basic.bean.*
import com.ding.basic.dao.*
import com.ding.basic.database.BookDatabase
import com.ding.basic.database.provider.BookDataProvider
import com.ding.basic.rx.SchedulerHelper
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.util.*
import net.lzbook.kit.data.db.help.ChapterDaoHelper

/**
 * Created on 2018/3/16.
 * Created by crazylei.
 */
class BookDataProviderHelper private constructor(private var bookdao: BookDao,
                                                 private var bookFixDao: BookFixDao,
                                                 private var bookmarkDao: BookmarkDao,
                                                 private var historyDao: HistoryDao,
                                                 private var searchDao: SearchDao) : BookDataProvider {

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
                        database!!.searchDao())

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
                    }catch (e:Exception){
                        e.printStackTrace()
                    }

                    try {
                        migrateTable(oldDB, "book_fix", providerHelper.bookFixDao, BookFix::class.java)
                        it.onNext(80)
                    }catch (e:Exception){
                        e.printStackTrace()
                    }

                    try {
                        migrateTable(oldDB, "search_recommend", providerHelper.searchDao, SearchRecommendBook.DataBean::class.java)
                        it.onNext(90)
                    }catch (e:Exception){
                        e.printStackTrace()
                    }

                    try {
                        migrateTable(oldDB, "book_mark", providerHelper.bookmarkDao, Bookmark::class.java)
                        it.onNext(100)
                    }catch (e:Exception){
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

    override fun checkBookSubscribe(book_id: String): Book? {
        return bookdao.checkBookSubscribe(book_id)
    }

    @Synchronized
    override fun insertBook(book: Book, context: Context): Long {
        val MAX_COUNT = 49
        if (TextUtils.isEmpty(book.book_id) || TextUtils.isEmpty(book.book_source_id)) {
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
        return bookdao.updateBook(book) != -1
    }

    override fun updateBooks(books: List<Book>): Boolean {
        return bookdao.updateBooks(books) != -1
    }

    @Synchronized
    override fun deleteBook(book_id: String, context: Context): Boolean {
        var isSuc = bookdao.deleteBook(book_id) != -1
        ChapterDaoHelper.deleteDataBase(book_id, context)
        deleteBookFix(book_id)
        return isSuc
    }

    @Synchronized
    override fun deleteBooks(books: List<Book>, context: Context) {
        books.forEach {
            deleteBook(it.book_id, context)
        }
    }

    override fun deleteShelfBooks() {
        return bookdao.deleteShelfBooks()
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

//        val books = bookdao.loadBooks()
//
//        for (i in books.indices) {
//            stringBuilder.append(books[i].id)
//            stringBuilder.append(if (i == books.size - 1) "" else ",")
//        }

        return stringBuilder.toString()
    }


    override fun insertBookFix(bookFix: BookFix) {
        return bookFixDao.insertBookFix(bookFix)
    }

    @Synchronized
    override fun deleteBookFix(id: String) {
        if (loadBookFix(id) != null) {
            bookFixDao.deleteBookFix(id)
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
        ids.forEach({
            bookmarkDao.deleteById(it)
        })
    }

    @Synchronized
    override fun deleteBookMark(book_id: String) {
        bookmarkDao.deleteByBookId(book_id)
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
        bookmarkDao.deleteByExatly(book_id, sequence, offset)
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
        historyDao.deleteAllHistory()
    }

    @Synchronized
    override fun insertOrUpdateHistory(historyInfo: HistoryInfo): Boolean {
        return historyDao.insertHistoryInfo(historyInfo) > 0
    }

    @Synchronized
    override fun deleteSmallTimeHistory() {
        historyDao.deleteSmallTime()
    }

}