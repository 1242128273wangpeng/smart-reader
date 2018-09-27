package com.ding.basic.db.dao

import android.arch.persistence.room.*
import android.util.Log
import com.ding.basic.bean.Book
import io.reactivex.Single

/**
 * Created on 2018/3/13.
 * Created by crazylei.
 */
@Dao
interface BookDao :BaseDao<Book>{

    @Query("SELECT * FROM book WHERE book_id = :book_id")
    fun checkBookSubscribe(book_id: String): Book?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBook(book: Book): Long

    @Update
    fun updateBook(book: Book): Int

    @Update
    @JvmSuppressWildcards
    fun updateBooks(books: List<Book>): Int

    @Query("DELETE FROM book WHERE book_id = :book_id")
    fun deleteBook(book_id: String): Int

    @Query("DELETE FROM book WHERE id = :id")
    fun deleteBookById(id: Int): Int

    @Query("SELECT * FROM book WHERE book_id = :book_id")
    fun loadBook(book_id: String): Book?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insertBooks(books: List<Book>)

    @Delete
    @JvmSuppressWildcards
    fun deleteBooks(books: List<Book>)

    @Query("DELETE FROM book")
    fun deleteShelfBooks()

    @Query("SELECT * FROM book")
    fun loadBooks(): List<Book>?

    @Query("SELECT * FROM book WHERE readed = 1")
    fun loadReadBooks(): List<Book>?

    @Query("SELECT COUNT(*) FROM book")
    fun loadBookCount(): Long
}