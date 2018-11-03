package com.dingyue.statistics.utils

object FormatUtil {

    fun forMatMap(map: Map<String, String>): String {
        if (map.isEmpty()) {
            return ""
        }
        val data = StringBuilder()
        for ((key, value) in map) {
            data.append(key).append(":").append(value).append("`")
        }
        val index = data.lastIndexOf("`")
        if (index == data.length - 1)
            data.replace(data.length - 1, data.length, "")

        return data.toString()
    }
}

