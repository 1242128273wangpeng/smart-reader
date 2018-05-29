package com.ding.basic.bean

import java.io.Serializable

/**
 * Created on 2018/3/20.
 * Created by crazylei.
 */
class ChapterBean: Serializable {
    /**
     * id : 59f17a6885b1ce043c5439b7
     * book_souce_id : 58c08020df43fe69c00d6cdd
     * name : 第七百八十九章 毒魂体
     * serial_number : 844
     * host : www.basicnos.cn
     * url : https://www.basicnos.cn/v3/book/chaptersContents?chapterId=59f17a6885b1ce043c5439b7
     * url1 : null
     * terminal : WEB
     * status : ENABLE
     * update_time : 1508997736204
     * word_count : 4423
     * vip : 0
     * price : 0
     */

    var id: String? = null
    var book_souce_id: String? = null
    var name: String? = null
    var serial_number: Int = 0
    var host: String? = null
    var url: String? = null
    var url1: Any? = null
    var terminal: String? = null
    var status: String? = null
    var update_time: Long = 0
    var word_count: Int = 0
    var vip: Int = 0
    var price: Int = 0
}