package com.ding.basic.bean

import java.io.Serializable

/**
 * Created on 2018/3/20.
 * Created by crazylei.
 */
class FixContent : Serializable {

    var book_id: String = ""

    var book_source_id: String = ""

    var book_chapter_id: String = ""

    var host: String = ""

    var last_update: Long = 0

    var list_version: Int = 0

    var c_version: Int = 0

    var chapters: List<Chapter>? = null

    var chapterCount: Int = 0
}