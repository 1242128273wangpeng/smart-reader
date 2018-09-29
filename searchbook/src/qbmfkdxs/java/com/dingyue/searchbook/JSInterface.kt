package com.dingyue.searchbook

import android.os.Handler
import android.text.TextUtils
import android.webkit.JavascriptInterface
import com.ding.basic.repository.RequestRepositoryFactory
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.WebViewJsInterface
import java.util.*

/**
 * Desc：js逻辑处理
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/9/26 0025 16:21
 */
class JSInterface : WebViewJsInterface {

     var enterApp: OnEnterAppClick? = null
     var anotherWeb: OnAnotherWebClick? = null
     var gameAppClick: OnGameAppClick? = null
     var webGameClick: OnWebGameClick? = null
     var search: OnSearchClick? = null
     var cover: OnEnterCover? = null
     var read: OnEnterRead? = null
     var toRead: onTurnRead? = null
     var mCategory: OnEnterCategory? = null
     var ad: OnOpenAd? = null
     var showToast: OnShowToastListener? = null
     var closeWebView: OnCloseWebViewListener? = null
     var handler: Handler
     var insertBook: OnInsertBook? = null
     var deleteBook: OnDeleteBook? = null
     var pagerInfo: OnH5PagerInfoListener? = null
     var strings: String? = null
     var searchWordClick: OnSearchWordClick? = null
     var subSearchBook: OnSubSearchBook? = null
     var searchResultNotify: OnSearchResultNotify? = null

    init {
        handler = Handler()

    }

    fun setBookString(strings: String) {
        this.strings = strings
    }

    fun setOnInsertBook(insertBook: OnInsertBook) {
        this.insertBook = insertBook
    }

    fun setOnDeleteBook(deleteBook: OnDeleteBook) {
        this.deleteBook = deleteBook
    }

    fun setOnEnterAppClick(enterApp: OnEnterAppClick) {
        this.enterApp = enterApp
    }

    fun setOnAnotherWebClick(another: OnAnotherWebClick) {
        this.anotherWeb = another
    }

    fun setOnWebGameClick(webGameClick: OnWebGameClick) {
        this.webGameClick = webGameClick
    }

    fun setOnGameAppClick(gameAppClick: OnGameAppClick) {
        this.gameAppClick = gameAppClick
    }

    fun setOnSearchClick(search: OnSearchClick) {
        this.search = search
    }

    fun setOnEnterCover(cover: OnEnterCover) {
        this.cover = cover
    }

    fun setOnEnterRead(read: OnEnterRead) {
        this.read = read
    }

    fun setOnEnterCategory(category: OnEnterCategory) {
        this.mCategory = category
    }

    fun setOnOpenAd(ad: OnOpenAd) {
        this.ad = ad
    }

    fun setOnH5PagerInfo(info: OnH5PagerInfoListener) {
        this.pagerInfo = info
    }

    fun setOnSearchResultNotify(notify: OnSearchResultNotify) {
        this.searchResultNotify = notify
    }

    @JavascriptInterface
    override fun enterSearch(keyWord: String?, search_type: String?, filter_type: String?, filter_word: String, sort_type: String) {
        if (keyWord == null || search_type == null || filter_type == null)
            return

        handler.post {

                search?.doSearch(keyWord, search_type, filter_type, filter_word, sort_type)
//
        }
    }

    @JavascriptInterface
    override fun buildAjaxUrl(url: String?): String? {
        var mUrl = url
        if (mUrl != null) {
            val array = mUrl.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (array.size == 2) {
                mUrl = array[0]
                mUrl = UrlUtils.buildWebUrl(mUrl, UrlUtils.getUrlParams(array[1]))
            } else if (array.size == 1) {
                mUrl = UrlUtils.buildWebUrl(mUrl, HashMap())
            }
        }
        return mUrl
    }

    @JavascriptInterface
    override fun returnBooks(): String {
        return strings?:""
    }

    @JavascriptInterface
    override fun doInsertBook(host: String, book_id: String?, book_source_id: String?, name: String?, author: String?, status: String, category: String, imgUrl: String, last_chapter: String, chapter_count: String, updateTime: String, parameter: String, extra_parameter: String, dex: String) {

        if (name == null || book_id == null || book_source_id == null || author == null)
            return

        handler.post {
            if (insertBook != null) {
                val dex1 = if (TextUtils.isEmpty(dex)) 1 else Integer.parseInt(dex)
                insertBook?.doInsertBook(host, book_id, book_source_id, name, author, status, category, imgUrl, last_chapter, chapter_count, java.lang.Long.valueOf(updateTime)!!, parameter, extra_parameter, dex1)
            }
        }
    }

