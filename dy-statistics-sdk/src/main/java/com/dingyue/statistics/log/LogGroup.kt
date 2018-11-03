package com.dingyue.statistics.log

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.util.ArrayList

/**
 * Created by wangjwchn on 16/8/2.
 */

class LogGroup {
    private var mContent: MutableList<ServerLog> = ArrayList()
    private var mTopic = ""
    private var mSource = ""
    var project: String? = null
    var logstore: String? = null

    val logs: List<ServerLog>
        get() = mContent

    constructor(topic: String, source: String, project: String, logstore: String) {
        mTopic = topic
        mSource = source
        this.project = project
        this.logstore = logstore
    }

    constructor(topic: String, source: String) {
        mTopic = topic
        mSource = source
    }

    fun putTopic(topic: String) {
        mTopic = topic
    }

    fun putSource(source: String) {
        mSource = source
    }

    fun putLog(log: ServerLog) {
        mContent.add(log)
    }

    fun toJsonString(): String? {
        try {
            val json_log_group = JSONObject()
            json_log_group.put("__source__", mSource)
            json_log_group.put("__topic__", mTopic)
            val log_arrays = JSONArray()

            for (log in mContent) {
                val map = log.content
                val json_log = JSONObject(map)
                log_arrays.put(json_log)
            }
            json_log_group.put("__logs__", log_arrays)
            return json_log_group.toString()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return null
    }

}
