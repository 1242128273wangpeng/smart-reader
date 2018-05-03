package com.intelligent.reader.presenter.search

import net.lzbook.kit.data.search.SearchHotBean

/**
 * Created by yuchao on 2017/11/23 0023.
 */
interface SearchView {

    interface AvtView {
        fun onJsSearch()
        fun onStartLoad(url: String)
        fun onNoneResultSearch(searchWord: String)
    }

    interface HelpView {
        fun notifyHisData()
        fun setHistoryHeadersTitleView()
        fun onStartSearch(searchWord: String?, searchType: String?)
        fun hotItemClick(hotword: String, searchType: String)
        fun showLinearParent(show: Boolean)
        fun setHotWordAdapter(hotWords: MutableList<SearchHotBean.DataBean>?)
        fun onSuggestBack()
        fun setEditText(text: String?)
        fun onHistoryClear()
        fun showLoading()
        fun dimissLoading()
    }

    interface View {
        fun notifyHisData()
        fun onStartSearch(searchWord: String?, searchType: String?)
        fun hotItemClick(hotword: String, searchType: String)
        fun showLinearParent(show: Boolean)
        fun setHotWordAdapter(hotWords: MutableList<SearchHotBean.DataBean>?)
        fun onSuggestBack()
        fun setEditText(text: String?)

    }
}