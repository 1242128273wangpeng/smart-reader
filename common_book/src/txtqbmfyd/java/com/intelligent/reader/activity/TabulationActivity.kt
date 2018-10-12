package com.intelligent.reader.activity


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import com.alibaba.android.arouter.facade.annotation.Route
import com.baidu.mobstat.StatService
import com.dingyue.contract.web.CustomWebClient
import com.dingyue.contract.web.JSInterfaceObject
import com.dingyue.searchbook.SearchBookActivity
import com.google.gson.Gson
import com.intelligent.reader.R
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.txtqbmfyd.act_tabulation.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.bean.PagerDesc
import net.lzbook.kit.ui.activity.base.FrameActivity
import net.lzbook.kit.ui.widget.LoadingPage
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.IS_FROM_PUSH
import net.lzbook.kit.utils.oneclick.OneClickUtil
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.runOnMain
import net.lzbook.kit.utils.swipeback.ActivityLifecycleHelper
import net.lzbook.kit.utils.uiThread
import net.lzbook.kit.utils.webview.UrlUtils
import java.util.*


@Route(path = RouterConfig.TABULATION_ACTIVITY)
class TabulationActivity : FrameActivity() {

    private var urls = ArrayList<String?>()
    private var titles = ArrayList<String?>()

    private var url: String? = null
    private var title: String? = null

    private var fromPush = false

    private var customWebClient: CustomWebClient? = null

    private var pagerDesc: PagerDesc? = null

    private var margin: Int = 0

    private var backClickCount: Int = 0

    private var loadingPage: LoadingPage? = null

    private var fromType = ""

    private var supportSlide = true

    private val WEB_AUTHOR = "/h5/{packageName}/author"

    private var handler: Handler? = Handler()

    private val isNeedInterceptSlide: Boolean
        get() {
            val packageName = AppUtils.getPackageName()
            return ("cc.kdqbxs.reader" == packageName || "cc.quanbennovel" == packageName) &&
                    !TextUtils.isEmpty(title) && ((title?.contains("男频")
                    ?: false) || (title?.contains("女频") ?: false))
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_tabulation)

        if (intent != null) {
            url = intent.getStringExtra("url")
            urls.add(url)

            title = intent.getStringExtra("title")
            titles.add(title)

            fromType = intent.getStringExtra("from")

            fromPush = intent.getBooleanExtra(IS_FROM_PUSH, false)
        }

        if (url == null || title == null) {
            onBackPressed()
            return
        }

        initParameter()

