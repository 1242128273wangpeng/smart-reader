package com.ding.basic.bean

import com.google.gson.annotations.Expose


/**
 * Function：搜索热词(二期)
 *
 * Created by JoannChen on 2018/7/19 0019 16:20
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
class HotWordBean {

    var keywordType: Int = 0
    var keyword: String? = null
    var superscript: String? = null
    var color: String? = null
    var sort: Int = 0

    override fun toString(): String {
        return "HotWordBean{" +
                "keywordType=" + keywordType +
                ", keyword='" + keyword + '\'' +
                ", superscript='" + superscript + '\'' +
                ", color='" + color + '\'' +
                ", sort=" + sort +
                '}'
    }

    companion object {

        @Expose
        val NEW_TAG = "新"
        @Expose
        val HOT_TAG = "热"
        @Expose
        val RECOMMEND_TAG = "荐"
    }
}