    @JavascriptInterface
    override fun doDeleteBook(book_id: String?) {
        if (book_id == null)
            return

        handler.post {
            if (deleteBook != null) {
                deleteBook?.doDeleteBook(book_id)
            }
        }
    }

    @JavascriptInterface
    override fun openWebView(url: String?, name: String?) {
        if (url == null || name == null)
            return


        handler.post {
            if (anotherWeb != null) {
                anotherWeb?.doAnotherWeb(url, name)
            }
        }
    }

    @JavascriptInterface
    override fun openWebGame(url: String?, name: String?) {
        if (url == null || name == null)
            return


        handler.post {
            if (webGameClick != null) {
                webGameClick?.openWebGame(url, name)
            }
        }

    }

    @JavascriptInterface
    override fun downloadGame(url: String?, name: String?) {
        if (url == null || name == null)
            return


        handler.post {
            if (gameAppClick != null) {
                gameAppClick?.downloadGame(url, name)
            }
        }

    }

    @JavascriptInterface
    override fun enterApp(name: String?) {
        if (name == null)
            return

        handler.post {
            if (enterApp != null) {
                enterApp?.doEnterApp(name)
            }
        }


    }

    @JavascriptInterface
    override fun openAd(url: String?) {
        if (url == null)
            return

        handler.post {
            if (ad != null) {
                ad?.doOpenAd(url)
            }
        }


    }

    @JavascriptInterface
    override fun enterCover(host: String?, book_id: String?, book_source_id: String?, name: String, author: String, parameter: String, extra_parameter: String) {
        if (host == null || book_id == null || book_source_id == null)
            return

        handler.post {
            if (cover != null) {
                cover?.doCover(host, book_id, book_source_id, name, author, parameter, extra_parameter)
            }
        }


    }

    @JavascriptInterface
    override fun enterRead(host: String, book_id: String?, book_source_id: String, name: String?, author: String?, status: String, category: String, imgUrl: String, last_chapter: String, chapter_count: String, updateTime: String, parameter: String, extra_parameter: String, dex: String) {
        if (book_id == null || name == null || author == null)
            return

        handler.post {
            if (read != null) {
                val dex1 = if (TextUtils.isEmpty(dex)) 1 else Integer.parseInt(dex)
                read?.doRead(host, book_id, book_source_id, name, author, status, category, imgUrl, last_chapter, chapter_count, java.lang.Long.valueOf(updateTime)!!, parameter, extra_parameter, dex1)
            }
        }
    }


    // ========================================================
    // js调用 java 方法 并传参 ; js-->java :tell what to do
    // ======================================================
    //去重书架上的书
    @JavascriptInterface
    override fun uploadBookShelfList(): String {

        val bookShelfList = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBooks()
        val bookIdList = StringBuilder()

        if (bookShelfList != null && bookShelfList.isNotEmpty()) {
            for (i in bookShelfList.indices) {
                val book = bookShelfList[i]
                if (i > 0) bookIdList.append(",")
                bookIdList.append(book.book_id)
            }
        }

        return bookIdList.toString()
    }

    //搜索无结果 点击订阅
    @JavascriptInterface
    override fun showSubBookDialog(word: String) {
        handler.post {
            if (subSearchBook != null) {
                subSearchBook?.showSubSearchBook(word)
            }
        }
    }

    //收集打点信息,用于统计信息，提供给h5打点数据的通道
    @JavascriptInterface
    override fun collectInfo(urlData: String) {
        AppLog.e("searchResult", "collectInfo")
        if (urlData != "") {
            val data = UrlUtils.getDataParams(urlData)
            //截取页面编码
            val pageCode = data["page_code"]
            data.remove("page_code")
            //截取功能编码
            val functionCode = data["func_code"]
            data.remove("func_code")
//            StartLogClickUtil.upLoadEventLog(context, pageCode, functionCode, data)
        }
    }

    @JavascriptInterface
    override fun getH5ViewPagerInfo(x: String, y: String, width: String, height: String) {
        if (this.pagerInfo != null) {
            try {
                this.pagerInfo!!.onH5PagerInfo(java.lang.Float.parseFloat(x), java.lang.Float.parseFloat(y), java.lang.Float.parseFloat(width), java.lang.Float.parseFloat(height))
            } catch (e: Exception) {
                e.printStackTrace()
                AppLog.e("kk", e.toString())
            }

        }
    }

    @JavascriptInterface
    override fun enterCategory(gid: String?, nid: String?,
                               name: String?, lastSort: String?) {
        if (gid == null || nid == null || lastSort == null || name == null)
            return

        handler.post {
            if (mCategory != null) {
                mCategory!!.doCategory(Integer.parseInt(gid),
                        Integer.parseInt(nid), name,
                        Integer.parseInt(lastSort))
            }
        }

    }

