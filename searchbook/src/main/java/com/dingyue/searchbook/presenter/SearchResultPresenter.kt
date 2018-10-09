package com.dingyue.searchbook.presenter

import android.os.Bundle
import com.dingyue.searchbook.interfaces.OnSearchResult
import com.dingyue.searchbook.model.HistoryModel
import com.dingyue.searchbook.model.SearchResultModel
import com.dingyue.searchbook.view.ISearchResultView
import net.lzbook.kit.utils.runOnMain


/**
 * Desc：
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/9/25 0025 16:11
 */
class SearchResultPresenter(private var searchResultView: ISearchResultView?) : BasePresenter, OnSearchResult {

    private var searchResultModel: SearchResultModel? = null
    private var historyModel: HistoryModel? = null

    override fun onCreate() {
        searchResultModel = SearchResultModel(this)
        historyModel = HistoryModel()

        searchResultModel?.initJSModel()?.let {
            searchResultView?.obtainJSInterface(it)
        }
    }


    fun loadKeyWord(keyWord: String) {
        searchResultView?.showLoading()
        historyModel?.addHistoryWord(keyWord)
        searchResultModel?.setWord(keyWord)
        searchResultModel?.startLoadData(0)?.let {
            onSearchResult(it)
        }
    }


    override fun onSearchResult(url: String) {
        runOnMain {
            searchResultView?.hideLoading()
        }
        searchResultView?.onSearchResult(url)
    }


    override fun onCoverResult(bundle: Bundle) {
        searchResultView?.onCoverResult(bundle)
    }


    override fun onAnotherResult(bundle: Bundle) {
        searchResultView?.onAnotherResult(bundle)
    }


    override fun onSearchWordResult(searchWord: String) {
        historyModel?.addHistoryWord(searchWord)
        searchResultView?.onSearchWordResult(searchWord)
    }


    override fun onTurnReadResult(bundle: Bundle) {
        searchResultView?.onTurnReadResult(bundle)
    }


    override fun onEnterReadResult(bundle: Bundle) {
        searchResultView?.onTurnReadResult(bundle)
    }


    override fun onDestroy() {
        searchResultModel = null
        searchResultView = null
        historyModel = null
    }

}