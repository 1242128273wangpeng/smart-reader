package com.ding.basic.bean

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey

import java.io.Serializable


/**
 * 搜索推荐模块  一期二期实体类一致
 * Created by zhenXiang on 2018\1\16 0016.
 */
class SearchRecommendBook : Serializable {

    var respCode: String? = null
    var message: String? = null
    var data: ArrayList<DataBean>? = null

    @Entity(tableName = "search_recommend")
    class DataBean {
        /**
         * id : 58bfe2a64ed3437e6b6b5a77
         * bookId : 57692e291b341116f66491d1
         * bookName : 万道剑尊
         * authorName : 打死都要钱
         * description : 下无双！他是独一无二的逆天君王，杀伐果断，杀尽世间一切该杀之人！他，更是掌控万道，亘古以来史上第一剑尊！
         * serialStatus : SERIALIZE
         * label : 玄幻魔法,历史军事,穿越,文学美文,玄幻,东方玄幻,玄幻奇幻
         * genre : 玄幻
         * subGenre : 东方玄幻
         * sourceImageUrl : http://qidian.qpic.cn/qdbimg/349573/1003414055/180
         * imageId : null
         * attribute : {"gid":"200008377","nid":"0"}
         * host : www.mantantd.cn
         * url :
         * terminal : WEB
         * bookChapterMd5 : new-sync
         * bookChapterId : 58bfe2a64ed3437e6b6b5a76
         * lastChapterId : 5a3a4f9485b1ce5665afd7b1
         * lastChapterName : 第二千七百八十一章 天虚宫
         * lastSerialNumber : 2792
         * v : 1597
         * updateTime : 1513770900786
         * createTime : 1488970406693
         * chapterCount : 2784
         * chapterBlankCount : null
         * chapterShortCount : 2450000
         * wordCount : null
         * wordCountDescp : 571.3万
         * readerCount : 380156
         * readerCountDescp : 38.0万
         * dex : 0
         * hot : null
         * score : 10
         * vip : 0
         * chapterPrice : 0
         * bookprice : 0
         * selfPrice : 0
         * selfBookPrice : 0
         */

        @ColumnInfo(name = "booksource_id")
        var id: String? = null
        @PrimaryKey
        @ColumnInfo(name = "book_id")
        var bookId: String? = null
        @ColumnInfo(name = "book_name")
        var bookName: String? = null
        @ColumnInfo(name = "author_name")
        var authorName: String? = null
        @Ignore
        var description: String? = null
        @ColumnInfo(name = "serial_status")
        var serialStatus: String? = null
        @Ignore
        var label: String? = null
        @ColumnInfo(name = "genre")
        var genre: String? = null
        @Ignore
        var subGenre: String? = null
        @ColumnInfo(name = "source_imageurl")
        var sourceImageUrl: String? = null
        @Ignore
        var imageId: String? = null
        @Ignore
        var attribute: AttributeBean? = null
        @ColumnInfo(name = "host")
        var host: String? = null
        @Ignore
        var url: String? = null
        @Ignore
        var terminal: String? = null
        @Ignore
        var bookChapterMd5: String? = null
        @Ignore
        var bookChapterId: String? = null
        @Ignore
        var lastChapterId: String? = null
        @Ignore
        var lastChapterName: String? = null
        @Ignore
        var lastSerialNumber: Long = 0
        @Ignore
        var v: Long = 0
        @Ignore
        var updateTime: Long = 0
        @Ignore
        var createTime: Long = 0
        @Ignore
        var chapterCount: Int = 0
        @Ignore
        private var chapterBlankCount: Int = 0
        @Ignore
        var chapterShortCount: Int = 0
        @Ignore
        var wordCount: Int = 0
        @ColumnInfo(name = "word_count_descp")
        var wordCountDescp: String? = null
        @Ignore
        var readerCount: Int = 0
        @ColumnInfo(name = "reader_count_descp")
        var readerCountDescp: String? = null
        @Ignore
        var dex: Int = 0
        @Ignore
        private var hot: Int = 0
        @ColumnInfo(name = "score")
        private var score: Double = 0.toDouble()
        @Ignore
        var vip: Int = 0
        @Ignore
        var chapterPrice: Float = 0.toFloat()
        @Ignore
        var bookprice: Float = 0.toFloat()
        @Ignore
        var selfPrice: Float = 0.toFloat()
        @Ignore
        var selfBookPrice: Float = 0.toFloat()

        class AttributeBean {
            /**
             * gid : 200008377
             * nid : 0
             */

            var gid: String? = null
            var nid: String? = null
        }
    }
}
