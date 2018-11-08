package com.intelligent.reader.presenter.search

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import com.ding.basic.bean.Book
import com.ding.basic.bean.Chapter
import com.ding.basic.bean.SearchAutoCompleteBeanYouHua
import com.ding.basic.bean.SearchCommonBeanYouHua
import com.ding.basic.repository.RequestRepositoryFactory
import com.ding.basic.request.RequestService
import com.ding.basic.request.RequestSubscriber
import com.dingyue.contract.IPresenter
import com.orhanobut.logger.Logger
import io.reactivex.disposables.Disposable
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.statistic.alilog
import net.lzbook.kit.statistic.buildSearch
import net.lzbook.kit.statistic.model.Search
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.AppUtils
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.*

/**
 * SearchCommonBean和SearchCommonBeanYouHua合并
 * Created by yuchao on 2017/8/2 0002.
 */
class SearchPresenter(private val mContext: Activity, override var view: SearchView.AvtView?) : IPresenter<SearchView.AvtView> {
    private val wordInfoMap = HashMap<String, WordInfo>()

    var word: String? = null
    var searchType: String? = "0"
    private var filterType: String? = "0"
    private var filterWord: String? = "ALL"
    private var sortType: String? = "0"
    private var mUrl: String? = null
    var fromClass: String? = null
    private val url_tag: String? = null
    private var searchSuggestCallBack: SearchSuggestCallBack? = null
    private var transmitBean: SearchAutoCompleteBeanYouHua? = null
    private var disposable: Disposable? = null

    fun startSearchSuggestData(searchWord: String?) {
        var searchWord = searchWord
        AppLog.e("word11", searchWord)
        try {
            if (searchWord != null && !TextUtils.isEmpty(searchWord)) {
                searchWord = URLDecoder.decode(searchWord, "utf-8")
                AppLog.e("word22", searchWord)
            }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }


        if (searchWord != null && !TextUtils.isEmpty(searchWord)) {
            RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestAutoCompleteV4(searchWord, object : RequestSubscriber<SearchAutoCompleteBeanYouHua>() {
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
                            resultSuggest.add(searchCommonBean)
                        }
                        for (i in 0 until result.data!!.label!!.size) {
                            val searchCommonBean = SearchCommonBeanYouHua()
                            searchCommonBean.suggest = result.data!!.label!![i].suggest
                            searchCommonBean.wordtype = result.data!!.label!![i].wordtype
                            resultSuggest.add(searchCommonBean)
                        }
                        for (i in 0 until result.data!!.name!!.size) {
                            val searchCommonBean = SearchCommonBeanYouHua()
                            searchCommonBean.suggest = result.data!!.name!![i].suggest
                            searchCommonBean.wordtype = result.data!!.name!![i].wordtype
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

    fun setSearchSuggestCallBack(ssb: SearchSuggestCallBack) {
        searchSuggestCallBack = ssb
    }

    fun setStartedAction() {
        word?.let {
            wordInfoMap.put(it, WordInfo())
        }
    }

    fun onLoadFinished() {
        val wordInfo = wordInfoMap[word]
        wordInfo?.computeUseTime()
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

    protected fun genCoverBook(host: String, book_id: String, book_source_id: String, name: String, author: String, status: String, category: String,
                               imgUrl: String, last_chapter: String, chapter_count: String, update_time: Long, parameter: String, extra_parameter: String, dex: Int): Book {
        val book = Book()
        book.status = status
        book.book_id = book_id
        book.book_source_id = book_source_id
        book.name = name
        book.label = category
        book.author = author
        book.img_url = imgUrl
        book.host = host
        book.chapter_count = Integer.valueOf(chapter_count)!!
        val lastChapter = Chapter()
        lastChapter.update_time = update_time
        lastChapter.name = last_chapter
        book.last_chapter = lastChapter
        book.last_update_success_time = System.currentTimeMillis()
        book.update_date_fusion = 0
        return book
    }

    fun startLoadData() {
        word?.let {
            val params = HashMap<String, String>()
            params.put("keyword", it)
            params.put("search_type", searchType ?: "")
            params.put("filter_type", filterType ?: "")
            params.put("filter_word", filterWord ?: "")
            params.put("sort_type", sortType ?: "")
            params.put("searchEmpty", "1")
            AppLog.e("kk", "$it==$searchType==$filterType==$filterWord===$sortType")
            val uri = RequestService.SEARCH_VUE.replace("{packageName}", AppUtils.getPackageName())
            mUrl = UrlUtils.buildWebUrl(uri, params)
        }

        view?.onStartLoad(mUrl!!)

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

    companion object {
        private val TAG = SearchPresenter::class.java.simpleName

    }
}