package com.ding.basic.bean

import java.io.Serializable

class Source : Serializable {

    //小说来源
    var host: String? = null
    var book_source_id: String = ""
    var book_chapter_id: String = ""

    //最新章节名称
    var last_chapter_id: String = ""
    var last_chapter_name: String? = null

    //当前来源小说更新时间
    var update_time: Long = 0

}