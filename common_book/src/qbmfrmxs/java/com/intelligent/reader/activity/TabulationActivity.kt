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

import com.alibaba.android.arouter.facade.annotation.Route
import com.baidu.mobstat.StatService
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.util.BuildRequestUrlHandler
import com.dingyue.contract.util.CustomWebViewClient
import com.dingyue.contract.util.SharedPreUtil
import com.dingyue.contract.util.StartCoverHandler
import com.github.lzyzsd.jsbridge.DefaultHandler
import com.google.gson.Gson
import com.intelligent.reader.R
import com.intelligent.reader.util.PagerDesc
import com.orhanobut.logger.Logger

import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.encrypt.URLBuilderIntterface
import net.lzbook.kit.request.UrlUtils

import java.util.ArrayList
import java.util.HashMap

import iyouqu.theme.FrameActivity
import kotlinx.android.synthetic.qbmfrmxs.act_tabulation.*
import net.lzbook.kit.utils.*
import java.io.Serializable

@Route(path = RouterConfig.TABULATION_ACTIVITY)
class TabulationActivity : FrameActivity(), View.OnClickListener {

    private var handler: Handler = Handler()

    private var currentUrl: String? = null

    private var currentTitle: String? = null

    private var fromType: String? = null

    private var customWebViewClient: CustomWebViewClient? = null

    private var urls: ArrayList<String>? = null

    private var names: ArrayList<String>? = null

    private var loadingPage: LoadingPage? = null

    private var sharedPreUtil: SharedPreUtil? = null

    private var backClickCount: Int = 0

    private var mPagerDesc: PagerDesc? = null
    private var h5Margin: Int = 0
    private var isSupport = true

    private val needInterceptSlide: Boolean
        get() {
            return !TextUtils.isEmpty(currentTitle) && (currentTitle!!.contains("男频") || currentTitle!!.contains("女频"))
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.act_tabulation)

        urls = ArrayList()
        names = ArrayList()

        val intent = intent

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

        if (sharedPreUtil == null) {
            sharedPreUtil = SharedPreUtil(SharedPreUtil.SHARE_DEFAULT)
        }

        fromType = sharedPreUtil?.getString(SharedPreUtil.HOME_FINDBOOK_SEARCH, "other")

        initView()

        if (!TextUtils.isEmpty(currentUrl)) {
            loadWebViewData(currentUrl, currentTitle)
        }
    }

    private fun initView() {

        initListener()

        //判断是否是作者主页
        if (currentUrl != null && currentUrl!!.contains(URLBuilderIntterface.AUTHOR_V4)) {
            img_tabulation_search?.visibility = View.GONE
        } else {
            img_tabulation_search?.visibility = View.VISIBLE
        }

        rl_tabulation_main?.setLayerType(View.LAYER_TYPE_NONE, null)

        loadingPage = LoadingPage(this, rl_tabulation_main, LoadingPage.setting_result)

        if (bwv_tabulation_result != null) {
            customWebViewClient = CustomWebViewClient(this, bwv_tabulation_result)
        }

        customWebViewClient?.setWebSettings()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bwv_tabulation_result?.settings?.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        bwv_tabulation_result?.webViewClient = customWebViewClient

        bwv_tabulation_result?.setDefaultHandler(DefaultHandler())

        bwv_tabulation_result?.registerHandler("buildRequestUrl", BuildRequestUrlHandler())

        bwv_tabulation_result?.registerHandler("startCoverActivity", StartCoverHandler(this@TabulationActivity))

        bwv_tabulation_result?.send("Hello", { data ->
            Logger.e("来自JS的消息: " + data.toString())
        })
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

        insertTouchListener()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun insertTouchListener() {
        if (bwv_tabulation_result != null && needInterceptSlide) {
            bwv_tabulation_result?.setOnTouchListener { _, event ->
                val y = event.rawY

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (bwv_tabulation_result != null) {
                            val location = IntArray(2)
                            bwv_tabulation_result?.getLocationOnScreen(location)
                            h5Margin = location[1]
                        }

                        if (null != mPagerDesc) {
                            var top = mPagerDesc!!.top
                            var bottom = top + (mPagerDesc!!.bottom - mPagerDesc!!.top)
                            val metric = resources.displayMetrics
                            top = (top * metric.density).toInt() + h5Margin
                            bottom = (bottom * metric.density).toInt() + h5Margin

                            isSupport = !(y > top && y < bottom)
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

        if (bwv_tabulation_result != null) {

            bwv_tabulation_result?.clearCache(true)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                if (bwv_tabulation_result?.parent != null) {
                    (bwv_tabulation_result?.parent as ViewGroup).removeView(bwv_tabulation_result)
                }

                bwv_tabulation_result?.stopLoading()
                bwv_tabulation_result?.settings?.javaScriptEnabled = false
                bwv_tabulation_result?.clearHistory()
                bwv_tabulation_result?.removeAllViews()
                bwv_tabulation_result?.destroy()
            } else {
                bwv_tabulation_result?.stopLoading()
                bwv_tabulation_result?.settings?.javaScriptEnabled = false
                bwv_tabulation_result?.clearHistory()
                bwv_tabulation_result?.removeAllViews()
                bwv_tabulation_result?.destroy()

                if (bwv_tabulation_result?.parent != null) {
                    (bwv_tabulation_result?.parent as ViewGroup).removeView(bwv_tabulation_result)
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
        if (bwv_tabulation_result == null) {
            return
        }

        handler.post { loadingData(url) }
    }

    private fun loadingData(url: String?) {
        if (customWebViewClient != null) {
            customWebViewClient?.doClear()
        }

        if (!TextUtils.isEmpty(url) && bwv_tabulation_result != null) {
            try {
                bwv_tabulation_result?.loadUrl(url)
            } catch (e: NullPointerException) {
                e.printStackTrace()
                this.finish()
            }
        }
    }

    private fun initWebViewCallback() {
        customWebViewClient?.setStartedAction { url -> Logger.i("LoadStartedAction: $url") }

        customWebViewClient?.setErrorAction {
            Logger.i("LoadErrorAction")

            if (loadingPage != null) {
                loadingPage?.onErrorVisable()
            }
        }

        customWebViewClient?.setFinishedAction {
            Logger.i("LoadFinishAction")

            if (loadingPage != null) {
                loadingPage?.onSuccessGone()
            }

            checkViewSlide()
        }

        if (loadingPage != null) {
            loadingPage?.setReloadAction(LoadingPage.reloadCallback {

                if (customWebViewClient != null) {
                    customWebViewClient?.doClear()
                }

                bwv_tabulation_result?.reload()
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

    private fun checkViewSlide() {
        if (bwv_tabulation_result != null && needInterceptSlide) {

            bwv_tabulation_result?.callHandler("loadPageInformation", currentTitle, { data ->

                val jsRegion = Gson().fromJson(data, JSRegion::class.java)

                mPagerDesc = if (jsRegion != null) {
                    PagerDesc(jsRegion.y, jsRegion.x, jsRegion.x + jsRegion.w, jsRegion.y + jsRegion.h)
                } else {
                    null
                }

                Logger.e("调用JS获取页面信息: $data")
            })
        }
    }

    override fun supportSlideBack(): Boolean {
        return isSupport
    }

    inner class JSRegion :Serializable {
        var x:Int = 0
        var y:Int = 0
        var w:Int = 0
        var h:Int = 0
    }
}