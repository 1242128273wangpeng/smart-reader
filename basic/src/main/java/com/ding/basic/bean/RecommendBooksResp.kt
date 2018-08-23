package com.ding.basic.bean

import java.io.Serializable

/**
 * 项目名称：11m
 * 类描述：
 * 创建人：Zach
 * 创建时间：2017/11/2 0002
 */

class RecommendBooksResp: Serializable {

    var respCode: String? = null
    var message: String? = null

    var data: DataBean? = null

    class DataBean {
        var map: MapBean? = null

        class MapBean {
            var znList: List<ZnListBean>? = null
            var qgList: List<QgListBean>? = null
            var feeList: List<*>? = null

            class ZnListBean {
                var id: String? = null
                var bookId: String? = null
                var bookName: String? = null
                var authorName: String? = null
                var description: String? = null
                var serialStatus: String? = null
                var label: String? = null
                var sourceImageUrl: String? = null
                var imageId: Any? = null
                /**
                 * gid : 0
                 * nid : 0
                 */

                var attribute: AttributeBean? = null
                var host: String? = null
                var url: String? = null
                var terminal: String? = null
                var bookChapterMd5: String? = null
                var bookChapterId: String? = null
                var lastChapterId: String? = null
                var lastChapterName: String? = null
                var lastSerialNumber: Int = 0
                var v: Int = 0
                var updateTime: Long = 0
                var createTime: Long = 0
                var chapterCount: Int = 0
                var chapterBlankCount: Any? = null
                var chapterShortCount: Int = 0
                var wordCount: Int = 0
                var wordCountDescp: String? = null
                var readerCount: Int = 0
                var readerCountDescp: String? = null
                var dex: Int = 0
                var hot: Any? = null
                var score: Double = 0.toDouble()
                    private set
                var vip: Int = 0
                var chapterPrice: Int = 0
                var bookprice: Int = 0
                var selfPrice: Int = 0
                var selfBookPrice: Int = 0

                fun setScore(score: Int) {
                    this.score = score.toDouble()
                }


                class AttributeBean {
                    var gid: String? = null
                    var nid: String? = null
                }

                override fun toString(): String {
                    return "ZnListBean{" +
                            "id='" + id + '\''.toString() +
                            ", bookId='" + bookId + '\''.toString() +
                            ", bookName='" + bookName + '\''.toString() +
                            ", authorName='" + authorName + '\''.toString() +
                            ", serialStatus='" + serialStatus + '\''.toString() +
                            ", label='" + label + '\''.toString() +
                            ", sourceImageUrl='" + sourceImageUrl + '\''.toString() +
                            ", imageId=" + imageId +
                            ", host='" + host + '\''.toString() +
                            ", url='" + url + '\''.toString() +
                            ", terminal='" + terminal + '\''.toString() +
                            ", bookChapterMd5='" + bookChapterMd5 + '\''.toString() +
                            ", bookChapterId='" + bookChapterId + '\''.toString() +
                            ", lastChapterId='" + lastChapterId + '\''.toString() +
                            ", lastChapterName='" + lastChapterName + '\''.toString() +
                            ", lastSerialNumber=" + lastSerialNumber +
                            ", v=" + v +
                            ", updateTime=" + updateTime +
                            ", createTime=" + createTime +
                            ", chapterCount=" + chapterCount +
                            ", chapterBlankCount=" + chapterBlankCount +
                            ", chapterShortCount=" + chapterShortCount +
                            ", wordCount=" + wordCount +
                            ", wordCountDescp='" + wordCountDescp + '\''.toString() +
                            ", readerCount=" + readerCount +
                            ", readerCountDescp='" + readerCountDescp + '\''.toString() +
                            ", dex=" + dex +
                            ", hot=" + hot +
                            ", score=" + score +
                            ", vip=" + vip +
                            ", chapterPrice=" + chapterPrice +
                            ", bookprice=" + bookprice +
                            ", selfPrice=" + selfPrice +
                            ", selfBookPrice=" + selfBookPrice +
                            '}'.toString()
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
                var url: Any? = null
                var author_is_sign: Int = 0
                var avatar: Any? = null
                var chapter_id: String? = null
                var chapter_name: String? = null
                var chapter_sn: Int = 0
                var chapter_content: Any? = null
                var create_time: Long = 0
                var update_time: Long = 0

                override fun toString(): String {
                    return "QgListBean{" +
                            "id='" + id + '\''.toString() +
                            ", bookSourceId='" + bookSourceId + '\''.toString() +
                            ", host='" + host + '\''.toString() +
                            ", bookName='" + bookName + '\''.toString() +
                            ", description='" + description + '\''.toString() +
                            ", serialStatus='" + serialStatus + '\''.toString() +
                            ", is_sign=" + is_sign +
                            ", labels='" + labels + '\''.toString() +
                            ", style='" + style + '\''.toString() +
                            ", ending='" + ending + '\''.toString() +
                            ", image='" + image + '\''.toString() +
                            ", channel='" + channel + '\''.toString() +
                            ", word_count=" + word_count +
                            ", read_count=" + read_count +
                            ", author_id='" + author_id + '\''.toString() +
                            ", author_name='" + author_name + '\''.toString() +
                            ", url=" + url +
                            ", author_is_sign=" + author_is_sign +
                            ", avatar=" + avatar +
                            ", chapter_id='" + chapter_id + '\''.toString() +
                            ", chapter_name='" + chapter_name + '\''.toString() +
                            ", chapter_sn=" + chapter_sn +
                            ", chapter_content=" + chapter_content +
                            ", create_time=" + create_time +
                            ", update_time=" + update_time +
                            '}'.toString()
                }
            }

        }
    }
}