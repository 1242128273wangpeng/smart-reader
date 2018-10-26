package com.intelligent.reader.fragment.scroll

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.widget.FrameLayout
import com.intelligent.reader.BuildConfig
import com.intelligent.reader.R
import com.intelligent.reader.app.BookApplication
import com.intelligent.reader.fragment.WebViewFragment
import com.intelligent.reader.view.scroll.ScrollWebView
import com.orhanobut.logger.Logger
import net.lzbook.kit.ui.widget.LoadingPage
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.logger.AppLog
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.web.CustomWebClient
import net.lzbook.kit.utils.webview.JSInterfaceHelper
import java.lang.ref.WeakReference

/**
 * Desc：推荐Fragment子页面：精选、男频、女频、完本
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/10/26 0026 11:14
 */
class ScrollWebFragment : Fragment(), View.OnClickListener {

    var url: String? = null
    private var weakReference: WeakReference<Activity>? = null
    private var rootView: View? = null
    private var contentLayout: FrameLayout? = null
    private var contentView: ScrollWebView? = null
    private var customWebClient: CustomWebClient? = null
    private var jsInterfaceHelper: JSInterfaceHelper? = null
    private var fragmentCallback: WebViewFragment.FragmentCallback? = null
    private var loadingPage: LoadingPage? = null
    private var handler: Handler? = null

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        this.weakReference = WeakReference<Activity>(activity)
        this.fragmentCallback = activity as WebViewFragment.FragmentCallback?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = Handler()
        val bundle = this.arguments
        if (bundle != null) {
            this.url = bundle.getString("url")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        try {
            rootView = inflater.inflate(R.layout.webview_scroll_layout, container, false)
        } catch (e: InflateException) {
            e.printStackTrace()
        }

        if (weakReference != null) {
            AppUtils.disableAccessibility(weakReference!!.get())
        }

        return rootView
    }

    @SuppressLint("JavascriptInterface")
    private fun initView() {
        if (rootView != null) {
            contentLayout = view!!.findViewById(R.id.fl_content_layout)
            contentView = view!!.findViewById(R.id.web_content_view)
            contentView!!.setLayerType(View.LAYER_TYPE_NONE, null)
        }

        if (weakReference != null) {
            loadingPage = LoadingPage(weakReference!!.get(), contentLayout)
            //            //父布局为scroll时，loading视图高度为包裹内容，这里手动给它赋值，高度按照推荐页内容调整
            //            底部导航栏，顶部搜索栏、tab栏等高度和margin
            //            loadingPage.getLayoutParams().height =
            //                    getContext().getResources().getDisplayMetrics()
            //                            .heightPixels - AppUtils.dip2px(context, 36f + 34f + 50f + 13f);

        }

        if (contentView != null && context != null) {
            customWebClient = CustomWebClient(context, contentView)
        }

        if (contentView != null && customWebClient != null) {
            customWebClient?.initWebViewSetting()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                contentView!!.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
            contentView!!.webViewClient = customWebClient
        }

        if (contentView != null && context != null) {
            jsInterfaceHelper = JSInterfaceHelper(context, contentView)
        }

        if (jsInterfaceHelper != null && contentView != null) {
            contentView!!.addJavascriptInterface(jsInterfaceHelper, "J_search")
            contentView!!.addJavascriptInterface(JsPositionInterface(), "J_banner")
        }


        if (fragmentCallback != null && jsInterfaceHelper != null) {
            fragmentCallback!!.webJsCallback(jsInterfaceHelper!!)
        }


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        url?.let {
            loadWebData(it)
        }
    }

    fun loadWebData(url: String) {
        var urlNew = url
        AppLog.e("loadWebData url: " + url)
        if (fragmentCallback != null) {
            urlNew = fragmentCallback!!.startLoad(contentView, urlNew)
        }
        AppLog.e("loadWebData url2: " + urlNew)
        startLoading(handler, urlNew)
        initWebViewCallback()
    }

    private fun startLoading(handler: Handler?, url: String) {
        if (contentView == null) {
            return
        }

        handler?.post { loadingData(url) } ?: loadingData(url)
    }

    private fun loadingData(url: String) {
        customWebClient?.initParameter()
        if (!TextUtils.isEmpty(url) && contentView != null) {
            try {
                AppLog.e("WebViewFragment LoadingData ==> " + url)
                contentView!!.loadUrl(url)
            } catch (e: NullPointerException) {
                e.printStackTrace()
                weakReference!!.get()?.finish()
            }

        }
    }

    private fun initWebViewCallback() {
        if (rootView == null) {
            return
        }

        if (customWebClient != null) {
            customWebClient?.setLoadingWebViewStart {
                Logger.e("LoadingWebView: $it")
            }

            customWebClient?.setLoadingWebViewError {
                if (loadingPage != null) {
                    loadingPage?.onErrorVisable()
                }
            }

            customWebClient?.setLoadingWebViewFinish {
                if (loadingPage != null) {
                    loadingPage?.onSuccessGone()
                }
            }
        }


        loadingPage?.setReloadAction(LoadingPage.reloadCallback {
            if (customWebClient != null) {
                customWebClient?.initParameter()
            }
            contentView!!.reload()
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
        if (contentView != null) {
            contentView!!.clearCache(true) //清空缓存
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (contentLayout != null) {
                    contentLayout!!.removeView(contentView)

                }
                contentView!!.stopLoading()
                contentView!!.removeAllViews()
                contentView!!.destroy()
            } else {
                contentView!!.stopLoading()
                contentView!!.removeAllViews()
                contentView!!.destroy()
                if (contentLayout != null) {
                    contentLayout!!.removeView(contentView)
                }
            }
            contentView = null
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
        fun getH5ViewPagerInfo(x: String, y: String, width: String, height: String) {
            AppLog.e("jsPosition" + x + " " + y + " " +
                    width + " " + height + " " + contentView!!.scaleX + "  " + contentView!!.scaleY)
            try {
                val bWidht = java.lang.Float.parseFloat(width)
                val bHeight = java.lang.Float.parseFloat(height)
                val scale = contentView!!.resources.displayMetrics.widthPixels / (bWidht + 1)


                contentView!!.setBannerRect(RectF(
                        java.lang.Float.parseFloat(x), java.lang.Float.parseFloat(y), bWidht * scale,
                        bHeight * scale))
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }
}
