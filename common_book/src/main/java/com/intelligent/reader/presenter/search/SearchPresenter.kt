package com.intelligent.reader.presenter.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.intelligent.reader.R
import com.intelligent.reader.activity.CoverPageActivity
import com.intelligent.reader.activity.ReadingActivity
import com.dingyue.contract.IPresenter
import com.dingyue.contract.util.showToastMessage
import com.intelligent.reader.read.help.BookHelper
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.RequestItem
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.data.search.SearchAutoCompleteBean
import net.lzbook.kit.data.search.SearchCommonBean
import net.lzbook.kit.encrypt.URLBuilderIntterface
import net.lzbook.kit.net.custom.service.NetService
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.statistic.alilog
import net.lzbook.kit.statistic.buildSearch
import net.lzbook.kit.statistic.model.Search
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.FootprintUtils
import net.lzbook.kit.utils.JSInterfaceHelper
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.*

/**
 * Created by yuchao on 2017/8/2 0002.
 */

class SearchPresenter(private val mContext: Context, override var view: SearchView.AvtView?) : IPresenter<SearchView.AvtView> {
    private var bookDaoHelper: BookDaoHelper? = null
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
    private var transmitBean: SearchAutoCompleteBean? = null


    init {
        if (bookDaoHelper == null) {
            bookDaoHelper = BookDaoHelper.getInstance()
        }
    }

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
            val searchService = NetService.userService
            searchService.searchAutoComplete(searchWord)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<SearchAutoCompleteBean> {
                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onNext(bean: SearchAutoCompleteBean) {
                            val resultSuggest = ArrayList<SearchCommonBean>()
                            resultSuggest.clear()
                            transmitBean = bean
                            AppLog.e("bean", bean.toString())
                            if (bean.respCode == "20000" && bean.data != null) {
                                for (i in 0..bean.data.authors.size - 1) {
                                    val searchCommonBean = SearchCommonBean()
                                    searchCommonBean.suggest = bean.data.authors[i].suggest
                                    searchCommonBean.wordtype = bean.data.authors[i].wordtype
                                    resultSuggest.add(searchCommonBean)
                                }
                                for (i in 0..bean.data.label.size - 1) {
                                    val searchCommonBean = SearchCommonBean()
                                    searchCommonBean.suggest = bean.data.label[i].suggest
                                    searchCommonBean.wordtype = bean.data.label[i].wordtype
                                    resultSuggest.add(searchCommonBean)
                                }
                                for (i in 0..bean.data.name.size - 1) {
                                    val searchCommonBean = SearchCommonBean()
                                    searchCommonBean.suggest = bean.data.name[i].suggest
                                    searchCommonBean.wordtype = bean.data.name[i].wordtype
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

                        override fun onError(e: Throwable) {

                            AppLog.e("result", e.toString())
                        }

                        override fun onComplete() {
                            AppLog.e("result22", "onComplete")
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
            AppLog.e(TAG, "doCover")

            val requestItem = RequestItem()
            requestItem.book_id = book_id
            requestItem.book_source_id = book_source_id
            requestItem.host = host
            requestItem.name = name
            requestItem.author = author
            requestItem.parameter = parameter
            requestItem.extra_parameter = extra_parameter

            val wordInfo = wordInfoMap[word]
            if (wordInfo != null && word != null) {
                wordInfo.actioned = true
                alilog(buildSearch(requestItem, word!!, Search.OP.COVER, wordInfo.computeUseTime()))
            }
            val intent = Intent()
            intent.setClass(mContext, CoverPageActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable(Constants.REQUEST_ITEM, requestItem)
            intent.putExtras(bundle)
            mContext.startActivity(intent)
        }

        jsInterfaceHelper.setSearchWordClick(JSInterfaceHelper.onSearchWordClick { searchWord, search_type ->
            word = searchWord
            searchType = search_type

            startLoadData()

            view?.onNoneResultSearch(searchWord)
        })

        jsInterfaceHelper.setOnTurnRead(JSInterfaceHelper.onTurnRead { book_id, book_source_id, host, name, author, parameter, extra_parameter, update_type, last_chapter_name, serial_number, img_url, update_time, desc, label, status, bookType ->
            val intent = Intent()
            val bundle = Bundle()

            bundle.putInt("sequence", 0)
            bundle.putInt("offset", 0)

            val book = Book()
            book.book_id = book_id
            book.book_source_id = book_source_id
            book.site = host
            book.author = author
            book.name = name
            book.last_chapter_name = last_chapter_name
            book.chapter_count = serial_number
            book.img_url = img_url
            book.last_updatetime_native = update_time
            book.sequence = -1
            book.desc = desc
            book.category = label
            if ("FINISH" == status) {
                book.status = 2
            } else {
                book.status = 1
            }
            //                book.mBookType = Integer.parseInt(bookType);
            //bookType为是否付费书籍标签 除快读外不加

            bundle.putSerializable("book", book)
            FootprintUtils.saveHistoryShelf(book)
            val requestItem = RequestItem()
            requestItem.book_id = book_id
            requestItem.book_source_id = book_source_id
            requestItem.host = host
            requestItem.name = name
            requestItem.author = author
            //                requestItem.mBookType = Integer.parseInt(bookType);
            bundle.putSerializable(Constants.REQUEST_ITEM, requestItem)

            bundle.putSerializable("book", book)

            AppLog.e(TAG, "GotoReading: " + book.site + " : " + requestItem.host)
            intent.setClass(mContext, ReadingActivity::class.java)
            intent.putExtras(bundle)
            mContext.startActivity(intent)
        })

        jsInterfaceHelper.setOnEnterRead { host, book_id, book_source_id, name, author, status, category, imgUrl, last_chapter, chapter_count, updateTime, parameter, extra_parameter, dex ->
            AppLog.e(TAG, "doRead")
            val coverBook = genCoverBook(host, book_id, book_source_id, name, author, status, category, imgUrl, last_chapter, chapter_count,
                    updateTime, parameter, extra_parameter, dex)
            AppLog.e(TAG, "DoRead : " + coverBook.sequence)

            //                alilog(buildSearch(coverBook, word, Search.OP.RETURN));

            BookHelper.goToRead(mContext, coverBook)
        }

        val booksOnLine = bookDaoHelper!!.booksOnLineList
        val stringBuilder = StringBuilder()
        stringBuilder.append("[")
        for (i in booksOnLine.indices) {
            stringBuilder.append("{'id':'").append(booksOnLine[i].book_id).append("'}")
            if (i != booksOnLine.size - 1) {
                stringBuilder.append(",")
            }
        }
        stringBuilder.append("]")
        AppLog.e(TAG, "StringBuilder : " + stringBuilder.toString())
        jsInterfaceHelper.setBookString(stringBuilder.toString())

        jsInterfaceHelper.setOnInsertBook { host, book_id, book_source_id, name, author, status, category, imgUrl, last_chapter, chapter_count, updateTime, parameter, extra_parameter, dex ->
            AppLog.e(TAG, "doInsertBook")
            val book = genCoverBook(host, book_id, book_source_id, name, author, status, category, imgUrl, last_chapter, chapter_count,
                    updateTime, parameter, extra_parameter, dex)
            val wordInfo = wordInfoMap[word]
            if (wordInfo != null && word != null) {
                wordInfo.actioned = true
                alilog(buildSearch(book, word!!, Search.OP.BOOKSHELF, wordInfo.computeUseTime()))
            }
            val succeed = bookDaoHelper!!.insertBook(book)
            if (succeed) {
                mContext.showToastMessage(R.string.bookshelf_insert_success)
            }
        }

        jsInterfaceHelper.setOnDeleteBook { book_id ->
            AppLog.e(TAG, "doDeleteBook")
            bookDaoHelper!!.deleteBook(book_id)
            mContext.showToastMessage(R.string.bookshelf_delete_success)
        }
    }

    protected fun genCoverBook(host: String, book_id: String, book_source_id: String, name: String, author: String, status: String, category: String,
                               imgUrl: String, last_chapter: String, chapter_count: String, update_time: Long, parameter: String, extra_parameter: String, dex: Int): Book {
        val book = Book()

        if (status == "FINISH") {
            book.status = 2
        } else {
            book.status = 1
        }

        book.book_id = book_id
        book.book_source_id = book_source_id
        book.name = name
        book.category = category
        book.author = author
        book.img_url = imgUrl
        book.site = host
        book.last_chapter_name = last_chapter
        book.chapter_count = Integer.valueOf(chapter_count)!!
        book.last_updatetime_native = update_time
        book.parameter = parameter
        book.extra_parameter = extra_parameter
        book.dex = dex
        book.last_updateSucessTime = System.currentTimeMillis()
        AppLog.i(TAG, "book.dex = " + book.dex)
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
                    AppLog.e(TAG, searchWord)
                }
            }

            val params = HashMap<String, String>()
            params.put("word", searchWord)
            params.put("search_type", searchType ?: "")
            params.put("filter_type", filterType ?: "")
            params.put("filter_word", filterWord ?: "")
            params.put("sort_type", sortType ?: "")
            params.put("searchEmpty", "1")
            AppLog.e("kk", "$searchWord==$searchType==$filterType==$filterWord===$sortType")
            mUrl = UrlUtils.buildWebUrl(URLBuilderIntterface.SEARCH, params)
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
        fun onSearchResult(suggestList: List<SearchCommonBean>, transmitBean: SearchAutoCompleteBean)
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
