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
import com.dingyue.contract.CommonContract
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dingyue.contract.web.CustomWebClient
import com.dingyue.contract.web.JSInterfaceObject
import com.google.gson.Gson
import com.intelligent.reader.BuildConfig
import com.intelligent.reader.R
import com.intelligent.reader.activity.SearchBookActivity
import com.intelligent.reader.activity.SettingActivity
import com.intelligent.reader.app.BookApplication
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.zsmfqbxs.view_home_header.*
import kotlinx.android.synthetic.zsmfqbxs.webview_layout.*
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.utils.AppUtils
import java.util.*

open class WebViewFragment : Fragment(), View.OnClickListener {

    var url: String? = ""
    private var type: String? = null

    private var customWebClient: CustomWebClient? = null
    private var loadingpage: LoadingPage? = null

    private var handler: Handler? = null
    private var isPrepared: Boolean = false
    private var isVisibled: Boolean = false
    private var isFirstVisible = true
    private var bottomType: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = Handler()
        val bundle = this.arguments
        if (bundle != null) {
            this.url = bundle.getString("url")
            this.type = bundle.getString("type")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.webview_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppUtils.disableAccessibility(requireActivity())

        initView()
        isPrepared = true
        if (type != null && (type == "recommendMan" || type == "recommendWoman"
                || type == "recommendFinish")) {
            lazyLoad()
        } else {
            if (!TextUtils.isEmpty(url)) {
                loadWebData(url!!)
            }
        }
    }

    fun setTitle(title: String, logBottomType: Int) {
        if (txt_header_title != null) {
            txt_header_title!!.text = title
        }
        bottomType = logBottomType
    }

    @SuppressLint("JavascriptInterface", "AddJavascriptInterface")
    private fun initView() {

        web_content_view?.setLayerType(View.LAYER_TYPE_NONE, null)

        if ("recommend" == type || "recommendMan" == type || "recommendWoman" == type || "recommendFinish" == type) {
            rl_home_header.visibility = View.GONE
        } else {
            rl_home_header.visibility = View.VISIBLE
        }

        img_header_setting.setOnClickListener {
            fp_header_point?.visibility = View.GONE
            val parameter = HashMap<String, String>()
            if ("rank" == type) {
                parameter.put("pk", "榜单")
            } else if ("category" == type) {
                parameter.put("pk", "分类")
            }
            StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                    StartLogClickUtil.PAGE_HOME, StartLogClickUtil.ACTION_HOME_PERSONAL, parameter)

            startActivity(Intent(requireActivity(), SettingActivity::class.java))
        }
        img_header_search.setOnClickListener {

            RouterUtil.navigation(requireActivity(),
                    RouterConfig.SEARCH_BOOK_ACTIVITY)
            when (bottomType) {
                2 -> StartLogClickUtil.upLoadEventLog(requireActivity(),
                        StartLogClickUtil.RECOMMEND_PAGE, StartLogClickUtil.QG_TJY_SEARCH)
                3 -> StartLogClickUtil.upLoadEventLog(requireActivity(), StartLogClickUtil.TOP_PAGE,
                        StartLogClickUtil.QG_BDY_SEARCH)
                4 -> StartLogClickUtil.upLoadEventLog(requireActivity(),
                        StartLogClickUtil.CLASS_PAGE, StartLogClickUtil.QG_FL_SEARCH)
                else -> StartLogClickUtil.upLoadEventLog(requireActivity(), StartLogClickUtil.MAIN_PAGE,
                        StartLogClickUtil.SEARCH)
            }
        }

        img_header_cache.setOnClickListener {
            RouterUtil.navigation(requireActivity(), RouterConfig.DOWNLOAD_MANAGER_ACTIVITY)

            val parameter = HashMap<String, String>()
            if ("rank" == type) {
                parameter.put("pk", "榜单")
            } else if ("category" == type) {
                parameter.put("pk", "分类")
            }
            StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                    StartLogClickUtil.PAGE_HOME, StartLogClickUtil.CACHEMANAGE, parameter)
        }

        loadingpage = LoadingPage(requireActivity(), rl_web_content)

        customWebClient = CustomWebClient(requireContext(), web_content_view)

        customWebClient?.initWebViewSetting()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            web_content_view?.settings?.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        web_content_view?.webViewClient = customWebClient

        web_content_view?.addJavascriptInterface(object : JSInterfaceObject(requireActivity()) {

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
                                    redirect.from == "ranking" -> bundle.putString("from", "rank")
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


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (userVisibleHint) {
            isVisibled = true
            onVisible()
        } else {
            isVisibled = false
            onInvisible()
        }
    }

    private fun onVisible() {
        if (type != null) {
            if (type == "rank") {//榜单
                notifyWebLog()//通知 H5 打点
            }
            if (type == "recommend") {//推荐
                notifyWebLog()
            }
            if (type == "recommendMan" || type == "recommendWoman"
                    || type == "recommendFinish") {//分类-女频
                lazyLoad()
            }
        }
    }

    private fun onInvisible() {}

    private fun notifyWebLog() {
        if (!isVisibled || !isPrepared) {
            return
        }
    }

    private fun lazyLoad() {
        if (!isVisibled || !isPrepared || !isFirstVisible) {
            return
        }
        if (!TextUtils.isEmpty(url)) {
            loadWebData(url!!)
        }
        isFirstVisible = false
    }

    fun loadWebData(url: String) {
        startLoading(handler, url)
        webViewCallback()
    }

    private fun startLoading(handler: Handler?, url: String) {
        if (web_content_view == null) {
            return
        }

        handler?.post { loadingData(url) } ?: loadingData(url)
    }

    private fun loadingData(url: String) {
        customWebClient?.initParameter()
        if (!TextUtils.isEmpty(url) && web_content_view != null) {
            try {
                web_content_view?.loadUrl(url)
            } catch (exception: NullPointerException) {
                exception.printStackTrace()
                requireActivity().finish()
            }

        }
    }

    private fun webViewCallback() {
        if (web_content_view == null) {
            return
        }

        customWebClient?.setLoadingWebViewStart { url -> Logger.e("开始加载WebView: " + url) }

        customWebClient?.setLoadingWebViewError {
            if (loadingpage != null) {
                loadingpage!!.onErrorVisable()
            }
        }

        customWebClient?.setLoadingWebViewFinish {
            if (loadingpage != null) {
                loadingpage!!.onSuccessGone()
            }
        }

        loadingpage?.setReloadAction(LoadingPage.reloadCallback {
            customWebClient?.initParameter()
            web_content_view?.reload()
        })

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.content_head_search -> {
                val intent = Intent(requireActivity(), SearchBookActivity::class.java)
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            R.id.content_download_manage -> try {
                RouterUtil.navigation(requireActivity(),
                        RouterConfig.DOWNLOAD_MANAGER_ACTIVITY)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        web_content_view?.clearCache(true)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            rl_web_content?.removeView(web_content_view)
            web_content_view?.stopLoading()
            web_content_view?.settings?.javaScriptEnabled = false
            web_content_view?.clearHistory()
            web_content_view?.removeAllViews()
            web_content_view?.destroy()
        } else {
            web_content_view?.stopLoading()
            web_content_view?.settings?.javaScriptEnabled = false
            web_content_view?.clearHistory()
            web_content_view?.removeAllViews()
            web_content_view?.destroy()
            rl_web_content?.removeView(web_content_view)
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