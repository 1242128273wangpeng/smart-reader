package com.dingyue.searchbook.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import com.dingyue.searchbook.R
import com.dingyue.searchbook.adapter.WebSearchResultAdapter
import com.dingyue.searchbook.interfaces.OnResultListener
import com.dingyue.searchbook.presenter.SearchResultPresenter
import com.dingyue.searchbook.view.ISearchResultView
import kotlinx.android.synthetic.main.fragment_search_result.*
import net.lzbook.kit.bean.CrawlerResult
import net.lzbook.kit.ui.widget.LoadingPage
import net.lzbook.kit.utils.BDCrawler
import net.lzbook.kit.utils.loge
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.runOnMain
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.utils.web.CustomWebClient


/**
 * Desc 搜索结果
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/19 0019 22:05
 */
class SearchResultFragment : Fragment(), ISearchResultView {

    var onResultListener: OnResultListener<String>? = null

    var searchNoResult = false

    private var loadingPage: LoadingPage? = null

    private var customWebClient: CustomWebClient? = null

    private var webSearchResultAdapter: WebSearchResultAdapter? = null

    private var webSearchResultList: MutableList<CrawlerResult>? = null

    private var keyWord: String = ""

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

    /**
     * 加载关键字
     * searchType：0 全部 1 标签 2 作者 3 书名
     */
    fun loadKeyWord(keyWord: String, searchType: String = "0", isAuthor: Int = 0) {
        webSearchResultAdapter = null
        searchNoResult = false
        this.keyWord = keyWord
        searchResultPresenter.loadKeyWord(keyWord, searchType, isAuthor)
    }


    override fun getCurrentActivity(): Activity = requireActivity()

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface", "JavascriptInterface")
    override fun obtainJSInterface(jsInterface: Any) {

        if (Build.VERSION.SDK_INT >= 14) {
            search_result_content?.setLayerType(View.LAYER_TYPE_NONE, null)
        }

        if (search_result_content != null) {
            customWebClient = CustomWebClient(activity, search_result_content)
        }

        customWebClient?.initWebViewSetting()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            search_result_content?.settings?.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        search_result_content?.webViewClient = customWebClient
        search_result_content?.addJavascriptInterface(jsInterface, "J_search")
    }


    override fun onSearchResult(url: String) {
        showLoading()
        webViewCallback()
        search_result_content?.loadUrl(url)
    }

    // 无结果通知
    override fun onNoResult(keyWord: String?) {
        runOnMain {
            search_result_content.visibility = View.GONE
            rv_catch_result.visibility = View.VISIBLE
            searchNoResult = true
            showLoading()
        }
    }

    /**
     * 重置结果页
     */
    fun resetResult(){
        if(rv_catch_result.visibility ==View.VISIBLE) {
            search_result_content.visibility = View.VISIBLE
            rv_catch_result.visibility = View.GONE
            searchNoResult = false
            if(webSearchResultAdapter!=null) {
                webSearchResultList?.clear()
                webSearchResultAdapter?.setData(webSearchResultList.orEmpty())
                webSearchResultAdapter?.notifyDataSetChanged()
            }
            BDCrawler.cancelCrawler()
        }
    }

    /**
     * 百度抓取数据展示
     */
    override fun onWebSearchResult(res: MutableList<CrawlerResult>?) {
        hideLoading()
        if (res != null) {
            rv_catch_result.layoutManager = LinearLayoutManager(requireContext())
            // 适配器加载数据
            if (webSearchResultAdapter == null) {
                webSearchResultList = res
                webSearchResultAdapter = WebSearchResultAdapter(requireContext())
                webSearchResultAdapter!!.onItemClick = {
                    if (!it.isNullOrBlank()) toWebActivity(it!!)
                }
                webSearchResultAdapter!!.onLatestChapterClick = {
                    loge("url: $it")
                    if (!it.isNullOrBlank()) toWebActivity(it!!)
                }
                webSearchResultAdapter!!.setData(webSearchResultList.orEmpty())
                rv_catch_result.adapter = webSearchResultAdapter
            } else {
                webSearchResultList?.addAll(res)
                webSearchResultAdapter?.setData(webSearchResultList.orEmpty())
                webSearchResultAdapter?.notifyDataSetChanged()
            }
        } else ToastUtil.showToastMessage("全网搜索无结果")
    }

    private fun toWebActivity(url: String) {
        val bundle = Bundle()
        bundle.putString("url", url)
        RouterUtil.navigation(requireActivity(), RouterConfig.WEB_VIEW_ACTIVITY, bundle)
    }

    private fun webViewCallback() {

        if (search_result_content == null) {
            return
        }

        customWebClient?.setLoadingWebViewStart {

            searchResultPresenter.setStartedAction()
        }

        customWebClient?.setLoadingWebViewError {
            if (loadingPage != null) {
                loadingPage?.onErrorVisable()
            }
        }

        customWebClient?.setLoadingWebViewFinish {
            searchResultPresenter.onLoadFinished()
            if (loadingPage != null && !searchNoResult) {
                hideLoading()
            }
        }

        loadingPage?.setReloadAction(LoadingPage.reloadCallback {
            customWebClient?.initParameter()
            search_result_content?.reload()
        })
    }

    override fun onSearchWordResult(searchWord: String) {
        onResultListener?.onSuccess(searchWord)
    }

    override fun onCoverResult(bundle: Bundle) {
        RouterUtil.navigation(requireActivity(), RouterConfig.COVER_PAGE_ACTIVITY, bundle)
    }

    override fun onAnotherResult(bundle: Bundle) {
        RouterUtil.navigation(requireActivity(), RouterConfig.FIND_BOOK_DETAIL_ACTIVITY, bundle)
    }

    override fun onAnotherResultNew(bundle: Bundle) {
        RouterUtil.navigation(requireActivity(), RouterConfig.TABULATION_ACTIVITY, bundle)
    }

    override fun onTurnReadResult(bundle: Bundle) {
        val flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        RouterUtil.navigation(requireActivity(), RouterConfig.READER_ACTIVITY, bundle, flags)
    }

    override fun onEnterReadResult(bundle: Bundle) {
        val flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        RouterUtil.navigation(requireActivity(), RouterConfig.READER_ACTIVITY, bundle, flags)
    }


    override fun onResume() {
        super.onResume()
        if (search_result_content != null) {
            if (keyWord.isNotEmpty()) {
                search_result_content?.post {
                    try {
                        search_result_content?.loadUrl("javascript:refreshNew()")
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BDCrawler.cancelCrawler()
        searchResultPresenter.onDestroy()
        search_result_content?.clearCache(false) //清空缓存
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            search_result_content?.removeAllViews()
            search_result_content?.stopLoading()
        } else {
            search_result_content?.stopLoading()
            search_result_content?.removeAllViews()
        }
    }


}