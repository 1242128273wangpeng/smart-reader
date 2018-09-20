package com.dingyue.searchbook.view

import com.ding.basic.bean.HotWordBean
import com.ding.basic.bean.SearchRecommendBook


/**
 * Desc 展示热词和推荐列表
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/19 0019 22:28
 */
interface IHotWordView : IBaseView {

    fun showHotWordList(hotWordList: ArrayList<HotWordBean>)//展示热词列表
    fun showRecommendFreeList(recommendList: ArrayList<SearchRecommendBook.DataBean>)//全站免费列表
    fun showRecommendWantList(recommendList: ArrayList<SearchRecommendBook.DataBean>)//想读就读列表

}