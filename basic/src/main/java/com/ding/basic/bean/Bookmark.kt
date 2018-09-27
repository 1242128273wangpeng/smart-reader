package com.ding.basic.bean

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.ding.basic.db.migration.FieldMigration
import java.io.Serializable

/**
 * Created by yuchao on 2018/3/15 0015.
 */

@Entity(tableName = "book_mark")
class Bookmark : Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0

    // 书籍id
    @ColumnInfo(name = "book_id")
    var book_id: String = ""

    //小说来源id
    @ColumnInfo(name = "book_source_id")
    var book_source_id: String = ""

    //小说来源id(修正)
    @ColumnInfo(name = "book_chapter_id")
    var book_chapter_id: String = ""

    //记录当前阅读位置
    @ColumnInfo(name = "sequence")
    var sequence = -1

    //记录书签偏移量
    @ColumnInfo(name = "offset")
    var offset = -1

    //添加书签的时间
    @FieldMigration(oldName = "last_time")
    @ColumnInfo(name = "insert_time")
    var insert_time: Long = 0

    //当前章节名
    @ColumnInfo(name = "chapter_name")
    var chapter_name: String? = null

    //当前章节内容
    @ColumnInfo(name = "chapter_content")
    var chapter_content: String? = null
}