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
import com.alibaba.android.arouter.facade.annotation.Route
import com.baidu.mobstat.StatService
import com.ding.basic.net.api.service.RequestService
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.google.gson.Gson
import com.intelligent.reader.R
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.qbmfrmxs.act_tabulation.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.bean.PagerDesc
import net.lzbook.kit.ui.activity.base.FrameActivity
import net.lzbook.kit.ui.widget.LoadingPage
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.oneclick.OneClickUtil
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.runOnMain
import net.lzbook.kit.utils.uiThread
import net.lzbook.kit.utils.web.CustomWebClient
import net.lzbook.kit.utils.web.JSInterfaceObject
import net.lzbook.kit.utils.webview.UrlUtils
import java.util.*

/**
 * WebView二级页面
 */
@Route(path = RouterConfig.TABULATION_ACTIVITY)
class TabulationActivity : FrameActivity(), View.OnClickListener {

    private var handler: Handler = Handler()

    private var currentUrl: String? = null
    private var currentTitle: String? = null

    private var fromType: String? = null

    private var customWebClient: CustomWebClient? = null

    private var urls: ArrayList<String>? = null
    private var names: ArrayList<String>? = null

    private var loadingPage: LoadingPage? = null


    private var backClickCount: Int = 0

    private var isSupport = true

    private var mPagerDesc: PagerDesc? = null

