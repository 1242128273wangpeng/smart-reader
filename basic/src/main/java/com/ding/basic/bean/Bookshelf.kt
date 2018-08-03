package net.lzbook.kit.data.user

import com.ding.basic.bean.Book
import com.ding.basic.bean.HistoryInfo

/**
 * Desc 云书架
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/4/2 0002 19:27
 */
data class Bookshelf(
        var userId: String,
        var list: List<UserBook>
)

class UserBook {
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
        book.name = name
        book.author = author
        book.desc = desc
        book.sub_genre = labels
        book.img_url = imgUrl
        if ("FINISH" == status) {
            book.status = "2"
        } else {
            book.status = "1"
        }
        book.book_id = bookId.toString()
        book.book_source_id = bookSourceId.toString()
        book.host = host
        book.offset = offset
        book.sequence = sequence
        book.last_read_time = readTime
        book.chapter_count = chapterCount
        book.last_chapter?.name = lastChapter!!.name
        book.last_check_update_time = lastChapter!!.update_time
        book.last_chapter?.url = lastChapter!!.url
        return book

    }

    fun transToHistoryInfo():HistoryInfo{
        val historyInfo=HistoryInfo()
        historyInfo.author=author
        historyInfo.img_url=imgUrl
        historyInfo.name=name
        historyInfo.book_id=bookId.toString()
        historyInfo.book_source_id=bookSourceId.toString()
        historyInfo.label=labels
        historyInfo.host=host
        historyInfo.desc=desc
        historyInfo.browse_time= addTime?.toLong() ?:0



        return  historyInfo
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
)

data class Source(
        var gid: String,
        var nid: String
)