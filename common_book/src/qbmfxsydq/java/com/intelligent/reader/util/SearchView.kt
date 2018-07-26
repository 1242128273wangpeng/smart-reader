package com.intelligent.reader.util

import com.ding.basic.bean.HotWordBean
import com.ding.basic.bean.SearchHotBean

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
        fun showDialogState(isShouldShow: Boolean)
    }

    interface View {
        fun notifyHisData()
        fun onStartSearch(searchWord: String?, searchType: String?,isAuthor: Int)
        fun hotItemClick(hotword: String, searchType: String)
        fun showLinearParent(show: Boolean)
        fun setHotWordAdapter(hotWords: MutableList<HotWordBean>?)
        fun onSuggestBack()
        fun setEditText(text: String?)
    }
}