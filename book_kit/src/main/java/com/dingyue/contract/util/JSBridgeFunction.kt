package com.dingyue.contract.util

import android.app.Activity
import android.os.Bundle
import com.ding.basic.bean.Book
import com.ding.basic.bean.Chapter
import com.ding.basic.bean.RecommendBean
import com.ding.basic.repository.RequestRepositoryFactory
import com.dingyue.contract.CommonContract
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.github.lzyzsd.jsbridge.BridgeHandler
import com.github.lzyzsd.jsbridge.CallBackFunction
import com.google.gson.Gson
import com.orhanobut.logger.Logger
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.request.UrlUtils
import java.io.Serializable
import java.util.HashMap

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/6/26 16:27
 */

/***
 * 拼接Url
 * action : buildRequestUrl
 * **/
class BuildRequestUrlHandler : BridgeHandler {

    override fun handler(data: String?, function: CallBackFunction?) {
        if (data != null && data.isNotEmpty()) {

            var url = data

            var parameters: Map<String, String>? = null

            val array = url.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            url = array[0]

            if (array.size == 2) {
                parameters = UrlUtils.getUrlParams(array[1])
            } else if (array.size == 1) {
                parameters = HashMap()
            }

            url = UrlUtils.buildWebUrl(url, parameters)

            Logger.e("BuildRequestUrl: $data")

            function?.onCallBack(url)
        }
    }
}

/***
 * 跳转书籍列表页
 * action : startTabulationActivity
 * **/
class StartTabulationHandler(val activity: Activity) : BridgeHandler {

    override fun handler(data: String?, function: CallBackFunction?) {
        if (data != null && data.isNotEmpty()) {
            if (!activity.isFinishing) {
                try {
                    val jsUrl = Gson().fromJson(data, JSUrl::class.java)

                    if (jsUrl?.url != null && jsUrl.title != null) {
                        val bundle = Bundle()
                        bundle.putString("url", jsUrl.url)
                        bundle.putString("title", jsUrl.title)

                        RouterUtil.navigation(activity, RouterConfig.TABULATION_ACTIVITY, bundle)
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
        }
    }
}

/***
 * 添加书架
 * action : insertBookShelf
 * **/
class InsertBookShelfHandler(val activity: Activity) : BridgeHandler {

    override fun handler(data: String?, function: CallBackFunction?) {
        if (data != null && data.isNotEmpty()) {
            val recommend = Gson().fromJson(data, RecommendBean::class.java)

            val book = Book()
            book.book_id = recommend.bookId
            book.book_source_id = recommend.id
            book.book_chapter_id = recommend.bookChapterId
            book.name = recommend.bookName
            book.author = recommend.authorName
            book.desc = recommend.description
            book.label = recommend.label
            book.genre = recommend.genre
            book.sub_genre = recommend.subGenre
            book.img_url = recommend.sourceImageUrl
            book.status = recommend.serialStatus
            book.host = recommend.host
            book.book_type = recommend.bookType
            book.word_count = recommend.wordCountDescp
            book.score = recommend.score
            book.uv = recommend.uv

            val chapter = Chapter()
            chapter.name = recommend.lastChapterName
            chapter.update_time = recommend.updateTime

            book.last_chapter = chapter

            val succeed = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).insertBook(book)

            if (succeed > 0) {
                activity.applicationContext.showToastMessage("成功添加到书架！")
                function?.onCallBack("true")
            } else {
                function?.onCallBack("false")
            }
        }
    }
}

/***
 * 移除书架
 * action : removeBookShelf
 * **/
class RemoveBookShelfHandler(val activity: Activity) : BridgeHandler {

    override fun handler(data: String?, function: CallBackFunction?) {
        if (data != null && data.isNotEmpty()) {

            val result = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).deleteBook(data)
            if (result) {
                CacheManager.stop(data)
                CacheManager.resetTask(data)
                activity.applicationContext.showToastMessage("成功从书架中移除！")
                function?.onCallBack("true")
            } else {
                function?.onCallBack("false")
            }
        }
    }
}

/***
 * 获取书架信息
 * action : loadBookShelfInfo
 * **/
class LoadBookShelfInfo : BridgeHandler {

    override fun handler(data: String?, function: CallBackFunction?) {
        if (data != null && data.isNotEmpty()) {
            val books = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBooks()
            val stringBuilder = StringBuilder()

            if (books != null && books.isNotEmpty()) {
                for (i in books.indices) {
                    val book = books[i]
                    if (i > 0) {
                        stringBuilder.append(",")
                    }
                    stringBuilder.append(book.book_id)
                }
            }

            Logger.e("获取书架列表: " + stringBuilder.toString())

            function?.onCallBack(stringBuilder.toString())
        }
    }
}

/***
 * 跳转进入封面
 * action : startCoverActivity
 * **/
class StartCoverHandler(val activity: Activity) : BridgeHandler {

    override fun handler(data: String?, function: CallBackFunction?) {
        if (data != null && data.isNotEmpty()) {
            if (!activity.isFinishing) {
                try {
                    if(CommonContract.isDoubleClick(System.currentTimeMillis())){
                        return
                    }
                    val jsCover = Gson().fromJson(data, JSCover::class.java)

                    if (jsCover?.book_id != null && jsCover.book_source_id != null && jsCover.book_chapter_id != null) {
                        val bundle = Bundle()
                        bundle.putString("book_id", jsCover.book_id)
                        bundle.putString("book_source_id", jsCover.book_source_id)
                        bundle.putString("book_chapter_id", jsCover.book_chapter_id)

                        RouterUtil.navigation(activity, RouterConfig.COVER_PAGE_ACTIVITY, bundle)
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
        }
    }
}

/***
 * 跳转进入搜索
 * action : startSearchActivity
 * **/
class StartSearchHandler(val activity: Activity) : BridgeHandler {

    override fun handler(data: String?, function: CallBackFunction?) {
        if (data != null && data.isNotEmpty()) {
            try {
                if(CommonContract.isDoubleClick(System.currentTimeMillis())){
                    return
                }
                val bundle = Bundle()
                bundle.putString("word", data)
                bundle.putString("from_class", "search")
                bundle.putString("search_type", "0")
                bundle.putString("filter_type", "0")
                bundle.putString("filter_word", "ALL")
                bundle.putString("sort_type", "0")
                RouterUtil.navigation(activity, RouterConfig.SEARCH_BOOK_ACTIVITY, bundle)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}


class JSCover : Serializable {
    var book_id: String? = null

    var book_source_id: String? = null

    var book_chapter_id: String? = null
}

class JSUrl : Serializable {
    var url: String? = null

    var title: String? = null
}