    @JavascriptInterface
    override fun showToast(str: String?) {
        if (str == null)
            return
        handler.post {
            if (showToast != null) {
                showToast?.onShowToast(str)
            }
        }
    }

    @JavascriptInterface
    override fun closeWebview() {

        handler.post {
            if (closeWebView != null) {
                closeWebView?.onCloseWebView()
            }
        }
    }

    interface OnInsertBook {
        fun doInsertBook(host: String, book_id: String?, book_source_id: String?,
                         name: String?, author: String?, status: String, category: String,
                         imgUrl: String, last_chapter: String, chapter_count: String,
                         updateTime: Long, parameter: String, extra_parameter: String,
                         dex: Int)
    }

    interface OnDeleteBook {
        fun doDeleteBook(book_id: String?)
    }

    interface OnEnterAppClick {
        fun doEnterApp(name: String?)
    }

    interface OnAnotherWebClick {
        fun doAnotherWeb(url: String?, name: String?)
    }

    interface OnGameAppClick {
        fun downloadGame(url: String?, name: String?)
    }

    interface OnWebGameClick {
        fun openWebGame(url: String?, name: String?)
    }

    interface OnSearchClick {
        fun doSearch(keyWord: String?, search_type: String?, filter_type: String?,
                     filter_word: String, sort_type: String)
    }

    interface OnSearchResultNotify {
        fun onSearchResult(result: Int)
    }

    //搜索优化新增

    interface OnSearchWordClick {
        fun sendSearchWord(searchWord: String, search_type: String)
    }

    fun setSearchWordClicks(searchWordClick: OnSearchWordClick) {
        this.searchWordClick = searchWordClick
    }

    interface onTurnRead {
        fun turnRead(book_id: String, book_source_id: String, host: String, name: String,
                     author: String, parameter: String, extra_parameter: String, update_type: String,
                     last_chapter_name: String, serial_number: Int, img_url: String,
                     update_time: Long, desc: String, label: String, status: String,
                     bookType: String)
    }

    fun setOnTurnRead(turnRead: onTurnRead) {
        this.toRead = turnRead
    }


    //搜索无结果 点击订阅
    interface OnSubSearchBook {
        fun showSubSearchBook(word: String)
    }

    fun setSubSearchBooks(subSearchBook: OnSubSearchBook) {
        this.subSearchBook = subSearchBook
    }


    @JavascriptInterface
    override fun turnToRead(book_id: String, book_source_id: String, host: String, name: String, author: String, parameter: String, extra_parameter: String, update_type: String, last_chapter_name: String, serial_number: String, img_url: String, update_time: String, desc: String, label: String, status: String, bookType: String) {
        if (book_id != "" && book_source_id != "") {

            handler.post {
                if (toRead != null) {
                    toRead!!.turnRead(book_id, book_source_id, host, name, author, parameter, extra_parameter, update_type, last_chapter_name, Integer.valueOf(serial_number)!!, img_url, java.lang.Long.valueOf(update_time)!!, desc, label, status, bookType)
                }
            }
        }
    }

    @JavascriptInterface
    override fun sendSearchWord(searchWord: String, search_type: String) {
        if (searchWord != "") {

            handler.post {
                if (searchWordClick != null) {
                    searchWordClick?.sendSearchWord(searchWord, search_type)
                }
            }
        }
    }

    /**
     * 搜索结果H5回调
     *
     * @param result 1表示有结果，2表示搜索无结果
     */
    @JavascriptInterface
    override fun onSearchResult(result: Int) {
        if (searchResultNotify != null) searchResultNotify!!.onSearchResult(result)
    }

    // ========================================================
    // 预留
    // ======================================================

    interface OnEnterCover {
        fun doCover(host: String?, book_id: String?, book_source_id: String?, name: String, author: String,
                    parameter: String, extra_parameter: String)
    }

    interface OnEnterRead {
        fun doRead(host: String, book_id: String?, book_source_id: String, name: String?, author: String?,
                   status: String, category: String, imgUrl: String, last_chapter: String,
                   chapter_count: String, updateTime: Long, parameter: String, extra_parameter: String,
                   dex: Int)
    }

    interface OnEnterCategory {
        fun doCategory(gid: Int, nid: Int, name: String?,
                       lastSort: Int)
    }


    interface OnOpenAd {
        fun doOpenAd(url: String?)
    }

    interface OnShowToastListener {
        fun onShowToast(str: String?)
    }

    interface OnCloseWebViewListener {
        fun onCloseWebView()

    }

    interface OnH5PagerInfoListener {
        fun onH5PagerInfo(x: Float, y: Float, width: Float, height: Float)
    }


}
