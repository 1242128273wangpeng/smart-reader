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
import com.dingyue.contract.router.RouterConfig
import com.intelligent.reader.R
import com.intelligent.reader.util.PagerDesc
import com.orhanobut.logger.Logger

import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.request.UrlUtils

import java.util.ArrayList
import java.util.HashMap

import iyouqu.theme.FrameActivity
import kotlinx.android.synthetic.txtqbmfyd.act_tabulation.*
import net.lzbook.kit.CustomWebViewClient
import net.lzbook.kit.WebViewInterfaceObject
import net.lzbook.kit.utils.*
import swipeback.ActivityLifecycleHelper

@Route(path = RouterConfig.TABULATION_ACTIVITY)
class TabulationActivity : FrameActivity() {

    private var urls = ArrayList<String?>()
    private var titles = ArrayList<String?>()

    private var url: String? = null
    private var title: String? = null

    private var fromPush = false

    private var customWebViewClient: CustomWebViewClient? = null

    private var pagerDesc: PagerDesc? = null

    private var margin: Int = 0

    private var backClickCount: Int = 0

    private var loadingPage: LoadingPage? = null

    private var fromType = ""

    private var supportSlide = true


    private val WEB_AUTHOR = "/h5/{packageName}/author"

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

//    private fun initJSHelp() {
//        jsInterfaceHelper?.setOnSearchClick(JSInterfaceHelper.onSearchClick { keyWord, search_type, filter_type, filter_word, sort_type ->
//            if (CommonContract.isDoubleClick(System.currentTimeMillis())) {
//                return@onSearchClick
//            }
//            try {
//                val data = HashMap<String, String>()
//                data["keyword"] = keyWord
//                data["type"] = "1"//0 代表从分类过来 1 代表从FindBookDetail
//                StartLogClickUtil.upLoadEventLog(this@TabulationActivity,
//                        StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.SYSTEM_SEARCHRESULT,
//                        data)
//
//                val intent = Intent()
//                intent.setClass(this@TabulationActivity, SearchBookActivity::class.java)
//                intent.putExtra("word", keyWord)
//                intent.putExtra("search_type", search_type)
//                intent.putExtra("filter_type", filter_type)
//                intent.putExtra("filter_word", filter_word)
//                intent.putExtra("sort_type", sort_type)
//                intent.putExtra("from_class", "findBookDetail")
//                startActivity(intent)
//                AppLog.i(TAG, "enterSearch success")
//            } catch (e: Exception) {
//                AppLog.e(TAG, "Search failed")
//                e.printStackTrace()
//            }
//        })
//
//        jsInterfaceHelper?.setOnEnterCover(JSInterfaceHelper.onEnterCover { host, book_id, book_source_id, name, author, parameter, extra_parameter ->
//            AppLog.e(TAG, "doCover")
//
//            if (CommonContract.isDoubleClick(System.currentTimeMillis())) {
//                return@onEnterCover
//            }
//            val data = HashMap<String, String>()
//            data["BOOKID"] = book_id
//            data["source"] = "WEBVIEW"
//            StartLogClickUtil.upLoadEventLog(this@TabulationActivity,
//                    StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.ENTER, data)
//
//
//            val intent = Intent()
//            intent.setClass(applicationContext, CoverPageActivity::class.java)
//            val bundle = Bundle()
//            bundle.putString("author", author)
//            bundle.putString("book_id", book_id)
//            bundle.putString("book_source_id", book_source_id)
//            intent.putExtras(bundle)
//            startActivity(intent)
//        })
//
//        jsInterfaceHelper?.setOnAnotherWebClick(JSInterfaceHelper.onAnotherWebClick { url, name ->
//            AppLog.e(TAG, "doAnotherWeb")
//            val packageName = AppUtils.getPackageName()
//            if (CommonContract.isDoubleClick(System.currentTimeMillis())) {
//                return@onAnotherWebClick
//            }
//            if ("cc.kdqbxs.reader" == packageName || "cn.txtkdxsdq.reader" == packageName) {
//                try {
//                    val intent = Intent()
//                    intent.setClass(this@TabulationActivity, TabulationActivity::class.java)
//                    intent.putExtra("url", url)
//                    intent.putExtra("title", name)
//                    startActivity(intent)
//                    AppLog.e(TAG, "EnterAnotherWeb")
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//
//            } else {
//                try {
//                    this.url = url
//                    title = name
//                    urls?.add(this.url)
//                    titles?.add(title)
//                    loadWebData(this.url, name)
//                    //                    setTitle(name);
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//
//            }
//        })
//
//        if (isNeedInterceptSlide) {
//            jsInterfaceHelper?.setOnH5PagerInfo { x, y, width, height -> pagerDesc = PagerDesc(y, x, x + width, y + height) }
//
//        }
//
//        jsInterfaceHelper?.setOnInsertBook { host, book_id, book_source_id, name, author, status, category, imgUrl, last_chapter, chapter_count, updateTime, parameter, extra_parameter, dex ->
//            AppLog.e(TAG, "doInsertBook")
//            val book = genCoverBook(host, book_id, book_source_id, name, author, status,
//                    category, imgUrl, last_chapter, chapter_count, updateTime, parameter,
//                    extra_parameter, dex)
//            val succeed = RequestRepositoryFactory.loadRequestRepositoryFactory(
//                    BaseBookApplication.getGlobalContext()).insertBook(book) > 0
//            if (succeed) {
//                Toast.makeText(this@TabulationActivity.applicationContext,
//                        R.string.bookshelf_insert_success, Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        jsInterfaceHelper?.setOnDeleteBook { book_id ->
//            AppLog.e(TAG, "doDeleteBook")
//            RequestRepositoryFactory.loadRequestRepositoryFactory(
//                    BaseBookApplication.getGlobalContext()).deleteBook(book_id)
//            CacheManager.stop(book_id)
//            CacheManager.resetTask(book_id)
//            Toast.makeText(this@TabulationActivity.applicationContext,
//                    R.string.bookshelf_delete_success, Toast.LENGTH_SHORT).show()
//        }
//    }

//    protected fun genCoverBook(host: String, book_id: String, book_source_id: String, name: String,
//                               author: String, status: String, category: String,
//                               imgUrl: String, last_chapter: String, chapter_count: String, update_time: Long,
//                               parameter: String, extra_parameter: String, dex: Int): Book {
//        val book = Book()
//        book.status = status
//        book.update_date_fusion = 0
//        book.book_id = book_id
//        book.book_source_id = book_source_id
//        book.name = name
//        book.label = category
//        book.author = author
//        book.img_url = imgUrl
//        book.host = host
//        book.chapter_count = Integer.valueOf(chapter_count)
//
//        val lastChapter = Chapter()
//        lastChapter.name = last_chapter
//        lastChapter.update_time = update_time
//        book.last_update_success_time = System.currentTimeMillis()
//        return book
//
//    }


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
            customWebViewClient = CustomWebViewClient(this, web_tabulation_content)
        }

        customWebViewClient?.initWebViewSetting()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            web_tabulation_content?.settings?.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        web_tabulation_content?.webViewClient = customWebViewClient

        web_tabulation_content?.addJavascriptInterface(object : WebViewInterfaceObject(this@TabulationActivity) {

            @JavascriptInterface
            override fun startSearchActivity(data: String?) {

            }

        }, "J_search")
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
                            var bottom = top + ((pagerDesc?.bottom ?: 0f) - (pagerDesc?.top ?: 0f))

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

        web_tabulation_content?.post {
            handleLoadingWebViewData(url)
        }
    }

    /***
     * 处理WebView请求
     * **/
    private fun handleLoadingWebViewData(url: String?) {
        if (customWebViewClient != null) {
            customWebViewClient?.initParameter()
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

        if (customWebViewClient != null) {
            customWebViewClient?.setLoadingWebViewStart {
                Logger.e("WebView页面开始加载 $it")
            }

            customWebViewClient?.setLoadingWebViewFinish {
                Logger.e("WebView页面加载结束！")
                if (loadingPage != null) {
                    loadingPage?.onSuccessGone()
                }
                requestWebViewPager(web_tabulation_content)
            }

            customWebViewClient?.setLoadingWebViewError {
                Logger.e("WebView页面加载异常！")
                if (loadingPage != null) {
                    loadingPage?.onErrorVisable()
                }
            }
        }

        if (loadingPage != null) {
            loadingPage?.setReloadAction(LoadingPage.reloadCallback {
                if (customWebViewClient != null) {
                    customWebViewClient?.initParameter()
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
