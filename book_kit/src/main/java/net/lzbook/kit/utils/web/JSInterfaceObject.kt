package net.lzbook.kit.utils.web

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.bean.Book
import com.ding.basic.bean.Chapter
import com.ding.basic.bean.RecommendBean
import com.ding.basic.net.api.MicroAPI
import com.ding.basic.util.*
import com.dingyue.statistics.DyStatService
import com.google.gson.Gson
import com.orhanobut.logger.Logger
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.utils.download.CacheManager
import net.lzbook.kit.utils.oneclick.OneClickUtil
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.utils.webview.UrlUtils
import java.io.Serializable

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei911228@gmail.com
 * Date 2018/9/18 17:45
 */
abstract class JSInterfaceObject(var activity: Activity) {

    /***
     * H5调用原生方法：拼接请求链接
     * **/
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

    /***
     * H5调用原生方法：跳转到封面页
     * **/
    @JavascriptInterface
    fun startCoverActivity(data: String?) {
        if (data != null && data.isNotEmpty() && !activity.isFinishing) {
            if (OneClickUtil.isDoubleClick(System.currentTimeMillis())) {
                return
            }

            try {
                val cover = Gson().fromJson(data, JSCover::class.java)

                if (cover?.book_id != null) {
                    val bundle = Bundle()
                    bundle.putString("book_id", cover.book_id)
                    bundle.putString("book_source_id", cover.book_source_id ?: "")
                    bundle.putString("book_chapter_id", cover.book_chapter_id ?: "")

                    RouterUtil.navigation(activity, RouterConfig.COVER_PAGE_ACTIVITY, bundle)
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }

    /***
     * H5调用原生方法：跳转到阅读页
     * **/
    @JavascriptInterface
    fun startReaderActivity(data: String?) {
        if (data != null && data.isNotEmpty() && !activity.isFinishing) {
            if (OneClickUtil.isDoubleClick(System.currentTimeMillis())) {
                return
            }

            try {
                val book = loadBook(data)
                val bundle = Bundle()
                bundle.putInt("sequence", book.sequence)
                bundle.putInt("offset", book.offset)
                bundle.putSerializable("book", book)
                val flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                RouterUtil.navigation(activity, RouterConfig.READER_ACTIVITY, bundle, flags)
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }

    /***
     * H5调用原生方法：加入书架
     * **/
    @JavascriptInterface
    fun insertBookShelf(data: String?): Boolean {
        if (data != null && data.isNotEmpty() && !activity.isFinishing) {
            try {
                val book = loadBook(data)

                val succeed = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).insertBook(book)

                return if (succeed > 0) {
                    ToastUtil.showToastMessage("成功添加到书架！")
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

    /***
     * H5调用原生方法：移除书架
     * **/
    @JavascriptInterface
    fun removeBookShelf(data: String?): Boolean {
        if (data != null && data.isNotEmpty() && !activity.isFinishing) {
            return try {
                val delete = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).deleteBook(data)

                if (delete) {
                    CacheManager.stop(data)
                    CacheManager.resetTask(data)
                    ToastUtil.showToastMessage("成功从书架中移除！")
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

    /***
     * H5调用原生方法：获取书架书籍列表
     * **/
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

    /***
     * H5调用原生方法：统计打点
     * **/
    @JavascriptInterface
    fun statisticsWebInformation(data: String?) {
        if (data != null && !data.isNullOrEmpty()) {
            val parameters = loadMassageParameters(data)
            //截取页面编码
            val pageCode = parameters["page_code"]
            parameters.remove("page_code")
            //截取功能编码
            val functionCode = parameters["func_code"]
            parameters.remove("func_code")

            if (!pageCode.isNullOrBlank() && !functionCode.isNullOrBlank()) DyStatService.onEvent(pageCode!!, functionCode!!, parameters)
        }
    }

    /***
     * H5调用原生方法：拼接请求链接(微服务接口)
     * **/
    @JavascriptInterface
    fun buildMicroRequestUrl(url: String): String {
        val result = buildMicroRequest(url, true)
        Logger.e("拼接精选页面链接: $result")
        return result
    }

    /***
     * H5调用原生方法：鉴权失败请求重试接口(微服务接口)
     * **/
    @JavascriptInterface
    fun authAccessMicroRequest(url: String): String {
        if (MicroAPI.expire - System.currentTimeMillis() <= 5000) {
            val result = MicroAPI.requestAuthAccessSync()

            Logger.e("WebView请求鉴权接口结果: " + result + " : " + MicroAPI.publicKey + " : " + MicroAPI.privateKey)
        }
        val result = buildMicroRequest(url, true)
        Logger.e("鉴权异常，生成的链接为: $result")
        return result
    }

    /**
     * H5调用,搜索无结果回调
     */
    @JavascriptInterface
    open fun onSearchNoResult(keyword: String?) {

    }

    /**
     * 搜索无结果页推荐词点击事件
     */
    @JavascriptInterface
    abstract fun startSearchActivity(data: String?)

    /**
     * WebView二级页面
     * 搜索作者主页
     */
    @JavascriptInterface
    abstract fun startTabulationActivity(data: String?)

    /**
     * 获取书籍对象
     */
    protected fun loadBook(data: String): Book {

        val recommend = Gson().fromJson(data, RecommendBean::class.java)

        val book = Book()

        book.book_id = recommend.bookId
        book.book_source_id = if (recommend.id.isEmpty()) recommend.bookSourceId else recommend.id
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
        book.img_url = recommend.bookImgUrl
        book.sub_genre = recommend.subGenre
        book.book_type = recommend.bookType
        book.word_count = recommend.wordCountDescp

        val chapter = Chapter()
        chapter.name = recommend.lastChapterName
        chapter.update_time = recommend.updateTime
        chapter.serial_number = recommend.lastSerialNumber.toInt()

        book.last_chapter = chapter

        return book
    }

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