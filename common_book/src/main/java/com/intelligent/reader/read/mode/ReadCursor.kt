package com.intelligent.reader.read.mode

import net.lzbook.kit.data.bean.Book

/**
 * 阅读游标
 * Created by wt on 2017/12/20.
 */
data class ReadCursor(
        var curBook: Book,
        var sequence:Int
)