package com.intelligent.reader.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import com.intelligent.reader.BuildConfig
import com.intelligent.reader.R
import com.intelligent.reader.app.BookApplication
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.qbmfrmxs.webview_layout.*
import net.lzbook.kit.ui.widget.LoadingPage
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.web.CustomWebClient
import net.lzbook.kit.utils.webview.JSInterfaceHelper


open class WebViewFragment : Fragment() {

    private var url: String? = null
    private var type: String? = null

    private var customWebClient: CustomWebClient? = null

    private var prepared: Boolean = false

    private var visibleAble: Boolean = false

    private var loadingPage: LoadingPage? = null

    private var handler: Handler = Handler()

    private var jsInterfaceHelper: JSInterfaceHelper? = null

    private var fragmentCallback: FragmentCallback? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.fragmentCallback = context as FragmentCallback?
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = this.arguments

        if (bundle != null) {
            this.url = bundle.getString("url")
            this.type = bundle.getString("type")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.webview_layout, container, false)
    }

    fun setTitle(title: String) {
        txt_web_view_header_title?.text = title
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()

        prepared = true

        if (!TextUtils.isEmpty(url)) {
            loadWebViewData(url)
        }
    }

    @SuppressLint("JavascriptInterface", "AddJavascriptInterface")
    private fun initView() {
        when (type) {
            "recommend" -> {
                txt_web_view_header_title.text = "推荐"
                rl_web_view_header.visibility = View.VISIBLE
            }
            "rank" -> {
                txt_web_view_header_title.text = "排行"
                rl_web_view_header.visibility = View.VISIBLE
            }
            else -> rl_web_view_header.visibility = View.GONE
        }

        AppUtils.disableAccessibility(requireActivity())

        loadingPage = LoadingPage(requireActivity(), rl_web_view_content)


        if (wv_web_view_result != null) {
            customWebClient = CustomWebClient(requireContext(), wv_web_view_result)
        }

        customWebClient?.initWebViewSetting()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            wv_web_view_result?.settings?.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        wv_web_view_result?.webViewClient = customWebClient

        if (wv_web_view_result != null) {
            jsInterfaceHelper = JSInterfaceHelper(requireContext(), wv_web_view_result)
        }

        if (jsInterfaceHelper != null && wv_web_view_result != null) {
            wv_web_view_result?.addJavascriptInterface(jsInterfaceHelper, "J_search")
        }

        if (fragmentCallback != null && jsInterfaceHelper != null) {
            fragmentCallback?.webJsCallback(jsInterfaceHelper!!)
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (userVisibleHint) {
            visibleAble = true
            fragmentVisible()
        } else {
            visibleAble = false
            fragmentInvisible()
        }
    }

    private fun fragmentVisible() {
        if (type != null) {
            if (type == "rank") {
                notifyWebLog()
            }

            if (type == "recommend") {
                notifyWebLog()
            }
        }
    }

    private fun fragmentInvisible() {}

    private fun notifyWebLog() {
        if (!visibleAble || !prepared) {
            return
        }

        wv_web_view_result?.loadUrl("javascript:startEventFunc()")
    }

    private fun loadWebViewData(url: String?) {
        if (url != null && url.isNotEmpty()) {

            handleLoadWebViewAction(url)

            initWebViewCallback()
        }
    }

    private fun handleLoadWebViewAction(url: String) {
        if (wv_web_view_result == null) {
            return
        }

        handler.post { loadingWebView(url) }
    }

    private fun loadingWebView(url: String) {
        customWebClient?.initParameter()

        if (wv_web_view_result != null && url.isNotEmpty()) {
            try {
                wv_web_view_result?.loadUrl(url)
            } catch (exception: NullPointerException) {
                exception.printStackTrace()
                requireActivity().finish()
            }
        }
    }

    private fun initWebViewCallback() {
        customWebClient?.setLoadingWebViewStart { url ->
            Logger.i("LoadStartedAction: $url")
        }

        customWebClient?.setLoadingWebViewError {
            Logger.i("LoadErrorAction")

            loadingPage?.onErrorVisable()
        }

        customWebClient?.setLoadingWebViewFinish {
            Logger.i("LoadFinishAction: $type")
            loadingPage?.onSuccessGone()
        }

        loadingPage?.setReloadAction(LoadingPage.reloadCallback {
            customWebClient?.initParameter()
            wv_web_view_result?.reload()
        })
    }

    override fun onDestroy() {
        super.onDestroy()

        handler.removeCallbacksAndMessages(null)

        wv_web_view_result?.clearCache(false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            rl_web_view_main?.removeView(wv_web_view_result)

            wv_web_view_result?.stopLoading()
            wv_web_view_result?.settings?.javaScriptEnabled = false
            wv_web_view_result?.clearHistory()
            wv_web_view_result?.removeAllViews()
            wv_web_view_result?.destroy()
        } else {
            wv_web_view_result?.stopLoading()
            wv_web_view_result?.settings?.javaScriptEnabled = false
            wv_web_view_result?.clearHistory()
            wv_web_view_result?.removeAllViews()
            wv_web_view_result?.destroy()

            rl_web_view_main?.removeView(wv_web_view_result)
        }

        if (BuildConfig.DEBUG) {
            BookApplication.getRefWatcher().watch(this)
        }
    }

    interface FragmentCallback {
        fun webJsCallback(jsInterfaceHelper: JSInterfaceHelper)

        fun startLoad(webView: WebView, url: String): String
    }
}