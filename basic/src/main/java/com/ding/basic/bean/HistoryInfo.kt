package com.ding.basic.bean

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.ding.basic.db.migration.FieldMigration
import com.ding.basic.db.migration.StatusConverter
import java.io.Serializable

/**
 * Created by yuchao on 2018/3/15 0015.
 */

@Entity(tableName = "history_info")
class HistoryInfo: Serializable {

    @PrimaryKey
    @ColumnInfo(name = "book_id")
    var book_id: String = ""

    @ColumnInfo(name = "book_source_id")
    var book_source_id: String = ""

    @ColumnInfo(name = "book_chapter_id")
    var book_chapter_id: String = ""

    @ColumnInfo(name = "name")
    var name: String? = null

    @ColumnInfo(name = "author")
    var author: String? = null

    @ColumnInfo(name = "label")
    var label: String? = null

    @ColumnInfo(name = "img_url")
    var img_url: String? = null

    @FieldMigration(oldName = "site")
    @ColumnInfo(name = "host")
    var host: String? = null

    @ColumnInfo(name = "desc")
    var desc: String? = null

    @FieldMigration(oldName = "status", converter = StatusConverter::class)
    @ColumnInfo(name = "status")
    var status: String? = null

    @ColumnInfo(name = "book_type")
    var book_type: String? = null

    @ColumnInfo(name = "chapter_count")
    var chapter_count: Int = 0

    @FieldMigration(oldName = "last_brow_time")
    @ColumnInfo(name = "browse_time")
    var browse_time: Long = 0

    var last_chapter_name: String? = null

    var last_chapter_update_time: Long? = null
}