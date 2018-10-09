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
import com.dingyue.contract.CommonContract

import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dingyue.contract.util.CommonUtil
import com.dingyue.contract.web.CustomWebClient
import com.dingyue.contract.web.JSInterfaceObject
import com.google.gson.Gson
import com.intelligent.reader.R
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.mfqbxssc.frag_web_view.*
import kotlinx.android.synthetic.mfqbxssc.view_web_view_refresh.view.*

import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.pulllist.SuperSwipeRefreshLayout
import net.lzbook.kit.utils.NetWorkUtils

class WebViewFragment : Fragment() {

    private var url: String? = ""
    private var type: String? = null
    
    private var customWebClient: CustomWebClient? = null

    private var loadingPage: LoadingPage? = null

    private var handle = Handler()

    private val refreshHeader: View by lazy {
        LayoutInflater.from(srl_web_view_refresh.context).inflate(R.layout.view_web_view_refresh, null)
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
        return inflater.inflate(R.layout.frag_web_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initParameter()

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
                "category" -> {
                    handle.postDelayed({
                        requestWebViewData(url)
                    }, 2000)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (web_view_content != null) {
            web_view_content?.clearCache(false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (rl_web_view_content != null) {
                    rl_web_view_content?.removeView(web_view_content)

                }
                web_view_content?.stopLoading()
                web_view_content?.removeAllViews()
                web_view_content?.destroy()
            } else {
                web_view_content?.stopLoading()
                web_view_content?.removeAllViews()
                web_view_content?.destroy()
                if (rl_web_view_content != null) {
                    rl_web_view_content?.removeView(web_view_content)
                }
            }
        }
    }

    @SuppressLint("AddJavascriptInterface", "JavascriptInterface")
    private fun initParameter() {
        web_view_content?.setLayerType(View.LAYER_TYPE_NONE, null)

        refreshContentHeader()

        initRefreshHeader()

        loadingPage = LoadingPage(requireActivity(), rl_web_view_content)

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
                if (data != null && data.isNotEmpty() && !activity.isFinishing) {
                    if (CommonContract.isDoubleClick(System.currentTimeMillis())) {
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
                                bundle.putString("from", "other")
                            }

                            RouterUtil.navigation(activity, RouterConfig.TABULATION_ACTIVITY, bundle)
                        }
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                }
            }
        }, "J_search")


        rl_web_view_header_recommend?.setOnClickListener {
            RouterUtil.navigation(requireActivity(),
                    RouterConfig.SEARCH_BOOK_ACTIVITY)

            StartLogClickUtil.upLoadEventLog(requireActivity(),
                    StartLogClickUtil.RECOMMEND_PAGE, StartLogClickUtil.QG_TJY_SEARCH)
        }

        img_web_view_header_other_search?.setOnClickListener {
            RouterUtil.navigation(requireActivity(),
                    RouterConfig.SEARCH_BOOK_ACTIVITY)
            if ("rank" == type) {
                StartLogClickUtil.upLoadEventLog(requireActivity(),
                        StartLogClickUtil.TOP_PAGE, StartLogClickUtil.QG_BDY_SEARCH)
            } else if ("category" == type) {
                StartLogClickUtil.upLoadEventLog(requireActivity(),
                        StartLogClickUtil.CLASS_PAGE, StartLogClickUtil.QG_FL_SEARCH)
            }
        }
    }


    private fun refreshContentHeader() {
        if ("recommend" == type) {
            if (rl_web_view_header_recommend != null) {
                rl_web_view_header_recommend?.visibility = View.VISIBLE
            }

            if (rl_web_view_header_other != null) {
                rl_web_view_header_other?.visibility = View.GONE
            }

            return
        }

        if (rl_web_view_header_other != null) {
            rl_web_view_header_other?.visibility = View.VISIBLE
        }

        if (rl_web_view_header_recommend != null) {
            rl_web_view_header_recommend?.visibility = View.GONE
        }

        if (txt_web_view_header_title != null && type != null) {
            if ("rank" == type) {
                txt_web_view_header_title?.text = "榜单"
            } else if ("category" == type) {
                txt_web_view_header_title?.text = "分类"
            }
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

        if (customWebClient != null) {
            customWebClient?.setLoadingWebViewStart {
                Logger.e("LoadingWebView: $it")
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

    private fun initRefreshHeader() {
        if (!TextUtils.isEmpty(url)) {
            srl_web_view_refresh?.setHeaderViewBackgroundColor(0x00000000)
            srl_web_view_refresh?.setHeaderView(createHeaderView())
            srl_web_view_refresh?.isTargetScrollWithLayout = true

            srl_web_view_refresh?.setOnPullRefreshListener(object : SuperSwipeRefreshLayout.OnPullRefreshListener {
                override fun onRefresh() {
                    refreshHeader.txt_refresh_prompt.text = "正在刷新"
                    refreshHeader.img_refresh_arrow.visibility = View.GONE
                    refreshHeader.pgbar_refresh_loading.visibility = View.VISIBLE
                    refreshContentView()
                }

                override fun onPullDistance(distance: Int) {}

                override fun onPullEnable(enable: Boolean) {
                    refreshHeader.pgbar_refresh_loading.visibility = View.GONE
                    refreshHeader.txt_refresh_prompt.text = if (enable) "松开刷新" else "下拉刷新"
                    refreshHeader.img_refresh_arrow.visibility = View.VISIBLE
                    refreshHeader.img_refresh_arrow.rotation = (if (enable) 180 else 0).toFloat()
                }
                
            })
            if ("recommend" == type) {
                srl_web_view_refresh?.setPullToRefreshEnabled(true)
            } else {
                srl_web_view_refresh?.setPullToRefreshEnabled(false)
            }
        }
    }

    private fun createHeaderView(): View {
        refreshHeader.txt_refresh_prompt.text = "下拉刷新"
        refreshHeader.img_refresh_arrow.visibility = View.VISIBLE
        refreshHeader.img_refresh_arrow.setImageResource(R.drawable.pulltorefresh_down_arrow)
        refreshHeader.pgbar_refresh_loading.visibility = View.GONE
        return refreshHeader
    }

    private fun refreshContentView() {
        if (srl_web_view_refresh == null) {
            return
        }

        if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
            srl_web_view_refresh?.isRefreshing = false
            CommonUtil.showToastMessage("网络不给力")
            return
        }

        srl_web_view_refresh?.onRefreshComplete()

        refreshContent("javascript:refreshNew()")
    }

    private fun refreshContent(message: String) {
        if (!TextUtils.isEmpty(message)) {
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
}