package com.intelligent.reader.read.util

import com.intelligent.reader.read.exception.ReadCustomException
import com.intelligent.reader.read.mode.NovelPageBean
import java.util.*

/**
 * Created by wt on 2018/1/16.
 */
object ReadQueryUtil {

    //通过偏移量获取页码Index
    fun findPageIndexByOffset(offset: Int, chapterSeparate: ArrayList<NovelPageBean>): Int {
        if (chapterSeparate.isEmpty()) throw ReadCustomException.PageIndexException("集合为空")
        val filter = chapterSeparate.filter {
            it.offset <= offset
        }
        return filter.size
    }

    //通过偏移量获取章节
    fun findNovelPageBeanByOffset(offset: Int, chapterSeparate: ArrayList<NovelPageBean>): NovelPageBean {
        if (chapterSeparate.isEmpty()) throw ReadCustomException.PageOffsetException("集合为空")
        //过滤集合 小于offset的最后一个元素
        val filter = chapterSeparate.filter {
            it.offset <= offset
        }
        return filter.last()
    }
}