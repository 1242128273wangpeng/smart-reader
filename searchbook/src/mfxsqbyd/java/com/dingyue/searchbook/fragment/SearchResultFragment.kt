package com.dingyue.searchbook.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import com.dingyue.searchbook.R
import com.dingyue.searchbook.presenter.SearchResultPresenter
import com.dingyue.searchbook.view.ISearchResultView
import kotlinx.android.synthetic.mfxsqbyd.fragment_search_result.*
import net.lzbook.kit.ui.widget.LoadingPage
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.webview.CustomWebClient
import net.lzbook.kit.utils.webview.WebViewJsInterface


/**
 * Desc 搜索结果
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/19 0019 22:05
 */
class SearchResultFragment : Fragment(), ISearchResultView {

    private var loadingPage: LoadingPage? = null

    private var customWebClient: CustomWebClient? = null

    private val searchResultPresenter: SearchResultPresenter  by lazy {
        SearchResultPresenter(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchResultPresenter.onCreate()
    }

    override fun showLoading() {
        if (loadingPage == null) {
            loadingPage = LoadingPage(requireActivity(), search_result_main, LoadingPage.setting_result)
        }
    }

    override fun hideLoading() {
        loadingPage?.onSuccess()
        loadingPage = null
    }

    fun loadKeyWord(keyWord: String, searchType: String = "0") {
        searchResultPresenter.loadKeyWord(keyWord, searchType)
    }

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    override fun obtainJSInterface(jsInterface: WebViewJsInterface) {

        if (Build.VERSION.SDK_INT >= 14) {
            search_result_content?.setLayerType(View.LAYER_TYPE_NONE, null)
        }

        search_result_content?.webViewClient = WebViewClient()
        search_result_content?.webChromeClient = WebChromeClient()
        search_result_content?.settings?.javaScriptEnabled = true
        search_result_content?.settings?.domStorageEnabled = true
        search_result_content?.addJavascriptInterface(jsInterface, "J_search")
    }


    override fun onSearchResult(url: String) {
        showLoading()
        //加载URL
        search_result_content?.loadUrl(url)
    }

    override fun onCoverResult(bundle: Bundle) {
        RouterUtil.navigation(requireActivity(), RouterConfig.COVER_PAGE_ACTIVITY, bundle)
    }


    override fun onAnotherResult(bundle: Bundle) {
        RouterUtil.navigation(requireActivity(), RouterConfig.FIND_BOOK_DETAIL_ACTIVITY, bundle)
    }


    override fun onSearchWordResult(searchWord: String) {

    }


    override fun onTurnReadResult(bundle: Bundle) {
        val flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        RouterUtil.navigation(requireActivity(), RouterConfig.READER_ACTIVITY, bundle, flags)
    }

    override fun onEnterReadResult(bundle: Bundle) {
        val flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        RouterUtil.navigation(requireActivity(), RouterConfig.READER_ACTIVITY, bundle, flags)
    }

    override fun onDestroy() {
        super.onDestroy()
        searchResultPresenter.onDestroy()
        search_result_content?.clearCache(false) //清空缓存
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            search_result_content?.stopLoading()
            search_result_content?.removeAllViews()
        } else {
            search_result_content?.stopLoading()
            search_result_content?.removeAllViews()
        }
    }

}