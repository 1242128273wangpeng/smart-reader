package com.intelligent.reader.search

import com.ding.basic.bean.HotWordBean
import com.ding.basic.bean.SearchHotBean

/**
 * Function：
 *
 * Created by JoannChen on 2018/6/16 0016 10:11
 * E-mail:yongzuo_chen@dingyuegroup.cn
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
        fun hotItemClick(hotWord: String, searchType: String)
        fun showLinearParent(show: Boolean)
        fun setHotWordAdapter(hotWords: MutableList<HotWordBean>?)
        fun onSuggestBack()
        fun setEditText(text: String?)
        fun showDialogState(isShouldShow: Boolean)
    }

    interface View {
        fun notifyHisData()
        fun onStartSearch(searchWord: String?, searchType: String?,isAuthor: Int)
        fun hotItemClick(hotWord: String, searchType: String)
        fun showLinearParent(show: Boolean)
        fun setHotWordAdapter(hotWords: MutableList<HotWordBean>?)
        fun onSuggestBack()
        fun setEditText(text: String?)
    }
}