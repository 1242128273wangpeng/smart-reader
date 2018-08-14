package net.lzbook.kit.data.book

/**
 * @desc 书架请求 Body
 * @author lijun Lee
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/4/9 20:46
 */

data class BookReqBody(var accountId: String,
                       var list: List<BookBody>)

data class BookBody(var bookId: String,
                    var bookSourceId: String,
                    var offset: Int,
                    var sequence: Int,
                    var host: String,
                    var imgUrl: String?,
                    var name: String,
                    var author: String,
                    var readTime: Long,
                    var chapterCount: Int,
                    var lastChapterName: String,
                    var lastChapterUpdateTime: Long
)



