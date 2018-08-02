package com.intelligent.reader.presenter.search

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.TextUtils
import android.widget.Toast
import com.ding.basic.bean.SearchAutoCompleteBeanYouHua
import com.intelligent.reader.R
import com.intelligent.reader.activity.CoverPageActivity
import com.intelligent.reader.activity.FindBookDetail
import com.intelligent.reader.activity.ReadingActivity
import com.intelligent.reader.activity.SearchBookActivity
import com.intelligent.reader.adapter.SearchSuggestAdapter
import com.intelligent.reader.presenter.IPresenter
import com.intelligent.reader.read.help.BookHelper
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.RequestItem
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.data.search.*
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
import net.lzbook.kit.utils.oneclick.AntiShake
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by yuchao on 2017/8/2 0002.
 */

class SearchSCPresenter(private val mContext: Context, override var view: SearchSCView.AvtView?) : IPresenter<SearchSCView.AvtView> {
    private var bookDaoHelper: BookDaoHelper? = null
    private val wordInfoMap = HashMap<String, WordInfo>()

    private var sharedPreferences: SharedPreferences? = null
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
    private var disposable: Disposable? = null
    private val TAG = SearchSCPresenter::class.java.simpleName


    init {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
        if (bookDaoHelper == null) {
            bookDaoHelper = BookDaoHelper.getInstance()
        }
    }

