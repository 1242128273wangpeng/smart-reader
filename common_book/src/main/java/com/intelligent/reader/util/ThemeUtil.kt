package com.intelligent.reader.util

import android.content.res.Resources
import android.view.View
import com.intelligent.reader.R
import net.lzbook.kit.constants.Constants

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
                    Constants.MODE == 51 -> // night1
                        color_int = R.color.reading_operation_text_color_first
                    Constants.MODE == 52 -> // day
                        color_int = R.color.reading_operation_text_color_second
                    Constants.MODE == 53 -> // eye
                        color_int = R.color.reading_operation_text_color_third
                    Constants.MODE == 54 -> // powersave
                        color_int = R.color.reading_operation_text_color_fourth
                    Constants.MODE == 55 -> // color -4
                        color_int = R.color.reading_operation_text_color_fifth
                    Constants.MODE == 56 -> // color -5
                        color_int = R.color.reading_operation_text_color_sixth
                    Constants.MODE == 61 -> // night2
                        color_int = R.color.reading_operation_text_color_night
                }
                return color_int
            }

        val modeLoadTextColor: Int
            get() {
                var color = R.color.reading_text_color_first
                when {
                    Constants.MODE == 51 ->
                        color = net.lzbook.kit.R.color.reading_text_color_first
                    Constants.MODE == 52 ->
                        color = net.lzbook.kit.R.color.reading_text_color_second
                    Constants.MODE == 53 ->
                        color = net.lzbook.kit.R.color.reading_text_color_third
                    Constants.MODE == 54 ->
                        color = net.lzbook.kit.R.color.reading_text_color_fourth
                    Constants.MODE == 55 ->
                        color = net.lzbook.kit.R.color.reading_text_color_fifth
                    Constants.MODE == 56 ->
                        color = net.lzbook.kit.R.color.reading_text_color_sixth
                    Constants.MODE == 57 ->
                        color = net.lzbook.kit.R.color.reading_text_color_seventh
                    Constants.MODE == 58 ->
                        color = net.lzbook.kit.R.color.reading_text_color_eighth
                    Constants.MODE == 59 ->
                        color = net.lzbook.kit.R.color.reading_text_color_ninth
                    Constants.MODE == 60 ->
                        color = net.lzbook.kit.R.color.reading_text_color_tenth
                    Constants.MODE == 61 ->
                        color = net.lzbook.kit.R.color.reading_text_color_night
                }
                return color
            }

        val modeLoadBgColor: Int
            get() {
                var color = R.color.reading_backdrop_first
                when {
                    Constants.MODE == 51 ->
                        color = net.lzbook.kit.R.color.reading_backdrop_first
                    Constants.MODE == 52 ->
                        color = net.lzbook.kit.R.color.reading_backdrop_second
                    Constants.MODE == 53 ->
                        color = net.lzbook.kit.R.color.reading_backdrop_third
                    Constants.MODE == 54 ->
                        color = net.lzbook.kit.R.color.reading_backdrop_fourth
                    Constants.MODE == 55 ->
                        color = net.lzbook.kit.R.color.reading_backdrop_fifth
                    Constants.MODE == 56 ->
                        color = net.lzbook.kit.R.color.reading_backdrop_sixth
                    Constants.MODE == 57 ->
                        color = net.lzbook.kit.R.color.reading_backdrop_seventh
                    Constants.MODE == 58 ->
                        color = net.lzbook.kit.R.color.reading_backdrop_eighth
                    Constants.MODE == 59 ->
                        color = net.lzbook.kit.R.color.reading_backdrop_ninth
                    Constants.MODE == 60 ->
                        color = net.lzbook.kit.R.color.reading_backdrop_tenth
                    Constants.MODE == 61 ->
                        color = net.lzbook.kit.R.color.reading_backdrop_night
                }
                return color
            }


        fun getModePrimaryBackground(resources: Resources, view: View) {
            if (Constants.MODE == 51) {// 牛皮纸
                view.setBackgroundResource(R.drawable.read_page_bg_default)
            } else {
                // 通过新的画布，将矩形画新的bitmap上去
                var color_int = R.color.reading_backdrop_first
                when {
                    Constants.MODE == 52 -> // day
                        color_int = R.color.reading_backdrop_second
                    Constants.MODE == 53 -> // eye
                        color_int = R.color.reading_backdrop_third
                    Constants.MODE == 54 -> // powersave
                        color_int = R.color.reading_backdrop_fourth
                    Constants.MODE == 55 -> // color -4
                        color_int = R.color.reading_backdrop_fifth
                    Constants.MODE == 56 -> // color -5
                        color_int = R.color.reading_backdrop_sixth
                    Constants.MODE == 61 -> //night3
                        color_int = R.color.reading_backdrop_night
                }
                view.setBackgroundColor(resources.getColor(color_int))
            }
        }
    }
}
