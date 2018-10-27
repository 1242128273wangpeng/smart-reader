package com.intelligent.reader.fragment.scroll

import android.annotation.SuppressLint
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import com.google.gson.Gson
import com.intelligent.reader.BuildConfig
import com.intelligent.reader.R
import com.intelligent.reader.app.BookApplication
import com.intelligent.reader.view.scroll.ScrollWebView
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.qbmfkkydq.webview_scroll_layout.*
import net.lzbook.kit.ui.widget.LoadingPage
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.oneclick.OneClickUtil
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.web.CustomWebClient
import net.lzbook.kit.utils.web.JSInterfaceObject

/**
 * Desc：推荐Fragment子页面：精选、男频、女频、完本
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/10/26 0026 11:14
 */
class ScrollWebFragment : Fragment(), View.OnClickListener {

    var url: String? = null
    private var loadingPage: LoadingPage? = null
    private var customWebClient: CustomWebClient? = null
    private var customChangeListener: ScrollWebView.ScrollChangeListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = this.arguments
        if (bundle != null) {
            this.url = bundle.getString("url")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.webview_scroll_layout, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AppUtils.disableAccessibility(requireContext())
        initView()
        requestWebViewData(url)
    }

    @SuppressLint("JavascriptInterface", "AddJavascriptInterface")
    private fun initView() {

        web_content_view?.setLayerType(View.LAYER_TYPE_NONE, null)

        loadingPage = LoadingPage(requireActivity(), fl_content_layout)
        //            //父布局为scroll时，loading视图高度为包裹内容，这里手动给它赋值，高度按照推荐页内容调整
        //            底部导航栏，顶部搜索栏、tab栏等高度和margin
        //            loadingPage.getLayoutParams().height =
        //                    getContext().getResources().getDisplayMetrics()
        //                            .heightPixels - AppUtils.dip2px(context, 36f + 34f + 50f + 13f);

        customWebClient = CustomWebClient(requireContext(), web_content_view)

        customWebClient?.initWebViewSetting()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            web_content_view?.settings?.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        web_content_view?.webViewClient = customWebClient


        if (customChangeListener != null) {
            web_content_view.insertScrollChangeListener(customChangeListener)
        }

        web_content_view?.addJavascriptInterface(object : JSInterfaceObject(requireActivity()) {

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


        }, "J_search")

        web_content_view?.addJavascriptInterface(JsPositionInterface(), "J_position")

    }


    private fun requestWebViewData(url: String?) {
        startLoadingWebViewData(url)
        initWebViewCallback()
    }

    private fun startLoadingWebViewData(url: String?) {
        if (web_content_view == null) {
            return
        }
        web_content_view?.post {
            handleLoadingWebViewData(url)
        }
    }


    private fun handleLoadingWebViewData(url: String?) {
        customWebClient?.initParameter()

        if (url != null && url.isNotEmpty()) {
            try {
                web_content_view?.loadUrl(url)
            } catch (exception: NullPointerException) {
                exception.printStackTrace()
                requireActivity().finish()
            }
        }
    }


    private fun initWebViewCallback() {
        if (web_content_view == null) {
            return
        }

        if (customWebClient != null) {
            customWebClient?.setLoadingWebViewStart {
                Logger.e("LoadingWebView: $it")
            }

            customWebClient?.setLoadingWebViewError {
                loadingPage?.onErrorVisable()
            }

            customWebClient?.setLoadingWebViewFinish {
                loadingPage?.onSuccessGone()
            }
        }


        loadingPage?.setReloadAction(LoadingPage.reloadCallback {
            customWebClient?.initParameter()
            web_content_view?.reload()
        })


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
        web_content_view?.clearCache(true) //清空缓存
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fl_content_layout?.removeView(web_content_view)
            web_content_view?.stopLoading()
            web_content_view?.removeAllViews()
            web_content_view?.destroy()
        } else {
            web_content_view?.stopLoading()
            web_content_view?.removeAllViews()
            web_content_view?.destroy()
            fl_content_layout?.removeView(web_content_view)
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
                val scale = web_content_view.resources.displayMetrics.widthPixels / viewWidth

                web_content_view.insertProhibitSlideArea(RectF(
                        x.toFloat() * scale, y.toFloat() * scale, (x.toFloat() + viewWidth) * scale,
                        (y.toFloat() + viewHeight) * scale))
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }
}
