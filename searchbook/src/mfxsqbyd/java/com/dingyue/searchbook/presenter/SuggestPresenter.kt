package com.dingyue.searchbook.presenter

import com.dingyue.searchbook.interfaces.OnResultListener
import com.dingyue.searchbook.model.SuggestModel
import com.dingyue.searchbook.view.ISuggestView


/**
 * Desc：
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/9/25 0025 16:42
 */
class SuggestPresenter(private var suggestView: ISuggestView?) : BasePresenter {

    private var suggestModel: SuggestModel? = null

    override fun onCreate() {
        suggestModel = SuggestModel()
    }

    override fun onDestroy() {
        suggestModel = null
        suggestView = null
    }

    fun loadSuggestData(finalQuery: String) {

        suggestModel?.loadSuggestData(finalQuery, object : OnResultListener<ArrayList<Any>> {
            override fun onSuccess(result: ArrayList<Any>) {
                suggestView?.showSuggestList(result)
            }

        })
    }
}