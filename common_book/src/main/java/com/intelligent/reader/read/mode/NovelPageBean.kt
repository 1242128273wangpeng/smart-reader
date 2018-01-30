package com.intelligent.reader.read.mode

import android.view.ViewGroup
import net.lzbook.kit.data.bean.NovelLineBean

/**
 * 分页数据封装
 * Created by wt on 2017/12/27.
 */
data class NovelPageBean(
        var lines: ArrayList<NovelLineBean>,
        var offset: Int,
        var chapterNameLines: ArrayList<NovelLineBean>
) {
    var adBigView: ViewGroup? = null
    var adSmallView: ViewGroup? = null
    var isAd: Boolean = false
    var height: Float = 0.0f
    var isLastPage: Boolean = false
    var contentLength:Int = 0
}