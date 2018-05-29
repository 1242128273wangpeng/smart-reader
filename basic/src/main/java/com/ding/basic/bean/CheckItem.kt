package com.ding.basic.bean

import java.io.Serializable

/**
 * Created on 2018/3/20.
 * Created by crazylei.
 */
class CheckItem: Serializable {

    var book_id: String? = null

    var book_source_id: String? = null

    var book_chapter_id: String? = null

    var last_chapter_id: String? = null

    var add_bookshelf_time: Long? = 0

    var list_version: Int? = -1

    var c_version: Int? = -1
}