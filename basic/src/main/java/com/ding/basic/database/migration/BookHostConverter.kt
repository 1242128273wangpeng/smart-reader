package com.ding.basic.database.migration

/**
 * Created by yuchao on 2018/4/8 0008.
 */
class BookHostConverter: DBFieldConverter<String, String> {
    override fun convert(old: String): String {
        return when(old) {
            "api.qingoo.cn" -> {
                 "open.qingoo.cn"
            }
            else -> {
                old
            }
        }
    }
}