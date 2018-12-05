package net.lzbook.kit.utils.web

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.View
import android.webkit.*
import android.webkit.WebSettings.LayoutAlgorithm
import android.webkit.WebSettings.RenderPriority
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.orhanobut.logger.Logger
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.toast.ToastUtil

class CustomWebClient(var context: Context?, internal var webView: WebView?) : WebViewClient() {

    private var loadingStartCount = 0
    private var loadingFinishCount = 0

    private var loadingErrorCount = 0

    private var webSettings: WebSettings? = null

    private var webResourceCache = WebResourceCache.loadWebResourceCache()

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

    init {
        this.webView?.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                loadingWebViewFinish()
            }
        }
    }

    companion object {
        private val staticResourceRule = SPUtils.getOnlineConfigSharedString(SPKey.DY_STATIC_RESOURCE_RULE)
        val interceptHostList = staticResourceRule.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    }

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        if (url.startsWith("weixin://wap/pay?")) {
            return try {
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.data = Uri.parse(url)
                context?.startActivity(intent)
                true
            } catch (exception: Exception) {
                if (exception is ActivityNotFoundException) {
                    ToastUtil.showToastMessage("未找到微信客户端，请先安装微信！")
                }
                exception.printStackTrace()
                true
            }
        } else if (url.startsWith("alipays://platformapi/startApp?")) {
            return try {
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.data = Uri.parse(url)
                context?.startActivity(intent)
                true
            } catch (exception: Exception) {
                if (exception is ActivityNotFoundException) {
                    ToastUtil.showToastMessage("未找到支付宝客户端，请先安装支付宝！")
                }
                exception.printStackTrace()
                true
            }
        }
        return super.shouldOverrideUrlLoading(view, url)
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        loadingStartCount++

        if (loadingWebViewStart != null && loadingStartCount == 1) {
            loadingWebViewStart?.invoke(url)
        }

        if (webView != null && loadingStartCount == 1) {
            webView?.visibility = View.GONE
        } else if (loadingErrorCount != 0 && loadingWebViewError != null) {
            loadingWebViewError()
        }
        if(loadingEveryWebViewStart != null){
            loadingEveryWebViewStart?.invoke(url)
        }
        super.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        loadingFinishCount++
        if (loadingErrorCount == 0 && loadingWebViewFinish != null) {
           loadingWebViewFinish()
        } else if (loadingErrorCount != 0 && loadingWebViewError != null) {
            loadingWebViewError()
        }
        super.onPageFinished(view, url)
    }

    override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
        loadingErrorCount = errorCode
        loadingWebViewError()
        super.onReceivedError(view, errorCode, description, failingUrl)
    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        handler.proceed()
    }

//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
//        return interceptWebViewRequest(view, request)
//    }

    /***
     * WebView加载进度
     * **/
    private fun loadingWebViewFinish() {
        if (null != webView) {
            if (webView?.progress ?: 0 > 60) {
                loadingWebViewFinish?.invoke()
            }

            webView?.visibility = View.VISIBLE
        }

        webSettings?.blockNetworkImage = false
    }

    /***
     * WebView加载失败
     * **/
    private fun loadingWebViewError() {
        if (webView != null) {
            webView?.clearView()
            webView?.stopLoading()
            webView?.visibility = View.GONE
        }
        loadingWebViewError?.invoke()
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
     * 拦截WebView请求
     * **/
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun interceptWebViewRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
        if (request != null && request.url != null && request.url.toString().isNotEmpty()) {
            val url = request.url.toString()
            val webResourceCached = webResourceCache.loadWebViewCache(url)

            if (webResourceCached != null) {
                Logger.e("命中缓存: $url")
                return WebResourceResponse(webResourceCached.mimeType, webResourceCached.encoding, webResourceCached.file.inputStream())
            }

            return when {
                url.startsWith("http") -> {
                    val host = request.url.host
                    when {
                        interceptHostList.contains(host) -> handleInterceptRequest(view, request, url)
                        else -> super.shouldInterceptRequest(view, request)
                    }
                }
                url.startsWith("file://") -> handleInterceptRequest(view, request, url)
                else -> return super.shouldInterceptRequest(view, request)
            }
        } else {
            return super.shouldInterceptRequest(view, request)
        }
    }

    /***
     * 处理拦截请求
     * **/
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun handleInterceptRequest(view: WebView?, request: WebResourceRequest?, url: String): WebResourceResponse? {
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(url)
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension)

        return if (fileExtension.isNotEmpty()) {
            if ((mimeType == "image/jpeg" || mimeType == "image/png")) {
                webResourceCache.handleImageRequest(url, mimeType) ?: super.shouldInterceptRequest(view, request)
            } else if (fileExtension == "js" || fileExtension == "css") {
                return webResourceCache.handleOtherRequest(url, mimeType) ?: super.shouldInterceptRequest(view, request)
            } else {
                super.shouldInterceptRequest(view, request)
            }
        } else {
            super.shouldInterceptRequest(view, request)
        }
    }

    /***
     * 初始化WebView设置
     * **/
    @SuppressLint("SetJavaScriptEnabled")
    fun initWebViewSetting() {
        var cachePath: String? = null
        var databasePath: String? = null

        if (webView != null && webSettings == null) {
            webView?.scrollBarStyle = WebView.SCROLLBARS_INSIDE_OVERLAY
            webSettings = webView?.settings
        }

        if (context == null) {
            context = BaseBookApplication.getGlobalContext()
        }

        if (context != null && context?.cacheDir != null && context!!.getDir("databases", 0) != null) {
            try {
                cachePath = context?.cacheDir?.absolutePath
                databasePath = context?.getDir("databases", Context.MODE_PRIVATE)?.path
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }

        if (webSettings != null && cachePath != null && cachePath.isNotEmpty()) {
            webSettings?.setAppCachePath(cachePath)
        }

        if (webSettings != null && databasePath != null && databasePath.isNotEmpty()) {
            webSettings?.databasePath = databasePath
        }

        if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
            //如果有本地缓存则直接使用本地缓存，而不管缓存数据是否过期失效，否则加载网络数据
            webSettings?.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        } else {
            //如果有本地缓存，且缓存有效未过期，则直接使用本地缓存，否则加载网络数据
            webSettings?.cacheMode = WebSettings.LOAD_DEFAULT
        }

        webSettings?.setAppCacheEnabled(true)

        webSettings?.databaseEnabled = true

        webSettings?.domStorageEnabled = true

        webSettings?.allowFileAccess = true

        webSettings?.setAppCacheMaxSize((1024 * 1024 * 15).toLong())

        webSettings?.setNeedInitialFocus(false)

        webSettings?.setSupportMultipleWindows(true)

        try {
            webSettings?.javaScriptEnabled = true
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        webSettings?.javaScriptCanOpenWindowsAutomatically = false

        webSettings?.loadsImagesAutomatically = true

        webSettings?.layoutAlgorithm = LayoutAlgorithm.NORMAL

        webSettings?.setRenderPriority(RenderPriority.HIGH)

        webSettings?.blockNetworkImage = false

        webSettings?.useWideViewPort = true

        webSettings?.textZoom = 100
    }

    /***
     * 初始化部分数据
     * **/
    fun initParameter() {
        if (loadingErrorCount != 0) {
            loadingErrorCount = 0
        }
    }
}