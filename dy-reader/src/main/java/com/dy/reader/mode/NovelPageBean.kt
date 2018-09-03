package com.dy.reader.mode

/**
 * 分页数据封装
 * Created by wt on 2017/12/27.
 */
data class NovelPageBean(
        var lines: ArrayList<NovelLineBean>,
        var offset: Int,
        var chapterNameLines: ArrayList<NovelLineBean>
) {
    var height: Float = 0.0f
    var isLastPage: Boolean = false
    var contentLength:Int = 0
    var adType = ""
}