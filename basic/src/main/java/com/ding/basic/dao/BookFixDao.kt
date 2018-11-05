package com.ding.basic.dao

import android.arch.persistence.room.*
import com.ding.basic.bean.BookFix

/**
 * Created on 2018/3/20.
 * Created by crazylei.
 */
@Dao
interface BookFixDao :BaseDao<BookFix>{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBookFix(bookFix: BookFix)

    @Query("DELETE FROM book_fix WHERE book_id = :id")
    @Throws(Exception::class)
    fun deleteBookFix(id: String)

    @Query("SELECT * FROM book_fix")
    fun loadBookFixs(): List<BookFix>?

    @Query("SELECT * FROM book_fix WHERE book_id = :book_id")
    fun loadBookFix(book_id: String): BookFix?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateBookFix(bookFix: BookFix)
}