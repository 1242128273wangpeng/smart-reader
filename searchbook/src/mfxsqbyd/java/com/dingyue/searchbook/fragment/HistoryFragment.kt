package com.dingyue.searchbook.fragment

import android.view.View
import com.dingyue.searchbook.R
import kotlinx.android.synthetic.main.fragment_listview.*

/**
 * Desc 历史记录
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/19 0019 22:05
 */
class HistoryFragment : BaseHistoryFragment() {

    override fun addFooterView() {
        val historyTitleView = View.inflate(context, R.layout.item_history_hearer_layout, null)
        listView.addFooterView(historyTitleView)
    }

    override fun onHistoryIndex(index: Int) {
        historyPresenter.removeHistoryRecord(index)
        historyAdapter?.notifyDataSetChanged()
    }


}