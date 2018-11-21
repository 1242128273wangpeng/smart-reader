package com.dingyue.searchbook.model

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.ding.basic.bean.SearchAutoCompleteBeanYouHua
import com.ding.basic.net.Config
import com.ding.basic.net.api.service.RequestService
import com.ding.basic.util.sp.SPUtils
import com.dingyue.searchbook.interfaces.OnResultListener
import com.dingyue.searchbook.interfaces.OnSearchResult
import com.google.gson.Gson
import com.orhanobut.logger.Logger
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.oneclick.AntiShake
import net.lzbook.kit.utils.oneclick.OneClickUtil
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.statistic.alilog
import net.lzbook.kit.utils.statistic.buildSearch
import net.lzbook.kit.utils.statistic.model.Search
import net.lzbook.kit.utils.web.JSInterfaceObject
import net.lzbook.kit.utils.web.WebViewIndex
import java.net.URLEncoder
import java.util.*

/**
 * Desc：搜索结果集逻辑处理
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/9/25 0025 16:21
 */
class SearchResultModel {

    private val wordInfoMap = HashMap<String, WordInfo>()

    private var mUrl: String? = null

    private var word: String = ""
    private var searchType = "0"
    private var filterType = "0"
    private var filterWord = "ALL"
    private var sortType = "0"
    private lateinit var fromClass: String


    private var searchSuggestCallBack: SearchSuggestCallBack? = null
    private var jsNoneResultSearchCall: JsNoneResultSearchCall? = null


    fun loadSearchResultData(listener: OnResultListener<SearchAutoCompleteBeanYouHua>) {}

    fun setStartedAction() {
        wordInfoMap.put(word, WordInfo())
    }

    fun onLoadFinished() {
        val wordInfo = wordInfoMap[word]
        wordInfo?.computeUseTime()
    }

    fun getWord(): String {
        return word
    }

    fun setWord(word: String) {
        this.word = word
    }

    fun getFromClass(): String {
        return fromClass
    }

    fun setFromClass(fromClass: String) {
        this.fromClass = fromClass
    }

    fun getSearchType(): String {
        return searchType
    }

    fun setSearchType(searchType: String) {
        this.searchType = searchType
    }

    fun setHotWordType(word: String, type: String) {
        this.word = word
        searchType = type
        filterType = "0"
        filterWord = "ALL"
        sortType = "0"
    }


    fun initSearchType(intent: Intent) {
        word = intent.getStringExtra("word")
        fromClass = intent.getStringExtra("from_class")
        searchType = intent.getStringExtra("search_type")
        filterType = intent.getStringExtra("filter_type")
        filterWord = intent.getStringExtra("filter_word")
        sortType = intent.getStringExtra("sort_type")

    }

    private val shake = AntiShake()

    fun initJSModel(listener: OnSearchResult?, activity: Activity, webView: WebView?): JSInterfaceObject {
        return (object : JSInterfaceObject(activity) {
            @JavascriptInterface
            override fun startSearchActivity(data: String?) {
                if (data != null && data.isNotEmpty() && !activity.isFinishing) {
                    if (OneClickUtil.isDoubleClick(System.currentTimeMillis())) {
                        return
                    }

                    try {
                        val search = Gson().fromJson(data, JSSearch()::class.java)
                        listener?.onLoadKeyWord(search?.word, search?.type)

                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                }
            }

            @JavascriptInterface
            override fun startTabulationActivity(data: String?) {
                if (data != null && data.isNotEmpty() && !activity.isFinishing) {
                    if (OneClickUtil.isDoubleClick(System.currentTimeMillis())) {
                        return
                    }

                    try {
                        val redirect = Gson().fromJson(data, JSRedirect::class.java)

                        if (redirect?.url != null && redirect.title != null) {
                            val bundle = Bundle()
                            bundle.putString("url", Config.webViewBaseHost + redirect.url)
                            bundle.putString("title", redirect.title)
                            bundle.putString("from", "other")

                            RouterUtil.navigation(activity, RouterConfig.TABULATION_ACTIVITY, bundle)
                        }
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                }
            }

            @JavascriptInterface
            override fun handleBackAction() {

            }

            @JavascriptInterface
            override fun hideWebViewLoading() {
                listener?.hideWebViewLoading()
            }

            override fun handleWebRequestResult(method: String?) {
                if (null != webView) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        webView.evaluateJavascript(method) { value -> Logger.e("ReceivedValue: $value") }
                    } else {
                        webView.loadUrl(method)
                    }
                }
            }
        })
    }


    fun onDestroy() {
        val strings = wordInfoMap.keys
        for (key in strings) {
            val wordInfo = wordInfoMap[key]
            if (wordInfo != null && !wordInfo.actioned) {
                alilog(buildSearch(key, Search.OP.CANCEL, wordInfo.computeUseTime()))
            }
        }
        wordInfoMap.clear()
    }


    private var mJsCallSearchCall: JsCallSearchCall? = null

    fun setJsCallSearchCall(jsCallSearchCall: JsCallSearchCall) {
        mJsCallSearchCall = jsCallSearchCall
    }

    interface JsCallSearchCall {
        fun onJsSearch()
    }


    inner class WordInfo {
        internal var actioned = false
        private val startTime = System.currentTimeMillis()
        private var useTime: Long = 0

        fun computeUseTime(): Long {
            if (useTime == 0L) {
                useTime = System.currentTimeMillis() - startTime
            }
            return useTime
        }
    }

    interface JsNoneResultSearchCall {
        fun onNoneResultSearch(searchWord: String)
    }

    interface SearchSuggestCallBack {
        fun onSearchResult(suggestList: List<Any>, transmitBean: SearchAutoCompleteBeanYouHua)
    }


    /**
     * isAuthor：0 不显示作者  1 显示作者
     */
    fun startLoadData(listener: OnSearchResult?, isAuthor: Int = 0): String? {
        var searchWord: String
        if (word.isNotEmpty()) {
            searchWord = word

            if (searchType == "2" && isAuthor == 1) {
                val params = HashMap<String, String>()
                params.put("author", searchWord)
                mUrl = RequestService.AUTHOR_h5.replace("{packageName}", AppUtils.getPackageName()) + "?author=" + searchWord
                try {
                    //FindBookDetail 返回键时标识
                    SPUtils.editDefaultShared {
                        putString(Constants.FINDBOOK_SEARCH, "author")
                    }
                    val bundle = Bundle()
                    bundle.putString("url", mUrl)
                    bundle.putString("title", "作者主页")
                    listener?.onAnotherResult(bundle)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            } else {
                mUrl = if (mUrl.isNullOrEmpty()) {
                    try {
                        Config.webViewBaseHost + WebViewIndex.search + "?keyword=${URLEncoder.encode(searchWord, "UTF-8")}&searchType=$searchType"
                    } catch (exception: Exception) {
                        Config.webViewBaseHost + WebViewIndex.search + "?keyword=$searchWord&searchType=$searchType"
                    }
                } else {
                    String.format(Locale.getDefault(), "%s:%s", "javascript", "refreshContentView('$searchWord','$searchType')")
                }
            }
        }

        return mUrl
    }
}