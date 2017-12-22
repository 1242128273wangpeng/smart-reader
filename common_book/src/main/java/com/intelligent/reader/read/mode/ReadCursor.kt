package com.intelligent.reader.read.mode

import com.google.gson.Gson
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.ReadStatus

/**
 * 阅读游标
 * Created by wt on 2017/12/20.
 */
data class ReadCursor(
        var curBook: Book,
        var sequence: Int,//章节
        var pageIdex: Int,//页数
        var pageStatus: ReadViewEnums.PageIndex?,//view tag
        var readStatus: ReadStatus
) {
    var pageIdexSum: Int? = null
    override fun toString(): String {
        return Gson().toJson(this)
    }
}