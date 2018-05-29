package com.ding.basic

import android.content.Context
import android.os.Environment
import android.text.TextUtils
import com.ding.basic.util.ReplaceConstants
import com.ding.basic.util.URLBuilder
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import org.jetbrains.annotations.NotNull

/**
 * Created on 2018/3/13.
 * Created by crazylei.
 */
object Config {

    const val Develop: Boolean = true

    private var requestParameters: HashMap<String, String> = HashMap()


    private var webViewHost: String? = ""
    private var requestAPIHost: String? = ""

    private var bookContent: String? = null


    var SDCARD_PATH = Environment.getExternalStorageDirectory().absolutePath


    const val DRAWABLE = 1
    const val COLOR = 2
    const val STYLE = 3

    private var context: Context? = null

    fun beginInit(context: Context){
        Config.context = context

//        webViewHost = "http://prod2.zn.bookapi.cn"
//        requestAPIHost = "http://prod2.zn.bookapi.cn"


        webViewHost = ReplaceConstants.getReplaceConstants().BOOK_WEBVIEW_HOST
        requestAPIHost = ReplaceConstants.getReplaceConstants().BOOK_NOVEL_DEPLOY_HOST
    }

    fun getContext(): Context?{
        return Config.context
    }


    fun insertWebViewHost(webViewHost: String) {
        if (!TextUtils.isEmpty(webViewHost)) {
            Config.webViewHost = webViewHost
        }
    }

    fun loadWebViewHost(): String {
        return webViewHost!!
    }

    fun insertRequestAPIHost(requestAPIHost: String) {
        if (!TextUtils.isEmpty(requestAPIHost)) {
            Config.requestAPIHost = requestAPIHost
        }
    }

    fun loadRequestAPIHost(): String {
        return requestAPIHost!!
    }

    fun insertBookContent(bookContent: String) {
        Config.bookContent = bookContent
    }

    fun loadBookContent(): String? {
        return bookContent
    }

    fun insertRequestParameter(@NotNull key: String, @NotNull value: String) {
        requestParameters[key] = value
    }

    fun loadRequestParameter(@NotNull key: String): String {
        return if (requestParameters.containsKey(key)) {
            requestParameters[key].toString()
        } else {
            ""
        }
    }

    fun insertRequestParameters(@NotNull parameters:HashMap<String, String>) {
        requestParameters.putAll(parameters)
    }

    fun loadRequestParameters(): HashMap<String, String> {
        return requestParameters
    }

    fun initializeLogger() {

        val formatStrategy = PrettyFormatStrategy.newBuilder()
                .tag("DingYue").methodCount(0).showThreadInfo(false).build()

        Logger.addLogAdapter(object : AndroidLogAdapter(formatStrategy) {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return Develop
            }
        })
    }

    fun buildUrl(request: String?, parameters: MutableMap<String, String>): String? {

        if (request == null) {
            return null
        }

        return URLBuilder.buildUrl(requestAPIHost, request, parameters)

    }


}