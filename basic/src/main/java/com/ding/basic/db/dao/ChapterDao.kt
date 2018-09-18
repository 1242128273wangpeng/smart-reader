package com.ding.basic.db.dao

import android.arch.persistence.room.*
import com.ding.basic.bean.Chapter
import io.reactivex.Flowable

/**
 * Created by yuchao on 2018/3/16 0016.
 */
@Dao
interface ChapterDao : BaseDao<Chapter> {

    /**************************增****************************/

    /**
     * 批量插入章节，如果已存在该章节，则替换
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insertChapterList(chapterList: List<Chapter>): LongArray

    /**
     * 单章插入章节，如果已存在该章节，则回滚
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChapter(chapter: Chapter): Long

    /**************************删****************************/

    /**
     * 删除表中所有数据
     */
    @Query("DELETE FROM chapters")
    fun deleteAllChapters()


    @Query("DELETE FROM chapters WHERE sequence >= :sequence")
    fun deleteChapters(sequence: Int)

    /**************************改****************************/

    /**
     * 根据章节id(主键)更新章节
     */
    @Update
    fun updateChapter(chapter: Chapter): Int

    /**
     * 更新表中所有book_chapter_id
     */
    @Query("UPDATE chapters SET book_chapter_id = :book_chapter_id")
    fun updateBookChapterId(book_chapter_id: String): Int

    /**
     * 更新表中所有book_source_id
     */
    @Query("UPDATE chapters SET book_source_id = :book_source_id")
    fun updateBookSourceId(book_source_id: String): Int

    /**************************查****************************/

    /**
     * 根据id查询章节
     */
    @Query("SELECT * FROM chapters WHERE chapter_id = :chapter_id")
    fun getChapterById(chapter_id: String): Chapter?

    /**
     * 查询章节数量
     */
    @Query("SELECT COUNT(*) FROM chapters")
    fun getCount(): Int

    /**
     * 查询章节
     */
    @Query("SELECT * FROM chapters")
    fun queryChapters(): List<Chapter>

    /**
     * 查询sequence最大的章节
     */
    @Query("SELECT * FROM chapters WHERE sequence = (SELECT MAX(sequence) FROM chapters)")
    fun queryLastChapter(): Chapter?

    /**
     * 根据sequence查询章节
     */
    @Query("SELECT * FROM chapters WHERE sequence = :sequence")
    fun queryChapterBySequence(sequence: Int): Chapter?

    /**
     * 根据sequence查询章节id
     */
    @Query("SELECT chapter_id FROM chapters WHERE sequence = :sequence")
    fun queryChapterIdBySequence(sequence: Int): String?

    /**
     * 查询章节数量
     */
    @Query("SELECT COUNT(*) FROM chapters WHERE chapter_id = :chapter_id")
    fun queryChapterCountById(chapter_id: String): Int

}