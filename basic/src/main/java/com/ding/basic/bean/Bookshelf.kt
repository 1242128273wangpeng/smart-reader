package net.lzbook.kit.data.user

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

data class UserBook(
        var name: String,
        var author: String,
        var desc: String,
        var labels: String,
        var imgUrl: String,
        var url: String,
        var status: String,
        var source: Source,
        var bookId: String,
        var bookSourceId: String,
        var host: String,
        var dex: Int,
        var lastChapter: LastChapter,
        var offset: Int,
        var sequence: Int,
        var chapterCount: Int,
        var addTime: String,
        var readTime: Long
)

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