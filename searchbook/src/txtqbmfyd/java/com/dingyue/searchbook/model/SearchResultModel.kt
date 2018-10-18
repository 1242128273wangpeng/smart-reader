package com.dingyue.searchbook.model

import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import com.ding.basic.bean.SearchAutoCompleteBeanYouHua
import com.ding.basic.net.api.service.RequestService
import com.ding.basic.util.sp.SPUtils
import com.dingyue.contract.web.JSInterfaceObject
import com.dingyue.searchbook.interfaces.OnResultListener
import com.dingyue.searchbook.interfaces.OnSearchResult
import com.google.gson.Gson
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.oneclick.OneClickUtil
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.statistic.alilog
import net.lzbook.kit.utils.statistic.buildSearch
import net.lzbook.kit.utils.statistic.model.Search
import net.lzbook.kit.utils.webview.UrlUtils
import java.util.*


/**
 * Desc：搜索结果集逻辑处理
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/9/25 0025 16:21
 */
class SearchResultModel(var listener: OnSearchResult?) {

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


    fun initJSModel(): JSInterfaceObject {
        val jsInterfaceModel = (object : JSInterfaceObject(listener?.getCurrentActivity()) {
            @JavascriptInterface
            override fun startSearchActivity(data: String?) {
                if (data != null && data.isNotEmpty() && !activity!!.isFinishing) {
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
                if (data != null && data.isNotEmpty() && !activity!!.isFinishing) {
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

                            RouterUtil.navigation(activity!!, RouterConfig.TABULATION_ACTIVITY, bundle)
                        }
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                }
            }
        })

        return jsInterfaceModel
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
    fun startLoadData(isAuthor: Int = 0): String? {

        var searchWord: String
        if (word.isNotEmpty()) {
            searchWord = word
            val channelID = AppUtils.getChannelId()
            if (channelID == "blp1298_10882_001"
                    || channelID == "blp1298_10883_001"
                    || channelID == "blp1298_10699_001") {
                if (Constants.isBaiduExamine && Constants.versionCode == AppUtils.getVersionCode()) {
                    searchWord = getReplaceWord()
                }
            }

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
                params.put("keyword", searchWord)
                params.put("searchType", searchType)
                params.put("filter_type", filterType)
                params.put("filter_word", filterWord)
                params.put("sort_type", sortType)
                params.put("wordType", searchType)
                params.put("searchEmpty", "1")
                val uri = RequestService.SEARCH_VUE.replace("{packageName}", AppUtils.getPackageName())
                mUrl = UrlUtils.buildWebUrl(uri, params)
            }
        }

        return mUrl
    }

    private fun getReplaceWord(): String {
        val words = arrayOf("品质随时购", "春节不打烊", "轻松过大年", "便携无屏电视", "游戏笔记本电脑", "全自动洗衣机", "家团圆礼盒")
        val random = Random()
        val index = random.nextInt(7)
        return words[index]
    }

}