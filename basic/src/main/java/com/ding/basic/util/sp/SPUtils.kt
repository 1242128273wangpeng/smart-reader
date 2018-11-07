package com.ding.basic.util.sp

import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.text.TextUtils
import com.ding.basic.net.Config
import com.google.gson.Gson

/**
 * Desc   SharedPreference操作工具
 * Author yangweining
 * Mail   weining_yang@dingyuegroup.cn
 * Date   2018/9/20 16:25
 */
object SPUtils {

    private val defaultPreferences = PreferenceManager.getDefaultSharedPreferences(Config.getContext())
        get() {
            return field ?: PreferenceManager.getDefaultSharedPreferences(Config.getContext())
        }


    /**
     * 系统默认Shared 批量操作
     */
    fun editDefaultShared(job: SharedPreferences.Editor.() -> Unit) {
        val shared = PreferenceManager.getDefaultSharedPreferences(Config.getContext())
        val editor = shared.edit()
        editor.job()
        editor.apply()
    }

    /**
     * 在线配置Shared 批量操作
     */
    fun editOnlineConfigShared(job: SharedPreferences.Editor.() -> Unit) {
        val shared = Config.getContext()!!.getSharedPreferences(SPKey.getSHAREDPREFERENCES_KEY(), 0)
        val editor = shared.edit()
        editor.job()
        editor.apply()
    }

    /**
     * 获取系统默认Shared Boolean
     */
    fun getDefaultSharedBoolean(key: String, defValue: Boolean = false): Boolean {
        val shared = PreferenceManager.getDefaultSharedPreferences(Config.getContext())
        return shared.getBoolean(key, defValue)
    }

    /**
     * 获取系统默认Shared Int
     */
    fun getDefaultSharedInt(key: String, defValue: Int = 0): Int {
        val shared = PreferenceManager.getDefaultSharedPreferences(Config.getContext())
        return shared.getInt(key, defValue)
    }

    /**
     * 获取系统默认Shared String
     */
    fun getDefaultSharedString(key: String, defValue: String = ""): String {
        val shared = PreferenceManager.getDefaultSharedPreferences(Config.getContext())
        return shared.getString(key, defValue)
    }

    /**
     * 获取系统默认Shared Long
     */
    fun getDefaultSharedLong(key: String, defValue: Long = 0L): Long {
        val shared = PreferenceManager.getDefaultSharedPreferences(Config.getContext())
        return shared.getLong(key, defValue)
    }

