package net.lzbook.kit.data.db.help

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.ding.basic.bean.*
import com.ding.basic.dao.ChapterDao
import com.ding.basic.database.helper.ChapterDaoBase
import com.ding.basic.database.helper.MigrationDBOpenHeler
import com.ding.basic.database.helper.migrateTable
import com.example.android.observability.persistence.ChaptersDatabase
import com.orhanobut.logger.Logger
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import java.lang.ref.WeakReference

/**
 * Created by yuchao on 2018/3/16 0016.
 */
class ChapterDaoHelper private constructor(private val chapterDao: ChapterDao) : ChapterDaoBase {

    companion object {

        private val helperMap: HashMap<String, WeakReference<ChapterDaoHelper>> = HashMap()
        private val dbMap: HashMap<String, ChaptersDatabase> = HashMap()

        @Synchronized
        fun loadChapterDataProviderHelper(context: Context, book_id: String): ChapterDaoHelper {
            val daoHelper = helperMap[book_id]?.get()
            if (daoHelper != null) {
                Logger.d("use cached chapter helper")
                return daoHelper
            }

            helperMap.filter {
                it.value.get() == null
            }.forEach {
                if(dbMap[it.key]?.isOpen == true && dbMap[it.key]?.inTransaction() != true) {
                    helperMap.remove(it.key)
                    dbMap.remove(it.key)?.close()
                    Logger.d("release chapter helper -> ${it.key}")
                }
            }
            Logger.d("new chapter helper")
            val chapterDatabase = ChaptersDatabase.loadChapterDatabase(context, "$book_id")
            val helper = ChapterDaoHelper(chapterDatabase.chapterDao())

            dbMap.put(book_id, chapterDatabase)
            helperMap.put(book_id, WeakReference(helper))

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

        @Synchronized fun release(){
            dbMap.forEach {
                it.value.close()
            }

            dbMap.clear()
            helperMap.clear()
        }

        fun deleteDataBase(book_id: String, context: Context){

            dbMap.remove(book_id)?.close()
            helperMap.remove(book_id)

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
                        if (chapterDao.queryChapterCountById(it.chapter_id) <= 0){
                            count++
                            it.sequence = count
                            arr.add(it)
                        }
                    }
                    if (arr.isNotEmpty()){
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
        var chapters = arrayListOf<Chapter>()
        chapters.addAll(chapterDao.queryChapters())
        Logger.v("queryAllChapters, chapters.size = " + chapters.size)
        return chapters
    }

    @Synchronized
    override fun queryLastChapter(): Chapter? {
        var chapter: Chapter? = null
        chapter = chapterDao.queryLastChapter()
//        Logger.v("queryLastChapter, chapter.name = " + chapter?.name)
        return chapter
    }

    @Synchronized
    override fun queryChapterBySequence(sequence: Int): Chapter? {
        var chapter: Chapter? = null
        chapter = chapterDao.queryChapterBySequence(sequence)
//        Logger.v("queryChapterBySequence, chapter.name = " + chapter?.name)
        return chapter
    }

    /**
     * 根据章节sequence更新章节
     */
    @Synchronized
    fun updateChapterBySequence(chapter: Chapter) {
        var id = chapterDao.queryChapterIdBySequence(chapter.sequence)
        if (id != null && "" != id) {
            chapter.chapter_id = id
            updateChapter(chapter)
        }
    }

    @Synchronized
    fun updateBookChapterId(book_chapter_id: String){
        chapterDao.updateBookChapterId(book_chapter_id)
    }

    @Synchronized
    fun updateBookSourceId(book_source_id: String){
        chapterDao.updateBookSourceId(book_source_id)
    }
}