package com.dingyue.statistics.utils

import android.content.SharedPreferences

class SharedPreferencesUtil(private val sp: SharedPreferences) {

    fun putBoolean(key: String, value: Boolean) {
        sp.edit().putBoolean(key, value).apply()
    }

    fun putInt(key: String, value: Int) {
        sp.edit().putInt(key, value).apply()
    }

    fun putString(key: String, value: String) {
        sp.edit().putString(key, value).apply()
    }

    fun putLong(key: String, value: Long) {
        sp.edit().putLong(key, value).apply()
    }

    fun getBoolean(key: String): Boolean {
        return sp.getBoolean(key, false)
    }

    fun getInt(key: String): Int {
        return sp.getInt(key, 0)
    }

    fun getString(key: String): String {
        return sp.getString(key, "")
    }

    fun getString(key: String, defaultValue: String): String? {
        return sp.getString(key, defaultValue)
    }

    fun getLong(key: String): Long {
        return sp.getLong(key, 0)
    }
}

