package com.intelligent.reader.search

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.TextUtils
import com.ding.basic.bean.SearchAutoCompleteBeanYouHua
import com.ding.basic.bean.SearchCommonBeanYouHua
import com.ding.basic.repository.RequestRepositoryFactory
import com.ding.basic.request.RequestService
import com.ding.basic.request.RequestSubscriber
import com.dingyue.contract.IPresenter
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dingyue.contract.util.SharedPreUtil
import com.intelligent.reader.activity.SearchBookActivity
import com.orhanobut.logger.Logger
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.statistic.alilog
import net.lzbook.kit.statistic.buildSearch
import net.lzbook.kit.statistic.model.Search
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.AppUtils
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.*


class SearchPresenter(private val mContext: SearchBookActivity,
                      override var view: SearchView.AvtView?) : IPresenter<SearchView.AvtView> {

    private val wordInfoMap = HashMap<String, WordInfo>()

    var word: String? = null
    var searchType: String? = "0"
    private var filterType: String? = "0"
    private var filterWord: String? = "ALL"
    private var sortType: String? = "0"
    private var mUrl: String? = null
    var fromClass: String? = null

    private var searchSuggestCallBack: SearchSuggestCallBack? = null
    private var transmitBean: SearchAutoCompleteBeanYouHua? = null
    private var shareUtil: SharedPreUtil? = null

    init {
        shareUtil = SharedPreUtil(SharedPreUtil.SHARE_DEFAULT);
    }

    private inner class WordInfo {
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

    fun startSearchSuggestData(searchWord: String?) {
        var searchWord = searchWord
        try {
            if (searchWord != null && !TextUtils.isEmpty(searchWord)) {
                searchWord = URLDecoder.decode(searchWord, "utf-8")
            }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        if (searchWord != null && !TextUtils.isEmpty(searchWord)) {
            RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                    .requestAutoCompleteV5(searchWord, object : RequestSubscriber<SearchAutoCompleteBeanYouHua>() {
                override fun requestResult(result: SearchAutoCompleteBeanYouHua?) {
                    val resultSuggest = ArrayList<SearchCommonBeanYouHua>()
                    resultSuggest.clear()
                    transmitBean = result
                    AppLog.e("bean", result.toString())
                    if (result != null && result.respCode == "20000" && result.data != null) {
                        for (i in 0 until result.data!!.authors!!.size) {
                            val searchCommonBean = SearchCommonBeanYouHua()
                            searchCommonBean.suggest = result.data!!.authors!![i].suggest
                            searchCommonBean.wordtype = result.data!!.authors!![i].wordtype
                            searchCommonBean.image_url = ""
                            searchCommonBean.isAuthor = result.data!!.authors!![i].isAuthor
                            resultSuggest.add(searchCommonBean)
                        }
                        for (i in 0 until result.data!!.label!!.size) {
                            val searchCommonBean = SearchCommonBeanYouHua()
                            searchCommonBean.suggest = result.data!!.label!![i].suggest
                            searchCommonBean.wordtype = result.data!!.label!![i].wordtype
                            searchCommonBean.image_url = ""
                            resultSuggest.add(searchCommonBean)
                        }
                        for (i in 0 until result.data!!.name!!.size) {
                            val searchCommonBean = SearchCommonBeanYouHua()
                            val nameBean = result.data!!.name!![i]

                            searchCommonBean.suggest = nameBean.suggest
                            searchCommonBean.wordtype = nameBean.wordtype
                            searchCommonBean.image_url = nameBean.imgUrl

                            searchCommonBean.host = nameBean.host
                            searchCommonBean.book_id = nameBean.bookid
                            searchCommonBean.book_source_id = nameBean.bookSourceId
                            searchCommonBean.name = nameBean.bookName
                            searchCommonBean.author = nameBean.author
                            searchCommonBean.parameter = nameBean.parameter
                            searchCommonBean.extra_parameter = nameBean.extraParameter
                            searchCommonBean.bookType = nameBean.vip.toString() + ""

                            resultSuggest.add(searchCommonBean)
                        }

                        for (bean1 in resultSuggest) {
                            AppLog.e("uuu", bean1.toString())
                        }
                        if (searchSuggestCallBack != null && transmitBean != null) {

                            searchSuggestCallBack!!.onSearchResult(resultSuggest, transmitBean!!)
                        }
                    }
                }

                override fun requestError(message: String) {
                    Logger.e("请求自动补全失败！")
                }

                override fun requestComplete() {

                }
            })
        }
    }


    private var mStartLoadCall: StartLoadCall? = null
    private var mJsCallSearchCall: JsCallSearchCall? = null
    private var jsNoneResultSearchCall: JsNoneResultSearchCall? = null

    fun setStartLoadCall(startLoadCall: StartLoadCall) {
        mStartLoadCall = startLoadCall
    }

    fun setJsCallSearchCall(jsCallSearchCall: JsCallSearchCall) {
        mJsCallSearchCall = jsCallSearchCall
    }

    fun setJsNoneResultSearchCall(jsNoneResultSearchCall: JsNoneResultSearchCall) {
        this.jsNoneResultSearchCall = jsNoneResultSearchCall
    }

    interface StartLoadCall {
        fun onStartLoad(url: String)
    }

    interface JsCallSearchCall {
        fun onJsSearch()
    }

    interface JsNoneResultSearchCall {
        fun onNoneResultSearch(searchWord: String)
    }


    fun setSearchSuggestCallBack(ssb: SearchSuggestCallBack) {
        searchSuggestCallBack = ssb
    }

    fun setStartedAction() {
        word?.let {
            wordInfoMap.put(it, WordInfo())
        }
    }

    fun setHotWordType(word: String?, type: String?) {
        this.word = word
        searchType = type
        filterType = "0"
        filterWord = "ALL"
        sortType = "0"
    }

    fun setInitType(intent: Intent) {
        word = intent.getStringExtra("word")
        fromClass = intent.getStringExtra("from_class")
        searchType = intent.getStringExtra("search_type")
        filterType = intent.getStringExtra("filter_type")
        filterWord = intent.getStringExtra("filter_word")
        sortType = intent.getStringExtra("sort_type")

    }


    fun onLoadFinished() {
        val wordInfo = wordInfoMap[word]
        wordInfo?.computeUseTime()
    }


    fun startLoadData(isAuthor: Int) {

        if (word == null) return

        var searchWord = word
        val channelID = AppUtils.getChannelId()
        if (channelID == "blp1298_10882_001" || channelID == "blp1298_10883_001" || channelID == "blp1298_10699_001") {
            if (Constants.isBaiduExamine && Constants.versionCode == AppUtils.getVersionCode()) {
                searchWord = replaceWord
            }
        }

        if (searchType == "2" && isAuthor == 1) {

            val params = HashMap<String, String>()
            params.put("author", searchWord!!)
            mUrl = RequestService.WEB_AUTHOR.replace("{packageName}", AppUtils.getPackageName()) + "?author=" + searchWord
            try {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                sharedPreferences.edit().putString(Constants.FINDBOOK_SEARCH, "author").apply()//FindBookDetail 返回键时标识
                SearchBookActivity.isStayHistory = true
                fromClass = "findBookDetail"
                val bundle = Bundle()
                bundle.putString("url", mUrl)
                bundle.putString("title", "作者主页")
                bundle.putString("from", "other")
                RouterUtil.navigation(mContext, RouterConfig.TABULATION_ACTIVITY, bundle)
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else {
            val params = HashMap<String, String>()
            params.put("keyword", searchWord ?: "")
            params.put("searchType", searchType ?: "")
            params.put("filter_type", filterType ?: "")
            params.put("filter_word", filterWord ?: "")
            params.put("sort_type", sortType ?: "")
            params.put("wordType", searchType ?: "")
            params.put("searchEmpty", "1")
            AppLog.e("kk", "$searchWord==$searchType==$filterType==$filterWord===$sortType")
            val uri = RequestService.SEARCH_VUE.replace("{packageName}", AppUtils.getPackageName())
            mUrl = UrlUtils.buildWebUrl(uri, params)
        }

        mUrl?.let {
            view?.onStartLoad(it)
        }

    }

    fun startLoadData() {

        if (word == null) return

        var searchWord = word
        val channelID = AppUtils.getChannelId()

        if (channelID == "blp1298_10882_001" || channelID == "blp1298_10883_001" || channelID == "blp1298_10699_001") {
            if (Constants.isBaiduExamine && Constants.versionCode == AppUtils.getVersionCode()) {
                searchWord = replaceWord
            }
        }

        val params = HashMap<String, String>()
        params.put("word", searchWord ?: "")
        params.put("search_type", searchType ?: "")
        params.put("filter_type", filterType ?: "")
        params.put("filter_word", filterWord ?: "")
        params.put("sort_type", sortType ?: "")
        Logger.e("$searchWord==$searchType==$filterType==$filterWord===$sortType")

        mUrl = UrlUtils.buildWebUrl(RequestService.SEARCH_V4, params)

        mUrl?.let {
            view?.onStartLoad(it)
        }

    }

    val replaceWord: String
        get() {
            val words = arrayOf("品质随时购", "春节不打烊", "轻松过大年", "便携无屏电视", "游戏笔记本电脑", "全自动洗衣机", "家团圆礼盒")
            val random = Random()
            val index = random.nextInt(7)
            return words[index]
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

    interface SearchSuggestCallBack {
        fun onSearchResult(suggestList: List<SearchCommonBeanYouHua>, transmitBean: SearchAutoCompleteBeanYouHua)
    }


}