package com.ding.basic.bean

import java.io.Serializable

/**
 * Created on 2018/3/19.
 * Created by crazylei.
 */
class RecommendBean : Serializable {

    //book_source_id
    var id: String = ""

    var bookId: String = ""

    var bookName: String? = null

    var authorName: String? = null

    var description: String? = null

    var serialStatus: String? = null

    var label: String? = null

    var sourceImageUrl: String? = null

    var host: String? = null

    var bookChapterId: String = ""

    var lastChapterId: String? = null

    var lastChapterName: String? = null

    var lastSerialNumber: Long = 0

    var updateTime: Long = 0

    var chapterCount: Int = 0

    var chapterBlankCount: Int? = null

    var chapterShortCount: Int? = null

    //字数
    var wordCount: Int? = null

    //字数
    var wordCountDescp: String? = null

    var uv: Long = 0

    //多少人在读
    var readerCount: Int? = null

    //多少人在读
    var readerCountDescp: String? = null

    //书籍评分
    var score: Float = 0.0f

    //一级分类
    var genre: String? = null

    //二级分类
    var subGenre: String? = null

    //日阅读用户数
    var dayUv: String? = null

    //周阅读用户数
    var weekUv: String? = null

    //书籍类型
    var bookType: String? = null
}