package net.lzbook.kit

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.support.annotation.RequiresApi
import android.text.TextUtils
import android.util.Base64
import android.view.View
import android.webkit.*
import android.webkit.WebSettings.LayoutAlgorithm
import android.webkit.WebSettings.RenderPriority
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target.*
import com.ding.basic.util.ReplaceConstants

import com.dingyue.contract.util.showToastMessage
import com.orhanobut.logger.Logger

import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.utils.NetWorkUtils
import java.io.*
import java.math.BigInteger
import java.net.URL
import java.security.MessageDigest

class CustomWebViewClient(internal var context: Context?, internal var webView: WebView?) : WebViewClient() {

    private var loadingStartCount = 0
    private var loadingFinishCount = 0

    private var loadingErrorCount = 0

    var webSettings: WebSettings? = null

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

    companion object {
        val interceptHostList = mutableListOf("s.image.qingoo.cn", "sta-cntxtqbmfydreader.readzq.com")
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
                    context?.showToastMessage("未找到微信客户端，请先安装微信！")
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
                    context?.showToastMessage("未找到支付宝客户端，请先安装支付宝！")
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
            if (webView != null) {
                webView?.stopLoading()
                webView?.loadUrl("about:blank")
                webView?.visibility = View.GONE
            }
            loadingWebViewError?.invoke()
        }
        super.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        loadingFinishCount++
        if (loadingErrorCount == 0 && loadingWebViewFinish != null) {
            loadingWebViewFinish?.invoke()
            if (webView != null) {
                webView?.visibility = View.VISIBLE
            }
            if (webSettings != null) {
                webSettings?.blockNetworkImage = false
            }
        } else if (loadingErrorCount != 0 && loadingWebViewError != null) {
            if (webView != null) {
                webView?.stopLoading()
                webView?.loadUrl("about:blank")
                webView?.visibility = View.GONE
            }
            loadingWebViewError?.invoke()
        }
        super.onPageFinished(view, url)
    }

    override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
        loadingErrorCount = errorCode
        if (webView != null) {
            webView?.stopLoading()
            webView?.loadUrl("about:blank")
        }
        super.onReceivedError(view, errorCode, description, failingUrl)
    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        handler.proceed()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
        return interceptWebViewRequest(view, request)
    }

    /***
     * 设置加载WebView开始监听
     * **/
    fun setLoadingWebViewStart(loadingWebViewStart: (url: String?) -> Unit) {
        this.loadingWebViewStart = loadingWebViewStart
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
        if (request != null && request.url != null && !TextUtils.isEmpty(request.url.toString())) {
            val url = request.url.toString()
            val schema = request.url.scheme

            return if (!TextUtils.isEmpty(url) && schema != null && schema == "http" || schema == "https") {
                val host = request.url.host
                if (interceptHostList.contains(host)) {
                    Logger.e("需要缓存的地址: $url")
                    handleInterceptRequest(view, request, url)
                } else {
                    super.shouldInterceptRequest(view, request)
                }
            } else {
                super.shouldInterceptRequest(view, request)
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

        return if (mimeType == "image/jpeg" || mimeType == "image/png") {
            handleImageRequest(view, request, url, mimeType)
        } else {
            handleOtherRequest(view, request, url, mimeType, fileExtension)
        }
    }

    /***
     * 处理图片拦截请求
     * **/
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun handleImageRequest(view: WebView?, request: WebResourceRequest?, url: String, mimeType: String?): WebResourceResponse? {
        try {
            val cacheFile = Glide.with(context)
                    .load(url)
                    .downloadOnly(SIZE_ORIGINAL, SIZE_ORIGINAL)
                    .get()

            return try {
                WebResourceResponse(mimeType, null, cacheFile.inputStream())
            } catch (exception: Exception) {
                exception.printStackTrace()
                super.shouldInterceptRequest(view, request)
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            return super.shouldInterceptRequest(view, request)
        }
    }

    /***
     * 处理其他拦截请求
     * **/
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun handleOtherRequest(view: WebView?, request: WebResourceRequest?, url: String, mimeType: String?, fileExtension: String): WebResourceResponse? {
        val fileName = loadCacheFileName(url, "MD5")
        val filePath = ReplaceConstants.getReplaceConstants().APP_PATH_CACHE + fileName + "." + fileExtension

        Logger.e("FilePath: $filePath")

        val file = File(filePath)

        if (file.exists()) {
            return try {
                Logger.e("从文件获取网络请求结果，网络请求地址: $url")
                WebResourceResponse(mimeType, "UTF-8", file.inputStream())
            } catch (exception: Exception) {
                exception.printStackTrace()
                super.shouldInterceptRequest(view, request)
            }
        } else {
            return try {
                val urlConnection = URL(url).openConnection()

                val inputStream = urlConnection.getInputStream()

                Logger.e("缓存网络请求地址: $url    缓存文件格式: $fileExtension")
                cacheWebViewSource(inputStream, file)

                WebResourceResponse(urlConnection.contentType, urlConnection.getHeaderField("encoding"), inputStream)
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
                super.shouldInterceptRequest(view, request)
            }
        }
    }

    /***
     * 获取缓存文件名
     * **/
    private fun loadCacheFileName(url: String, method: String): String? {
        return try {
            val messageDigest = MessageDigest.getInstance(method)
            messageDigest?.update(url.toByteArray())
            BigInteger(1, messageDigest.digest()).toString(16)
        } catch (exception: Exception) {
            exception.printStackTrace()
            Base64.encodeToString(url.toByteArray(), Base64.DEFAULT)
        }
    }

    /***
     * 缓存获取到的文件流
     * **/
    private fun cacheWebViewSource(inputStream: InputStream?, file: File?) {
        var read: Int = -1
        inputStream?.use { input ->
            file?.outputStream().use {
                while (input.read().also { read = it } != -1) {
                    it?.write(read)
                }
            }
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
            webSettings?.setDatabasePath(databasePath)
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

        webSettings?.blockNetworkImage = true

        webSettings?.useWideViewPort = true
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