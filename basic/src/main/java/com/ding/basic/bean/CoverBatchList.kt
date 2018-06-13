package com.ding.basic.bean

import java.io.Serializable


/**
 * Desc 书架每天第一次批量更新书籍信息
 * Author zhenxiang
 * Mail zhenxiang_lin@dingyuegroup.cn
 * Date 2018\6\12 0012 14:54
 */
class CoverBatchList : Serializable {
    var coverBatchList: List<Book> ?= null
}