package com.dingyue.searchbook.activity

import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.bean.WebPageFavorite
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.dingyue.searchbook.R
import com.dingyue.statistics.DyStatService
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebView
import kotlinx.android.synthetic.txtqbmfyd.act_web_view.*
import net.lzbook.kit.bean.WebFavoriteUpdateBean
import net.lzbook.kit.pointpage.EventPoint
import net.lzbook.kit.ui.activity.base.FrameActivity
import net.lzbook.kit.ui.widget.LoadingPage
import net.lzbook.kit.utils.loge
import net.lzbook.kit.utils.oneclick.OneClickUtil
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.utils.web.SimpleWebClient
import org.greenrobot.eventbus.EventBus

/**
 * Desc 加载网页数据
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/11/1 15:11
 */
@Route(path = RouterConfig.WEB_VIEW_ACTIVITY)
class WebViewActivity : FrameActivity() {

    private var customWebClient: SimpleWebClient? = null
    private var loadingPage: LoadingPage? = null
    private var requestRepositoryFactory: RequestRepositoryFactory? = null
    private var list: List<WebPageFavorite>? = null
    private var isLoadFinish = true
    private val DEFAULT_TITLE = "小主的收藏"
    private val chromeClient: WebChromeClient by lazy {
        object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress > 80) {
                    if (!isLoadFinish) {
                        isLoadFinish = true
                        hideLoading()
                        btn_page_favorite.visibility = View.VISIBLE
                    }
                }
            }
        }
    }


    override fun onCreate(paramBundle: Bundle?) {
        super.onCreate(paramBundle)
        setContentView(R.layout.act_web_view)
        window.setFormat(PixelFormat.TRANSLUCENT)
        requestRepositoryFactory = RequestRepositoryFactory.loadRequestRepositoryFactory(this)
        img_back.setOnClickListener { onBackPressed() }
        img_close.setOnClickListener {
            DyStatService.onEvent(EventPoint.WEBSEARCHRESULT_CLOSE)
            finish()
        }
        btn_page_favorite.setOnClickListener { clickFavorite() }
        initWebView()
        initWebViewCallback()
        if (intent.hasExtra("url")) web_view.loadUrl(intent.getStringExtra("url"))
    }

    private fun clickFavorite() {
        if (OneClickUtil.isDoubleClick(System.currentTimeMillis())) return
        loge("title:[${web_view.title}],url:[${web_view.url}]")
        if (!web_view.url.isNullOrBlank() && web_view.url.startsWith("http")) {
            list = requestRepositoryFactory?.getWebFavoriteByTitleAndLink(web_view.title.orEmpty(), web_view.url)
            if (list != null && list!!.isNotEmpty()) {
                ToastUtil.showToastMessage("已收藏过")
                return
            }
            DyStatService.onEvent(EventPoint.WEBSEARCHRESULT_WEBCOLLECT, mapOf("title" to web_view.title, "link" to web_view.url))
            val favorite = WebPageFavorite()
            favorite.webTitle = if (web_view.title.isNullOrBlank()) DEFAULT_TITLE else web_view.title
            favorite.webLink = web_view.url
            favorite.createTime = System.currentTimeMillis()
            requestRepositoryFactory?.addWebFavorite(favorite)
            if (!SPUtils.getDefaultSharedBoolean(SPKey.WEB_FAVORITE_FIRST_USE_CLICK)) {
                ToastUtil.showToastMessage("收藏成功，可在个人中心-网页收藏中查看")
                SPUtils.putDefaultSharedBoolean(SPKey.WEB_FAVORITE_FIRST_USE_CLICK, true)
            } else {
                ToastUtil.showToastMessage("收藏成功")
            }
            SPUtils.putDefaultSharedBoolean(SPKey.WEB_FAVORITE_FIRST_USE, false)
            EventBus.getDefault().post(WebFavoriteUpdateBean())
        } else {
            ToastUtil.showToastMessage("收藏失败")
        }
    }

    private fun initWebView() {
        customWebClient = SimpleWebClient(this, web_view)
        customWebClient?.initWebViewSetting()
        web_view.webViewClient = customWebClient
        web_view.webChromeClient = chromeClient
    }

    private fun initWebViewCallback() {

        if (customWebClient != null) {
            customWebClient?.setLoadingWebViewStart {
                isLoadFinish = false
                showLoading()
            }
            customWebClient?.setLoadingEveryWebViewStart {
                isLoadFinish = false
                btn_page_favorite.visibility = View.GONE
                showLoading()
            }
            customWebClient?.setLoadingWebViewError {
                isLoadFinish = true
                loadingPage?.onErrorVisible("页面加载失败，请稍后再试~")
            }

            customWebClient?.setLoadingWebViewFinish {
                if (!isLoadFinish) {
                    isLoadFinish = true
                    hideLoading()
                    btn_page_favorite.visibility = View.VISIBLE
                }
            }
        }

    }

    private fun showLoading() {
        if (loadingPage == null) {
            loadingPage = LoadingPage(this, rl_content, LoadingPage.setting_result)
            loadingPage?.setReloadAction(LoadingPage.reloadCallback {
                web_view?.reload()
            })
        }
    }

    private fun hideLoading() {
        loadingPage?.onSuccess()
        loadingPage = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            web_view.removeAllViews()
            web_view.stopLoading()
        } else {
            web_view.stopLoading()
            web_view.removeAllViews()
        }
    }

    override fun onBackPressed() {
        if (web_view.canGoBack()) {
            web_view.goBack()
            return
        }
        super.onBackPressed()
    }


}