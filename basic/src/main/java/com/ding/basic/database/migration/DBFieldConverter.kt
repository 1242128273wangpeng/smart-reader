package com.ding.basic.database.migration

interface DBFieldConverter<in F, out T> {
    fun convert(old:F):T
}