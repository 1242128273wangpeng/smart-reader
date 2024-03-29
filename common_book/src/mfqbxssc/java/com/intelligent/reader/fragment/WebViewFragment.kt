package com.intelligent.reader.fragment

import android.annotation.SuppressLint
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



import com.google.gson.Gson
import com.intelligent.reader.R
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.mfqbxssc.frag_web_view.*

import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.ui.widget.LoadingPage

import net.lzbook.kit.utils.CustomWebView
import net.lzbook.kit.utils.book.CommonContract
import net.lzbook.kit.utils.oneclick.OneClickUtil
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.web.CustomWebClient
import net.lzbook.kit.utils.web.JSInterfaceObject

class WebViewFragment : Fragment() {

    private var url: String? = ""
    private var type: String? = null
    
    private var customWebClient: CustomWebClient? = null

    private var loadingPage: LoadingPage? = null

    private var handle = Handler()

    private var customChangeListener: CustomWebView.ScrollChangeListener? = null

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
                "recommendMan" -> {
                    requestWebViewData(url)
                }
                "recommendWoman" -> {
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

        loadingPage = LoadingPage(requireActivity(), rl_web_view_content)

        customWebClient = CustomWebClient(requireContext(), web_view_content)

        customWebClient?.initWebViewSetting()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            web_view_content?.settings?.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        web_view_content?.webViewClient = customWebClient

        if (customChangeListener != null) {
            web_view_content.insertScrollChangeListener(customChangeListener)
        }

        web_view_content?.addJavascriptInterface(object : JSInterfaceObject(requireActivity()) {

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

        web_view_content?.addJavascriptInterface(JsPositionInterface(), "J_position")

        img_web_view_header_search?.setOnClickListener {
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
        if ("recommendMan" == type || "recommendWoman" == type) {
            if (rl_web_view_header != null) {
                rl_web_view_header?.visibility = View.GONE
            }

            return
        }

        if (rl_web_view_header != null) {
            rl_web_view_header?.visibility = View.VISIBLE
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
                web_view_content?.loadUrl(url)
            })
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