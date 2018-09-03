package net.lzbook.kit.data.book

import java.io.Serializable

/**
 * @desc 足迹数据上传Body
 * @author lijun Lee
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/4/3 10:32
 */
data class BookBrowseReqBody(var accountId: String,
                             var list: List<BookInfoBody>): Serializable {

    data class BookInfoBody(var bookId: String,
                            var bookSourceId: String,
                            var addTime: String,
                            var host: String,
                            var imgUrl: String,
                            var name: String,
                            var author: String): Serializable
}