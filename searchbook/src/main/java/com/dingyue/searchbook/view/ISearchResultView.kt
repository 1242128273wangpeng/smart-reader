package com.dingyue.searchbook.view

import android.app.Activity
import android.webkit.WebView
import com.dingyue.searchbook.interfaces.OnSearchResult
import net.lzbook.kit.utils.web.JSInterfaceObject


/**
 * Desc：
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/9/25 0025 16:00
 */
interface ISearchResultView : IBaseView, OnSearchResult {

    fun obtainJSInterface(jsInterface: JSInterfaceObject)

    fun getCurrentActivity(): Activity

    fun loadContentWebView(): WebView
}