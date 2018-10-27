package com.intelligent.reader.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import com.google.gson.Gson
import com.intelligent.reader.BuildConfig
import com.intelligent.reader.R
import com.intelligent.reader.app.BookApplication
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.qbmfrmxs.webview_layout.*
import net.lzbook.kit.ui.widget.LoadingPage
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.oneclick.OneClickUtil
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.web.CustomWebClient
import net.lzbook.kit.utils.web.JSInterfaceObject


open class WebViewFragment : Fragment() {

    private var url: String? = null
    private var type: String? = null

    private var customWebClient: CustomWebClient? = null

    private var visibleAble: Boolean = false

    private var prepared: Boolean = false

    private var loadingPage: LoadingPage? = null

    private var handler: Handler = Handler()

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

        wv_web_view_result?.addJavascriptInterface(object : JSInterfaceObject(requireActivity()) {

            @JavascriptInterface
            override fun startSearchActivity(data: String?) {

            }

            @JavascriptInterface
            override fun startTabulationActivity(data: String?) {
                if (data != null && data.isNotEmpty() && !activity.isFinishing) {
                    if (OneClickUtil.isDoubleClick(System.currentTimeMillis())) {
                        return
                    }

                    try {
                        val redirect = Gson().fromJson(data, JSRedirect::class.java)

                        if (redirect?.url != null && redirect.title != null) {
                            val bundle = Bundle()
                            bundle.putString("url", redirect.url)
                            bundle.putString("title", redirect.title)

                            if (redirect.from != null && (redirect.from?.isNotEmpty() == true)) {
                                when {
                                    redirect.from == "recommend" -> bundle.putString("from", "recommend")
                                    redirect.from == "ranking" -> bundle.putString("from", "ranking")
                                    redirect.from == "category" -> bundle.putString("from", "category")
                                    else -> bundle.putString("from", "other")
                                }
                            } else {
                                bundle.putString("from", "authorType")
                            }

                            RouterUtil.navigation(activity, RouterConfig.TABULATION_ACTIVITY, bundle)
                        }
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                }
            }
        }, "J_search")
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

}