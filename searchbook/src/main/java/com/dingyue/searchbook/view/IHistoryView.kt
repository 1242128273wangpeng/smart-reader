package com.dingyue.searchbook.view


/**
 * Desc：搜索历史记录
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/9/20 0020 16:28
 */
interface IHistoryView : IBaseView {

    fun showHistoryRecord(historyList:ArrayList<String>)//展示搜索历史记录
    fun clearHistoryResult()
}