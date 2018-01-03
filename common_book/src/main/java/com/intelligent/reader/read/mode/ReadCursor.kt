package com.intelligent.reader.read.mode

import com.google.gson.Gson
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.ReadStatus
import net.lzbook.kit.data.bean.ReadViewEnums

/**
 * 阅读游标
 * Created by wt on 2017/12/20.
 */
data class ReadCursor(
        var curBook: Book,
        var sequence: Int,//章节
        var offset: Int,//字符位移
        var pageStatus: ReadViewEnums.PageIndex?,//view tag
        var readStatus: ReadStatus
) {
    var lastOffset: Int = 0
    var nextOffset: Int = 0
    var isShowAdBanner = false
    override fun toString(): String {
        return Gson().toJson(this)
    }
}