    private var h5Margin: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.act_tabulation)

        urls = ArrayList()
        names = ArrayList()

        val intent = intent

        initIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        initIntent(intent)
    }

    private fun initIntent(intent: Intent?) {
        if (intent != null) {

            if (intent.hasExtra("url")) {
                currentUrl = intent.getStringExtra("url")

                if (currentUrl != null && currentUrl!!.isNotEmpty()) {
                    urls?.add(currentUrl!!)
                }
            }

            if (intent.hasExtra("title")) {
                currentTitle = intent.getStringExtra("title")

                if (currentTitle != null && currentTitle!!.isNotEmpty()) {
                    names?.add(currentTitle!!)
                }
            }
        }

        fromType = SPUtils.getDefaultSharedString(SPKey.HOME_FINDBOOK_SEARCH, "other")

        initView()

        initJSHelp()

        if (!TextUtils.isEmpty(currentUrl)) {
            loadWebViewData(currentUrl, currentTitle)
        }
    }

    @SuppressLint("JavascriptInterface", "AddJavascriptInterface")
    private fun initView() {

        initListener()

        //判断是否是作者主页
        if (currentUrl != null && currentUrl!!.contains(RequestService.AUTHOR_V4)) {
            img_tabulation_search?.visibility = View.GONE
        } else {
            img_tabulation_search?.visibility = View.VISIBLE
        }

        rl_tabulation_main?.setLayerType(View.LAYER_TYPE_NONE, null)

        loadingPage = LoadingPage(this, rl_tabulation_main, LoadingPage.setting_result)

        if (wv_tabulation_result != null) {
            customWebClient = CustomWebClient(this, wv_tabulation_result)
        }

        customWebClient?.initWebViewSetting()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            wv_tabulation_result?.settings?.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        wv_tabulation_result?.webViewClient = customWebClient

    }

    private fun initListener() {
        if (!TextUtils.isEmpty(currentTitle)) {
            txt_tabulation_title?.text = currentTitle
        }

        img_tabulation_back?.setOnClickListener(this)

        img_tabulation_search?.setOnClickListener(this)

        addTouchListener()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.img_tabulation_back -> {
                val data = HashMap<String, String>()
                data["type"] = "1"
                when (fromType) {

                    "class" -> {
                        if (currentTitle != null) {
                            data["firstclass"] = currentTitle!!
                        }
                        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTCLASS_PAGE, StartLogClickUtil.BACK, data)
                    }

                    "top" -> {
                        if (currentTitle != null) {
                            data["firsttop"] = currentTitle!!
                        }
                        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTTOP_PAGE, StartLogClickUtil.BACK, data)
                    }

                    "recommend" -> {
                        if (currentTitle != null) {
                            data["firstrecommend"] = currentTitle!!
                        }
                        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTRECOMMEND_PAGE, StartLogClickUtil.BACK, data)
                    }

                    "author" -> StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.AUTHORPAGE_PAGE, StartLogClickUtil.BACK, data)
                }
                clickBackBtn()
            }
            R.id.img_tabulation_search -> {
                val postData = HashMap<String, String>()

                when (fromType) {
                    "class" -> {
                        if (currentTitle != null) {
                            postData["firstclass"] = currentTitle!!
                        }
                        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTCLASS_PAGE, StartLogClickUtil.SEARCH, postData)
                    }

                    "top" -> {
                        if (currentTitle != null) {
                            postData["firsttop"] = currentTitle!!
                        }
                        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTTOP_PAGE, StartLogClickUtil.SEARCH, postData)
                    }
                    "recommend" -> {
                        if (currentTitle != null) {
                            postData["firstrecommend"] = currentTitle!!
                        }
                        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTRECOMMEND_PAGE, StartLogClickUtil.SEARCH, postData)
                    }
                }

                RouterUtil.navigation(this, RouterConfig.SEARCH_BOOK_ACTIVITY)
            }
        }
    }


    override fun onBackPressed() {
        if (urls != null) {
            if (urls!!.size - backClickCount <= 1) {
                super.onBackPressed()
            } else {
                backClickCount++

                val index = urls!!.size - 1 - backClickCount

                currentUrl = urls!![index]
                currentTitle = names!![index]

                loadWebViewData(currentUrl, currentTitle)
            }
        }
    }

    private fun loadWebViewData(url: String?, title: String?) {
        var requestUrl = url

        insertTitle(title)

        var parameters: Map<String, String>? = null

        if (requestUrl != null) {
            val array = requestUrl.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            requestUrl = array[0]

            if (array.size == 2) {
                parameters = UrlUtils.getUrlParams(array[1])
            } else if (array.size == 1) {
                parameters = HashMap()
            }
            requestUrl = UrlUtils.buildWebUrl(requestUrl, parameters)
        }

        handleLoadWebViewAction(requestUrl)

        initWebViewCallback()
    }

    private fun handleLoadWebViewAction(url: String?) {
        if (wv_tabulation_result == null) {
            return
        }

        handler.post { loadingData(url) }
    }

    private fun loadingData(url: String?) {
        if (customWebClient != null) {
            customWebClient?.initParameter()
        }

        if (!TextUtils.isEmpty(url) && wv_tabulation_result != null) {
            try {
                wv_tabulation_result?.loadUrl(url)
            } catch (e: NullPointerException) {
                e.printStackTrace()
                this.finish()
            }
        }
    }

    private fun initWebViewCallback() {
        customWebClient?.setLoadingWebViewStart { url -> Logger.i("LoadStartedAction: $url") }

        customWebClient?.setLoadingWebViewError {
            Logger.i("LoadErrorAction")
            loadingPage?.onErrorVisable()
        }

        customWebClient?.setLoadingWebViewFinish {
            Logger.i("LoadFinishAction")
            loadingPage?.onSuccessGone()
        }

        if (loadingPage != null) {
            loadingPage?.setReloadAction(LoadingPage.reloadCallback {

                customWebClient?.initParameter()
                wv_tabulation_result?.reload()
            })
        }
    }

    private fun insertTitle(name: String?) {
        uiThread { txt_tabulation_title?.text = name }
    }

    private fun clickBackBtn() {
        if (urls != null) {
            if (urls!!.size - backClickCount <= 1) {
                this@TabulationActivity.finish()
            } else {
                backClickCount++

                val index = urls!!.size - 1 - backClickCount

                currentUrl = urls!![index]
                currentTitle = names!![index]

                loadWebViewData(currentUrl, currentTitle)
            }
            return
        } else {
            finish()
        }
    }

    override fun supportSlideBack(): Boolean {
        return isSupport
    }


    @SuppressLint("AddJavascriptInterface")
    private fun initJSHelp() {

        wv_tabulation_result?.addJavascriptInterface(object : JSInterfaceObject(this@TabulationActivity) {

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

    private fun refreshTabulationContent(url: String?, title: String?) {
        currentUrl = url
        currentTitle = title

        urls?.add(currentUrl ?: "")
        names?.add(currentTitle ?: "")

        runOnMain {
            loadWebViewData(currentUrl, currentTitle)
        }
    }


    private fun isNeedInterceptSlide(): Boolean {
        val packageName = AppUtils.getPackageName()
        return (("cc.kdqbxs.reader" == packageName || "cc.quanbennovel" == packageName
                || "cn.txtkdxsdq.reader" == packageName) && !TextUtils.isEmpty(currentTitle)
                && (currentTitle!!.contains("男频") || currentTitle!!.contains("女频")))
    }


    private fun addTouchListener() {
        if (wv_tabulation_result != null && isNeedInterceptSlide()) {
            wv_tabulation_result.setOnTouchListener({ _, event ->
                val y = event.rawY
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (wv_tabulation_result != null) {
                            val loction = IntArray(2)
                            wv_tabulation_result.getLocationOnScreen(loction)
                            h5Margin = loction[1]
                        }
                        if (null != mPagerDesc) {
                            var top = mPagerDesc!!.top
                            var bottom = top + (mPagerDesc!!.bottom - mPagerDesc!!.top)
                            val metric = resources.displayMetrics
                            top = ((top * metric.density).toInt() + h5Margin).toFloat()
                            bottom = ((bottom * metric.density).toInt() + h5Margin).toFloat()
                            isSupport = !(y > top && y < bottom)
                        }
                    }
                    MotionEvent.ACTION_UP -> isSupport = true
                    MotionEvent.ACTION_MOVE -> {
                    }
                    else -> isSupport = true
                }
                false
            })
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

        handler.removeCallbacksAndMessages(null)


        wv_tabulation_result?.clearCache(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if (wv_tabulation_result?.parent != null) {
                (wv_tabulation_result?.parent as ViewGroup).removeView(wv_tabulation_result)
            }

            wv_tabulation_result?.stopLoading()
            wv_tabulation_result?.settings?.javaScriptEnabled = false
            wv_tabulation_result?.clearHistory()
            wv_tabulation_result?.removeAllViews()
            wv_tabulation_result?.destroy()
        } else {
            wv_tabulation_result?.stopLoading()
            wv_tabulation_result?.settings?.javaScriptEnabled = false
            wv_tabulation_result?.clearHistory()
            wv_tabulation_result?.removeAllViews()
            wv_tabulation_result?.destroy()

            if (wv_tabulation_result?.parent != null) {
                (wv_tabulation_result?.parent as ViewGroup).removeView(wv_tabulation_result)
            }
        }
    }
}