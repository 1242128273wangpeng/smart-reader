package com.dingyue.searchbook.model

import android.content.Intent
import android.os.Bundle
import com.ding.basic.bean.Book
import com.ding.basic.bean.Chapter
import com.ding.basic.bean.SearchAutoCompleteBeanYouHua
import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.net.api.service.RequestService
import com.ding.basic.util.editShared

import com.dingyue.searchbook.interfaces.OnSearchResult
import com.dingyue.searchbook.interfaces.OnResultListener
import com.dingyue.searchbook.JSInterface
import com.dingyue.searchbook.R
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.utils.download.CacheManager
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.webview.UrlUtils
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.book.FootprintUtils
import net.lzbook.kit.utils.webview.JSInterfaceHelper
import net.lzbook.kit.utils.oneclick.AntiShake
import net.lzbook.kit.utils.statistic.alilog
import net.lzbook.kit.utils.statistic.buildSearch
import net.lzbook.kit.utils.statistic.model.Search
import net.lzbook.kit.utils.toast.ToastUtil
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

    fun initJSModel(): JSInterfaceHelper {
        val jsInterfaceModel = JSInterfaceHelper()
        jsInterfaceModel.setOnSearchClick(object : JSInterfaceHelper.onSearchClick {
            override fun doSearch(keyWord: String?, search_type: String?, filter_type: String?, filter_word: String, sort_type: String) {
                word = keyWord ?: ""
                searchType = search_type ?: ""
                filterType = filter_type ?: ""
                filterWord = filter_word
                sortType = sort_type

                listener?.onSearchResult(startLoadData(0) ?: "")

            }

        })

        jsInterfaceModel.setOnEnterCover(object : JSInterfaceHelper.onEnterCover {
            override fun doCover(host: String?, book_id: String?, book_source_id: String?, name: String, author: String, parameter: String, extra_parameter: String) {
                val data = HashMap<String, String>()
                data.put("BOOKID", book_id ?: "")
                data.put("source", "WEBVIEW")
                StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(), StartLogClickUtil.BOOOKDETAIL_PAGE,
                        StartLogClickUtil.ENTER, data)

                val book = Book()
                book.book_id = book_id ?: ""
                book.book_source_id = book_source_id ?: ""
                book.host = host
                book.name = name
                book.author = author

                val wordInfo = wordInfoMap[word]
                if (wordInfo != null) {
                    wordInfo.actioned = true
                    alilog(buildSearch(book, word, Search.OP.COVER, wordInfo.computeUseTime()))
                }
                val bundle = Bundle()
                bundle.putString("book_id", book_id)
                bundle.putString("book_source_id", book_source_id)
                listener?.onCoverResult(bundle)
            }
        })


        jsInterfaceModel.setOnAnotherWebClick(object : JSInterfaceHelper.onAnotherWebClick {
            override fun doAnotherWeb(url: String?, name: String?) {
                if (shake.check()) {
                    return
                }
                try {
                    if (url?.contains(RequestService.AUTHOR_h5.replace("{packageName}", AppUtils.getPackageName())) == true) {
                        //FindBookDetail 返回键时标识
                        BaseBookApplication.getGlobalContext().editShared {
                            putString(Constants.FINDBOOK_SEARCH, "author")
                        }
                    }

                    val bundle = Bundle()
                    bundle.putString("url", url)
                    bundle.putString("title", name)
                    listener?.onAnotherResult(bundle)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        })


        jsInterfaceModel.setSearchWordClick(object : JSInterfaceHelper.onSearchWordClick {
            override fun sendSearchWord(searchWord: String, search_type: String) {
                if (shake.check()) {
                    return
                }
                word = searchWord
                searchType = search_type

                //不正常后删除回掉
                listener?.onSearchWordResult(searchWord)
                listener?.onSearchResult(startLoadData(0) ?: "")


            }

        })


        jsInterfaceModel.setOnTurnRead(object : JSInterfaceHelper.onTurnRead {
            override fun turnRead(book_id: String, book_source_id: String, host: String, name: String, author: String, parameter: String, extra_parameter: String, update_type: String, last_chapter_name: String, serial_number: Int, img_url: String, update_time: Long, desc: String, label: String, status: String, bookType: String) {
                val book = Book()
                book.book_id = book_id
                book.book_source_id = book_source_id
                book.host = host
                book.author = author
                book.name = name
                val chapter = Chapter()
                chapter.name = last_chapter_name
                book.last_chapter = chapter
                book.chapter_count = serial_number
                book.img_url = img_url
                book.last_update_success_time = update_time
                book.sequence = -1
                book.desc = desc
                book.label = label
                book.status = status

                //                book.mBookType = Integer.parseInt(bookType);
                //bookType为是否付费书籍标签 除快读外不加

                FootprintUtils.saveHistoryShelf(book)

                val bundle = Bundle()

                bundle.putInt("sequence", 0)
                bundle.putInt("offset", 0)
                bundle.putSerializable("book", book)
                listener?.onTurnReadResult(bundle)
            }
        })


        jsInterfaceModel.setOnEnterRead(object : JSInterfaceHelper.onEnterRead {
            override fun doRead(host: String, book_id: String?, book_source_id: String, name: String?, author: String?, status: String, category: String, imgUrl: String, last_chapter: String, chapter_count: String, updateTime: Long, parameter: String, extra_parameter: String, dex: Int) {
                val coverBook = genCoverBook(host, book_id ?: "", book_source_id, name ?: "", author ?: "", status,
                        category, imgUrl, last_chapter, chapter_count,
                        updateTime, parameter, extra_parameter, dex)

                val bundle = Bundle()
                bundle.putInt("sequence", coverBook.sequence)
                bundle.putInt("offset", coverBook.offset)
                bundle.putSerializable("book", coverBook)
                listener?.onEnterReadResult(bundle)
            }

        })


        val booksOnLine = RequestRepositoryFactory.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).loadBooks()
        val stringBuilder = StringBuilder()
        stringBuilder.append("[")
        if (booksOnLine != null) {
            for (i in booksOnLine.indices) {
                stringBuilder.append("{'id':'").append(booksOnLine[i].book_id).append(
                        "'}")
                if (i != booksOnLine.size - 1) {
                    stringBuilder.append(",")
                }
            }
            stringBuilder.append("]")
//            AppLog.e(TAG, "StringBuilder : " + stringBuilder.toString())
            jsInterfaceModel.setBookString(stringBuilder.toString())
        }


        jsInterfaceModel.setOnInsertBook(object : JSInterfaceHelper.OnInsertBook {
            override fun doInsertBook(host: String, book_id: String?, book_source_id: String?, name: String?, author: String?, status: String, category: String, imgUrl: String, last_chapter: String, chapter_count: String, updateTime: Long, parameter: String, extra_parameter: String, dex: Int) {
                val book = genCoverBook(host, book_id ?: "", book_source_id ?: "", name ?: "", author ?: "", status,
                        category, imgUrl, last_chapter, chapter_count,
                        updateTime, parameter, extra_parameter, dex)
                val wordInfo = wordInfoMap[word]
                if (wordInfo != null) {
                    wordInfo.actioned = true
                    alilog(buildSearch(book, word, Search.OP.BOOKSHELF, wordInfo.computeUseTime()))
                }
                val succeed = RequestRepositoryFactory.loadRequestRepositoryFactory(
                        BaseBookApplication.getGlobalContext()).insertBook(book)
                if (succeed > 0) {
                    ToastUtil.showToastMessage(R.string.bookshelf_insert_success)
                }

            }

        })


        jsInterfaceModel.setOnDeleteBook(object : JSInterfaceHelper.OnDeleteBook {
            override fun doDeleteBook(book_id: String?) {
                RequestRepositoryFactory.loadRequestRepositoryFactory(
                        BaseBookApplication.getGlobalContext()).deleteBook(book_id ?: "")
                CacheManager.stop(book_id ?: "")
                CacheManager.resetTask(book_id ?: "")
                ToastUtil.showToastMessage(R.string.bookshelf_delete_success)
            }
        })

        return jsInterfaceModel
    }

    private fun genCoverBook(host: String, book_id: String, book_source_id: String, name: String,
                             author: String, status: String, category: String,
                             imgUrl: String, last_chapter: String, chapter_count: String, update_time: Long,
                             parameter: String, extra_parameter: String, dex: Int): Book {
        val book = Book()
        book.status = status
        book.book_id = book_id
        book.book_source_id = book_source_id
        book.name = name
        book.label = category
        book.author = author
        book.img_url = imgUrl
        book.host = host
        val chapter = Chapter()
        chapter.name = last_chapter
        chapter.update_time = update_time
        book.last_chapter = chapter
        book.chapter_count = Integer.valueOf(chapter_count)
        book.last_update_success_time = System.currentTimeMillis()
        return book
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


    fun startLoadData(isAuthor: Int): String? {
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
                    BaseBookApplication.getGlobalContext().editShared {
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