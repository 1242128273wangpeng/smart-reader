package com.intelligent.reader.util

import android.content.res.Resources
import android.view.View
import com.intelligent.reader.R
import net.lzbook.kit.data.bean.ReadConfig

/**
 * @author lijun Lee
 * @desc 主题管理
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/11/2 14:53
 */

class ThemeUtil {

    companion object {
        val modePrimaryColor: Int
            get() {
                var color_int = R.color.reading_operation_text_color_first
                when {
                    ReadConfig.MODE == 51 -> // night1
                        color_int = R.color.reading_operation_text_color_first
                    ReadConfig.MODE == 52 -> // day
                        color_int = R.color.reading_operation_text_color_second
                    ReadConfig.MODE == 53 -> // eye
                        color_int = R.color.reading_operation_text_color_third
                    ReadConfig.MODE == 54 -> // powersave
                        color_int = R.color.reading_operation_text_color_fourth
                    ReadConfig.MODE == 55 -> // color -4
                        color_int = R.color.reading_operation_text_color_fifth
                    ReadConfig.MODE == 56 -> // color -5
                        color_int = R.color.reading_operation_text_color_sixth
                    ReadConfig.MODE == 61 -> // night2
                        color_int = R.color.reading_operation_text_color_night
                }
                return color_int
            }

        val modeLoadTextColor: Int
            get() {
                var color = R.color.reading_text_color_first
                when {
                    ReadConfig.MODE == 51 ->
                        color = net.lzbook.kit.R.color.reading_text_color_first
                    ReadConfig.MODE == 52 ->
                        color = net.lzbook.kit.R.color.reading_text_color_second
                    ReadConfig.MODE == 53 ->
                        color = net.lzbook.kit.R.color.reading_text_color_third
                    ReadConfig.MODE == 54 ->
                        color = net.lzbook.kit.R.color.reading_text_color_fourth
                    ReadConfig.MODE == 55 ->
                        color = net.lzbook.kit.R.color.reading_text_color_fifth
                    ReadConfig.MODE == 56 ->
                        color = net.lzbook.kit.R.color.reading_text_color_sixth
                    ReadConfig.MODE == 57 ->
                        color = net.lzbook.kit.R.color.reading_text_color_seventh
                    ReadConfig.MODE == 58 ->
                        color = net.lzbook.kit.R.color.reading_text_color_eighth
                    ReadConfig.MODE == 59 ->
                        color = net.lzbook.kit.R.color.reading_text_color_ninth
                    ReadConfig.MODE == 60 ->
                        color = net.lzbook.kit.R.color.reading_text_color_tenth
                    ReadConfig.MODE == 61 ->
                        color = net.lzbook.kit.R.color.reading_text_color_night
                }
                return color
            }

        val modeLoadBgColor: Int
            get() {
                var color = R.color.reading_backdrop_first
                when {
                    ReadConfig.MODE == 51 ->
                        color = net.lzbook.kit.R.color.reading_backdrop_first
                    ReadConfig.MODE == 52 ->
                        color = net.lzbook.kit.R.color.reading_backdrop_second
                    ReadConfig.MODE == 53 ->
                        color = net.lzbook.kit.R.color.reading_backdrop_third
                    ReadConfig.MODE == 54 ->
                        color = net.lzbook.kit.R.color.reading_backdrop_fourth
                    ReadConfig.MODE == 55 ->
                        color = net.lzbook.kit.R.color.reading_backdrop_fifth
                    ReadConfig.MODE == 56 ->
                        color = net.lzbook.kit.R.color.reading_backdrop_sixth
                    ReadConfig.MODE == 57 ->
                        color = net.lzbook.kit.R.color.reading_backdrop_seventh
                    ReadConfig.MODE == 58 ->
                        color = net.lzbook.kit.R.color.reading_backdrop_eighth
                    ReadConfig.MODE == 59 ->
                        color = net.lzbook.kit.R.color.reading_backdrop_ninth
                    ReadConfig.MODE == 60 ->
                        color = net.lzbook.kit.R.color.reading_backdrop_tenth
                    ReadConfig.MODE == 61 ->
                        color = net.lzbook.kit.R.color.reading_backdrop_night
                }
                return color
            }


        fun getModePrimaryBackground(resources: Resources, view: View) {
            if (ReadConfig.MODE == 51) {// 牛皮纸
                view.setBackgroundResource(R.drawable.read_page_bg_default)
            } else {
                // 通过新的画布，将矩形画新的bitmap上去
                var color_int = R.color.reading_backdrop_first
                when {
                    ReadConfig.MODE == 52 -> // day
                        color_int = R.color.reading_backdrop_second
                    ReadConfig.MODE == 53 -> // eye
                        color_int = R.color.reading_backdrop_third
                    ReadConfig.MODE == 54 -> // powersave
                        color_int = R.color.reading_backdrop_fourth
                    ReadConfig.MODE == 55 -> // color -4
                        color_int = R.color.reading_backdrop_fifth
                    ReadConfig.MODE == 56 -> // color -5
                        color_int = R.color.reading_backdrop_sixth
                    ReadConfig.MODE == 61 -> //night3
                        color_int = R.color.reading_backdrop_night
                }
                view.setBackgroundColor(resources.getColor(color_int))
            }
        }
    }
}
