package com.ding.basic.db.migration

interface DBFieldConverter<in F, out T> {
    fun convert(old:F):T
}