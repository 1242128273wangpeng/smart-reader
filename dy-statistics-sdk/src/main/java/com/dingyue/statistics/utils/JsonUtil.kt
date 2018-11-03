package com.dingyue.statistics.utils

import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap


object JsonUtil {

    /**
     * json 字符串 转 HashMap
     */
    fun fromJson(jsonStr: String): HashMap<String, Any?> {
        val json =
                if (jsonStr.startsWith("[") && jsonStr.endsWith("]")) JSONObject("{\"fakelist\":$jsonStr}")
                else JSONObject(jsonStr)
        return fromJson(json)
    }


    /**
     * JSONObject 转 HashMap
     */
    fun fromJson(json: JSONObject): HashMap<String, Any?> {
        val map = HashMap<String, Any?>()
        // 循环key
        json.keys().forEach {
            // 获取value
            val value = json[it]
            if (value != null && value is JSONObject) {
                map[it] = fromJson(value)
            } else if (value != null && value is JSONArray) {
                map[it] = fromJson(value)
            } else {
                map[it] = value
            }
        }
        return map
    }


    /**
     * JSONArray 转 ArrayList
     */
    fun fromJson(array: JSONArray): ArrayList<Any> {
        val list = ArrayList<Any>()
        for (i in 0 until array.length()) {
            val value = array.opt(i)
            if (value != null && value is JSONObject) {
                list.add(fromJson(value))
            } else if (value != null && value is JSONArray) {
                list.add(fromJson(value))
            } else {
                list.add(value)
            }
        }
        return list
    }

    /**
     * 将指定的 [Map]<String></String>, Object>对象转成json数据
     */
    fun fromMap(map: Map<String, Any?>): String {
        try {
            return getJSONObject(map).toString()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        return ""
    }

    private fun getJSONObject(map: Map<*, *>): JSONObject {
        val json = JSONObject()
        for (entry in map.entries) {
            var value = entry.value
            if (value is Map<*, *>) {
                value = getJSONObject(value)
            } else if (value is ArrayList<*>) {
                value = getJSONArray(value)
            }
            json.put(entry.key.toString(), value)
        }
        return json
    }

    private fun getJSONArray(list: ArrayList<*>): JSONArray {
        val array = JSONArray()
        for (value in list) {
            if (value is Map<*, *>) {
                array.put(getJSONObject(value))
            } else if (value is ArrayList<*>) {
                array.put(getJSONArray(value))
            }
        }
        return array
    }
}
