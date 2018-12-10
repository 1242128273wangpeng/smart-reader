package com.intelligent.reader.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.RectF
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
import com.ding.basic.net.Config
import com.dingyue.searchbook.activity.SearchBookActivity
import com.google.gson.Gson
import com.intelligent.reader.R
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.qbmfkdxs.frag_web_view.*
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.ui.widget.LoadingPage
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.oneclick.OneClickUtil
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.runOnMain
import net.lzbook.kit.utils.web.CustomWebClient
import net.lzbook.kit.utils.web.JSInterfaceObject

open class WebViewFragment : Fragment(), View.OnClickListener {

    private var url: String? = ""
    private var type: String? = null

    private var customWebClient: CustomWebClient? = null
    private var jsInterfaceObject: JSInterfaceObject? = null

    private var loadingPage: LoadingPage? = null

    private var viewVisible: Boolean = false
    private var viewPrepared: Boolean = false

    val TYPE_RECOMM = "recommend"
    val TYPE_RANK = "rank"
    val TYPE_CATEGORY = "category"
    private var handle = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = this.arguments

        if (bundle != null) {
            this.url = bundle.getString("url")
            this.type = bundle.getString("type")
        }
    }

    private fun setTitle() {
        if (type?.contains(TYPE_RECOMM) == false) {
            rl_head.visibility = View.VISIBLE
            if (TYPE_RANK == type) {
                txt_title?.text = "榜单"
            } else if (TYPE_CATEGORY == type) {
                txt_title?.text = "分类"
            }
        } else {
            rl_head.visibility = View.GONE
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.frag_web_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppUtils.disableAccessibility(requireActivity())
        initParameter()

        viewPrepared = true
        img_search?.setOnClickListener(this)

        if (!TextUtils.isEmpty(url)) {
            when (type) {
                "recommend" -> {
                    requestWebViewData(url)
                }
                "recommendMan" -> {
                    requestWebViewData(url)
                }
                "recommendWoman" -> {
                    requestWebViewData(url)
                }
                "recommendFinish" -> {
                    handle.postDelayed({
                        requestWebViewData(url)
                    }, 2000)
                }
                "recommendFantasy" -> {
                    handle.postDelayed({
                        requestWebViewData(url)
                    }, 2000)
                }
                "recommendModern" -> {
                    handle.postDelayed({
                        requestWebViewData(url)
                    }, 2000)
                }
                "rank" -> {
                    handle.postDelayed({
                        requestWebViewData(url)
                    }, 2000)
                }
                "category" -> {
                    handle.postDelayed({
                        requestWebViewData(url)
                    }, 2000)
                }
            }
        }
        setTitle()
    }


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (userVisibleHint) {
            viewVisible = true
            contentViewVisible()
        } else {
            viewVisible = false
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.img_search -> {
                try {
                    var pageCode = StartLogClickUtil.RECOMMEND_PAGE
                    if (!TextUtils.isEmpty(url)) {
                        when (type) {
                            "recommend", "recommendMan", "recommendWoman", "recommendFinish", "recommendFantasy", "recommendModern" -> {
                                pageCode = StartLogClickUtil.RECOMMEND_PAGE
                            }
                            "rank" -> {
                                pageCode = StartLogClickUtil.TOP_PAGE
                            }
                            "category" -> {
                                pageCode = StartLogClickUtil.CLASS_PAGE
                            }
                        }
                    }

                    StartLogClickUtil.upLoadEventLog(requireContext(), pageCode, StartLogClickUtil.SEARCH)
                    val intent = Intent(activity, SearchBookActivity::class.java)
                    startActivity(intent)
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
        }
    }

    @SuppressLint("AddJavascriptInterface", "JavascriptInterface")
    private fun initParameter() {

        web_view_content?.setLayerType(View.LAYER_TYPE_NONE, null)

        loadingPage = LoadingPage(requireActivity(), rl_web_view_root)

        customWebClient = CustomWebClient(requireContext(), web_view_content)

        customWebClient?.initWebViewSetting()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            web_view_content?.settings?.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        web_view_content?.webViewClient = customWebClient


        jsInterfaceObject = object : JSInterfaceObject(requireActivity()) {


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
                            bundle.putString("url", Config.webViewBaseHost + redirect.url)
                            bundle.putString("title", redirect.title)

                            if (redirect.from != null && (redirect.from?.isNotEmpty() == true)) {
                                when {
                                    redirect.from == "recommend" -> bundle.putString("from", "recommend")
                                    redirect.from == "ranking" -> bundle.putString("from", "ranking")
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

            @JavascriptInterface
            override fun hideWebViewLoading() {
                runOnMain {
                    loadingPage?.onSuccessGone()
                }
            }

            @JavascriptInterface
            override fun handleWebRequestResult(method: String?) {
                if (null != web_view_content) {
                    Logger.e("WebViewMethod: $method")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        web_view_content.evaluateJavascript(method) { value -> Logger.e("ReceivedValue: $value") }
                    } else {
                        web_view_content.loadUrl(method)
                    }
                }
            }
        }

        web_view_content?.addJavascriptInterface(jsInterfaceObject, "J_search")

        web_view_content.addJavascriptInterface(JsPositionInterface(), "J_position")
    }

    private fun contentViewVisible() {
        if (type != null) {
            if (type == "rank") {
                notifyWebLog()
            }
            if (type == "recommend") {
                notifyWebLog()
            }
            if (type == "category") {
                notifyWebLog()
            }
        }
    }

    private fun notifyWebLog() {
        if (!viewVisible || !viewPrepared) {
            return
        }
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

        customWebClient?.setLoadingWebViewStart {
            Logger.e("LoadingWebView: $it")
        }

        customWebClient?.setLoadingWebViewError {
            loadingPage?.onErrorVisable()
        }

        customWebClient?.setLoadingWebViewFinish {
            //无网无缓存（error）
            val isOfflineNotStorage = jsInterfaceObject?.isOfflineNotStorage() ?: false
            if (!NetWorkUtils.isNetworkAvailable(BaseBookApplication.getGlobalContext()) && isOfflineNotStorage) {
                loadingPage?.onErrorVisable()
            } else {
                loadingPage?.onSuccessGone()
            }
        }

        loadingPage?.setReloadAction(LoadingPage.reloadCallback {
            customWebClient?.initParameter()
            web_view_content?.reload()
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (web_view_content != null) {
            web_view_content?.clearCache(true)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                rl_web_view_root?.removeView(web_view_content)
                web_view_content?.stopLoading()
                web_view_content?.settings?.javaScriptEnabled = false
                web_view_content?.clearHistory()
                web_view_content?.removeAllViews()
                web_view_content?.destroy()
            } else {
                web_view_content?.stopLoading()
                web_view_content?.settings?.javaScriptEnabled = false
                web_view_content?.clearHistory()
                web_view_content?.removeAllViews()
                web_view_content?.destroy()
                rl_web_view_root?.removeView(web_view_content)
            }
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