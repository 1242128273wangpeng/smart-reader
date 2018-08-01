package net.lzbook.kit.data.book

import com.ding.basic.bean.Bookmark


/**
 * @desc 上传书签请求 Body
 * @author lijun Lee
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/4/9 14:43
 */
data class BookMarkBody(
        var accountId: String,
        var list: List<Book>
)

data class Book(
        var bookId: String,
        var bookSourceId: String,
        var marks: List<BookMark>
)

data class BookMark(
        var chapterName: String,
        var offset: Int,
        var sequence: Int,
        var markContent: String,
        var addTimeStr: String) {

//    companion object {
//        fun create(bookmark: Bookmark): BookMark =
//                BookMark(bookmark.chapter_name.toString(),
//                        bookmark.offset,
//                        bookmark.sequence,
//                        bookmark.chapter_content.toString(),
//                        bookmark.last_time.toString()
//                )
//    }
}