package com.ding.basic.database.provider

import android.content.Context
import com.ding.basic.bean.Book
import com.ding.basic.bean.BookFix
import com.ding.basic.bean.Bookmark
import com.ding.basic.bean.HistoryInfo

/**
 * Created on 2018/3/16.
 * Created by crazylei.
 */
interface BookDataProvider {

    fun checkBookSubscribe(book_id: String): Book?

    fun insertBook(book: Book, context: Context): Long?

    fun updateBook(book: Book): Boolean

    fun updateBooks(books: List<Book>): Boolean

    fun deleteBook(book_id: String, context: Context): Boolean

    fun deleteBooks(books: List<Book>, context: Context)

    fun deleteShelfBooks()

    fun loadBook(book_id: String): Book?

    fun loadBooks(): List<Book>?

    fun loadReadBooks(): List<Book>?

    fun loadBookCount(): Long?

    fun insertBooks(books: List<Book>)

    fun loadBookShelfIDs(): String


    fun insertBookFix(bookFix: BookFix)

    fun deleteBookFix(id: String)

    fun loadBookFixs(): List<BookFix>?

    fun loadBookFix(book_id: String): BookFix?

    fun updateBookFix(bookFix: BookFix)


    fun deleteBookMark(ids: ArrayList<Int>)

    fun deleteBookMark(book_id: String)

    fun insertBookMark(bookMark: Bookmark)

    fun isBookMarkExist(book_id: String, sequence: Int, offset: Int): Boolean

    fun deleteBookMark(book_id: String, sequence: Int, offset: Int)

    fun getBookMarks(book_id: String): ArrayList<Bookmark>


    fun getHistoryCount(): Long

    fun queryHistoryPaging(startNum: Long, limtNum: Long): ArrayList<HistoryInfo>

    fun deleteAllHistory()

    fun insertOrUpdateHistory(historyInfo: HistoryInfo): Boolean

    fun deleteSmallTimeHistory()
}