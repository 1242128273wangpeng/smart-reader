package com.ding.basic.database.migration

class DefaultConverter : DBFieldConverter<Any, Any> {
    override fun convert(old: Any): Any {
        return old
    }
}