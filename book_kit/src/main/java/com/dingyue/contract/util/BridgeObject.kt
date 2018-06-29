package com.dingyue.contract.util

import android.app.Activity
import android.os.Bundle
import android.webkit.JavascriptInterface
import com.ding.basic.bean.Book
import com.ding.basic.bean.Chapter
import com.ding.basic.bean.RecommendBean
import com.ding.basic.repository.RequestRepositoryFactory
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.google.gson.Gson

import com.orhanobut.logger.Logger
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.request.UrlUtils

import wendu.dsbridge.CompletionHandler
import java.io.Serializable
import java.util.HashMap

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/6/28 16:37
 */
class BridgeObject(var activity: Activity) {

    @JavascriptInterface
    fun buildRequestUrl(data: Any?, completionHandler: CompletionHandler<String>) {
        if (data != null) {
            var url = data as String
            var parameters: Map<String, String>? = null

            val array = url.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            url = array[0]

            if (array.size == 2) {
                parameters = UrlUtils.getUrlParams(array[1])
            } else if (array.size == 1) {
                parameters = HashMap()
            }

            url = UrlUtils.buildWebUrl(url, parameters)

            completionHandler.complete(url)
        } else {
            completionHandler.complete("Android端处理异常！")
        }
    }

    @JavascriptInterface
    fun startCoverActivity(data: Any?, completionHandler: CompletionHandler<String>) {
        if (data != null) {
            if (!activity.isFinishing) {
                try {
                    val result = data as String

                    val jsCover = Gson().fromJson(result, JSCover::class.java)

                    if (jsCover?.book_id != null && jsCover.book_source_id != null && jsCover.book_chapter_id != null) {
                        val bundle = Bundle()
                        bundle.putString("book_id", jsCover.book_id)
                        bundle.putString("book_source_id", jsCover.book_source_id)
                        bundle.putString("book_chapter_id", jsCover.book_chapter_id)

                        RouterUtil.navigation(activity, RouterConfig.COVER_PAGE_ACTIVITY, bundle)

                        completionHandler.complete("跳转封面页正常！")
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()

                    completionHandler.complete("跳转封面页异常！")
                }
            }
        } else {
            completionHandler.complete("Android端处理异常！")
        }
    }

    @JavascriptInterface
    fun startTabulationActivity(data: Any?, completionHandler: CompletionHandler<String>) {
        if (data != null) {
            if (!activity.isFinishing) {
                try {
                    val result = data as String

                    val jsUrl = Gson().fromJson(result, JSUrl::class.java)

                    if (jsUrl?.url != null && jsUrl.title != null) {
                        val bundle = Bundle()
                        bundle.putString("url", jsUrl.url)
                        bundle.putString("title", jsUrl.title)

                        RouterUtil.navigation(activity, RouterConfig.TABULATION_ACTIVITY, bundle)

                        completionHandler.complete("跳转列表页正常！")
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()

                    completionHandler.complete("跳转列表页异常！")
                }
            }
        } else {
            completionHandler.complete("Android端处理异常！")
        }
    }

    @JavascriptInterface
    fun insertBookShelf(data: Any?, completionHandler: CompletionHandler<Boolean>) {
        if (data != null) {
            try {
                val result = data as String
                val recommend = Gson().fromJson(result, RecommendBean::class.java)

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

                book.last_chapter = chapter

                val succeed = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).insertBook(book)

                if (succeed > 0) {
                    activity.applicationContext.showToastMessage("成功添加到书架！")
                    completionHandler.complete(true)
                } else {
                    completionHandler.complete(false)
                }

            } catch (exception: Exception) {
                exception.printStackTrace()

                completionHandler.complete(false)
            }
        } else {
            completionHandler.complete(false)
        }
    }

    @JavascriptInterface
    fun removeBookShelf(data: Any?, completionHandler: CompletionHandler<Boolean>) {
        if (data != null) {
            try {
                val result = data as String

                val delete = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).deleteBook(data)

                if (delete) {
                    CacheManager.stop(data)
                    CacheManager.resetTask(data)
                    activity.applicationContext.showToastMessage("成功从书架中移除！")
                    completionHandler.complete(true)
                } else {
                    completionHandler.complete(false)
                }
            } catch (exception: Exception) {
                exception.printStackTrace()

                completionHandler.complete(false)
            }
        } else {
            completionHandler.complete(false)
        }
    }

    @JavascriptInterface
    fun loadBookShelfInfo(data: Any?, completionHandler: CompletionHandler<String>) {
        if (data != null) {
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

            completionHandler.complete(stringBuilder.toString())
        } else {
            completionHandler.complete("Android端处理异常！")
        }
    }

    inner class JSCover : Serializable {
        var book_id: String? = null

        var book_source_id: String? = null

        var book_chapter_id: String? = null
    }

    inner class JSUrl : Serializable {
        var url: String? = null

        var title: String? = null
    }
}