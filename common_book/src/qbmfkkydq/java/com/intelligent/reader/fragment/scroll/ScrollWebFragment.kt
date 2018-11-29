package com.intelligent.reader.fragment.scroll

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import com.ding.basic.net.Config
import com.dingyue.searchbook.activity.SearchBookActivity
import com.google.gson.Gson
import com.intelligent.reader.BuildConfig
import com.intelligent.reader.R
import com.intelligent.reader.app.BookApplication
import com.intelligent.reader.view.scroll.ScrollWebView
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.qbmfkkydq.webview_scroll_layout.*
import net.lzbook.kit.receiver.ConnectionChangeReceiver
import net.lzbook.kit.ui.widget.LoadingPage
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.loge
import net.lzbook.kit.utils.oneclick.OneClickUtil
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.runOnMain
import net.lzbook.kit.utils.web.CustomWebClient
import net.lzbook.kit.utils.web.JSInterfaceObject

/**
 * Desc：推荐Fragment子页面：精选、男频、女频、完本
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/10/26 0026 11:14
 */
class ScrollWebFragment : Fragment(), View.OnClickListener, ConnectionChangeReceiver.RefreshWebViewData {

    override fun onRefreshWebViewData() {
        customWebClient?.initParameter()
        web_view_content?.reload()
    }

    var url: String? = null
    var type: String? = null

    var handler = Handler()

    private var loadingPage: LoadingPage? = null
    private var customWebClient: CustomWebClient? = null
    private var customChangeListener: ScrollWebView.ScrollChangeListener? = null

    private var jSInterfaceObject: JSInterfaceObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = this.arguments
        if (bundle != null) {
            this.url = bundle.getString("url")
            this.type = bundle.getString("type")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.webview_scroll_layout, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AppUtils.disableAccessibility(requireContext())
        initView()

        ConnectionChangeReceiver.bindRefreshWebViewList(this)

        loge("WebView Url: $url")

        if (type == "recommend") {
            requestWebViewData(url)
            jSInterfaceObject?.requestWebViewResult(Config.webViewData)
        } else {
            handler.postDelayed({
                requestWebViewData(url)
            }, 1000)
        }
    }

    var time = 0L
    @SuppressLint("JavascriptInterface", "AddJavascriptInterface")
    private fun initView() {

        web_view_content?.setLayerType(View.LAYER_TYPE_NONE, null)

        if (NetWorkUtils.isNetworkAvailable(context)) {
            time = System.currentTimeMillis()
            loge("JoannChen---------------")
//            loadingPage = LoadingPage(requireActivity(), fl_content_layout)
        }

        customWebClient = CustomWebClient(requireContext(), web_view_content)

        customWebClient?.initWebViewSetting()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            web_view_content.settings?.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        web_view_content?.webViewClient = customWebClient


        if (customChangeListener != null) {
            web_view_content.insertScrollChangeListener(customChangeListener)
        }

        jSInterfaceObject = object : JSInterfaceObject(requireActivity()) {

            @JavascriptInterface
            override fun startSearchActivity(data: String?) {
                val intent = Intent()
                intent.setClass(requireContext(), SearchBookActivity::class.java)
                startActivity(intent)
            }

            @JavascriptInterface
            override fun startTabulationActivity(data: String?) {
                if (data != null && data.isNotEmpty() && !activity.isFinishing) {
                    if (OneClickUtil.isDoubleClick(System.currentTimeMillis())) {
                        return
                    }

                    try {
                        val redirect = Gson().fromJson(data, JSInterfaceObject.JSRedirect::class.java)

                        if (redirect?.url != null && redirect.title != null) {
                            val bundle = Bundle()
                            bundle.putString("url", Config.webViewBaseHost + redirect.url)
                            bundle.putString("title", redirect.title)

                            if (redirect.from != null && (redirect.from?.isNotEmpty() == true)) {
                                when {
                                    redirect.from == "recommend" -> bundle.putString("from", "recommend")
                                    redirect.from == "rank" -> bundle.putString("from", "rank")
                                    redirect.from == "category" -> bundle.putString("from", "category")
                                    else -> bundle.putString("from", "other")
                                }
                            } else {
                                bundle.putString("from", "other")
                            }

                            RouterUtil.navigation(activity, RouterConfig.TABULATION_ACTIVITY, bundle)
                        }
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                }
            }

            @JavascriptInterface
            override fun handleBackAction() {

            }

            override fun hideWebViewLoading() {
                runOnMain {
                    //                    loadingPage?.onSuccessGone()
                    loge("JoannChen结束: ${System.currentTimeMillis() - time}")
                }
            }

            override fun handleWebRequestResult(method: String?) {
                Logger.e("WebViewMethod: $method")
                if (null != web_view_content) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        web_view_content.evaluateJavascript(method) { value -> Logger.e("ReceivedValue: $value") }
                    } else {
                        web_view_content.loadUrl(method)
                    }
                }
            }
        }

