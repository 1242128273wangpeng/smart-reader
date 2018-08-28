package com.ding.basic.bean

import android.arch.persistence.room.*
import com.ding.basic.database.migration.FieldMigration
import java.io.Serializable

@Entity(tableName = "chapters", indices = arrayOf(Index("sequence", unique = true)))
open class Chapter : Serializable {
    /************************************ 数据融合 ************************************/
    //书籍ID
    @ColumnInfo(name = "book_id")
    var book_id: String = ""
    //书籍SourceID
    @ColumnInfo(name = "book_source_id")
    var book_source_id: String = ""
    //书籍ChapterID
    @ColumnInfo(name = "book_chapter_id")
    var book_chapter_id: String = ""
    //章节ID
    @PrimaryKey
    @ColumnInfo(name = "chapter_id")
    var chapter_id: String = ""
    //服务端章节序号
    @FieldMigration(oldName = "sort")
    @ColumnInfo(name = "serial_number")
    var serial_number: Int = 0
    //章节名
    @FieldMigration(oldName = "chapter_name")
    @ColumnInfo(name = "name")
    var name: String? = null
    //章节Url
    @FieldMigration(oldName = "curl")
    @ColumnInfo(name = "url")
    var url: String? = null
    //章节更新时间
    @FieldMigration(oldName = "chapter_update_time")
    @ColumnInfo(name = "update_time")
    var update_time: Long = 0
    //章节字数
    @ColumnInfo(name = "word_count")
    var word_count: Int = 0
    //收费标识
    @ColumnInfo(name = "vip")
    var vip: Int = 0
    //章节内容
    @Ignore
    var content: String? = null
    //章节价格
    @ColumnInfo(name = "price")
    var price: Double = 0.0

    //本地记录当前第几章
    @ColumnInfo(name = "sequence")
    var sequence: Int = -1
    //小说来源站
    @FieldMigration(oldName = "site")
    @ColumnInfo(name = "host")
    var host: String? = null

    @ColumnInfo(name = "chapter_status")
    var chapter_status: String? = null

    // 章节状态
    @Ignore
    var status = ChapterState.CONTENT_NORMAL

    /************************************ 大壳字段 ************************************/

    //是否收费章节 0为免费 1为付费
    @ColumnInfo(name = "charge")
    var chargeChapter: Int = 0

    //是否购买本章 0未购 1为已经
    @ColumnInfo(name = "purchase")
    var purchase: Int = 0

    //用户余额
    @ColumnInfo(name = "gold")
    var gold: Int = 0

    //自动购买
    @ColumnInfo(name = "auto_flag")
    var autoFlag: String? = null

    //是否试读内容1 为试读  0为全部内容
    @ColumnInfo(name = "ex_content")
    var exContent: Int = 0


    constructor()

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        } else {
            if (other is Chapter) {
                if ("" != other.chapter_id) {
                    return other.chapter_id == this.chapter_id
                }
            }
        }

        return false
    }

    override fun toString(): String {
        return "Chapter(book_id='$book_id', book_source_id='$book_source_id', book_chapter_id='$book_chapter_id', chapter_id='$chapter_id', serial_number=$serial_number, name=$name, url=$url, update_time=$update_time, word_count=$word_count, vip=$vip, content=$content, price=$price, sequence=$sequence, host=$host, chapter_status=$chapter_status, status=$status, chargeChapter=$chargeChapter, purchase=$purchase, gold=$gold, autoFlag=$autoFlag, exContent=$exContent)"
    }


}