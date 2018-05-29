package com.ding.basic.bean

import java.io.Serializable

/**
 * Created on 2018/3/19.
 * Created by crazylei.
 */
class BookSource : Serializable {
    var name: String? = null
    var author: String? = null
    var book_id: String? = null

    var items:List<Source>? = null
}