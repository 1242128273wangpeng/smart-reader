package com.ding.basic.bean

import android.arch.persistence.room.*
import android.view.ViewGroup
import com.ding.basic.db.migration.*
import java.io.Serializable

@Entity(tableName = "book")
open class Book : Serializable, Comparable<Book>, Cloneable {
    /***
     * 数据升级，字段问题
     * 1、字段冲突 -> book_type
     * 原来标识书籍种类，区分线上书籍或者广告类型
     * 现在标识书籍来源，区分智能书籍、青果书籍和VIP书籍
     * 2、类型冲突 -> state
     * 原来使用Int类型标识书籍完结、连载状态
     * 现在使用String类型标识书籍完结、连载状态
     *
     * 解决方法：
     * 1、修改原有book_type为item_type
     * 2、数据库迁移过程中手动修改
     */

    /************************************ 数据融合 ************************************/

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0

    //书籍ID
    @ColumnInfo(name = "book_id")
    var book_id: String = ""

    //书籍ChapterID，新版接口主要字段
    @ColumnInfo(name = "book_chapter_id")
    var book_chapter_id: String = ""

    //书籍SourceID，逐步废弃
    @ColumnInfo(name = "book_source_id")
    var book_source_id: String = ""

    //书籍名称
    @ColumnInfo(name = "name")
    var name: String? = null

    //书籍作者
    @ColumnInfo(name = "author")
    var author: String? = null

    //书籍描述
    @ColumnInfo(name = "desc")
    var desc: String? = null

    //书籍标签，字符串格式，逗号隔开
    @ColumnInfo(name = "label")
    var label: String? = null

    //一级分类
    @ColumnInfo(name = "genre")
    var genre: String? = null

    //二级分类
    @ColumnInfo(name = "sub_genre")
    var sub_genre: String? = null

    //书籍封面地址
    @ColumnInfo(name = "img_url")
    var img_url: String? = null

    //书籍状态：FINISH
    @FieldMigration(oldName = "status", converter = StatusConverter::class)
    @ColumnInfo(name = "status")
    var status: String? = null

    //书籍源站
    @FieldMigration(oldName = "site", converter = BookHostConverter::class)
    @ColumnInfo(name = "host")
    var host: String? = null

    @FieldMigration(oldName = "site", converter = BookTypeConverter::class)
    //书籍种类：zn智能；vip付费；qg青果
    @ColumnInfo(name = "book_type")
    var book_type: String? = null

    //书籍字数
    @ColumnInfo(name = "word_count")
    var word_count: String? = null

    //目录Version
    @ColumnInfo(name = "list_version")
    var list_version: Int = -1

    //内容Version
    @ColumnInfo(name = "c_version")
    var c_version: Int= -1

    //最新章节
    @FieldMigration(oldName = "last_chapter_name", converter = BookChapterConverter::class)
    @Embedded(prefix = "chapter_")
    var last_chapter: Chapter? = null


    @ColumnInfo(name= "score")
    var score: Float = 0.0f

    @ColumnInfo(name= "uv")
    var uv: Long = 0

    /************************************ 本地字段 ************************************/
    //书籍阅读状态
    @ColumnInfo(name = "readed")
    var readed: Int = 0

    //书籍阅读进度
    @ColumnInfo(name = "sequence")
    var sequence: Int = -2

    //阅读章节偏移量
    @ColumnInfo(name = "offset")
    var offset: Int = -1

    //书籍阅读时间
    @FieldMigration(oldName = "sequence_time")
    @ColumnInfo(name = "last_read_time")
    var last_read_time: Long = 0

    //书籍加入书架时间
    @ColumnInfo(name = "insert_time")
    var insert_time: Long = 0

    //书籍更新状态
    @ColumnInfo(name = "update_status")
    var update_status: Int = -1

    //小说章节数量
    @ColumnInfo(name = "chapter_count")
    var chapter_count: Int = 0

    //最后检查更新的时间,旧字段：last_updatetime_native
    @ColumnInfo(name = "last_check_update_time")
    var last_check_update_time: Long = 0

    //最后最后一次本地更新成功的时间, 与最后一章节中的时间有区别
    @ColumnInfo(name = "last_updateSucessTime")
    var last_update_success_time: Long = 0

    //数据融合升级，默认为0
    @ColumnInfo(name = "update_date_fusion")
    var update_date_fusion: Int = 0


    //上一次更新到的章节序号，为了和青果适配而新增的字段
    @ColumnInfo(name = "chapters_update_index")
    var chapters_update_index: Int = 0

    @ColumnInfo(name = "list_version_fix")
    var list_version_fix: Int = -1

    @ColumnInfo(name = "force_fix")
    var force_fix: Int = 0

    //区分书籍状态，目前没有本地书籍，暂时标识书籍和广告两种状态
    @Ignore
    var item_type: Int = 0
    //广告布局
    @Ignore
    @Transient
    var item_view: ViewGroup? = null

    //统计打点 当前页面是来自书籍封面/书架/上一页翻页
    @Ignore
    var fromType: Int = 0
    //统计当前阅读的小说是来自青果还是智能 打点统计
    @Ignore
    var channel_code: Int = 0


    constructor() : this(0)

    @Ignore
    constructor(id: Int) {
        this.id = id
    }

    /**
     * 书籍是否来源于青果
     */
    fun fromQingoo(): Boolean {
        host?.let {
            return it.contains("qingoo")
        }
        return false
    }

    /**
     * 书籍是否处于等待目录修复状态
     */
    fun waitingCataFix(): Boolean {
        if (force_fix == 1 || list_version < list_version_fix) {
            return true
        }
        return false
    }

    public override fun clone(): Book {
        return super.clone() as Book
    }

    override fun compareTo(other: Book): Int {
        return if (this.last_read_time == other.last_read_time) 0 else if (this.last_read_time < other.last_read_time) 1 else -1
    }

    override fun toString(): String {
        return "Book(id=$id, book_id='$book_id', book_chapter_id='$book_chapter_id', book_source_id='$book_source_id', name=$name, author=$author, desc=$desc, label=$label, genre=$genre, sub_genre=$sub_genre, img_url=$img_url, status=$status, host=$host, book_type=$book_type, word_count=$word_count, list_version=$list_version, c_version=$c_version, last_chapter=$last_chapter, readed=$readed, sequence=$sequence, offset=$offset, last_read_time=$last_read_time, insert_time=$insert_time, update_status=$update_status, chapter_count=$chapter_count, last_check_update_time=$last_check_update_time, last_update_success_time=$last_update_success_time, update_date_fusion=$update_date_fusion, chapters_update_index=$chapters_update_index, item_type=$item_type, fromType=$fromType, channel_code=$channel_code)"
    }

    companion object {
        const val STATUS_FINISH = "FINISH"
        const val STATUS_SERIALIZE = "SERIALIZE"
    }


}