package com.ding.basic.dao

import android.arch.persistence.room.*
import com.ding.basic.bean.Book
import com.ding.basic.bean.Bookmark
import com.ding.basic.bean.Chapter
import io.reactivex.Flowable

/**
 * Created by yuchao on 2018/3/16 0016.
 */
@Dao
interface BookmarkDao : BaseDao<Bookmark> {

    /**************************增****************************/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBookmark(mark: Bookmark)

    /**************************删****************************/

    @Query("DELETE FROM book_mark WHERE ID = :id")
    fun deleteById(id: Int)

    @Query("DELETE FROM book_mark WHERE book_id = :book_id")
    fun deleteByBookId(book_id: String)

    @Query("DELETE FROM book_mark WHERE book_id = :book_id AND sequence = :sequence AND offset = :offset")
    fun deleteByExatly(book_id: String, sequence: Int, offset: Int)

    /**************************改****************************/


    /**************************查****************************/

    @Query("SELECT COUNT(*) FROM book_mark WHERE book_id = :book_id AND sequence = :sequence AND offset = :offset")
    fun queryBookmarkCount(book_id: String, sequence: Int, offset: Int): Long

    @Query("SELECT * FROM book_mark WHERE book_id = :book_id")
    fun queryBookmarkByBookId(book_id: String): List<Bookmark>

}