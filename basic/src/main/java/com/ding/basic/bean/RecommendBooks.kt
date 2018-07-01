package com.ding.basic.bean

import java.io.Serializable
import java.util.*

class RecommendBooks : Serializable {

    var znList: ArrayList<RecommendBean>? = null
    var qgList: ArrayList<RecommendBean>? = null
    var feeList: ArrayList<RecommendBean>? = null
}