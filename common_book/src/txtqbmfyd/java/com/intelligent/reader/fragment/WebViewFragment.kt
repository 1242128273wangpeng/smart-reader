package com.intelligent.reader.fragment

import android.annotation.SuppressLint
import android.content.Intent
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
import com.dingyue.contract.web.JSInterfaceObject
import com.dingyue.searchbook.SearchBookActivity
import com.google.gson.Gson
import com.intelligent.reader.R
import kotlinx.android.synthetic.main.view_refresh_header.view.*
import kotlinx.android.synthetic.txtqbmfyd.webview_layout.*
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.ui.widget.LoadingPage
import net.lzbook.kit.ui.widget.pulllist.SuperSwipeRefreshLayout
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.oneclick.OneClickUtil
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.utils.web.CustomWebClient

open class WebViewFragment : Fragment(), View.OnClickListener {

    private var customWebClient: CustomWebClient? = null

    private var loadingPage: LoadingPage? = null

    private var viewVisible = false
    private var viewPrepared = false

    private var userVisibleView = false

    private var url: String? = ""
    private var type: String? = null

    private var handle = Handler()

    private val refreshHeader: View by lazy {
        LayoutInflater.from(srl_web_view_refresh.context).inflate(R.layout.view_refresh_header, null)
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

        initParameter()

        initRefreshView()

        viewPrepared = true

        if (type != null && type == "category_female") {
            handleLazyLoading()
        } else {
            if (!TextUtils.isEmpty(url)) {
                when (type) {
                    "recommend" -> {
                        requestWebViewData(url)
                    }
                    "rank" -> {
                        handle.postDelayed({
                            requestWebViewData(url)
                        }, 2000)
                    }
                    "category_male" -> {
                        handle.postDelayed({
                            requestWebViewData(url)
                        }, 2000)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (web_view_content != null) {
            web_view_content?.clearCache(false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (web_view_root != null) {
                    web_view_root?.removeView(web_view_content)
                }
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

                if (web_view_root != null) {
                    web_view_root?.removeView(web_view_content)
                }
            }
        }
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

    override fun onClick(view: View) {
        when (view.id) {
            R.id.content_head_search -> {
                val intent = Intent(activity, SearchBookActivity::class.java)
                try {
                    startActivity(intent)
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
        }
    }

    /***
     * 初始化参数
     * **/
    @SuppressLint("AddJavascriptInterface", "JavascriptInterface")
    private fun initParameter() {

        web_view_content?.topShadow = img_head_shadow

        web_view_content?.setLayerType(View.LAYER_TYPE_NONE, null)

        when (type) {
            "recommend" -> {
                rl_recommend_head?.visibility = View.VISIBLE
                rl_ranking_head?.visibility = View.GONE

                rl_web_view_head?.visibility = View.VISIBLE
            }

            "rank" -> {
                rl_recommend_head?.visibility = View.GONE
                rl_ranking_head?.visibility = View.VISIBLE

                rl_web_view_head?.visibility = View.VISIBLE
            }

            else -> rl_web_view_head?.visibility = View.GONE
        }

        rl_recommend_search?.setOnClickListener {
            startActivity(Intent(requireContext(), SearchBookActivity::class.java))
            StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.RECOMMEND_PAGE, StartLogClickUtil.QG_TJY_SEARCH)
        }

        img_ranking_search?.setOnClickListener {
            startActivity(Intent(requireContext(), SearchBookActivity::class.java))
            StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.TOP_PAGE, StartLogClickUtil.QG_BDY_SEARCH)
        }

        loadingPage = LoadingPage(requireActivity(), web_view_root)

        customWebClient = CustomWebClient(requireContext(), web_view_content)

        customWebClient?.initWebViewSetting()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            web_view_content?.settings?.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        web_view_content?.webViewClient = customWebClient

        web_view_content?.addJavascriptInterface(object : JSInterfaceObject(requireActivity()) {

            @JavascriptInterface
            override fun startSearchActivity(data: String?) {

            }

            @JavascriptInterface
            override fun startTabulationActivity(data: String?) {
                if (data != null && data.isNotEmpty() && !activity!!.isFinishing) {
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
                                    redirect.from == "rank" -> bundle.putString("from", "rank")
                                    redirect.from == "category" -> bundle.putString("from", "category")
                                    else -> bundle.putString("from", "other")
                                }
                            } else {
                                bundle.putString("from", "other")
                            }

                            RouterUtil.navigation(activity!!, RouterConfig.TABULATION_ACTIVITY, bundle)
                        }
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                }
            }
        }, "J_search")
    }

    /***
     * 初始化刷新布局
     * **/
    private fun initRefreshView() {
        if (url != null && !TextUtils.isEmpty(url)) {
            srl_web_view_refresh?.setHeaderViewBackgroundColor(0x00000000)
            srl_web_view_refresh?.setHeaderView(initRefreshHeaderView())
            srl_web_view_refresh?.isTargetScrollWithLayout = true
            srl_web_view_refresh?.setOnPullRefreshListener(object : SuperSwipeRefreshLayout.OnPullRefreshListener {

                override fun onRefresh() {
                    refreshHeader.txt_refresh_prompt.text = "正在刷新"
                    refreshHeader.img_refresh_arrow.visibility = View.GONE
                    refreshHeader.pgbar_refresh_loading.visibility = View.VISIBLE
                    refreshContentData()
                }

                override fun onPullDistance(distance: Int) {}

                override fun onPullEnable(enable: Boolean) {
                    refreshHeader.pgbar_refresh_loading.visibility = View.GONE
                    refreshHeader.txt_refresh_prompt.text = if (enable) "松开刷新" else "下拉刷新"
                    refreshHeader.img_refresh_arrow.visibility = View.VISIBLE
                    refreshHeader.img_refresh_arrow.rotation = (if (enable) 180 else 0).toFloat()
                }
            })

            if (type.equals("recommend")) {
                srl_web_view_refresh?.setPullToRefreshEnabled(true)
            } else {
                srl_web_view_refresh?.setPullToRefreshEnabled(false)
            }
        }
    }

    /***
     * 初始化刷新头布局
     * **/
    private fun initRefreshHeaderView(): View {
        refreshHeader.txt_refresh_prompt.text = "下拉刷新"
        refreshHeader.img_refresh_arrow.visibility = View.VISIBLE
        refreshHeader.img_refresh_arrow.setImageResource(R.drawable.pulltorefresh_down_arrow)
        refreshHeader.pgbar_refresh_loading.visibility = View.GONE
        return refreshHeader
    }

    /***
     * 刷新WebView内容
     * **/
    private fun refreshContentData() {
        if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
            srl_web_view_refresh?.isRefreshing = false
           ToastUtil.showToastMessage("网络不给力！")
            return
        }

        srl_web_view_refresh?.onRefreshComplete()

        handleRefreshContentData("javascript:refreshNew()")

        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.RECOMMEND_PAGE, StartLogClickUtil.DROPDOWN)
    }

