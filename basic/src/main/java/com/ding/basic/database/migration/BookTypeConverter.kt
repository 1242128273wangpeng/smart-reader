package com.ding.basic.database.migration

class BookTypeConverter : DBFieldConverter<String, String> {
    override fun convert(old: String): String {
        return when (old) {
            "api.qingoo.cn" -> {
                "qg"
            }
            else -> {
                "zh"
            }
        }
    }

}