        if (url != null && url?.isNotEmpty() == true) {
            requestWebViewData(url, title)
        }
    }

    override fun onResume() {
        super.onResume()
        StatService.onResume(this)

    }

    override fun onPause() {
        super.onPause()
        StatService.onPause(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (web_tabulation_content != null) {
            web_tabulation_content?.clearCache(false)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (web_tabulation_content?.parent != null) {
                    (web_tabulation_content?.parent as ViewGroup).removeView(web_tabulation_content)
                }

                web_tabulation_content?.stopLoading()
                web_tabulation_content?.settings?.javaScriptEnabled = false
                web_tabulation_content?.clearHistory()
                web_tabulation_content?.removeAllViews()
                web_tabulation_content?.destroy()
            } else {
                web_tabulation_content?.stopLoading()
                web_tabulation_content?.settings?.javaScriptEnabled = false
                web_tabulation_content?.clearHistory()
                web_tabulation_content?.removeAllViews()
                web_tabulation_content?.destroy()

                if (web_tabulation_content?.parent != null) {
                    (web_tabulation_content?.parent as ViewGroup).removeView(web_tabulation_content)
                }
            }
        }
    }

    override fun onBackPressed() {
        if (urls.size - backClickCount <= 1) {
            super.onBackPressed()
        } else {
            backClickCount++
            val index = urls.size - 1 - backClickCount

            url = urls[index]
            title = titles[index]

            requestWebViewData(url, title)
        }
    }

    override fun finish() {
        super.finish()

        if (fromPush && ActivityLifecycleHelper.getActivities().size <= 1) {
            startActivity(Intent(this, SplashActivity::class.java))
        }
    }

    override fun supportSlideBack(): Boolean {
        return ActivityLifecycleHelper.getActivities().size > 1 && supportSlide
    }

    /***
     * 初始化参数
     * **/
    @SuppressLint("AddJavascriptInterface", "JavascriptInterface")
    private fun initParameter() {

        if (txt_tabulation_header_title != null) {
            txt_tabulation_header_title?.text = title ?: "列表"
        }

        if (img_tabulation_header_back != null) {
            img_tabulation_header_back?.setOnClickListener {

                statisticsTabulationBack()

                if (urls.size - backClickCount <= 1) {
                    this@TabulationActivity.finish()
                } else {
                    backClickCount++
                    val index = urls.size - 1 - backClickCount

                    url = urls[index]
                    title = titles[index]

                    requestWebViewData(url, title)
                }
            }
        }

        img_tabulation_header_search?.setOnClickListener {

            statisticsTabulationSearch()

            val intent = Intent()
            intent.setClass(this, SearchBookActivity::class.java)
            startActivity(intent)
        }

        insertTouchListener()

        //判断是否是作者主页
        if (url != null && url?.contains(WEB_AUTHOR.replace("{packageName}", AppUtils.getPackageName())) == true) {
            img_tabulation_header_search?.visibility = View.GONE
        } else {
            img_tabulation_header_search?.visibility = View.VISIBLE
        }

        rl_tabulation_root?.setLayerType(View.LAYER_TYPE_NONE, null)

        loadingPage = LoadingPage(this, rl_tabulation_root, LoadingPage.setting_result)

        if (web_tabulation_content != null) {
            customWebClient = CustomWebClient(this, web_tabulation_content)
        }

        customWebClient?.initWebViewSetting()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            web_tabulation_content?.settings?.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        web_tabulation_content?.webViewClient = customWebClient

        web_tabulation_content?.addJavascriptInterface(object : JSInterfaceObject(this@TabulationActivity) {

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
                            try {
                                refreshTabulationContent(redirect.url, redirect.title)
                            } catch (exception: Exception) {
                                exception.printStackTrace()
                            }
                        }
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                }
            }

        }, "J_search")
    }

    /***
     *
     * **/
    private fun refreshTabulationContent(url: String?, title: String?) {
        this.url = url
        this.title = title

        urls.add(this.url)
        titles.add(title)

        runOnMain {
            requestWebViewData(this.url, this.title)
        }
    }

    /***
     * 统计列表返回点击事件
     * **/
    private fun statisticsTabulationBack() {
        val data = HashMap<String, String?>()
        data["type"] = "1"

        when (fromType) {
            "category" -> {
                data["FIRSTCLASS"] = title
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTCLASS_PAGE, StartLogClickUtil.BACK, data)
            }

            "rank" -> {
                data["FIRSTTOP"] = title
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTTOP_PAGE, StartLogClickUtil.BACK, data)
            }

            "recommend" -> {
                data["FIRSTRECOMMEND"] = title
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTRECOMMEND_PAGE, StartLogClickUtil.BACK, data)
            }

            "authorType" -> {
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.AUTHORPAGE_PAGE, StartLogClickUtil.BACK, data)
            }
        }
    }

    /***
     * 统计列表搜索点击事件
     * **/
    private fun statisticsTabulationSearch() {
        val data = HashMap<String, String?>()

        when (fromType) {
            "category" -> {
                data["firstclass"] = title
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTCLASS_PAGE, StartLogClickUtil.SEARCH, data)
            }

            "rank" -> {
                data["firsttop"] = title
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTTOP_PAGE, StartLogClickUtil.SEARCH, data)
            }

            "recommend" -> {
                data["firstrecommend"] = title
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTRECOMMEND_PAGE, StartLogClickUtil.SEARCH, data)
            }
            "authorType" -> {
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.AUTHORPAGE_PAGE, StartLogClickUtil.SEARCH, data)
            }
        }
    }

    /***
     * 对整个WebView页面添加点击监听
     * **/
    private fun insertTouchListener() {
        if (isNeedInterceptSlide && web_tabulation_content != null) {
            web_tabulation_content?.setOnTouchListener { _, event ->
                val y = event.rawY
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (web_tabulation_content != null) {
                            val location = IntArray(2)
                            web_tabulation_content?.getLocationOnScreen(location)
                            margin = location[1]
                        }

                        if (null != pagerDesc) {
                            val displayMetrics = resources.displayMetrics

                            var top = pagerDesc?.top ?: 0f
                            var bottom = top + ((pagerDesc?.bottom ?: 0f) - (pagerDesc?.top
                                    ?: 0f))

                            top = ((top * displayMetrics.density).toInt() + margin).toFloat()
                            bottom = ((bottom * displayMetrics.density).toInt() + margin).toFloat()

                            supportSlide = !(y > top && y < bottom)
                        }
                    }
                    MotionEvent.ACTION_UP -> supportSlide = true

                    MotionEvent.ACTION_MOVE -> {
                    }
                    else -> supportSlide = true
                }
                false
            }
        }
    }

    /***
     * 请求WebView数据
     * **/
    private fun requestWebViewData(url: String?, name: String?) {
        var request = url
        var parameters: Map<String, String>? = null
        if (request != null) {
            val array = request.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            request = array[0]
            if (array.size == 2) {
                parameters = UrlUtils.getUrlParams(array[1])
            } else if (array.size == 1) {
                parameters = HashMap()
            }

            request = UrlUtils.buildWebUrl(request, parameters)
        }

        insertTabulationTitle(name)

        startLoadingWebViewData(request)

        initWebViewCallback()
    }

    /***
     * 设置标题
     * **/
    private fun insertTabulationTitle(name: String?) {
        uiThread {
            txt_tabulation_header_title?.text = name ?: "列表"
        }
    }

    /***
     * 开始请求WebView数据
     * **/
    private fun startLoadingWebViewData(url: String?) {
        if (web_tabulation_content == null) {
            return
        }

        handler?.post { handleLoadingWebViewData(url) } ?: handleLoadingWebViewData(url)
    }

    /***
     * 处理WebView请求
     * **/
    private fun handleLoadingWebViewData(url: String?) {
        if (customWebClient != null) {
            customWebClient?.initParameter()
        }

        if (url != null && url.isNotEmpty()) {
            try {
                web_tabulation_content?.loadUrl(url)
            } catch (exception: Exception) {
                exception.printStackTrace()
                this.finish()
            }
        }
    }

    /***
     * 初始化WebView请求回调
     * **/
    private fun initWebViewCallback() {
        if (web_tabulation_content == null) {
            return
        }

        if (customWebClient != null) {
            customWebClient?.setLoadingWebViewStart {
                Logger.e("WebView页面开始加载 $it")
            }

            customWebClient?.setLoadingWebViewFinish {
                Logger.e("WebView页面加载结束！")
                if (loadingPage != null) {
                    loadingPage?.onSuccessGone()
                }
                requestWebViewPager(web_tabulation_content)
            }

            customWebClient?.setLoadingWebViewError {
                Logger.e("WebView页面加载异常！")
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
                web_tabulation_content?.reload()
            })
        }
    }

    /***
     * 获取H5页面信息
     * **/
    private fun requestWebViewPager(web_tabulation_content: WebView?) {
        if (isNeedInterceptSlide && web_tabulation_content != null) {
            web_tabulation_content.loadUrl("javascript:getViewPagerInfo()")
        }
    }
}
