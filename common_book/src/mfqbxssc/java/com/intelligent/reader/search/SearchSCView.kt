package com.intelligent.reader.presenter.search

import com.ding.basic.bean.HotWordBean
import net.lzbook.kit.data.search.SearchHotBean

/**
 * Created by yuchao on 2017/11/23 0023.
 */
interface SearchSCView {

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
    }

    interface View {
        fun notifyHisData()
        fun onStartSearch(searchWord: String?, searchType: String?,isAuthor: Int)
        fun hotItemClick(hotword: String, searchType: String)
        fun showLinearParent(show: Boolean)
        fun setHotWordAdapter(hotWords: MutableList<HotWordBean>?)
        fun onSuggestBack()
        fun setEditText(text: String?)
        fun showLoading()
        fun dimissLoading()
    }
}