package net.lzbook.kit

import android.app.Activity
import android.os.Bundle
import android.webkit.JavascriptInterface
import com.ding.basic.bean.Book
import com.ding.basic.bean.Chapter
import com.ding.basic.bean.RecommendBean
import com.ding.basic.repository.RequestRepositoryFactory
import com.dingyue.contract.CommonContract
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dingyue.contract.util.showToastMessage
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
 * Mail crazylei911228@gmail.com
 * Date 2018/9/18 17:45
 */
abstract class WebViewInterfaceObject(var activity: Activity) {

    @JavascriptInterface
    fun buildRequestUrl(data: String?): String? {
        if (data != null && data.isNotEmpty() && !activity.isFinishing) {
            var url = data
            var parameters: Map<String, String>? = null

            val array = url.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            url = array[0]

            if (array.size == 2) {
                parameters = UrlUtils.getUrlParams(array[1])
            } else if (array.size == 1) {
                parameters = HashMap()
            }

            return UrlUtils.buildWebUrl(url, parameters)
        } else {
            return null
        }
    }

    @JavascriptInterface
    fun startCoverActivity(data: String?) {
        if (data != null && data.isNotEmpty() && !activity.isFinishing) {
            if (CommonContract.isDoubleClick(System.currentTimeMillis())) {
                return
            }

            try {
                val cover = Gson().fromJson(data, JSCover::class.java)

                if (cover?.book_id != null && cover.book_source_id != null && cover.book_chapter_id != null) {
                    val bundle = Bundle()
                    bundle.putString("book_id", cover.book_id)
                    bundle.putString("book_source_id", cover.book_source_id)
                    bundle.putString("book_chapter_id", cover.book_chapter_id)

                    RouterUtil.navigation(activity, RouterConfig.COVER_PAGE_ACTIVITY, bundle)
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }

    @JavascriptInterface
    fun insertBookShelf(data: String?): Boolean {
        if (data != null && data.isNotEmpty() && !activity.isFinishing) {
            try {
                val recommend = Gson().fromJson(data, RecommendBean::class.java)

                val book = Book()
                book.book_id = recommend.bookId
                book.book_source_id = recommend.id
                book.book_chapter_id = recommend.bookChapterId
                book.uv = recommend.uv
                book.name = recommend.bookName
                book.desc = recommend.description
                book.host = recommend.host
                book.label = recommend.label
                book.genre = recommend.genre
                book.score = recommend.score
                book.author = recommend.authorName
                book.status = recommend.serialStatus
                book.img_url = recommend.sourceImageUrl
                book.sub_genre = recommend.subGenre
                book.book_type = recommend.bookType
                book.word_count = recommend.wordCountDescp

                val chapter = Chapter()
                chapter.name = recommend.lastChapterName
                chapter.update_time = recommend.updateTime

                book.last_chapter = chapter

                val succeed = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).insertBook(book)

                return if (succeed > 0) {
                    activity.applicationContext.showToastMessage("成功添加到书架！")
                    true
                } else {
                    false
                }

            } catch (exception: Exception) {
                exception.printStackTrace()
                return false
            }
        } else {
            return false
        }
    }

    @JavascriptInterface
    fun removeBookShelf(data: String?): Boolean {
        if (data != null && data.isNotEmpty() && !activity.isFinishing) {
            return try {
                val delete = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).deleteBook(data)

                if (delete) {
                    CacheManager.stop(data)
                    CacheManager.resetTask(data)
                    activity.applicationContext.showToastMessage("成功从书架中移除！")
                    true
                } else {
                    false
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
                false
            }
        } else {
            return false
        }
    }

    @JavascriptInterface
    fun loadBookShelfInfo(): String {
        val stringBuilder = StringBuilder()

        val books = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBooks()

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
        return stringBuilder.toString()
    }

    @JavascriptInterface
    abstract fun startSearchActivity(data: String?)

    @JavascriptInterface
    abstract fun startTabulationActivity(data: String?)

    inner class JSCover : Serializable {
        var book_id: String? = null

        var book_source_id: String? = null

        var book_chapter_id: String? = null
    }

    inner class JSRedirect : Serializable {
        var url: String? = null

        var title: String? = null

        var from: String? = null
    }

    inner class JSSearch : Serializable {
        var word: String? = null

        var type: String? = null
    }
}