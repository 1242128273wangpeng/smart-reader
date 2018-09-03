package com.ding.basic.bean

import java.io.Serializable

/**
 * Created by Administrator on 2017\11\1 0001.
 */

class CoverRecommendBean : Serializable {

    var respCode: String? = null
    var message: String? = null
    var data: DataBean? = null

    class DataBean {

        var map: MapBean? = null

        class MapBean {
            var znList: List<ZnListBean>? = null
            var qgList: List<QgListBean>? = null
            var feeList: List<FeeListBean>? = null

            class ZnListBean {

                var id: String? = null
                var bookId: String? = null
                var bookName: String? = null
                var authorName: String? = null
                var description: String? = null
                var serialStatus: String? = null
                var label: String? = null
                var sourceImageUrl: String? = null
                private var imageId: String? = null
                var host: String? = null
                var url: String? = null
                var terminal: String? = null
                var bookChapterMd5: String? = null
                var bookChapterId: String? = null
                var lastChapterId: String? = null
                var lastChapterName: String? = null
                var lastSerialNumber: Long = 0
                    private set
                var v: Long = 0
                    private set
                var updateTime: Long = 0
                var createTime: Long = 0
                var chapterCount: Int = 0
                private var chapterBlankCount: Int = 0
                var chapterShortCount: Int = 0
                var wordCount: Int = 0
                var wordCountDescp: String? = null
                var readerCount: Int = 0
                var readerCountDescp: String? = null
                var dex: Int = 0
                private var hot: Int = 0
                var score: Double = 0.toDouble()
                    private set
                var vip: Int = 0
                var chapterPrice: Float = 0.toFloat()
                    private set
                var bookprice: Float = 0.toFloat()
                    private set
                var selfPrice: Float = 0.toFloat()
                    private set
                var selfBookPrice: Float = 0.toFloat()
                    private set

                fun getImageId(): Any? {
                    return imageId
                }

                fun setImageId(imageId: String) {
                    this.imageId = imageId
                }

                fun setLastSerialNumber(lastSerialNumber: Int) {
                    this.lastSerialNumber = lastSerialNumber.toLong()
                }

                fun setV(v: Int) {
                    this.v = v.toLong()
                }

                fun getChapterBlankCount(): Any {
                    return chapterBlankCount
                }

                fun setChapterBlankCount(chapterBlankCount: Int) {
                    this.chapterBlankCount = chapterBlankCount
                }

                fun getHot(): Any {
                    return hot
                }

                fun setHot(hot: Int) {
                    this.hot = hot
                }

                fun setScore(score: Int) {
                    this.score = score.toDouble()
                }

                fun setChapterPrice(chapterPrice: Int) {
                    this.chapterPrice = chapterPrice.toFloat()
                }

                fun setBookprice(bookprice: Int) {
                    this.bookprice = bookprice.toFloat()
                }

                fun setSelfPrice(selfPrice: Int) {
                    this.selfPrice = selfPrice.toFloat()
                }

                fun setSelfBookPrice(selfBookPrice: Int) {
                    this.selfBookPrice = selfBookPrice.toFloat()
                }
            }

            class FeeListBean {

                var id: String? = null
                var bookId: String? = null
                var bookName: String? = null
                var authorName: String? = null
                var description: String? = null
                var serialStatus: String? = null
                var label: String? = null
                var sourceImageUrl: String? = null
                private var imageId: String? = null
                var attribute: FeeListBean.AttributeBean? = null
                var host: String? = null
                var url: String? = null
                var terminal: String? = null
                var bookChapterMd5: String? = null
                var bookChapterId: String? = null
                var lastChapterId: String? = null
                var lastChapterName: String? = null
                var lastSerialNumber: Long = 0
                    private set
                var v: Long = 0
                    private set
                var updateTime: Long = 0
                var createTime: Long = 0
                var chapterCount: Int = 0
                private var chapterBlankCount: Int = 0
                var chapterShortCount: Int = 0
                var wordCount: Int = 0
                var wordCountDescp: String? = null
                var readerCount: Int = 0
                var readerCountDescp: String? = null
                var dex: Int = 0
                private var hot: Int = 0
                var score: Double = 0.toDouble()
                    private set
                var vip: Int = 0
                var chapterPrice: Float = 0.toFloat()
                    private set
                var bookprice: Float = 0.toFloat()
                    private set
                var selfPrice: Float = 0.toFloat()
                    private set
                var selfBookPrice: Float = 0.toFloat()
                    private set

                fun getImageId(): Any? {
                    return imageId
                }

                fun setImageId(imageId: String) {
                    this.imageId = imageId
                }

                fun setLastSerialNumber(lastSerialNumber: Int) {
                    this.lastSerialNumber = lastSerialNumber.toLong()
                }

                fun setV(v: Int) {
                    this.v = v.toLong()
                }

                fun getChapterBlankCount(): Any {
                    return chapterBlankCount
                }

                fun setChapterBlankCount(chapterBlankCount: Int) {
                    this.chapterBlankCount = chapterBlankCount
                }

                fun getHot(): Any {
                    return hot
                }

                fun setHot(hot: Int) {
                    this.hot = hot
                }

                fun setScore(score: Int) {
                    this.score = score.toDouble()
                }

                fun setChapterPrice(chapterPrice: Int) {
                    this.chapterPrice = chapterPrice.toFloat()
                }

                fun setBookprice(bookprice: Int) {
                    this.bookprice = bookprice.toFloat()
                }

                fun setSelfPrice(selfPrice: Int) {
                    this.selfPrice = selfPrice.toFloat()
                }

                fun setSelfBookPrice(selfBookPrice: Int) {
                    this.selfBookPrice = selfBookPrice.toFloat()
                }

                class AttributeBean {
                    /**
                     * gid : 200002434
                     * nid : 0
                     */

                    var gid: String? = null
                    var nid: String? = null
                }
            }

            class QgListBean {

                var id: String? = null
                var bookSourceId: String? = null
                var host: String? = null
                var bookName: String? = null
                var description: String? = null
                var serialStatus: String? = null
                var is_sign: Int = 0
                var labels: String? = null
                var style: String? = null
                var ending: String? = null
                var image: String? = null
                var channel: String? = null
                var word_count: Int = 0
                var read_count: Int = 0
                var author_id: String? = null
                var author_name: String? = null
                private var url: String? = null
                var author_is_sign: Int = 0
                var avatar: Any? = null
                var chapter_id: String? = null
                var chapter_name: String? = null
                var chapter_sn: Int = 0
                private var chapter_content: String? = null
                var create_time: Long = 0
                var update_time: Long = 0

                fun getUrl(): Any? {
                    return url
                }

                fun setUrl(url: String) {
                    this.url = url
                }

                fun getChapter_content(): Any? {
                    return chapter_content
                }

                fun setChapter_content(chapter_content: String) {
                    this.chapter_content = chapter_content
                }
            }
        }
    }
}
