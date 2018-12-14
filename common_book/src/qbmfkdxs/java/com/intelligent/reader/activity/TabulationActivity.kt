package com.intelligent.reader.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import com.alibaba.android.arouter.facade.annotation.Route
import com.baidu.mobstat.StatService
import com.ding.basic.net.Config
import com.ding.basic.net.api.service.RequestService
import com.dingyue.searchbook.activity.SearchBookActivity
import com.google.gson.Gson
import com.intelligent.reader.R
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.qbmfkdxs.act_tabulation.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.bean.PagerDesc
import net.lzbook.kit.ui.activity.base.FrameActivity
import net.lzbook.kit.ui.widget.LoadingPage
import net.lzbook.kit.utils.*
import net.lzbook.kit.utils.oneclick.OneClickUtil
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.swipeback.ActivityLifecycleHelper
import net.lzbook.kit.utils.web.CustomWebClient
import net.lzbook.kit.utils.web.JSInterfaceObject
import java.util.*


@Route(path = RouterConfig.TABULATION_ACTIVITY)
class TabulationActivity : FrameActivity() {

    private var backClickCount: Int = 0

    private var loadingPage: LoadingPage? = null

    private var customWebClient: CustomWebClient? = null

    private var fromType = ""

    private var pagerDesc: PagerDesc? = null

    private var margin: Int = 0

    private var supportSlide = true

    private var urls = ArrayList<String?>()
    private var titles = ArrayList<String?>()

    private var url: String? = null
    private var title: String? = null

    private var fromPush = false


    private val needInterceptSlide: Boolean
        get() {
            val packageName = AppUtils.getPackageName()
            return (("cc.quanbennovel" == packageName || "cn.txtkdxsdq.reader" == packageName)
                    && !TextUtils.isEmpty(this.title) && ((this.title?.contains("男频")
                    ?: false) || (this.title?.contains("女频") ?: false)))
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.act_tabulation)

        val intent = intent
        AppUtils.disableAccessibility(this)
        if (intent != null) {
            this.url = intent.getStringExtra("url")
            urls.add(this.url)

            this.title = intent.getStringExtra("title")
            this.titles.add(this.title)

            fromType = intent.getStringExtra("from")

            this.fromPush = intent.getBooleanExtra(IS_FROM_PUSH, false)
        }

        if (this.url == null || this.title == null) {
            onBackPressed()
            return
        }

        initParameter()

