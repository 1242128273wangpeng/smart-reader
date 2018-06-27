package com.dingyue.contract.util

import android.app.Activity
import android.os.Bundle
import com.ding.basic.bean.Book
import com.ding.basic.bean.RecommendBean
import com.ding.basic.repository.RequestRepositoryFactory
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.github.lzyzsd.jsbridge.BridgeHandler
import com.github.lzyzsd.jsbridge.CallBackFunction
import com.google.gson.Gson
import com.orhanobut.logger.Logger
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.request.UrlUtils
import java.io.Serializable

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
        Logger.e("BuildRequestUrl")
        if (data != null && data.isNotEmpty()) {

            var url = data

            val array = url.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            if (array.size == 2) {
                url = array[0]
            }
            url = UrlUtils.buildWebUrl(url, UrlUtils.getUrlParams(array[1]))

            Logger.e("BuildRequestUrl结果: $url")

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
class InsertBookShelfHandler(activity: Activity) : BridgeHandler {

    override fun handler(data: String?, function: CallBackFunction?) {
        if (data != null && data.isNotEmpty()) {
            var recommend = Gson().fromJson(data, RecommendBean::class.java)
            Logger.e("Recommend: " + recommend.toString())

            function?.onCallBack("false")
        }
    }
}

/***
 * 移除书架
 * action : removeBookShelf
 * **/
class RemoveBookShelfHandler(activity: Activity) : BridgeHandler {

    override fun handler(data: String?, function: CallBackFunction?) {
        if (data != null && data.isNotEmpty()) {

            function?.onCallBack("true")
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
class StartSearchHandler : BridgeHandler {

    override fun handler(data: String?, function: CallBackFunction?) {
//            try {
//                val data = HashMap<String, String>()
//                data["keyword"] = keyWord
//                data["type"] = "0"//0 代表从分类过来
//                StartLogClickUtil.upLoadEventLog(this@HomeActivity, StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.SYSTEM_SEARCHRESULT, data)
//
//                val intent = Intent()
//                intent.setClass(this@HomeActivity, SearchBookActivity::class.java)
//                intent.putExtra("word", keyWord)
//                intent.putExtra("search_type", search_type)
//                intent.putExtra("filter_type", filter_type)
//                intent.putExtra("filter_word", filter_word)
//                intent.putExtra("sort_type", sort_type)
//                intent.putExtra("from_class", "fromClass")
//                startActivity(intent)
//            } catch (e: Exception) {
//                AppLog.e(TAG, "Search failed")
//                e.printStackTrace()
//            }
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