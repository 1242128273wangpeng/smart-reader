package com.intelligent.reader.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings

import com.alibaba.android.arouter.facade.annotation.Route
import com.baidu.mobstat.StatService
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.util.BridgeObject
import com.dingyue.contract.util.SharedPreUtil
import com.intelligent.reader.R
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

    private var sharedPreUtil: SharedPreUtil? = null

    private var backClickCount: Int = 0

    private var isSupport = true

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
            customWebClient = CustomWebClient(this, bwv_tabulation_result)
        }

        customWebClient?.setWebSettings()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bwv_tabulation_result?.settings?.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        bwv_tabulation_result?.webViewClient = customWebClient

        bwv_tabulation_result.addJavascriptObject(BridgeObject(this@TabulationActivity), "DingYue")
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
        if (customWebClient != null) {
            customWebClient?.doClear()
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

    override fun supportSlideBack(): Boolean {
        return isSupport
    }
}