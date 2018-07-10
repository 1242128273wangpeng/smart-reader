package com.ding.basic

import android.annotation.SuppressLint
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
@SuppressLint("StaticFieldLeak")
object Config {

    const val Develop: Boolean = true

    //WebView地址
    private var webViewHost: String? = ""
    //智能API接口
    private var requestAPIHost: String? = ""
    //微服务API接口
    private var microAPIHost: String = "https://unionapi.bookapi.cn"
    //微服务内容接口
    private var contentAPIHost: String = "https://unioncontent.bookapi.cn"

    //设置页福利中心地址
    const val WelfareHost: String = "https://st.quanbennovel.com/static/welfareCenter/welfareCenter.html"

    /***
     * 鉴权临时秘钥
     * **/
    private var accessKey: String = "wangpeng12345678"

    private var privateKey: String = ""

    private var bookContent: String? = null

    private var requestParameters: HashMap<String, String> = HashMap()


    var SDCARD_PATH = Environment.getExternalStorageDirectory().absolutePath


    const val DRAWABLE = 1
    const val COLOR = 2
    const val STYLE = 3

    private var context: Context? = null

    private var publicKey: String = ""

    fun beginInit(context: Context) {
        Config.context = context

//        webViewHost = "http://8068.zn.bookapi.cn"
//        requestAPIHost = "http://8068.zn.bookapi.cn"

        webViewHost = ReplaceConstants.getReplaceConstants().BOOK_WEBVIEW_HOST
        requestAPIHost = ReplaceConstants.getReplaceConstants().BOOK_NOVEL_DEPLOY_HOST
    }

    fun getContext(): Context? {
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

    fun insertRequestParameters(@NotNull parameters: HashMap<String, String>) {
        requestParameters.putAll(parameters)
    }

    fun loadRequestParameters(): HashMap<String, String> {
        return requestParameters
    }


    fun initializeLogger() {

        val formatStrategy = PrettyFormatStrategy.newBuilder()
                .tag("DingYue").methodCount(0).showThreadInfo(true).build()

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


    fun insertMicroAPIHost(microAPIHost: String) {
        if (!TextUtils.isEmpty(microAPIHost)) {
            Config.microAPIHost = microAPIHost
        }
    }

    fun loadMicroAPIHost(): String {
        return microAPIHost
    }

    fun insertContentAPIHost(contentAPIHost: String) {
        if (!TextUtils.isEmpty(contentAPIHost)) {
            Config.contentAPIHost = contentAPIHost
        }
    }

    fun loadContentAPIHost(): String {
        return contentAPIHost
    }

    fun insertAccessKey(accessKey: String) {
        if (!TextUtils.isEmpty(accessKey)) {
            Config.accessKey = accessKey
        }
    }

    fun loadAccessKey(): String {
        return accessKey
    }

    fun insertPublicKey(publicKey: String) {
        this.publicKey = publicKey

        val sharedPreferences = context?.getSharedPreferences("Basic_Preference", Context.MODE_PRIVATE)
        sharedPreferences?.edit()?.putString("Access_Public_Key", publicKey)?.apply()
    }

    fun loadPublicKey(): String {
        if (publicKey.isEmpty()) {
            val sharedPreferences = context?.getSharedPreferences("Basic_Preference", Context.MODE_PRIVATE)
            if (sharedPreferences != null) {
                publicKey = sharedPreferences.getString("Access_Public_Key", "")
            }
        }

        return publicKey
    }

    fun insertPrivateKey(privateKey: String) {
        this.privateKey = privateKey

        val sharedPreferences = context?.getSharedPreferences("Basic_Preference", Context.MODE_PRIVATE)
        sharedPreferences?.edit()?.putString("Access_Private_Key", privateKey)?.apply()
    }

    fun loadPrivateKey(): String {
        if (privateKey.isEmpty()) {
            val sharedPreferences = context?.getSharedPreferences("Basic_Preference", Context.MODE_PRIVATE)
            if (sharedPreferences != null) {
                privateKey = sharedPreferences.getString("Access_Private_Key", "")
            }
        }

        return privateKey
    }
}