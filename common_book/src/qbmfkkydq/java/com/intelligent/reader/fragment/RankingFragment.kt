package com.intelligent.reader.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ding.basic.net.Config

import com.intelligent.reader.R
import kotlinx.android.synthetic.qbmfkkydq.frag_ranking_layout.*
import net.lzbook.kit.constants.ReplaceConstants

import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.web.WebResourceCache
import net.lzbook.kit.utils.web.WebViewIndex
import java.io.File

/**
 * Date: 2018/7/19 11:52
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 榜单Fragment
 */
class RankingFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.frag_ranking_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }


    private fun initView() {
        iv_search.setOnClickListener {
            RouterUtil.navigation(requireActivity(), RouterConfig.SEARCH_BOOK_ACTIVITY)
        }

        val webFragment = WebViewFragment()
        val bundle = Bundle()

        val webViewHost = Config.webViewBaseHost

        val filePath = webViewHost.replace(WebResourceCache.internetPath, ReplaceConstants.getReplaceConstants().APP_PATH_CACHE) + "/index.html"

        val localFileExist = File(filePath).exists()

        val url = if (localFileExist) {
            "file://$filePath${WebViewIndex.rank}"
        } else {
            Config.webViewBaseHost + "/index.html" +  WebViewIndex.rank
        }

        bundle.putString("url", url)
        webFragment.arguments = bundle

        childFragmentManager.beginTransaction().replace(R.id.fl_content, webFragment).commit()

    }
}