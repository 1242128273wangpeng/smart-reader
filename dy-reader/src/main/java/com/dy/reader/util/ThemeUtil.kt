package com.dy.reader.util

import android.content.res.Resources
import android.view.View
import com.dy.reader.R
import com.dy.reader.setting.ReaderSettings

/**
 * Created by yuchao on 2018/4/28 0028.
 */
object ThemeUtil {
    val readerSettings = ReaderSettings.instance
    val modePrimaryColor: Int
        get() {
            var color_int = R.color.reading_operation_text_color_first
            when {
                readerSettings.readThemeMode == 51 -> // night1
                    color_int = R.color.reading_operation_text_color_first
                readerSettings.readThemeMode == 52 -> // day
                    color_int = R.color.reading_operation_text_color_second
                readerSettings.readThemeMode == 53 -> // eye
                    color_int = R.color.reading_operation_text_color_third
                readerSettings.readThemeMode == 54 -> // powersave
                    color_int = R.color.reading_operation_text_color_fourth
                readerSettings.readThemeMode == 55 -> // color -4
                    color_int = R.color.reading_operation_text_color_fifth
                readerSettings.readThemeMode == 56 -> // color -5
                    color_int = R.color.reading_operation_text_color_sixth
                readerSettings.readThemeMode == 61 -> // night2
                    color_int = R.color.reading_operation_text_color_night
            }
            return color_int
        }

    val modeLoadTextColor: Int
        get() {
            var color = R.color.reading_text_color_first
            when {
                readerSettings.readThemeMode == 51 ->
                    color = R.color.reading_text_color_first
                readerSettings.readThemeMode == 52 ->
                    color = R.color.reading_text_color_second
                readerSettings.readThemeMode == 53 ->
                    color = R.color.reading_text_color_third
                readerSettings.readThemeMode == 54 ->
                    color = R.color.reading_text_color_fourth
                readerSettings.readThemeMode == 55 ->
                    color = R.color.reading_text_color_fifth
                readerSettings.readThemeMode == 56 ->
                    color = R.color.reading_text_color_sixth
                readerSettings.readThemeMode == 57 ->
                    color = R.color.reading_text_color_seventh
                readerSettings.readThemeMode == 58 ->
                    color = R.color.reading_text_color_eighth
                readerSettings.readThemeMode == 59 ->
                    color = R.color.reading_text_color_ninth
                readerSettings.readThemeMode == 60 ->
                    color = R.color.reading_text_color_tenth
                readerSettings.readThemeMode == 61 ->
                    color = R.color.reading_text_color_night
            }
            return color
        }

    val modeLoadBgColor: Int
        get() {
            var color = R.color.reading_backdrop_first
            when {
                readerSettings.readThemeMode == 51 ->
                    color = R.color.reading_backdrop_first
                readerSettings.readThemeMode == 52 ->
                    color = R.color.reading_backdrop_second
                readerSettings.readThemeMode == 53 ->
                    color = R.color.reading_backdrop_third
                readerSettings.readThemeMode == 54 ->
                    color = R.color.reading_backdrop_fourth
                readerSettings.readThemeMode == 55 ->
                    color = R.color.reading_backdrop_fifth
                readerSettings.readThemeMode == 56 ->
                    color = R.color.reading_backdrop_sixth
                readerSettings.readThemeMode == 57 ->
                    color = R.color.reading_backdrop_seventh
                readerSettings.readThemeMode == 58 ->
                    color = R.color.reading_backdrop_eighth
                readerSettings.readThemeMode == 59 ->
                    color = R.color.reading_backdrop_ninth
                readerSettings.readThemeMode == 60 ->
                    color = R.color.reading_backdrop_tenth
                readerSettings.readThemeMode == 61 ->
                    color = R.color.reading_backdrop_night
            }
            return color
        }


    fun getModePrimaryBackground(resources: Resources, view: View) {
        if (readerSettings.readThemeMode == 51) {// 牛皮纸
            view.setBackgroundResource(R.drawable.read_page_bg_default)
        } else {
            // 通过新的画布，将矩形画新的bitmap上去
            var color_int = R.color.reading_backdrop_first
            when {
                readerSettings.readThemeMode == 52 -> // day
                    color_int = R.color.reading_backdrop_second
                readerSettings.readThemeMode == 53 -> // eye
                    color_int = R.color.reading_backdrop_third
                readerSettings.readThemeMode == 54 -> // powersave
                    color_int = R.color.reading_backdrop_fourth
                readerSettings.readThemeMode == 55 -> // color -4
                    color_int = R.color.reading_backdrop_fifth
                readerSettings.readThemeMode == 56 -> // color -5
                    color_int = R.color.reading_backdrop_sixth
                readerSettings.readThemeMode == 61 -> //night3
                    color_int = R.color.reading_backdrop_night
            }
            view.setBackgroundColor(resources.getColor(color_int))
        }
    }

    fun getBackgroundColor(resources:Resources): Int {
        var color_int = R.color.reading_backdrop_first
        when (readerSettings.readThemeMode){
            52 -> // day
                color_int = R.color.reading_backdrop_second
            53 -> // eye
                color_int = R.color.reading_backdrop_third
            54 -> // powersave
                color_int = R.color.reading_backdrop_fourth
            55 -> // color -4
                color_int = R.color.reading_backdrop_fifth
            56 -> // color -5
                color_int = R.color.reading_backdrop_sixth
            61 -> //night3
                color_int = R.color.reading_backdrop_night
        }
        return resources.getColor(color_int)
    }

    fun getTitleColor(resources:Resources):Int{
        //设置电池,转码阅读,原网页等字体色
        var colorInt = when (readerSettings.readThemeMode) {
            51 -> R.color.reading_operation_text_color_first
            52 -> R.color.reading_operation_text_color_second
            53 -> R.color.reading_operation_text_color_third
            54 -> R.color.reading_operation_text_color_fourth
            55 -> R.color.reading_operation_text_color_fifth
            56 -> R.color.reading_operation_text_color_sixth
            61 -> R.color.reading_operation_text_color_night
            else -> R.color.reading_operation_text_color_first
        }
        return resources.getColor(colorInt)
    }

    fun getTextColor(resources:Resources):Int{
        //设置阅读正文字体色
        var colorInt = when (readerSettings.readThemeMode) {
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