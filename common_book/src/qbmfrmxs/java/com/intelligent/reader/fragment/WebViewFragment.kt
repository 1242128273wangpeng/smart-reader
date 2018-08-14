package com.intelligent.reader.fragment

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import com.dingyue.contract.util.*

import com.intelligent.reader.BuildConfig
import com.intelligent.reader.R
import com.intelligent.reader.app.BookApplication
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.qbmfrmxs.webview_layout.*
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.CustomWebClient
import wendu.dsbridge.OnReturnValue

open class WebViewFragment : Fragment() {

    private var url: String? = null
    private var type: String? = null

    private var customWebClient: CustomWebClient? = null

    private var prepared: Boolean = false

    private var visibleAble: Boolean = false

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

    fun setTitle(title:String){
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


        if (bwv_web_view_result != null) {
            customWebClient = CustomWebClient(requireContext(), bwv_web_view_result)
        }

        customWebClient?.setWebSettings()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bwv_web_view_result?.settings?.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        bwv_web_view_result?.webViewClient = customWebClient

        bwv_web_view_result.addJavascriptObject(BridgeObject(requireActivity()), "DingYue")
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

        type.let {
            bwv_web_view_result.callHandler("handleEvent", arrayOf(type), OnReturnValue<Boolean> { data ->
                Logger.e("调用JS打点: $data")
            })
        }
    }

    private fun loadWebViewData(url: String?) {
        if (url != null && url.isNotEmpty()) {

            handleLoadWebViewAction(url)

            initWebViewCallback()
        }
    }

    private fun handleLoadWebViewAction(url: String) {
        if (bwv_web_view_result == null) {
            return
        }

        handler.post { loadingWebView(url) }
    }

    private fun loadingWebView(url: String) {
        customWebClient?.doClear()

        if (bwv_web_view_result != null && url.isNotEmpty()) {
            try {
                bwv_web_view_result?.loadUrl(url)
            } catch (exception: NullPointerException) {
                exception.printStackTrace()
                requireActivity().finish()
            }
        }
    }

    private fun initWebViewCallback() {
        customWebClient?.setStartedAction { url ->
            Logger.i("LoadStartedAction: $url")
        }

        customWebClient?.setErrorAction {
            Logger.i("LoadErrorAction")

            if (loadingPage != null) {
                loadingPage?.onErrorVisable()
            }
        }

        customWebClient?.setFinishedAction {
            Logger.i("LoadFinishAction: $type")

            if (loadingPage != null) {
                loadingPage?.onSuccessGone()
            }
        }

        if (loadingPage != null) {
            loadingPage?.setReloadAction(LoadingPage.reloadCallback {

                if (customWebClient != null) {
                    customWebClient?.doClear()
                }

                bwv_web_view_result?.reload()
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        handler.removeCallbacksAndMessages(null)

        if (bwv_web_view_result != null) {
            bwv_web_view_result?.clearCache(false)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                if (rl_web_view_main != null) {
                    rl_web_view_main?.removeView(bwv_web_view_result)
                }

                bwv_web_view_result?.stopLoading()
                bwv_web_view_result?.settings?.javaScriptEnabled = false
                bwv_web_view_result?.clearHistory()
                bwv_web_view_result?.removeAllViews()
                bwv_web_view_result?.destroy()
            } else {
                bwv_web_view_result?.stopLoading()
                bwv_web_view_result?.settings?.javaScriptEnabled = false
                bwv_web_view_result?.clearHistory()
                bwv_web_view_result?.removeAllViews()
                bwv_web_view_result?.destroy()

                if (rl_web_view_main != null) {
                    rl_web_view_main?.removeView(bwv_web_view_result)
                }
            }
        }

        if (BuildConfig.DEBUG) {
            BookApplication.getRefWatcher().watch(this)
        }
    }
}