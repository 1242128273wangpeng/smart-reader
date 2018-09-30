package com.dingyue.searchbook.view


/**
 * Desc：自动补全结果集
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/9/25 0025 16:40
 */
interface ISuggestView : IBaseView {

    fun showSuggestList(suggestList: MutableList<Any>)

}