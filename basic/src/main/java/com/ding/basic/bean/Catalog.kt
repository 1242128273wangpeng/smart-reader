package com.ding.basic.bean

import java.io.Serializable

/**
 * Created on 2018/3/19.
 * Created by crazylei.
 */
class Catalog : Serializable {
    var host: String? = null

    var chapters: List<Chapter>? = null

    var chapterCount: Int? = 0

    var book_id: String = ""

    var book_source_id: String = ""

    var book_chapter_id: String = ""

    var update_time: Long? = 0

    // 目录修复版本号
    var listVersion: Int? = -1

    // 章节内容修复版本号
    var contentVersion: Int? = -1
}