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
import com.dingyue.statistics.DyStatService
import kotlinx.android.synthetic.txtqbmfyd.act_web_view.*
import net.lzbook.kit.pointpage.EventPoint
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
    private var requestRepositoryFactory: RequestRepositoryFactory? = null
    private var isFavorite: Boolean = false
    private var list: List<WebPageFavorite>? = null

    override fun onCreate(paramBundle: Bundle?) {
        super.onCreate(paramBundle)
        setContentView(R.layout.act_web_view)
        requestRepositoryFactory = RequestRepositoryFactory.loadRequestRepositoryFactory(this);
        img_back.setOnClickListener { finish() }
        btn_page_favorite.antiShakeClick { click() }
        initWebView()
        initWebViewCallback()
        if (intent.hasExtra("url")) web_view.loadUrl(intent.getStringExtra("url"))
    }

    private fun clickFavorite() {
        if (!web_view.title.isNullOrBlank() && !web_view.url.isNullOrBlank() && web_view.url.startsWith("http")) {
            loge("title:[${web_view.title}],url:[${web_view.url}]")
            DyStatService.onEvent(EventPoint.WEBSEARCHRESULT_WEBCOLLECT, mapOf("title" to web_view.title, "link" to web_view.url))
            btn_page_favorite.isEnabled = false
            val favorite = WebPageFavorite()
            favorite.webTitle = web_view.title
            favorite.webLink = web_view.url
            favorite.createTime = System.currentTimeMillis()
            requestRepositoryFactory?.addWebFavorite(favorite)
            if (!SPUtils.getDefaultSharedBoolean(SPKey.WEB_FAVORITE_FIRST_USE_CLICK)) {
                ToastUtil.showToastMessage("收藏成功，可在个人中心-网页收藏中查看")
                SPUtils.putDefaultSharedBoolean(SPKey.WEB_FAVORITE_FIRST_USE_CLICK, true)
            } else {
                ToastUtil.showToastMessage("收藏成功")
            }
            btn_page_favorite.isEnabled = true
        }else{
            ToastUtil.showToastMessage("收藏失败")
        }
    }

    fun click() {
        if (isFavorite) {
            cancelFavorite()
            changeBtnStatus()
        } else {
            clickFavorite()
            changeBtnStatus()
        }
    }

    /**
     * 取消收藏
     */
    fun cancelFavorite() {
        btn_page_favorite.isEnabled = false
        requestRepositoryFactory?.deleteWebFavoriteList(list)
        ToastUtil.showToastMessage("已取消")
        btn_page_favorite.isEnabled = true
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
            customWebClient?.setLoadingWebViewStart {
                showLoading()
            }
            customWebClient?.setLoadingEveryWebViewStart {
                btn_page_favorite.visibility = View.GONE
            }
            customWebClient?.setLoadingWebViewError {
                loadingPage?.onErrorVisable()
                btn_page_favorite.visibility = View.GONE
            }

            customWebClient?.setLoadingWebViewFinish {
                hideLoading()
                changeBtnStatus()
            }
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

    /**
     * 改变收藏按钮状态
     */
    fun changeBtnStatus() {
        if (!web_view.title.isNullOrBlank() && !web_view.url.isNullOrBlank() && web_view.url.startsWith("http")) {
            list = requestRepositoryFactory?.getWebFavoriteByTitleAndLink(web_view.title, web_view.url)
        }else{
            list = null
        }
        btn_page_favorite.visibility = View.VISIBLE
        if (list != null && list!!.isNotEmpty()) {
            isFavorite = true
            btn_page_favorite.setText(R.string.page_cancel_favorite)
        } else {
            isFavorite = false
            btn_page_favorite.setText(R.string.page_favorite)
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