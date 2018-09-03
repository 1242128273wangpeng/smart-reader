package com.dy.reader.presenter

import com.ding.basic.bean.RecommendBean
import com.ding.basic.bean.RecommendBooksEndResp
import com.ding.basic.bean.Source
import java.util.*

interface BookEndContract {

    fun showSourceList(sourceList: ArrayList<Source>)

    fun showRecommend(recommends: ArrayList<RecommendBean>?)
    /**
     * 结页推荐（后期数据融合铺开后，可以删掉）
     */
    fun showRecommendV4(one: Boolean, two: Boolean, recommendRes: RecommendBooksEndResp){}
}
