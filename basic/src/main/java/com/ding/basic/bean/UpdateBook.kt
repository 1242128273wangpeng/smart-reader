package com.ding.basic.bean

/**
 * Created on 2018/3/20.
 * Created by crazylei.
 */
class UpdateBook {
    var book_id: String = ""

    var book_source_id: String = ""

    var book_chapter_id: String = ""

    var host: String = ""

    var update_time: Long = 0

    var chapters: List<Chapter>? = null

    var chapterCount: Int = 0
}