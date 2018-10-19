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
class SearchResultPresenter(private var searchResultView: ISearchResultView) : BasePresenter, OnSearchResult {

    private var searchResultModel: SearchResultModel? = null
    private var historyModel: HistoryModel? = null

    override fun onCreate() {
        searchResultModel = SearchResultModel()
        historyModel = HistoryModel()

        searchResultModel?.initJSModel(this, searchResultView.getCurrentActivity())?.let {
            searchResultView.obtainJSInterface(it)
        }
    }


    /**
     * searchType：0 全部 1 标签 2 作者 3 书名
     * isAuthor: 是否显示作者页，0为默认不显示 (目前新壳2显示作者页)
     */
    fun loadKeyWord(keyWord: String, searchType: String = "0", isAuthor: Int = 0) {
        runOnMain {
            searchResultView.showLoading()
            searchResultView.onSearchWordResult(keyWord)
        }
        historyModel?.addHistoryWord(keyWord)

        searchResultModel?.setWord(keyWord)
        searchResultModel?.setSearchType(searchType)
        searchResultModel?.startLoadData(this, isAuthor)?.let {
            onSearchResult(it)
        }
    }


    override fun onSearchResult(url: String) {
        runOnMain {
            searchResultView.hideLoading()
            searchResultView.onSearchResult(url)
        }
    }


    override fun onCoverResult(bundle: Bundle) {
        searchResultView.onCoverResult(bundle)
    }


    override fun onAnotherResult(bundle: Bundle) {
        searchResultView.onAnotherResult(bundle)
    }


    override fun onAnotherResultNew(bundle: Bundle) {
        searchResultView.onAnotherResultNew(bundle)
    }


    override fun onSearchWordResult(searchWord: String) {
        historyModel?.addHistoryWord(searchWord)
        searchResultView.onSearchWordResult(searchWord)
    }


    override fun onTurnReadResult(bundle: Bundle) {
        searchResultView.onTurnReadResult(bundle)
    }


    override fun onEnterReadResult(bundle: Bundle) {
        searchResultView.onTurnReadResult(bundle)
    }


    override fun onLoadKeyWord(keyWord: String?, searchType: String?) {
        loadKeyWord(keyWord!!, searchType!!)
    }

    override fun onDestroy() {
        searchResultModel = null
        historyModel = null
    }

    fun setStartedAction() {
        searchResultModel?.setStartedAction()
    }

    fun onLoadFinished() {
        searchResultModel?.onLoadFinished()
    }

}