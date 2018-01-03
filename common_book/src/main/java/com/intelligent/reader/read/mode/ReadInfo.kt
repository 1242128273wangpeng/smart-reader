package com.intelligent.reader.read.mode

import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.ReadStatus
import net.lzbook.kit.data.bean.ReadViewEnums

/**
 * 阅读信息
 * Created by wt on 2017/12/13.
 */
data class ReadInfo(
        var curBook:Book,//Book
        var mReadStatus:ReadStatus,
        var animaEnums: ReadViewEnums.Animation//动画模式
)

