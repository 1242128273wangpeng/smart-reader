package com.dingyue.searchbook.view


/**
 * Desc：
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/9/25 0025 16:40
 */
interface ISuggestView : IBaseView {

    fun showSuggestList(suggestList: MutableList<Any>)//展示自动补全列表

}