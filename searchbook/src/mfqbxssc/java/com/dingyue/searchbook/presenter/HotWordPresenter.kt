package com.dingyue.searchbook.presenter

import com.ding.basic.bean.SearchRecommendBook
import com.ding.basic.bean.SearchResult
import com.dingyue.searchbook.IResultListener
import com.dingyue.searchbook.model.HotWordModel
import com.dingyue.searchbook.view.IHotWordView


/**
 * Desc 热词和推荐
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/19 0019
 */
class HotWordPresenter(var hotView: IHotWordView?) : BasePresenter {

    //从标签和作者的webView页面返回是否保留焦点
    var isFocus = true
    //运营模块返回标识
    var isBackSearch = false

    var hotWordModel: HotWordModel? = null

    override fun onCreate() {
        hotWordModel = HotWordModel()
    }

    override fun onDestroy() {
        hotView = null
        hotWordModel = null
    }

    fun loadHotWordData() {
        hotWordModel?.loadHotWordData(object : IResultListener<SearchResult> {
            override fun onSuccess(result: SearchResult) {
                hotView?.hideLoading()
                hotView?.showHotWordList(result.hotWords ?: arrayListOf())
            }
        })
    }

    fun loadRecommendData(){
        hotWordModel?.loadRecommendData(object :IResultListener<ArrayList<SearchRecommendBook.DataBean>>{
            override fun onSuccess(result: ArrayList<SearchRecommendBook.DataBean>) {
                hotView?.hideLoading()
                hotView?.showRecommendFreeList(result)
            }

        })
    }


}