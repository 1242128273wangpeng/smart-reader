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
import com.ding.basic.net.api.service.RequestService
import com.dingyue.searchbook.SearchBookActivity
import com.google.gson.Gson
import com.intelligent.reader.R
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.qbmfkkydq.act_tabulation.*
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
import net.lzbook.kit.utils.web.CustomWebClient
import net.lzbook.kit.utils.web.JSInterfaceObject
import net.lzbook.kit.utils.webview.UrlUtils
import java.util.*


/**
 * Desc：WebView二级页面
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/10/26 0026 10:29
 */
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
            return (("cc.kdqbxs.reader" == packageName || "cc.quanbennovel" == packageName || "cn.txtkdxsdq.reader" == packageName)
                    && !TextUtils.isEmpty(this.title) && ((this.title?.contains("男频")
                    ?: false) || (this.title?.contains("女频") ?: false)))
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.act_tabulation)

        val intent = intent

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
        //离线消息 跳转到主页
        if (this.fromPush && ActivityLifecycleHelper.getActivities().size <= 1) {
            startActivity(Intent(this, SplashActivity::class.java))
        }
    }

    override fun supportSlideBack(): Boolean {
        return ActivityLifecycleHelper.getActivities().size > 1 && supportSlide
    }

    @SuppressLint("AddJavascriptInterface", "JavascriptInterface")
    private fun initParameter() {

        if (find_book_detail_title != null) {
            find_book_detail_title?.text = title ?: "列表"
        }

        find_book_detail_back.setOnClickListener {
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

        find_book_detail_search?.setOnClickListener {
            statisticsTabulationSearch()

            val intent = Intent()
            intent.setClass(this, SearchBookActivity::class.java)
            startActivity(intent)
        }

        insertTouchListener()


        //判断是否是作者主页
        if (url != null && url?.contains(RequestService.AUTHOR_h5.replace("{packageName}", AppUtils.getPackageName())) == true) {
            find_book_detail_search?.visibility = View.GONE
        } else {
            find_book_detail_search?.visibility = View.VISIBLE
        }

        find_book_detail_main?.setLayerType(View.LAYER_TYPE_NONE, null)


        loadingPage = LoadingPage(this, find_book_detail_main, LoadingPage.setting_result)

        if (rank_content != null) {
            customWebClient = CustomWebClient(this, rank_content)
        }

        customWebClient?.initWebViewSetting()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            rank_content?.settings?.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        rank_content?.webViewClient = customWebClient

        rank_content?.addJavascriptInterface(object : JSInterfaceObject(this@TabulationActivity) {

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
            "class" -> {
                data["firstclass"] = title
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTCLASS_PAGE, StartLogClickUtil.BACK, data)
            }

            "top" -> {
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

            "top" -> {
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
        if (rank_content != null && this.needInterceptSlide) {
            rank_content?.setOnTouchListener { v, event ->
                val y = event.rawY
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (rank_content != null) {
                            val location = IntArray(2)
                            rank_content?.getLocationOnScreen(location)
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


    private fun insertTabulationTitle(name: String?) {
        uiThread {
            find_book_detail_title?.text = name ?: "列表"
        }
    }

    private fun startLoadingWebViewData(url: String?) {
        if (rank_content == null) {
            return
        }

        rank_content?.post {
            handleLoadingWebViewData(url)
        }
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
                rank_content?.loadUrl(url)
            } catch (exception: Exception) {
                exception.printStackTrace()
                this.finish()
            }
        }
    }

    private fun initWebViewCallback() {
        if (find_book_detail_main == null) {
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
                requestWebViewPager(rank_content)
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
                rank_content?.reload()
            })
        }
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
        if (rank_content != null) {
            rank_content?.post {
                try {
                    rank_content?.loadUrl("javascript:refreshNew()")
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    finish()
                }
            }
        }
        StatService.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        StatService.onPause(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (rank_content != null) {
            rank_content?.clearCache(false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (rank_content?.parent != null) {
                    (rank_content?.parent as ViewGroup).removeView(rank_content)
                }
                // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
                rank_content?.stopLoading()
                rank_content?.settings?.javaScriptEnabled = false
                rank_content?.clearHistory()
                rank_content?.removeAllViews()
                rank_content?.destroy()
            } else {
                // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
                rank_content?.stopLoading()
                rank_content?.settings?.javaScriptEnabled = false
                rank_content?.clearHistory()
                rank_content?.removeAllViews()
                rank_content?.destroy()
                if (rank_content?.parent != null) {
                    (rank_content?.parent as ViewGroup).removeView(rank_content)
                }
            }
        }
    }

}