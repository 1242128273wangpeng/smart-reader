package com.intelligent.reader.read.util

import android.content.res.Resources
import com.intelligent.reader.R
import com.intelligent.reader.read.exception.ReadCustomException
import com.intelligent.reader.read.mode.NovelPageBean
import net.lzbook.kit.data.bean.ReadConfig
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

    fun getColor(resources:Resources):Int{
        //设置TextColor
        var colorInt = when (ReadConfig.MODE) {
            51 -> R.color.reading_operation_text_color_first
            52 -> R.color.reading_text_color_second
            53 -> R.color.reading_text_color_third
            54 -> R.color.reading_text_color_fourth
            55 -> R.color.reading_text_color_fifth
            56 -> R.color.reading_text_color_sixth
            61 -> R.color.reading_text_color_night
            else -> R.color.reading_operation_text_color_first
        }
        return resources.getColor(colorInt)
    }

    fun getHomePageColor(resources:Resources):Int{
        //设置TextColor
        var colorInt = when (ReadConfig.MODE) {
            51 -> R.color.reading_text_color_first
            52 -> R.color.reading_text_color_second
            53 -> R.color.reading_text_color_third
            54 -> R.color.reading_text_color_fourth
            55 -> R.color.reading_text_color_fifth
            56 -> R.color.reading_text_color_sixth
            61 -> R.color.reading_text_color_night
            else -> R.color.reading_text_color_first
        }
        return resources.getColor(colorInt)
    }
}