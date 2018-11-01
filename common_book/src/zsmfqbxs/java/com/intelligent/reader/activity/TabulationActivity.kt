package com.intelligent.reader.activity


import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
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
import com.ding.basic.Config
import com.ding.basic.bean.Book
import com.ding.basic.bean.Chapter
import com.ding.basic.request.RequestService
import com.dingyue.contract.CommonContract
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.util.SharedPreUtil
import com.dingyue.contract.web.CustomWebClient
import com.dingyue.contract.web.JSInterfaceObject
import com.google.gson.Gson
import com.intelligent.reader.R
import com.intelligent.reader.util.PagerDesc
import iyouqu.theme.FrameActivity
import kotlinx.android.synthetic.zsmfqbxs.act_find_detail.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.IS_FROM_PUSH
import swipeback.ActivityLifecycleHelper
import java.util.*

/**
 * WebView二级页面
 */
@Route(path = RouterConfig.TABULATION_ACTIVITY)
class TabulationActivity : FrameActivity(), View.OnClickListener {
    private var rankType: String? = null

    private var currentUrl: String? = null
    private var currentTitle: String? = null
    private var urls: ArrayList<String>? = null
    private var names: ArrayList<String>? = null
    private var backClickCount: Int = 0
    private var loadingpage: LoadingPage? = null
    private var customWebClient: CustomWebClient? = null
    private var handler: Handler? = null
    private var sharedPreUtil: SharedPreUtil? = null
    private var fromType = ""
    private var mPagerDesc: PagerDesc? = null
    private var h5Margin: Int = 0
    private var isSupport = true
    private var isFromPush = false

