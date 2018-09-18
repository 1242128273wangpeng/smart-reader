package com.ding.basic.db.provider.impl

import android.content.Context
import com.ding.basic.bean.*
import com.ding.basic.db.dao.ChapterDao
import com.ding.basic.db.provider.ChapterDataProvider
import com.ding.basic.db.migration.helper.MigrationDBOpenHeler
import com.ding.basic.db.migration.helper.migrateTable
import com.ding.basic.db.database.ChaptersDatabase
import com.orhanobut.logger.Logger
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import java.lang.ref.WeakReference

/**
 * Created by yuchao on 2018/3/16 0016.
 */
class ChapterDataProviderHelper private constructor(private val chapterDao: ChapterDao) : ChapterDataProvider {

    companion object {

        private val PROVIDER_HELPER_MAP: HashMap<String, WeakReference<ChapterDataProviderHelper>> = HashMap()
        private val dbMap: HashMap<String, ChaptersDatabase> = HashMap()

        @Synchronized
        fun loadChapterDataProviderHelper(context: Context, book_id: String): ChapterDataProviderHelper {
            val daoHelper = PROVIDER_HELPER_MAP[book_id]?.get()
            if (daoHelper != null) {
                Logger.d("use cached chapter helper")
                return daoHelper
            }

            PROVIDER_HELPER_MAP.filter {
                it.value.get() == null
            }.forEach {
                if (dbMap[it.key]?.isOpen == true && dbMap[it.key]?.inTransaction() != true) {
                    PROVIDER_HELPER_MAP.remove(it.key)
                    dbMap.remove(it.key)?.close()
                    Logger.d("release chapter helper -> ${it.key}")
                }
            }
            Logger.d("new chapter helper")
            val chapterDatabase = ChaptersDatabase.loadChapterDatabase(context, book_id)
            val helper = ChapterDataProviderHelper(chapterDatabase.chapterDao())

            dbMap[book_id] = chapterDatabase
            PROVIDER_HELPER_MAP[book_id] = WeakReference(helper)

            return helper
        }


        fun upgradeFromOld(context: Context, book_ids: List<String>): Flowable<Int> {
            return Flowable.create<Int>({

                try {
                    for (i in 0 until book_ids.size) {
                        Logger.e("migrateDB book_chapter_${book_ids[i]}")
                        val oldDB = MigrationDBOpenHeler(context, "book_chapter_${book_ids[i]}").writableDatabase
                        val chapterDao = loadChapterDataProviderHelper(context, book_ids[i]).chapterDao

                        migrateTable(oldDB, "chapter", chapterDao, Chapter::class.java)

                        //context.deleteDatabase("book_chapter_${book_ids[i]}")

                        it.onNext((100F * (i + 1) / book_ids.size).toInt())
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
            dbMap.forEach {
                it.value.close()
            }

            dbMap.clear()
            PROVIDER_HELPER_MAP.clear()
        }

        fun deleteDataBase(book_id: String, context: Context) {

            dbMap.remove(book_id)?.close()
            PROVIDER_HELPER_MAP.remove(book_id)

            context.deleteDatabase(ChaptersDatabase.CHAPTER_DATABASE + book_id + ".db")
        }
    }

    @Synchronized
    override fun insertOrUpdateChapter(chapterList: List<Chapter>): Boolean {
        if (chapterList.isNotEmpty()) {
            var isSuc = false
            if (getCount() > 0) {
                val c = chapterDao.queryLastChapter()
                if (c != null) {
                    var count = c.sequence
                    val arr = arrayListOf<Chapter>()
                    chapterList.forEach {
                        if (chapterDao.queryChapterCountById(it.chapter_id) <= 0) {
                            count++
                            it.sequence = count
                            arr.add(it)
                        }
                    }
                    if (arr.isNotEmpty()) {
                        if (chapterDao.insertChapterList(arr).isNotEmpty()) {
                            isSuc = true
                        }
                    }
                }
            } else {
                var count = -1
                chapterList.forEach {
                    count++
                    it.sequence = count
                }
                if (chapterDao.insertChapterList(chapterList).isNotEmpty()) {
                    isSuc = true
                }
            }
            if (isSuc) {
                return true
            }
        }
        return false
    }

    @Synchronized
    override fun deleteAllChapters() {
        chapterDao.deleteAllChapters()
    }

    @Synchronized
    override fun deleteChapters(sequence: Int) {
        chapterDao.deleteChapters(sequence)
    }

    @Synchronized
    override fun updateChapter(chapter: Chapter): Boolean {
        return chapterDao.updateChapter(chapter) > 0
    }

    @Synchronized
    override fun getChapterById(chapter_id: String): Chapter? {
        var chapter: Chapter? = null
        chapter = chapterDao.getChapterById(chapter_id)
        Logger.v("getChapterById, chapter.name = " + chapter?.name)
        return chapter
    }

    @Synchronized
    override fun getCount(): Int {
        return chapterDao.getCount()
    }

    @Synchronized
    override fun queryAllChapters(): List<Chapter> {
        val chapters = arrayListOf<Chapter>()
        chapters.addAll(chapterDao.queryChapters())
        Logger.v("queryAllChapters, chapters.size = " + chapters.size)
        return chapters
    }

    @Synchronized
    override fun queryLastChapter() = chapterDao.queryLastChapter()


    @Synchronized
    override fun queryChapterBySequence(sequence: Int) = chapterDao.queryChapterBySequence(sequence)

    /**
     * 根据章节sequence更新章节
     */
    @Synchronized
    fun updateChapterBySequence(chapter: Chapter) {
        val id = chapterDao.queryChapterIdBySequence(chapter.sequence)
        if (!id.isNullOrBlank()) {
            chapter.chapter_id = id!!
            updateChapter(chapter)
        }
    }

    @Synchronized
    fun updateBookChapterId(book_chapter_id: String) {
        chapterDao.updateBookChapterId(book_chapter_id)
    }

    @Synchronized
    fun updateBookSourceId(book_source_id: String) {
        chapterDao.updateBookSourceId(book_source_id)
    }
}