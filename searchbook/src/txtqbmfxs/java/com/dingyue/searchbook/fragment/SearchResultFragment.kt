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
import android.webkit.WebSettings
import android.webkit.WebViewClient
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dingyue.searchbook.JSInterface
import com.dingyue.searchbook.R
import com.dingyue.searchbook.presenter.SearchResultPresenter
import com.dingyue.searchbook.view.ISearchResultView
import kotlinx.android.synthetic.txtqbmfxs.fragment_search_result.view.*
import net.lzbook.kit.utils.CustomWebClient
import net.lzbook.kit.utils.JSInterfaceHelper


/**
 * Desc 搜索结果
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/19 0019 22:05
 */
class SearchResultFragment : Fragment(), ISearchResultView {

    private var mView: View? = null
    private var customWebClient: CustomWebClient? = null

    private val searchResultPresenter: SearchResultPresenter  by lazy {
        SearchResultPresenter(this)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_search_result, container, false)
        searchResultPresenter.onCreate()
        return mView
    }

    fun loadKeyWord(keyWord: String) {
        searchResultPresenter.loadKeyWord(keyWord)
    }

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    override fun obtainJSInterface(jsInterface: JSInterfaceHelper) {

        if (Build.VERSION.SDK_INT >= 14) {
            mView?.search_result_content?.setLayerType(View.LAYER_TYPE_NONE, null)
        }

//        customWebClient = CustomWebClient(requireContext(), mView?.search_result_content)
//        customWebClient?.setWebSettings()

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            mView?.search_result_content?.settings?.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
//        }
////
        mView?.search_result_content?.webViewClient = WebViewClient()
        mView?.search_result_content?.webChromeClient = WebChromeClient()
        mView?.search_result_content?.settings?.javaScriptEnabled = true
        mView?.search_result_content?.settings?.domStorageEnabled = true
        mView?.search_result_content?.addJavascriptInterface(jsInterface, "J_search")
    }


    override fun onSearchResult(url: String) {
        showLoading()
        //加载URL
        mView?.search_result_content?.loadUrl(url)
    }

    override fun onCoverResult(bundle: Bundle) {
        RouterUtil.navigation(requireActivity(), RouterConfig.COVER_PAGE_ACTIVITY, bundle)
    }


    override fun onAnotherResult(bundle: Bundle) {
        RouterUtil.navigation(requireActivity(), RouterConfig.FIND_BOOK_DETAIL_ACTIVITY, bundle)
    }


    override fun onSearchWordResult(searchWord: String) {
//        if (search_result_default.visibility != View.VISIBLE) {
//            search_result_default.visibility = View.VISIBLE
//        }
//
//        search_result_input.setText(searchWord)
//        search_result_content.clearView()
    }


    override fun onTurnReadResult(bundle: Bundle) {
        val flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        RouterUtil.navigation(requireActivity(), RouterConfig.READER_ACTIVITY, bundle, flags)
    }

    override fun onEnterReadResult(bundle: Bundle) {
        val flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        RouterUtil.navigation(requireActivity(), RouterConfig.READER_ACTIVITY, bundle, flags)
    }


    override fun showLoading() {

    }

    override fun hideLoading() {
    }

    override fun onDestroy() {
        super.onDestroy()
        searchResultPresenter.onDestroy()
            mView?.search_result_content?.clearCache(false) //清空缓存
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                if ( mView?.search_result_main != null) {
//                    mView?.search_result_main.removeView(search_result_content)
//                }
                mView?.search_result_content?.stopLoading()
                mView?.search_result_content?.removeAllViews()
                //search_result_content.destroy();
            } else {
                mView?.search_result_content?.stopLoading()
                mView?.search_result_content?.removeAllViews()
                //search_result_content.destroy();
//                if (search_result_main != null) {
//                    search_result_main.removeView(search_result_content)
//                }
            }
    }

}