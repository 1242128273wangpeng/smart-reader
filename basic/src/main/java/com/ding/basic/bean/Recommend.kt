package com.ding.basic.bean

import java.io.Serializable

/**
 * Created on 2018/3/19.
 * Created by crazylei.
 */
class Recommend : Serializable {

    var book_id: String? = null

    var book_chapter_id: String? = null

    var book_source_id: String? = null

    var name: String? = null

    var author: String? = null

    var desc: String? = null

    var label: String? = null

    var genre: String? = null

    var sub_genre: String? = null

    var img_url: String? = null

    var status: String? = null

    var host: String? = null

    var book_type: String? = null

    var word_count: String? = null

    var last_update: Long = 0

    var list_version = -1

    var c_version = -1

    var last_chapter: Chapter? = null

    var readed: Int = 0

    var sequence = -2

    var offset = -1

    var sequence_time: Long = 0

    var insert_time: Long = 0

    var update_status = -1
}