package com.ding.basic.util

import android.content.Context
import android.content.SharedPreferences.Editor
import android.preference.PreferenceManager
import android.text.TextUtils
import com.google.gson.Gson
import java.util.*

/**
 * Desc 系统默认 SharedPreference
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/9/3 16:25
 */
fun Context.editShared(job: Editor.() -> Unit) {
    val shared = PreferenceManager.getDefaultSharedPreferences(this)
    val editor = shared.edit()
    editor.job()
    editor.apply()
}

fun Context.getSharedBoolean(key: String, defValue: Boolean = false): Boolean {
    val shared = PreferenceManager.getDefaultSharedPreferences(this)
    return shared.getBoolean(key, defValue)
}

fun Context.getSharedInt(key: String, defValue: Int = 0): Int {
    val shared = PreferenceManager.getDefaultSharedPreferences(this)
    return shared.getInt(key, defValue)
}


fun Context.getSharedString(key: String, defValue: String = ""): String {
    val shared = PreferenceManager.getDefaultSharedPreferences(this)
    return shared.getString(key, defValue)
}

fun Context.getSharedLong(key: String, defValue: Long = 0L): Long {
    val shared = PreferenceManager.getDefaultSharedPreferences(this)
    return shared.getLong(key, defValue)
}

fun <T> Context.getSharedObject(key: String, clazz: Class<T>): T? {
    val json = getSharedString(key, "")
    if (TextUtils.isEmpty(json)) {
        return null
    }
    return try {
        val gson = Gson()
        gson.fromJson<T>(json, clazz)
    } catch (e: Exception) {
        null
    }

}

fun Editor.putObject(key: String, obj: Any) {
    val gson = Gson()
    val json = gson.toJson(obj)
    putString(key, json)
}

fun isSameDay(lastTime: Long, currentTime: Long):Boolean {
    val pre = Calendar.getInstance()
    val predate = Date(currentTime)
    pre.time = predate

    val cal = Calendar.getInstance()
    val date = Date(lastTime)
    cal.time = date;

    if (cal.get(Calendar.YEAR) == (pre.get(Calendar.YEAR))) {
        val sameDay = cal.get(Calendar.DAY_OF_YEAR) - pre.get(Calendar.DAY_OF_YEAR);
        if (sameDay == 0) {
            return true
        }
    }
    return false
}