        if (url != null && url?.isNotEmpty() == true) {
            requestWebViewData(url, title)
        }
    }


    override fun onBackPressed() {
        if (urls.size - backClickCount <= 1) {
            super.onBackPressed()
        } else {
            backClickCount++

            val index = urls.size - 1 - backClickCount

            this.url = urls[index]
            this.title = titles[index]

            requestWebViewData(this.url, this.title)
        }
    }

    override fun finish() {
        super.finish()
        if (this.fromPush && ActivityLifecycleHelper.getActivities().size <= 1) {
            startActivity(Intent(this, SplashActivity::class.java))
        }
    }

    override fun supportSlideBack(): Boolean {
        return ActivityLifecycleHelper.getActivities().size > 1 && supportSlide
    }

    @SuppressLint("AddJavascriptInterface", "JavascriptInterface")
    private fun initParameter() {

        if (txt_tabulation_header_title != null) {
            txt_tabulation_header_title?.text = title ?: "列表"
        }

        img_tabulation_header_back.setOnClickListener {
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

        img_tabulation_header_search?.setOnClickListener {
            statisticsTabulationSearch()

            val intent = Intent()
            intent.setClass(this, SearchBookActivity::class.java)
            startActivity(intent)
        }

        insertTouchListener()


        //判断是否是作者主页
        if (url != null && url?.contains("author") == true) {
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
                                refreshTabulationContent(Config.webViewBaseHost + redirect.url, redirect.title)
                            } catch (exception: Exception) {
                                exception.printStackTrace()
                            }
                        }
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                }
            }

            @JavascriptInterface
            override fun handleBackAction() {
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

            @JavascriptInterface
            override fun hideWebViewLoading() {
                runOnMain {
                    loadingPage?.onSuccessGone()
                }
            }

            @JavascriptInterface
            override fun handleWebRequestResult(method: String?) {
                if (null != web_tabulation_content) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        web_tabulation_content.evaluateJavascript(method) { value -> Logger.e("ReceivedValue: $value") }
                    } else {
                        web_tabulation_content.loadUrl(method)
                    }
                }
            }

        }, "J_search")
    }


    /***
     * 刷新数据
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
                data["firstclass"] = title
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTCLASS_PAGE, StartLogClickUtil.BACK, data)
            }

            "ranking" -> {
                data["firsttop"] = title
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTTOP_PAGE, StartLogClickUtil.BACK, data)
            }

            "recommend" -> {
                data["firstrecommend"] = title
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
            "class" -> {
                data["firstclass"] = title
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTCLASS_PAGE, StartLogClickUtil.SEARCH, data)
            }

            "ranking" -> {
                data["firsttop"] = title
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTTOP_PAGE, StartLogClickUtil.SEARCH, data)
            }

            "recommend" -> {
                data["firstrecommend"] = title
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTRECOMMEND_PAGE, StartLogClickUtil.SEARCH, data)
            }
        }
    }

    private fun insertTouchListener() {
        if (web_tabulation_content != null && this.needInterceptSlide) {
            web_tabulation_content?.setOnTouchListener { v, event ->
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

                            top = ((top * displayMetrics.density).toInt() + this.margin).toFloat()
                            bottom = ((bottom * displayMetrics.density).toInt() + this.margin).toFloat()

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

    private fun requestWebViewData(url: String?, name: String?) {
        insertTabulationTitle(name)

        startLoadingWebViewData(url)

        initWebViewCallback()
    }

    private fun insertTabulationTitle(name: String?) {
        uiThread {
            txt_tabulation_header_title?.text = name ?: "列表"
        }
    }

    private fun startLoadingWebViewData(url: String?) {
        if (web_tabulation_content == null) {
            return
        }

        web_tabulation_content?.post {
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
                web_tabulation_content?.loadUrl(url)
            } catch (exception: Exception) {
                exception.printStackTrace()
                this.finish()
            }
        }
    }

    private fun initWebViewCallback() {
        if (rl_tabulation_root == null) {
            return
        }

        if (customWebClient != null) {
            customWebClient?.setLoadingWebViewStart {
                Logger.e("WebView页面开始加载 $it")
            }

            customWebClient?.setLoadingWebViewFinish {
                Logger.e("WebView页面加载结束！")
                if (NetWorkUtils.isNetworkAvailable(this)) {
                    loadingPage?.onSuccessGone()
                    requestWebViewPager(web_tabulation_content)
                } else {
                    loadingPage?.onErrorVisable()
                }
            }

            customWebClient?.setLoadingWebViewError {
                Logger.e("WebView页面加载异常！")
                loadingPage?.onErrorVisable()
            }
        }


        loadingPage?.setReloadAction(LoadingPage.reloadCallback {
            customWebClient?.initParameter()
            web_tabulation_content?.reload()
        })
    }

    /***
     * 获取H5页面信息
     * **/
    private fun requestWebViewPager(rank_content: WebView?) {
        if (needInterceptSlide && rank_content != null) {
            rank_content.loadUrl("javascript:getViewPagerInfo()")
        }
    }


    override fun shouldLightStatusBase(): Boolean {
        return if ("cc.quanben.novel" == AppUtils.getPackageName()) {
            true
        } else super.shouldLightStatusBase()
    }

    override fun onResume() {
        super.onResume()
        StatService.onResume(this)
        if (web_tabulation_content != null) {
            web_tabulation_content?.post {
                try {
                    web_tabulation_content.loadUrl("javascript:refreshNew()")
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    finish()
                }
            }
        }
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
}