package com.dingyue.searchbook.view

import com.dingyue.searchbook.interfaces.OnSearchResult
import net.lzbook.kit.utils.webview.WebViewJsInterface


/**
 * Desc：
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/9/25 0025 16:00
 */
interface ISearchResultView : IBaseView, OnSearchResult {

    fun obtainJSInterface(jsInterface: WebViewJsInterface)
}