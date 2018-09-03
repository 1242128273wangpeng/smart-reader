package com.intelligent.reader.search

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.TextUtils
import com.ding.basic.bean.Book
import com.ding.basic.bean.Chapter
import com.ding.basic.bean.SearchAutoCompleteBeanYouHua
import com.ding.basic.bean.SearchCommonBeanYouHua
import com.ding.basic.repository.RequestRepositoryFactory
import com.ding.basic.request.RequestService
import com.ding.basic.request.RequestSubscriber
import com.dingyue.contract.CommonContract
import com.dingyue.contract.IPresenter
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dingyue.contract.util.SharedPreUtil
import com.dingyue.contract.util.showToastMessage
import com.intelligent.reader.R
import com.intelligent.reader.activity.CoverPageActivity
import com.intelligent.reader.activity.FindBookDetail
import com.intelligent.reader.activity.SearchBookActivity
import com.intelligent.reader.adapter.SearchSuggest
import com.intelligent.reader.adapter.SearchSuggestAdapter
import com.intelligent.reader.presenter.search.SearchSCView
import com.orhanobut.logger.Logger
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.constants.Constants
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


class SearchPresenter(private val mContext: SearchBookActivity,
                      override var view: SearchSCView.AvtView?) : IPresenter<SearchSCView.AvtView> {

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
                    .requestAutoCompleteV4(searchWord, object : RequestSubscriber<SearchAutoCompleteBeanYouHua>() {
                override fun requestResult(result: SearchAutoCompleteBeanYouHua?) {
                    if (result != null && SearchAutoCompleteBeanYouHua.REQUESR_SUCCESS.equals(result.respCode) && result.data != null) {
                        packageData(result)
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

    /**
     * 根据策略，显示数据的顺序为 两个书名 + 一个间隔 + 两个作者 + 一个间隔 + 两个标签 + 一个间隔 + 剩余书名
     */
    fun packageData(bean: SearchAutoCompleteBeanYouHua) {
        val resultSuggest = ArrayList<SearchSuggest>()
        var isAllBook = true
        //两个书名
        if (bean.data!!.name != null && bean.data!!.name!!.size > 0) {
            val size = bean!!.data!!.name!!.size
            for (i in 0..(if (size >= 2) 2 else size) - 1) {
                val nameBean = bean.data!!.name!![i]
                if (nameBean != null) {
                    val searchCommonBean = SearchCommonBeanYouHua()
                    searchCommonBean.suggest = nameBean.suggest
                    searchCommonBean.wordtype = nameBean.wordtype
                    searchCommonBean.image_url = nameBean.imgUrl

                    //-----------------书名特有字段-------------------------------
                    searchCommonBean.host = nameBean.host
                    searchCommonBean.book_id = nameBean.bookid
                    searchCommonBean.book_source_id = nameBean.bookSourceId
                    searchCommonBean.name = nameBean.bookName
                    searchCommonBean.author = nameBean.author
                    searchCommonBean.bookType = nameBean.vip.toString() + ""
                    //------------------------------------------------------------

                    resultSuggest.add(SearchSuggest(searchCommonBean, SearchSuggestAdapter.ITEM_VIEW_TYPE_DATA))
                    resultSuggest.add(SearchSuggest(SearchSuggestAdapter.ITEM_VIEW_MARGEN_ONE))
                }
            }
            resultSuggest.removeAt(resultSuggest.size - 1)
            resultSuggest.add(SearchSuggest(SearchSuggestAdapter.ITEM_VIEW_MARGEN_TWO))
        }

        if (bean.data!!.authors != null && bean.data!!.authors!!.size > 0) {

            //两个作者
            for (i in 0..(if (bean.data!!.authors!!.size >= 2) 2 else bean.data!!.authors!!.size) - 1) {
                val authorsBean = bean.data!!.authors!![i]
                if (authorsBean != null) {
                    val searchCommonBean = SearchCommonBeanYouHua()
                    searchCommonBean.suggest = bean.data!!.authors!![i].suggest
                    searchCommonBean.wordtype = bean.data!!.authors!![i].wordtype
                    searchCommonBean.image_url = ""
                    searchCommonBean.isAuthor = bean.data!!.authors!![i].isAuthor
                    resultSuggest.add(SearchSuggest(searchCommonBean, SearchSuggestAdapter.ITEM_VIEW_TYPE_DATA))
                    resultSuggest.add(SearchSuggest(SearchSuggestAdapter.ITEM_VIEW_MARGEN_ONE))
                    isAllBook = false

                }
            }
            resultSuggest.removeAt(resultSuggest.size - 1)
            resultSuggest.add(SearchSuggest(SearchSuggestAdapter.ITEM_VIEW_MARGEN_TWO))
        }

        if (bean.data!!.label != null && bean.data!!.label!!.size > 0) {

            //两个标签
            for (i in 0..(if (bean.data!!.label!!.size >= 2) 2 else bean.data!!.label!!.size) - 1) {
                val labelBean = bean.data!!.label!![i]
                if (labelBean != null) {
                    val searchCommonBean = SearchCommonBeanYouHua()
                    searchCommonBean.suggest = bean.data!!.label!![i].suggest
                    searchCommonBean.wordtype = bean.data!!.label!![i].wordtype
                    searchCommonBean.image_url = ""
                    resultSuggest.add(SearchSuggest(searchCommonBean, SearchSuggestAdapter.ITEM_VIEW_TYPE_DATA))
                    resultSuggest.add(SearchSuggest(SearchSuggestAdapter.ITEM_VIEW_MARGEN_ONE))
                    isAllBook = false

                }
            }
            resultSuggest.removeAt(resultSuggest.size - 1)
            resultSuggest.add(SearchSuggest(SearchSuggestAdapter.ITEM_VIEW_MARGEN_TWO))
        }

        //其余书名
        if (bean.data!!.name != null) {
            for (i in 2..bean.data!!.name!!.size - 1) {
                val searchCommonBean = SearchCommonBeanYouHua()
                val nameBean = bean.data!!.name!![i]
                if (nameBean != null) {
                    searchCommonBean.suggest = nameBean.suggest
                    searchCommonBean.wordtype = nameBean.wordtype
                    searchCommonBean.image_url = nameBean.imgUrl

                    //-----------------书名特有字段-------------------------------
                    searchCommonBean.host = nameBean.host
                    searchCommonBean.book_id = nameBean.bookid
                    searchCommonBean.book_source_id = nameBean.bookSourceId
                    searchCommonBean.name = nameBean.bookName
                    searchCommonBean.author = nameBean.author
                    searchCommonBean.bookType = nameBean.vip.toString() + ""
                    //------------------------------------------------------------

                    resultSuggest.add(SearchSuggest(searchCommonBean, SearchSuggestAdapter.ITEM_VIEW_TYPE_DATA))
                    resultSuggest.add(SearchSuggest(SearchSuggestAdapter.ITEM_VIEW_MARGEN_ONE))
                }
            }
        }

//        // 数据全是书名标签,不显示lis中类别之间的间距
//        if (isAllBook) {
//            for (b in resultSuggest) {
//                if (SearchSuggestAdapter.ITEM_VIEW_MARGEN_TWO === b.type) {
//                    b.type = SearchSuggestAdapter.ITEM_VIEW_MARGEN_ONE
//                }
//            }
//        }

        if (!resultSuggest.isEmpty() && SearchSuggestAdapter.ITEM_VIEW_MARGEN_TWO === resultSuggest.get(resultSuggest.size - 1).type) {
            resultSuggest[resultSuggest.size - 1].type = SearchSuggestAdapter.ITEM_VIEW_MARGEN_ONE
        }

        for (bean1 in resultSuggest) {
            AppLog.e("uuu", bean1.toString())
        }
        if (searchSuggestCallBack != null) {
            searchSuggestCallBack!!.onSearchResult(resultSuggest, bean)
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

            startLoadData(0) //0代表不是作者

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
            bundle.putString("author", author)
            bundle.putString("book_id", book_id)
            bundle.putString("book_source_id", book_source_id)
            intent.putExtras(bundle)
            mContext.startActivity(intent)
        }

        jsInterfaceHelper.setOnAnotherWebClick(JSInterfaceHelper.onAnotherWebClick { url, name ->
            if (CommonContract.isDoubleClick(System.currentTimeMillis())) {
                return@onAnotherWebClick
            }
            try {
                if (url.contains(RequestService.AUTHOR_V4)) {
                    shareUtil!!.putString(Constants.FINDBOOK_SEARCH,
                            "author")//FindBookDetail 返回键时标识
                }
                val intent = Intent()
                intent.setClass(mContext, FindBookDetail::class.java)
                intent.putExtra("url", url)
                intent.putExtra("title", name)
                mContext.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        jsInterfaceHelper.setSearchWordClick(JSInterfaceHelper.onSearchWordClick { searchWord, search_type ->
            word = searchWord
            searchType = search_type

            startLoadData(0)

            view?.onNoneResultSearch(searchWord)
        })

        jsInterfaceHelper.setOnTurnRead(JSInterfaceHelper.onTurnRead { book_id, book_source_id, host, name, author, parameter, extra_parameter, update_type, last_chapter_name, serial_number, img_url, update_time, desc, label, status, bookType ->


            val book = Book()
            book.book_id = book_id
            book.book_source_id = book_source_id
            book.host = host
            book.author = author
            book.name = name
            book.last_chapter?.name = last_chapter_name
            book.chapter_count = serial_number
            book.img_url = img_url
            book.last_chapter?.update_time = update_time
            book.sequence = -1
            book.desc = desc
            book.label = label

            book.status = status

            //bookType为是否付费书籍标签 除快读外不加

            FootprintUtils.saveHistoryShelf(book)
            val bundle = Bundle()

            bundle.putInt("sequence", 0)
            bundle.putInt("offset", 0)
            bundle.putSerializable("book", book)
            RouterUtil.navigation(mContext, RouterConfig.READER_ACTIVITY, bundle)
        })

        jsInterfaceHelper.setOnEnterRead { host, book_id, book_source_id, name, author, status, category, imgUrl, last_chapter, chapter_count, updateTime, parameter, extra_parameter, dex ->
            val coverBook = genCoverBook(host, book_id, book_source_id, name, author, status, category, imgUrl, last_chapter, chapter_count,
                    updateTime, parameter, extra_parameter, dex)

            val bundle = Bundle()
            bundle.putInt("sequence", 0)
            bundle.putInt("offset", 0)
            bundle.putSerializable("book", coverBook)

            RouterUtil.navigation(mContext, RouterConfig.READER_ACTIVITY, bundle)
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
                mContext.showToastMessage(R.string.bookshelf_insert_success)
            }
        }

        jsInterfaceHelper.setOnDeleteBook { book_id ->
            RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).deleteBook(book_id)
            CacheManager.stop(book_id)
            CacheManager.resetTask(book_id)
            mContext.showToastMessage(R.string.bookshelf_delete_success)
        }
    }

    fun genCoverBook(host: String, book_id: String, book_source_id: String, name: String, author: String, status: String, category: String,
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
            mUrl = RequestService.AUTHOR_V4 + "?author=" + searchWord
            try {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                sharedPreferences.edit().putString(Constants.FINDBOOK_SEARCH, "author").apply()//FindBookDetail 返回键时标识
                SearchBookActivity.isStayHistory = true
                val intent = Intent()
                intent.setClass(mContext, FindBookDetail::class.java)
                intent.putExtra("url", mUrl)
                intent.putExtra("title", "作者主页")
                fromClass = "findBookDetail"
                mContext.startActivity(intent)
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else {
            val params = HashMap<String, String>()
            params.put("keyword", searchWord ?: "")
            params.put("search_type", searchType ?: "")
            params.put("filter_type", filterType ?: "")
            params.put("filter_word", filterWord ?: "")
            params.put("sort_type", sortType ?: "")
            params.put("wordType", searchType ?: "")
            params.put("searchEmpty", "1")
            AppLog.e("kk", "$searchWord==$searchType==$filterType==$filterWord===$sortType")
            mUrl = UrlUtils.buildWebUrl(RequestService.SEARCH_V4, params)
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
        fun onSearchResult(suggestList: List<SearchSuggest>, transmitBean: SearchAutoCompleteBeanYouHua)
    }


}