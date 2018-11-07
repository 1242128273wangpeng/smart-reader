package com.dy.reader.util

import android.annotation.SuppressLint
import android.content.Context
import com.dy.reader.R
import net.lzbook.kit.utils.AppLog

fun isNotchScreen(context: Context): Boolean {
    return xiaomiNotch(context) || huaweiNotch(context) || oppoNotch(context) || vivoNotch(context)
}

fun getNotchSize(context: Context): Int {
    var result = 0
    val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = context.resources.getDimensionPixelSize(resourceId)
    }
    return result
}

fun xiaomiNotch(context: Context): Boolean {
    var xiaomi = false
    try {
        val cl = context.classLoader
        val HwNotchSizeUtil = cl.loadClass("android.os.SystemProperties")
        val get = HwNotchSizeUtil.getMethod("getInt", String::class.java, Int::class.java)
        xiaomi = get.invoke(HwNotchSizeUtil, "ro.miui.notch", 0) == 1
    } catch (e: Exception) {
    }
    return xiaomi
}

private fun huaweiNotch(context: Context): Boolean {
    var ret = false
    try {
        val cl = context.classLoader
        val HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil")
        val get = HwNotchSizeUtil.getMethod("hasNotchInScreen")
        ret = get.invoke(HwNotchSizeUtil) as Boolean
    } catch (e: Exception) {
    }

    return ret
}

private fun oppoNotch(context: Context): Boolean {
    if (context.packageManager != null) {
        return context.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism")
    } else {
        return false
    }
}

private val NOTCH_IN_SCREEN_VOIO = 0x00000020//是否有凹槽
private val ROUNDED_IN_SCREEN_VOIO = 0x00000008//是否有圆角

@SuppressLint("PrivateApi")
fun vivoNotch(context: Context): Boolean {
    var ret = false
    try {
        val classLoader = context.classLoader
        val ftFeature = classLoader.loadClass("android.util.FtFeature")
        val method = ftFeature.getMethod("isFeatureSupport", Int::class.javaPrimitiveType)
        ret = method.invoke(ftFeature, NOTCH_IN_SCREEN_VOIO) as Boolean
    } catch (e: ClassNotFoundException) {
        AppLog.w("Notch", "vivoNotch ClassNotFoundException")
    } catch (e: NoSuchMethodException) {
        AppLog.w("Notch", "vivoNotch NoSuchMethodException")
    } catch (e: Exception) {
        AppLog.w("Notch", "vivoNotch Exception")
    } finally {
        return ret
    }
}

private var vivoNotchSize: Int = 0

fun getVivoNotchSize(context: Context): Int {
    if (vivoNotchSize == 0) {
        vivoNotchSize = context.resources.getDimension(R.dimen.notch_vivo).toInt()
    }
    return vivoNotchSize
}

private var vivoRoundedSize: Int = 0

fun getVivoRoundedSize(context: Context): Int {
    if (vivoRoundedSize == 0) {
        vivoRoundedSize = context.resources.getDimension(R.dimen.rounded_vivo).toInt()
    }
    return vivoRoundedSize
}
