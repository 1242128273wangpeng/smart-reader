package net.lzbook.kit.utils.sp

import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.text.TextUtils
import com.google.gson.Gson
import net.lzbook.kit.base.BaseBookApplication

/**
 * Desc   SharedPreference操作工具
 * Author yangweining
 * Mail   weining_yang@dingyuegroup.cn
 * Date   2018/9/20 16:25
 */
object SPUtils {
        /**
         * 系统默认Shared 批量操作
         */
        fun editDefaultShared(job: SharedPreferences.Editor.() -> Unit) {
            val shared = PreferenceManager.getDefaultSharedPreferences(BaseBookApplication.getGlobalContext())
            val editor = shared.edit()
            editor.job()
            editor.apply()
        }

        /**
         * 在线配置Shared 批量操作
         */
        fun editOnlineConfigShared(job: SharedPreferences.Editor.() -> Unit) {
            val shared = BaseBookApplication.getGlobalContext().getSharedPreferences(SPKey.SHAREDPREFERENCES_KEY, 0)
            val editor = shared.edit()
            editor.job()
            editor.apply()
        }

        /**
         * 获取系统默认Shared Boolean
         */
        fun getDefaultSharedBoolean(key: String, defValue: Boolean = false): Boolean {
            val shared = PreferenceManager.getDefaultSharedPreferences(BaseBookApplication.getGlobalContext())
            return shared.getBoolean(key, defValue)
        }

        /**
         * 获取系统默认Shared Int
         */
        fun getDefaultSharedInt(key: String, defValue: Int = 0): Int {
            val shared = PreferenceManager.getDefaultSharedPreferences(BaseBookApplication.getGlobalContext())
            return shared.getInt(key, defValue)
        }

        /**
         * 获取系统默认Shared String
         */
        fun getDefaultSharedString(key: String, defValue: String = ""): String {
            val shared = PreferenceManager.getDefaultSharedPreferences(BaseBookApplication.getGlobalContext())
            return shared.getString(key, defValue)
        }

        /**
         * 获取系统默认Shared Long
         */
        fun getDefaultSharedLong(key: String, defValue: Long = 0L): Long {
            val shared = PreferenceManager.getDefaultSharedPreferences(BaseBookApplication.getGlobalContext())
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
            val shared = BaseBookApplication.getGlobalContext().getSharedPreferences(SPKey.SHAREDPREFERENCES_KEY, 0)
            return shared.getBoolean(key, defValue)
        }

        /**
         * 获取在线配置Shared Int
         */
        fun getOnlineConfigSharedInt(key: String, defValue: Int = 0): Int {
            val shared = BaseBookApplication.getGlobalContext().getSharedPreferences(SPKey.SHAREDPREFERENCES_KEY, 0)
            return shared.getInt(key, defValue)
        }

        /**
         * 获取在线配置Shared String
         */
        fun getOnlineConfigSharedString(key: String, defValue: String = ""): String {
            val shared = BaseBookApplication.getGlobalContext().getSharedPreferences(SPKey.SHAREDPREFERENCES_KEY, 0)
            return shared.getString(key, defValue)
        }

        /**
         * 获取在线配置Shared Long
         */
        fun getOnlineConfigSharedLong(key: String, defValue: Long = 0L): Long {
            val shared = BaseBookApplication.getGlobalContext().getSharedPreferences(SPKey.SHAREDPREFERENCES_KEY, 0)
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
        fun putDefaultSharedBoolean(key: String, value: Boolean = false){
            val shared = PreferenceManager.getDefaultSharedPreferences(BaseBookApplication.getGlobalContext())
            shared.edit().putBoolean(key, value).apply()
        }

        /**
         * 保存系统默认Shared Int
         */
        fun putDefaultSharedInt(key: String, value: Int = 0){
            val shared = PreferenceManager.getDefaultSharedPreferences(BaseBookApplication.getGlobalContext())
            shared.edit().putInt(key, value).apply()
        }

        /**
         * 保存系统默认Shared String
         */
        fun putDefaultSharedString(key: String, value: String = ""){
            val shared = PreferenceManager.getDefaultSharedPreferences(BaseBookApplication.getGlobalContext())
            shared.edit().putString(key, value).apply()
        }

        /**
         * 保存系统默认Shared Long
         */
        fun putDefaultSharedLong(key: String, value: Long = 0L) {
            val shared = PreferenceManager.getDefaultSharedPreferences(BaseBookApplication.getGlobalContext())
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
        fun putOnlineConfigSharedBoolean(key: String, value: Boolean = false){
            val shared = BaseBookApplication.getGlobalContext().getSharedPreferences(SPKey.SHAREDPREFERENCES_KEY, 0)
            shared.edit().putBoolean(key, value).apply()
        }

        /**
         * 保存在线配置Shared Int
         */
        fun putOnlineConfigSharedInt(key: String, value: Int = 0){
            val shared = BaseBookApplication.getGlobalContext().getSharedPreferences(SPKey.SHAREDPREFERENCES_KEY, 0)
            shared.edit().putInt(key, value).apply()
        }

        /**
         * 保存在线配置Shared String
         */
        fun putOnlineConfigSharedString(key: String, value: String?){
            val shared = BaseBookApplication.getGlobalContext().getSharedPreferences(SPKey.SHAREDPREFERENCES_KEY, 0)
            shared.edit().putString(key, value).apply()
        }

        /**
         * 保存在线配置Shared Long
         */
        fun putOnlineConfigSharedLong(key: String, value: Long = 0L) {
            val shared = BaseBookApplication.getGlobalContext().getSharedPreferences(SPKey.SHAREDPREFERENCES_KEY, 0)
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
        fun SharedPreferences.Editor.putSharedBoolean(key: String, value: Boolean = false){
            putBoolean(key, value)
        }

        /**
         * Editor 保存Int扩展
         */
        fun SharedPreferences.Editor.putSharedInt(key: String, value: Int = 0){
            putInt(key, value)
        }

        /**
         * Editor 保存String扩展
         */
        fun SharedPreferences.Editor.putSharedString(key: String, value: String = ""){
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
}