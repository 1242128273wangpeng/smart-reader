package com.ding.basic.bean

import java.io.Serializable

class Source : Serializable {

    var host: String? = null
    var book_source_id: String = ""
    var book_chapter_id: String = ""
    var last_chapter_id: String = ""
    var last_chapter_name: String? = null

    var update_time: Long = 0
}