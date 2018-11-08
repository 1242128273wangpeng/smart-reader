package com.ding.basic.net

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.text.TextUtils
import com.ding.basic.config.ParameterConfig
import com.ding.basic.net.api.ContentAPI
import com.ding.basic.net.api.MicroAPI
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
@SuppressLint("StaticFieldLeak")
object Config {

    const val Develop: Boolean = true

    /***
     * WebView地址
     * **/
    private var webViewHost: String = ""

    /***
     * 智能API接口
     * **/
    private var requestAPIHost: String = ""

    /***
     * CDN
     * **/
    var cdnHost: String = ""

    /***
     * user tag 接口
     * **/
    private var userTagHost: String = "https://znapi-bigdata.zhuishuwang.com"


    /***
     * 设置页福利中心地址
     * **/
    const val WelfareHost: String = "https://st.quanbennovel.com/static/welfareCenter/welfareCenter.html"

    /***
     * 鉴权临时秘钥
     * **/
    private var accessKey: String = "wangpeng12345678"

    /***
     * 请求公共参数
     * **/
    private var requestParameters: HashMap<String, String> = HashMap()


    var SDCARD_PATH = Environment.getExternalStorageDirectory().absolutePath


    private var context: Context? = null


    fun beginInit(context: Context) {
        Config.context = context

        webViewHost = ReplaceConstants.getReplaceConstants().BOOK_WEBVIEW_HOST
        requestAPIHost = ReplaceConstants.getReplaceConstants().BOOK_NOVEL_DEPLOY_HOST

        MicroAPI.microHost = ReplaceConstants.getReplaceConstants().MICRO_API_HOST
        ContentAPI.contentHost = "http://20.20.200.206:8080"
//        ContentAPI.contentHost = ReplaceConstants.getReplaceConstants().CONTENT_API_HOST

        MicroAPI.initMicroService()
        ContentAPI.initContentService()

        cdnHost = ReplaceConstants.getReplaceConstants().CDN_HOST
    }

    fun getContext(): Context? {
        return context
    }

    fun insertWebViewHost(webViewHost: String) {
        if (!TextUtils.isEmpty(webViewHost)) {
            Config.webViewHost = webViewHost
        }
    }

    fun loadWebViewHost(): String {
        return webViewHost
    }

    fun insertRequestAPIHost(requestAPIHost: String) {
        if (!TextUtils.isEmpty(requestAPIHost)) {
            Config.requestAPIHost = requestAPIHost
        }
    }

    fun loadRequestAPIHost(): String {
        return requestAPIHost
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

    fun insertRequestParameters(@NotNull parameters: HashMap<String, String>) {
        requestParameters.putAll(parameters)
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

    fun buildRequestUrl(url: String?): String? {
        if (url == null) {
            return null
        }

        val parameters = HashMap<String, String>()
        parameters["os"] = loadRequestParameter("os")
        parameters["udid"] = loadRequestParameter("udid")
        parameters["version"] = loadRequestParameter("version")
        parameters["channelId"] = loadRequestParameter("channelId")
        parameters["packageName"] = loadRequestParameter("packageName")

        parameters["cityCode"] = ParameterConfig.cityCode
        parameters["latitude"] = ParameterConfig.latitude.toString()
        parameters["longitude"] = ParameterConfig.longitude.toString()

        return URLBuilder.buildUrl(requestAPIHost, url, parameters)
    }

    fun insertUserTagHost(userTagHost: String) {
        if (!TextUtils.isEmpty(userTagHost)) {
            Config.userTagHost = userTagHost
        }
    }

    fun loadUserTagHost(): String {
        return userTagHost
    }

    fun loadAccessKey(): String {
        return accessKey
    }
}