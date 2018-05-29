package com.ding.basic.bean

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity(tableName = "book_fix")
class BookFix {

    @Ignore
    var id: Int = 0

    @NotNull
    @PrimaryKey
    @ColumnInfo(name = "book_id")
    var book_id: String = ""

    @ColumnInfo(name = "fix_type")
    var fix_type: Int = 0

    @ColumnInfo(name = "list_version")
    var list_version: Int = 0

    @ColumnInfo(name = "c_version")
    var c_version: Int = 0

    @ColumnInfo(name = "dialog_flag")
    var dialog_flag = 0

    constructor(): this(0)

    constructor(id: Int) {
        this.id = id
    }
}