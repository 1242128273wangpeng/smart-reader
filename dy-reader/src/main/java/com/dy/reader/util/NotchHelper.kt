package com.dy.reader.util

import android.content.Context

fun isNotchScreen(context: Context):Boolean{
    return xiaomiNotch(context) || huaweiNotch(context) || oppoNotch(context) || vivoNotch(context)
}

fun getNotchSize(context: Context):Int{
    var result = 0
    val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = context.resources.getDimensionPixelSize(resourceId)
    }
    return result
}

fun xiaomiNotch(context: Context):Boolean{
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
    return context.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism")
}

private val NOTCH_IN_SCREEN_VOIO = 0x00000020//是否有凹槽
private val ROUNDED_IN_SCREEN_VOIO = 0x00000008//是否有圆角
private fun vivoNotch(context: Context): Boolean {
    var ret = false
    try {
        val cl = context.classLoader
        val FtFeature = cl.loadClass("com.util.FtFeature")
        val get = FtFeature.getMethod("isFeatureSupport", Int::class.javaPrimitiveType)
        ret = get.invoke(FtFeature, NOTCH_IN_SCREEN_VOIO) as Boolean

    } catch (e: Exception) {
    }
    return ret
}