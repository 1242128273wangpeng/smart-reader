package com.dingyue.searchbook.model

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import com.ding.basic.bean.SearchAutoCompleteBeanYouHua
import com.ding.basic.net.api.service.RequestService
import com.ding.basic.util.sp.SPUtils
import com.dingyue.searchbook.interfaces.OnResultListener
import com.dingyue.searchbook.interfaces.OnSearchResult
import com.google.gson.Gson
import net.lzbook.kit.bean.CrawlerResult
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.BDCrawler
import net.lzbook.kit.utils.loge
import net.lzbook.kit.utils.oneclick.OneClickUtil
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.statistic.alilog
import net.lzbook.kit.utils.statistic.buildSearch
import net.lzbook.kit.utils.statistic.model.Search
import net.lzbook.kit.utils.web.JSInterfaceObject
import net.lzbook.kit.utils.webview.UrlUtils
import org.json.JSONObject
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


    fun loadSearchResultData(listener: OnResultListener<SearchAutoCompleteBeanYouHua>) {}

    fun setStartedAction() {
        wordInfoMap[word] = WordInfo()
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

    fun setHotWordType(word: String?, type: String?) {
        this.word = word!!
        searchType = type!!
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


    fun initJSModel(listener: OnSearchResult?, activity: Activity): JSInterfaceObject {
        return (object : JSInterfaceObject(activity) {

            @JavascriptInterface
            override fun onSearchNoResult(keyword: String?) {
                if (!keyword.isNullOrBlank() && !activity.isFinishing) {
                    // 处理回传的json数据
                    val key = JSONObject(keyword).getString("keyword")
                    loge("====无结果=== keyword:$keyword")
                    // 无结果回调
                    listener?.onNoResult(key)
                    // 获取无结果数据
                    BDCrawler.startCrawler(key!!, object : BDCrawler.CrawlerCallback {
                        override fun onSuccess(resultList: MutableList<CrawlerResult>) {
                            listener?.onWebSearchResult(resultList)
                        }

                        override fun onFail() {
                            listener?.onWebSearchResult(null)
                        }
                    })
                }
            }

            @JavascriptInterface
            override fun startSearchActivity(data: String?) {
                if (data != null && data.isNotEmpty() && !activity.isFinishing) {
                    if (OneClickUtil.isDoubleClick(System.currentTimeMillis())) {
                        return
                    }

                    try {
                        val search = Gson().fromJson(data, JSSearch()::class.java)
                        listener?.onLoadKeyWord(search?.word, search?.type)
//                        listener?.onSearchWordResult(search?.word ?: "")

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
                            bundle.putString("url", redirect.url)
                            bundle.putString("title", redirect.title)
                            bundle.putString("from", "other")

                            RouterUtil.navigation(activity, RouterConfig.TABULATION_ACTIVITY, bundle)
                        }
                    } catch (exception: Exception) {
                        exception.printStackTrace()
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
     * isAuthor: 是否显示作者页，0为默认不显示
     * 目前新壳2显示作者页
     */
    fun startLoadData(listener: OnSearchResult?, isAuthor: Int = 0): String? {
        val searchWord: String
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
                    bundle.putString("from", "other")
                    listener?.onAnotherResult(bundle)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            } else {
                val params = HashMap<String, String>()
                params["keyword"] = searchWord
                params["searchType"] = searchType
                params["filter_type"] = filterType
                params["filter_word"] = filterWord
                params["sort_type"] = sortType
                params["wordType"] = searchType
                params["searchEmpty"] = "1"
                val uri = RequestService.SEARCH_VUE.replace("{packageName}", AppUtils.getPackageName())
                mUrl = UrlUtils.buildWebUrl(uri, params)
            }
        }

        return mUrl
    }
}