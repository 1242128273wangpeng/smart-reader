package com.ding.basic.bean

import java.io.Serializable

/**
 * Desc 热词和推荐实体类
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/19 0019
 */
data class SearchResult(
        var hotWords: ArrayList<HotWordBean>?,
        var operations: ArrayList<SearchOperations>?) : Serializable