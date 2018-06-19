package com.ding.basic.bean

import java.io.Serializable

class RecommendBooks : Serializable {

    var znList: List<RecommendBean>? = null
    var qgList: List<RecommendBean>? = null
    var feeList: List<RecommendBean>? = null
}