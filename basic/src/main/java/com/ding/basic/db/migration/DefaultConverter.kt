package com.ding.basic.db.migration

class DefaultConverter : DBFieldConverter<Any, Any> {
    override fun convert(old: Any): Any {
        return old
    }
}