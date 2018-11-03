package com.dingyue.statistics.log

import com.dingyue.statistics.DyStatService
import com.dingyue.statistics.common.PLItemKey
import com.dingyue.statistics.dao.bean.LocalLog
import com.dingyue.statistics.utils.JsonUtil

import java.util.HashMap
import java.util.LinkedHashMap

/**
 * Created by wangjwchn on 16/8/2
 */
class ServerLog {

    var content: HashMap<String, Any?>

    var eventType = LocalLog.MAJORITY

    var id = 0

    /**
     * 构建ServerLog
     *
     * @param type
     */
    constructor(type: PLItemKey) {
        content = if (DyStatService.needSavePointLog) {
            // 调试时，采用有序map，顺序输出便于人工查看
            LinkedHashMap()
        } else {
            // 实际使用时，采用无序map，节省资源
            HashMap()
        }
        if (!content.containsKey("project")) {
            content["project"] = type.project
        }
        if (!content.containsKey("logstore")) {
            content["logstore"] = type.logstore
        }
        if (type == PLItemKey.ZN_APP_APPSTORE || type == PLItemKey.ZN_USER || type == PLItemKey.ZN_PV) {
            eventType = LocalLog.MINORITY // minority直接上传，majority根据时间和数量，按条件上传
        }
        content["__time__"] = System.currentTimeMillis() / 1000
    }

    constructor(_id: Int, type: String, contentJson: String) {
        this.id = _id
        this.eventType = type
        this.content = JsonUtil.fromJson(contentJson)
    }

    fun putContent(key: String?, value: String?) {
        if (key == null || key.isEmpty()) {
            return
        }
        // 处理空参数 （将null或"" 均改为 大写NULL字符串  "NULL"）
        if (value.isNullOrBlank() || value == "null") {
            content[key] = "NULL"
        } else {
            content[key] = value.orEmpty()
        }
    }
}
