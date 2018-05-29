package com.dy.reader.presenter

import com.ding.basic.bean.RecommendBooksEndResp
import com.ding.basic.bean.Source
import java.util.*

/**
 * Created by zhenXiang on 2017\11\21 0021.
 */

interface BookEndContract {

    fun showSource(hasSource: Boolean, sourceList: ArrayList<Source>) //展示多个来源
    fun showRecommend(one:Boolean,two:Boolean,recommendRes: RecommendBooksEndResp)
}
