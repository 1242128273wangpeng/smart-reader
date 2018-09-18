package com.ding.basic.db.migration.helper

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.ding.basic.db.dao.BaseDao
import com.ding.basic.db.migration.DBFieldConverter
import com.ding.basic.db.migration.DefaultConverter
import com.ding.basic.db.migration.FieldMigration
import com.orhanobut.logger.Logger
import java.lang.reflect.Field

@Throws(Exception::class)
fun <T> migrateTable(fromDB: SQLiteDatabase, fromTable: String
                     , dao: BaseDao<T>, clazz: Class<T>) {

    val fields = clazz.declaredFields

    val fieldOldNameMap = mutableMapOf<Field, String>()
    val fieldConverterMap = mutableMapOf<Field, DBFieldConverter<Any, Any>?>()

    for (field in fields) {
        var key = field.name
        if (field.isAnnotationPresent(FieldMigration::class.java)) {
            key = field.getAnnotation(FieldMigration::class.java).oldName
        }
        var converter: DBFieldConverter<Any, Any>? = null
        if (field.isAnnotationPresent(FieldMigration::class.java)) {
            val annotationConverter = field.getAnnotation(FieldMigration::class.java).converter
            if (annotationConverter != DefaultConverter::class) {
                converter = annotationConverter.java.newInstance() as DBFieldConverter<Any, Any>
            }
        }
        fieldOldNameMap.put(field, key)
        fieldConverterMap.put(field, converter)
    }

    val indexTypeMap = mutableMapOf<Field, Pair<Int, Int>>()

    var cursor: Cursor? = null
    try {
        val readStartTime = System.currentTimeMillis()
        val cursor = fromDB.query(fromTable, null, null, null, null, null, null)

        val list = mutableListOf<T>()

        while (cursor.moveToNext()) {

            if(indexTypeMap.isEmpty()){
                fields.forEach {
                    var old = fieldOldNameMap[it]!!
                    val index = cursor.getColumnIndex(old)
                    indexTypeMap.put(it, Pair(index, cursor.getType(index)))
                    if (old == "offset" || old == "sequence_time") {
                        if (indexTypeMap.get(it)!!.second == Cursor.FIELD_TYPE_NULL) {
                            indexTypeMap.put(it, Pair(index, Cursor.FIELD_TYPE_INTEGER))
                        }
                    }
                }
            }

            val obj = clazz.newInstance()
            for (field in fields) {

                fillObjectFieldFromCursor(cursor
                        , indexTypeMap.get(field)!!.first
                        , indexTypeMap.get(field)!!.second
                        , field
                        , fieldConverterMap[field]
                        , obj as Object)
            }
            list.add(obj)
        }

        Logger.v("read count=${list.size} use time:${System.currentTimeMillis() - readStartTime}")
        val writeStartTime = System.currentTimeMillis()
        if (list.isNotEmpty()) {
            dao.insertOrUpdate(list)
        }
        Logger.v("write count=${list.size} use time:${System.currentTimeMillis() - writeStartTime}")
        Logger.e("migrateTable $fromTable use time:${System.currentTimeMillis() - readStartTime}")

    } finally {
        cursor?.close()
    }

}


private fun fillObjectFieldFromCursor(cursor: Cursor, index:Int, type:Int, field: Field, converter: DBFieldConverter<Any, Any>?, obj: Object) {
    field.isAccessible = true

    when (type) {
        Cursor.FIELD_TYPE_INTEGER -> {
            val long = cursor.getLong(index)

            if (converter != null) {
                if(field.type == Int::class.java) {
                    field.set(obj, converter.convert(long.toInt()))
                }else{
                    field.set(obj, converter.convert(long))
                }
            } else {
                if(field.type == Int::class.java){
                    field.set(obj, long.toInt())
                }else {
                    field.set(obj, long)
                }
            }
        }
        Cursor.FIELD_TYPE_FLOAT -> {
            val double = cursor.getDouble(index)
            if (converter != null) {
                if(field.type == Float::class.java) {
                    field.set(obj, converter.convert(double.toFloat()))
                }else{
                    field.set(obj, double.toFloat())
                }
            } else {
                if(field.type == Float::class.java) {
                    field.set(obj, double.toFloat())
                }else {
                    field.set(obj, double)
                }
            }
        }
        Cursor.FIELD_TYPE_STRING -> {
            if (converter != null) {
                field.set(obj, converter.convert(cursor.getString(index)))
            } else {
                field.set(obj, cursor.getString(index) ?: "")
            }
        }
        Cursor.FIELD_TYPE_BLOB -> {
            if (converter != null) {
                field.set(obj, converter.convert(cursor.getBlob(index)))
            } else {
                field.set(obj, cursor.getBlob(index))
            }
        }

    }
}

