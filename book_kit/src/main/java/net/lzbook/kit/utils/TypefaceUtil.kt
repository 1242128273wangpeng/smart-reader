package net.lzbook.kit.utils

import android.content.Context
import android.graphics.Typeface
import android.support.annotation.NonNull
import net.lzbook.kit.app.BaseBookApplication
import org.jetbrains.annotations.NotNull

import java.util.Hashtable

/**
 * Created on 17/7/12.
 * Created by crazylei.
 */

object TypefaceUtil {

    val TYPEFACE_SYSTEM = 0x80
    val TYPEFACE_ZHENG_KAI = 0x81

    private val typefaceTable = Hashtable<String, Typeface>()


    fun loadTypeface(type: Int): Typeface? {
        return when (type) {
            TYPEFACE_SYSTEM -> Typeface.DEFAULT
            TYPEFACE_ZHENG_KAI -> initTypeface(BaseBookApplication.getGlobalContext(), "fss.ttf")
            else -> Typeface.DEFAULT
        }
    }

    private fun initTypeface(@NonNull context: Context, @NonNull name: String): Typeface? {

        return if (!typefaceTable.containsKey(name)) {
            val typeface: Typeface?
            try {
                typeface = Typeface.createFromAsset(context.applicationContext.assets, "fonts/$name")
                if (typeface != null) {
                    typefaceTable[name] = typeface
                }
                typeface
            } catch (exception: Exception) {
                exception.printStackTrace()
                Typeface.DEFAULT
            }
        } else {
            typefaceTable[name]
        }
    }

    fun loadTypefaceTag(@NotNull type: Int): String {
        return when(type) {
            TYPEFACE_SYSTEM -> "默认字体"
            TYPEFACE_ZHENG_KAI -> "台湾正楷"
            else -> "默认字体"
        }
    }
}