    private val isNeedInterceptSlide: Boolean
        get() {
            val packageName = AppUtils.getPackageName()
            return (("cc.kdqbxs.reader" == packageName || "cc.quanbennovel" == packageName
                    || "cn.txtkdxsdq.reader" == packageName) && !TextUtils.isEmpty(currentTitle)
                    && (currentTitle!!.contains("男频") || currentTitle!!.contains("女频")))
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.act_find_detail)
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }

        handler = Handler()
        urls = ArrayList()
        names = ArrayList()

        val intent = intent
        if (intent != null) {
            currentUrl = intent.getStringExtra("url")
            urls?.add(currentUrl ?: "")
            currentTitle = intent.getStringExtra("title")
            names?.add(currentTitle ?: "")
            isFromPush = intent.getBooleanExtra(IS_FROM_PUSH, false)
        }
        if (currentUrl == null || currentTitle == null) {
            onBackPressed()
            return
        }
        sharedPreUtil = SharedPreUtil(SharedPreUtil.SHARE_DEFAULT)
        fromType = sharedPreUtil!!.getString(SharedPreUtil.HOME_FINDBOOK_SEARCH,
                "other")
        AppUtils.disableAccessibility(this)
        initView()


        if (!TextUtils.isEmpty(currentUrl)) {
            loadWebData(currentUrl, currentTitle)
        }
    }

    @SuppressLint("AddJavascriptInterface", "JavascriptInterface")
    private fun initView() {

        initListener()
        //判断是否是作者主页
        if (currentUrl!!.contains(RequestService.AUTHOR_V4) || currentUrl!!.contains(
                RequestService.AUTHOR_h5.replace("{packageName}", AppUtils.getPackageName()))) {
            find_book_detail_search?.visibility = View.GONE
        } else {
            find_book_detail_search?.visibility = View.VISIBLE
        }

        find_book_detail_main?.setLayerType(View.LAYER_TYPE_NONE, null)


        loadingpage = LoadingPage(this, find_book_detail_main, LoadingPage.setting_result)

        customWebClient = CustomWebClient(this, find_book_detail_content)

        customWebClient?.initWebViewSetting()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            find_book_detail_content?.settings?.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        find_book_detail_content?.webViewClient = customWebClient

        find_book_detail_content?.addJavascriptInterface(object : JSInterfaceObject(this@TabulationActivity) {

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
                            try {
                                loadWebData(redirect.url, redirect.title)
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

    private fun initListener() {
        if (!TextUtils.isEmpty(currentTitle)) {
            find_book_detail_title?.text = currentTitle
        }
        find_book_detail_title?.setOnClickListener(this)
        find_book_detail_back?.setOnClickListener(this)
        find_book_detail_search?.setOnClickListener(this)
        addTouchListener()
    }

    override fun shouldLightStatusBase(): Boolean {
        return if ("cc.quanben.novel" == AppUtils.getPackageName()) {
            true
        } else super.shouldLightStatusBase()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.find_book_detail_back -> {
                val data = HashMap<String, String>()
                data.put("type", "1")
                when (fromType) {
                    "class" -> {
                        data.put("firstclass", currentTitle ?: "")
                        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTCLASS_PAGE,
                                StartLogClickUtil.BACK, data)
                    }
                    "top" -> {
                        data.put("firsttop", currentTitle ?: "")
                        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTTOP_PAGE,
                                StartLogClickUtil.BACK, data)
                    }
                    "recommend" -> {
                        data.put("firstrecommend", currentTitle ?: "")
                        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTRECOMMEND_PAGE,
                                StartLogClickUtil.BACK, data)
                    }
                    "authorType" -> StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.AUTHORPAGE_PAGE,
                            StartLogClickUtil.BACK, data)
                }
                clickBackBtn()
            }
            R.id.find_book_detail_search -> {
                val postData = HashMap<String, String>()

                when (fromType) {
                    "class" -> {
                        postData.put("firstclass", currentTitle ?: "")
                        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTCLASS_PAGE,
                                StartLogClickUtil.SEARCH, postData)
                    }
                    "top" -> {
                        postData.put("firsttop", currentTitle ?: "")
                        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.FIRSTTOP_PAGE,
                                StartLogClickUtil.SEARCH, postData)
                    }
                    "recommend" -> {
                        postData.put("firstrecommend", currentTitle ?: "")
                        StartLogClickUtil.upLoadEventLog(this,
                                StartLogClickUtil.FIRSTRECOMMEND_PAGE,
                                StartLogClickUtil.SEARCH, postData)
                    }
                }

                val intent = Intent()
                intent.setClass(this, SearchBookActivity::class.java)
                startActivity(intent)
            }
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
        if (find_book_detail_content != null) {
            find_book_detail_content?.clearCache(false) //清空缓存
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (find_book_detail_content?.parent != null) {
                    (find_book_detail_content?.parent as ViewGroup).removeView(find_book_detail_content)
                }
                find_book_detail_content?.stopLoading()
                // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
                find_book_detail_content?.settings?.javaScriptEnabled = false
                find_book_detail_content?.clearHistory()
                find_book_detail_content?.removeAllViews()
                find_book_detail_content?.destroy()
            } else {
                find_book_detail_content?.stopLoading()
                // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
                find_book_detail_content?.settings?.javaScriptEnabled = false
                find_book_detail_content?.clearHistory()
                find_book_detail_content?.removeAllViews()
                find_book_detail_content?.destroy()
                if (find_book_detail_content?.parent != null) {
                    (find_book_detail_content?.parent as ViewGroup).removeView(find_book_detail_content)
                }
            }
        }
    }

    override fun onBackPressed() {
        if (urls!!.size - backClickCount <= 1) {
            super.onBackPressed()
        } else {
            backClickCount++
            val nowIndex = urls!!.size - 1 - backClickCount

            currentUrl = urls!![nowIndex]
            currentTitle = names!![nowIndex]
            loadWebData(currentUrl, currentTitle)
        }
    }

    private fun loadWebData(url: String?, name: String?) {
        var url = url
        var map: Map<String, String>? = null
        if (url != null) {
            val array = url.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            url = array[0]
            if (array.size == 2) {//如果传递过来的url带参数   /cn.kkqbtxtxs.reader/v3/rank/more
                // .do?type=100&rankType=0
                map = UrlUtils.getUrlParams(array[1])
            } else if (array.size == 1) {//如果传递过来的url不带参数   /cn.kkqbtxtxs.reader/v3/rank/index.do
                map = HashMap()
            }
            url = UrlUtils.buildWebUrl(url, map)
        }

        //如果可以切换周榜和月榜总榜
        if (map != null && map["qh"] != null && map["qh"] == "true") {
            rankType = analysisUrl(currentUrl)["rankType"]
        } else {//如果不可以切换周榜和月榜总榜
            setTitle(name)
        }

        startLoading(handler, url)
        webViewCallback()

    }

    private fun startLoading(handler: Handler?, url: String?) {
        if (find_book_detail_content == null) {
            return
        }

        handler?.post { loadingData(url) } ?: loadingData(url)
    }

    private fun loadingData(url: String?) {
        if (customWebClient != null) {
            customWebClient!!.initParameter()
        }
        AppLog.e(TAG, "LoadingData ==> " + url!!)
        if (!TextUtils.isEmpty(url) && find_book_detail_content != null) {
            try {
                find_book_detail_content?.loadUrl(url)
            } catch (e: NullPointerException) {
                e.printStackTrace()
                this.finish()
            }

        }
    }

    private fun webViewCallback() {
        if (find_book_detail_main == null) {
            return
        }

        customWebClient?.setLoadingWebViewStart { url ->
            AppLog.e(TAG, "onLoadStarted: " + url)
        }

        customWebClient?.setLoadingWebViewError {

            AppLog.e(TAG, "onErrorReceived")
            loadingpage?.onErrorVisable()
        }

        customWebClient?.setLoadingWebViewFinish {

            AppLog.e(TAG, "onLoadFinished")
            loadingpage?.onSuccessGone()
            addCheckSlide(find_book_detail_content)
        }

        loadingpage?.setReloadAction(LoadingPage.reloadCallback {
            AppLog.e(TAG, "doReload")
            customWebClient?.initParameter()
            find_book_detail_content?.reload()
        })

    }


    private fun setTitle(name: String?) {
        runOnUiThread { find_book_detail_title!!.text = name }
    }


    //    private fun setSearchBtnVisibility(visibility: Boolean) {
    private fun setSearchBtnVisibel(visibility: Boolean) {
        if (visibility) {
            find_book_detail_search!!.visibility = View.VISIBLE
        } else {
            find_book_detail_search!!.visibility = View.GONE
        }
    }

    private fun analysisUrl(ulr: String?): Map<String, String> {
        var ulr = ulr
        var map: Map<String, String> = HashMap()
        if (ulr != null) {
            val array = ulr.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (array.size == 2) {//如果传递过来的url带参数   /cn.kkqbtxtxs.reader/v3/rank/more
                // .do?type=100&rankType=0
                ulr = array[0]
                map = UrlUtils.getUrlParams(array[1])
            }
        }
        return map
    }

    private fun reLoadWebData(currentUrl: String?, type: Int) {
        var url = ""
        var map: MutableMap<String, String>? = HashMap()
        if (currentUrl != null) {
            val array = currentUrl.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (array.size == 2) {//如果传递过来的url带参数   /cn.kkqbtxtxs.reader/v3/rank/more
                // .do?type=100&rankType=0
                url = array[0]
                map = UrlUtils.getUrlParams(array[1])
            }
        }
        when (type) {
            0 -> map!!.put("rankType", "week")
            1 -> map!!.put("rankType", "month")
            2 -> map!!.put("rankType", "total")
        }

        url = UrlUtils.buildUrl(url, map)
        if (url.contains(Config.loadRequestAPIHost())) {
            val start = url.lastIndexOf(Config.loadRequestAPIHost()) + Config.loadRequestAPIHost().length
            this.currentUrl = url.substring(start, url.length)
        }

        startLoading(handler, url)
        webViewCallback()
    }

    private fun clickBackBtn() {
        if (urls!!.size - backClickCount <= 1) {
            this@TabulationActivity.finish()
        } else {
            backClickCount++
            val nowIndex = urls!!.size - 1 - backClickCount

            currentUrl = urls!![nowIndex]
            currentTitle = names!![nowIndex]
            loadWebData(currentUrl, currentTitle)
        }
    }

    private fun addCheckSlide(find_book_detail_content: WebView?) {
        if (find_book_detail_content != null && isNeedInterceptSlide) {
            find_book_detail_content.loadUrl("javascript:getViewPagerInfo()")
        }
    }

    private fun addTouchListener() {
        if (find_book_detail_content != null && isNeedInterceptSlide) {
            find_book_detail_content?.setOnTouchListener { v, event ->
                val y = event.rawY
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (find_book_detail_content != null) {
                            val loction = IntArray(2)
                            find_book_detail_content?.getLocationOnScreen(loction)
                            h5Margin = loction[1]
                        }
                        if (null != mPagerDesc) {
                            var top = mPagerDesc!!.top
                            var bottom = top + (mPagerDesc!!.bottom - mPagerDesc!!.top)
                            val metric = resources.displayMetrics
                            top = ((top * metric.density).toInt() + h5Margin).toFloat()
                            bottom = ((bottom * metric.density).toInt() + h5Margin).toFloat()
                            if (y > top && y < bottom) {
                                isSupport = false
                            } else {
                                isSupport = true
                            }
                        }
                    }
                    MotionEvent.ACTION_UP -> isSupport = true
                    MotionEvent.ACTION_MOVE -> {
                    }
                    else -> isSupport = true
                }
                false
            }
        }
    }


    override fun supportSlideBack(): Boolean {
        return ActivityLifecycleHelper.getActivities().size > 1 && isSupport
    }

    protected fun genCoverBook(host: String, book_id: String, book_source_id: String, name: String,
                               author: String, status: String, category: String,
                               imgUrl: String, last_chapter: String, chapter_count: String, update_time: Long,
                               parameter: String, extra_parameter: String, dex: Int): Book {
        val book = Book()
        book.status = status
        book.update_date_fusion = 0
        book.book_id = book_id
        book.book_source_id = book_source_id
        book.name = name
        book.label = category
        book.author = author
        book.img_url = imgUrl
        book.host = host
        book.chapter_count = Integer.valueOf(chapter_count)

        val lastChapter = Chapter()
        lastChapter.name = last_chapter
        lastChapter.update_time = update_time
        book.last_update_success_time = System.currentTimeMillis()
        return book

    }

    override fun finish() {
        super.finish()
        //离线消息 跳转到主页
        if (isFromPush && ActivityLifecycleHelper.getActivities().size <= 1) {
            startActivity(Intent(this, SplashActivity::class.java))
        }
    }

}
