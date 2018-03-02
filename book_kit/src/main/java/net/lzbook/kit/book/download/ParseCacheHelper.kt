package net.lzbook.kit.book.download

import android.text.TextUtils
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.db.BookChapterDao
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.request.DataCache
import net.lzbook.kit.request.WriteFileFailException
import net.lzbook.kit.request.own.OtherRequestChapterExecutor
import java.util.*

/**
 * Created by xian on 17-6-6.
 */

private fun buildChapterMap(chapterList: ArrayList<Chapter>): LinkedHashMap<String, Chapter> {
    val chapterMap = LinkedHashMap<String, Chapter>()
    for (i in chapterList.indices) {

        val chapter = chapterList[i]
        val key = chapter.book_id + "_" + chapter.sequence
        chapterMap.put(key, chapter)

    }
    return chapterMap
}

@Throws(Exception::class)
fun getChapterFromPackage(chapterIdList: ArrayList<String>, chapterList: ArrayList<Chapter>, bookChapterDao: BookChapterDao, bookDaoHelper: BookDaoHelper, p2: PackChapterProto.PackChapter): Chapter? {

    val retChapter = parseSingleChapter(chapterIdList, chapterList, p2)
    if (retChapter != null && !TextUtils.isEmpty(retChapter.content)) {
        retChapter.isSuccess = true
        // 自动切源需要就更新目录
        if (retChapter.flag == 1 && !TextUtils.isEmpty(retChapter.content)) {
            bookChapterDao.updateBookCurrentChapter(retChapter, retChapter.sequence)
        }
    }

    writeChapterFromPackage(bookDaoHelper, retChapter)
    return retChapter

}

@Throws(WriteFileFailException::class)
private fun writeChapterFromPackage(bookDaoHelper: BookDaoHelper, chapter: Chapter?) {
    if (chapter == null) {
        return
    }

    if (bookDaoHelper.isBookSubed(chapter.book_id)) {
        var content = chapter.content
        if (TextUtils.isEmpty(content)) {
            content = "null"
        }
        var write_success = false
        if (content == OtherRequestChapterExecutor.CACHE_EXIST) {
            write_success = true
            println(" content == OtherRequestChapterExecutor.CACHE_EXIST ")
        } else {
            write_success = DataCache.saveChapterFromPackage(content, chapter)
        }

        if (!write_success) {
            val writeFileFailException = WriteFileFailException()
            writeFileFailException.printStackTrace()
            throw writeFileFailException
        }
    } else {
        println(" ! bookDaoHelper.isBookSubed(chapter.book_id)")
    }
}

private fun parseSingleChapter(chapterIdList: ArrayList<String>, chapterList: ArrayList<Chapter>, p2: PackChapterProto.PackChapter?): Chapter? {
    if (chapterIdList.contains(p2!!.getChapterId())) {
        val chapter = chapterList.get(chapterIdList.indexOf(p2!!.getChapterId()))
        if (chapter != null && p2 != null) {

            try {
                chapter.content = p2!!.getContent()
                if (chapter.site != p2!!.getHost()) {
                    chapter.flag = 1
                }
            } catch (e: Exception) {
                chapter.status = Chapter.Status.SOURCE_ERROR
                e.printStackTrace()
            }

            if (!TextUtils.isEmpty(chapter.content)) {
                chapter.content = chapter.content.replace("\\n", "\n")
                chapter.content = chapter.content.replace("\\n\\n", "\n")
                chapter.content = chapter.content.replace("\\n \\n", "\n")
                chapter.content = chapter.content.replace("\\", "")
            }
        } else {
            println(" ! chapterIdList not contains ${p2!!.getChapterId()}")
        }
        return chapter
    }
    return null
}