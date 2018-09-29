package com.dingyue.searchbook.presenter

import com.ding.basic.bean.SearchRecommendBook
import com.ding.basic.bean.SearchResult
import com.dingyue.searchbook.interfaces.OnKeyWordListener
import com.dingyue.searchbook.interfaces.OnResultListener
import com.dingyue.searchbook.model.HistoryModel
import com.dingyue.searchbook.model.HotWordModel
import com.dingyue.searchbook.view.IHotWordView


/**
 * Desc 热词和推荐
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/19 0019
 */
class HotWordPresenter(var hotWordView: IHotWordView?) : BasePresenter, OnKeyWordListener {

    //从标签和作者的webView页面返回是否保留焦点
    var isFocus = true
    //运营模块返回标识
    var isBackSearch = false

    var hotWordModel: HotWordModel? = null
    var historyModel: HistoryModel? = null

    override fun onCreate() {
        hotWordModel = HotWordModel()
        historyModel = HistoryModel()
    }

    override fun onDestroy() {
        hotWordView = null
        hotWordModel = null
        historyModel = null
    }

    fun loadHotWordData() {
        hotWordModel?.loadHotWordData(object : OnResultListener<SearchResult> {
            override fun onSuccess(result: SearchResult) {
                hotWordView?.hideLoading()
                hotWordView?.showHotWordList(result.hotWords ?: arrayListOf())
            }
        })
    }

    fun loadRecommendData() {
        hotWordModel?.loadRecommendData(object : OnResultListener<ArrayList<SearchRecommendBook.DataBean>> {
            override fun onSuccess(result: ArrayList<SearchRecommendBook.DataBean>) {
                hotWordView?.hideLoading()
                hotWordView?.showRecommendList(result)
            }

        })
    }

    override fun onKeyWord(keyword: String?) {
        historyModel?.addHistoryWord(keyword)
    }


}