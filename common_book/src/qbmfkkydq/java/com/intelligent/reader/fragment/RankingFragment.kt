package com.intelligent.reader.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ding.basic.net.Config

import com.intelligent.reader.R
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.qbmfkkydq.frag_ranking_layout.*
import net.lzbook.kit.constants.ReplaceConstants

import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.web.WebResourceCache
import net.lzbook.kit.utils.web.WebViewIndex

/**
 * Date: 2018/7/19 11:52
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 榜单Fragment
 */
class RankingFragment : Fragment() {

    private var visibleState = false
    private var initializeState = false

    private val webViewFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()

        val bundle = Bundle()
        bundle.putString("url", loadChildViewBundleUrl(WebViewIndex.rank))

        fragment.arguments = bundle

        fragment
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.frag_ranking_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeView()

        initializeState = true
    }

    private fun initializeView() {
        iv_search.setOnClickListener {
            RouterUtil.navigation(requireActivity(), RouterConfig.SEARCH_BOOK_ACTIVITY)
        }

        childFragmentManager.beginTransaction().replace(R.id.fl_content, webViewFragment).commit()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        visibleState = isVisibleToUser

        checkViewVisibleState()
    }

    private fun checkViewVisibleState() {
        if (initializeState && visibleState) {
            webViewFragment.checkViewVisibleState()
        }
    }

    private fun loadChildViewBundleUrl(url: String): String {
        val webViewHost = Config.webViewBaseHost

        val filePath = webViewHost.replace(WebResourceCache.internetPath, ReplaceConstants.getReplaceConstants().APP_PATH_CACHE) + "/index.html"

        Logger.e("WebView地址: $webViewHost ${Config.webCacheAvailable}")

        return if (Config.webCacheAvailable) {
            "file://$filePath$url"
        } else {
            Config.webViewBaseHost + "/index.html" + url
        }
    }
}