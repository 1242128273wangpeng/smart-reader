package com.ding.basic.bean

import java.io.Serializable


/**
 * @desc 上传书签请求 Body
 * @author lijun Lee
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/4/9 14:43
 */
data class BookMarkBody(
        var accountId: String,
        var list: List<UserMarkBook>
): Serializable

data class UserMarkBook(
        var bookId: String,
        var bookSourceId: String,
        var marks: List<UserMark>
): Serializable

data class UserMark(
        var chapterName: String,
        var offset: Int,
        var sequence: Int,
        var markContent: String,
        var addTimeStr: String): Serializable {

    companion object {
        fun create(bookmark: Bookmark): UserMark =
                UserMark(bookmark.chapter_name.toString(),
                        bookmark.offset,
                        bookmark.sequence,
                        bookmark.chapter_content.toString(),
                        bookmark.insert_time.toString()
                )
    }
}