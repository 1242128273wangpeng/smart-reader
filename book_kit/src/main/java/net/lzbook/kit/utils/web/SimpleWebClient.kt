package net.lzbook.kit.utils.web

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import com.tencent.smtt.export.external.interfaces.SslError
import com.tencent.smtt.export.external.interfaces.SslErrorHandler
import com.tencent.smtt.sdk.WebSettings
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient


class SimpleWebClient(var context: Context?, internal var webView: WebView?) : WebViewClient() {

    private var webSettings: WebSettings? = null
    private var firstLoad = true

    /***
     * WebView加载开始监听
     * **/
    private var loadingWebViewStart: ((url: String?) -> Unit)? = null

    /***
     * WebView加载完成监听
     * **/
    private var loadingWebViewFinish: (() -> Unit)? = null

    /***
     * 加载WebView异常监听
     * **/
    private var loadingWebViewError: (() -> Unit)? = null

    /***
     * WebView每次加载开始监听
     * **/
    private var loadingEveryWebViewStart: ((url: String?) -> Unit)? = null

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        if (firstLoad) {
            loadingWebViewStart?.invoke(url)
            firstLoad = false
        } else {
            loadingEveryWebViewStart?.invoke(url)
        }
        super.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        loadingWebViewFinish?.invoke()
        super.onPageFinished(view, url)
    }

    override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
        loadingWebViewError?.invoke()
        super.onReceivedError(view, errorCode, description, failingUrl)
    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        handler.proceed()
    }

    /***
     * 设置加载WebView开始监听
     * **/
    fun setLoadingWebViewStart(loadingWebViewStart: (url: String?) -> Unit) {
        this.loadingWebViewStart = loadingWebViewStart
    }

    /***
     * 设置每次加载WebView开始监听
     * **/
    fun setLoadingEveryWebViewStart(loadingEveryWebViewStart: (url: String?) -> Unit) {
        this.loadingEveryWebViewStart = loadingEveryWebViewStart
    }

    /***
     * 设置WebView加载完成监听
     * **/
    fun setLoadingWebViewFinish(loadingWebViewFinish: () -> Unit) {
        this.loadingWebViewFinish = loadingWebViewFinish
    }

    /***
     * 设置加载WebView异常监听
     * **/
    fun setLoadingWebViewError(loadingWebViewError: () -> Unit) {
        this.loadingWebViewError = loadingWebViewError
    }

    /***
     * 初始化WebView设置
     * **/
    @SuppressLint("SetJavaScriptEnabled")
    fun initWebViewSetting() {
        try {
            webSettings = webView?.settings
            webSettings?.javaScriptEnabled = true
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }
}