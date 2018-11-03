package com.dingyue.searchbook.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import com.alibaba.android.arouter.facade.annotation.Route
import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.bean.WebPageFavorite
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.dingyue.searchbook.R
import kotlinx.android.synthetic.txtqbmfyd.act_web_view.*
import net.lzbook.kit.ui.activity.base.FrameActivity
import net.lzbook.kit.ui.widget.LoadingPage
import net.lzbook.kit.utils.antiShakeClick
import net.lzbook.kit.utils.loge
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.utils.web.CustomWebClient

/**
 * Desc 加载网页数据
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/11/1 15:11
 */
@Route(path = RouterConfig.WEB_VIEW_ACTIVITY)
class WebViewActivity : FrameActivity() {

    private var customWebClient: CustomWebClient? = null
    private var loadingPage: LoadingPage? = null

    override fun onCreate(paramBundle: Bundle?) {
        super.onCreate(paramBundle)
        setContentView(R.layout.act_web_view)
        img_back.setOnClickListener { finish() }
        btn_page_favorite.antiShakeClick { clickFavorite() }
        initWebView()
        initWebViewCallback()
        if (intent.hasExtra("url")) web_view.loadUrl(intent.getStringExtra("url"))
    }

    private fun clickFavorite() {
        if (!web_view.title.isNullOrBlank() && !web_view.url.isNullOrBlank() && web_view.url.startsWith("http")) {
            loge("title:[${web_view.title}],url:[${web_view.url}]")
            btn_page_favorite.isEnabled = false
            val favorite = WebPageFavorite()
            favorite.webTitle = web_view.title
            favorite.webLink = web_view.url
            favorite.createTime = System.currentTimeMillis()
            RequestRepositoryFactory.loadRequestRepositoryFactory(this).addWebFavorite(favorite)
            if (SPUtils.getDefaultSharedBoolean(SPKey.WEB_FAVORITE_FIRST_USE_CLICK)) {
                ToastUtil.showToastMessage("收藏成功，可在个人中心-网页收藏中查看")
                SPUtils.putDefaultSharedBoolean(SPKey.WEB_FAVORITE_FIRST_USE_CLICK, true)
            } else {
                ToastUtil.showToastMessage("收藏成功")
            }
            btn_page_favorite.isEnabled = true
        }
    }


    private fun initWebView() {
        web_view?.topShadow = img_head_shadow
        if (Build.VERSION.SDK_INT >= 14) web_view.setLayerType(View.LAYER_TYPE_NONE, null)

        customWebClient = CustomWebClient(this, web_view)
        customWebClient?.initWebViewSetting()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            web_view.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        web_view.webViewClient = customWebClient
    }

    private fun initWebViewCallback() {

        if (customWebClient != null) {
            customWebClient?.setLoadingWebViewStart { showLoading() }
            customWebClient?.setLoadingWebViewError { loadingPage?.onErrorVisable() }
            customWebClient?.setLoadingWebViewFinish { hideLoading() }
        }

        if (loadingPage != null) {
            loadingPage?.setReloadAction(LoadingPage.reloadCallback {
                if (customWebClient != null) {
                    customWebClient?.initParameter()
                }
                web_view?.reload()
            })
        }
    }

    private fun showLoading() {
        if (loadingPage == null) {
            loadingPage = LoadingPage(this, rl_content, LoadingPage.setting_result)
        }
    }

    private fun hideLoading() {
        loadingPage?.onSuccess()
        loadingPage = null
    }

    override fun onDestroy() {
        super.onDestroy()
        web_view.clearCache(false) //清空缓存
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