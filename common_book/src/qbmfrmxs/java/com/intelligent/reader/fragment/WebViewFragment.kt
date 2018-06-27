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
import kotlinx.android.synthetic.qbmfrmxs.webview_layout.*
import kotlinx.android.synthetic.qbmfrmxs.bookstore_refresh_header.view.*
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.pulllist.SuperSwipeRefreshLayout
import net.lzbook.kit.utils.NetWorkUtils
import com.orhanobut.logger.Logger
import com.github.lzyzsd.jsbridge.DefaultHandler

open class WebViewFragment : Fragment() {

    private var url: String? = null
    private var type: String? = null

    private var customWebViewClient: CustomWebViewClient? = null

    private var prepared: Boolean = false

    private var visibleAble: Boolean = false

    private var loadingPage: LoadingPage? = null

    private var handler: Handler = Handler()

    private val refreshHeader: View by lazy {
        LayoutInflater.from(srl_web_view_refresh.context).inflate(R.layout.bookstore_refresh_header, null)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initRefreshView()

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

        loadingPage = LoadingPage(requireActivity(), rl_web_view_content)

        if (bwv_web_view_result != null) {
            customWebViewClient = CustomWebViewClient(requireContext(), bwv_web_view_result)
        }

        customWebViewClient?.setWebSettings()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bwv_web_view_result?.settings?.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        bwv_web_view_result?.webViewClient = customWebViewClient

        bwv_web_view_result?.setDefaultHandler(DefaultHandler())

        bwv_web_view_result?.registerHandler("buildRequestUrl", BuildRequestUrlHandler())

        bwv_web_view_result?.registerHandler("startCoverActivity", StartCoverHandler(requireActivity()))

        bwv_web_view_result?.registerHandler("startTabulationActivity", StartTabulationHandler(requireActivity()))

        bwv_web_view_result?.send("Hello", { data ->
            Logger.e("来自JS的消息: " + data.toString())
        })
    }

    private fun initRefreshView() {
        if (!TextUtils.isEmpty(url)) {
            srl_web_view_refresh?.setHeaderViewBackgroundColor(0x00000000)
            srl_web_view_refresh?.setHeaderView(createHeaderView())
            srl_web_view_refresh?.isTargetScrollWithLayout = true
            srl_web_view_refresh?.setOnPullRefreshListener(object : SuperSwipeRefreshLayout.OnPullRefreshListener {

                override fun onRefresh() {
                    refreshHeader.txt_refresh_prompt.text = "正在刷新"
                    refreshHeader.img_refresh_arrow.visibility = View.GONE
                    refreshHeader.pgbar_refresh_loading.visibility = View.VISIBLE
                    checkUpdate()
                }

                override fun onPullDistance(distance: Int) {}

                override fun onPullEnable(enable: Boolean) {
                    refreshHeader.pgbar_refresh_loading.visibility = View.GONE
                    refreshHeader.txt_refresh_prompt.text = if (enable) "松开刷新" else "下拉刷新"
                    refreshHeader.img_refresh_arrow.visibility = View.VISIBLE
                    refreshHeader.img_refresh_arrow.rotation = (if (enable) 180 else 0).toFloat()
                }
            })

            if (url != null && url!!.isNotEmpty()) {
                if (url!!.contains("recommend")) {
                    srl_web_view_refresh?.setPullToRefreshEnabled(true)
                } else {
                    srl_web_view_refresh?.setPullToRefreshEnabled(false)
                }
            } else {
                srl_web_view_refresh?.setPullToRefreshEnabled(false)
            }
        }
    }

    private fun createHeaderView(): View {
        refreshHeader.txt_refresh_prompt.text = getString(com.dingyue.bookshelf.R.string.refresh_start)
        refreshHeader.img_refresh_arrow.visibility = View.VISIBLE
        refreshHeader.img_refresh_arrow.setImageResource(com.dingyue.bookshelf.R.drawable.pulltorefresh_down_arrow)
        refreshHeader.pgbar_refresh_loading.visibility = View.GONE
        return refreshHeader
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

        //Android调用JS打点
        bwv_web_view_result.callHandler("handleEvent", type, { data ->
            Logger.e("调用JS打点: $data")
        })
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
        customWebViewClient?.doClear()

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

        customWebViewClient?.setStartedAction { url ->
            Logger.i("LoadStartedAction: $url")
        }

        customWebViewClient?.setErrorAction {
            Logger.i("LoadErrorAction")

            if (loadingPage != null) {
                loadingPage?.onErrorVisable()
            }
        }

        customWebViewClient?.setFinishedAction {
            Logger.i("LoadFinishAction: $type")

            if (loadingPage != null) {
                loadingPage?.onSuccessGone()
            }

            if (type == "recommend") {
                checkViewSlide()
            }
        }

        if (loadingPage != null) {
            loadingPage?.setReloadAction(LoadingPage.reloadCallback {

                if (customWebViewClient != null) {
                    customWebViewClient?.doClear()
                }

                bwv_web_view_result?.reload()
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        handler.removeCallbacksAndMessages(null)

        if (bwv_web_view_result != null) {
            bwv_web_view_result?.clearCache(true) //清空缓存

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

    private fun checkUpdate() {
        if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
            srl_web_view_refresh?.isRefreshing = false
            activity?.applicationContext?.showToastMessage("网络不给力！")
            return
        }

        srl_web_view_refresh?.onRefreshComplete()

        bwv_web_view_result.callHandler("refreshView", type, { data ->
            Logger.e("调用JS刷新: $data")
        })
    }

    private fun checkViewSlide() {
        if (bwv_web_view_result != null) {

            bwv_web_view_result?.callHandler("loadPageInformation", type, { data ->
                Logger.e("调用JS获取页面信息: $data")
            })
        }
    }
}