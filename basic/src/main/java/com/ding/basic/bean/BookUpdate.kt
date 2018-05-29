package com.ding.basic.bean

import java.io.Serializable
import java.util.ArrayList

/**
 * Created on 2018/3/20.
 * Created by crazylei.
 */
class BookUpdate : Serializable {
    //更新章节数
    var update_count: Int = 0
    //小说组id
    var gid: Int = 0
    //更新时间
    var last_time: Long = 0
    //最新章节名称
    var last_chapter_name: String? = null
    //章节列表
    var chapterList: ArrayList<Chapter>? = null
    //最新章节序号
    var last_sort: Int = 0
    //最佳目录的章节序号
    var gsort: Int = 0

    //新的字段
    var book_id: String? = null
    var book_source_id: String? = null
    var parameter: String? = null
    var extra_parameter: String? = null
    //最新章节名对应的md5值
    var check_md5: String? = null
    //小说名称
    var book_name: String? = null
}