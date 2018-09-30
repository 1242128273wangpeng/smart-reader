package com.dy.reader.util

import android.graphics.Typeface
import com.dy.reader.service.FontDownLoadService
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import org.jetbrains.annotations.NotNull
import java.io.File
import java.util.*

/**
 * Created on 17/7/12.
 * Created by crazylei.
 */

object TypefaceUtil {

    const val TYPEFACE_SYSTEM = 0x80
    const val TYPEFACE_SIYUAN_SONG = 0x81
    const val TYPEFACE_ZHUSHITI = 0x82
    const val TYPEFACE_SIYUAN_HEI = 0x83

    private val typefaceTable = Hashtable<String, Typeface>()


    fun loadTypeface(type: Int): Typeface? {
        return when (type) {
            TYPEFACE_SYSTEM -> Typeface.DEFAULT
            TYPEFACE_SIYUAN_SONG -> initTypeface(FontDownLoadService.FONT_SIYUAN_SONG)
            TYPEFACE_ZHUSHITI -> initTypeface(FontDownLoadService.FONT_ZHUSHITI)
            TYPEFACE_SIYUAN_HEI -> initTypeface(FontDownLoadService.FONT_SIYUAN_HEI)
            else -> Typeface.DEFAULT
        }
    }

    private fun initTypeface(name: String): Typeface? {

        return if (!typefaceTable.containsKey(name)) {
            val typeface: Typeface?
            try {
                typeface = Typeface.createFromFile(File(FontDownLoadService.getFontPath(name)))
                if (typeface != null) {
                    typefaceTable[name] = typeface
                }
                typeface
            } catch (exception: Exception) {
                exception.printStackTrace()
                SPUtils.putDefaultSharedString(SPKey.READER_TYPE_FACE, name)
                Typeface.DEFAULT
            }
        } else {
            typefaceTable[name]
        }
    }

    fun loadTypefaceTag(@NotNull type: Int): String {
        return when (type) {
            TYPEFACE_SYSTEM -> "默认字体"
            TYPEFACE_SIYUAN_SONG -> "思源宋体"
            TYPEFACE_ZHUSHITI -> "杨任东竹石"
            TYPEFACE_SIYUAN_HEI -> "思源黑体"
            else -> "默认字体"
        }
    }

    fun getTypefaceCode(fontName: String): Int {
        return when (fontName) {
            FontDownLoadService.FONT_DEFAULT -> TYPEFACE_SYSTEM
            FontDownLoadService.FONT_SIYUAN_SONG -> TYPEFACE_SIYUAN_SONG
            FontDownLoadService.FONT_ZHUSHITI -> TYPEFACE_ZHUSHITI
            FontDownLoadService.FONT_SIYUAN_HEI -> TYPEFACE_SIYUAN_HEI
            else -> TYPEFACE_SYSTEM
        }
    }
}