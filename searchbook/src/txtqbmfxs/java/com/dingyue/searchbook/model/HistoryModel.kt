package com.dingyue.searchbook.model

import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.utils.Tools


/**
 * Desc：搜索历史业务逻辑处理
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/9/20 0020 16:30
 */
class HistoryModel {

    private var historyList: ArrayList<String>? = null
    /**
     * 加载搜索历史记录
     */
    fun loadHistoryRecord(): ArrayList<String> {
        historyList = Tools.getHistoryWord(BaseBookApplication.getGlobalContext()) ?: arrayListOf()
        return historyList ?: arrayListOf()
    }


    /**
     * 添加关键词到历史记录
     */
    fun addHistoryWord(keyword: String?) {


        if (keyword.isNullOrEmpty()) return

        loadHistoryRecord()

        historyList?.let {
            if (it.contains(keyword)) {
                it.remove(keyword)
            }

            if (!it.contains(keyword)) {
                val size = it.size
                if (size >= 30) {
                    it.removeAt(size - 1)
                }
                it.add(0, keyword ?: "")
                Tools.saveHistoryWord(BaseBookApplication.getGlobalContext(), it)
            }

        }

    }


    fun clearHistory(index: Int) {
        historyList?.let {
            if (index < it.size) {
                it.removeAt(index)
            }
            Tools.saveHistoryWord(BaseBookApplication.getGlobalContext(), historyList)
        }
    }

}