    /**
     * 获取系统默认Shared Object
     */
    fun <T> getDefaultSharedObject(key: String, clazz: Class<T>): T? {
        val json = getDefaultSharedString(key, "")
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

    /**
     * 获取在线配置Shared Boolean
     */
    fun getOnlineConfigSharedBoolean(key: String, defValue: Boolean = false): Boolean {
        val shared = Config.getContext()!!.getSharedPreferences(SPKey.getSHAREDPREFERENCES_KEY(), 0)
        return shared.getBoolean(key, defValue)
    }

    /**
     * 获取在线配置Shared Int
     */
    fun getOnlineConfigSharedInt(key: String, defValue: Int = 0): Int {
        val shared = Config.getContext()!!.getSharedPreferences(SPKey.getSHAREDPREFERENCES_KEY(), 0)
        return shared.getInt(key, defValue)
    }

    /**
     * 获取在线配置Shared String
     */
    fun getOnlineConfigSharedString(key: String, defValue: String = ""): String {
        val shared = Config.getContext()!!.getSharedPreferences(SPKey.getSHAREDPREFERENCES_KEY(), 0)
        return shared.getString(key, defValue)
    }

    /**
     * 获取在线配置Shared Long
     */
    fun getOnlineConfigSharedLong(key: String, defValue: Long = 0L): Long {
        val shared = Config.getContext()!!.getSharedPreferences(SPKey.getSHAREDPREFERENCES_KEY(), 0)
        return shared.getLong(key, defValue)
    }

    /**
     * 获取在线配置Shared Object
     */
    fun <T> getOnlineConfigSharedObject(key: String, clazz: Class<T>): T? {
        val json = getOnlineConfigSharedString(key, "")
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

    /**
     * 保存系统默认Shared Long
     */
    fun putDefaultSharedBoolean(key: String, value: Boolean = false) {
        val shared = PreferenceManager.getDefaultSharedPreferences(Config.getContext())
        shared.edit().putBoolean(key, value).apply()
    }

    /**
     * 保存系统默认Shared Int
     */
    fun putDefaultSharedInt(key: String, value: Int = 0) {
        val shared = PreferenceManager.getDefaultSharedPreferences(Config.getContext())
        shared.edit().putInt(key, value).apply()
    }

    /**
     * 保存系统默认Shared String
     */
    fun putDefaultSharedString(key: String, value: String? = "") {
        val shared = PreferenceManager.getDefaultSharedPreferences(Config.getContext())
        shared.edit().putString(key, value).apply()
    }

    /**
     * 保存系统默认Shared Long
     */
    fun putDefaultSharedLong(key: String, value: Long = 0L) {
        val shared = PreferenceManager.getDefaultSharedPreferences(Config.getContext())
        shared.edit().putLong(key, value).apply()
    }

    /**
     * 保存系统默认Shared Object
     */
    fun putDefaultSharedObject(key: String, obj: Any) {
        val gson = Gson()
        val json = gson.toJson(obj)
        putDefaultSharedString(key, json)
    }

    /**
     * 保存在线配置Shared Boolean
     */
    fun putOnlineConfigSharedBoolean(key: String, value: Boolean = false) {
        val shared = Config.getContext()!!.getSharedPreferences(SPKey.getSHAREDPREFERENCES_KEY(), 0)
        shared.edit().putBoolean(key, value).apply()
    }

    /**
     * 保存在线配置Shared Int
     */
    fun putOnlineConfigSharedInt(key: String, value: Int = 0) {
        val shared = Config.getContext()!!.getSharedPreferences(SPKey.getSHAREDPREFERENCES_KEY(), 0)
        shared.edit().putInt(key, value).apply()
    }

    /**
     * 保存在线配置Shared String
     */
    fun putOnlineConfigSharedString(key: String, value: String?) {
        val shared = Config.getContext()!!.getSharedPreferences(SPKey.getSHAREDPREFERENCES_KEY(), 0)
        shared.edit().putString(key, value).apply()
    }

    /**
     * 保存在线配置Shared Long
     */
    fun putOnlineConfigSharedLong(key: String, value: Long = 0L) {
        val shared = Config.getContext()!!.getSharedPreferences(SPKey.getSHAREDPREFERENCES_KEY(), 0)
        shared.edit().putLong(key, value).apply()
    }

    /**
     * 保存在线配置Shared Object
     */
    fun putOnlineConfigSharedObject(key: String, obj: Any) {
        val gson = Gson()
        val json = gson.toJson(obj)
        putOnlineConfigSharedString(key, json)
    }


    /**
     * Editor 保存Boolean扩展
     */
    fun SharedPreferences.Editor.putSharedBoolean(key: String, value: Boolean = false) {
        putBoolean(key, value)
    }

    /**
     * Editor 保存Int扩展
     */
    fun SharedPreferences.Editor.putSharedInt(key: String, value: Int = 0) {
        putInt(key, value)
    }

    /**
     * Editor 保存String扩展
     */
    fun SharedPreferences.Editor.putSharedString(key: String, value: String = "") {
        putString(key, value)
    }

    /**
     * Editor 保存Long扩展
     */
    fun SharedPreferences.Editor.putSharedLong(key: String, value: Long = 0L) {
        putLong(key, value)
    }

    /**
     * Editor 保存Object扩展
     */
    fun SharedPreferences.Editor.putSharedObject(key: String, obj: Any) {
        val gson = Gson()
        val json = gson.toJson(obj)
        putString(key, json)
    }


    fun loadSharedString(key: String, value: String = ""): String {
        return defaultPreferences?.getString(key, value) ?: value
    }

    fun insertSharedString(key: String, value: String) {
        defaultPreferences?.edit()?.putString(key, value)?.apply()
    }
}