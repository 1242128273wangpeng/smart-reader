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
import android.webkit.WebSettings
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Route
import com.baidu.mobstat.StatService
import com.ding.basic.bean.Book
import com.ding.basic.bean.Chapter
import com.ding.basic.repository.RequestRepositoryFactory
import com.ding.basic.request.RequestService
import com.intelligent.reader.R
import com.intelligent.reader.util.PagerDesc
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.qbmfrmxs.act_tabulation.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.base.BaseBookApplication
import net.lzbook.kit.base.activity.FrameActivity
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.download.CacheManager
import net.lzbook.kit.utils.logger.AppLog
import net.lzbook.kit.utils.oneclick.OneClickUtil
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.sp.SPKey
import net.lzbook.kit.utils.sp.SPUtils
import net.lzbook.kit.utils.uiThread
import net.lzbook.kit.utils.webview.CustomWebClient
import net.lzbook.kit.utils.webview.JSInterfaceHelper
import net.lzbook.kit.utils.webview.UrlUtils
import net.lzbook.kit.widget.LoadingPage
import java.util.*

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

    private var jsInterfaceHelper: JSInterfaceHelper? = null

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

        customWebClient?.setWebSettings()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            wv_tabulation_result?.settings?.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        wv_tabulation_result?.webViewClient = customWebClient

        if (wv_tabulation_result != null) {
            jsInterfaceHelper = JSInterfaceHelper(this, wv_tabulation_result)
        }

        if (jsInterfaceHelper != null && wv_tabulation_result != null) {
            wv_tabulation_result.addJavascriptInterface(jsInterfaceHelper, "J_search")
        }

    }

    private fun initListener() {
        if (txt_tabulation_title != null && !TextUtils.isEmpty(currentTitle)) {
            txt_tabulation_title?.text = currentTitle
        }

        if (img_tabulation_back != null) {
            img_tabulation_back?.setOnClickListener(this)
        }

        if (img_tabulation_search != null) {
            img_tabulation_search?.setOnClickListener(this)
        }

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

        handler.removeCallbacksAndMessages(null)

        if (wv_tabulation_result != null) {

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
            customWebClient?.doClear()
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
        customWebClient?.setStartedAction { url -> Logger.i("LoadStartedAction: $url") }

        customWebClient?.setErrorAction {
            Logger.i("LoadErrorAction")

            if (loadingPage != null) {
                loadingPage?.onErrorVisable()
            }
        }

        customWebClient?.setFinishedAction {
            Logger.i("LoadFinishAction")

            if (loadingPage != null) {
                loadingPage?.onSuccessGone()
            }
        }

        if (loadingPage != null) {
            loadingPage?.setReloadAction(LoadingPage.reloadCallback {

                if (customWebClient != null) {
                    customWebClient?.doClear()
                }

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


    private fun initJSHelp() {
        jsInterfaceHelper?.setOnSearchClick(JSInterfaceHelper.onSearchClick { keyWord, search_type, filter_type, filter_word, sort_type ->
            if (OneClickUtil.isDoubleClick(System.currentTimeMillis())) {
                return@onSearchClick
            }
            try {
                val data = HashMap<String, String>()
                data["keyword"] = keyWord
                data["type"] = "1"//0 代表从分类过来 1 代表从FindBookDetail
                StartLogClickUtil.upLoadEventLog(this@TabulationActivity,
                        StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.SYSTEM_SEARCHRESULT,
                        data)

                val intent = Intent()
                intent.setClass(this@TabulationActivity, SearchBookActivity::class.java)
                intent.putExtra("word", keyWord)
                intent.putExtra("search_type", search_type)
                intent.putExtra("filter_type", filter_type)
                intent.putExtra("filter_word", filter_word)
                intent.putExtra("sort_type", sort_type)
                intent.putExtra("from_class", "findBookDetail")
                startActivity(intent)
                AppLog.i(TAG, "enterSearch success")
            } catch (e: Exception) {
                AppLog.e(TAG, "Search failed")
                e.printStackTrace()
            }
        })

        jsInterfaceHelper?.setOnEnterCover(JSInterfaceHelper.onEnterCover { host, book_id, book_source_id, name, author, parameter, extra_parameter ->
            AppLog.e(TAG, "doCover")

            if (OneClickUtil.isDoubleClick(System.currentTimeMillis())) {
                return@onEnterCover
            }
            val data = HashMap<String, String>()
            data["BOOKID"] = book_id
            data["source"] = "WEBVIEW"
            StartLogClickUtil.upLoadEventLog(this@TabulationActivity,
                    StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.ENTER, data)


            val intent = Intent()
            intent.setClass(applicationContext, CoverPageActivity::class.java)
            val bundle = Bundle()
            bundle.putString("author", author)
            bundle.putString("book_id", book_id)
            bundle.putString("book_source_id", book_source_id)
            intent.putExtras(bundle)
            startActivity(intent)
        })

        jsInterfaceHelper?.setOnAnotherWebClick(JSInterfaceHelper.onAnotherWebClick { url, name ->
            AppLog.e(TAG, "doAnotherWeb")
            val packageName = AppUtils.getPackageName()
            if (OneClickUtil.isDoubleClick(System.currentTimeMillis())) {
                return@onAnotherWebClick
            }
            if ("cc.kdqbxs.reader" == packageName || "cn.txtkdxsdq.reader" == packageName) {
                try {
                    val intent = Intent()
                    intent.setClass(this@TabulationActivity, FindBookDetail::class.java)
                    intent.putExtra("url", url)
                    intent.putExtra("title", name)
                    startActivity(intent)
                    AppLog.e(TAG, "EnterAnotherWeb")
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            } else {
                try {
                    currentUrl = url
                    currentTitle = name

                    urls?.add(currentUrl ?: "")
                    names?.add(currentTitle ?: "")
                    loadWebViewData(currentUrl, name)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        })

        if (isNeedInterceptSlide()) {
            jsInterfaceHelper?.setOnH5PagerInfo(JSInterfaceHelper.OnH5PagerInfoListener { x, y, width, height -> mPagerDesc = PagerDesc(y, x, x + width, y + height) })

        }

        jsInterfaceHelper?.setOnInsertBook(JSInterfaceHelper.OnInsertBook { host, book_id, book_source_id, name, author, status, category, imgUrl, last_chapter, chapter_count, updateTime, parameter, extra_parameter, dex ->
            AppLog.e(TAG, "doInsertBook")
            val book = genCoverBook(host, book_id, book_source_id, name, author, status,
                    category, imgUrl, last_chapter, chapter_count, updateTime, parameter,
                    extra_parameter, dex)
            val succeed = RequestRepositoryFactory.loadRequestRepositoryFactory(
                    BaseBookApplication.getGlobalContext()).insertBook(book) > 0
            if (succeed) {
                Toast.makeText(this@TabulationActivity.applicationContext,
                        R.string.bookshelf_insert_success, Toast.LENGTH_SHORT).show()
            }
        })

        jsInterfaceHelper?.setOnDeleteBook(JSInterfaceHelper.OnDeleteBook { book_id ->
            AppLog.e(TAG, "doDeleteBook")
            RequestRepositoryFactory.loadRequestRepositoryFactory(
                    BaseBookApplication.getGlobalContext()).deleteBook(book_id)
            CacheManager.stop(book_id)
            CacheManager.resetTask(book_id)
            Toast.makeText(this@TabulationActivity.applicationContext,
                    R.string.bookshelf_delete_success, Toast.LENGTH_SHORT).show()
        })
    }

    private fun isNeedInterceptSlide(): Boolean {
        val packageName = AppUtils.getPackageName()
        return (("cc.kdqbxs.reader" == packageName || "cc.quanbennovel" == packageName
                || "cn.txtkdxsdq.reader" == packageName) && !TextUtils.isEmpty(currentTitle)
                && (currentTitle!!.contains("男频") || currentTitle!!.contains("女频")))
    }

    private fun genCoverBook(host: String, book_id: String, book_source_id: String, name: String,
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

    private fun addTouchListener() {
        if (wv_tabulation_result != null && isNeedInterceptSlide()) {
            wv_tabulation_result.setOnTouchListener(View.OnTouchListener { v, event ->
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
}