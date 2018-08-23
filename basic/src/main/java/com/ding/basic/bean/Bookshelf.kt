package net.lzbook.kit.data.user

import com.ding.basic.bean.Book
import com.ding.basic.bean.Chapter
import com.ding.basic.bean.HistoryInfo
import java.io.Serializable

/**
 * Desc 云书架
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/4/2 0002 19:27
 */
data class Bookshelf(
        var userId: String,
        var list: List<UserBook>
) : Serializable

class UserBook: Serializable {
    var name: String? = ""
    var author: String? = ""
    var desc: String? = ""
    var labels: String? = ""
    var imgUrl: String? = ""
    var url: String? = ""
    var status: String? = ""
    var source: Source? = null
    var bookId: String? = ""
    var bookSourceId: String? = ""
    var host: String? = ""
    var dex: Int = 0
    var lastChapter: LastChapter? = null
    var offset: Int = 0
    var sequence: Int = 0
    var chapterCount: Int = 0
    var addTime: String? = ""
    var readTime: Long = 0

    fun transToBook(): Book {
        val book = Book()
        try {
            book.name = name
            book.author = author
            book.desc = desc
            book.sub_genre = labels
            book.img_url = imgUrl
            book.status = status
            book.book_id = bookId.toString()
            book.book_source_id = bookSourceId.toString()
            book.host = host
            book.offset = offset
            book.sequence = sequence
            book.last_read_time = readTime
            book.chapter_count = chapterCount

            val chapter = Chapter()
            if (lastChapter != null) {
                chapter.name = lastChapter?.name
                chapter.update_time = lastChapter!!.update_time
                chapter.url = lastChapter!!.url
            }

            book.last_chapter = chapter

            book.last_check_update_time = lastChapter!!.update_time
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return book
    }



    fun transToHistoryInfo(): HistoryInfo {
        val historyInfo = HistoryInfo()
        historyInfo.author = author
        historyInfo.img_url = imgUrl
        historyInfo.name = name
        historyInfo.book_id = bookId.toString()
        historyInfo.book_source_id = bookSourceId.toString()
        historyInfo.label = labels
        historyInfo.host = host
        historyInfo.desc = desc
        historyInfo.browse_time = addTime?.toLong() ?: 0



        return historyInfo
    }

    override fun toString(): String {
        return "UserBook(name=$name, author=$author, desc=$desc, labels=$labels, imgUrl=$imgUrl, url=$url, status=$status, source=$source, bookId=$bookId, bookSourceId=$bookSourceId, host=$host, dex=$dex, lastChapter=$lastChapter, offset=$offset, sequence=$sequence, chapterCount=$chapterCount, addTime=$addTime, readTime=$readTime)"
    }

}

data class LastChapter(
        var id: String,
        var book_souce_id: String,
        var name: String,
        var serial_number: Int,
        var host: String,
        var url: String,
        var url1: String,
        var terminal: String,
        var status: String,
        var update_time: Long,
        var word_count: Int,
        var vip: Int,
        var price: Double
) : Serializable

data class Source(
        var gid: String,
        var nid: String
) : Serializable