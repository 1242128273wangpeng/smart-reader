package com.dingyue.searchbook.presenter

import com.dingyue.searchbook.model.HistoryModel
import com.dingyue.searchbook.view.IHistoryView


/**
 * Desc：
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/9/20 0020 23:06
 */
class HistoryPresenter(private var iHistoryView: IHistoryView?) : BasePresenter {

    private var historyModel: HistoryModel? = null


    override fun onCreate() {
        historyModel = HistoryModel()
    }

    override fun onDestroy() {
        historyModel = null
        iHistoryView = null
    }

    fun loadHistoryRecord() {
        iHistoryView?.showLoading()
        iHistoryView?.showHistoryRecord(historyModel?.loadHistoryRecord() ?: arrayListOf())
        iHistoryView?.hideLoading()

    }

    /**
     * 清空所有历史记录
     */
    fun removeHistoryRecord() {
        historyModel?.clearHistory()
    }

    /**
     * 删除某条历史记录
     */
    fun removeHistoryRecord(index: Int) {
        historyModel?.clearHistory(index)
    }


}