    /***
     * 处理刷新WebView内容请求
     * **/
    private fun handleRefreshContentData(message: String) {
        if (!TextUtils.isEmpty(message) && web_view_content != null) {

            web_view_content?.post {
                try {
                    web_view_content?.loadUrl(message)
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    requireActivity().finish()
                }
            }
        }
    }

    /***
     * 内容页面展示给用户
     * **/
    private fun contentViewVisible() {
        if (type != null) {
            if (type == "category_female") {
                handleLazyLoading()
            }
        }
    }

    /***
     * 延迟加载
     * **/
    private fun handleLazyLoading() {
        if (viewVisible && viewPrepared && !userVisibleView) {
            if (!TextUtils.isEmpty(url)) {
                requestWebViewData(url)
            }
            userVisibleView = true
        }
    }

    /***
     * 请求WebView数据
     * **/
    private fun requestWebViewData(url: String?) {
        startLoadingWebViewData(url)
        initWebViewCallback()
    }

    /***
     * 开始请求WebView数据
     * **/
    private fun startLoadingWebViewData(url: String?) {
        if (web_view_content == null) {
            return
        }
        web_view_content?.post {
            handleLoadingWebViewData(url)
        }
    }

    /***
     * 处理WebView请求
     * **/
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

    /***
     * 初始化WebView请求回调
     * **/
    private fun initWebViewCallback() {
        if (web_view_content == null) {
            return
        }

        if (customWebClient != null) {
            customWebClient?.setLoadingWebViewStart {

            }

            customWebClient?.setLoadingWebViewFinish {
                if (loadingPage != null) {
                    loadingPage?.onSuccessGone()
                }
            }

            customWebClient?.setLoadingWebViewError {
                if (loadingPage != null) {
                    loadingPage?.onErrorVisable()
                }
            }
        }

        if (loadingPage != null) {
            loadingPage?.setReloadAction(LoadingPage.reloadCallback {
                if (customWebClient != null) {
                    customWebClient?.initParameter()
                }
                web_view_content?.reload()
            })
        }
    }
}