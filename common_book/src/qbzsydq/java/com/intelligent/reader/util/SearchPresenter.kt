package com.intelligent.reader.util


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.ding.basic.bean.Book
import com.ding.basic.bean.Chapter
import com.ding.basic.bean.SearchAutoCompleteBeanYouHua
import com.ding.basic.bean.SearchCommonBeanYouHua
import com.ding.basic.repository.RequestRepositoryFactory
import com.ding.basic.request.RequestService
import com.ding.basic.request.RequestSubscriber
import com.intelligent.reader.R
import com.intelligent.reader.activity.CoverPageActivity
import com.intelligent.reader.activity.SearchBookActivity
import com.orhanobut.logger.Logger
import net.lzbook.kit.base.BaseBookApplication
import net.lzbook.kit.base.IPresenter
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.download.CacheManager
import net.lzbook.kit.utils.logger.AppLog
import net.lzbook.kit.utils.router.BookRouter
import net.lzbook.kit.utils.statistic.alilog
import net.lzbook.kit.utils.statistic.buildSearch
import net.lzbook.kit.utils.statistic.model.Search
import net.lzbook.kit.utils.webview.JSInterfaceHelper
import net.lzbook.kit.utils.webview.UrlUtils
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.*

/**
 * Created by yuchao on 2017/8/2 0002.
 */

class SearchPresenter(private val activity: SearchBookActivity, private val mContext: Context, override var view: SearchView.AvtView?) : IPresenter<SearchView.AvtView> {
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
            RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestAutoCompleteV5(searchWord, object : RequestSubscriber<SearchAutoCompleteBeanYouHua>() {
                override fun requestResult(result: SearchAutoCompleteBeanYouHua?) {
                    val resultSuggest = ArrayList<SearchCommonBeanYouHua>()
                    resultSuggest.clear()
                    transmitBean = result
                    AppLog.e("bean", result.toString())
                    if (result != null && result.respCode == "20000"&& result.data != null) {
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

    fun initJSHelp(jsInterfaceHelper: JSInterfaceHelper?) {

        if (jsInterfaceHelper == null) {
            return
        }

        jsInterfaceHelper.setOnSearchClick { keyWord, search_type, filter_type, filter_word, sort_type ->
            AppLog.e("aaa", "aaaa")
            word = keyWord
            searchType = search_type
            filterType = filter_type
            filterWord = filter_word
            sortType = sort_type

            startLoadData()

            view?.onJsSearch()
        }

        jsInterfaceHelper.setOnEnterCover { host, book_id, book_source_id, name, author, parameter, extra_parameter ->

            val book = Book()
            book.book_id = book_id
            book.book_source_id = book_source_id
            book.host = host
            book.name = name
            book.author = author

            val wordInfo = wordInfoMap[word]
            if (wordInfo != null && word != null) {
                wordInfo.actioned = true
                alilog(buildSearch(book, word!!, Search.OP.COVER, wordInfo.computeUseTime()))
            }
            val intent = Intent()
            intent.setClass(mContext, CoverPageActivity::class.java)
            val bundle = Bundle()
            bundle.putString("book_id",book_id)
            bundle.putString("book_source_id", book_source_id)
            intent.putExtras(bundle)
            mContext.startActivity(intent)
        }

        jsInterfaceHelper.setOnEnterRead { host, book_id, book_source_id, name, author, status, category, imgUrl, last_chapter, chapter_count, updateTime, parameter, extra_parameter, dex ->
            val coverBook = genCoverBook(host, book_id, book_source_id, name, author, status, category, imgUrl, last_chapter, chapter_count,
                    updateTime, parameter, extra_parameter, dex)
            BookRouter.navigateCoverOrRead(activity, coverBook, 0)
        }

        val booksOnLine = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBooks()
        val stringBuilder = StringBuilder()

        if (booksOnLine != null && booksOnLine.isNotEmpty()) {
            stringBuilder.append("[")
            for (i in booksOnLine.indices) {
                stringBuilder.append("{'id':'").append(booksOnLine[i].book_id).append("'}")
                if (i != booksOnLine.size - 1) {
                    stringBuilder.append(",")
                }
            }
            stringBuilder.append("]")
        }

        jsInterfaceHelper.setBookString(stringBuilder.toString())

        jsInterfaceHelper.setOnInsertBook { host, book_id, book_source_id, name, author, status, category, imgUrl, last_chapter, chapter_count, updateTime, parameter, extra_parameter, dex ->
            val book = genCoverBook(host, book_id, book_source_id, name, author, status, category, imgUrl, last_chapter, chapter_count,
                    updateTime, parameter, extra_parameter, dex)
            val wordInfo = wordInfoMap[word]
            if (wordInfo != null && word != null) {
                wordInfo.actioned = true
                alilog(buildSearch(book, word!!, Search.OP.BOOKSHELF, wordInfo.computeUseTime()))
            }
            val succeed = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).insertBook(book)
            if (succeed > 0) {
                Toast.makeText(mContext.applicationContext, R.string.bookshelf_insert_success, Toast.LENGTH_SHORT).show()
            }
        }

        jsInterfaceHelper.setOnDeleteBook { book_id ->
            RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).deleteBook(book_id)
            CacheManager.stop(book_id)
            CacheManager.resetTask(book_id)
            Toast.makeText(mContext.applicationContext, R.string.bookshelf_delete_success, Toast.LENGTH_SHORT).show()
        }
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
        var lastChapter = Chapter()
        lastChapter.update_time = update_time
        lastChapter.name = last_chapter
        book.last_chapter = lastChapter
        book.last_update_success_time = System.currentTimeMillis()
        book.update_date_fusion = 0
        return book
    }

    fun startLoadData() {
        var searchWord: String
        if (word != null) {
            searchWord = word as String
            val channelID = AppUtils.getChannelId()
            if (channelID == "blp1298_10882_001" || channelID == "blp1298_10883_001" || channelID == "blp1298_10699_001") {
                if (Constants.isBaiduExamine && Constants.versionCode == AppUtils.getVersionCode()) {
                    searchWord = replaceWord
                }
            }

            val params = HashMap<String, String>()
            params.put("word", searchWord)
            params.put("search_type", searchType ?: "")
            params.put("filter_type", filterType ?: "")
            params.put("filter_word", filterWord ?: "")
            params.put("sort_type", sortType ?: "")
            AppLog.e("kk", "$searchWord==$searchType==$filterType==$filterWord===$sortType")
            mUrl = UrlUtils.buildWebUrl(RequestService.SEARCH_V5, params)
        }

        view?.onStartLoad(mUrl!!)

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

}