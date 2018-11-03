package com.dingyue.statistics.common

/**
 * 针对project和logstore 指定一个对应的编码，用来存储使用
 */
enum class PLItemKey private constructor(val key: String, val project: String, val logstore: String, var desc: String?) {
    ZN_APP_EVENT("ZN_DF_APP_EVENT", "datastatistics-zn", "event", "智能数据流APP点击事件"),
    ZN_APP_APPSTORE("ZN_APP_APPSTORE", "datastatistics-zn", "appstore", "app列表"),
    ZN_APP_FEEDBACK("ZN_APP_FEEDBACK", "datastatistics-zn", "feedback", "客户端问题章节反馈"),
    ZN_USER("ZN_USER", "basestatistics", "zn_user", "用户信息"),
    ZN_PV("ZN_PV", "basestatistics", "zn_pv", "阅读pv");


    companion object {

        fun getKey(key: String): PLItemKey? {
            var itemKey: PLItemKey? = null
            for (item in PLItemKey.values()) {
                if (key.equals(item.key, ignoreCase = true)) {
                    itemKey = item
                    break
                }
            }
            return itemKey
        }
    }

}