    private fun recycleDisposable() {
        if (disposable != null && !disposable!!.isDisposed()) {
            disposable!!.dispose()
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
            val finalWord = searchWord
            recycleDisposable()
            disposable = Observable.create(ObservableOnSubscribe<String> { e -> e.onNext(finalWord) }).debounce(400, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .switchMap<SearchAutoCompleteBeanYouHua> { s -> NetService.userService.searchAutoCompleteSecond(s) }.observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ bean ->
                        if (SearchAutoCompleteBeanYouHua.REQUESR_SUCCESS.equals(bean.getRespCode()) && bean.getData() != null) {
                            packageData(bean)
                        }
                    }) { AppLog.e("rxjava", "error") }
        }

    }


    /**
     * 根据策略，显示数据的顺序为 两个书名 + 一个间隔 + 两个作者 + 一个间隔 + 两个标签 + 一个间隔 + 剩余书名
     */
    fun packageData(bean: SearchAutoCompleteBeanYouHua) {
        val resultSuggest = ArrayList<SearchSuggest>()
        var isAllBook = true
        //两个书名
        if (bean.data.name != null && bean.data.name.size > 0) {
            val size = bean.data.name.size
            for (i in 0..(if (size >= 2) 2 else size) - 1) {
                val nameBean = bean.data.name[i]
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

        if (bean.data.authors != null && bean.data.authors.size > 0) {

            //两个作者
            for (i in 0..(if (bean.data.authors.size >= 2) 2 else bean.data.authors.size) - 1) {
                val authorsBean = bean.data.authors[i]
                if (authorsBean != null) {
                    val searchCommonBean = SearchCommonBeanYouHua()
                    searchCommonBean.suggest = bean.data.authors[i].suggest
                    searchCommonBean.wordtype = bean.data.authors[i].wordtype
                    searchCommonBean.image_url = ""
                    searchCommonBean.isAuthor = bean.data.authors[i].isAuthor
                    resultSuggest.add(SearchSuggest(searchCommonBean, SearchSuggestAdapter.ITEM_VIEW_TYPE_DATA))
                    resultSuggest.add(SearchSuggest(SearchSuggestAdapter.ITEM_VIEW_MARGEN_ONE))
                    isAllBook = false

                }
            }
            resultSuggest.removeAt(resultSuggest.size - 1)
            resultSuggest.add(SearchSuggest(SearchSuggestAdapter.ITEM_VIEW_MARGEN_TWO))
        }

        if (bean.data.label != null && bean.data.label.size > 0) {

            //两个标签
            for (i in 0..(if (bean.data.label.size >= 2) 2 else bean.data.label.size) - 1) {
                val labelBean = bean.data.label[i]
                if (labelBean != null) {
                    val searchCommonBean = SearchCommonBeanYouHua()
                    searchCommonBean.suggest = bean.data.label[i].suggest
                    searchCommonBean.wordtype = bean.data.label[i].wordtype
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
        if (bean.data.name != null) {
            for (i in 2..bean.data.name.size - 1) {
                val searchCommonBean = SearchCommonBeanYouHua()
                val nameBean = bean.data.name[i]
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

    private val shake = AntiShake()
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

            startLoadData(0)

            view?.onJsSearch()
        }

        jsInterfaceHelper.setOnEnterCover { host, book_id, book_source_id, name, author, parameter, extra_parameter ->
            AppLog.e(TAG, "doCover")

            val data = HashMap<String, String>()
            data.put("BOOKID", book_id)
            data.put("source", "WEBVIEW")
            StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.ENTER, data)

            val requestItem = RequestItem()
            requestItem.book_id = book_id
            requestItem.book_source_id = book_source_id
            requestItem.host = host
            requestItem.name = name
            requestItem.author = author

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

        jsInterfaceHelper.setOnAnotherWebClick(JSInterfaceHelper.onAnotherWebClick { url, name ->
            if (shake.check()) {
                return@onAnotherWebClick
            }
            AppLog.e(TAG, "doAnotherWeb")
            try {
                if (url.contains(URLBuilderIntterface.AUTHOR_V4)) {
                    sharedPreferences!!.edit().putString(Constants.FINDBOOK_SEARCH, "author").apply()//FindBookDetail 返回键时标识
                }
                val intent = Intent()
                intent.setClass(mContext, FindBookDetail::class.java)
                intent.putExtra("url", url)
                intent.putExtra("title", name)
                mContext.startActivity(intent)
                AppLog.e(TAG, "EnterAnotherWeb")
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

        jsInterfaceHelper.setOnInsertBook(JSInterfaceHelper.OnInsertBook { host, book_id, book_source_id, name, author, status, category, imgUrl, last_chapter, chapter_count, updateTime, parameter, extra_parameter, dex ->
            AppLog.e(TAG, "doInsertBook")
            val book = genCoverBook(host, book_id, book_source_id, name, author, status, category, imgUrl, last_chapter, chapter_count,
                    updateTime, parameter, extra_parameter, dex)
            val wordInfo = wordInfoMap[word]
            if (wordInfo != null) {
                wordInfo.actioned = true
                alilog(buildSearch(book, word!!, Search.OP.BOOKSHELF, wordInfo.computeUseTime()))
            }
            val succeed = bookDaoHelper!!.insertBook(book)
            if (succeed) {
                Toast.makeText(mContext.applicationContext, R.string.bookshelf_insert_success, Toast.LENGTH_SHORT).show()
            }
        })

        jsInterfaceHelper.setOnDeleteBook(JSInterfaceHelper.OnDeleteBook { book_id ->
            AppLog.e(TAG, "doDeleteBook")
            bookDaoHelper!!.deleteBook(book_id)
            Toast.makeText(mContext.applicationContext, R.string.bookshelf_delete_success, Toast.LENGTH_SHORT).show()
        })

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
        book.dex = dex
        book.last_updateSucessTime = System.currentTimeMillis()
        AppLog.i(TAG, "book.dex = " + book.dex)
        return book
    }



    fun startLoadData(isAuthor: Int) {
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

            if (searchType == "2" && isAuthor == 1) {

                val params = HashMap<String, String>()
                params.put("author", searchWord)
                mUrl = URLBuilderIntterface.AUTHOR_V4 + "?author=" + searchWord
                try {
                    sharedPreferences!!.edit().putString(Constants.FINDBOOK_SEARCH, "author").apply()//FindBookDetail 返回键时标识
                    SearchBookActivity.isSatyHistory= true
                    val intent = Intent()
                    intent.setClass(mContext, FindBookDetail::class.java)
                    intent.putExtra("url", mUrl)
                    intent.putExtra("title", "作者主页")
                    fromClass = "findBookDetail"
                    mContext.startActivity(intent)
                    AppLog.e(TAG, "EnterAnotherWeb")
                    return
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            } else {
                val params = HashMap<String, String>()
                params.put("keyword", searchWord)
                params.put("search_type", searchType ?: "")
                params.put("filter_type", filterType ?: "")
                params.put("filter_word", filterWord ?: "")
                params.put("sort_type", sortType ?: "")
                params.put("wordType", searchType ?: "")
                params.put("searchEmpty", "1")
                AppLog.e("kk", "$searchWord==$searchType==$filterType==$filterWord===$sortType")
                mUrl = UrlUtils.buildWebUrl(URLBuilderIntterface.SEARCH_V4, params)
            }

        }

        view?.onStartLoad(mUrl!!)

    }


//    fun startLoadData() {
//        var searchWord: String
//        if (word != null) {
//            searchWord = word as String
//            val channelID = AppUtils.getChannelId()
//            if (channelID == "blp1298_10882_001" || channelID == "blp1298_10883_001" || channelID == "blp1298_10699_001") {
//                if (Constants.isBaiduExamine && Constants.versionCode == AppUtils.getVersionCode()) {
//                    searchWord = replaceWord
//                    AppLog.e(TAG, searchWord)
//                }
//            }
//
//            val params = HashMap<String, String>()
//            params.put("word", searchWord)
//            params.put("search_type", searchType ?: "")
//            params.put("filter_type", filterType ?: "")
//            params.put("filter_word", filterWord ?: "")
//            params.put("sort_type", sortType ?: "")
//            AppLog.e("kk", "$searchWord==$searchType==$filterType==$filterWord===$sortType")
//            mUrl = UrlUtils.buildWebUrl(URLBuilderIntterface.SEARCH, params)
//        }
//
//        view?.onStartLoad(mUrl!!)
//
//    }

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
