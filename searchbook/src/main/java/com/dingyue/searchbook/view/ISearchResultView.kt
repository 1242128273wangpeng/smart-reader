package com.dingyue.searchbook.view

import android.app.Activity
import com.dingyue.searchbook.interfaces.OnSearchResult


/**
 * Desc：
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/9/25 0025 16:00
 */
interface ISearchResultView : IBaseView, OnSearchResult {

    fun obtainJSInterface(jsInterface: Any)

    fun getCurrentActivity(): Activity

}