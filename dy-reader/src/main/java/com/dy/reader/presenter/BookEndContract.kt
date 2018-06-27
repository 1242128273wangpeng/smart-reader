package com.dy.reader.presenter

import com.ding.basic.bean.RecommendBean
import com.ding.basic.bean.RecommendBooksEndResp
import com.ding.basic.bean.Source
import java.util.*

interface BookEndContract {

    fun showSourceList(sourceList: ArrayList<Source>)

    fun showRecommend(recommends: ArrayList<RecommendBean>?)
}