        web_view_content?.addJavascriptInterface(jSInterfaceObject, "J_search")

        web_view_content?.addJavascriptInterface(JsPositionInterface(), "J_position")

    }


    private fun requestWebViewData(url: String?) {
        startLoadingWebViewData(url)
        initWebViewCallback()
    }

    private fun startLoadingWebViewData(url: String?) {
        if (web_view_content == null) {
            return
        }
        web_view_content?.post {
            handleLoadingWebViewData(url)
        }
    }


    private fun handleLoadingWebViewData(url: String?) {
        customWebClient?.initParameter()

        if (url != null && url.isNotEmpty()) {
            try {
                web_view_content?.loadUrl(url)
            } catch (exception: NullPointerException) {
                exception.printStackTrace()
                requireActivity().finish()
            }
        }
    }


    private fun initWebViewCallback() {
        if (web_view_content == null) {
            return
        }

        if (customWebClient != null) {
            customWebClient?.setLoadingWebViewStart {
                Logger.e("LoadingWebView: $it")
            }

            customWebClient?.setLoadingWebViewError {
                //                loadingPage?.onErrorVisable()
            }

            customWebClient?.setLoadingWebViewFinish {
                //                loadingPage?.onSuccessGone()
                loge("JoannChen回调: ${System.currentTimeMillis() - time}")
            }
        }


//        loadingPage?.setReloadAction(LoadingPage.reloadCallback {
//            customWebClient?.initParameter()
//            web_view_content?.reload()
//        })


    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.content_head_search -> RouterUtil.navigation(requireActivity(), RouterConfig.SEARCH_BOOK_ACTIVITY)
            R.id.content_download_manage -> try {
                RouterUtil.navigation(activity!!,
                        RouterConfig.DOWNLOAD_MANAGER_ACTIVITY)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ConnectionChangeReceiver.unbindRefreshWebViewList(this)
        web_view_content?.clearCache(true) //清空缓存
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fl_content_layout?.removeView(web_view_content)
            web_view_content?.stopLoading()
            web_view_content?.removeAllViews()
            web_view_content?.destroy()
        } else {
            web_view_content?.stopLoading()
            web_view_content?.removeAllViews()
            web_view_content?.destroy()
            fl_content_layout?.removeView(web_view_content)
        }

        if (BuildConfig.DEBUG) {
            BookApplication.getRefWatcher().watch(this)
        }

    }


    /**
     * 获取web中banner的位置js回调
     */
    inner class JsPositionInterface {

        @JavascriptInterface
        fun insertProhibitSlideArea(x: String, y: String, width: String, height: String) {
            try {
                val viewWidth = java.lang.Float.parseFloat(width)
                val viewHeight = java.lang.Float.parseFloat(height)
                val scale = web_view_content.resources.displayMetrics.widthPixels / viewWidth

                web_view_content.insertProhibitSlideArea(RectF(
                        x.toFloat() * scale, y.toFloat() * scale, (x.toFloat() + viewWidth) * scale,
                        (y.toFloat() + viewHeight) * scale))
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }
}