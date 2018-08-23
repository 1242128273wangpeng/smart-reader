package com.ding.basic.bean

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import org.jetbrains.annotations.NotNull
import java.io.Serializable

@Entity(tableName = "book_fix")
class BookFix: Serializable {

    @Ignore
    var id: Int = 0

    @NotNull
    @PrimaryKey
    @ColumnInfo(name = "book_id")
    var book_id: String = ""

    /**
     * 1：章节修复：标识已修复 等待toast提示用户
     * 2：目录修复：标识未修复，需要用户手动修复
     */
    @ColumnInfo(name = "fix_type")
    var fix_type: Int = 0

    @ColumnInfo(name = "list_version")
    var list_version: Int = 0

    @ColumnInfo(name = "c_version")
    var c_version: Int = 0

    /**
     * 目录修复提示对话框
     * 0：未显示
     * 1：已显示
     */
    @ColumnInfo(name = "dialog_flag")
    var dialog_flag = 0

    constructor() : this(0)

    constructor(id: Int) {
        this.id = id
    }
}