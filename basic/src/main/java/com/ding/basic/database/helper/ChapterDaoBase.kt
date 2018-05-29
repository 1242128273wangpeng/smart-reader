package com.ding.basic.database.helper

import com.ding.basic.bean.Chapter
import com.ding.basic.dao.BaseDao
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers

/**
 * Created by yuchao on 2018/3/16 0016.
 */
interface ChapterDaoBase {

    /**************************增****************************/

    /**
     * 批量插入章节，如果已存在该章节，则替换
     */
    fun insertOrUpdateChapter(chapterList: List<Chapter>): Boolean

    /**************************删****************************/

    /**
     * 删除表中所有数据
     */
    fun deleteAllChapters()

    /**************************改****************************/

    /**
     * 根据章节id(主键)更新章节
     */
    fun updateChapter(chapter: Chapter): Boolean

    /**************************查****************************/

    /**
     * 根据id查询章节
     */
    fun getChapterById(chapter_id: String): Chapter?

    /**
     * 查询章节数量
     */
    fun getCount(): Int

    /**
     * 查询章节
     */
    fun queryAllChapters(): List<Chapter>?

    /**
     * 查询sequence最大的章节
     */
    fun queryLastChapter(): Chapter?

    /**
     * 根据sequence查询章节
     */
    fun queryChapterBySequence(sequence: Int): Chapter?

}